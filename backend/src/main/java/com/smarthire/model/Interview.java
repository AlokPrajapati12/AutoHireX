package com.smarthire.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "interviews")
public class Interview {
    
    @Id
    private String id;
    
    // References
    private String shortlistedCandidateId; // Reference to ShortlistedCandidate
    private String applicationId; // Reference to Application
    private String jobId; // Reference to Job
    
    // Candidate Information
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    
    // Job Information
    private String jobTitle;
    private String company;
    
    // Interview Details
    private String interviewRound; // ROUND_1, ROUND_2, HR_ROUND
    private LocalDateTime scheduledDate;
    private String scheduledTime; // "10:00 AM - 11:00 AM"
    private String interviewMode; // ONLINE, OFFLINE, HYBRID
    private String meetingLink; // For online interviews
    private String venue; // For offline interviews
    
    // Interviewer Details
    private List<String> interviewerNames;
    private List<String> interviewerEmails;
    private String interviewPanel; // "Technical Panel" / "HR Panel"
    
    // Status Tracking
    private String status; // SCHEDULED, RESCHEDULED, COMPLETED, CANCELLED, NO_SHOW
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Interview Results (filled after interview)
    private String feedback;
    private Double technicalScore; // Out of 10
    private Double communicationScore; // Out of 10
    private Double overallRating; // Out of 10
    private String decision; // SELECTED, REJECTED, ON_HOLD, NEXT_ROUND
    private String interviewerRemarks;
    
    // Next Round Information
    private String nextRoundId; // ID of next round interview if NEXT_ROUND
    private Boolean isLastRound; // True if HR_ROUND or final round
    
    // Additional Fields
    private String notes;
    private Boolean notificationSent; // Email/SMS notification status
    private Boolean reminderSent;
    private LocalDateTime reminderSentAt;
    
    // Round tracking
    private Integer roundNumber; // 1, 2, 3 for ROUND_1, ROUND_2, HR_ROUND
    
    // Constructor for easy creation
    public Interview(String shortlistedCandidateId, String applicationId, String jobId,
                     String candidateName, String candidateEmail, String jobTitle,
                     String interviewRound, LocalDateTime scheduledDate) {
        this.shortlistedCandidateId = shortlistedCandidateId;
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.candidateName = candidateName;
        this.candidateEmail = candidateEmail;
        this.jobTitle = jobTitle;
        this.interviewRound = interviewRound;
        this.scheduledDate = scheduledDate;
        this.status = "SCHEDULED";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.notificationSent = false;
        this.reminderSent = false;
        this.isLastRound = interviewRound.equals("HR_ROUND");
        this.roundNumber = getRoundNumberFromName(interviewRound);
    }
    
    private Integer getRoundNumberFromName(String round) {
        switch (round) {
            case "ROUND_1": return 1;
            case "ROUND_2": return 2;
            case "HR_ROUND": return 3;
            default: return 0;
        }
    }
}
