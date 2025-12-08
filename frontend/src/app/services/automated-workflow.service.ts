import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, timer, Subscription } from 'rxjs';
import { AiService, AIJobDescriptionRequest } from './ai.service';
import { WorkflowService } from './workflow.service';
import { JobService } from './job.service';
import { Job } from '../models/job.model';

export interface ApplicationData {
  name: string;
  email: string;
  score?: number;
  [key: string]: any;
}

export interface WorkflowProgress {
  currentStep: number;
  stepName: string;
  status: 'idle' | 'running' | 'completed' | 'error' | 'waiting';  // Added 'waiting' status
  message: string;
  data?: any;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AutomatedWorkflowService {
  private workflowProgressSubject = new BehaviorSubject<WorkflowProgress>({
    currentStep: 0,
    stepName: 'Idle',
    status: 'idle',
    message: 'Waiting to start...'
  });

  private pollingSubscription?: Subscription;
  private jobDescription: string = '';
  private shortlistedCandidates: ApplicationData[] = [];
  private createdJobId: string = '';
  private isPaused: boolean = false;  // NEW: Track if workflow is paused

  workflowProgress$ = this.workflowProgressSubject.asObservable();

  constructor(
    private aiService: AiService,
    private workflowService: WorkflowService,
    private jobService: JobService
  ) {}

  /**
   * Start the automated workflow
   * NOW STOPS at Step 4 (Monitoring) and waits for user to proceed
   */
  async startAutomatedWorkflow(companyName: string, jobRole: string): Promise<void> {
    try {
      console.log('🚀 Starting automated workflow for:', companyName, jobRole);
      this.isPaused = false;
      
      // Step 1: Generate Job Description
      await this.executeStep1_GenerateJD(companyName, jobRole);
      
      // Step 2: Auto-approve and move to review
      await this.executeStep2_Review();
      
      // Step 3: Save to MongoDB and Post to Webhook
      await this.executeStep3_PostJob(companyName, jobRole);
      
      // Step 4: Monitor Applications (STOPS HERE and waits)
      await this.executeStep4_MonitorApplications();
      
      // STOP HERE - User must manually proceed to next steps
      console.log('⏸️ Workflow paused at Step 4 - User must click "Proceed to Shortlist"');
      
    } catch (error: any) {
      console.error('❌ Automated workflow failed:', error);
      this.updateProgress({
        currentStep: this.workflowProgressSubject.value.currentStep,
        stepName: 'Error',
        status: 'error',
        message: `❌ Workflow failed: ${error.message || 'Unknown error'}`,
        error: error
      });
      throw error;
    }
  }

  /**
   * NEW: Continue workflow from Step 5 onwards
   * Called when user clicks "Proceed to Shortlist" on monitoring page
   */
  async continueWorkflowFromMonitoring(jobRole: string): Promise<void> {
    try {
      console.log('▶️ Continuing workflow from Step 5...');
      this.isPaused = false;
      
      // Step 5: Shortlist Candidates
      await this.executeStep5_Shortlist();
      
      // Step 6: Schedule Interviews
      await this.executeStep6_ScheduleInterviews(jobRole);
      
      // Step 7: Send Offer (placeholder)
      await this.executeStep7_SendOffer();
      
      // Step 8: Onboarding (placeholder)
      await this.executeStep8_Onboarding();
      
      // Workflow completed
      this.updateProgress({
        currentStep: 8,
        stepName: 'Workflow Completed',
        status: 'completed',
        message: '🎉 Hiring workflow completed successfully!'
      });

      console.log('✅ Automated workflow completed successfully!');

    } catch (error: any) {
      console.error('❌ Workflow continuation failed:', error);
      this.updateProgress({
        currentStep: this.workflowProgressSubject.value.currentStep,
        stepName: 'Error',
        status: 'error',
        message: `❌ Workflow failed: ${error.message || 'Unknown error'}`,
        error: error
      });
      throw error;
    }
  }

  /**
   * Step 1: Generate Job Description
   */
  private async executeStep1_GenerateJD(companyName: string, jobRole: string): Promise<void> {
    console.log('📝 Step 1: Generating Job Description...');
    
    this.updateProgress({
      currentStep: 1,
      stepName: 'Generate Job Description',
      status: 'running',
      message: '🤖 Generating job description with AI...'
    });

    this.workflowService.setCurrentStep(1);
    await this.delay(500);

    return new Promise((resolve, reject) => {
      const request: AIJobDescriptionRequest = {
        companyName: companyName,
        jobTitle: jobRole,
        location: 'Remote',
        experienceLevel: 'MID',
        employmentType: 'FULL_TIME'
      };

      this.aiService.generateJobDescription(request)
        .subscribe({
          next: (response: any) => {
            this.jobDescription = response.job_description || response.description;
            console.log('✅ Step 1: Job description generated!');
            
            const jobData: Partial<Job> = {
              companyName: companyName,
              jobTitle: jobRole,
              jobDescription: this.jobDescription
            };
            this.workflowService.updateJobData(jobData as Job);

            this.updateProgress({
              currentStep: 1,
              stepName: 'Generate Job Description',
              status: 'completed',
              message: '✅ Job description generated successfully!',
              data: { jobDescription: this.jobDescription }
            });

            this.workflowService.completeStep(1);
            resolve();
          },
          error: (error: any) => {
            console.error('❌ Step 1 failed:', error);
            reject(new Error(`Failed to generate JD: ${error.message}`));
          }
        });
    });
  }

  /**
   * Step 2: Review & Approve (auto-approve in automated mode)
   */
  private async executeStep2_Review(): Promise<void> {
    console.log('📋 Step 2: Reviewing and approving...');
    await this.delay(1000);

    this.updateProgress({
      currentStep: 2,
      stepName: 'Review & Approve',
      status: 'running',
      message: '📋 Reviewing job description...'
    });

    this.workflowService.setCurrentStep(2);
    await this.delay(1500);

    console.log('✅ Step 2: Job description approved!');
    this.updateProgress({
      currentStep: 2,
      stepName: 'Review & Approve',
      status: 'completed',
      message: '✅ Job description approved!'
    });

    this.workflowService.completeStep(2);
  }

  /**
   * Step 3: Save to MongoDB and Post to Webhook
   */
  private async executeStep3_PostJob(companyName: string, jobRole: string): Promise<void> {
    console.log('📤 Step 3: Posting job...');
    await this.delay(1000);

    this.updateProgress({
      currentStep: 3,
      stepName: 'Post Job',
      status: 'running',
      message: '💾 Saving job to database...'
    });

    this.workflowService.setCurrentStep(3);
    await this.delay(500);

    return new Promise((resolve, reject) => {
      const jobData = {
        title: jobRole,
        description: this.jobDescription,
        company: companyName,
        location: 'Remote',
        employmentType: 'FULL_TIME',
        experienceLevel: 'MID',
        requiredSkills: '',
        salaryRange: ''
      };

      console.log('💾 Step 3a: Saving to Spring Boot MongoDB...', jobData);

      this.jobService.createJob(jobData).subscribe({
        next: (createdJob) => {
          console.log('✅ Job saved to MongoDB with ID:', createdJob.id);
          this.createdJobId = createdJob.id!;
          
          this.workflowService.setCurrentJob(createdJob);
          this.workflowService.currentJobId = createdJob.id!;  // Important: Set the job ID
          
          this.updateProgress({
            currentStep: 3,
            stepName: 'Post Job',
            status: 'running',
            message: '📤 Posting to webhook and job boards...'
          });

          console.log('📤 Step 3b: Posting to webhook via Spring Boot...');
          this.jobService.postJobToWebhook(createdJob.id!).subscribe({
            next: (webhookResponse) => {
              console.log('✅ Posted to webhook via Spring Boot:', webhookResponse);
              
              console.log('📤 Step 3c: Posting via Python AI service...');
              this.aiService.postJobDescription({ 
                company_name: companyName, 
                job_role: jobRole 
              }).subscribe({
                next: (aiResponse) => {
                  console.log('✅ Posted via Python AI service:', aiResponse);
                  
                  this.updateProgress({
                    currentStep: 3,
                    stepName: 'Post Job',
                    status: 'completed',
                    message: '✅ Job posted to MongoDB and webhook successfully!',
                    data: { 
                      mongoJob: createdJob,
                      springWebhook: webhookResponse,
                      pythonAI: aiResponse 
                    }
                  });

                  this.workflowService.completeStep(3);
                  resolve();
                },
                error: (aiError) => {
                  console.warn('⚠️ Python AI posting failed (non-critical):', aiError);
                  
                  this.updateProgress({
                    currentStep: 3,
                    stepName: 'Post Job',
                    status: 'completed',
                    message: '✅ Job posted to MongoDB and webhook (Python AI skipped)!',
                    data: { 
                      mongoJob: createdJob,
                      springWebhook: webhookResponse
                    }
                  });

                  this.workflowService.completeStep(3);
                  resolve();
                }
              });
            },
            error: (webhookError) => {
              console.warn('⚠️ Webhook posting failed (non-critical):', webhookError);
              
              this.updateProgress({
                currentStep: 3,
                stepName: 'Post Job',
                status: 'completed',
                message: '✅ Job saved to MongoDB (webhook posting skipped)!',
                data: { mongoJob: createdJob }
              });

              this.workflowService.completeStep(3);
              resolve();
            }
          });
        },
        error: (mongoError) => {
          console.error('❌ Step 3 failed - MongoDB save error:', mongoError);
          reject(new Error(`Failed to save job to MongoDB: ${mongoError.message}`));
        }
      });
    });
  }

  /**
   * Step 4: Monitor Applications
   * NOW STOPS and waits for user interaction instead of auto-continuing
   */
  private async executeStep4_MonitorApplications(): Promise<void> {
    console.log('👀 Step 4: Monitoring applications...');
    await this.delay(1000);

    this.updateProgress({
      currentStep: 4,
      stepName: 'Monitor Applications',
      status: 'running',
      message: '👀 Monitoring incoming applications...'
    });

    this.workflowService.setCurrentStep(4);
    await this.delay(500);

    // Poll for applications, but DON'T auto-continue
    return new Promise((resolve, reject) => {
      let checkCount = 0;
      const maxChecks = 60;

      this.pollingSubscription = timer(0, 3000).subscribe(() => {
        checkCount++;
        console.log(`🔄 Checking for applications (attempt ${checkCount}/${maxChecks})...`);

        this.aiService.getApplications().subscribe({
          next: (response) => {
            const count = response.count;
            console.log(`📊 Found ${count} applications`);
            
            // Check if we have enough applications
            if (count >= 3) {
              this.pollingSubscription?.unsubscribe();
              console.log('✅ Step 4: Enough applications received!');
              
              // CHANGE: Mark as WAITING instead of COMPLETED
              // Don't auto-continue - user must click button
              this.updateProgress({
                currentStep: 4,
                stepName: 'Monitor Applications',
                status: 'waiting',  // NEW STATUS
                message: `✅ Received ${count} applications! Click "Proceed to Shortlist" to continue.`,
                data: { applications: response.applications, count: count }
              });

              this.workflowService.completeStep(4);
              this.isPaused = true;  // Mark workflow as paused
              resolve();  // Resolve but DON'T continue
            } else {
              // Still waiting for more applications
              this.updateProgress({
                currentStep: 4,
                stepName: 'Monitor Applications',
                status: 'running',
                message: `📊 Received ${count} application${count !== 1 ? 's' : ''}... (minimum 3 needed)`,
                data: { applications: response.applications, count: count }
              });
              
              if (checkCount >= maxChecks) {
                this.pollingSubscription?.unsubscribe();
                console.error('❌ Step 4 timeout: Not enough applications');
                reject(new Error('Timeout: Not enough applications received'));
              }
            }
          },
          error: (error) => {
            this.pollingSubscription?.unsubscribe();
            console.error('❌ Step 4 failed:', error);
            reject(new Error(`Failed to fetch applications: ${error.message}`));
          }
        });
      });
    });
  }

  /**
   * Step 5: Shortlist Candidates
   */
  private async executeStep5_Shortlist(): Promise<void> {
    console.log('⭐ Step 5: Shortlisting candidates...');
    await this.delay(1000);

    this.updateProgress({
      currentStep: 5,
      stepName: 'Shortlist Candidates',
      status: 'running',
      message: '⭐ Analyzing candidates with ATS scoring...'
    });

    this.workflowService.setCurrentStep(5);
    await this.delay(500);

    return new Promise((resolve, reject) => {
      this.aiService.shortlistCandidates(this.jobDescription)
        .subscribe({
          next: (response: any) => {
            this.shortlistedCandidates = response.shortlist || [];
            const count = this.shortlistedCandidates.length;
            console.log(`✅ Step 5: Shortlisted ${count} candidates`);
            
            this.updateProgress({
              currentStep: 5,
              stepName: 'Shortlist Candidates',
              status: 'completed',
              message: `✅ Shortlisted ${count} top candidate${count !== 1 ? 's' : ''}!`,
              data: { shortlist: this.shortlistedCandidates, count: count }
            });

            this.workflowService.completeStep(5);
            resolve();
          },
          error: (error: any) => {
            console.error('❌ Step 5 failed:', error);
            reject(new Error(`Failed to shortlist candidates: ${error.message}`));
          }
        });
    });
  }

  /**
   * Step 6: Schedule Interviews
   */
  private async executeStep6_ScheduleInterviews(jobRole: string): Promise<void> {
    console.log('📅 Step 6: Scheduling interviews...');
    await this.delay(1000);

    this.updateProgress({
      currentStep: 6,
      stepName: 'Conduct Interviews',
      status: 'running',
      message: '📅 Scheduling interviews...'
    });

    this.workflowService.setCurrentStep(6);
    await this.delay(500);

    return new Promise((resolve, reject) => {
      this.aiService.scheduleInterviews(this.shortlistedCandidates, jobRole)
        .subscribe({
          next: (response: any) => {
            const count = response.count || 0;
            console.log(`✅ Step 6: Scheduled ${count} interviews`);
            
            this.updateProgress({
              currentStep: 6,
              stepName: 'Conduct Interviews',
              status: 'completed',
              message: `✅ Scheduled ${count} interview${count !== 1 ? 's' : ''}!`,
              data: { interviews: response.interviews, count: count }
            });

            this.workflowService.completeStep(6);
            resolve();
          },
          error: (error: any) => {
            console.error('❌ Step 6 failed:', error);
            reject(new Error(`Failed to schedule interviews: ${error.message}`));
          }
        });
    });
  }

  /**
   * Step 7: Send Offer (placeholder)
   */
  private async executeStep7_SendOffer(): Promise<void> {
    console.log('📧 Step 7: Sending offer...');
    await this.delay(1000);

    this.updateProgress({
      currentStep: 7,
      stepName: 'Send Offer',
      status: 'running',
      message: '📧 Preparing offer letter...'
    });

    this.workflowService.setCurrentStep(7);
    await this.delay(2000);

    console.log('✅ Step 7: Offer sent!');
    this.updateProgress({
      currentStep: 7,
      stepName: 'Send Offer',
      status: 'completed',
      message: '✅ Offer sent to selected candidate!'
    });

    this.workflowService.completeStep(7);
  }

  /**
   * Step 8: Onboarding (placeholder)
   */
  private async executeStep8_Onboarding(): Promise<void> {
    console.log('🎓 Step 8: Starting onboarding...');
    await this.delay(1000);

    this.updateProgress({
      currentStep: 8,
      stepName: 'Onboarding',
      status: 'running',
      message: '🎓 Initiating onboarding process...'
    });

    this.workflowService.setCurrentStep(8);
    await this.delay(2000);

    console.log('✅ Step 8: Onboarding started!');
    this.updateProgress({
      currentStep: 8,
      stepName: 'Onboarding',
      status: 'completed',
      message: '✅ Onboarding process started!'
    });

    this.workflowService.completeStep(8);
  }

  /**
   * Stop the automated workflow
   */
  stopWorkflow(): void {
    this.pollingSubscription?.unsubscribe();
    this.isPaused = true;
    this.updateProgress({
      currentStep: this.workflowProgressSubject.value.currentStep,
      stepName: 'Stopped',
      status: 'error',
      message: '⏸️ Workflow stopped by user'
    });
  }

  /**
   * Reset workflow state
   */
  resetWorkflow(): void {
    this.pollingSubscription?.unsubscribe();
    this.jobDescription = '';
    this.shortlistedCandidates = [];
    this.createdJobId = '';
    this.isPaused = false;
    this.updateProgress({
      currentStep: 0,
      stepName: 'Idle',
      status: 'idle',
      message: 'Waiting to start...'
    });
  }

  /**
   * Helper methods
   */
  private updateProgress(progress: WorkflowProgress): void {
    this.workflowProgressSubject.next(progress);
  }

  private delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  getCurrentProgress(): WorkflowProgress {
    return this.workflowProgressSubject.value;
  }

  isWorkflowPaused(): boolean {
    return this.isPaused;
  }

  getCreatedJobId(): string {
    return this.createdJobId;
  }
}
