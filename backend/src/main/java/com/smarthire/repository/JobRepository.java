package com.smarthire.repository;

import com.smarthire.model.Job;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends MongoRepository<Job, String> {

    // Find all jobs by status (OPEN, CLOSED, DRAFT)
    List<Job> findByStatus(Job.JobStatus status);

    // Find all jobs by status, sorted by createdAt descending
    List<Job> findByStatusOrderByCreatedAtDesc(Job.JobStatus status);

    // Find all jobs (including closed), sorted by createdAt descending
    List<Job> findAllByOrderByCreatedAtDesc();

    // Optional: Find jobs by location
    List<Job> findByLocation(String location);

    // Optional: Find jobs by employment type
    List<Job> findByEmploymentType(String employmentType);

    // Optional: Find jobs by company name
    List<Job> findByCompany(String company);
    
    // Find jobs posted by a specific employer
    List<Job> findByPostedBy(String postedBy);
}
