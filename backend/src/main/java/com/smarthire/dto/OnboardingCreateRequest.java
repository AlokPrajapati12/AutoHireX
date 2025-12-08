package com.smarthire.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingCreateRequest {
    
    private String candidateId;
    private String offerLetterId;
    private String applicationId;
    private String jobId;
    
    // Employee Information
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
    
    // Dates
    private LocalDate joiningDate;
    private LocalDate actualJoiningDate;
    
    // Additional Information
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    private String bloodGroup;
    
    // HR Details
    private String onboardingCoordinator;
    private Integer probationPeriod;
    private String additionalNotes;
}
