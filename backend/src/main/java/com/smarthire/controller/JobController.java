package com.smarthire.controller;

import com.smarthire.dto.JobRequest;
import com.smarthire.model.Job;
import com.smarthire.service.JobService;
import com.smarthire.service.WebhookService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private WebhookService webhookService;

    // =============================================================
    //  HEALTH CHECK
    // =============================================================
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Job service is running");
    }

    // =============================================================
    //  JOB LISTING ENDPOINTS
    // =============================================================
    @GetMapping("/list")
    public ResponseEntity<List<Job>> getAllOpenJobs() {
        try {
            System.out.println("📋 GET /api/jobs/list called");
            List<Job> jobs = jobService.getAllOpenJobs();
            System.out.println("✅ Returning " + jobs.size() + " open jobs");
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            System.err.println("❌ Error in /list: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    @GetMapping("/public")
    public ResponseEntity<List<Job>> getPublicJobs() {
        try {
            System.out.println("📋 GET /api/jobs/public called");
            List<Job> jobs = jobService.getAllOpenJobs();
            System.out.println("✅ Returning " + jobs.size() + " public jobs");
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            System.err.println("❌ Error in /public: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllJobs() {
        try {
            System.out.println("========================================");
            System.out.println("📋 GET /api/jobs/all called");
            System.out.println("========================================");
            
            List<Job> jobs = jobService.getAllJobs();
            
            System.out.println("✅ Retrieved " + jobs.size() + " jobs from database");
            
            // Log first few jobs for debugging
            for (int i = 0; i < Math.min(3, jobs.size()); i++) {
                Job job = jobs.get(i);
                System.out.println("   Job " + (i+1) + ":");
                System.out.println("     ID: " + job.getId());
                System.out.println("     Title: " + job.getTitle());
                System.out.println("     Company: " + job.getCompany());
                System.out.println("     Status: " + job.getStatus());
                System.out.println("     Applications: " + job.getApplicationCount());
            }
            
            System.out.println("========================================");
            return ResponseEntity.ok(jobs);
            
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("❌ ERROR in /api/jobs/all");
            System.err.println("Error Type: " + e.getClass().getName());
            System.err.println("Error Message: " + e.getMessage());
            System.err.println("========================================");
            e.printStackTrace();
            
            // Return error response with details
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to retrieve jobs");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("type", e.getClass().getSimpleName());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // =============================================================
    //  GET JOB BY ID
    // =============================================================
    @GetMapping("/{id}")
    public ResponseEntity<?> getJobById(@PathVariable String id) {
        try {
            System.out.println("🔍 GET /api/jobs/" + id + " called");
            Job job = jobService.getJobById(id);
            System.out.println("✅ Job found: " + job.getTitle());
            return ResponseEntity.ok(job);
        } catch (Exception e) {
            System.err.println("❌ Error getting job " + id + ": " + e.getMessage());
            return ResponseEntity.status(404).body(
                Map.of("error", "Job not found", "id", id, "message", e.getMessage())
            );
        }
    }

    // =============================================================
    //  UPDATE JOB SETTINGS
    // =============================================================
    @PatchMapping("/{id}/settings")
    public ResponseEntity<?> updateJobSettings(
            @PathVariable String id,
            @RequestBody Map<String, Object> settings) {

        try {
            System.out.println("⚙️ PATCH /api/jobs/" + id + "/settings called");
            Job job = jobService.getJobById(id);

            if (settings.containsKey("maxCandidates")) {
                Integer max = (Integer) settings.get("maxCandidates");
                job.setMaxCandidates(max);
                System.out.println("   Updated maxCandidates: " + max);
            }

            if (settings.containsKey("applicationCount")) {
                Integer count = (Integer) settings.get("applicationCount");
                job.setApplicationCount(count);
                System.out.println("   Updated applicationCount: " + count);
            }

            job.onUpdate();
            Job updated = jobService.updateJobSettings(job);
            
            System.out.println("✅ Job settings updated successfully");
            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            System.err.println("❌ Error updating settings: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                Map.of("message", "Failed to update settings", "error", e.getMessage())
            );
        }
    }

    // =============================================================
    //  SEND JOB TO WEBHOOK.SITE (ONLY POST POINT)
    // =============================================================
    @PostMapping("/{id}/webhook")
    public ResponseEntity<?> postJobToWebhook(@PathVariable String id) {

        try {
            System.out.println("========================================");
            System.out.println("📤 POST /api/jobs/" + id + "/webhook called");
            System.out.println("========================================");
            
            Job job = jobService.getJobById(id);
            System.out.println("✅ Job found: " + job.getTitle());
            
            System.out.println("📤 Posting job to webhook.site...");
            Map<String, Object> webhookResult = webhookService.postJobToWebhook(job);

            boolean success = (boolean) webhookResult.getOrDefault("success", false);

            if (success) {
                System.out.println("✅ Webhook posting successful");
                System.out.println("========================================");

                return ResponseEntity.ok(
                    Map.of(
                        "success", true,
                        "message", "Job posted to webhook successfully",
                        "jobId", id
                    )
                );
            } else {
                System.out.println("⚠️ Webhook acknowledged but response was not success");
                System.out.println("========================================");

                return ResponseEntity.ok(
                    Map.of(
                        "success", false,
                        "message", "Webhook responded but failed to accept job",
                        "jobId", id
                    )
                );
            }

        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("❌ Error posting to webhook");
            System.err.println("Error: " + e.getMessage());
            System.err.println("========================================");
            e.printStackTrace();

            return ResponseEntity.status(500).body(
                Map.of(
                    "success", false,
                    "message", "Failed to post to webhook",
                    "error", e.getMessage()
                )
            );
        }
    }

    // =============================================================
    //  CLOSE JOB FROM PUBLIC PORTAL
    // =============================================================
    @PatchMapping("/{id}/close")
    public ResponseEntity<?> closeJobFromPortal(@PathVariable String id) {

        try {
            System.out.println("🔒 PATCH /api/jobs/" + id + "/close called");
            Job job = jobService.getJobById(id);

            job.setStatus(Job.JobStatus.CLOSED);
            job.onUpdate();

            jobService.updateJobSettings(job);
            
            System.out.println("✅ Job closed successfully");

            return ResponseEntity.ok(
                Map.of(
                    "success", true,
                    "message", "Job closed successfully",
                    "jobId", id
                )
            );

        } catch (Exception e) {
            System.err.println("❌ Error closing job: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                Map.of("message", "Failed to close job", "error", e.getMessage())
            );
        }
    }

    // =============================================================
    //  PUBLIC JOB CREATION (NO AUTH)
    // =============================================================
    @PostMapping
    public ResponseEntity<?> createJobPublic(@RequestBody JobRequest request) {
        try {
            System.out.println("========================================");
            System.out.println("📝 POST /api/jobs called (public job creation)");
            System.out.println("   Title: " + request.getTitle());
            System.out.println("   Company: " + request.getCompany());
            System.out.println("========================================");

            String testEmail = "test@employer.com";

            Job job = jobService.createJobWithoutAuth(request, testEmail);
            
            System.out.println("✅ Job created successfully!");
            System.out.println("   Job ID: " + job.getId());
            System.out.println("========================================");

            return ResponseEntity.ok(job);

        } catch (RuntimeException e) {
            System.err.println("❌ Job creation failed (RuntimeException): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        } catch (Exception e) {
            System.err.println("❌ Job creation failed (Exception): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                Map.of("message", "Internal server error", "error", e.getMessage())
            );
        }
    }
}
