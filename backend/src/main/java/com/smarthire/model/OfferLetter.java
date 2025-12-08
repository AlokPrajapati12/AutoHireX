package com.smarthire.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "offer_letters")
public class OfferLetter {
    
    @Id
    private String id;
    
    // References
    private String candidateId; // Reference to shortlisted candidate
    private String applicationId; // Reference to application
    private String jobId; // Reference to job
    private String interviewId; // Reference to final interview (HR Round)
    
    // Candidate Information
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private String candidateAddress;
    
    // Job Information
    private String jobTitle;
    private String department;
    private String company;
    private String companyAddress;
    
    // Offer Details
    private String offerLetterNumber; // Unique offer letter ID
    private LocalDate offerDate;
    private LocalDate joiningDate;
    private String employmentType; // FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP
    private String workLocation; // REMOTE, ONSITE, HYBRID
    private String officeLocation;
    
    // Compensation Details
    private Double annualCtc; // Cost to Company
    private Double basicSalary;
    private Double hra; // House Rent Allowance
    private Double specialAllowance;
    private Double performanceBonus;
    private Double otherAllowances;
    private String currency; // INR, USD, EUR
    
    // Benefits
    private String benefits; // Health insurance, PF, etc.
    private Integer paidLeaves;
    private String additionalBenefits;
    
    // Offer Terms
    private Integer probationPeriod; // In months
    private Integer noticePeriod; // In days
    private String reportingManager;
    private String reportingManagerDesignation;
    
    // Status Tracking
    private String status; // GENERATED, SENT, ACCEPTED, REJECTED, EXPIRED, WITHDRAWN
    private LocalDateTime generatedAt;
    private LocalDateTime sentAt;
    private LocalDateTime respondedAt;
    private LocalDate expiryDate; // Offer valid till
    
    // Acceptance Details
    private Boolean isAccepted;
    private String acceptanceMethod; // EMAIL, PORTAL, SIGNED_COPY
    private String acceptanceRemarks;
    private String rejectionReason;
    
    // Document Details
    private String offerLetterPdfUrl; // PDF storage path/URL
    private Boolean isDownloaded;
    private Integer downloadCount;
    
    // HR Details
    private String generatedBy; // HR/Admin who generated
    private String approvedBy; // Manager/Director who approved
    private String hrRemarks;
    
    // Additional Information
    private String additionalNotes;
    private LocalDateTime updatedAt;
    
    // Constructor for easy creation
    public OfferLetter(String candidateId, String applicationId, String jobId,
                       String candidateName, String candidateEmail, String jobTitle,
                       Double annualCtc, LocalDate joiningDate) {
        this.candidateId = candidateId;
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.candidateName = candidateName;
        this.candidateEmail = candidateEmail;
        this.jobTitle = jobTitle;
        this.annualCtc = annualCtc;
        this.joiningDate = joiningDate;
        this.offerDate = LocalDate.now();
        this.offerLetterNumber = generateOfferNumber();
        this.status = "GENERATED";
        this.generatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isAccepted = false;
        this.isDownloaded = false;
        this.downloadCount = 0;
        this.currency = "INR";
        this.employmentType = "FULL_TIME";
    }
    
    private String generateOfferNumber() {
        return "OL-" + System.currentTimeMillis();
    }
}
