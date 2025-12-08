import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AIJobDescriptionRequest {
  companyName: string;
  jobTitle: string;
  location?: string;
  experienceLevel?: string;
  employmentType?: string;
}

export interface AIJobDescriptionResponse {
  job_description: string;
  company_name: string;
  job_title: string;
  location: string;
  experience_level: string;
  employment_type: string;
}

export interface AIHealthResponse {
  status: string;
  aiServiceAvailable: boolean;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class AiService {
  // ✅ FIXED: Now routing through backend instead of direct AI service connection
  private apiUrl = `${environment.apiUrl}/ai`;

  constructor(private http: HttpClient) {}

  /**
   * Generate job description using AI (through backend)
   * Routes: Frontend → Backend (/api/ai/generate-job-description) → AI Service
   */
  generateJobDescription(request: AIJobDescriptionRequest): Observable<any> {
    console.log('🔄 Calling backend AI endpoint:', `${this.apiUrl}/generate-job-description`);
    
    // Map frontend request to backend format
    const payload = {
      companyName: request.companyName,
      jobTitle: request.jobTitle,
      location: request.location,
      experienceLevel: request.experienceLevel,
      employmentType: request.employmentType
    };

    return this.http.post<any>(`${this.apiUrl}/generate-job-description`, payload);
  }

  /**
   * Check AI service health (through backend)
   * Routes: Frontend → Backend (/api/ai/status) → AI Service
   */
  checkAIHealth(): Observable<AIHealthResponse> {
    console.log('🔄 Checking AI health through backend:', `${this.apiUrl}/status`);
    return this.http.get<AIHealthResponse>(`${this.apiUrl}/status`);
  }

  /**
   * Get applications from AI service (through backend)
   * Routes: Frontend → Backend (/api/ai/applications) → AI Service
   */
  getApplications(): Observable<any> {
    console.log('🔄 Getting applications through backend:', `${this.apiUrl}/applications`);
    return this.http.get<any>(`${this.apiUrl}/applications`);
  }

  /**
   * Shortlist candidates (through backend)
   * Routes: Frontend → Backend (/api/ai/shortlist) → AI Service
   */
  shortlistCandidates(jobDescription: string): Observable<any> {
    console.log('🔄 Shortlisting through backend:', `${this.apiUrl}/shortlist`);
    return this.http.post<any>(`${this.apiUrl}/shortlist`, { jobDescription });
  }

  /**
   * Schedule interviews (through backend)
   * Routes: Frontend → Backend (/api/ai/schedule-interviews) → AI Service
   */
  scheduleInterviews(shortlist: any[], jobRole: string): Observable<any> {
    console.log('🔄 Scheduling interviews through backend:', `${this.apiUrl}/schedule-interviews`);
    return this.http.post<any>(`${this.apiUrl}/schedule-interviews`, { shortlist, jobRole });
  }

  /**
   * Run complete workflow (through backend)
   * Routes: Frontend → Backend (/api/ai/run-workflow) → AI Service
   */
  runCompleteWorkflow(companyName: string, jobRole: string): Observable<any> {
    console.log('🔄 Running workflow through backend:', `${this.apiUrl}/run-workflow`);
    return this.http.post<any>(`${this.apiUrl}/run-workflow`, { companyName, jobRole });
  }

  /**
   * Post job description (through backend)
   * Routes: Frontend → Backend (/api/ai/post-job-description) → AI Service
   */
  postJobDescription(data: any): Observable<any> {
    console.log('🔄 Posting job through backend:', `${this.apiUrl}/post-job-description`);
    return this.http.post<any>(`${this.apiUrl}/post-job-description`, data);
  }

  /**
   * Get monitoring status (through backend)
   * Routes: Frontend → Backend (/api/ai/monitor-status) → AI Service
   */
  getMonitorStatus(): Observable<any> {
    console.log('🔄 Getting monitor status through backend:', `${this.apiUrl}/monitor-status`);
    return this.http.get<any>(`${this.apiUrl}/monitor-status`);
  }
}
