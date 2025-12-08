import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { Subscription } from 'rxjs';
import { WorkflowService } from '../../../services/workflow.service';
import { AiService } from '../../../services/ai.service';
import { AutomatedWorkflowService, WorkflowProgress } from '../../../services/automated-workflow.service';
import { Job } from '../../../models/job.model';

@Component({
  selector: 'app-create-job',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatSnackBarModule,
    MatCardModule
  ],
  templateUrl: './create-job.component.html',
  styleUrls: ['./create-job.component.scss']
})
export class CreateJobComponent implements OnInit, OnDestroy {
  jobForm!: FormGroup;
  selectedBenefits: string[] = [];
  
  // View state
  showManualDetails = false;
  isGeneratingAI = false;
  aiGenerationComplete = false;
  isAutomatedRunning = false;
  
  // Workflow progress
  workflowProgress: WorkflowProgress | null = null;
  private progressSubscription?: Subscription;
  
  departments = ['Engineering', 'Product', 'Marketing', 'HR', 'Sales', 'Finance', 'Operations'];
  employmentTypes = [
    { label: 'Full-time', value: 'FULL_TIME' },
    { label: 'Part-time', value: 'PART_TIME' },
    { label: 'Contract', value: 'CONTRACT' },
    { label: 'Internship', value: 'INTERNSHIP' }
  ];
  experienceLevels = ['ENTRY', 'MID', 'SENIOR', 'LEAD', 'EXECUTIVE'];
  educationLevels = ['High School', 'Bachelor', 'Master', 'PhD'];
  benefitOptions = [
    'Health Insurance',
    '401k',
    'Paid Time Off',
    'Flexible Hours',
    'Remote Work',
    'Stock Options',
    'Learning Budget',
    'Gym Membership'
  ];

  constructor(
    private fb: FormBuilder,
    private workflowService: WorkflowService,
    private aiService: AiService,
    private automatedWorkflowService: AutomatedWorkflowService,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    // Simple form - only company and role required initially
    this.jobForm = this.fb.group({
      // Required fields (always visible)
      companyName: ['', Validators.required],
      jobTitle: ['', Validators.required],
      
      // Optional fields (hidden until manual mode)
      department: [''],
      location: ['Remote'],
      employmentType: ['FULL_TIME'],
      minSalary: [0],
      maxSalary: [0],
      experienceLevel: ['MID'],
      education: [''],
      keySkills: [''],
      jobDescription: ['']
    });

    // Load saved data if exists
    const saved = this.workflowService.currentJob;
    if (saved) {
      this.jobForm.patchValue(saved);
      this.selectedBenefits = saved.benefits || [];
      if (saved.jobDescription) {
        this.aiGenerationComplete = true;
        // Don't auto-show manual details, keep them hidden by default
        this.showManualDetails = false;
      }
    }

    // Subscribe to workflow progress
    this.progressSubscription = this.automatedWorkflowService.workflowProgress$.subscribe(
      progress => {
        this.workflowProgress = progress;
      }
    );
  }

  ngOnDestroy(): void {
    this.progressSubscription?.unsubscribe();
  }

  /**
   * Toggle manual details section
   */
  toggleManualMode(): void {
    this.showManualDetails = !this.showManualDetails;
    
    if (this.showManualDetails) {
      this.snackBar.open('📝 Manual mode enabled. Fill in all details below.', 'Close', {
        duration: 3000,
        panelClass: ['info-snackbar']
      });
      
      // Save current state so manual details stay open
      this.saveDraft();
    } else {
      this.snackBar.open('ℹ️ Manual mode hidden. Data is saved.', 'Close', {
        duration: 2000,
        panelClass: ['info-snackbar']
      });
    }
  }

