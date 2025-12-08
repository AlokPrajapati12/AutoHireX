package com.smarthire.repository;

import com.smarthire.model.OfferLetter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OfferLetterRepository extends MongoRepository<OfferLetter, String> {
    
    // Find by candidate
    Optional<OfferLetter> findByCandidateId(String candidateId);
    List<OfferLetter> findAllByCandidateId(String candidateId);
    
    // Find by application
    Optional<OfferLetter> findByApplicationId(String applicationId);
    
    // Find by job
    List<OfferLetter> findByJobId(String jobId);
    
    // Find by status
    List<OfferLetter> findByStatus(String status);
    
    // Find by offer letter number
    Optional<OfferLetter> findByOfferLetterNumber(String offerLetterNumber);
    
    // Find by candidate email
    Optional<OfferLetter> findByCandidateEmail(String email);
    
    // Find by date range
    List<OfferLetter> findByOfferDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Find pending offers (not accepted/rejected)
    List<OfferLetter> findByStatusIn(List<String> statuses);
    
    // Find by joining date
    List<OfferLetter> findByJoiningDate(LocalDate joiningDate);
    
    // Count by status
    long countByStatus(String status);
}
