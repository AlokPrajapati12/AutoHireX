package com.smarthire.service;

import com.smarthire.dto.ApplicationStatsDTO;
import com.smarthire.dto.ApplicationTimelineDTO;
import com.smarthire.model.CandidateApplication;
import com.smarthire.repository.CandidateApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MonitoringService {

    @Autowired
    private CandidateApplicationRepository applicationRepository;

    /**
     * Get comprehensive application statistics
     */
    public ApplicationStatsDTO getApplicationStats(String jobId, String startDate, String endDate) {
        List<CandidateApplication> applications;
        
        if (jobId != null && !jobId.isEmpty()) {
            applications = applicationRepository.findByJobId(jobId);
        } else {
            applications = applicationRepository.findAll();
        }

        // Apply date filtering if provided
        if (startDate != null && endDate != null) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
            applications = applications.stream()
                .filter(app -> app.getAppliedAt().isAfter(start) && app.getAppliedAt().isBefore(end))
                .collect(Collectors.toList());
        }

        ApplicationStatsDTO stats = new ApplicationStatsDTO();
        stats.setTotalApplications(applications.size());

        // Count by status
        Map<CandidateApplication.ApplicationStatus, Long> statusCounts = applications.stream()
            .collect(Collectors.groupingBy(CandidateApplication::getStatus, Collectors.counting()));

        stats.setSubmittedCount(statusCounts.getOrDefault(CandidateApplication.ApplicationStatus.NEW, 0L));
        stats.setUnderReviewCount(statusCounts.getOrDefault(CandidateApplication.ApplicationStatus.UNDER_REVIEW, 0L));
        stats.setShortlistedCount(statusCounts.getOrDefault(CandidateApplication.ApplicationStatus.SHORTLISTED, 0L));
        stats.setInterviewScheduledCount(statusCounts.getOrDefault(CandidateApplication.ApplicationStatus.INTERVIEW_SCHEDULED, 0L));
        stats.setAcceptedCount(statusCounts.getOrDefault(CandidateApplication.ApplicationStatus.ACCEPTED, 0L));
        stats.setRejectedCount(statusCounts.getOrDefault(CandidateApplication.ApplicationStatus.REJECTED, 0L));

        // Calculate success rate
        long totalProcessed = stats.getAcceptedCount() + stats.getRejectedCount();
        if (totalProcessed > 0) {
            stats.setSuccessRate((double) stats.getAcceptedCount() / totalProcessed * 100);
        }

        // Calculate average match score
        OptionalDouble avgScore = applications.stream()
            .filter(app -> app.getMatchScore() != null)
            .mapToDouble(CandidateApplication::getMatchScore)
            .average();
        stats.setAverageMatchScore(avgScore.orElse(0.0));

        // Today's applications
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        stats.setTodayApplications(applications.stream()
            .filter(app -> app.getAppliedAt().isAfter(todayStart))
            .count());

        // This week's applications
        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();
        stats.setThisWeekApplications(applications.stream()
            .filter(app -> app.getAppliedAt().isAfter(weekStart))
            .count());

        // Calculate trends
        stats.setSubmittedTrend(calculateTrend(applications, CandidateApplication.ApplicationStatus.NEW));
        stats.setUnderReviewTrend(calculateTrend(applications, CandidateApplication.ApplicationStatus.UNDER_REVIEW));
        stats.setShortlistedTrend(calculateTrend(applications, CandidateApplication.ApplicationStatus.SHORTLISTED));
        stats.setInterviewTrend(calculateTrend(applications, CandidateApplication.ApplicationStatus.INTERVIEW_SCHEDULED));
        stats.setAcceptedTrend(calculateTrend(applications, CandidateApplication.ApplicationStatus.ACCEPTED));

        return stats;
    }

    /**
     * Get paginated applications with filtering
     */
    public Map<String, Object> getApplications(String status, String jobId, String searchTerm, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<CandidateApplication> applicationPage;

        if (status != null && !status.isEmpty() && !status.equals("ALL")) {
            CandidateApplication.ApplicationStatus appStatus = CandidateApplication.ApplicationStatus.valueOf(status);
            applicationPage = applicationRepository.findByStatus(appStatus, pageRequest);
        } else {
            applicationPage = applicationRepository.findAll(pageRequest);
        }

        List<CandidateApplication> applications = applicationPage.getContent();

        // Apply additional filtering
        if (jobId != null && !jobId.isEmpty()) {
            applications = applications.stream()
                .filter(app -> jobId.equals(app.getJobId()))
                .collect(Collectors.toList());
        }

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String search = searchTerm.toLowerCase();
            applications = applications.stream()
                .filter(app -> 
                    app.getFullName().toLowerCase().contains(search) ||
                    app.getEmail().toLowerCase().contains(search) ||
                    app.getJobTitle().toLowerCase().contains(search))
                .collect(Collectors.toList());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("applications", applications);
        response.put("currentPage", page);
        response.put("totalItems", applicationPage.getTotalElements());
        response.put("totalPages", applicationPage.getTotalPages());

        return response;
    }

    /**
     * Get application timeline for tracking (Flipkart-style)
     */
    public ApplicationTimelineDTO getApplicationTimeline(String applicationId) {
        @SuppressWarnings("null")
        Optional<CandidateApplication> appOpt = applicationRepository.findById(applicationId);
        
        if (appOpt.isEmpty()) {
            throw new RuntimeException("Application not found");
        }

        CandidateApplication app = appOpt.get();
        ApplicationTimelineDTO timeline = new ApplicationTimelineDTO();
        
        timeline.setId(app.getId());
        timeline.setCandidateName(app.getFullName());
        timeline.setCandidateEmail(app.getEmail());
        timeline.setJobTitle(app.getJobTitle());
        timeline.setCompany(app.getCompany());
        timeline.setCurrentStatus(app.getStatus().name());
        timeline.setAppliedAt(app.getAppliedAt());

        // Build timeline events
        List<ApplicationTimelineDTO.TimelineEvent> events = buildTimelineEvents(app);
        timeline.setTimeline(events);

        return timeline;
    }

    /**
     * Get status distribution
     */
    public Map<String, Long> getStatusDistribution(String jobId) {
        List<CandidateApplication> applications;
        
        if (jobId != null && !jobId.isEmpty()) {
            applications = applicationRepository.findByJobId(jobId);
        } else {
            applications = applicationRepository.findAll();
        }

        return applications.stream()
            .collect(Collectors.groupingBy(
                app -> app.getStatus().name(),
                Collectors.counting()
            ));
    }

    /**
     * Get application trends
     */
    @SuppressWarnings("unused")
    public Map<String, Object> getApplicationTrends(String period) {
        List<CandidateApplication> applications = applicationRepository.findAll();
        Map<String, Object> trends = new HashMap<>();

        // Default period is last 7 days
        int days = period != null && period.equals("month") ? 30 : 7;
        LocalDateTime startDate = LocalDate.now().minusDays(days).atStartOfDay();

        Map<String, Long> dailyApplications = new LinkedHashMap<>();
        
        for (int i = days; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateKey = date.toString();
            
            long count = applications.stream()
                .filter(app -> app.getAppliedAt().toLocalDate().equals(date))
                .count();
            
            dailyApplications.put(dateKey, count);
        }

        trends.put("dailyApplications", dailyApplications);
        trends.put("period", period != null ? period : "week");
        
        return trends;
    }

    /**
     * Update application status
     */
    public void updateApplicationStatus(String applicationId, String newStatus, String notes) {
        @SuppressWarnings("null")
        Optional<CandidateApplication> appOpt = applicationRepository.findById(applicationId);
        
        if (appOpt.isEmpty()) {
            throw new RuntimeException("Application not found");
        }

        CandidateApplication app = appOpt.get();
        app.setStatus(CandidateApplication.ApplicationStatus.valueOf(newStatus));
        if (notes != null) {
            app.setNotes(notes);
        }
        app.onUpdate();
        
        applicationRepository.save(app);
    }

    /**
     * Export applications to CSV
     */
    public String exportApplicationsToCSV(String status, String jobId) {
        List<CandidateApplication> applications;
        
        if (jobId != null && !jobId.isEmpty()) {
            applications = applicationRepository.findByJobId(jobId);
        } else {
            applications = applicationRepository.findAll();
        }

        if (status != null && !status.isEmpty() && !status.equals("ALL")) {
            CandidateApplication.ApplicationStatus appStatus = CandidateApplication.ApplicationStatus.valueOf(status);
            applications = applications.stream()
                .filter(app -> app.getStatus() == appStatus)
                .collect(Collectors.toList());
        }

        StringBuilder csv = new StringBuilder();
        csv.append("Application ID,Candidate Name,Email,Phone,Job Title,Company,Status,Applied Date,Match Score\n");

        for (CandidateApplication app : applications) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%.2f\n",
                app.getId(),
                app.getFullName(),
                app.getEmail(),
                app.getPhone() != null ? app.getPhone() : "",
                app.getJobTitle(),
                app.getCompany(),
                app.getStatus(),
                app.getAppliedAt(),
                app.getMatchScore() != null ? app.getMatchScore() : 0.0
            ));
        }

        return csv.toString();
    }

    // Helper Methods

    private ApplicationStatsDTO.TrendData calculateTrend(
            List<CandidateApplication> applications, 
            CandidateApplication.ApplicationStatus status) {
        
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();

        long todayCount = applications.stream()
            .filter(app -> app.getStatus() == status)
            .filter(app -> app.getAppliedAt().isAfter(todayStart))
            .count();

        long yesterdayCount = applications.stream()
            .filter(app -> app.getStatus() == status)
            .filter(app -> app.getAppliedAt().isAfter(yesterdayStart) && app.getAppliedAt().isBefore(todayStart))
            .count();

        double changePercentage = 0.0;
        if (yesterdayCount > 0) {
            changePercentage = ((double) (todayCount - yesterdayCount) / yesterdayCount) * 100;
        } else if (todayCount > 0) {
            changePercentage = 100.0;
        }

        return new ApplicationStatsDTO.TrendData("today", todayCount, changePercentage);
    }

    private List<ApplicationTimelineDTO.TimelineEvent> buildTimelineEvents(CandidateApplication app) {
        List<ApplicationTimelineDTO.TimelineEvent> events = new ArrayList<>();
        CandidateApplication.ApplicationStatus currentStatus = app.getStatus();

        // Define all possible statuses in order
        Map<CandidateApplication.ApplicationStatus, StatusInfo> statusMap = new LinkedHashMap<>();
        statusMap.put(CandidateApplication.ApplicationStatus.NEW, 
            new StatusInfo("Application Submitted", "Your application has been received", "✓", "#10b981"));
        statusMap.put(CandidateApplication.ApplicationStatus.UNDER_REVIEW, 
            new StatusInfo("Under Review", "Our team is reviewing your application", "👁", "#3b82f6"));
        statusMap.put(CandidateApplication.ApplicationStatus.SHORTLISTED, 
            new StatusInfo("Shortlisted", "Congratulations! You've been shortlisted", "⭐", "#8b5cf6"));
        statusMap.put(CandidateApplication.ApplicationStatus.INTERVIEW_SCHEDULED, 
            new StatusInfo("Interview Scheduled", "Interview has been scheduled", "📅", "#06b6d4"));
        statusMap.put(CandidateApplication.ApplicationStatus.ACCEPTED, 
            new StatusInfo("Accepted", "Congratulations! You've been accepted", "🎉", "#10b981"));

        int currentStatusIndex = new ArrayList<>(statusMap.keySet()).indexOf(currentStatus);

        int index = 0;
        for (Map.Entry<CandidateApplication.ApplicationStatus, StatusInfo> entry : statusMap.entrySet()) {
            StatusInfo info = entry.getValue();
            boolean completed = index <= currentStatusIndex;
            boolean isCurrent = index == currentStatusIndex;

            ApplicationTimelineDTO.TimelineEvent event = new ApplicationTimelineDTO.TimelineEvent();
            event.setStatus(entry.getKey().name());
            event.setStatusLabel(info.label);
            event.setDescription(info.description);
            event.setCompleted(completed);
            event.setCurrent(isCurrent);
            event.setIcon(info.icon);
            event.setColor(info.color);
            
            // Set timestamp for completed events
            if (completed) {
                if (index == 0) {
                    event.setTimestamp(app.getAppliedAt());
                } else {
                    event.setTimestamp(app.getUpdatedAt());
                }
            }

            // Add notes for current status
            if (isCurrent && app.getNotes() != null) {
                event.setNotes(app.getNotes());
            }

            events.add(event);
            index++;
        }

        // Handle rejected status separately
        if (currentStatus == CandidateApplication.ApplicationStatus.REJECTED) {
            ApplicationTimelineDTO.TimelineEvent rejectedEvent = new ApplicationTimelineDTO.TimelineEvent();
            rejectedEvent.setStatus("REJECTED");
            rejectedEvent.setStatusLabel("Application Not Selected");
            rejectedEvent.setDescription("Thank you for your interest");
            rejectedEvent.setCompleted(true);
            rejectedEvent.setCurrent(true);
            rejectedEvent.setIcon("✗");
            rejectedEvent.setColor("#ef4444");
            rejectedEvent.setTimestamp(app.getUpdatedAt());
            if (app.getNotes() != null) {
                rejectedEvent.setNotes(app.getNotes());
            }
            events.add(rejectedEvent);
        }

        return events;
    }

    private static class StatusInfo {
        String label;
        String description;
        String icon;
        String color;

        StatusInfo(String label, String description, String icon, String color) {
            this.label = label;
            this.description = description;
            this.icon = icon;
            this.color = color;
        }
    }
}
