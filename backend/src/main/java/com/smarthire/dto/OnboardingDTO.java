package com.smarthire.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingDTO {
    
    private String id;
    
    // References
    private String candidateId;
    private String offerLetterId;
    private String applicationId;
    private String jobId;
    
    // Employee Information
    private String employeeId;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private String personalEmail;
    
    // Job Details
    private String jobTitle;
    private String department;
    private String designation;
    private String reportingManager;
    private String workLocation;
    
    // Onboarding Dates
    private LocalDate joiningDate;
    private LocalDate actualJoiningDate;
    private LocalDate onboardingStartDate;
    private LocalDate onboardingCompletionDate;
    
    // Onboarding Status
    private String status;
    private String currentStep;
    private Integer completionPercentage;
    
    // Required Documents
    private List<DocumentDTO> documents;
    
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
    private String onboardingCoordinator;
    private String hrRemarks;
    private String approvedBy;
    
    // Background Verification
    private Boolean backgroundVerificationRequired;
    private String backgroundVerificationStatus;
    private LocalDate backgroundVerificationDate;
    private String backgroundVerificationRemarks;
    
    // Probation Details
    private Integer probationPeriod;
    private LocalDate probationEndDate;
    
    // Additional Information
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    private String bloodGroup;
    private String additionalNotes;
    
    // Nested DTO for documents
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentDTO {
        private String documentType;
        private String documentName;
        private String documentUrl;
        private Boolean isRequired;
        private Boolean isSubmitted;
        private Boolean isVerified;
        private String verifiedBy;
        private String remarks;
        private Long fileSize;
        private String fileType;
    }
}
