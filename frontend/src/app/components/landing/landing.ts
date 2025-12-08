import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';

interface Feature {
  icon: string;
  title: string;
  description: string;
  items: string[];
  gradient: string;
}

interface WorkflowStep {
  icon: string;
  title: string;
  description: string;
  aiAgent: string;
}

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule
  ],
  templateUrl: './landing.html',
  styleUrls: ['./landing.css']
})
export class LandingComponent implements OnInit {
  
  particles: Array<{x: number, y: number, delay: number}> = [];
  
  features: Feature[] = [
    {
      icon: 'psychology',
      title: 'AI Job Description Generator',
      description: 'Autonomous creation of compelling, compliant job descriptions powered by advanced LLM agents',
      items: [
        'Context-aware content generation',
        'Industry-specific optimization',
        'ATS compliance checking',
        'Multi-language support'
      ],
      gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    },
    {
      icon: 'auto_awesome',
      title: 'Intelligent Candidate Screening',
      description: 'Multi-agent system analyzes resumes, extracts insights, and ranks candidates automatically',
      items: [
        'NLP-powered resume parsing',
        'Skill matching & scoring',
        'Automated shortlisting',
        'Bias-free evaluation'
      ],
      gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
    },
    {
      icon: 'hub',
      title: 'LangGraph Orchestration',
      description: 'Advanced workflow management with autonomous agent coordination and decision-making',
      items: [
        'State management',
        'Agent collaboration',
        'Dynamic routing',
        'Self-optimization'
      ],
      gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
    },
    {
      icon: 'insights',
      title: 'Real-time Analytics',
      description: 'Comprehensive insights powered by AI analysis of recruitment metrics and patterns',
      items: [
        'Pipeline analytics',
        'Performance tracking',
        'Predictive insights',
        'Custom dashboards'
      ],
      gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
    },
    {
      icon: 'schedule',
      title: 'Autonomous Scheduling',
      description: 'AI agents handle interview coordination, calendar management, and communication',
      items: [
        'Smart time allocation',
        'Automated reminders',
        'Conflict resolution',
        'Multi-timezone support'
      ],
      gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
    },
    {
      icon: 'badge',
      title: 'Intelligent Onboarding',
      description: 'Automated onboarding workflows with personalized experiences for new hires',
      items: [
        'Document automation',
        'Task assignment',
        'Progress tracking',
        'Integration ready'
      ],
      gradient: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)'
    }
  ];

  workflowSteps: WorkflowStep[] = [
    {
      icon: 'description',
      title: 'JD Creation',
      description: 'AI agent generates optimized job descriptions based on requirements',
      aiAgent: 'Content Generator Agent'
    },
    {
      icon: 'fact_check',
      title: 'Review & Approval',
      description: 'Compliance agent validates and refines job postings',
      aiAgent: 'Validation Agent'
    },
    {
      icon: 'publish',
      title: 'Multi-Channel Distribution',
      description: 'Distribution agent posts to multiple job boards automatically',
      aiAgent: 'Publisher Agent'
    },
    {
      icon: 'monitor',
      title: 'Application Monitoring',
      description: 'Tracking agent continuously monitors and categorizes applications',
      aiAgent: 'Monitoring Agent'
    },
    {
      icon: 'auto_awesome',
      title: 'AI Shortlisting',
      description: 'Screening agent analyzes and ranks candidates using NLP',
      aiAgent: 'Screening Agent'
    },
    {
      icon: 'event',
      title: 'Interview Coordination',
      description: 'Scheduler agent manages interview logistics autonomously',
      aiAgent: 'Scheduler Agent'
    },
    {
      icon: 'send',
      title: 'Offer Management',
      description: 'Offer agent prepares and sends personalized offer letters',
      aiAgent: 'Offer Agent'
    },
    {
      icon: 'how_to_reg',
      title: 'Onboarding Automation',
      description: 'Onboarding agent orchestrates new hire integration',
      aiAgent: 'Onboarding Agent'
    }
  ];

  constructor(private router: Router) {
    // Generate random particles for animation
    for (let i = 0; i < 20; i++) {
      this.particles.push({
        x: Math.random() * 100,
        y: Math.random() * 100,
        delay: Math.random() * 15
      });
    }
  }

  ngOnInit(): void {
    // Smooth scroll for anchor links
    this.setupSmoothScroll();
  }

  startWorkflow(): void {
    // Clear any existing workflow state to start fresh
    localStorage.removeItem('workflow_state');
    localStorage.removeItem('shortlistJobId');
    this.router.navigate(['/workflow', 1]);
  }

  goToMonitoring(): void {
    // Navigate directly to monitoring page (Step 4)
    this.router.navigate(['/workflow', 4]);
  }

  private setupSmoothScroll(): void {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
      anchor.addEventListener('click', (e) => {
        e.preventDefault();
        const target = document.querySelector(anchor.getAttribute('href')!);
        target?.scrollIntoView({
          behavior: 'smooth',
          block: 'start'
        });
      });
    });
  }
}
