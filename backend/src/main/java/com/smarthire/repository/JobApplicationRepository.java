package com.smarthire.repository;

import com.smarthire.model.JobApplication;
import com.smarthire.model.Job;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplicationRepository extends MongoRepository<JobApplication, String> {

    List<JobApplication> findByJob(Job job);
    List<JobApplication> findByJobOrderByMatchScoreDesc(Job job);
}
