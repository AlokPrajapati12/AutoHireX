package com.smarthire.service.monitoring;

import com.smarthire.model.Job;
import com.smarthire.model.JobApplication;
import com.smarthire.repository.JobApplicationRepository;
import com.smarthire.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationMonitoringService {

    @Autowired
    private JobApplicationRepository applicationRepository;
    
    @Autowired
    private JobRepository jobRepository;

    /**
     * Check if a job has received enough applications to be closed
     * Currently set to 3 applications minimum
     */
    public boolean hasEnoughApplications(String jobId) {
        try {
            @SuppressWarnings("null")
            Job job = jobRepository.findById(jobId).orElse(null);
            
            if (job == null) {
                System.err.println("⚠️ Job not found: " + jobId);
                return false;
            }
            
            // Get all applications for this job
            List<JobApplication> applications = applicationRepository.findByJob(job);
            int applicationCount = applications.size();
            
            // Check if job has maxCandidates set
            if (job.getMaxCandidates() != null && job.getMaxCandidates() > 0) {
                boolean hasEnough = applicationCount >= job.getMaxCandidates();
                System.out.println("🔍 Job \"" + job.getTitle() + "\" has " + applicationCount + 
                    "/" + job.getMaxCandidates() + " applications - Enough: " + hasEnough);
                return hasEnough;
            }
            
            // Default: Consider 3 applications as enough
            int defaultThreshold = 3;
            boolean hasEnough = applicationCount >= defaultThreshold;
            System.out.println("🔍 Job \"" + job.getTitle() + "\" has " + applicationCount + 
                "/" + defaultThreshold + " applications (default) - Enough: " + hasEnough);
            return hasEnough;
            
        } catch (Exception e) {
            System.err.println("❌ Error checking applications for job " + jobId + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get application count for a job
     */
    public int getApplicationCount(String jobId) {
        try {
            @SuppressWarnings("null")
            Job job = jobRepository.findById(jobId).orElse(null);
            
            if (job == null) {
                return 0;
            }
            
            List<JobApplication> applications = applicationRepository.findByJob(job);
            return applications.size();
            
        } catch (Exception e) {
            System.err.println("❌ Error getting application count for job " + jobId + ": " + e.getMessage());
            return 0;
        }
    }
}
