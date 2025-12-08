package com.smarthire.controller;

import com.smarthire.model.JobApplication;
import com.smarthire.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    /**
     * PUBLIC ENDPOINT: Submit a job application
     * Accepts form data with resume file upload
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitApplication(
            @RequestParam("jobId") String jobId,
            @RequestParam("candidateName") String candidateName,
            @RequestParam("candidateEmail") String candidateEmail,
            @RequestParam("candidatePhone") String candidatePhone,
            @RequestParam(value = "coverLetter", required = false, defaultValue = "") String coverLetter,
            @RequestParam("resume") MultipartFile resumeFile) {
        
        try {
            System.out.println("🎯 Received application for job: " + jobId);
            System.out.println("📧 Email: " + candidateEmail);
            
            JobApplication application = applicationService.submitApplication(
                jobId, candidateName, candidateEmail, candidatePhone, coverLetter, resumeFile
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Application submitted successfully!");
            response.put("applicationId", application.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error submitting application: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to submit application: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get all applications for a specific job
     */
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<JobApplication>> getApplicationsByJob(@PathVariable String jobId) {
        try {
            System.out.println("📋 GET /api/applications/job/" + jobId);
            List<JobApplication> applications = applicationService.getApplicationsByJob(jobId);
            System.out.println("✅ Found " + applications.size() + " applications");
            
            if (!applications.isEmpty()) {
                System.out.println("   First application: " + applications.get(0).getCandidateName() + 
                                 " (" + applications.get(0).getCandidateEmail() + ")");
                System.out.println("   Status: " + applications.get(0).getStatus());
            }
            
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            System.err.println("❌ Error fetching applications: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Get application details by ID
     */
    @GetMapping("/{applicationId}")
    public ResponseEntity<JobApplication> getApplication(@PathVariable String applicationId) {
        try {
            JobApplication application = applicationService.getApplicationById(applicationId);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Download resume file
     */
    @SuppressWarnings("null")
    @GetMapping("/{applicationId}/resume")
    public ResponseEntity<byte[]> downloadResume(@PathVariable String applicationId) {
        try {
            JobApplication application = applicationService.getApplicationById(applicationId);
            byte[] resumeData = application.getResumeData();
            
            if (resumeData == null) {
                return ResponseEntity.notFound().build();
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(application.getResumeContentType()));
            headers.setContentDispositionFormData("attachment", application.getResumeFileName());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resumeData);
                    
        } catch (Exception e) {
            System.err.println("Error downloading resume: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Update application status
     */
    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String applicationId,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            String statusStr = statusUpdate.get("status");
            JobApplication.ApplicationStatus status = JobApplication.ApplicationStatus.valueOf(statusStr);
            
            JobApplication updated = applicationService.updateApplicationStatus(applicationId, status);
            return ResponseEntity.ok(updated);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
