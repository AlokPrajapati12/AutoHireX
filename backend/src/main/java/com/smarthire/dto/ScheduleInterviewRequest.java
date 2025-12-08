package com.smarthire.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleInterviewRequest {
    
    private String shortlistedCandidateId;
    private String applicationId;
    private String jobId;
    private String interviewRound; // ROUND_1, ROUND_2, HR_ROUND
    
    // Schedule Details
    private LocalDateTime scheduledDate;
    private String scheduledTime; // "10:00 AM - 11:00 AM"
    private String interviewMode; // ONLINE, OFFLINE, HYBRID
    private String meetingLink;
    private String venue;
    
    // Interviewer Details
    private List<String> interviewerNames;
    private List<String> interviewerEmails;
    private String interviewPanel;
    
    // Additional
    private String notes;
    private Boolean sendNotification; // Whether to send email notification
}
