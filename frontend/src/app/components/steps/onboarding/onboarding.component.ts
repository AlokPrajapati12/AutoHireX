import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

interface OnboardingTask {
  id: string;
  name: string;
  description?: string;
  completed: boolean;
  actionText?: string;
}

interface DocumentItem {
  name: string;
  status: 'completed' | 'pending';
}

interface DateItem {
  label: string;
  value: string;
}

interface ActionItem {
  title: string;
  description: string;
  buttonText: string;
  action: string;
}

interface TeamMember {
  name: string;
  role: string;
  email: string;
  avatar?: string;
  initials: string;
}

interface ScheduleItem {
  day: string;
  date: string;
  title: string;
  activities: string[];
}

interface ResourceItem {
  title: string;
  description: string;
  icon: string;
  actionText: string;
}

@Component({
  selector: 'app-onboarding',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './onboarding.component.html',
  styleUrls: ['./onboarding.component.scss']
})
export class OnboardingComponent implements OnInit {

  // Candidate Information
  candidateName = 'Sarah Chen';
  candidateRole = 'AI Research Scientist';
  baseSalary = 150000;
  bonus = 25000;
  offerAcceptedDate = 'December 15, 2024';

  // Progress tracking
  overallProgress = 50;
  completedTasksCount = 3;
  totalTasksCount = 6;

  // Onboarding tasks with completion status
  onboardingTasks: OnboardingTask[] = [
    {
      id: 'welcome-email',
      name: 'Send welcome email',
      completed: true
    },
    {
      id: 'employee-profile',
      name: 'Create employee profile',
      completed: true
    },
    {
      id: 'orientation',
      name: 'Schedule orientation',
      completed: true
    },
    {
      id: 'it-equipment',
      name: 'Setup IT Equipment',
      description: 'Laptop, accounts, and software access',
      completed: false,
      actionText: 'Setup'
    },
    {
      id: 'notify-team',
      name: 'Notify Team',
      description: 'Introduce new team member to colleagues',
      completed: false,
      actionText: 'Notify'
    },
    {
      id: 'workspace',
      name: 'Prepare Workspace',
      description: 'Desk setup and office access badge',
      completed: false,
      actionText: 'Prepare'
    }
  ];

  // Important dates
  importantDates: DateItem[] = [
    { label: 'Start Date:', value: 'February 1, 2024' },
    { label: 'Orientation:', value: 'February 1, 9:00 AM' },
    { label: 'First Team Meeting:', value: 'February 2, 10:00 AM' },
    { label: '90-Day Review:', value: 'May 1, 2024' }
  ];

  // Documentation checklist
  documents: DocumentItem[] = [
    { name: 'Signed Offer Letter', status: 'completed' },
    { name: 'I-9 Verification', status: 'pending' },
    { name: 'Tax Forms (W-4)', status: 'pending' },
    { name: 'Emergency Contacts', status: 'pending' }
  ];

  // Immediate actions
  immediateActions: ActionItem[] = [
    {
      title: 'Send Welcome Package',
      description: 'Company swag, handbook, and first-day information',
      buttonText: 'Send',
      action: 'sendWelcomePackage'
    },
    {
      title: 'Setup IT Equipment',
      description: 'Laptop, accounts, and software access',
      buttonText: 'Setup',
      action: 'setupITEquipment'
    },
    {
      title: 'Notify Team',
      description: 'Introduce new team member to colleagues',
      buttonText: 'Notify',
      action: 'notifyTeam'
    }
  ];

  // Team members
  teamMembers: TeamMember[] = [
    {
      name: 'Dr. Michael Rodriguez',
      role: 'AI Research Manager',
      email: 'michael.rodriguez@company.com',
      initials: 'MR'
    },
    {
      name: 'Jessica Kim',
      role: 'Senior AI Engineer (Assigned Buddy)',
      email: 'jessica.kim@company.com',
      initials: 'JK'
    },
    {
      name: 'Lisa Thompson',
      role: 'HR Business Partner',
      email: 'lisa.thompson@company.com',
      initials: 'LT'
    }
  ];