  /**
   * Generate JD with AI using ONLY company name and job role
   */
  generateWithAI(): void {
    const companyName = this.jobForm.get('companyName')?.value;
    const jobTitle = this.jobForm.get('jobTitle')?.value;

    // Validate required fields
    if (!companyName || !jobTitle) {
      this.snackBar.open('❌ Please enter both Company Name and Job Title', 'Close', {
        duration: 4000,
        panelClass: ['error-snackbar']
      });
      return;
    }

    this.isGeneratingAI = true;

    this.snackBar.open('🤖 Generating professional job description with AI...', '', {
      duration: undefined,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['info-snackbar']
    });

    // Get additional data from form if manual mode was used
    const location = this.jobForm.get('location')?.value || 'Remote';
    const experienceLevel = this.jobForm.get('experienceLevel')?.value || 'MID';
    const employmentType = this.jobForm.get('employmentType')?.value || 'FULL_TIME';

    // Call AI service with company, role, and optional manual data
    this.aiService.generateJobDescription({
      companyName: companyName,
      jobTitle: jobTitle,
      location: location,
      experienceLevel: experienceLevel,
      employmentType: employmentType
    }).subscribe({
      next: (response: any) => {
        console.log('✅ AI Generation successful:', response);
        
        // Set the generated description
        this.jobForm.patchValue({
          jobDescription: response.job_description
        });

        this.isGeneratingAI = false;
        this.aiGenerationComplete = true;
        this.snackBar.dismiss();

        this.snackBar.open('✅ Job description generated successfully!', 'Close', {
          duration: 2000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
          panelClass: ['success-snackbar']
        });

        // Auto-redirect to review page after successful generation
        setTimeout(() => {
          this.proceedToReview();
        }, 1500);
      },
      error: (error: any) => {
        console.error('❌ AI Generation failed:', error);
        console.error('Error details:', {
          status: error.status,
          statusText: error.statusText,
          message: error.message,
          error: error.error
        });
        
        this.isGeneratingAI = false;
        this.snackBar.dismiss();

        let errorMessage = '❌ AI service unavailable. ';
        
        if (error.status === 0) {
          errorMessage += 'Please make sure AI service is running on port 5001. Run: fix-ai-generation.bat';
        } else if (error.status === 503) {
          errorMessage += 'AI service not ready. Check terminal for errors.';
        } else if (error.status === 500) {
          errorMessage += 'AI generation error. Try again or use manual mode.';
        } else {
          errorMessage += `Error: ${error.statusText || 'Unknown error'}. Try manual mode.`;
        }

        this.snackBar.open(
          errorMessage,
          'Close',
          {
            duration: 8000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['error-snackbar']
          }
        );
      }
    });
  }

  /**
   * Proceed to review page (after AI generation or manual completion)
   */
  proceedToReview(): void {
    // Validate that we have at least company, title, and description
    const companyName = this.jobForm.get('companyName')?.value;
    const jobTitle = this.jobForm.get('jobTitle')?.value;
    const jobDescription = this.jobForm.get('jobDescription')?.value;

    if (!companyName || !jobTitle) {
      this.snackBar.open('❌ Please enter Company Name and Job Title', 'Close', {
        duration: 4000,
        panelClass: ['error-snackbar']
      });
      return;
    }

    if (!jobDescription || jobDescription.length < 50) {
      this.snackBar.open('❌ Please generate a job description first (AI or Manual)', 'Close', {
        duration: 4000,
        panelClass: ['error-snackbar']
      });
      return;
    }

    // Save all data and proceed
    const jobData: Job = {
      ...this.jobForm.value,
      benefits: this.selectedBenefits
    };
    
    this.workflowService.updateJobData(jobData);
    this.workflowService.nextStep();
  }

