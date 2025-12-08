package com.smarthire.repository;

import com.smarthire.model.Job;
import com.smarthire.model.JobApplication;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends MongoRepository<JobApplication, String> {
    List<JobApplication> findByJob(Job job);
    List<JobApplication> findByStatus(JobApplication.ApplicationStatus status);
}
