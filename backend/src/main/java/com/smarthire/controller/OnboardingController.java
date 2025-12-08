package com.smarthire.controller;

import com.smarthire.dto.*;
import com.smarthire.model.OfferLetter;
import com.smarthire.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OnboardingController {
    
    private final OnboardingService onboardingService;
    
    /**
     * Create new onboarding
     * POST /api/onboarding
     */
    @PostMapping
    public ResponseEntity<?> createOnboarding(@RequestBody OnboardingCreateRequest request) {
        try {
            log.info("Creating onboarding for candidate: {}", request.getCandidateName());
            OnboardingDTO onboarding = onboardingService.createOnboarding(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Onboarding created successfully");
            response.put("data", onboarding);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating onboarding: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    /**
     * Get all onboardings
     * GET /api/onboarding
     */
    @GetMapping
    public ResponseEntity<?> getAllOnboardings() {
        try {
            log.info("Fetching all onboardings");
            List<OnboardingDTO> onboardings = onboardingService.getAllOnboardings();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", onboardings);
            response.put("total", onboardings.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching onboardings: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get onboarding by ID
     * GET /api/onboarding/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOnboardingById(@PathVariable String id) {
        try {
            log.info("Fetching onboarding with ID: {}", id);
            OnboardingDTO onboarding = onboardingService.getOnboardingById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", onboarding);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching onboarding: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    /**
     * Get onboarding by candidate ID
     * GET /api/onboarding/candidate/{candidateId}
     */
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<?> getOnboardingByCandidateId(@PathVariable String candidateId) {
        try {
            log.info("Fetching onboarding for candidate: {}", candidateId);
            OnboardingDTO onboarding = onboardingService.getOnboardingByCandidateId(candidateId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", onboarding);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching onboarding by candidate: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    /**
     * Get onboarding by employee ID
     * GET /api/onboarding/employee/{employeeId}
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getOnboardingByEmployeeId(@PathVariable String employeeId) {
        try {
            log.info("Fetching onboarding for employee: {}", employeeId);
            OnboardingDTO onboarding = onboardingService.getOnboardingByEmployeeId(employeeId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", onboarding);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching onboarding by employee: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    /**
     * Get onboardings by status
     * GET /api/onboarding/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOnboardingsByStatus(@PathVariable String status) {
        try {
            log.info("Fetching onboardings with status: {}", status);
            List<OnboardingDTO> onboardings = onboardingService.getOnboardingsByStatus(status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", onboardings);
            response.put("total", onboardings.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching onboardings by status: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get onboardings by current step
     * GET /api/onboarding/step/{step}
     */
    @GetMapping("/step/{step}")
    public ResponseEntity<?> getOnboardingsByStep(@PathVariable String step) {
        try {
            log.info("Fetching onboardings in step: {}", step);
            List<OnboardingDTO> onboardings = onboardingService.getOnboardingsByStep(step);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", onboardings);
            response.put("total", onboardings.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching onboardings by step: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get candidates eligible for onboarding
     * GET /api/onboarding/eligible-candidates
     */
    @GetMapping("/eligible-candidates")
    public ResponseEntity<?> getEligibleCandidates() {
        try {
            log.info("Fetching candidates eligible for onboarding");
            List<OfferLetter> eligibleCandidates = onboardingService.getEligibleCandidates();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", eligibleCandidates);
            response.put("total", eligibleCandidates.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching eligible candidates: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Upload document
     * POST /api/onboarding/upload-document
     */
    @PostMapping("/upload-document")
    public ResponseEntity<?> uploadDocument(@RequestBody DocumentUploadRequest request) {
        try {
            log.info("Uploading document for onboarding: {}", request.getOnboardingId());
            OnboardingDTO onboarding = onboardingService.uploadDocument(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Document uploaded successfully");
            response.put("data", onboarding);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading document: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    /**
     * Verify document
     * POST /api/onboarding/{id}/verify-document
     */
    @PostMapping("/{id}/verify-document")
    public ResponseEntity<?> verifyDocument(
            @PathVariable String id,
            @RequestParam String documentType,
            @RequestParam String verifiedBy,
            @RequestParam(required = false) String remarks) {
        try {
            log.info("Verifying document {} for onboarding: {}", documentType, id);
            OnboardingDTO onboarding = onboardingService.verifyDocument(id, documentType, verifiedBy, remarks);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Document verified successfully");
            response.put("data", onboarding);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error verifying document: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    /**
     * Update onboarding
     * PUT /api/onboarding/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOnboarding(
            @PathVariable String id,
            @RequestBody OnboardingUpdateRequest request) {
        try {
            log.info("Updating onboarding: {}", id);
            OnboardingDTO onboarding = onboardingService.updateOnboarding(id, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Onboarding updated successfully");
            response.put("data", onboarding);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating onboarding: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    /**
     * Complete onboarding
     * POST /api/onboarding/{id}/complete
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeOnboarding(
            @PathVariable String id,
            @RequestParam String approvedBy) {
        try {
            log.info("Completing onboarding: {}", id);
            OnboardingDTO onboarding = onboardingService.completeOnboarding(id, approvedBy);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Onboarding completed successfully");
            response.put("data", onboarding);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error completing onboarding: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    /**
     * Delete onboarding
     * DELETE /api/onboarding/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOnboarding(@PathVariable String id) {
        try {
            log.info("Deleting onboarding: {}", id);
            onboardingService.deleteOnboarding(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Onboarding deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting onboarding: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
