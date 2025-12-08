package com.smarthire.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferLetterRequest {
    
    // Required fields
    private String candidateId;
    private String applicationId;
    private String jobId;
    private String interviewId;
    
    // Offer details
    private LocalDate joiningDate;
    private String employmentType; // FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP
    private String workLocation; // REMOTE, ONSITE, HYBRID
    private String officeLocation;
    
    // Compensation
    private Double annualCtc;
    private Double basicSalary;
    private Double hra;
    private Double specialAllowance;
    private Double performanceBonus;
    private Double otherAllowances;
    
    // Benefits
    private String benefits;
    private Integer paidLeaves;
    private String additionalBenefits;
    
    // Terms
    private Integer probationPeriod; // In months
    private Integer noticePeriod; // In days
    private String reportingManager;
    private String reportingManagerDesignation;
    
    // Additional
    private String additionalNotes;
    private String generatedBy;
    private String approvedBy;
    private LocalDate expiryDate;
}
