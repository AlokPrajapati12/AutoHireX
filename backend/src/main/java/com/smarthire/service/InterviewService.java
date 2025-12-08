package com.smarthire.service;

import com.smarthire.dto.BatchInterviewScheduleRequest;
import com.smarthire.dto.InterviewFeedbackRequest;
import com.smarthire.dto.InterviewResponse;
import com.smarthire.dto.ScheduleInterviewRequest;
import com.smarthire.model.JobApplication;
import com.smarthire.model.Interview;
import com.smarthire.model.Job;
import com.smarthire.model.ShortlistedCandidate;
import com.smarthire.repository.JobApplicationRepository;
import com.smarthire.repository.InterviewRepository;
import com.smarthire.repository.JobRepository;
import com.smarthire.repository.ShortlistedCandidateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InterviewService {
    
    @Autowired
    private InterviewRepository interviewRepository;
    
    @Autowired
    private ShortlistedCandidateRepository shortlistedCandidateRepository;
    
    @Autowired
    private JobApplicationRepository applicationRepository;
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Schedule interviews for multiple candidates (batch scheduling)
     * Supports both MANUAL and VOICE_AI interview types
     */
    public Map<String, Object> scheduleBatchInterviews(BatchInterviewScheduleRequest request) {
        try {
            log.info("🚀 Starting batch interview scheduling for {} candidates", request.getCandidates().size());
            log.info("Interview Type: {}, Date: {}, Time: {}", 
                request.getInterviewType(), request.getScheduledDate(), request.getScheduledTime());
            
            // Get job details
            Optional<Job> jobOpt = jobRepository.findById(request.getJobId());
            if (!jobOpt.isPresent()) {
                return Map.of(
                    "success", false,
                    "message", "Job not found"
                );
            }
            Job job = jobOpt.get();
            
            List<Interview> scheduledInterviews = new ArrayList<>();
            List<String> successEmails = new ArrayList<>();
            List<String> failedEmails = new ArrayList<>();
            
            // Schedule interview for each candidate
            for (BatchInterviewScheduleRequest.CandidateInfo candidateInfo : request.getCandidates()) {
                try {
                    // Get application details
                    Optional<JobApplication> appOpt = applicationRepository.findById(candidateInfo.getApplicationId());
                    if (!appOpt.isPresent()) {
                        log.warn("Application not found: {}", candidateInfo.getApplicationId());
                        failedEmails.add(candidateInfo.getCandidateEmail());
                        continue;
                    }
                    JobApplication application = appOpt.get();
                    
                    // Create interview record
                    Interview interview = new Interview();
                    interview.setApplicationId(candidateInfo.getApplicationId());
                    interview.setJobId(request.getJobId());
                    interview.setCandidateName(candidateInfo.getCandidateName());
                    interview.setCandidateEmail(candidateInfo.getCandidateEmail());
                    interview.setCandidatePhone(application.getCandidatePhone());
                    interview.setJobTitle(job.getTitle());
                    interview.setCompany(job.getCompany());
                    
                    // Interview scheduling details
                    interview.setScheduledDate(request.getScheduledDate());
                    interview.setScheduledTime(request.getScheduledTime());
                    interview.setInterviewMode(request.getInterviewMode());
                    interview.setMeetingLink(request.getMeetingLink());
                    interview.setVenue(request.getVenue());
                    interview.setNotes(request.getAdditionalNotes());
                    
                    // Set interview type and round
                    if ("VOICE_AI".equals(request.getInterviewType())) {
                        interview.setInterviewRound("AI_VOICE_ROUND");
                        interview.setRoundNumber(1);
                        interview.setIsLastRound(false); // AI has 3 rounds
                    } else {
                        interview.setInterviewRound("ROUND_1");
                        interview.setRoundNumber(1);
                        interview.setIsLastRound(false);
                        interview.setInterviewerNames(request.getInterviewerNames());
                        interview.setInterviewerEmails(request.getInterviewerEmails());
                    }
                    
                    // Set status and timestamps
                    interview.setStatus("SCHEDULED");
                    interview.setCreatedAt(LocalDateTime.now());
                    interview.setUpdatedAt(LocalDateTime.now());
                    interview.setNotificationSent(false);
                    interview.setReminderSent(false);
                    
                    // Save interview
                    Interview savedInterview = interviewRepository.save(interview);
                    log.info("✅ Interview created: {} for {}", savedInterview.getId(), candidateInfo.getCandidateName());
                    
                    // Send email notification
                    boolean emailSent = emailService.sendInterviewInvitation(savedInterview, request.getInterviewType());
                    
                    if (emailSent) {
                        savedInterview.setNotificationSent(true);
                        interviewRepository.save(savedInterview);
                        successEmails.add(candidateInfo.getCandidateEmail());
                        log.info("📧 Email sent successfully to: {}", candidateInfo.getCandidateEmail());
                    } else {
                        log.warn("⚠️ Email failed for: {}", candidateInfo.getCandidateEmail());
                        failedEmails.add(candidateInfo.getCandidateEmail());
                    }
                    
                    scheduledInterviews.add(savedInterview);
                    
                } catch (Exception e) {
                    log.error("❌ Error scheduling interview for {}: ", candidateInfo.getCandidateEmail(), e);
                    failedEmails.add(candidateInfo.getCandidateEmail());
                }
            }
            
            log.info("✅ Batch scheduling complete: {} scheduled, {} failed", 
                scheduledInterviews.size(), failedEmails.size());
            
            return Map.of(
                "success", true,
                "message", String.format("Successfully scheduled %d out of %d interviews",
                    scheduledInterviews.size(), request.getCandidates().size()),
                "totalScheduled", scheduledInterviews.size(),
                "totalFailed", failedEmails.size(),
                "successEmails", successEmails,
                "failedEmails", failedEmails,
                "scheduledInterviews", scheduledInterviews.stream()
                    .map(i -> Map.of(
                        "interviewId", i.getId(),
                        "candidateName", i.getCandidateName(),
                        "candidateEmail", i.getCandidateEmail(),
                        "status", i.getStatus()
                    ))
                    .collect(Collectors.toList())
            );
            
        } catch (Exception e) {
            log.error("❌ Batch scheduling failed: ", e);
            return Map.of(
                "success", false,
                "message", "Error during batch scheduling: " + e.getMessage()
            );
        }
    }
    
    /**
     * Schedule a new interview for a shortlisted candidate
     */
    public InterviewResponse scheduleInterview(ScheduleInterviewRequest request) {
        try {
            log.info("Scheduling interview for candidate: {}", request.getShortlistedCandidateId());
            
            // Get shortlisted candidate details
            Optional<ShortlistedCandidate> candidateOpt = 
                shortlistedCandidateRepository.findById(request.getShortlistedCandidateId());
            
            if (!candidateOpt.isPresent()) {
                return new InterviewResponse(false, "Shortlisted candidate not found", (Interview) null);
            }
            
            ShortlistedCandidate candidate = candidateOpt.get();
            
            // Create interview
            Interview interview = new Interview();
            interview.setShortlistedCandidateId(request.getShortlistedCandidateId());
            interview.setApplicationId(candidate.getApplicationId());
            interview.setJobId(candidate.getJobId());
            interview.setCandidateName(candidate.getCandidateName());
            interview.setCandidateEmail(candidate.getCandidateEmail());
            interview.setCandidatePhone(candidate.getCandidatePhone());
            interview.setJobTitle(candidate.getJobTitle());
            interview.setCompany(candidate.getCompany());
            
            // Interview details
            interview.setInterviewRound(request.getInterviewRound());
            interview.setScheduledDate(request.getScheduledDate());
            interview.setScheduledTime(request.getScheduledTime());
            interview.setInterviewMode(request.getInterviewMode());
            interview.setMeetingLink(request.getMeetingLink());
            interview.setVenue(request.getVenue());
            
            // Interviewer details
            interview.setInterviewerNames(request.getInterviewerNames());
            interview.setInterviewerEmails(request.getInterviewerEmails());
            interview.setInterviewPanel(request.getInterviewPanel());
            
            // Status
            interview.setStatus("SCHEDULED");
            interview.setCreatedAt(LocalDateTime.now());
            interview.setUpdatedAt(LocalDateTime.now());
            interview.setNotes(request.getNotes());
            interview.setNotificationSent(false);
            interview.setReminderSent(false);
            
            // Set round number and isLastRound
            interview.setRoundNumber(getRoundNumber(request.getInterviewRound()));
            interview.setIsLastRound(request.getInterviewRound().equals("HR_ROUND"));
            
            // Save interview
            Interview savedInterview = interviewRepository.save(interview);
            
            // Update shortlisted candidate status
            candidate.setInterviewScheduled(true);
            candidate.setInterviewId(savedInterview.getId());
            candidate.setInterviewDate(request.getScheduledDate());
            candidate.setStatus(ShortlistedCandidate.ShortlistStatus.INTERVIEW_SCHEDULED);
            candidate.setUpdatedAt(LocalDateTime.now());
            shortlistedCandidateRepository.save(candidate);
            
            // Send email notification if requested
            if (request.getSendNotification() != null && request.getSendNotification()) {
                emailService.sendInterviewInvitation(savedInterview, "MANUAL");
                savedInterview.setNotificationSent(true);
                interviewRepository.save(savedInterview);
            }
            
            log.info("Interview scheduled successfully: {}", savedInterview.getId());
            return new InterviewResponse(true, "Interview scheduled successfully", savedInterview);
            
        } catch (Exception e) {
            log.error("Error scheduling interview: ", e);
            return new InterviewResponse(false, "Error scheduling interview: " + e.getMessage(), (Interview) null);
        }
    }
    
    /**
     * Get all interviews for a job
     */
    public InterviewResponse getInterviewsByJob(String jobId) {
        try {
            List<Interview> interviews = interviewRepository.findByJobId(jobId);
            
            Long scheduled = interviewRepository.countByJobIdAndStatus(jobId, "SCHEDULED");
            Long completed = interviewRepository.countByJobIdAndStatus(jobId, "COMPLETED");
            Long cancelled = interviewRepository.countByJobIdAndStatus(jobId, "CANCELLED");
            
            InterviewResponse response = new InterviewResponse(true, "Interviews fetched successfully", interviews);
            response.setTotalScheduled(scheduled);
            response.setTotalCompleted(completed);
            response.setTotalCancelled(cancelled);
            
            return response;
        } catch (Exception e) {
            log.error("Error fetching interviews: ", e);
            return new InterviewResponse(false, "Error fetching interviews", List.of());
        }
    }
    
    /**
     * Get interviews for a specific candidate
     */
    public InterviewResponse getInterviewsByCandidate(String applicationId) {
        try {
            List<Interview> interviews = interviewRepository.findByApplicationId(applicationId);
            return new InterviewResponse(true, "Interviews fetched successfully", interviews);
        } catch (Exception e) {
            log.error("Error fetching candidate interviews: ", e);
            return new InterviewResponse(false, "Error fetching interviews", List.of());
        }
    }
    
    /**
     * Get a specific interview
     */
    public InterviewResponse getInterview(String interviewId) {
        try {
            Optional<Interview> interview = interviewRepository.findById(interviewId);
            if (interview.isPresent()) {
                return new InterviewResponse(true, "Interview found", interview.get());
            }
            return new InterviewResponse(false, "Interview not found", (Interview) null);
        } catch (Exception e) {
            log.error("Error fetching interview: ", e);
            return new InterviewResponse(false, "Error fetching interview", (Interview) null);
        }
    }
    
    /**
     * Update interview status
     */
    public InterviewResponse updateInterviewStatus(String interviewId, String status) {
        try {
            Optional<Interview> interviewOpt = interviewRepository.findById(interviewId);
            if (!interviewOpt.isPresent()) {
                return new InterviewResponse(false, "Interview not found", (Interview) null);
            }
            
            Interview interview = interviewOpt.get();
            interview.setStatus(status);
            interview.setUpdatedAt(LocalDateTime.now());
            
            Interview updated = interviewRepository.save(interview);
            return new InterviewResponse(true, "Interview status updated", updated);
        } catch (Exception e) {
            log.error("Error updating interview status: ", e);
            return new InterviewResponse(false, "Error updating status", (Interview) null);
        }
    }
    
    /**
     * Submit interview feedback and decision
     */
    public InterviewResponse submitInterviewFeedback(InterviewFeedbackRequest request) {
        try {
            log.info("Submitting feedback for interview: {}", request.getInterviewId());
            
            Optional<Interview> interviewOpt = interviewRepository.findById(request.getInterviewId());
            if (!interviewOpt.isPresent()) {
                return new InterviewResponse(false, "Interview not found", (Interview) null);
            }
            
            Interview interview = interviewOpt.get();
            
            // Update feedback
            interview.setFeedback(request.getFeedback());
            interview.setTechnicalScore(request.getTechnicalScore());
            interview.setCommunicationScore(request.getCommunicationScore());
            interview.setOverallRating(request.getOverallRating());
            interview.setDecision(request.getDecision());
            interview.setInterviewerRemarks(request.getInterviewerRemarks());
            interview.setStatus("COMPLETED");
            interview.setUpdatedAt(LocalDateTime.now());
            
            Interview savedInterview = interviewRepository.save(interview);
            
            // Handle decision
            if ("NEXT_ROUND".equals(request.getDecision()) && request.getNextRound() != null) {
                scheduleNextRound(interview, request.getNextRound());
            } else if ("SELECTED".equals(request.getDecision()) && interview.getIsLastRound()) {
                updateCandidateToOfferStage(interview);
            } else if ("REJECTED".equals(request.getDecision())) {
                updateCandidateStatus(interview, "REJECTED");
            }
            
            log.info("Interview feedback submitted successfully");
            return new InterviewResponse(true, "Feedback submitted successfully", savedInterview);
            
        } catch (Exception e) {
            log.error("Error submitting feedback: ", e);
            return new InterviewResponse(false, "Error submitting feedback: " + e.getMessage(), (Interview) null);
        }
    }
    
    /**
     * Schedule next round interview
     */
    private void scheduleNextRound(Interview currentInterview, String nextRound) {
        try {
            log.info("Scheduling next round: {} for candidate: {}", nextRound, currentInterview.getCandidateName());
            
            Optional<ShortlistedCandidate> candidateOpt = 
                shortlistedCandidateRepository.findById(currentInterview.getShortlistedCandidateId());
            
            if (candidateOpt.isPresent()) {
                ShortlistedCandidate candidate = candidateOpt.get();
                candidate.setStatus(ShortlistedCandidate.ShortlistStatus.MOVED_TO_NEXT_ROUND);
                candidate.setNotes("Cleared " + currentInterview.getInterviewRound() + ", proceeding to " + nextRound);
                shortlistedCandidateRepository.save(candidate);
            }
            
        } catch (Exception e) {
            log.error("Error scheduling next round: ", e);
        }
    }
    
    /**
     * Update candidate to offer letter stage
     */
    private void updateCandidateToOfferStage(Interview interview) {
        try {
            Optional<ShortlistedCandidate> candidateOpt = 
                shortlistedCandidateRepository.findById(interview.getShortlistedCandidateId());
            
            if (candidateOpt.isPresent()) {
                ShortlistedCandidate candidate = candidateOpt.get();
                candidate.setStatus(ShortlistedCandidate.ShortlistStatus.INTERVIEW_COMPLETED);
                candidate.setNotes("Cleared all interview rounds");
                shortlistedCandidateRepository.save(candidate);
            }
        } catch (Exception e) {
            log.error("Error updating candidate to offer stage: ", e);
        }
    }
    
    /**
     * Update candidate status
     */
    private void updateCandidateStatus(Interview interview, String status) {
        try {
            Optional<ShortlistedCandidate> candidateOpt = 
                shortlistedCandidateRepository.findById(interview.getShortlistedCandidateId());
            
            if (candidateOpt.isPresent()) {
                ShortlistedCandidate candidate = candidateOpt.get();
                candidate.setStatus(ShortlistedCandidate.ShortlistStatus.REJECTED);
                shortlistedCandidateRepository.save(candidate);
            }
        } catch (Exception e) {
            log.error("Error updating candidate status: ", e);
        }
    }
    
    /**
     * Get round number from round name
     */
    private Integer getRoundNumber(String roundName) {
        switch (roundName) {
            case "ROUND_1": return 1;
            case "ROUND_2": return 2;
            case "HR_ROUND": return 3;
            case "AI_VOICE_ROUND": return 1;
            default: return 0;
        }
    }
    
    /**
     * Reschedule interview
     */
    public InterviewResponse rescheduleInterview(String interviewId, LocalDateTime newDate, String newTime) {
        try {
            Optional<Interview> interviewOpt = interviewRepository.findById(interviewId);
            if (!interviewOpt.isPresent()) {
                return new InterviewResponse(false, "Interview not found", (Interview) null);
            }
            
            Interview interview = interviewOpt.get();
            interview.setScheduledDate(newDate);
            interview.setScheduledTime(newTime);
            interview.setStatus("RESCHEDULED");
            interview.setUpdatedAt(LocalDateTime.now());
            
            Interview updated = interviewRepository.save(interview);
            
            // Send rescheduling notification
            emailService.sendInterviewInvitation(updated, "MANUAL");
            updated.setNotificationSent(true);
            interviewRepository.save(updated);
            
            return new InterviewResponse(true, "Interview rescheduled successfully", updated);
        } catch (Exception e) {
            log.error("Error rescheduling interview: ", e);
            return new InterviewResponse(false, "Error rescheduling interview", (Interview) null);
        }
    }
    
    /**
     * Cancel interview
     */
    public InterviewResponse cancelInterview(String interviewId, String reason) {
        try {
            Optional<Interview> interviewOpt = interviewRepository.findById(interviewId);
            if (!interviewOpt.isPresent()) {
                return new InterviewResponse(false, "Interview not found", (Interview) null);
            }
            
            Interview interview = interviewOpt.get();
            interview.setStatus("CANCELLED");
            interview.setNotes("Cancelled: " + reason);
            interview.setUpdatedAt(LocalDateTime.now());
            
            Interview updated = interviewRepository.save(interview);
            return new InterviewResponse(true, "Interview cancelled", updated);
        } catch (Exception e) {
            log.error("Error cancelling interview: ", e);
            return new InterviewResponse(false, "Error cancelling interview", (Interview) null);
        }
    }
}
