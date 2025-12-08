package com.smarthire.controller;

import com.smarthire.model.Job;
import com.smarthire.model.JobApplication;
import com.smarthire.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI-Powered Monitoring Controller
 * Integrates with Python AI service for intelligent candidate monitoring
 */
@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    @Autowired
    private JobService jobService;
    
    @Autowired
    private com.smarthire.repository.JobApplicationRepository applicationRepository;
    
    @Value("${ai.service.url:http://localhost:5001}")
    private String aiServiceUrl;
    
    private static final int MINIMUM_CANDIDATES = 3; // AI threshold: len(resumes) >= 3
    
    /**
     * Check if a specific job has enough applications
     * Implements AI logic: enough_applications = len(resumes) >= 3
     */
    @GetMapping("/job/{jobId}")
    public ResponseEntity<?> monitorJob(@PathVariable String jobId) {
        try {
            Job job = jobService.getJobById(jobId);
            List<JobApplication> applications = applicationRepository.findByJob(job);
            
            int applicationCount = applications.size();
            boolean enoughApplications = applicationCount >= MINIMUM_CANDIDATES;
            
            Map<String, Object> response = new HashMap<>();
            response.put("jobId", jobId);
            response.put("jobTitle", job.getTitle());
            response.put("applicationCount", applicationCount);
            response.put("enoughApplications", enoughApplications);
            response.put("minimumRequired", MINIMUM_CANDIDATES);
            response.put("jobStatus", job.getStatus().toString());
            
            // Auto-close job if enough applications and still open
            boolean autoClosedJob = false;
            if (enoughApplications && job.getStatus() == Job.JobStatus.OPEN) {
                job.setStatus(Job.JobStatus.CLOSED);
                job.onUpdate();
                jobService.updateJobSettings(job);
                autoClosedJob = true;
                
                System.out.println("🔒 AUTO-CLOSED Job: " + job.getTitle() + 
                                 " (Received " + applicationCount + " applications)");
            }
            
            response.put("autoClosedJob", autoClosedJob);
            
            if (enoughApplications) {
                response.put("message", "✅ Job has enough candidates! " + 
                           (autoClosedJob ? "Job automatically closed." : ""));
            } else {
                response.put("message", "⏳ Waiting for more candidates... " + 
                           "(" + applicationCount + "/" + MINIMUM_CANDIDATES + ")");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error monitoring job: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Check all active jobs and auto-close those with enough applications
     * This endpoint can be called periodically or on-demand
     */
    @PostMapping("/check-all")
    public ResponseEntity<?> checkAllJobs() {
        try {
            List<Job> openJobs = jobService.getAllOpenJobs();
            int jobsClosed = 0;
            Map<String, Integer> closedJobsMap = new HashMap<>();
            
            for (Job job : openJobs) {
                List<JobApplication> applications = applicationRepository.findByJob(job);
                int count = applications.size();
                
                // AI Logic: enough_applications = len(resumes) >= 3
                if (count >= MINIMUM_CANDIDATES) {
                    job.setStatus(Job.JobStatus.CLOSED);
                    job.onUpdate();
                    jobService.updateJobSettings(job);
                    
                    jobsClosed++;
                    closedJobsMap.put(job.getTitle(), count);
                    
                    System.out.println("🔒 AUTO-CLOSED: " + job.getTitle() + 
                                     " with " + count + " applications");
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalJobsChecked", openJobs.size());
            response.put("jobsClosed", jobsClosed);
            response.put("closedJobs", closedJobsMap);
            response.put("message", jobsClosed > 0 ? 
                       "✅ " + jobsClosed + " job(s) automatically closed" : 
                       "No jobs to close yet");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error checking jobs: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Get AI-powered monitoring status from Python service
     * Calls the application_monitoring_node directly
     */
    @SuppressWarnings("null")
    @GetMapping("/ai-status")
    public ResponseEntity<?> getAIMonitoringStatus() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = aiServiceUrl + "/applications";
            
            @SuppressWarnings("unchecked")
            Map<String, Object> aiResponse = restTemplate.getForObject(url, Map.class);
            
            // Add our own processing
            int count = (Integer) aiResponse.getOrDefault("count", 0);
            boolean enoughApplications = count >= MINIMUM_CANDIDATES;
            
            Map<String, Object> response = new HashMap<>();
            response.put("aiServiceConnected", true);
            response.put("applications", aiResponse.get("applications"));
            response.put("count", count);
            response.put("enoughApplications", enoughApplications);
            response.put("minimumRequired", MINIMUM_CANDIDATES);
            response.put("recommendation", enoughApplications ? 
                        "Ready to proceed with shortlisting" : 
                        "Keep monitoring for more applications");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("⚠️ AI Service not available: " + e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("aiServiceConnected", false);
            error.put("error", "AI service unavailable: " + e.getMessage());
            error.put("fallbackMode", true);
            return ResponseEntity.status(503).body(error);
        }
    }
    
    /**
     * Check if a job should accept more applications
     */
    @GetMapping("/job/{jobId}/can-accept")
    public ResponseEntity<?> canAcceptApplications(@PathVariable String jobId) {
        try {
            Job job = jobService.getJobById(jobId);
            List<JobApplication> applications = applicationRepository.findByJob(job);
            
            int count = applications.size();
            boolean isFull = count >= MINIMUM_CANDIDATES;
            boolean isOpen = job.getStatus() == Job.JobStatus.OPEN;
            boolean canAccept = isOpen && !isFull;
            
            Map<String, Object> response = new HashMap<>();
            response.put("jobId", jobId);
            response.put("canAcceptApplications", canAccept);
            response.put("isFull", isFull);
            response.put("isOpen", isOpen);
            response.put("currentCount", count);
            response.put("threshold", MINIMUM_CANDIDATES);
            
            if (!canAccept) {
                if (isFull) {
                    response.put("message", "🚫 This position has received enough applications. " +
                               "We are not accepting more candidates at this time.");
                } else if (!isOpen) {
                    response.put("message", "🚫 This job posting is currently closed.");
                }
            } else {
                response.put("message", "✅ Application form is open. " +
                           "(" + count + "/" + MINIMUM_CANDIDATES + " candidates received)");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
