package com.smarthire.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "candidate_applications")
public class CandidateApplication {

    @Id
    private String id;

    // Job Information
    private String jobId;
    private String jobTitle;
    private String company;

    // Candidate Information
    private String fullName;
    private String email;
    private String phone;
    private String currentRole;
    private Integer yearsOfExperience;
    private String skills;
    private String linkedIn;
    private String portfolio;
    private String coverLetter;

    // Resume Information
    private String resumeFileName;
    private String resumeFileType;
    private Long resumeFileSize;
    private String resumeBase64; // Store resume as base64 for easy retrieval

    // Application Status
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
    private String notes; // For employer notes

    // Screening/Matching (for future AI features)
    private Double matchScore;
    private String extractedSkills;

    public void onCreate() {
        this.appliedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ApplicationStatus.NEW;
        }
    }

    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum ApplicationStatus {
        NEW,
        UNDER_REVIEW,
        SHORTLISTED,
        INTERVIEW_SCHEDULED,
        REJECTED,
        ACCEPTED
    }
}
