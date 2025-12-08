import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Job, JobRequest, JobApplication, ApplicationRequest } from '../models/job.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class JobService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getAllJobs(): Observable<Job[]> {
    return this.http.get<Job[]>(`${this.apiUrl}/jobs/list`);
  }

  getJobById(id: string | number): Observable<Job> {
    return this.http.get<Job>(`${this.apiUrl}/jobs/${id}`);
  }

  createJob(request: JobRequest): Observable<Job> {
    // Use public endpoint - no authentication required
    return this.http.post<Job>(`${this.apiUrl}/jobs`, request);
  }

  updateJob(id: string | number, request: JobRequest): Observable<Job> {
    return this.http.put<Job>(`${this.apiUrl}/employer/jobs/${id}`, request);
  }

  deleteJob(id: string | number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/employer/jobs/${id}`);
  }

  getEmployerJobs(): Observable<Job[]> {
    return this.http.get<Job[]>(`${this.apiUrl}/employer/jobs`);
  }

  submitApplication(request: ApplicationRequest): Observable<JobApplication> {
    return this.http.post<JobApplication>(`${this.apiUrl}/candidate/applications`, request);
  }

  getMyApplications(): Observable<JobApplication[]> {
    return this.http.get<JobApplication[]>(`${this.apiUrl}/candidate/applications`);
  }

  getJobApplications(jobId: string | number): Observable<JobApplication[]> {
    return this.http.get<JobApplication[]>(`${this.apiUrl}/employer/applications/job/${jobId}`);
  }

  /**
   * Post existing job to webhook.site
   * This calls the backend endpoint: POST /api/jobs/{id}/webhook
   */
  postJobToWebhook(jobId: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/jobs/${jobId}/webhook`, {});
  }
}
