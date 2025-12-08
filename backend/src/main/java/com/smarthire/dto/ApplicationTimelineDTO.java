package com.smarthire.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationTimelineDTO {
    private String id;
    private String candidateName;
    private String candidateEmail;
    private String jobTitle;
    private String company;
    private String currentStatus;
    private LocalDateTime appliedAt;
    private List<TimelineEvent> timeline;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineEvent {
        private String status;
        private String statusLabel;
        private String description;
        private LocalDateTime timestamp;
        private String notes;
        private boolean completed;
        private boolean current;
        
        // For visual representation
        private String icon;
        private String color;
    }
}
