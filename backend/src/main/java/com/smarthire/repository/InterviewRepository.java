package com.smarthire.repository;

import com.smarthire.model.Interview;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends MongoRepository<Interview, String> {
    
    // Find all interviews for a job
    List<Interview> findByJobId(String jobId);
    
    // Find all interviews for a candidate
    List<Interview> findByApplicationId(String applicationId);
    
    // Find interviews for a shortlisted candidate
    List<Interview> findByShortlistedCandidateId(String shortlistedCandidateId);
    
    // Find interviews by round
    List<Interview> findByJobIdAndInterviewRound(String jobId, String interviewRound);
    
    // Find interviews by status
    List<Interview> findByJobIdAndStatus(String jobId, String status);
    
    // Find scheduled interviews between dates
    List<Interview> findByScheduledDateBetween(LocalDateTime start, LocalDateTime end);
    
    // Find interviews by candidate email
    List<Interview> findByCandidateEmail(String email);
    
    // Find completed interviews for a candidate
    List<Interview> findByApplicationIdAndStatus(String applicationId, String status);
    
    // Find latest interview round for a candidate
    Optional<Interview> findFirstByApplicationIdOrderByRoundNumberDesc(String applicationId);
    
    // Count interviews by status
    Long countByJobIdAndStatus(String jobId, String status);
    
    // Check if candidate has scheduled interview
    Boolean existsByApplicationIdAndStatus(String applicationId, String status);
    
    // Find interviews needing reminders (scheduled in next 24 hours, reminder not sent)
    List<Interview> findByScheduledDateBetweenAndReminderSentFalse(LocalDateTime start, LocalDateTime end);
    
    // Find interviews by round and decision
    List<Interview> findByInterviewRoundAndDecision(String interviewRound, String decision);
}
