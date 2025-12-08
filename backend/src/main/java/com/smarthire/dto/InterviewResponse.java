package com.smarthire.dto;

import com.smarthire.model.Interview;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewResponse {
    
    private Boolean success;
    private String message;
    private Interview interview;
    private List<Interview> interviews;
    
    // Statistics
    private Long totalScheduled;
    private Long totalCompleted;
    private Long totalCancelled;
    
    public InterviewResponse(Boolean success, String message, Interview interview) {
        this.success = success;
        this.message = message;
        this.interview = interview;
    }
    
    public InterviewResponse(Boolean success, String message, List<Interview> interviews) {
        this.success = success;
        this.message = message;
        this.interviews = interviews;
    }
}
