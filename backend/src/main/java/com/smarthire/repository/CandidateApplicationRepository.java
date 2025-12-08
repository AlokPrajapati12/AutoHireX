package com.smarthire.repository;

import com.smarthire.model.CandidateApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CandidateApplicationRepository extends MongoRepository<CandidateApplication, String> {
    
    // Find all applications for a specific job
    List<CandidateApplication> findByJobId(String jobId);
    
    // Find applications by candidate email
    List<CandidateApplication> findByEmail(String email);
    
    // Find applications by status
    List<CandidateApplication> findByStatus(CandidateApplication.ApplicationStatus status);
    
    // Find applications by job ID and status
    List<CandidateApplication> findByJobIdAndStatus(String jobId, CandidateApplication.ApplicationStatus status);
    
    // Find applications with pagination
    Page<CandidateApplication> findByStatus(CandidateApplication.ApplicationStatus status, Pageable pageable);
}
