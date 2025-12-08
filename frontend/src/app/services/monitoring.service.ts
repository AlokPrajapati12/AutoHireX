import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


interface MonitoringStatus {
  jobId: string;
  jobTitle: string;
  applicationCount: number;
  enoughApplications: boolean;
  minimumRequired: number;
  jobStatus: string;
  autoClosedJob?: boolean;
  message: string;
}

interface CanAcceptResponse {
  jobId: string;
  canAcceptApplications: boolean;
  isFull: boolean;
  isOpen: boolean;
  currentCount: number;
  threshold: number;
  message: string;
}

interface AIMonitoringStatus {
  aiServiceConnected: boolean;
  applications: any[];
  count: number;
  enoughApplications: boolean;
  minimumRequired: number;
  status: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class MonitoringService {
  private apiUrl = 'http://localhost:8080/api/monitoring';
  private aiServiceUrl = 'http://localhost:5001';

  constructor(private http: HttpClient) { }

  /**
   * Check monitoring status for a specific job
   * Returns if job has enough applications and should be closed
   */
  monitorJob(jobId: string): Observable<MonitoringStatus> {
    return this.http.get<MonitoringStatus>(`${this.apiUrl}/job/${jobId}`);
  }

  /**
   * Check all jobs and auto-close those with enough applications
   */
  checkAllJobs(): Observable<any> {
    return this.http.post(`${this.apiUrl}/check-all`, {});
  }

  /**
   * Check if a job can still accept applications
   * Returns false if job is full (enough candidates) or closed
   */
  canAcceptApplications(jobId: string): Observable<CanAcceptResponse> {
    return this.http.get<CanAcceptResponse>(`${this.apiUrl}/job/${jobId}/can-accept`);
  }

  /**
   * Get AI monitoring status from Python service
   */
  getAIMonitoringStatus(): Observable<AIMonitoringStatus> {
    return this.http.get<AIMonitoringStatus>(`${this.apiUrl}/ai-status`);
  }

  /**
   * Get direct AI service monitoring status
   */
  getDirectAIStatus(): Observable<any> {
    return this.http.get(`${this.aiServiceUrl}/monitor-status`);
  }
}
