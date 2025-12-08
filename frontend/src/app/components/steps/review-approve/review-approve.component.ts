import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { WorkflowService } from '../../../services/workflow.service';
import { JobService } from '../../../services/job.service';
import { AuthService } from '../../../services/auth.service';
import { Observable } from 'rxjs';
import { Job, JobRequest } from '../../../models/job.model';

@Component({
  selector: 'app-review-approve',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './review-approve.component.html',
  styleUrls: ['./review-approve.component.scss']
})
export class ReviewApproveComponent implements OnInit {

  jobData$!: Observable<Job | null>;
  jobData: Job | null = null;

  isSaving = false;
  isEditing = false;

  /** Prevent duplicate auto-saves */
  private autoSaveTriggered = false;

  constructor(
    private workflowService: WorkflowService,
    private jobService: JobService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.jobData$ = this.workflowService.currentJob$;

    this.jobData$.subscribe(job => {
      this.jobData = job;
      console.log('ReviewApprove: job loaded ->', this.jobData);

      // Auto-save logic (only when:
      //  - job exists,
      //  - job has NO id,
      //  - auto-save not yet triggered,
      //  - automationEnabled is true)
      if (this.jobData && !this.jobData.id && !this.autoSaveTriggered && this.workflowService.automationEnabled) {
        this.autoSaveTriggered = true;
        console.log('🤖 Auto-save scheduled (ReviewApprove) ...');
        setTimeout(() => this.autoSaveJobDescription(), 1000);
      }
    });
  }

  toggleEdit() {
    this.isEditing = !this.isEditing;
  }

  saveEdits() {
    if (!this.jobData) return;
    this.workflowService.updateJobData(this.jobData);
    this.isEditing = false;
    this.snackBar.open('✅ Changes saved locally', 'Close', { duration: 2000 });
  }

  goToPrevious() {
    this.workflowService.setCurrentStep(1);
  }

  /**
   * Called by timer to auto-save once
   */
  autoSaveJobDescription() {
    if (!this.jobData) return;
    if (this.jobData.id) {
      console.log('Auto-save skipped: job already has id.');
      return;
    }
    if (this.isSaving) {
      console.log('Auto-save skipped: currently saving.');
      return;
    }

    // perform save
    this.saveJobDescription();
  }

  /**
   * Save job to backend (MongoDB).
   * This method contains guards so it doesn't create duplicates.
   */
  saveJobDescription() {
    if (!this.jobData) {
      this.snackBar.open('❌ No job data to save', 'Close', { duration: 3000 });
      return;
    }

    // Guard: if job already has id (saved) - skip
    if (this.jobData.id) {
      console.log('saveJobDescription: job already has ID, skipping duplicate save.');
      return;
    }

    // Guard: if currently saving - skip
    if (this.isSaving) {
      console.log('saveJobDescription: currently saving, skipping.');
      return;
    }

    this.isSaving = true;

    const jobRequest: JobRequest = {
      title: this.jobData.jobTitle,
      description: this.jobData.jobDescription,
      company: this.jobData.companyName,
      location: this.jobData.location,
      employmentType: this.jobData.employmentType,
      experienceLevel: this.jobData.experienceLevel || 'MID',
      requiredSkills: this.jobData.keySkills || '',
      salaryRange:
        this.jobData.minSalary && this.jobData.maxSalary
          ? `${this.jobData.minSalary} - ${this.jobData.maxSalary}`
          : undefined
    };

    console.log('📤 ReviewApprove: sending job create request ->', jobRequest);

    this.jobService.createJob(jobRequest).subscribe({
      next: (createdJob) => {
        console.log('✅ Job created:', createdJob);

        // Update workflow with the created job (contains Mongo id)
        this.workflowService.setCurrentJob(createdJob);

        // Mark Step 2 complete and move to Step 3 (Post Job)
        this.workflowService.completeStep(2);
        this.workflowService.setCurrentStep(3);

        // For your requested behavior: also mark Step 3 complete
        // because automation is expected to auto-stop after posting.
        this.workflowService.completeStep(3);

        // Stop automation so it doesn't auto-progress further
        this.workflowService.stopAutomation();

        this.isSaving = false;

        this.snackBar.open('✅ Job saved and moved to Post Job (Step 3)', 'Close', { duration: 3000 });
        console.log('➡ Auto-workflow progressed to Step 3 and stopped automation.');
      },
      error: (err) => {
        this.isSaving = false;
        console.error('❌ Failed to create job:', err);
        this.snackBar.open('❌ Failed to save job to database', 'Close', { duration: 4000, panelClass: ['error-snackbar'] });
      }
    });
  }
}
