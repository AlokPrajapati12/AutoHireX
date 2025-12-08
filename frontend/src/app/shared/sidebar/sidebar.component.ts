import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { WorkflowStep } from '../../models/workflow.interface';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent {
  @Input() steps: WorkflowStep[] = [];
  @Input() currentStep: number = 1;
  @Output() stepClick = new EventEmitter<number>();

  onStepClick(stepId: number) {
    this.stepClick.emit(stepId);
  }

  isStepActive(stepId: number): boolean {
    return this.currentStep === stepId;
  }

  isStepCompleted(step: WorkflowStep): boolean {
    return step.completed;
  }

  canNavigateToStep(stepId: number): boolean {
    // Users can navigate to any completed step or current step
    const step = this.steps.find(s => s.id === stepId);
    return !!step && (step.completed || stepId === this.currentStep);
  }

  getCompletedStepsCount(): number {
    return this.steps.filter(step => step.completed).length;
  }

  getProgressPercentage(): number {
    if (!this.steps || this.steps.length === 0) {
      return 0;
    }
    const completedCount = this.getCompletedStepsCount();
    return Math.round((completedCount / this.steps.length) * 100);
  }

  hasCompletedSteps(): boolean {
    return this.getCompletedStepsCount() > 0;
  }
}
