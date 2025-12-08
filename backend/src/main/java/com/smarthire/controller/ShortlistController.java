package com.smarthire.controller;

import com.smarthire.dto.ShortlistRequest;
import com.smarthire.dto.ShortlistResponse;
import com.smarthire.model.ShortlistedCandidate;
import com.smarthire.service.ShortlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ShortlistController - REST API for AI-powered candidate shortlisting
 * Implements Step 5: Shortlisting Candidates Using ATS
 */
@RestController
@RequestMapping("/api/shortlist")
public class ShortlistController {

    @Autowired
    private ShortlistService shortlistService;

    /**
     * Trigger AI shortlisting process for a specific job
     * POST /api/shortlist/process
     */
    @PostMapping("/process")
    public ResponseEntity<ShortlistResponse> processJobShortlisting(@RequestBody ShortlistRequest request) {
        System.out.println("🚀 Received shortlisting request for job: " + request.getJobId());
        
        try {
            ShortlistResponse response = shortlistService.processJobApplications(
                request.getJobId(),
                request.getMinScore(),
                request.getMaxCandidates()
            );
            
            if (response.isSuccess()) {
                System.out.println("✅ Shortlisting completed: " + 
                                 response.getShortlistedCount() + " candidates shortlisted");
            } else {
                System.err.println("❌ Shortlisting failed: " + response.getMessage());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error in shortlisting endpoint: " + e.getMessage());
            e.printStackTrace();
            
            ShortlistResponse errorResponse = new ShortlistResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Internal server error: " + e.getMessage());
            errorResponse.setJobId(request.getJobId());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Get all shortlisted candidates for a job
     * GET /api/shortlist/job/{jobId}
     */
    @GetMapping("/job/{jobId}")
    public ResponseEntity<Map<String, Object>> getShortlistedCandidates(@PathVariable String jobId) {
        try {
            List<ShortlistedCandidate> candidates = shortlistService.getShortlistedCandidates(jobId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("jobId", jobId);
            response.put("count", candidates.size());
            response.put("candidates", candidates);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching shortlisted candidates: " + e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Get a specific shortlisted candidate by ID
     * GET /api/shortlist/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getShortlistedCandidate(@PathVariable String id) {
        try {
            ShortlistedCandidate candidate = shortlistService.getShortlistedCandidateById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("candidate", candidate);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(404).body(error);
        }
    }

    /**
     * Update shortlisted candidate status
     * PUT /api/shortlist/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateCandidateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        try {
            String statusStr = request.get("status");
            
            ShortlistedCandidate updated = shortlistService.updateStatus(id, statusStr);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Status updated successfully");
            response.put("candidate", updated);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Invalid status value");
            
            return ResponseEntity.status(400).body(error);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Quick check: Get shortlist count for a job
     * GET /api/shortlist/job/{jobId}/count
     */
    @GetMapping("/job/{jobId}/count")
    public ResponseEntity<Map<String, Object>> getShortlistCount(@PathVariable String jobId) {
        try {
            List<ShortlistedCandidate> candidates = shortlistService.getShortlistedCandidates(jobId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("jobId", jobId);
            response.put("count", candidates.size());
            response.put("hasShortlist", !candidates.isEmpty());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Health check endpoint
     * GET /api/shortlist/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("service", "Shortlist Service");
        health.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(health);
    }
}
