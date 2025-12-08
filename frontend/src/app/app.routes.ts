import { Routes } from '@angular/router';
import { LandingComponent } from './components/landing/landing';
import { WorkflowComponent } from './components/workflow/workflow.component';
import { ConnectionTestComponent } from './components/connection-test/connection-test.component';
import { JobBoardComponent } from './components/public/job-board/job-board.component';
import { JobDetailsComponent } from './components/public/job-details/job-details.component';
import { ApplyJobComponent } from './components/apply/apply-job.component';
import { MonitorAppsComponent } from './components/steps/monitor-apps/monitor-apps.component';
import { OnboardingComponent } from './components/onboarding/onboarding.component';

export const appRoutes: Routes = [
  // Landing Page as Main Dashboard/Home
  { 
    path: '', 
    component: LandingComponent,
    data: { title: 'AutoHireX - AI-Powered Recruitment' }
  },
  
  // Dashboard alias (redirects to landing)
  { 
    path: 'dashboard', 
    redirectTo: '',
    pathMatch: 'full'
  },
  
  // Workflow
  { 
    path: 'workflow', 
    component: WorkflowComponent 
  },
  { 
    path: 'workflow/:step', 
    component: WorkflowComponent 
  },
  
  // Connection test
  { 
    path: 'connection-test', 
    component: ConnectionTestComponent 
  },
  { 
    path: 'test', 
    component: ConnectionTestComponent 
  },
  
  // Public job board
  { 
    path: 'jobs', 
    component: JobBoardComponent 
  },
  { 
    path: 'jobs/:id', 
    component: JobDetailsComponent 
  },
  
  // ⭐ APPLICATION FORM - This is the important route!
  { 
    path: 'apply/:id', 
    component: ApplyJobComponent,
    data: { 
      title: 'Apply for Job',
      requiresAuth: false  // Public route, no auth needed
    }
  },
  
  // 🎯 ADMIN MONITORING DASHBOARD
  { 
    path: 'admin/monitoring', 
    component: MonitorAppsComponent,
    data: { 
      title: 'Application Monitoring',
      requiresAuth: false  // TODO: Add authentication guard in production
    }
  },
  
  // 🎓 ONBOARDING & DOCUMENT COLLECTION
  { 
    path: 'admin/onboarding', 
    component: OnboardingComponent,
    data: { 
      title: 'Onboarding & Documents',
      requiresAuth: false  // TODO: Add authentication guard in production
    }
  },
  
  // Catch all - redirect to home (landing page)
  { 
    path: '**', 
    redirectTo: '' 
  }
];
