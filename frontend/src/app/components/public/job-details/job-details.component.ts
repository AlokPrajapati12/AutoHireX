import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-job-details',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './job-details.component.html',
  styleUrls: ['./job-details.component.scss']
})
export class JobDetailsComponent implements OnInit {
  jobId: string = '';
  job: any = null;
  applicationForm!: FormGroup;
  isLoading = true;
  isSubmitting = false;
  applicationSubmitted = false;
  selectedFile: File | null = null;

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.jobId = this.route.snapshot.params['id'];
    this.initForm();
    this.loadJobDetails();
  }

  initForm(): void {
    this.applicationForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      linkedIn: [''],
      coverLetter: [''],  // Made optional - no validators
      yearsOfExperience: ['', [Validators.required, Validators.min(0)]]
    });
  }

  loadJobDetails(): void {
    console.log('Loading job with ID:', this.jobId);
    this.http.get(`/api/jobs/${this.jobId}`).subscribe({
      next: (job) => {
        console.log('✅ Job loaded:', job);
        this.job = job;
        
        // Debug: Check application limit status
        console.log('📊 Application Limit Check:');
        console.log('  - maxCandidates:', this.job.maxCandidates);
        console.log('  - applicationCount:', this.job.applicationCount);
        
        if (this.job.maxCandidates) {
          const isFull = this.job.applicationCount >= this.job.maxCandidates;
          const spotsRemaining = this.job.maxCandidates - this.job.applicationCount;
          console.log('  - Is Full?', isFull);
          console.log('  - Spots Remaining:', spotsRemaining);
          console.log('  - Form will be:', isFull ? 'HIDDEN' : 'VISIBLE');
        } else {
          console.log('  - No limit set (unlimited applications)');
        }
        
        this.isLoading = false;
      },
      error: (error) => {
        console.error('❌ Failed to load job:', error);
        console.error('Job ID:', this.jobId);
        console.error('URL:', `/api/jobs/${this.jobId}`);
        this.isLoading = false;
        
        let errorMessage = '❌ Failed to load job details. ';
        
        if (error.status === 404) {
          errorMessage += 'Job not found. Please check if the job exists in the database.';
        } else if (error.status === 0) {
          errorMessage += 'Cannot connect to server. Please check if backend is running.';
        } else {
          errorMessage += error.message || 'Unknown error';
        }
        
        this.snackBar.open(errorMessage, 'Close', {
          duration: 8000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      // Validate file type
      const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
      if (!allowedTypes.includes(file.type)) {
        this.snackBar.open('❌ Please upload PDF or Word document only', 'Close', {
          duration: 4000
        });
        return;
      }

      // Validate file size (5MB max)
      if (file.size > 5 * 1024 * 1024) {
        this.snackBar.open('❌ File size must be less than 5MB', 'Close', {
          duration: 4000
        });
        return;
      }

      this.selectedFile = file;
    }
  }

  onButtonClick(): void {
    console.log('🖱️ BUTTON CLICKED in job-details component!');
    this.submitApplication();
  }

  submitApplication(): void {
    console.log('📤 ========== SUBMIT APPLICATION CALLED ==========');
    console.log('Form valid:', this.applicationForm.valid);
    console.log('Form values:', this.applicationForm.value);
    console.log('Selected file:', this.selectedFile);
    console.log('Job ID:', this.jobId);

    if (this.applicationForm.invalid) {
      console.warn('⚠️ Form is invalid');
      Object.keys(this.applicationForm.controls).forEach(key => {
        const control = this.applicationForm.get(key);
        if (control?.invalid) {
          console.warn(`  - ${key}: `, control.errors);
        }
      });
      
      this.snackBar.open('❌ Please fill all required fields correctly', 'Close', {
        duration: 4000
      });
      return;
    }

    if (!this.selectedFile) {
      console.warn('⚠️ No resume file selected');
      this.snackBar.open('❌ Please upload your resume', 'Close', {
        duration: 4000
      });
      return;
    }

    this.isSubmitting = true;
    console.log('🚀 Starting application submission...');

    // Create FormData with the EXACT field names expected by backend
    const formData = new FormData();
    formData.append('jobId', this.jobId);
    formData.append('candidateName', this.applicationForm.get('fullName')?.value);
    formData.append('candidateEmail', this.applicationForm.get('email')?.value);
    formData.append('candidatePhone', this.applicationForm.get('phone')?.value);
    formData.append('coverLetter', this.applicationForm.get('coverLetter')?.value || '');
    formData.append('resume', this.selectedFile);

    console.log('📦 FormData prepared:');
    console.log('  - jobId:', this.jobId);
    console.log('  - candidateName:', this.applicationForm.get('fullName')?.value);
    console.log('  - candidateEmail:', this.applicationForm.get('email')?.value);
    console.log('  - candidatePhone:', this.applicationForm.get('phone')?.value);
    console.log('  - resume:', this.selectedFile.name);

    // Submit to backend API endpoint (matching backend controller)
    this.http.post('/api/applications', formData).subscribe({
      next: (response) => {
        console.log('✅ Application submitted successfully!', response);
        this.isSubmitting = false;
        this.applicationSubmitted = true;
        
        this.snackBar.open('✅ Application submitted successfully! Your resume has been saved.', 'Close', {
          duration: 5000,
          panelClass: ['success-snackbar']
        });
      },
      error: (error) => {
        console.error('❌ Application submission failed:', error);
        console.error('Error details:', error.error);
        this.isSubmitting = false;
        
        const errorMessage = error.error?.message || 'Failed to submit application. Please try again.';
        this.snackBar.open(`❌ ${errorMessage}`, 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  getFileSize(): string {
    if (!this.selectedFile) return '';
    const sizeInMB = this.selectedFile.size / (1024 * 1024);
    return `${sizeInMB.toFixed(2)} MB`;
  }
}
