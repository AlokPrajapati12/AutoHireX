package com.smarthire.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingUpdateRequest {
    
    // Status Updates
    private String status;
    private String currentStep;
    
    // System Setup
    private Boolean emailAccountCreated;
    private Boolean systemAccessProvided;
    private Boolean idCardIssued;
    private Boolean workstationAssigned;
    private String workstationNumber;
    
    // Orientation
    private Boolean orientationCompleted;
    private LocalDate orientationDate;
    private String orientationConductedBy;
    private String orientationRemarks;
    
    // Background Verification
    private String backgroundVerificationStatus;
    private LocalDate backgroundVerificationDate;
    private String backgroundVerificationRemarks;
    
    // Dates
    private LocalDate actualJoiningDate;
    
    // HR Details
    private String hrRemarks;
    private String approvedBy;
    
    private String additionalNotes;
}
