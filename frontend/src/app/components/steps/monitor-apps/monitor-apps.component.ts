import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { WorkflowService } from '../../../services/workflow.service';

interface JobApplicationStats {
  jobId: string;
  jobTitle: string;
  company: string;
  location: string;
  postedDate: string;
  status: string;
  totalApplications: number;
  submittedCount: number;
  underReviewCount: number;
  shortlistedCount: number;
  interviewScheduledCount: number;
  acceptedCount: number;
  rejectedCount: number;
}

@Component({
  selector: 'app-monitor-apps',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatBadgeModule,
    MatTooltipModule,
    MatChipsModule
  ],
  templateUrl: './monitor-apps.component.html',
  styleUrls: ['./monitor-apps.component.scss']
})
export class MonitorAppsComponent implements OnInit {

  private apiUrl = 'http://localhost:8080/api';

  isLoading = false;
  jobsData: JobApplicationStats[] = [];
  totalApplications = 0;
  totalJobs = 0;

  minimumCandidates = 3;
  hasEnoughCandidates = false;

  constructor(
    private router: Router,
    private http: HttpClient,
    private snackBar: MatSnackBar,
    public workflowService: WorkflowService
  ) { }

  ngOnInit(): void {
    this.loadAllJobsApplications();
  }

  /** ---------------------------------------------
   *  LOAD ALL JOBS + APPLICATION COUNTS
   * ---------------------------------------------- */
  loadAllJobsApplications(): void {
    this.isLoading = true;
    console.log('🔍 Starting to load jobs from:', `${this.apiUrl}/jobs/all`);

    this.http.get<any[]>(`${this.apiUrl}/jobs/all`).subscribe({
      next: (jobs) => {
        console.log('✅ Jobs received:', jobs.length);
        console.log('📋 Jobs data:', jobs);
        
        if (!jobs || jobs.length === 0) {
          console.warn('⚠️ No jobs found in database');
          this.isLoading = false;
          this.jobsData = [];
          this.totalJobs = 0;
          this.totalApplications = 0;
          return;
        }

        const requests = jobs.map(job => {
          console.log(`📊 Loading applications for job: ${job.id} - ${job.title}`);
          
          return this.http.get<any[]>(`${this.apiUrl}/applications/job/${job.id}`)
            .toPromise()
            .then(apps => {
              console.log(`  ✅ Found ${apps?.length || 0} applications for ${job.title}`);
              return this.buildStats(job, apps || []);
            })
            .catch(err => {
              console.error(`  ❌ Error loading applications for ${job.title}:`, err);
              return this.buildStats(job, []);
            });
        });

        Promise.all(requests).then(results => {
          console.log('✅ All jobs processed:', results.length);
          this.jobsData = results;
          this.totalJobs = results.length;
          this.totalApplications = results.reduce((s, j) => s + j.totalApplications, 0);
          
          console.log('📊 Final stats:', {
            totalJobs: this.totalJobs,
            totalApplications: this.totalApplications,
            jobsData: this.jobsData
          });

          const currentId = this.workflowService.currentJobId;
          if (currentId) {
            const stats = results.find(r => r.jobId === currentId);
            this.hasEnoughCandidates = (stats?.totalApplications || 0) >= this.minimumCandidates;
          }

          this.isLoading = false;
        });
      },
      error: (err) => {
        console.error('❌ Error loading jobs:', err);
        console.error('Error details:', {
          status: err.status,
          statusText: err.statusText,
          message: err.message,
          url: err.url
        });
        this.isLoading = false;
        this.snackBar.open(`Error loading jobs: ${err.message}`, "Close", { duration: 5000 });
      }
    });
  }

  /** ---------------------------------------------
   *  BUILD STATS OBJECT FOR ONE JOB
   * ---------------------------------------------- */
  buildStats(job: any, apps: any[]): JobApplicationStats {
    return {
      jobId: job.id,
      jobTitle: job.title,
      company: job.company || "N/A",
      location: job.location || "Remote",
      postedDate: job.createdAt,
      status: job.status || "OPEN",
      totalApplications: apps.length,
      submittedCount: apps.filter(a => a.status === "SUBMITTED").length,
      underReviewCount: apps.filter(a => a.status === "UNDER_REVIEW").length,
      shortlistedCount: apps.filter(a => a.status === "SHORTLISTED").length,
      interviewScheduledCount: apps.filter(a => a.status === "INTERVIEW_SCHEDULED").length,
      acceptedCount: apps.filter(a => a.status === "ACCEPTED").length,
      rejectedCount: apps.filter(a => a.status === "REJECTED").length,
    };
  }

