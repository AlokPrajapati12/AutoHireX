package com.smarthire.service;

import com.smarthire.model.Job;
import com.smarthire.model.JobApplication;
import com.smarthire.repository.JobApplicationRepository;
import com.smarthire.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ApplicationService {

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private com.smarthire.service.monitoring.ApplicationMonitoringService monitoringService;

    /**
     * Submit a job application with resume upload
     * This is for public (non-authenticated) candidates
     */
    public JobApplication submitApplication(
            String jobId, 
            String candidateName,
            String candidateEmail,
            String candidatePhone,
            String coverLetter,
            MultipartFile resumeFile) throws IOException {
        
        System.out.println("📝 Processing application for job: " + jobId);
        System.out.println("👤 Candidate: " + candidateName + " (" + candidateEmail + ")");
        
        // Get the job
        @SuppressWarnings("null")
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with ID: " + jobId));
        
        // Check if job can accept applications
        if (!job.canAcceptApplications()) {
            if (job.isFull()) {
                throw new RuntimeException("This job has reached its maximum number of candidates (" + 
                    job.getMaxCandidates() + "). Applications are now closed.");
            }
            throw new RuntimeException("This job is not accepting applications at the moment.");
        }
        
        // Create application
        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setCandidateName(candidateName);
        application.setCandidateEmail(candidateEmail);
        application.setCandidatePhone(candidatePhone);
        application.setCoverLetter(coverLetter);
        
        // Store resume file
        if (resumeFile != null && !resumeFile.isEmpty()) {
            application.setResumeFileName(resumeFile.getOriginalFilename());
            application.setResumeContentType(resumeFile.getContentType());
            application.setResumeData(resumeFile.getBytes());
            System.out.println("📎 Resume uploaded: " + resumeFile.getOriginalFilename() + 
                             " (" + resumeFile.getSize() + " bytes)");
        }
        
        application.onCreate();
        
        // Save to MongoDB
        JobApplication savedApplication = applicationRepository.save(application);
        System.out.println("✅ Application saved to MongoDB with ID: " + savedApplication.getId());
        
        // Increment job application count
        job.incrementApplicationCount();
        job.onUpdate();
        jobRepository.save(job);
        System.out.println("📊 Job application count updated: " + job.getApplicationCount() + 
            (job.getMaxCandidates() != null ? "/" + job.getMaxCandidates() : ""));
        
        
        // 🤖 AI MONITORING: Check if job has enough applications
        System.out.println("🤖 AI: Checking if job has enough applications...");
        boolean hasEnoughApplications = monitoringService.hasEnoughApplications(jobId);
        
        // Get actual application count
        List<JobApplication> allApplications = applicationRepository.findByJob(job);
        int actualApplicationCount = allApplications.size();
        
        if (hasEnoughApplications && job.getStatus() == Job.JobStatus.OPEN) {
            System.out.println("🔒 AI: AUTO-CLOSING JOB - Enough candidates received!");
            job.setStatus(Job.JobStatus.CLOSED);
            job.onUpdate();
            jobRepository.save(job);
            System.out.println("✅ Job \"" + job.getTitle() + "\" closed automatically");
        } else if (hasEnoughApplications) {
            System.out.println("ℹ️ AI: Job already closed");
        } else {
            System.out.println("📊 AI: Still waiting for more applications (" + actualApplicationCount + "/3)");
        }
        
        return savedApplication;
    }

    /**
     * Get all applications for a specific job
     */
    @SuppressWarnings("null")
    public List<JobApplication> getApplicationsByJob(String jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        return applicationRepository.findByJob(job);
    }

    /**
     * Get application by ID
     */
    @SuppressWarnings("null")
    public JobApplication getApplicationById(String applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
    }

    /**
     * Update application status
     */
    public JobApplication updateApplicationStatus(String applicationId, JobApplication.ApplicationStatus status) {
        JobApplication application = getApplicationById(applicationId);
        application.setStatus(status);
        application.onUpdate();
        return applicationRepository.save(application);
    }

    /**
     * Get resume file data
     */
    public byte[] getResumeData(String applicationId) {
        JobApplication application = getApplicationById(applicationId);
        return application.getResumeData();
    }
}
