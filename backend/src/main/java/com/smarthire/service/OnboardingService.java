package com.smarthire.service;

import com.smarthire.dto.*;
import com.smarthire.model.Onboarding;
import com.smarthire.model.OfferLetter;
import com.smarthire.repository.OnboardingRepository;
import com.smarthire.repository.OfferLetterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingService {
    
    private final OnboardingRepository onboardingRepository;
    private final OfferLetterRepository offerLetterRepository;
    
    /**
     * Create onboarding for a candidate who accepted offer letter
     */
    @Transactional
    public OnboardingDTO createOnboarding(OnboardingCreateRequest request) {
        log.info("Creating onboarding for candidate: {}", request.getCandidateId());
        
        // Validate offer letter is accepted
        OfferLetter offerLetter = offerLetterRepository.findById(request.getOfferLetterId())
            .orElseThrow(() -> new RuntimeException("Offer letter not found"));
        
        if (!Boolean.TRUE.equals(offerLetter.getIsAccepted()) || !"ACCEPTED".equals(offerLetter.getStatus())) {
            throw new RuntimeException("Offer letter must be accepted before onboarding");
        }
        
        // Check if onboarding already exists for this candidate
        if (onboardingRepository.existsByCandidateId(request.getCandidateId())) {
            throw new RuntimeException("Onboarding already exists for this candidate");
        }
        
        // Check if this offer letter already used
        if (onboardingRepository.existsByOfferLetterId(request.getOfferLetterId())) {
            throw new RuntimeException("This offer letter is already used for onboarding");
        }
        
        // Create new onboarding
        Onboarding onboarding = new Onboarding(
            request.getCandidateId(),
            request.getOfferLetterId(),
            request.getApplicationId(),
            request.getJobId(),
            request.getCandidateName(),
            request.getCandidateEmail(),
            request.getJobTitle(),
            request.getJoiningDate()
        );
        
        // Set additional details
        onboarding.setPersonalEmail(request.getPersonalEmail());
        onboarding.setCandidatePhone(request.getCandidatePhone());
        onboarding.setDepartment(request.getDepartment());
        onboarding.setDesignation(request.getDesignation());
        onboarding.setReportingManager(request.getReportingManager());
        onboarding.setWorkLocation(request.getWorkLocation());
        onboarding.setEmergencyContactName(request.getEmergencyContactName());
        onboarding.setEmergencyContactPhone(request.getEmergencyContactPhone());
        onboarding.setEmergencyContactRelation(request.getEmergencyContactRelation());
        onboarding.setBloodGroup(request.getBloodGroup());
        onboarding.setOnboardingCoordinator(request.getOnboardingCoordinator());
        onboarding.setProbationPeriod(request.getProbationPeriod());
        onboarding.setAdditionalNotes(request.getAdditionalNotes());
        onboarding.setActualJoiningDate(request.getActualJoiningDate());
        
        // Calculate probation end date
        if (request.getProbationPeriod() != null && request.getJoiningDate() != null) {
            onboarding.setProbationEndDate(request.getJoiningDate().plusMonths(request.getProbationPeriod()));
        }
        
        Onboarding saved = onboardingRepository.save(onboarding);
        log.info("Onboarding created successfully with ID: {}", saved.getId());
        
        return convertToDTO(saved);
    }
    
    /**
     * Get all onboardings
     */
    public List<OnboardingDTO> getAllOnboardings() {
        return onboardingRepository.findAll()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get onboarding by ID
     */
    public OnboardingDTO getOnboardingById(String id) {
        Onboarding onboarding = onboardingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Onboarding not found with ID: " + id));
        return convertToDTO(onboarding);
    }
    
    /**
     * Get onboarding by candidate ID
     */
    public OnboardingDTO getOnboardingByCandidateId(String candidateId) {
        Onboarding onboarding = onboardingRepository.findByCandidateId(candidateId)
            .orElseThrow(() -> new RuntimeException("Onboarding not found for candidate: " + candidateId));
        return convertToDTO(onboarding);
    }
    
    /**
     * Get onboarding by employee ID
     */
    public OnboardingDTO getOnboardingByEmployeeId(String employeeId) {
        Onboarding onboarding = onboardingRepository.findByEmployeeId(employeeId)
            .orElseThrow(() -> new RuntimeException("Onboarding not found for employee: " + employeeId));
        return convertToDTO(onboarding);
    }
    
    /**
     * Get onboardings by status
     */
    public List<OnboardingDTO> getOnboardingsByStatus(String status) {
        return onboardingRepository.findByStatus(status)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get onboardings by current step
     */
    public List<OnboardingDTO> getOnboardingsByStep(String step) {
        return onboardingRepository.findByCurrentStep(step)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get candidates eligible for onboarding (accepted offer letters)
     */
    public List<OfferLetter> getEligibleCandidates() {
        // Get all accepted offer letters
        List<OfferLetter> acceptedOffers = offerLetterRepository.findByStatus("ACCEPTED");
        
        // Filter out those who already have onboarding
        return acceptedOffers.stream()
            .filter(offer -> !onboardingRepository.existsByOfferLetterId(offer.getId()))
            .collect(Collectors.toList());
    }
    
    /**
     * Upload document for onboarding
     */
    @Transactional
    public OnboardingDTO uploadDocument(DocumentUploadRequest request) {
        log.info("Uploading document {} for onboarding: {}", request.getDocumentType(), request.getOnboardingId());
        
        Onboarding onboarding = onboardingRepository.findById(request.getOnboardingId())
            .orElseThrow(() -> new RuntimeException("Onboarding not found"));
        
        // Find the document in the list
        Onboarding.OnboardingDocument document = onboarding.getDocuments().stream()
            .filter(doc -> doc.getDocumentType().equals(request.getDocumentType()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Document type not found: " + request.getDocumentType()));
        
        // Update document details
        document.setDocumentUrl(request.getDocumentUrl());
        document.setDocumentName(request.getDocumentName());
        document.setFileType(request.getFileType());
        document.setFileSize(request.getFileSize());
        document.setIsSubmitted(true);
        document.setSubmittedAt(LocalDateTime.now());
        document.setRemarks(request.getRemarks());
        
        // Update status if all required documents are submitted
        updateOnboardingStatus(onboarding);
        
        onboarding.setUpdatedAt(LocalDateTime.now());
        Onboarding saved = onboardingRepository.save(onboarding);
        
        log.info("Document uploaded successfully for onboarding: {}", saved.getId());
        return convertToDTO(saved);
    }
    
    /**
     * Verify document
     */
    @Transactional
    public OnboardingDTO verifyDocument(String onboardingId, String documentType, String verifiedBy, String remarks) {
        log.info("Verifying document {} for onboarding: {}", documentType, onboardingId);
        
        Onboarding onboarding = onboardingRepository.findById(onboardingId)
            .orElseThrow(() -> new RuntimeException("Onboarding not found"));
        
        // Find the document
        Onboarding.OnboardingDocument document = onboarding.getDocuments().stream()
            .filter(doc -> doc.getDocumentType().equals(documentType))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Document type not found: " + documentType));
        
        // Verify document
        document.setIsVerified(true);
        document.setVerifiedAt(LocalDateTime.now());
        document.setVerifiedBy(verifiedBy);
        if (remarks != null) {
            document.setRemarks(remarks);
        }
        
        // Update status if all required documents are verified
        updateOnboardingStatus(onboarding);
        
        onboarding.setUpdatedAt(LocalDateTime.now());
        Onboarding saved = onboardingRepository.save(onboarding);
        
        log.info("Document verified successfully for onboarding: {}", saved.getId());
        return convertToDTO(saved);
    }
    
    /**
     * Update onboarding details
     */
    @Transactional
    public OnboardingDTO updateOnboarding(String id, OnboardingUpdateRequest request) {
        log.info("Updating onboarding: {}", id);
        
        Onboarding onboarding = onboardingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Onboarding not found"));
        
        // Update status and step
        if (request.getStatus() != null) {
            onboarding.setStatus(request.getStatus());
        }
        if (request.getCurrentStep() != null) {
            onboarding.setCurrentStep(request.getCurrentStep());
        }
        
        // Update system setup
        if (request.getEmailAccountCreated() != null) {
            onboarding.setEmailAccountCreated(request.getEmailAccountCreated());
        }
        if (request.getSystemAccessProvided() != null) {
            onboarding.setSystemAccessProvided(request.getSystemAccessProvided());
        }
        if (request.getIdCardIssued() != null) {
            onboarding.setIdCardIssued(request.getIdCardIssued());
        }
        if (request.getWorkstationAssigned() != null) {
            onboarding.setWorkstationAssigned(request.getWorkstationAssigned());
        }
        if (request.getWorkstationNumber() != null) {
            onboarding.setWorkstationNumber(request.getWorkstationNumber());
        }
        
        // Update orientation
        if (request.getOrientationCompleted() != null) {
            onboarding.setOrientationCompleted(request.getOrientationCompleted());
        }
        if (request.getOrientationDate() != null) {
            onboarding.setOrientationDate(request.getOrientationDate());
        }
        if (request.getOrientationConductedBy() != null) {
            onboarding.setOrientationConductedBy(request.getOrientationConductedBy());
        }
        if (request.getOrientationRemarks() != null) {
            onboarding.setOrientationRemarks(request.getOrientationRemarks());
        }
        
        // Update background verification
        if (request.getBackgroundVerificationStatus() != null) {
            onboarding.setBackgroundVerificationStatus(request.getBackgroundVerificationStatus());
        }
        if (request.getBackgroundVerificationDate() != null) {
            onboarding.setBackgroundVerificationDate(request.getBackgroundVerificationDate());
        }
        if (request.getBackgroundVerificationRemarks() != null) {
            onboarding.setBackgroundVerificationRemarks(request.getBackgroundVerificationRemarks());
        }
        
        // Update dates
        if (request.getActualJoiningDate() != null) {
            onboarding.setActualJoiningDate(request.getActualJoiningDate());
        }
        
        // Update HR details
        if (request.getHrRemarks() != null) {
            onboarding.setHrRemarks(request.getHrRemarks());
        }
        if (request.getApprovedBy() != null) {
            onboarding.setApprovedBy(request.getApprovedBy());
            onboarding.setApprovedAt(LocalDateTime.now());
        }
        if (request.getAdditionalNotes() != null) {
            onboarding.setAdditionalNotes(request.getAdditionalNotes());
        }
        
        // Update completion status
        if ("COMPLETED".equals(request.getStatus())) {
            onboarding.setOnboardingCompletionDate(LocalDate.now());
        }
        
        onboarding.updateCompletionPercentage();
        onboarding.setUpdatedAt(LocalDateTime.now());
        
        Onboarding saved = onboardingRepository.save(onboarding);
        log.info("Onboarding updated successfully: {}", saved.getId());
        
        return convertToDTO(saved);
    }
    
    /**
     * Complete onboarding
     */
    @Transactional
    public OnboardingDTO completeOnboarding(String id, String approvedBy) {
        log.info("Completing onboarding: {}", id);
        
        Onboarding onboarding = onboardingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Onboarding not found"));
        
        // Validate all required documents are verified
        long unverifiedRequired = onboarding.getDocuments().stream()
            .filter(Onboarding.OnboardingDocument::getIsRequired)
            .filter(doc -> !Boolean.TRUE.equals(doc.getIsVerified()))
            .count();
        
        if (unverifiedRequired > 0) {
            throw new RuntimeException("Cannot complete onboarding. All required documents must be verified.");
        }
        
        // Validate system setup
        if (!Boolean.TRUE.equals(onboarding.getEmailAccountCreated()) ||
            !Boolean.TRUE.equals(onboarding.getSystemAccessProvided()) ||
            !Boolean.TRUE.equals(onboarding.getIdCardIssued())) {
            throw new RuntimeException("Cannot complete onboarding. System setup is incomplete.");
        }
        
        // Validate orientation
        if (!Boolean.TRUE.equals(onboarding.getOrientationCompleted())) {
            throw new RuntimeException("Cannot complete onboarding. Orientation must be completed.");
        }
        
        // Complete onboarding
        onboarding.setStatus("COMPLETED");
        onboarding.setCurrentStep("COMPLETED");
        onboarding.setOnboardingCompletionDate(LocalDate.now());
        onboarding.setApprovedBy(approvedBy);
        onboarding.setApprovedAt(LocalDateTime.now());
        onboarding.updateCompletionPercentage();
        onboarding.setUpdatedAt(LocalDateTime.now());
        
        Onboarding saved = onboardingRepository.save(onboarding);
        log.info("Onboarding completed successfully: {}", saved.getId());
        
        return convertToDTO(saved);
    }
    
    /**
     * Delete onboarding
     */
    @Transactional
    public void deleteOnboarding(String id) {
        log.info("Deleting onboarding: {}", id);
        
        if (!onboardingRepository.existsById(id)) {
            throw new RuntimeException("Onboarding not found");
        }
        
        onboardingRepository.deleteById(id);
        log.info("Onboarding deleted successfully");
    }
    
    /**
     * Helper: Update onboarding status based on document submission
     */
    private void updateOnboardingStatus(Onboarding onboarding) {
        // Count required documents
        long totalRequired = onboarding.getDocuments().stream()
            .filter(Onboarding.OnboardingDocument::getIsRequired)
            .count();
        
        long submittedRequired = onboarding.getDocuments().stream()
            .filter(Onboarding.OnboardingDocument::getIsRequired)
            .filter(Onboarding.OnboardingDocument::getIsSubmitted)
            .count();
        
        long verifiedRequired = onboarding.getDocuments().stream()
            .filter(Onboarding.OnboardingDocument::getIsRequired)
            .filter(Onboarding.OnboardingDocument::getIsVerified)
            .count();
        
        // Update status
        if (verifiedRequired == totalRequired) {
            onboarding.setStatus("VERIFIED");
            onboarding.setCurrentStep("SYSTEM_SETUP");
        } else if (submittedRequired == totalRequired) {
            onboarding.setStatus("DOCUMENTS_SUBMITTED");
            onboarding.setCurrentStep("VERIFICATION");
        } else if (submittedRequired > 0) {
            onboarding.setStatus("PENDING");
            onboarding.setCurrentStep("DOCUMENT_COLLECTION");
        }
        
        // Update completion percentage
        onboarding.updateCompletionPercentage();
    }
    
    /**
     * Convert entity to DTO
     */
    private OnboardingDTO convertToDTO(Onboarding onboarding) {
        OnboardingDTO dto = new OnboardingDTO();
        
        dto.setId(onboarding.getId());
        dto.setCandidateId(onboarding.getCandidateId());
        dto.setOfferLetterId(onboarding.getOfferLetterId());
        dto.setApplicationId(onboarding.getApplicationId());
        dto.setJobId(onboarding.getJobId());
        dto.setEmployeeId(onboarding.getEmployeeId());
        dto.setCandidateName(onboarding.getCandidateName());
        dto.setCandidateEmail(onboarding.getCandidateEmail());
        dto.setCandidatePhone(onboarding.getCandidatePhone());
        dto.setPersonalEmail(onboarding.getPersonalEmail());
        dto.setJobTitle(onboarding.getJobTitle());
        dto.setDepartment(onboarding.getDepartment());
        dto.setDesignation(onboarding.getDesignation());
        dto.setReportingManager(onboarding.getReportingManager());
        dto.setWorkLocation(onboarding.getWorkLocation());
        dto.setJoiningDate(onboarding.getJoiningDate());
        dto.setActualJoiningDate(onboarding.getActualJoiningDate());
        dto.setOnboardingStartDate(onboarding.getOnboardingStartDate());
        dto.setOnboardingCompletionDate(onboarding.getOnboardingCompletionDate());
        dto.setStatus(onboarding.getStatus());
        dto.setCurrentStep(onboarding.getCurrentStep());
        dto.setCompletionPercentage(onboarding.getCompletionPercentage());
        dto.setEmailAccountCreated(onboarding.getEmailAccountCreated());
        dto.setSystemAccessProvided(onboarding.getSystemAccessProvided());
        dto.setIdCardIssued(onboarding.getIdCardIssued());
        dto.setWorkstationAssigned(onboarding.getWorkstationAssigned());
        dto.setWorkstationNumber(onboarding.getWorkstationNumber());
        dto.setOrientationCompleted(onboarding.getOrientationCompleted());
        dto.setOrientationDate(onboarding.getOrientationDate());
        dto.setOrientationConductedBy(onboarding.getOrientationConductedBy());
        dto.setOrientationRemarks(onboarding.getOrientationRemarks());
        dto.setOnboardingCoordinator(onboarding.getOnboardingCoordinator());
        dto.setHrRemarks(onboarding.getHrRemarks());
        dto.setApprovedBy(onboarding.getApprovedBy());
        dto.setBackgroundVerificationRequired(onboarding.getBackgroundVerificationRequired());
        dto.setBackgroundVerificationStatus(onboarding.getBackgroundVerificationStatus());
        dto.setBackgroundVerificationDate(onboarding.getBackgroundVerificationDate());
        dto.setBackgroundVerificationRemarks(onboarding.getBackgroundVerificationRemarks());
        dto.setProbationPeriod(onboarding.getProbationPeriod());
        dto.setProbationEndDate(onboarding.getProbationEndDate());
        dto.setEmergencyContactName(onboarding.getEmergencyContactName());
        dto.setEmergencyContactPhone(onboarding.getEmergencyContactPhone());
        dto.setEmergencyContactRelation(onboarding.getEmergencyContactRelation());
        dto.setBloodGroup(onboarding.getBloodGroup());
        dto.setAdditionalNotes(onboarding.getAdditionalNotes());
        
        // Convert documents
        if (onboarding.getDocuments() != null) {
            List<OnboardingDTO.DocumentDTO> documentDTOs = onboarding.getDocuments().stream()
                .map(doc -> new OnboardingDTO.DocumentDTO(
                    doc.getDocumentType(),
                    doc.getDocumentName(),
                    doc.getDocumentUrl(),
                    doc.getIsRequired(),
                    doc.getIsSubmitted(),
                    doc.getIsVerified(),
                    doc.getVerifiedBy(),
                    doc.getRemarks(),
                    doc.getFileSize(),
                    doc.getFileType()
                ))
                .collect(Collectors.toList());
            dto.setDocuments(documentDTOs);
        }
        
        return dto;
    }
}
