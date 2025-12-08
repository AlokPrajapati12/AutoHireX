package com.smarthire.controller;

import com.smarthire.service.PythonAIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private PythonAIClient pythonAIClient;

    /**
     * Health check endpoint for Python AI service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        System.out.println("🔄 AIController: Health check requested");
        Map<String, Object> response = pythonAIClient.healthCheck();

        if ("healthy".equals(response.get("status")) || "ok".equals(response.get("status"))) {
            System.out.println("✅ AIController: AI Service is healthy");
            return ResponseEntity.ok(response);
        } else {
            System.out.println("❌ AIController: AI Service is unhealthy");
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * Generate Job Description using Python AI service
     */
    @PostMapping("/generate-job-description")
    public ResponseEntity<Map<String, Object>> generateJobDescription(@RequestBody Map<String, Object> jobData) {
        System.out.println("🔄 AIController: Generate JD requested");
        System.out.println("📦 Frontend Request Data: " + jobData);

        Map<String, Object> response = new HashMap<>();

        try {
            // Prepare request for Python AI
            Map<String, Object> aiRequestData = new HashMap<>();
            aiRequestData.put("company_name", jobData.get("companyName"));
            aiRequestData.put("job_role", jobData.get("jobTitle"));
            aiRequestData.put("location", jobData.getOrDefault("location", "Remote"));
            aiRequestData.put("experience_level", jobData.getOrDefault("experienceLevel", "MID"));
            aiRequestData.put("employment_type", jobData.getOrDefault("employmentType", "FULL_TIME"));

            System.out.println("📤 Sending to AI Service: " + aiRequestData);

            Map<String, Object> aiResponse = pythonAIClient.generateJobDescription(aiRequestData);

            // Handle AI error
            if (aiResponse.containsKey("error")) {
                response.put("success", false);
                response.put("error", aiResponse.get("error"));
                return ResponseEntity.status(500).body(response);
            }

            // Return JD to frontend
            response.put("success", true);
            response.put("job_description", aiResponse.get("job_description"));
            response.put("company_name", aiResponse.get("company_name"));
            response.put("job_role", aiResponse.get("job_role"));

            System.out.println("✅ AIController: JD generated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ AIController Exception: " + e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * ⚠️ REMOVED — This caused duplicate JD posting to webhook
     * /post-job-description is disabled to avoid double + triple JD posts
     */


    /***********************
     * APPLICATION HANDLING
     ***********************/
    @GetMapping("/applications")
    public ResponseEntity<Map<String, Object>> getApplications() {
        try {
            Map<String, Object> aiResponse = pythonAIClient.getApplications();
            return ResponseEntity.ok(aiResponse);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/monitor-status")
    public ResponseEntity<Map<String, Object>> getMonitorStatus() {
        try {
            Map<String, Object> aiResponse = pythonAIClient.getMonitorStatus();
            return ResponseEntity.ok(aiResponse);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /***********************
     * SHORTLISTING
     ***********************/
    @PostMapping("/shortlist")
    public ResponseEntity<Map<String, Object>> shortlistCandidates(@RequestBody Map<String, String> request) {

        String jd = request.get("jobDescription");
        if (jd == null || jd.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Job description is required");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Map<String, Object> aiResponse = pythonAIClient.shortlistCandidates(jd);

            if (aiResponse.containsKey("error")) {
                Map<String, Object> res = new HashMap<>();
                res.put("success", false);
                res.put("error", aiResponse.get("error"));
                return ResponseEntity.status(500).body(res);
            }

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("shortlist", aiResponse.get("shortlist"));
            res.put("count", aiResponse.get("count"));

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("error", e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    /***********************
     * INTERVIEW SCHEDULING
     ***********************/
    @SuppressWarnings("unchecked")
    @PostMapping("/schedule-interviews")
    public ResponseEntity<Map<String, Object>> scheduleInterviews(@RequestBody Map<String, Object> request) {

        try {
            List<Map<String, Object>> shortlist = (List<Map<String, Object>>) request.get("shortlist");
            String jobRole = (String) request.get("jobRole");

            if (shortlist == null || jobRole == null) {
                Map<String, Object> res = new HashMap<>();
                res.put("success", false);
                res.put("message", "Missing shortlist or jobRole");
                return ResponseEntity.badRequest().body(res);
            }

            Map<String, Object> aiResponse = pythonAIClient.scheduleInterviews(shortlist, jobRole);

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("interviews", aiResponse.get("interviews"));
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("error", e.getMessage());
            return ResponseEntity.status(500).body(res);
        }
    }

    /***********************
     * AI SERVICE STATUS
     ***********************/
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAIStatus() {

        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> health = pythonAIClient.healthCheck();
            boolean ok = "healthy".equals(health.get("status")) || "ok".equals(health.get("status"));

            response.put("aiServiceAvailable", ok);
            response.put("status", ok ? "healthy" : "unavailable");
            response.put("details", health);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("aiServiceAvailable", false);
            response.put("status", "unavailable");
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }
}
