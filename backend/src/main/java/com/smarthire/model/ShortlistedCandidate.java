package com.smarthire.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ShortlistedCandidate - Stores AI-analyzed and shortlisted candidates
 * Created after ATS processing in Step 5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "shortlisted_candidates")
public class ShortlistedCandidate {

    @Id
    private String id;

    // Reference to original application
    private String applicationId;
    private String jobId;
    private String jobTitle;
    private String company;

    // Candidate Information
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;

    // AI Analysis Results
    private Double finalScore;  // Overall ATS score (0-100)
    private Double semanticSimilarity;  // Resume-JD similarity
    private Double skillMatchPercentage;  // Skill match %
    private Double experienceMatch;  // Experience relevance
    private Double educationMatch;  // Education relevance
    private Double llmScore;  // LLM evaluation score

    // Skill Analysis
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String extractedSkills;  // All skills found in resume

    // LLM Evaluation
    private String llmDecision;  // SHORTLIST / REJECT / MAYBE
    private String llmReasoning;  // Why this decision
    private String interviewRecommendation;  // Interview strategy
    private String keyStrengths;  // Candidate strengths
    private String developmentAreas;  // Areas for improvement

    // Resume Embeddings
    private List<Double> resumeEmbedding;  // Vector representation
    private String embeddingMetadata;  // Additional embedding info

    // Status & Tracking
    private ShortlistStatus status;
    private Integer rank;  // Ranking among shortlisted candidates
    private LocalDateTime shortlistedAt;
    private LocalDateTime updatedAt;
    private String notes;  // HR/Recruiter notes

    // Interview Scheduling (for next step)
    private boolean interviewScheduled;
    private String interviewId;
    private LocalDateTime interviewDate;
    
    // Offer Letter Status
    private Boolean offerLetterGenerated;
    private String offerLetterId;

    public void onCreate() {
        this.shortlistedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ShortlistStatus.PENDING_REVIEW;
        }
        this.interviewScheduled = false;
    }

    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper method to update status from String
    public void setStatusFromString(String statusStr) {
        try {
            this.status = ShortlistStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            // Handle custom status strings - default to PENDING_REVIEW
            this.status = ShortlistStatus.PENDING_REVIEW;
        }
    }
    
    public enum ShortlistStatus {
        PENDING_REVIEW,      // Just shortlisted, waiting for review
        APPROVED,            // Approved by HR/recruiter
        INTERVIEW_SCHEDULED, // Interview scheduled
        INTERVIEW_COMPLETED, // Interview done
        MOVED_TO_NEXT_ROUND, // Passed to next interview round
        REJECTED,            // Rejected after review
        OFFER_EXTENDED,      // Offer letter sent
        OFFER_LETTER_SENT,   // Offer letter generated and sent
        OFFER_ACCEPTED,      // Candidate accepted offer
        OFFER_REJECTED       // Candidate rejected offer
    }
}
