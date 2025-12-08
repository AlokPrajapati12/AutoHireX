package com.smarthire.service;

import com.smarthire.dto.JobRequest;
import com.smarthire.model.Job;
import com.smarthire.model.User;
import com.smarthire.repository.JobRepository;
import com.smarthire.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WebhookService webhookService;

    @Autowired
    private com.smarthire.repository.JobApplicationRepository applicationRepository;

    // Create a new job posting
    public Job createJob(JobRequest request, String employerEmail) {
        System.out.println("JobService.createJob called for email: " + employerEmail);
        
        User employer = userRepository.findByEmail(employerEmail)
                .orElseThrow(() -> {
                    System.err.println("Employer not found in database: " + employerEmail);
                    return new RuntimeException("Employer not found: " + employerEmail);
                });

        System.out.println("Employer found: " + employer.getEmail() + " (ID: " + employer.getId() + ")");

        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setCompany(request.getCompany());
        job.setLocation(request.getLocation());
        job.setEmploymentType(request.getEmploymentType());
        job.setExperienceLevel(request.getExperienceLevel());
        job.setRequiredSkills(request.getRequiredSkills());
        job.setSalaryRange(request.getSalaryRange());
        job.setPostedBy(employerEmail);  // Track who posted this job
        job.setStatus(Job.JobStatus.OPEN);
        job.onCreate(); // set createdAt and updatedAt

        System.out.println("Saving job to MongoDB...");
        Job savedJob = jobRepository.save(job);
        System.out.println("Job saved successfully with ID: " + savedJob.getId());
        
        // Post job to webhook.site
        webhookService.postJobToWebhook(savedJob);
        
        return savedJob;
    }

    // Update a job posting
    public Job updateJob(String jobId, JobRequest request, String employerEmail) {
        @SuppressWarnings("null")
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setCompany(request.getCompany());
        job.setLocation(request.getLocation());
        job.setEmploymentType(request.getEmploymentType());
        job.setExperienceLevel(request.getExperienceLevel());
        job.setRequiredSkills(request.getRequiredSkills());
        job.setSalaryRange(request.getSalaryRange());
        job.onUpdate(); // update timestamp

        return jobRepository.save(job);
    }

    // Delete a job posting
    @SuppressWarnings("null")
    public void deleteJob(String jobId, String employerEmail) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        jobRepository.delete(job);
    }

    // Get all jobs posted by an employer
    public List<Job> getEmployerJobs(String employerEmail) {
        User employer = userRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new RuntimeException("Employer not found"));
        return jobRepository.findByPostedBy(employerEmail);
    }

    // Get all open jobs, sorted by createdAt descending
    public List<Job> getAllOpenJobs() {
        return jobRepository.findByStatusOrderByCreatedAtDesc(Job.JobStatus.OPEN);
    }

    // Get all jobs (including closed), sorted by createdAt descending
    public List<Job> getAllJobs() {
        return jobRepository.findAllByOrderByCreatedAtDesc();
    }

    // Get job by ID
    @SuppressWarnings("null")
    public Job getJobById(String jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        
        // Sync application count with actual database count
        long actualCount = applicationRepository.findByJob(job).size();
        if (job.getApplicationCount() == null || job.getApplicationCount() != actualCount) {
            System.out.println("🔄 Syncing application count for job: " + jobId);
            System.out.println("   Stored count: " + job.getApplicationCount());
            System.out.println("   Actual count: " + actualCount);
            job.setApplicationCount((int) actualCount);
            jobRepository.save(job);
        }
        
        return job;
    }

    // Update job settings
    @SuppressWarnings("null")
    public Job updateJobSettings(Job job) {
        return jobRepository.save(job);
    }

    /**
     * Create job WITHOUT authentication and WITHOUT posting to webhook
     * Used by Step 2 (Review & Approve) - ONLY saves to MongoDB
     */
    public Job createJobWithoutAuth(JobRequest request, String testEmployerEmail) {
        System.out.println("========================================");
        System.out.println("💾 SAVING JOB TO DATABASE ONLY");
        System.out.println("(No webhook posting at this step)");
        System.out.println("========================================");
        System.out.println("Using test employer email: " + testEmployerEmail);
        
        // Try to find the test employer, create if doesn't exist
        User employer = userRepository.findByEmail(testEmployerEmail)
                .orElseGet(() -> {
                    System.out.println("⚠️  Test employer not found, creating temporary employer record");
                    User testEmployer = new User();
                    testEmployer.setEmail(testEmployerEmail);
                    testEmployer.setFirstName("Test");
                    testEmployer.setLastName("Employer");
                    testEmployer.setRole(User.UserRole.EMPLOYER);
                    testEmployer.setPassword("temp123"); // This won't be used
                    testEmployer.onCreate();
                    return userRepository.save(testEmployer);
                });

        System.out.println("✅ Employer record ready: " + employer.getEmail());

        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setCompany(request.getCompany());
        job.setLocation(request.getLocation());
        job.setEmploymentType(request.getEmploymentType());
        job.setExperienceLevel(request.getExperienceLevel());
        job.setRequiredSkills(request.getRequiredSkills());
        job.setSalaryRange(request.getSalaryRange());
        job.setPostedBy(testEmployerEmail);  // Track who posted this job
        job.setStatus(Job.JobStatus.OPEN);
        // Note: Job model doesn't have employer field
        // If you need to track employer, add a field to Job model
        job.onCreate();

        System.out.println("💾 Saving job to MongoDB...");
        Job savedJob = jobRepository.save(job);
        System.out.println("========================================");
        System.out.println("✅ JOB SAVED TO DATABASE!");
        System.out.println("MongoDB ID: " + savedJob.getId());
        System.out.println("Title: " + savedJob.getTitle());
        System.out.println("⚠️  NOT posted to webhook yet");
        System.out.println("(Webhook posting happens in Step 3)");
        System.out.println("========================================");
        
        // DO NOT post to webhook here - that happens in Step 3!
        
        return savedJob;
    }
}
