import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { WorkflowStep } from '../models/workflow.interface';
import { Job } from '../models/job.model';

@Injectable({
  providedIn: 'root'
})
export class WorkflowService {

  /* -------------------------------------------------------
     AUTOMATION CONTROL
     ------------------------------------------------------- */
  public automationEnabled = true;  // Auto-run steps until Step 3 is reached

  stopAutomation() {
    this.automationEnabled = false;
    console.log('🛑 Automation disabled');
    this.saveWorkflowState();
  }

  /* -------------------------------------------------------
     WORKFLOW STEP STATE
     ------------------------------------------------------- */
  private currentStepSubject = new BehaviorSubject<number>(1);

  private workflowStepsSubject = new BehaviorSubject<WorkflowStep[]>([
    { id: 1, title: 'Create Job Description', subtitle: 'Define role requirements', completed: false },
    { id: 2, title: 'Review & Approve', subtitle: 'Review generated JD', completed: false },
    { id: 3, title: 'Post Job', subtitle: 'Publish job online', completed: false },
    { id: 4, title: 'Monitor Applications', subtitle: 'Track applicants', completed: false },
    { id: 5, title: 'Shortlist Candidates', subtitle: 'Identify best candidates', completed: false },
    { id: 6, title: 'Conduct Interviews', subtitle: 'Interview candidates', completed: false },
    { id: 7, title: 'Send Offer', subtitle: 'Send job offer', completed: false },
    { id: 8, title: 'Onboarding', subtitle: 'Onboard new hire', completed: false }
  ]);

  get currentStep$(): Observable<number> {
    return this.currentStepSubject.asObservable();
  }

  get workflowSteps$(): Observable<WorkflowStep[]> {
    return this.workflowStepsSubject.asObservable();
  }

  /* -------------------------------------------------------
     JOB STATE
     ------------------------------------------------------- */
  private currentJobSubject = new BehaviorSubject<Job | null>(null);
  private currentJobIdSubject = new BehaviorSubject<string | null>(null);

  get currentJob$(): Observable<Job | null> {
    return this.currentJobSubject.asObservable();
  }

  get currentJob(): Job | null {
    return this.currentJobSubject.value;
  }

  get currentJobId(): string | null {
    return this.currentJobIdSubject.value;
  }

  set currentJobId(id: string | null) {
    this.currentJobIdSubject.next(id);
    this.saveWorkflowState();
  }

  // Backwards-compatible accessor (some components expect currentJobData)
  get currentJobData(): Job | null {
    return this.currentJobSubject.value;
  }

  constructor() {
    this.loadWorkflowState();
  }

  /* -------------------------------------------------------
     SAVE + LOAD LocalStorage
     ------------------------------------------------------- */
  private saveWorkflowState(): void {
    const state = {
      step: this.currentStepSubject.value,
      steps: this.workflowStepsSubject.value,
      job: this.currentJobSubject.value,
      jobId: this.currentJobIdSubject.value,
      automationEnabled: this.automationEnabled
    };
    localStorage.setItem('workflow_state', JSON.stringify(state));
  }

  private loadWorkflowState(): void {
    const saved = localStorage.getItem('workflow_state');
    if (!saved) return;

    try {
      const state = JSON.parse(saved);

      if (state.step) this.currentStepSubject.next(state.step);
      if (state.steps) this.workflowStepsSubject.next(state.steps);
      if (state.job) this.currentJobSubject.next(state.job);
      if (state.jobId) this.currentJobIdSubject.next(state.jobId);
      if (state.automationEnabled === false) this.automationEnabled = false;
    } catch (e) {
      console.error('Failed to load workflow state:', e);
    }
  }

  clearWorkflowState(): void {
    localStorage.removeItem('workflow_state');
    this.currentJobSubject.next(null);
    this.currentJobIdSubject.next(null);
    this.automationEnabled = true;
  }

  /* -------------------------------------------------------
     JOB MANAGEMENT
     ------------------------------------------------------- */
  updateJobData(jobPartial: Partial<Job>): void {
    const existing = this.currentJobSubject.value || ({} as Job);
    const merged = { ...existing, ...jobPartial };
    this.currentJobSubject.next(merged as Job);
    this.saveWorkflowState();
  }

  setCurrentJob(job: Job): void {
    this.currentJobSubject.next(job);
    if (job.id) this.currentJobIdSubject.next(job.id);
    this.saveWorkflowState();
  }

  /* -------------------------------------------------------
     WORKFLOW STEP MANAGEMENT
     ------------------------------------------------------- */
  setCurrentStep(step: number): void {
    if (step < 1 || step > 8) return;
    this.currentStepSubject.next(step);
    this.saveWorkflowState();
  }

  completeStep(stepId: number): void {
    const steps = this.workflowStepsSubject.value;
    const step = steps.find(s => s.id === stepId);
    if (step) {
      step.completed = true;
      this.workflowStepsSubject.next([...steps]);
      this.saveWorkflowState();
    }
  }

  /**
   * Auto-step method used by any component that wants to progress automatically.
   * This only auto-runs steps 1 -> 2 -> 3 then disables automation.
   */
  nextStep(): void {
    if (!this.automationEnabled) {
      console.log('🛑 Automation is disabled; nextStep will not auto-run.');
      return;
    }

    const current = this.currentStepSubject.value;

    // Only auto-advance up to Step 3
    if (current < 3) {
      this.completeStep(current);
      this.setCurrentStep(current + 1);

      if (current + 1 === 3) {
        // Reached Step 3 — disable further automation
        this.automationEnabled = false;
        this.saveWorkflowState();
        console.log('🛑 Auto-workflow stopped at Step 3.');
      }
    }
  }

  previousStep(): void {
    const current = this.currentStepSubject.value;
    if (current > 1) this.setCurrentStep(current - 1);
  }

  /* -------------------------------------------------------
     BUTTON LABELS + ICONS
     ------------------------------------------------------- */
  getNextButtonText(step: number): string {
    switch (step) {
      case 1: return 'Continue to Review';
      case 2: return 'Approve & Post Job';
      case 3: return 'Start Monitoring';
      case 4: return 'Review Applications';
      case 5: return 'Schedule Interviews';
      case 6: return 'Prepare Offer';
      case 7: return 'Begin Onboarding';
      case 8: return 'Complete Workflow';
      default: return 'Next';
    }
  }

  getNextButtonIcon(step: number): string {
    switch (step) {
      case 2: return 'check';
      case 3: return 'assignment';
      case 4: return 'star';
      case 5: return 'event';
      case 6: return 'work';
      case 7: return 'celebration';
      case 8: return 'done_all';
      default: return 'arrow_forward';
    }
  }

  /* -------------------------------------------------------
     WORKFLOW ACTIONS
     ------------------------------------------------------- */
  approveJobDescription(): void {
    this.completeStep(2);
    this.setCurrentStep(3);
  }

  startMonitoring(): void {
    this.completeStep(3);
    this.setCurrentStep(4);
  }

  completeWorkflow(): void {
    this.completeStep(8);
    this.saveWorkflowState();
  }

  isWorkflowCompleted(): boolean {
    return this.workflowStepsSubject.value.every(s => s.completed);
  }

  startNewWorkflow(): void {
    this.clearWorkflowState();
    this.currentStepSubject.next(1);

    const steps = this.workflowStepsSubject.value;
    steps.forEach(s => s.completed = false);
    this.workflowStepsSubject.next([...steps]);

    this.saveWorkflowState();
  }
}
