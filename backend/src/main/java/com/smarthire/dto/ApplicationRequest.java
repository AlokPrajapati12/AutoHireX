package com.smarthire.dto;

import lombok.Data;

@Data
public class ApplicationRequest {
    private String jobId;       // Use String for MongoDB IDs
    private String resumeContent;
}
