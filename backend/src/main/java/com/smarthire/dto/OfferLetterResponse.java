package com.smarthire.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferLetterResponse {
    
    private String id;
    private String offerLetterNumber;
    
    // Candidate Info
    private String candidateName;
    private String candidateEmail;
    private String jobTitle;
    private String company;
    
    // Offer Details
    private LocalDate offerDate;
    private LocalDate joiningDate;
    private String employmentType;
    private String workLocation;
    
    // Compensation
    private Double annualCtc;
    private String currency;
    
    // Status
    private String status;
    private LocalDateTime generatedAt;
    private LocalDateTime sentAt;
    private Boolean isAccepted;
    
    // Document
    private String offerLetterPdfUrl;
    private Boolean isDownloaded;
    
    private String message;
}
