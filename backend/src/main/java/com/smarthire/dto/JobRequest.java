package com.smarthire.dto;

import lombok.Data;

@Data
public class JobRequest {
    private String jobId;         
    private String title;
    private String description;
    private String company;
    private String location;
    private String employmentType;
    private String experienceLevel;
    private String requiredSkills;
    private String salaryRange;
}
