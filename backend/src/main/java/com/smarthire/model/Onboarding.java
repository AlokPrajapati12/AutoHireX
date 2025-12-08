package com.smarthire.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "onboarding")
public class Onboarding {
    
    @Id
    private String id;
    
    // References
    private String candidateId; // Reference to shortlisted candidate
    private String offerLetterId; // Reference to accepted offer letter
    private String applicationId; // Reference to application
    private String jobId; // Reference to job
    
    // Employee Information
    private String employeeId; // Auto-generated employee ID
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private String personalEmail; // Personal email for communication
    
    // Job Details
    private String jobTitle;
    private String department;
    private String designation;
    private String reportingManager;
    private String workLocation;
    
    // Onboarding Dates
    private LocalDate joiningDate; // Expected joining date from offer
    private LocalDate actualJoiningDate; // Actual date joined
    private LocalDate onboardingStartDate; // When onboarding process started
    private LocalDate onboardingCompletionDate; // When fully onboarded
    
    // Onboarding Status
    private String status; // PENDING, DOCUMENTS_SUBMITTED, VERIFIED, APPROVED, COMPLETED, REJECTED
    private String currentStep; // DOCUMENT_COLLECTION, VERIFICATION, SYSTEM_SETUP, ORIENTATION, COMPLETED
    private Integer completionPercentage; // 0-100%
    
    // Required Documents (Checklist)
    private List<OnboardingDocument> documents;
    
    // System Setup
    private Boolean emailAccountCreated;
    private Boolean systemAccessProvided;
    private Boolean idCardIssued;
    private Boolean workstationAssigned;
    private String workstationNumber;
    
    // Orientation Details
    private Boolean orientationCompleted;
    private LocalDate orientationDate;
    private String orientationConductedBy;
    private String orientationRemarks;
    
    // HR Details
    private String onboardingCoordinator; // HR person handling onboarding
    private String hrRemarks;
    private String approvedBy;
    private LocalDateTime approvedAt;
    
    // Background Verification
    private Boolean backgroundVerificationRequired;
    private String backgroundVerificationStatus; // PENDING, IN_PROGRESS, COMPLETED, FAILED
    private LocalDate backgroundVerificationDate;
    private String backgroundVerificationRemarks;
    
    // Probation Details
    private Integer probationPeriod; // In months
    private LocalDate probationEndDate;
    
    // Additional Information
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    private String bloodGroup;
    private String additionalNotes;
    
    // Tracking
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // Nested class for documents
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OnboardingDocument {
        private String documentType; // AADHAAR, PAN, PHOTO, EDUCATIONAL_CERT, EXPERIENCE_CERT, etc.
        private String documentName;
        private String documentUrl; // Storage path/URL
        private Boolean isRequired;
        private Boolean isSubmitted;
        private Boolean isVerified;
        private LocalDateTime submittedAt;
        private LocalDateTime verifiedAt;
        private String verifiedBy;
        private String remarks;
        private Long fileSize; // In bytes
        private String fileType; // PDF, JPG, PNG, etc.
    }
    
    // Constructor for easy creation
    public Onboarding(String candidateId, String offerLetterId, String applicationId, 
                      String jobId, String candidateName, String candidateEmail, 
                      String jobTitle, LocalDate joiningDate) {
        this.candidateId = candidateId;
        this.offerLetterId = offerLetterId;
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.candidateName = candidateName;
        this.candidateEmail = candidateEmail;
        this.jobTitle = jobTitle;
        this.joiningDate = joiningDate;
        this.employeeId = generateEmployeeId();
        this.status = "PENDING";
        this.currentStep = "DOCUMENT_COLLECTION";
        this.completionPercentage = 0;
        this.onboardingStartDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.emailAccountCreated = false;
        this.systemAccessProvided = false;
        this.idCardIssued = false;
        this.workstationAssigned = false;
        this.orientationCompleted = false;
        this.backgroundVerificationRequired = true;
        this.backgroundVerificationStatus = "PENDING";
        this.documents = initializeRequiredDocuments();
    }
    
    // Generate unique employee ID
    private String generateEmployeeId() {
        return "EMP" + System.currentTimeMillis();
    }
    
    // Initialize required documents checklist
    private List<OnboardingDocument> initializeRequiredDocuments() {
        List<OnboardingDocument> docs = new ArrayList<>();
        
        // Mandatory documents
        docs.add(new OnboardingDocument("AADHAAR_CARD", "Aadhaar Card", null, true, false, false, null, null, null, null, null, null));
        docs.add(new OnboardingDocument("PAN_CARD", "PAN Card", null, true, false, false, null, null, null, null, null, null));
        docs.add(new OnboardingDocument("PASSPORT_PHOTO", "Passport Size Photo", null, true, false, false, null, null, null, null, null, null));
        docs.add(new OnboardingDocument("EDUCATIONAL_CERTIFICATE", "Educational Certificate (Highest)", null, true, false, false, null, null, null, null, null, null));
        docs.add(new OnboardingDocument("ADDRESS_PROOF", "Address Proof", null, true, false, false, null, null, null, null, null, null));
        docs.add(new OnboardingDocument("CANCELLED_CHEQUE", "Cancelled Cheque / Bank Statement", null, true, false, false, null, null, null, null, null, null));
        
        // Optional documents
        docs.add(new OnboardingDocument("EXPERIENCE_LETTER", "Experience Letter (If applicable)", null, false, false, false, null, null, null, null, null, null));
        docs.add(new OnboardingDocument("RELIEVING_LETTER", "Relieving Letter (If applicable)", null, false, false, false, null, null, null, null, null, null));
        docs.add(new OnboardingDocument("SALARY_SLIPS", "Last 3 Salary Slips (If applicable)", null, false, false, false, null, null, null, null, null, null));
        docs.add(new OnboardingDocument("COVID_CERTIFICATE", "COVID Vaccination Certificate", null, false, false, false, null, null, null, null, null, null));
        
        return docs;
    }
    
    // Update completion percentage
    public void updateCompletionPercentage() {
        int totalSteps = 5; // Document Collection, Verification, System Setup, Orientation, Completion
        int completedSteps = 0;
        
        // Check document submission
        long submittedRequired = documents.stream()
            .filter(OnboardingDocument::getIsRequired)
            .filter(OnboardingDocument::getIsSubmitted)
            .count();
        long totalRequired = documents.stream()
            .filter(OnboardingDocument::getIsRequired)
            .count();
        
        if (submittedRequired == totalRequired) completedSteps++;
        
        // Check verification
        long verifiedRequired = documents.stream()
            .filter(OnboardingDocument::getIsRequired)
            .filter(OnboardingDocument::getIsVerified)
            .count();
        
        if (verifiedRequired == totalRequired) completedSteps++;
        
        // Check system setup
        if (emailAccountCreated && systemAccessProvided && idCardIssued) completedSteps++;
        
        // Check orientation
        if (orientationCompleted) completedSteps++;
        
        // Check completion
        if ("COMPLETED".equals(status)) completedSteps++;
        
        this.completionPercentage = (completedSteps * 100) / totalSteps;
    }
}
