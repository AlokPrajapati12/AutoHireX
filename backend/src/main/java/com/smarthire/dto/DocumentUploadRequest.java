package com.smarthire.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {
    
    private String onboardingId;
    private String documentType; // AADHAAR, PAN, PHOTO, etc.
    private String documentName;
    private String documentUrl; // File path or base64 data
    private String fileType; // PDF, JPG, PNG
    private Long fileSize; // In bytes
    private String remarks;
}