  // First week schedule
  scheduleItems: ScheduleItem[] = [
    {
      day: 'Day 1',
      date: 'Feb 1',
      title: 'Welcome & Orientation',
      activities: [
        '9:00 AM - Office tour and initial setup',
        '11:00 AM - HR orientation session',
        '2:00 PM - Team introduction lunch'
      ]
    },
    {
      day: 'Day 2',
      date: 'Feb 2',
      title: 'Technical Setup & Training',
      activities: [
        '9:00 AM - IT equipment and software setup',
        '10:00 AM - First team meeting',
        '2:00 PM - Project overview and goals'
      ]
    },
    {
      day: 'Day 3',
      date: 'Feb 3',
      title: 'Deep Dive Sessions',
      activities: [
        '9:00 AM - Company culture workshop',
        '11:00 AM - Department processes training',
        '3:00 PM - One-on-one with manager'
      ]
    }
  ];

  // Resources and quick links
  resources: ResourceItem[] = [
    {
      title: 'Employee Handbook',
      description: 'Company policies and procedures',
      icon: '📖',
      actionText: 'View'
    },
    {
      title: 'Benefits Portal',
      description: 'Health insurance and retirement plans',
      icon: '💼',
      actionText: 'Access'
    },
    {
      title: 'Learning Hub',
      description: 'Training courses and development',
      icon: '🎓',
      actionText: 'Learn'
    },
    {
      title: 'IT Support',
      description: 'Technical help and system access',
      icon: '🛠️',
      actionText: 'Contact'
    }
  ];

  ngOnInit(): void {
    this.calculateProgress();
  }

  calculateProgress(): void {
    const completed = this.onboardingTasks.filter(task => task.completed).length;
    this.completedTasksCount = completed;
    this.overallProgress = Math.round((completed / this.onboardingTasks.length) * 100);
  }

  // Action handlers for various buttons
  setupITEquipment(): void {
    console.log('Setting up IT equipment...');
    this.completeTask('it-equipment');
    // Handle IT setup process
  }

  notifyTeam(): void {
    console.log('Notifying team...');
    this.completeTask('notify-team');
    // Handle team notification
  }

  sendWelcomePackage(): void {
    console.log('Sending welcome package...');
    // Handle welcome package sending
  }

  prepareWorkspace(): void {
    console.log('Preparing workspace...');
    this.completeTask('workspace');
    // Handle workspace preparation
  }

  completeTask(taskId: string): void {
    const task = this.onboardingTasks.find(t => t.id === taskId);
    if (task) {
      task.completed = true;
      this.calculateProgress();
    }
  }

  contactMember(email: string): void {
    window.location.href = `mailto:${email}`;
  }

  accessResource(resource: string): void {
    console.log(`Accessing resource: ${resource}`);
    // Handle resource access based on the resource type
    switch (resource) {
      case 'Employee Handbook':
        // Open handbook
        break;
      case 'Benefits Portal':
        // Navigate to benefits
        break;
      case 'Learning Hub':
        // Open learning platform
        break;
      case 'IT Support':
        // Open support ticket or contact
        break;
    }
  }

  executeAction(actionType: string): void {
    switch (actionType) {
      case 'sendWelcomePackage':
        this.sendWelcomePackage();
        break;
      case 'setupITEquipment':
        this.setupITEquipment();
        break;
      case 'notifyTeam':
        this.notifyTeam();
        break;
      default:
        console.log(`Unknown action: ${actionType}`);
    }
  }

  // Utility method to get progress percentage
  getProgressPercentage(): number {
    return this.overallProgress;
  }

  // Utility method to get completed vs total tasks
  getTaskProgress(): string {
    return `${this.completedTasksCount}/${this.totalTasksCount}`;
  }

  // Check if all critical tasks are completed
  isCriticalTasksCompleted(): boolean {
    const criticalTasks = ['welcome-email', 'employee-profile', 'it-equipment'];
    return criticalTasks.every(taskId => 
      this.onboardingTasks.find(t => t.id === taskId)?.completed
    );
  }

  // Get total compensation package
  getTotalCompensation(): number {
    return this.baseSalary + this.bonus;
  }

  // Format currency
  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount);
  }
}
