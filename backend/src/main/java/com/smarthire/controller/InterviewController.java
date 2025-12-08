package com.smarthire.controller;

import com.smarthire.dto.BatchInterviewScheduleRequest;
import com.smarthire.dto.InterviewFeedbackRequest;
import com.smarthire.dto.InterviewResponse;
import com.smarthire.dto.ScheduleInterviewRequest;
import com.smarthire.service.InterviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/interviews")
@CrossOrigin(origins = "*")
@Slf4j
public class InterviewController {
    
    @Autowired
    private InterviewService interviewService;
    
    /**
     * Schedule interviews for multiple candidates (batch scheduling)
     * POST /api/interviews/schedule
     */
    @PostMapping("/schedule")
    public ResponseEntity<?> scheduleInterviews(@RequestBody BatchInterviewScheduleRequest request) {
        log.info("📅 Received batch interview scheduling request for {} candidates", 
            request.getCandidates() != null ? request.getCandidates().size() : 0);
        
        try {
            Map<String, Object> response = interviewService.scheduleBatchInterviews(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error in batch scheduling: ", e);
            return ResponseEntity.status(500).body(
                Map.of(
                    "success", false,
                    "message", "Error scheduling interviews: " + e.getMessage()
                )
            );
        }
    }
    
    /**
     * Schedule a single interview
     * POST /api/interviews/schedule-single
     */
    @PostMapping("/schedule-single")
    public ResponseEntity<InterviewResponse> scheduleSingleInterview(@RequestBody ScheduleInterviewRequest request) {
        log.info("Received request to schedule interview for candidate: {}", request.getShortlistedCandidateId());
        InterviewResponse response = interviewService.scheduleInterview(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all interviews for a job
     * GET /api/interviews/job/{jobId}
     */
    @GetMapping("/job/{jobId}")
    public ResponseEntity<InterviewResponse> getInterviewsByJob(@PathVariable String jobId) {
        log.info("Fetching interviews for job: {}", jobId);
        InterviewResponse response = interviewService.getInterviewsByJob(jobId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all interviews for a candidate
     * GET /api/interviews/candidate/{applicationId}
     */
    @GetMapping("/candidate/{applicationId}")
    public ResponseEntity<InterviewResponse> getInterviewsByCandidate(@PathVariable String applicationId) {
        log.info("Fetching interviews for candidate: {}", applicationId);
        InterviewResponse response = interviewService.getInterviewsByCandidate(applicationId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get a specific interview
     * GET /api/interviews/{interviewId}
     */
    @GetMapping("/{interviewId}")
    public ResponseEntity<InterviewResponse> getInterview(@PathVariable String interviewId) {
        log.info("Fetching interview: {}", interviewId);
        InterviewResponse response = interviewService.getInterview(interviewId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update interview status
     * PUT /api/interviews/{interviewId}/status
     */
    @PutMapping("/{interviewId}/status")
    public ResponseEntity<InterviewResponse> updateInterviewStatus(
            @PathVariable String interviewId,
            @RequestParam String status) {
        log.info("Updating interview {} status to: {}", interviewId, status);
        InterviewResponse response = interviewService.updateInterviewStatus(interviewId, status);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Submit interview feedback and decision
     * POST /api/interviews/feedback
     */
    @PostMapping("/feedback")
    public ResponseEntity<InterviewResponse> submitFeedback(@RequestBody InterviewFeedbackRequest request) {
        log.info("Submitting feedback for interview: {}", request.getInterviewId());
        InterviewResponse response = interviewService.submitInterviewFeedback(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Reschedule an interview
     * PUT /api/interviews/{interviewId}/reschedule
     */
    @PutMapping("/{interviewId}/reschedule")
    public ResponseEntity<InterviewResponse> rescheduleInterview(
            @PathVariable String interviewId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDate,
            @RequestParam String newTime) {
        log.info("Rescheduling interview: {}", interviewId);
        InterviewResponse response = interviewService.rescheduleInterview(interviewId, newDate, newTime);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancel an interview
     * PUT /api/interviews/{interviewId}/cancel
     */
    @PutMapping("/{interviewId}/cancel")
    public ResponseEntity<InterviewResponse> cancelInterview(
            @PathVariable String interviewId,
            @RequestParam String reason) {
        log.info("Cancelling interview: {}", interviewId);
        InterviewResponse response = interviewService.cancelInterview(interviewId, reason);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health check
     * GET /api/interviews/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Interview service is running");
    }
}
