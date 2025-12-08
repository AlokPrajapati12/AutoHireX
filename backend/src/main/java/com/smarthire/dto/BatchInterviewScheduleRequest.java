package com.smarthire.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for batch scheduling interviews for multiple candidates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchInterviewScheduleRequest {
    
    /**
     * Job ID
     */
    private String jobId;
    
    /**
     * List of candidates to schedule interviews for
     */
    private List<CandidateInfo> candidates;
    
    /**
     * Type of interview: MANUAL or VOICE_AI
     */
    private String interviewType;
    
    /**
     * Scheduled date for interviews
     */
    private LocalDateTime scheduledDate;
    
    /**
     * Scheduled time (e.g., "10:00 AM")
     */
    private String scheduledTime;
    
    /**
     * Additional notes or instructions
     */
    private String additionalNotes;
    
    /**
     * Interview mode: ONLINE, OFFLINE, or PHONE
     */
    private String interviewMode = "ONLINE";
    
    /**
     * Meeting link (for online interviews)
     */
    private String meetingLink;
    
    /**
     * Venue (for offline interviews)
     */
    private String venue;
    
    /**
     * Interviewer details (for manual interviews)
     */
    private List<String> interviewerNames;
    private List<String> interviewerEmails;
    
    /**
     * Candidate information nested class
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandidateInfo {
        private String applicationId;
        private String candidateEmail;
        private String candidateName;
    }
}
