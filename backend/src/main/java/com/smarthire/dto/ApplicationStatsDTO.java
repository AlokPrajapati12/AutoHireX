package com.smarthire.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatsDTO {
    private long totalApplications;
    private long todayApplications;
    private long thisWeekApplications;
    
    private long submittedCount;
    private long underReviewCount;
    private long shortlistedCount;
    private long interviewScheduledCount;
    private long acceptedCount;
    private long rejectedCount;
    
    private double successRate;
    private double averageMatchScore;
    
    // Trends
    private TrendData submittedTrend;
    private TrendData underReviewTrend;
    private TrendData shortlistedTrend;
    private TrendData interviewTrend;
    private TrendData acceptedTrend;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private String period; // "today", "this_week", "this_month"
        private long count;
        private double changePercentage;
    }
}
