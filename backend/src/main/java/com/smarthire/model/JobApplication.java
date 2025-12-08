package com.smarthire.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "job_applications")
public class JobApplication {

    @Id
    private String id;

    @DBRef
    private Job job;
    
    // Required fields for public applications (non-registered candidates)
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    
    private ApplicationStatus status;
    private String coverLetter;
    
    // Resume storage
    private String resumeFileName;      // Original file name
    private String resumeContentType;   // MIME type (e.g., application/pdf)
    private byte[] resumeData;          // Actual file content stored as binary
    
    // AI-generated fields
    private String resumeContent;       // Extracted text from resume
    private String extractedSkills;     // Skills parsed from resume
    private Double matchScore;          // AI match score (0-100)
    private String matchedSkills;       // Skills that match job requirements
    private String missingSkills;       // Skills missing from candidate's profile
    private String aiRecommendation;    // AI-generated recommendation
    private String notes;               // Additional notes

    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    // Set timestamps before saving
    public void onCreate() {
        this.appliedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ApplicationStatus.SUBMITTED;
        }
    }

    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum ApplicationStatus {
        SUBMITTED,
        UNDER_REVIEW,
        SHORTLISTED,
        INTERVIEW_SCHEDULED,
        REJECTED,
        ACCEPTED
    }
}