  /**
   * Generate manual description from filled details
   */
  generateManualDescription(): void {
    const companyName = this.jobForm.get('companyName')?.value;
    const jobTitle = this.jobForm.get('jobTitle')?.value;
    const location = this.jobForm.get('location')?.value;
    const employmentType = this.jobForm.get('employmentType')?.value;
    const experienceLevel = this.jobForm.get('experienceLevel')?.value;
    const keySkills = this.jobForm.get('keySkills')?.value;
    const department = this.jobForm.get('department')?.value;
    const minSalary = this.jobForm.get('minSalary')?.value;
    const maxSalary = this.jobForm.get('maxSalary')?.value;

    // Validate
    if (!companyName || !jobTitle) {
      this.snackBar.open('❌ Please fill Company Name and Job Title', 'Close', {
        duration: 3000,
        panelClass: ['error-snackbar']
      });
      return;
    }

    // Create professional description from manual data
    let description = `# ${jobTitle}\n\n`;
    description += `## About the Role\n\n`;
    description += `${companyName} is seeking a talented ${jobTitle} to join our ${department || 'team'}. `;
    description += `This is a ${employmentType.replace('_', '-').toLowerCase()} position`;
    if (location) {
      description += ` based in ${location}`;
    }
    description += `. We're looking for someone with ${experienceLevel.toLowerCase()}-level experience.\n\n`;

    description += `## Key Responsibilities\n\n`;
    description += `- Lead and contribute to key projects and initiatives\n`;
    description += `- Collaborate with cross-functional teams\n`;
    description += `- Drive innovation and continuous improvement\n`;
    description += `- Mentor and support team members\n`;
    description += `- Deliver high-quality results\n\n`;

    description += `## Required Qualifications\n\n`;
    description += `- ${experienceLevel} level experience in ${jobTitle} or related field\n`;
    
    if (keySkills) {
      const skillsArray = keySkills.split(',').map((s: string) => s.trim());
      description += `- Strong skills in: ${skillsArray.join(', ')}\n`;
    }
    
    description += `- Excellent communication and collaboration skills\n`;
    description += `- Problem-solving mindset and attention to detail\n\n`;

    description += `## What We Offer\n\n`;
    if (this.selectedBenefits.length > 0) {
      description += this.selectedBenefits.map(b => `- ${b}`).join('\n') + '\n';
    } else {
      description += `- Competitive compensation and benefits\n`;
      description += `- Professional growth opportunities\n`;
      description += `- Collaborative work environment\n`;
    }

    if (minSalary > 0 && maxSalary > 0) {
      description += `\n## Compensation\n\n`;
      description += `Salary Range: $${minSalary.toLocaleString()} - $${maxSalary.toLocaleString()}\n`;
    }

    description += `\n## About ${companyName}\n\n`;
    description += `Join us and be part of a team that values innovation, collaboration, and excellence.\n`;

    // Set description
    this.jobForm.patchValue({
      jobDescription: description
    });

    this.aiGenerationComplete = true;

    this.snackBar.open('✅ Job description created from your details!', 'Close', {
      duration: 2000,
      horizontalPosition: 'right',
      verticalPosition: 'top',
      panelClass: ['success-snackbar']
    });
  }

  isBenefitSelected(benefit: string): boolean {
    return this.selectedBenefits.includes(benefit);
  }

  onBenefitChange(benefit: string, checked: boolean): void {
    if (checked) {
      this.selectedBenefits.push(benefit);
    } else {
      this.selectedBenefits = this.selectedBenefits.filter(b => b !== benefit);
    }
  }

  saveDraft(): void {
    const jobData: Job = {
      ...this.jobForm.value,
      benefits: this.selectedBenefits
    };
    this.workflowService.updateJobData(jobData);
    this.snackBar.open('✅ Draft saved!', 'Close', {
      duration: 2000,
      panelClass: ['success-snackbar']
    });
  }

  /**
   * Start automated workflow - runs all steps automatically
   */
  async startAutomatedWorkflow(): Promise<void> {
    const companyName = this.jobForm.get('companyName')?.value;
    const jobTitle = this.jobForm.get('jobTitle')?.value;

    if (!companyName || !jobTitle) {
      this.snackBar.open('❌ Please enter Company Name and Job Title', 'Close', {
        duration: 4000,
        panelClass: ['error-snackbar']
      });
      return;
    }

    this.isAutomatedRunning = true;

    this.snackBar.open('🚀 Starting automated hiring workflow...', '', {
      duration: 3000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: ['info-snackbar']
    });

    try {
      await this.automatedWorkflowService.startAutomatedWorkflow(companyName, jobTitle);
      
      this.isAutomatedRunning = false;
      
      this.snackBar.open('🎉 Automated workflow completed successfully!', 'Close', {
        duration: 5000,
        horizontalPosition: 'center',
        verticalPosition: 'top',
        panelClass: ['success-snackbar']
      });
    } catch (error: any) {
      console.error('❌ Automated workflow failed:', error);
      this.isAutomatedRunning = false;
      
      this.snackBar.open(
        `❌ Workflow failed: ${error.message || 'Unknown error'}`,
        'Close',
        {
          duration: 8000,
          horizontalPosition: 'center',
          verticalPosition: 'top',
          panelClass: ['error-snackbar']
        }
      );
    }
  }

  /**
   * Get icon based on workflow status
   */
  getProgressIcon(status: string): string {
    switch (status) {
      case 'running': return 'sync';
      case 'completed': return 'check_circle';
      case 'error': return 'error';
      default: return 'info';
    }
  }
}
