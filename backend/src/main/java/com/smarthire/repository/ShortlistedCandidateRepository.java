package com.smarthire.repository;

import com.smarthire.model.ShortlistedCandidate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShortlistedCandidateRepository extends MongoRepository<ShortlistedCandidate, String> {
    
    /**
     * Find all shortlisted candidates for a specific job
     */
    List<ShortlistedCandidate> findByJobIdOrderByFinalScoreDesc(String jobId);
    
    /**
     * Find shortlisted candidate by application ID
     */
    Optional<ShortlistedCandidate> findByApplicationId(String applicationId);
    
    /**
     * Find by job ID and status
     */
    List<ShortlistedCandidate> findByJobIdAndStatus(String jobId, ShortlistedCandidate.ShortlistStatus status);
    
    /**
     * Find all candidates with specific status
     */
    List<ShortlistedCandidate> findByStatus(ShortlistedCandidate.ShortlistStatus status);
    
    /**
     * Find candidates by job ID with score above threshold
     */
    List<ShortlistedCandidate> findByJobIdAndFinalScoreGreaterThanEqualOrderByFinalScoreDesc(
        String jobId, Double minScore);
    
    /**
     * Count shortlisted candidates for a job
     */
    long countByJobId(String jobId);
    
    /**
     * Check if application is already shortlisted
     */
    boolean existsByApplicationId(String applicationId);
}
