package com.smarthire.service;

import com.smarthire.dto.ShortlistResponse;
import com.smarthire.model.*;
import com.smarthire.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ShortlistService - Orchestrates AI-powered candidate shortlisting
 * Connects to Python AI service for ATS analysis
 */
@Service
public class ShortlistService {

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private ShortlistedCandidateRepository shortlistedRepository;

    @Autowired
    private JobRepository jobRepository;

    @Value("${ai.service.url:http://localhost:5001}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Main method: Process all applications for a job using AI ATS
     */
    public ShortlistResponse processJobApplications(String jobId, Double minScore, Integer maxCandidates) {
        System.out.println("🤖 Starting ATS Shortlisting for Job: " + jobId);

        try {
            // 1. Get job details
            Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

            // 2. Get all submitted applications for this job
            List<JobApplication> applications = applicationRepository.findByJob(job);
            
            if (applications.isEmpty()) {
                return createEmptyResponse(jobId, job.getTitle(), "No applications found for this job");
            }

            System.out.println("📋 Found " + applications.size() + " applications to process");

            // 3. Call Python AI service for batch shortlisting
            Map<String, Object> aiRequest = new HashMap<>();
            aiRequest.put("job_description", job.getDescription());
            aiRequest.put("job_id", jobId);
            
            // Send request to Python AI service
            String aiUrl = aiServiceUrl + "/shortlist";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(aiRequest, headers);

            System.out.println("🐍 Calling Python AI Service: " + aiUrl);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> aiResponse = restTemplate.postForObject(aiUrl, entity, Map.class);

            if (aiResponse == null) {
                return createErrorResponse(jobId, job.getTitle(), "AI service returned null response");
            }

            // 4. Process AI results
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> shortlistData = (List<Map<String, Object>>) aiResponse.get("shortlist");
            
            if (shortlistData == null || shortlistData.isEmpty()) {
                return createEmptyResponse(jobId, job.getTitle(), "AI service found no suitable candidates");
            }

            System.out.println("✅ AI Service returned " + shortlistData.size() + " shortlisted candidates");

            // 5. Save shortlisted candidates to MongoDB
            List<ShortlistedCandidate> savedCandidates = new ArrayList<>();
            int rank = 1;
            
            // Apply filters if provided
            Double scoreThreshold = minScore != null ? minScore : 50.0;
            
            for (Map<String, Object> candidateData : shortlistData) {
                try {
                    String applicationId = (String) candidateData.get("application_id");
                    if (applicationId == null) {
                        applicationId = (String) candidateData.get("_id");
                    }
                    
                    Double finalScore = getDoubleValue(candidateData.get("final_score"));
                    if (finalScore == null) {
                        finalScore = getDoubleValue(candidateData.get("ats_score"));
                    }
                    
                    // Skip if below threshold
                    if (finalScore != null && finalScore < scoreThreshold) {
                        continue;
                    }
                    
                    // Check max candidates limit
                    if (maxCandidates != null && savedCandidates.size() >= maxCandidates) {
                        break;
                    }

                    // Check if already shortlisted
                    if (shortlistedRepository.existsByApplicationId(applicationId)) {
                        System.out.println("⚠️ Candidate already shortlisted: " + applicationId);
                        continue;
                    }

                    ShortlistedCandidate shortlisted = mapAIResponseToModel(candidateData, job, rank);
                    shortlisted.onCreate();
                    
                    ShortlistedCandidate saved = shortlistedRepository.save(shortlisted);
                    savedCandidates.add(saved);

                    // Update original application status
                    updateApplicationStatus(applicationId, JobApplication.ApplicationStatus.SHORTLISTED);

                    rank++;
                    System.out.println("✅ Saved shortlisted candidate #" + (rank-1) + ": " + shortlisted.getCandidateName());

                } catch (Exception e) {
                    System.err.println("❌ Error processing candidate: " + e.getMessage());
                }
            }

            // 6. Build response
            return buildSuccessResponse(savedCandidates, applications.size(), jobId, job.getTitle());

        } catch (Exception e) {
            System.err.println("❌ Error in shortlisting process: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse(jobId, "Unknown", "Error: " + e.getMessage());
        }
    }

    /**
     * Map AI service response to ShortlistedCandidate model
     */
    @SuppressWarnings("unchecked")
    private ShortlistedCandidate mapAIResponseToModel(Map<String, Object> aiData, Job job, int rank) {
        ShortlistedCandidate candidate = new ShortlistedCandidate();

        // Basic info
        candidate.setApplicationId((String) aiData.get("application_id"));
        if (candidate.getApplicationId() == null) {
            candidate.setApplicationId((String) aiData.get("_id"));
        }
        candidate.setJobId(job.getId());
        candidate.setJobTitle(job.getTitle());
        candidate.setCompany(job.getCompany());
        candidate.setRank(rank);

        // Candidate details
        candidate.setCandidateName((String) aiData.get("candidate_name"));
        candidate.setCandidateEmail((String) aiData.get("email"));
        candidate.setCandidatePhone((String) aiData.get("phone"));

        // Scores - try multiple field names
        candidate.setFinalScore(getDoubleValue(aiData.get("final_score"), aiData.get("ats_score"), aiData.get("final_ats_score")));
        
        // Component scores
        Map<String, Object> componentScores = (Map<String, Object>) aiData.get("component_scores");
        if (componentScores != null) {
            candidate.setSemanticSimilarity(getDoubleValue(componentScores.get("semantic_similarity")));
            candidate.setSkillMatchPercentage(getDoubleValue(componentScores.get("skill_match")));
            candidate.setExperienceMatch(getDoubleValue(componentScores.get("experience_match")));
            candidate.setEducationMatch(getDoubleValue(componentScores.get("education_match")));
            candidate.setLlmScore(getDoubleValue(componentScores.get("llm_score")));
        }

        // Skills
        Map<String, Object> skillAnalysis = (Map<String, Object>) aiData.get("skill_analysis");
        if (skillAnalysis != null) {
            candidate.setMatchedSkills((List<String>) skillAnalysis.get("matching_skills"));
            candidate.setMissingSkills((List<String>) skillAnalysis.get("missing_skills"));
            candidate.setSkillMatchPercentage(getDoubleValue(skillAnalysis.get("match_percentage")));
        }

        // LLM Evaluation
        Map<String, Object> llmEval = (Map<String, Object>) aiData.get("llm_evaluation");
        if (llmEval != null) {
            candidate.setLlmDecision((String) llmEval.get("decision"));
            candidate.setLlmReasoning((String) llmEval.get("reasoning"));
            candidate.setInterviewRecommendation((String) llmEval.get("interview_recommendation"));
            candidate.setKeyStrengths((String) llmEval.get("key_strengths"));
            candidate.setDevelopmentAreas((String) llmEval.get("development_areas"));
        }

        return candidate;
    }

    /**
     * Helper to safely get Double value from various types
     */
    private Double getDoubleValue(Object... values) {
        for (Object value : values) {
            if (value == null) continue;
            
            if (value instanceof Double) {
                return (Double) value;
            } else if (value instanceof Integer) {
                return ((Integer) value).doubleValue();
            } else if (value instanceof Float) {
                return ((Float) value).doubleValue();
            } else if (value instanceof String) {
                try {
                    return Double.parseDouble((String) value);
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return null;
    }

    /**
     * Update application status
     */
    private void updateApplicationStatus(String applicationId, JobApplication.ApplicationStatus status) {
        try {
            Optional<JobApplication> appOpt = applicationRepository.findById(applicationId);
            if (appOpt.isPresent()) {
                JobApplication app = appOpt.get();
                app.setStatus(status);
                app.onUpdate();
                applicationRepository.save(app);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not update application status: " + e.getMessage());
        }
    }

    /**
     * Build success response
     */
    private ShortlistResponse buildSuccessResponse(List<ShortlistedCandidate> candidates, int totalProcessed, String jobId, String jobTitle) {
        ShortlistResponse response = new ShortlistResponse();
        response.setSuccess(true);
        response.setMessage("Successfully shortlisted " + candidates.size() + " candidates");
        response.setTotalProcessed(totalProcessed);
        response.setShortlistedCount(candidates.size());
        response.setRejectedCount(totalProcessed - candidates.size());
        response.setJobId(jobId);
        response.setJobTitle(jobTitle);

        List<ShortlistResponse.ShortlistedCandidateDTO> dtos = candidates.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        response.setShortlistedCandidates(dtos);

        return response;
    }

    /**
     * Convert entity to DTO
     */
    private ShortlistResponse.ShortlistedCandidateDTO convertToDTO(ShortlistedCandidate candidate) {
        ShortlistResponse.ShortlistedCandidateDTO dto = new ShortlistResponse.ShortlistedCandidateDTO();
        dto.setId(candidate.getId());
        dto.setApplicationId(candidate.getApplicationId());
        dto.setCandidateName(candidate.getCandidateName());
        dto.setCandidateEmail(candidate.getCandidateEmail());
        dto.setFinalScore(candidate.getFinalScore());
        dto.setSkillMatchPercentage(candidate.getSkillMatchPercentage());
        dto.setMatchedSkills(candidate.getMatchedSkills());
        dto.setMissingSkills(candidate.getMissingSkills());
        dto.setLlmDecision(candidate.getLlmDecision());
        dto.setLlmReasoning(candidate.getLlmReasoning());
        dto.setRank(candidate.getRank());
        dto.setStatus(candidate.getStatus().toString());
        return dto;
    }

    /**
     * Create empty response
     */
    private ShortlistResponse createEmptyResponse(String jobId, String jobTitle, String message) {
        ShortlistResponse response = new ShortlistResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setTotalProcessed(0);
        response.setShortlistedCount(0);
        response.setRejectedCount(0);
        response.setJobId(jobId);
        response.setJobTitle(jobTitle);
        response.setShortlistedCandidates(new ArrayList<>());
        return response;
    }

    /**
     * Create error response
     */
    private ShortlistResponse createErrorResponse(String jobId, String jobTitle, String message) {
        ShortlistResponse response = new ShortlistResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setJobId(jobId);
        response.setJobTitle(jobTitle);
        response.setShortlistedCandidates(new ArrayList<>());
        return response;
    }

    /**
     * Get shortlisted candidates for a job
     */
    public List<ShortlistedCandidate> getShortlistedCandidates(String jobId) {
        return shortlistedRepository.findByJobIdOrderByFinalScoreDesc(jobId);
    }

    /**
     * Get shortlisted candidate by ID
     */
    public Optional<ShortlistedCandidate> getShortlistedCandidateById(String id) {
        return shortlistedRepository.findById(id);
    }

    /**
     * Update shortlisted candidate status
     */
    public ShortlistedCandidate updateStatus(String id, String statusStr) {
        ShortlistedCandidate candidate = shortlistedRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Shortlisted candidate not found"));
        
        candidate.onUpdate();
        
        return shortlistedRepository.save(candidate);
    }
}
