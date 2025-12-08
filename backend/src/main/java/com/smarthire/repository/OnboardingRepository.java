package com.smarthire.repository;

import com.smarthire.model.Onboarding;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OnboardingRepository extends MongoRepository<Onboarding, String> {
    
    // Find by candidate
    Optional<Onboarding> findByCandidateId(String candidateId);
    
    // Find by offer letter
    Optional<Onboarding> findByOfferLetterId(String offerLetterId);
    
    // Find by employee ID
    Optional<Onboarding> findByEmployeeId(String employeeId);
    
    // Find by status
    List<Onboarding> findByStatus(String status);
    
    // Find by current step
    List<Onboarding> findByCurrentStep(String currentStep);
    
    // Find by joining date
    List<Onboarding> findByJoiningDate(LocalDate joiningDate);
    List<Onboarding> findByJoiningDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Find by department
    List<Onboarding> findByDepartment(String department);
    
    // Find by onboarding coordinator
    List<Onboarding> findByOnboardingCoordinator(String coordinator);
    
    // Find pending onboardings
    List<Onboarding> findByStatusIn(List<String> statuses);
    
    // Find by background verification status
    List<Onboarding> findByBackgroundVerificationStatus(String verificationStatus);
    
    // Check if candidate already has onboarding
    boolean existsByCandidateId(String candidateId);
    
    // Check if offer letter already used for onboarding
    boolean existsByOfferLetterId(String offerLetterId);
}
