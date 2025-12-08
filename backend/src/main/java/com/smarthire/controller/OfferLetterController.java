package com.smarthire.controller;

import com.smarthire.dto.OfferLetterRequest;
import com.smarthire.dto.OfferLetterResponse;
import com.smarthire.model.ShortlistedCandidate;
import com.smarthire.service.OfferLetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/offer-letters")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OfferLetterController {
    
    private final OfferLetterService offerLetterService;
    
    /**
     * Generate new offer letter
     * POST /api/offer-letters/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<OfferLetterResponse> generateOfferLetter(
            @RequestBody OfferLetterRequest request) {
        try {
            OfferLetterResponse response = offerLetterService.generateOfferLetter(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            OfferLetterResponse errorResponse = new OfferLetterResponse();
            errorResponse.setMessage("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * Get all offer letters
     * GET /api/offer-letters
     */
    @GetMapping
    public ResponseEntity<List<OfferLetterResponse>> getAllOfferLetters() {
        try {
            List<OfferLetterResponse> offerLetters = offerLetterService.getAllOfferLetters();
            return ResponseEntity.ok(offerLetters);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get offer letter by ID
     * GET /api/offer-letters/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<OfferLetterResponse> getOfferLetterById(@PathVariable String id) {
        try {
            OfferLetterResponse response = offerLetterService.getOfferLetterById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Get offer letter by candidate ID
     * GET /api/offer-letters/candidate/{candidateId}
     */
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<OfferLetterResponse> getOfferLetterByCandidateId(
            @PathVariable String candidateId) {
        try {
            OfferLetterResponse response = offerLetterService.getOfferLetterByCandidateId(candidateId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Get offer letters by status
     * GET /api/offer-letters/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OfferLetterResponse>> getOfferLettersByStatus(
            @PathVariable String status) {
        try {
            List<OfferLetterResponse> offerLetters = offerLetterService.getOfferLettersByStatus(status);
            return ResponseEntity.ok(offerLetters);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Send offer letter to candidate
     * POST /api/offer-letters/{id}/send
     */
    @PostMapping("/{id}/send")
    public ResponseEntity<OfferLetterResponse> sendOfferLetter(@PathVariable String id) {
        try {
            OfferLetterResponse response = offerLetterService.sendOfferLetter(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            OfferLetterResponse errorResponse = new OfferLetterResponse();
            errorResponse.setMessage("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * Accept offer letter
     * POST /api/offer-letters/{id}/accept
     */
    @PostMapping("/{id}/accept")
    public ResponseEntity<OfferLetterResponse> acceptOfferLetter(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String acceptanceMethod = body != null ? body.get("acceptanceMethod") : "PORTAL";
            OfferLetterResponse response = offerLetterService.acceptOfferLetter(id, acceptanceMethod);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            OfferLetterResponse errorResponse = new OfferLetterResponse();
            errorResponse.setMessage("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * Reject offer letter
     * POST /api/offer-letters/{id}/reject
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<OfferLetterResponse> rejectOfferLetter(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            String rejectionReason = body.get("rejectionReason");
            OfferLetterResponse response = offerLetterService.rejectOfferLetter(id, rejectionReason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            OfferLetterResponse errorResponse = new OfferLetterResponse();
            errorResponse.setMessage("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * Download offer letter (increment download count)
     * GET /api/offer-letters/{id}/download
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<OfferLetterResponse> downloadOfferLetter(@PathVariable String id) {
        try {
            OfferLetterResponse response = offerLetterService.downloadOfferLetter(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Get candidates eligible for offer letter (cleared HR round)
     * GET /api/offer-letters/eligible-candidates
     */
    @GetMapping("/eligible-candidates")
    public ResponseEntity<List<ShortlistedCandidate>> getEligibleCandidates() {
        try {
            List<ShortlistedCandidate> candidates = offerLetterService.getCandidatesEligibleForOfferLetter();
            return ResponseEntity.ok(candidates);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
