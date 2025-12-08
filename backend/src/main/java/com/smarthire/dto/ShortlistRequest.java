package com.smarthire.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO for ATS Shortlisting Request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortlistRequest {
    private String jobId;
    private String jobDescription;
    private Double minScore;  // Optional: minimum score threshold (default: 50)
    private Integer maxCandidates;  // Optional: max candidates to shortlist (default: all above threshold)
}
