package com.smarthire.service;

import com.smarthire.dto.OfferLetterRequest;
import com.smarthire.dto.OfferLetterResponse;
import com.smarthire.model.CandidateApplication;
import com.smarthire.model.Interview;
import com.smarthire.model.Job;
import com.smarthire.model.OfferLetter;
import com.smarthire.model.ShortlistedCandidate;
import com.smarthire.repository.CandidateApplicationRepository;
import com.smarthire.repository.InterviewRepository;
import com.smarthire.repository.JobRepository;
import com.smarthire.repository.OfferLetterRepository;
import com.smarthire.repository.ShortlistedCandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfferLetterService {
    
    private final OfferLetterRepository offerLetterRepository;
    private final InterviewRepository interviewRepository;
    private final ShortlistedCandidateRepository shortlistedCandidateRepository;
    private final CandidateApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    
    /**
     * Generate offer letter for candidate who cleared all interviews
     */
    public OfferLetterResponse generateOfferLetter(OfferLetterRequest request) {
        try {
            // Validate that candidate cleared HR round
            Interview hrInterview = interviewRepository.findById(request.getInterviewId())
                    .orElseThrow(() -> new RuntimeException("Interview not found"));
            
            if (!"HR_ROUND".equals(hrInterview.getInterviewRound())) {
                throw new RuntimeException("Candidate has not completed HR round");
            }
            
            if (!"SELECTED".equals(hrInterview.getDecision())) {
                throw new RuntimeException("Candidate not selected in HR round");
            }
            
            // Check if offer letter already exists
            if (offerLetterRepository.findByApplicationId(request.getApplicationId()).isPresent()) {
                throw new RuntimeException("Offer letter already generated for this candidate");
            }
            
            // Get candidate details
            ShortlistedCandidate candidate = shortlistedCandidateRepository
                    .findById(request.getCandidateId())
                    .orElseThrow(() -> new RuntimeException("Candidate not found"));
            
            // Get application details
            CandidateApplication application = applicationRepository
                    .findById(request.getApplicationId())
                    .orElseThrow(() -> new RuntimeException("Application not found"));
            
            // Get job details
            Job job = jobRepository.findById(request.getJobId())
                    .orElseThrow(() -> new RuntimeException("Job not found"));
            
            // Create offer letter
            OfferLetter offerLetter = new OfferLetter();
            offerLetter.setCandidateId(request.getCandidateId());
            offerLetter.setApplicationId(request.getApplicationId());
            offerLetter.setJobId(request.getJobId());
            offerLetter.setInterviewId(request.getInterviewId());
            
            // Candidate information
            offerLetter.setCandidateName(candidate.getCandidateName());
            offerLetter.setCandidateEmail(candidate.getCandidateEmail());
            offerLetter.setCandidatePhone(application.getPhone());
            
            // Job information
            offerLetter.setJobTitle(job.getTitle());
            offerLetter.setDepartment(null); // Department not available in Job model
            offerLetter.setCompany(job.getCompany());
            
            // Offer details
            offerLetter.setOfferLetterNumber("OL-" + System.currentTimeMillis());
            offerLetter.setOfferDate(LocalDate.now());
            offerLetter.setJoiningDate(request.getJoiningDate());
            offerLetter.setEmploymentType(request.getEmploymentType() != null ? request.getEmploymentType() : "FULL_TIME");
            offerLetter.setWorkLocation(request.getWorkLocation() != null ? request.getWorkLocation() : job.getLocation());
            offerLetter.setOfficeLocation(request.getOfficeLocation() != null ? request.getOfficeLocation() : job.getLocation());
            
            // Compensation
            offerLetter.setAnnualCtc(request.getAnnualCtc());
            offerLetter.setBasicSalary(request.getBasicSalary());
            offerLetter.setHra(request.getHra());
            offerLetter.setSpecialAllowance(request.getSpecialAllowance());
            offerLetter.setPerformanceBonus(request.getPerformanceBonus());
            offerLetter.setOtherAllowances(request.getOtherAllowances());
            offerLetter.setCurrency("INR");
            
            // Benefits
            offerLetter.setBenefits(request.getBenefits());
            offerLetter.setPaidLeaves(request.getPaidLeaves() != null ? request.getPaidLeaves() : 24);
            offerLetter.setAdditionalBenefits(request.getAdditionalBenefits());
            
            // Terms
            offerLetter.setProbationPeriod(request.getProbationPeriod() != null ? request.getProbationPeriod() : 3);
            offerLetter.setNoticePeriod(request.getNoticePeriod() != null ? request.getNoticePeriod() : 30);
            offerLetter.setReportingManager(request.getReportingManager());
            offerLetter.setReportingManagerDesignation(request.getReportingManagerDesignation());
            
            // Status
            offerLetter.setStatus("GENERATED");
            offerLetter.setGeneratedAt(LocalDateTime.now());
            LocalDate expiryDateValue = request.getExpiryDate() != null ? request.getExpiryDate() : LocalDate.now().plusDays(15);
            offerLetter.setExpiryDate(expiryDateValue);
            
            // Additional
            offerLetter.setAdditionalNotes(request.getAdditionalNotes());
            offerLetter.setGeneratedBy(request.getGeneratedBy());
            offerLetter.setApprovedBy(request.getApprovedBy());
            offerLetter.setIsAccepted(false);
            offerLetter.setIsDownloaded(false);
            offerLetter.setDownloadCount(0);
            offerLetter.setUpdatedAt(LocalDateTime.now());
            
            // Save offer letter
            OfferLetter saved = offerLetterRepository.save(offerLetter);
            
            // Update candidate status in shortlisted collection
            candidate.setOfferLetterGenerated(true);
            candidate.setOfferLetterId(saved.getId());
            candidate.setStatus(ShortlistedCandidate.ShortlistStatus.OFFER_LETTER_SENT);
            shortlistedCandidateRepository.save(candidate);
            
            return mapToResponse(saved, "Offer letter generated successfully");
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate offer letter: " + e.getMessage());
        }
    }
    
    /**
     * Get all offer letters
     */
    public List<OfferLetterResponse> getAllOfferLetters() {
        return offerLetterRepository.findAll().stream()
                .map(ol -> mapToResponse(ol, null))
                .collect(Collectors.toList());
    }
    
    /**
     * Get offer letter by ID
     */
    public OfferLetterResponse getOfferLetterById(String id) {
        OfferLetter offerLetter = offerLetterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer letter not found"));
        return mapToResponse(offerLetter, null);
    }
    
    /**
     * Get offer letter by candidate ID
     */
    public OfferLetterResponse getOfferLetterByCandidateId(String candidateId) {
        OfferLetter offerLetter = offerLetterRepository.findByCandidateId(candidateId)
                .orElseThrow(() -> new RuntimeException("No offer letter found for this candidate"));
        return mapToResponse(offerLetter, null);
    }
    
    /**
     * Get offer letters by status
     */
    public List<OfferLetterResponse> getOfferLettersByStatus(String status) {
        return offerLetterRepository.findByStatus(status).stream()
                .map(ol -> mapToResponse(ol, null))
                .collect(Collectors.toList());
    }
    
    /**
     * Send offer letter to candidate
     */
    public OfferLetterResponse sendOfferLetter(String id) {
        OfferLetter offerLetter = offerLetterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer letter not found"));
        
        offerLetter.setStatus("SENT");
        offerLetter.setSentAt(LocalDateTime.now());
        offerLetter.setUpdatedAt(LocalDateTime.now());
        
        OfferLetter updated = offerLetterRepository.save(offerLetter);
        
        // TODO: Send email to candidate with offer letter
        
        return mapToResponse(updated, "Offer letter sent successfully");
    }
    
    /**
     * Accept offer letter
     */
    public OfferLetterResponse acceptOfferLetter(String id, String acceptanceMethod) {
        OfferLetter offerLetter = offerLetterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer letter not found"));
        
        if (!"SENT".equals(offerLetter.getStatus())) {
            throw new RuntimeException("Offer letter must be in SENT status to accept");
        }
        
        offerLetter.setStatus("ACCEPTED");
        offerLetter.setIsAccepted(true);
        offerLetter.setRespondedAt(LocalDateTime.now());
        offerLetter.setAcceptanceMethod(acceptanceMethod != null ? acceptanceMethod : "PORTAL");
        offerLetter.setUpdatedAt(LocalDateTime.now());
        
        OfferLetter updated = offerLetterRepository.save(offerLetter);
        
        // Update candidate status
        ShortlistedCandidate candidate = shortlistedCandidateRepository
                .findById(offerLetter.getCandidateId())
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
        candidate.setStatus(ShortlistedCandidate.ShortlistStatus.OFFER_ACCEPTED);
        shortlistedCandidateRepository.save(candidate);
        
        return mapToResponse(updated, "Offer letter accepted successfully");
    }
    
    /**
     * Reject offer letter
     */
    public OfferLetterResponse rejectOfferLetter(String id, String rejectionReason) {
        OfferLetter offerLetter = offerLetterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer letter not found"));
        
        offerLetter.setStatus("REJECTED");
        offerLetter.setIsAccepted(false);
        offerLetter.setRespondedAt(LocalDateTime.now());
        offerLetter.setRejectionReason(rejectionReason);
        offerLetter.setUpdatedAt(LocalDateTime.now());
        
        OfferLetter updated = offerLetterRepository.save(offerLetter);
        
        // Update candidate status
        ShortlistedCandidate candidate = shortlistedCandidateRepository
                .findById(offerLetter.getCandidateId())
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
        candidate.setStatus(ShortlistedCandidate.ShortlistStatus.OFFER_REJECTED);
        shortlistedCandidateRepository.save(candidate);
        
        return mapToResponse(updated, "Offer letter rejected");
    }
    
    /**
     * Download offer letter (increment download count)
     */
    public OfferLetterResponse downloadOfferLetter(String id) {
        OfferLetter offerLetter = offerLetterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer letter not found"));
        
        offerLetter.setIsDownloaded(true);
        offerLetter.setDownloadCount(offerLetter.getDownloadCount() + 1);
        offerLetter.setUpdatedAt(LocalDateTime.now());
        
        OfferLetter updated = offerLetterRepository.save(offerLetter);
        
        return mapToResponse(updated, "Offer letter downloaded");
    }
    
    /**
     * Get candidates eligible for offer letter (cleared HR round)
     */
    public List<ShortlistedCandidate> getCandidatesEligibleForOfferLetter() {
        // Find all HR round interviews with SELECTED decision
        List<Interview> hrInterviews = interviewRepository
                .findByInterviewRoundAndDecision("HR_ROUND", "SELECTED");
        
        // Get shortlisted candidates who don't have offer letter yet
        return hrInterviews.stream()
                .map(interview -> shortlistedCandidateRepository
                        .findById(interview.getShortlistedCandidateId())
                        .orElse(null))
                .filter(candidate -> candidate != null && 
                        (candidate.getOfferLetterGenerated() == null || !candidate.getOfferLetterGenerated()))
                .collect(Collectors.toList());
    }
    
    /**
     * Helper method to map OfferLetter to Response
     */
    private OfferLetterResponse mapToResponse(OfferLetter offerLetter, String message) {
        OfferLetterResponse response = new OfferLetterResponse();
        response.setId(offerLetter.getId());
        response.setOfferLetterNumber(offerLetter.getOfferLetterNumber());
        response.setCandidateName(offerLetter.getCandidateName());
        response.setCandidateEmail(offerLetter.getCandidateEmail());
        response.setJobTitle(offerLetter.getJobTitle());
        response.setCompany(offerLetter.getCompany());
        response.setOfferDate(offerLetter.getOfferDate());
        response.setJoiningDate(offerLetter.getJoiningDate());
        response.setEmploymentType(offerLetter.getEmploymentType());
        response.setWorkLocation(offerLetter.getWorkLocation());
        response.setAnnualCtc(offerLetter.getAnnualCtc());
        response.setCurrency(offerLetter.getCurrency());
        response.setStatus(offerLetter.getStatus());
        response.setGeneratedAt(offerLetter.getGeneratedAt());
        response.setSentAt(offerLetter.getSentAt());
        response.setIsAccepted(offerLetter.getIsAccepted());
        response.setOfferLetterPdfUrl(offerLetter.getOfferLetterPdfUrl());
        response.setIsDownloaded(offerLetter.getIsDownloaded());
        response.setMessage(message);
        return response;
    }
}
