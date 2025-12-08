package com.smarthire.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "jobs")
@JsonInclude(JsonInclude.Include.NON_NULL)  // Exclude null fields from JSON
public class Job {

    @Id
    private String id;
    
    private String title;
    private String description;
    private String company;
    private String location;
    private String employmentType;
    private String experienceLevel;
    private String requiredSkills;
    private String salaryRange;
    
    // Employer information
    private String postedBy;

    // Application limits - Initialize with defaults
    private Integer maxCandidates = 0;
    private Integer applicationCount = 0;

    private JobStatus status = JobStatus.OPEN;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Set timestamps before save or update
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = JobStatus.OPEN;
        }
        if (this.applicationCount == null) {
            this.applicationCount = 0;
        }
        if (this.maxCandidates == null) {
            this.maxCandidates = 0;
        }
    }

    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper methods for application management
    public boolean isFull() {
        int max = (maxCandidates != null) ? maxCandidates : 0;
        int count = (applicationCount != null) ? applicationCount : 0;
        return max > 0 && count >= max;
    }

    public boolean canAcceptApplications() {
        return status == JobStatus.OPEN && !isFull();
    }

    public void incrementApplicationCount() {
        if (this.applicationCount == null) {
            this.applicationCount = 0;
        }
        this.applicationCount++;
    }
    
    // Getters with null safety
    public Integer getMaxCandidates() {
        return maxCandidates != null ? maxCandidates : 0;
    }
    
    public Integer getApplicationCount() {
        return applicationCount != null ? applicationCount : 0;
    }

    public enum JobStatus {
        OPEN,
        CLOSED,
        DRAFT,
        PUBLISHED  // Added to handle existing database records
    }
}
