import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-apply-job',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './apply-job.component.html',
  styleUrls: ['./apply-job.component.scss']
})
export class ApplyJobComponent implements OnInit {
  jobId: string | null = null;
  jobData: any = null;
  applicationForm!: FormGroup;
  isLoading = false;
  isSubmitting = false;
  applicationSubmitted = false;
  selectedFile: File | null = null;
  
  // AI Monitoring Integration
  jobIsFull = false;
  jobIsClosed = false;
  canAcceptApplications = true;
  applicationCount = 0;
  minimumCandidates = 3;
  closedMessage = '';

  private apiUrl = 'http://localhost:8080/api';

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    console.log('🎯 ApplyJobComponent initialized!');
    
    this.jobId = this.route.snapshot.paramMap.get('id');
    console.log('Job ID from route:', this.jobId);

    // Initialize form with ONLY the fields needed by backend
    this.applicationForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required]],
      currentRole: ['', Validators.required],
      yearsOfExperience: ['', [Validators.required, Validators.min(0)]],
      linkedIn: [''],
      portfolio: ['']
    });

    // Log form status changes
    this.applicationForm.statusChanges.subscribe(status => {
      console.log('Form status changed:', status);
      console.log('Form valid:', this.applicationForm.valid);
      console.log('Form errors:', this.getFormValidationErrors());
    });

    if (this.jobId) {
      this.loadJobDetails();
      this.checkApplicationStatus();
    }
  }

  getFormValidationErrors() {
    const errors: any = {};
    Object.keys(this.applicationForm.controls).forEach(key => {
      const controlErrors = this.applicationForm.get(key)?.errors;
      if (controlErrors) {
        errors[key] = controlErrors;
      }
    });
    return errors;
  }

  loadJobDetails(): void {
    this.isLoading = true;
    console.log('🔍 Loading job details for ID:', this.jobId);

    this.http.get<any>(`${this.apiUrl}/jobs/${this.jobId}`).subscribe({
      next: (job) => {
        this.jobData = job;
        this.isLoading = false;
        console.log('✅ Job loaded:', job);
        
        // Debug: Check application limit status
        console.log('📊 Application Limit Check (Apply Page):');
        console.log('  - maxCandidates:', this.jobData.maxCandidates);
        console.log('  - applicationCount:', this.jobData.applicationCount);
        
        if (this.jobData.maxCandidates) {
          const isFull = this.jobData.applicationCount >= this.jobData.maxCandidates;
          const spotsRemaining = this.jobData.maxCandidates - this.jobData.applicationCount;
          console.log('  - Is Full?', isFull);
          console.log('  - Spots Remaining:', spotsRemaining);
          console.log('  - Form will be:', isFull ? 'HIDDEN' : 'VISIBLE');
        } else {
          console.log('  - No limit set (unlimited applications)');
        }
      },
      error: (error) => {
        console.error('❌ Error loading job:', error);
        this.isLoading = false;
        this.snackBar.open('❌ Job not found', 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }
  
  /**
   * Check if job can still accept applications using AI monitoring
   * Implements: enough_applications = len(resumes) >= 3
   */
  checkApplicationStatus(): void {
    console.log('🤖 Checking AI monitoring status for job:', this.jobId);
    
    this.http.get<any>(`${this.apiUrl}/monitoring/job/${this.jobId}/can-accept`).subscribe({
      next: (response) => {
        console.log('✅ AI Monitoring Response:', response);
        
        this.canAcceptApplications = response.canAcceptApplications;
        this.jobIsFull = response.isFull;
        this.jobIsClosed = !response.isOpen;
        this.applicationCount = response.currentCount;
        this.closedMessage = response.message;
        
        console.log('📊 Application Status:');
        console.log('  - Can Accept:', this.canAcceptApplications);
        console.log('  - Is Full:', this.jobIsFull);
        console.log('  - Is Closed:', this.jobIsClosed);
        console.log('  - Count:', this.applicationCount);
        console.log('  - Threshold:', response.threshold);
        
        if (!this.canAcceptApplications) {
          this.snackBar.open(this.closedMessage, 'Close', {
            duration: 8000,
            panelClass: ['info-snackbar']
          });
        }
      },
      error: (error) => {
        console.warn('⚠️ Could not check AI monitoring status:', error);
        // Fallback: allow applications if monitoring check fails
        this.canAcceptApplications = true;
      }
    });
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    console.log('📎 File selected:', file);
    
    if (file) {
      // Validate PDF
      if (file.type !== 'application/pdf') {
        this.snackBar.open('⚠️ Please upload a PDF file', 'Close', {
          duration: 3000
        });
        this.selectedFile = null;
        return;
      }

      // Validate size (5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.snackBar.open('⚠️ File must be less than 5MB', 'Close', {
          duration: 3000
        });
        this.selectedFile = null;
        return;
      }

      this.selectedFile = file;
      console.log('✅ Resume file accepted:', file.name, '(' + (file.size / 1024).toFixed(2) + ' KB)');
      
      this.snackBar.open(`✅ Resume uploaded: ${file.name}`, 'Close', {
        duration: 2000
      });
    }
  }

  onButtonClick(): void {
    console.log('🖱️ BUTTON CLICKED - Event fired!');
    this.submitApplication();
  }

  submitApplication(): void {
    console.log('📤 ========== SUBMIT APPLICATION CALLED ==========');
    console.log('Form valid:', this.applicationForm.valid);
    console.log('Form values:', this.applicationForm.value);
    console.log('Selected file:', this.selectedFile);
    console.log('Job ID:', this.jobId);
    console.log('Form errors:', this.getFormValidationErrors());
    console.log('Is submitting:', this.isSubmitting);

    if (this.applicationForm.invalid) {
      console.warn('⚠️ Form is invalid');
      Object.keys(this.applicationForm.controls).forEach(key => {
        const control = this.applicationForm.get(key);
        if (control?.invalid) {
          console.warn(`  - ${key}: `, control.errors);
        }
      });
      
      this.snackBar.open('⚠️ Please fill all required fields correctly', 'Close', {
        duration: 4000
      });
      return;
    }

    if (!this.selectedFile) {
      console.warn('⚠️ No resume file selected');
      this.snackBar.open('⚠️ Please upload your resume (PDF)', 'Close', {
        duration: 4000
      });
      return;
    }

    this.isSubmitting = true;
    console.log('🚀 Starting application submission...');

    // Create FormData for file upload
    const formData = new FormData();
    formData.append('jobId', this.jobId!);
    formData.append('candidateName', this.applicationForm.value.fullName);
    formData.append('candidateEmail', this.applicationForm.value.email);
    formData.append('candidatePhone', this.applicationForm.value.phone);
    formData.append('coverLetter', '');
    formData.append('resume', this.selectedFile);

    console.log('📦 FormData prepared:');
    console.log('  - jobId:', this.jobId);
    console.log('  - candidateName:', this.applicationForm.value.fullName);
    console.log('  - candidateEmail:', this.applicationForm.value.email);
    console.log('  - candidatePhone:', this.applicationForm.value.phone);
    console.log('  - resume:', this.selectedFile.name);

    // Submit to backend
    this.http.post<any>(`${this.apiUrl}/applications`, formData).subscribe({
      next: (response) => {
        console.log('✅ Application submitted successfully!', response);
        this.handleSuccess();
      },
      error: (error) => {
        console.error('❌ Application submission failed:', error);
        console.error('Error details:', error.error);
        this.isSubmitting = false;
        
        const errorMessage = error.error?.message || 'Failed to submit application';
        this.snackBar.open(`❌ ${errorMessage}`, 'Close', {
          duration: 6000
        });
      }
    });
  }

  private handleSuccess(): void {
    this.isSubmitting = false;
    this.applicationSubmitted = true;

    this.snackBar.open(
      '✅ Application submitted successfully! The employer has been notified.',
      'Close',
      {
        duration: 6000,
        panelClass: ['success-snackbar']
      }
    );
  }

  resetForm(): void {
    this.applicationForm.reset();
    this.selectedFile = null;
    this.applicationSubmitted = false;
  }

  goToJobs(): void {
    window.location.href = '/jobs';
  }
  
  /**
   * Get display message when job is closed
   */
  getClosedMessage(): string {
    if (this.jobIsFull) {
      return `🎉 Thank you for your interest! This position has received enough applications (${this.applicationCount}/${this.minimumCandidates} candidates). We are no longer accepting new applications.`;
    } else if (this.jobIsClosed) {
      return '🚫 This job posting is currently closed and is not accepting applications.';
    }
    return this.closedMessage || 'This position is not accepting applications at this time.';
  }
}
