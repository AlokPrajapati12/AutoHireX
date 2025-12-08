import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { trigger, state, style, transition, animate } from '@angular/animations';
import { WorkflowService } from '../../../services/workflow.service';
import { ShortlistService, ShortlistRequest, ShortlistResponse } from '../../../services/shortlist.service';
import { InterviewScheduleDialogComponent } from './interview-schedule-dialog.component';

interface MatchResult {
  applicationId: string;
  candidateName: string;
  candidateEmail?: string;
  matchScore: number;
  matchedSkills: string[];
  missingSkills: string[];
  recommendation: string;
  llmDecision?: string;
  llmReasoning?: string;
  rank?: number;
}

@Component({
  selector: 'app-shortlist',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
    MatSnackBarModule,
    MatTableModule,
    MatChipsModule,
    MatBadgeModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatFormFieldModule,
    MatInputModule,
    MatDialogModule
  ],
  templateUrl: './shortlist.component.html',
  styleUrls: ['./shortlist.component.scss'],
  animations: [
    trigger('fadeIn', [
      state('void', style({ opacity: 0, transform: 'translateY(20px)' })),
      transition(':enter', animate('500ms ease-out', style({ opacity: 1, transform: 'translateY(0)' })))
    ]),
    trigger('slideIn', [
      state('void', style({ opacity: 0, transform: 'translateX(-50px)' })),
      transition(':enter', animate('400ms {{delay}}ms ease-out', style({ opacity: 1, transform: 'translateX(0)' })), {
        params: { delay: 0 }
      })
    ]),
    trigger('scaleIn', [
      state('void', style({ opacity: 0, transform: 'scale(0.8)' })),
      transition(':enter', animate('300ms ease-out', style({ opacity: 1, transform: 'scale(1)' })))
    ])
  ]
})
export class ShortlistComponent implements OnInit {
  private apiUrl = 'http://localhost:8080/api';

  // State management
  isProcessing = true; // Start processing automatically
  processingComplete = false;
  
  // Progress tracking
  currentStep = '';
  currentStepDetails = '';
  progress = 0;
  processedCount = 0;
  totalCandidates = 0;
  
  // Data
  shortlistedCandidates: MatchResult[] = [];
  averageMatchScore = 0;
  currentJobId: string | null = null;

  constructor(
    private router: Router,
    private http: HttpClient,
    private snackBar: MatSnackBar,
    private workflowService: WorkflowService,
    private shortlistService: ShortlistService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.currentJobId = localStorage.getItem('shortlistJobId') || this.workflowService.currentJobId;
    
    console.log('🎯 Shortlist Component initialized - Starting automatic AI analysis');
    console.log('Current Job ID:', this.currentJobId);
    
    if (this.currentJobId) {
      // Auto-start AI matching process in background
      this.startAutomaticAIMatching();
    } else {
      this.snackBar.open('No job selected for shortlisting', 'Close', { duration: 3000 });
      this.isProcessing = false;
    }
  }

  /**
   * Automatically start AI Matching Process in background (no button click needed)
   */
  async startAutomaticAIMatching(): Promise<void> {
    if (!this.currentJobId) {
      this.snackBar.open('No job selected for shortlisting', 'Close', { duration: 3000 });
      this.isProcessing = false;
      return;
    }

    this.isProcessing = true;
    this.progress = 0;
    this.processedCount = 0;
    this.shortlistedCandidates = [];

    try {
      // Step 1: Initialize
      this.currentStep = 'Initializing AI Shortlisting';
      this.currentStepDetails = 'Connecting to AI service...';
      this.progress = 10;
      await this.delay(800);

      console.log('🤖 Starting automatic AI shortlisting for job:', this.currentJobId);
      
      // Step 2: Get job description
      this.currentStep = 'Loading Job Requirements';
      this.currentStepDetails = 'Fetching job description and requirements...';
      this.progress = 20;
      
      const jobDescription = await this.getJobDescription();
      
      // Get application count
      const applications = await this.getApplications();
      this.totalCandidates = applications.length;
      
      // Step 3: Call backend shortlisting API
      this.currentStep = 'AI Analysis in Progress';
      this.currentStepDetails = 'Deep learning models analyzing candidates...';
      this.progress = 30;

      const request: ShortlistRequest = {
        jobId: this.currentJobId,
        jobDescription: jobDescription,
        minScore: 50.0
      };

      // Simulate progress during backend processing
      const progressInterval = setInterval(() => {
        if (this.progress < 80) {
          this.progress += 2;
          const currentCandidate = Math.floor((this.progress - 30) / 50 * this.totalCandidates);
          this.processedCount = Math.min(currentCandidate, this.totalCandidates);
          this.currentStepDetails = `Analyzing candidate ${this.processedCount}/${this.totalCandidates}...`;
        }
      }, 500);

      // Call the backend service
      this.shortlistService.processJobShortlisting(request).subscribe({
        next: async (response: ShortlistResponse) => {
          clearInterval(progressInterval);
          
          console.log('✅ Backend response:', response);
          
          if (response.success) {
            // Step 4: Process results
            this.currentStep = 'Finalizing Results';
            this.currentStepDetails = 'Ranking candidates by match score...';
            this.progress = 90;
            
            // Map backend DTOs to frontend format
            this.shortlistedCandidates = response.shortlistedCandidates.map(dto => ({
              applicationId: dto.applicationId,
              candidateName: dto.candidateName,
              candidateEmail: dto.candidateEmail,
              matchScore: dto.finalScore,
              matchedSkills: dto.matchedSkills || [],
              missingSkills: dto.missingSkills || [],
              recommendation: dto.llmReasoning || this.generateRecommendation(dto.finalScore),
              llmDecision: dto.llmDecision,
              llmReasoning: dto.llmReasoning,
              rank: dto.rank
            }));
            
            this.processedCount = response.totalProcessed;
            this.totalCandidates = response.totalProcessed;
            
            // Calculate average score
            if (this.shortlistedCandidates.length > 0) {
              const total = this.shortlistedCandidates.reduce((sum, c) => sum + c.matchScore, 0);
              this.averageMatchScore = Math.round(total / this.shortlistedCandidates.length);
            }
            
            this.progress = 100;
            await this.delay(800);
            
            this.processingComplete = true;
            this.isProcessing = false;
            
            this.snackBar.open(
              `✅ Successfully shortlisted ${this.shortlistedCandidates.length} out of ${response.totalProcessed} candidates!`,
              'Close',
              { duration: 5000 }
            );
            
          } else {
            throw new Error(response.message || 'Shortlisting failed');
          }
        },
        error: (error) => {
          clearInterval(progressInterval);
          console.error('❌ Backend error:', error);
          this.isProcessing = false;
          this.processingComplete = true;
          this.snackBar.open(
            '❌ Error during AI matching: ' + (error.error?.message || error.message || 'Unknown error'),
            'Close',
            { duration: 5000 }
          );
        }
      });

    } catch (error: any) {
      console.error('❌ Error during matching:', error);
      this.isProcessing = false;
      this.processingComplete = true;
      this.snackBar.open('Error during AI matching process', 'Close', { duration: 5000 });
    }
  }

