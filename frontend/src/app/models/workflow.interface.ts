export interface WorkflowStep {
  id: number;
  title: string;
  subtitle: string;
  completed: boolean;
  icon?: string;
}

export interface ApplicationStats {
  totalApplications: number;
  qualified: number;
  underReview: number;
  rejected: number;
  views: number;
  daysPosted: number;
  responseTime: string;
  applicationRate?: string;
  qualityScore?: string;
}

export interface InterviewSchedule {
  candidate: string;
  type: string;
  time: string;
  status: 'Scheduled' | 'Pending' | 'Completed';
}

export interface OnboardingTask {
  task: string;
  status: 'Completed' | 'Pending' | 'In Progress';
}


