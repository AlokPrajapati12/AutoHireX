package com.smarthire.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO for Shortlist Response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortlistResponse {
    
    private boolean success;
    private String message;
    private int totalProcessed;
    private int shortlistedCount;
    private int rejectedCount;
    private List<ShortlistedCandidateDTO> shortlistedCandidates;
    private String jobId;
    private String jobTitle;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShortlistedCandidateDTO {
        private String id;
        private String applicationId;
        private String candidateName;
        private String candidateEmail;
        private Double finalScore;
        private Double skillMatchPercentage;
        private List<String> matchedSkills;
        private List<String> missingSkills;
        private String llmDecision;
        private String llmReasoning;
        private Integer rank;
        private String status;
    }
}