  /**
   * Get applications count
   */
  private async getApplications(): Promise<any[]> {
    if (!this.currentJobId) return [];
    
    return new Promise((resolve) => {
      this.http.get<any[]>(`${this.apiUrl}/applications/job/${this.currentJobId}`).subscribe({
        next: (apps) => {
          resolve(apps.filter(app => app.status !== 'REJECTED'));
        },
        error: () => resolve([])
      });
    });
  }

  /**
   * Get job description from backend
   */
  private async getJobDescription(): Promise<string> {
    if (!this.currentJobId) return '';
    
    return new Promise((resolve) => {
      this.http.get(`${this.apiUrl}/jobs/${this.currentJobId}`).subscribe({
        next: (job: any) => {
          resolve(job.description || '');
        },
        error: () => resolve('')
      });
    });
  }

  /**
   * Generate recommendation based on score
   */
  private generateRecommendation(score: number): string {
    if (score >= 80) {
      return `Excellent candidate! Strong match with high ATS score. Highly recommended for interview.`;
    } else if (score >= 60) {
      return `Good candidate with decent match. Shows promise and potential for the role.`;
    } else {
      return `Moderate match. May need additional evaluation or training.`;
    }
  }

  /**
   * Helper delay function
   */
  private delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  /**
   * Schedule Manual Interview by HR
   */
  scheduleManualInterview(): void {
    const dialogRef = this.dialog.open(InterviewScheduleDialogComponent, {
      width: '600px',
      data: {
        candidates: this.shortlistedCandidates,
        jobId: this.currentJobId,
        interviewType: 'MANUAL'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.sendInterviewInvites(result, 'MANUAL');
      }
    });
  }

  /**
   * Schedule Voice AI Agent Interview (3 rounds)
   */
  scheduleVoiceAIInterview(): void {
    const dialogRef = this.dialog.open(InterviewScheduleDialogComponent, {
      width: '600px',
      data: {
        candidates: this.shortlistedCandidates,
        jobId: this.currentJobId,
        interviewType: 'VOICE_AI'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.sendInterviewInvites(result, 'VOICE_AI');
      }
    });
  }

  /**
   * Send interview invites via email
   */
  private sendInterviewInvites(scheduleData: any, interviewType: string): void {
    const payload = {
      jobId: this.currentJobId,
      candidates: this.shortlistedCandidates.map(c => ({
        applicationId: c.applicationId,
        candidateEmail: c.candidateEmail,
        candidateName: c.candidateName
      })),
      interviewType: interviewType,
      scheduledDate: scheduleData.date,
      scheduledTime: scheduleData.time,
      additionalNotes: scheduleData.notes
    };

    this.http.post(`${this.apiUrl}/interviews/schedule`, payload).subscribe({
      next: (response: any) => {
        this.snackBar.open(
          `✅ Interview invites sent to ${this.shortlistedCandidates.length} candidates!`,
          'Close',
          { duration: 5000 }
        );
        
        // Navigate to interviews monitoring page
        this.workflowService.setCurrentStep(6);
        this.router.navigate(['/workflow', 6]);
      },
      error: (error) => {
        console.error('Error scheduling interviews:', error);
        this.snackBar.open(
          '❌ Failed to send interview invites. Please try again.',
          'Close',
          { duration: 5000 }
        );
      }
    });
  }

  /**
   * Go back to previous step
   */
  goBack(): void {
    this.workflowService.previousStep();
    this.router.navigate(['/workflow', 4]);
  }

  /**
   * Get match score badge class
   */
  getMatchScoreClass(score: number): string {
    if (score >= 80) return 'excellent';
    if (score >= 60) return 'good';
    return 'fair';
  }
}