  /** ---------------------------------------------
   *  REQUIRED FUNCTIONS FOR HTML (Missing earlier)
   * ---------------------------------------------- */

  formatDate(dateString: string): string {
    if (!dateString) return "Unknown";

    const date = new Date(dateString);
    return date.toLocaleDateString();
  }

  getStatusColor(stage: string): string {
    const map: any = {
      submitted: '#FF9800',
      review: '#9C27B0',
      shortlisted: '#4CAF50',
      interview: '#00BCD4',
      accepted: '#4CAF50',
      rejected: '#F44336'
    };
    return map[stage] || '#999';
  }

  getFunnelProgress(job: JobApplicationStats): number {
    if (job.totalApplications === 0) return 0;

    const active = job.submittedCount + job.underReviewCount + job.shortlistedCount + job.interviewScheduledCount;
    return Math.round((active / job.totalApplications) * 100);
  }

  hasEnoughCandidatesForJob(job: JobApplicationStats): boolean {
    return job.totalApplications >= this.minimumCandidates;
  }

  /** ---------------------------------------------
   *  BUTTON ACTIONS
   * ---------------------------------------------- */
  proceedToShortlistForJob(jobId: string): void {
    // Save job ID for shortlisting
    localStorage.setItem("shortlistJobId", jobId);
    this.workflowService.currentJobId = jobId;
    
    // Show loading message
    this.snackBar.open(
      "🤖 Starting AI-powered shortlisting in background...",
      "Close",
      { duration: 3000 }
    );
    
    // Complete Step 4 and move to Step 5
    this.workflowService.completeStep(4);
    this.workflowService.setCurrentStep(5);
    
    // AI shortlisting will start automatically when Step 5 component loads
  }

  regenerateJDForJob(jobId: string): void {
    localStorage.setItem("regenerating_job_id", jobId);
    this.workflowService.setCurrentStep(1);
  }

  refresh(): void {
    this.snackBar.open("Refreshing...", "Close", { duration: 1000 });
    this.loadAllJobsApplications();
  }

  goBack(): void {
    this.workflowService.previousStep();
  }

  getSuccessRate(job: JobApplicationStats): number {
    if (job.totalApplications === 0) return 0;
    return Math.round((job.acceptedCount / job.totalApplications) * 100);
  }
  /** 
 * Regenerate JD when user clicks the global button 
 * (Used when NO jobs have enough candidates)
 */
  regenerateJD(): void {
    this.snackBar.open(
      "🔄 Regenerating Job Description to attract more candidates...",
      "Close",
      { duration: 3000 }
    );

    // Save current job ID (if available)
    if (this.workflowService.currentJobId) {
      localStorage.setItem("regenerating_job_id", this.workflowService.currentJobId);
    }

    // Move user back to Step 1 (Create Job Description)
    this.workflowService.setCurrentStep(1);

    this.snackBar.open(
      "📝 Redirected to Job Creation. Please generate a new JD!",
      "Close",
      { duration: 5000 }
    );
  }

  /**
   * Create a new job - resets workflow and goes to Step 1
   */
  createNewJob(): void {
    this.snackBar.open(
      "🎯 Starting new job creation workflow...",
      "Close",
      { duration: 2000 }
    );

    // Clear previous workflow state
    localStorage.removeItem('workflow_state');
    localStorage.removeItem('shortlistJobId');
    localStorage.removeItem('regenerating_job_id');
    
    // Reset workflow service
    this.workflowService.startNewWorkflow();
    
    // Navigate to Step 1
    this.router.navigate(['/workflow', 1]);
  }

  /**
   * Go back to home/landing page
   */
  goToHome(): void {
    this.router.navigate(['/']);
  }

}
