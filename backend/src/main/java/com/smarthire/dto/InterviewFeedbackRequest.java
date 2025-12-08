package com.smarthire.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewFeedbackRequest {
    
    private String interviewId;
    
    // Feedback Details
    private String feedback;
    private Double technicalScore; // Out of 10
    private Double communicationScore; // Out of 10
    private Double overallRating; // Out of 10
    
    // Decision
    private String decision; // SELECTED, REJECTED, ON_HOLD, NEXT_ROUND
    private String interviewerRemarks;
    
    // If decision is NEXT_ROUND
    private String nextRound; // ROUND_2, HR_ROUND
}
