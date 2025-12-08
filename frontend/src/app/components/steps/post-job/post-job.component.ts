import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { WorkflowService } from '../../../services/workflow.service';
import { WorkflowStep } from '../../../models/workflow.interface';

@Component({
  selector: 'app-post-job',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './post-job.component.html',
  styleUrls: ['./post-job.component.scss']
})
export class PostJobComponent implements OnInit {

  jobData: any = null;
  postedJobId: string | null = null;
  publicJobUrl: string = '';
  webhookUrl: string = '';

  // UI states
  isPosting = false;
  isPosted = false;

  constructor(
    public workflowService: WorkflowService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    // Subscribe job
    this.workflowService.currentJob$.subscribe(job => {
      this.jobData = job;

      if (job && job.id) {
        this.postedJobId = job.id;
        this.publicJobUrl = `${window.location.origin}/apply/${job.id}`;
        this.webhookUrl = 'https://webhook.site/#!/view/92b7a908-f6dc-41ca-8e56-84f082e9da5c';
        console.log('PostJob: job loaded', job);
      }
    });

    // Subscribe workflow steps to determine if step 3 was auto-completed
    this.workflowService.workflowSteps$.subscribe(steps => {
      const s3 = steps.find(s => s.id === 3);
      if (s3 && s3.completed) {
        this.isPosted = true; // automation already posted / marked as posted
        console.log('PostJob: detected Step 3 completed => isPosted = true');
      }
    });
  }

  /**
   * UI-only post action (no backend call here).
   * In your architecture, Python AI handles actual external posting.
   */
  postJobNow(): void {
    if (this.isPosting || this.isPosted) return;

    this.isPosting = true;
    console.log('PostJob: UI-only post triggered');

    setTimeout(() => {
      this.isPosting = false;
      this.isPosted = true;

      // Mark step 3 completed if user clicks post manually
      this.workflowService.completeStep(3);

      // Important: stop automation to prevent any further auto steps
      this.workflowService.stopAutomation();

      this.snackBar.open('✅ Job marked as posted', 'Close', { duration: 2000, panelClass: ['success-snackbar'] });
    }, 1200);
  }

  getSalaryRange(): string {
    const min = this.jobData?.minSalary;
    const max = this.jobData?.maxSalary;
    if (min && max && min > 0 && max > 0) {
      return `$${min.toLocaleString()} - $${max.toLocaleString()}`;
    }
    return this.jobData?.salaryRange || 'Competitive Salary';
  }

  copyJobUrl(): void {
    if (!this.publicJobUrl) return;
    navigator.clipboard.writeText(this.publicJobUrl).then(() => {
      this.snackBar.open('✅ Application URL copied', 'Close', { duration: 2000 });
    });
  }

  copyWebhookUrl(): void {
    if (!this.webhookUrl) return;
    navigator.clipboard.writeText(this.webhookUrl).then(() => {
      this.snackBar.open('✅ Webhook URL copied', 'Close', { duration: 2000 });
    });
  }

  openWebhookSite(): void {
    if (!this.webhookUrl) return;
    window.open(this.webhookUrl, '_blank');
  }

  viewApplicationPage(): void {
    if (this.postedJobId) {
      window.open(`/apply/${this.postedJobId}`, '_blank');
    }
  }

  goBack(): void {
    this.workflowService.setCurrentStep(2);
  }

  proceedToNext(): void {
    // User-controlled navigation (no auto-jump to Step 4)
    this.workflowService.setCurrentStep(4);
  }
}
