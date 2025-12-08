import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { WorkflowService } from '../../services/workflow.service';
import { WorkflowStep } from '../../models/workflow.interface';

// Standalone components
import { SidebarComponent } from '../../shared/sidebar/sidebar.component';
import { CreateJobComponent } from '../steps/create-job/create-job.component';
import { ReviewApproveComponent } from '../steps/review-approve/review-approve.component';
import { PostJobComponent } from '../steps/post-job/post-job.component';
import { MonitorAppsComponent } from '../steps/monitor-apps/monitor-apps.component';
import { ShortlistComponent } from '../steps/shortlist/shortlist.component';
import { InterviewsComponent } from '../steps/interviews/interviews.component';
import { SendOfferComponent } from '../steps/send-offer/send-offer.component';
import { OnboardingComponent } from '../steps/onboarding/onboarding.component';

@Component({
  selector: 'app-workflow',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    SidebarComponent,
    CreateJobComponent,
    ReviewApproveComponent,
    PostJobComponent,
    MonitorAppsComponent,
    ShortlistComponent,
    InterviewsComponent,
    SendOfferComponent,
    OnboardingComponent
  ],
  templateUrl: './workflow.component.html',
  styleUrls: ['./workflow.component.css']
})
export class WorkflowComponent implements OnInit {
  currentStep: number = 1;
  workflowSteps: WorkflowStep[] = [];
  isWorkflowCompleted: boolean = false;

  @ViewChild(CreateJobComponent)
  createJobStep?: CreateJobComponent;

  constructor(
    private workflowService: WorkflowService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {

    this.workflowService.workflowSteps$.subscribe(steps => {
      this.workflowSteps = steps;
    });

    this.workflowService.currentStep$.subscribe(step => {
      this.currentStep = step;

      const currentRoute = this.route.snapshot.paramMap.get('step');
      if (currentRoute !== step.toString()) {
        this.router.navigate(['/workflow', step], { replaceUrl: true });
      }

      this.isWorkflowCompleted = this.workflowService.isWorkflowCompleted();
    });

    this.route.paramMap.subscribe(params => {
      const step = params.get('step');
      if (step) {
        const stepNumber = parseInt(step, 10);
        this.workflowService.setCurrentStep(stepNumber);
        this.currentStep = stepNumber;
      }
    });
  }

  onStepClick(stepId: number): void {
    this.router.navigate(['/workflow', stepId]);
  }

  nextStep(): void {
    this.workflowService.nextStep();
  }

  previousStep(): void {
    this.workflowService.previousStep();
  }

  get nextButtonText(): string {
    return this.workflowService.getNextButtonText(this.currentStep);
  }

  get nextButtonIcon(): string {
    return this.workflowService.getNextButtonIcon(this.currentStep);
  }

  completeWorkflow(): void {
    this.workflowService.completeWorkflow();
  }

  startNewWorkflow(): void {
    this.workflowService.startNewWorkflow();
    this.router.navigate(['/workflow', 1]);
  }
}
