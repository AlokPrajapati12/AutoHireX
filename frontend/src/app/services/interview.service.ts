import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ScheduleInterviewRequest {
  shortlistedCandidateId: string;
  applicationId: string;
  jobId: string;
  interviewRound: string; // ROUND_1, ROUND_2, HR_ROUND
  scheduledDate: string; // ISO date string
  scheduledTime: string; // "10:00 AM - 11:00 AM"
  interviewMode: string; // ONLINE, OFFLINE, HYBRID
  meetingLink?: string;
  venue?: string;
  interviewerNames?: string[];
  interviewerEmails?: string[];
  interviewPanel?: string;
  notes?: string;
  sendNotification?: boolean;
}

export interface InterviewFeedbackRequest {
  interviewId: string;
  feedback: string;
  technicalScore: number;
  communicationScore: number;
  overallRating: number;
  decision: string; // SELECTED, REJECTED, ON_HOLD, NEXT_ROUND
  interviewerRemarks: string;
  nextRound?: string; // ROUND_2, HR_ROUND
}

export interface Interview {
  id: string;
  shortlistedCandidateId: string;
  applicationId: string;
  jobId: string;
  candidateName: string;
  candidateEmail: string;
  candidatePhone: string;
  jobTitle: string;
  company: string;
  interviewRound: string;
  scheduledDate: string;
  scheduledTime: string;
  interviewMode: string;
  meetingLink?: string;
  venue?: string;
  interviewerNames?: string[];
  interviewerEmails?: string[];
  interviewPanel?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  feedback?: string;
  technicalScore?: number;
  communicationScore?: number;
  overallRating?: number;
  decision?: string;
  interviewerRemarks?: string;
  nextRoundId?: string;
  isLastRound: boolean;
  notes?: string;
  notificationSent: boolean;
  reminderSent: boolean;
  roundNumber: number;
}

export interface InterviewResponse {
  success: boolean;
  message: string;
  interview?: Interview;
  interviews?: Interview[];
  totalScheduled?: number;
  totalCompleted?: number;
  totalCancelled?: number;
}

@Injectable({
  providedIn: 'root'
})
export class InterviewService {
  private apiUrl = `${environment.apiUrl}/interviews`;

  constructor(private http: HttpClient) { }

  /**
   * Schedule a new interview
   */
  scheduleInterview(request: ScheduleInterviewRequest): Observable<InterviewResponse> {
    return this.http.post<InterviewResponse>(`${this.apiUrl}/schedule`, request);
  }

  /**
   * Get all interviews for a job
   */
  getInterviewsByJob(jobId: string): Observable<InterviewResponse> {
    return this.http.get<InterviewResponse>(`${this.apiUrl}/job/${jobId}`);
  }

  /**
   * Get all interviews for a candidate
   */
  getInterviewsByCandidate(candidateId: string): Observable<Interview[]> {
    return this.http.get<Interview[]>(`${this.apiUrl}/candidate/${candidateId}`);
  }

  /**
   * Get a specific interview
   */
  getInterview(interviewId: string): Observable<InterviewResponse> {
    return this.http.get<InterviewResponse>(`${this.apiUrl}/${interviewId}`);
  }

  /**
   * Update interview status
   */
  updateInterviewStatus(interviewId: string, status: string): Observable<InterviewResponse> {
    const params = new HttpParams().set('status', status);
    return this.http.put<InterviewResponse>(`${this.apiUrl}/${interviewId}/status`, null, { params });
  }

  /**
   * Submit interview feedback
   */
  submitFeedback(request: InterviewFeedbackRequest): Observable<InterviewResponse> {
    return this.http.post<InterviewResponse>(`${this.apiUrl}/feedback`, request);
  }

  /**
   * Reschedule interview
   */
  rescheduleInterview(interviewId: string, newDate: string, newTime: string): Observable<InterviewResponse> {
    const params = new HttpParams()
      .set('newDate', newDate)
      .set('newTime', newTime);
    return this.http.put<InterviewResponse>(`${this.apiUrl}/${interviewId}/reschedule`, null, { params });
  }

  /**
   * Cancel interview
   */
  cancelInterview(interviewId: string, reason: string): Observable<InterviewResponse> {
    const params = new HttpParams().set('reason', reason);
    return this.http.put<InterviewResponse>(`${this.apiUrl}/${interviewId}/cancel`, null, { params });
  }

  /**
   * Health check
   */
  healthCheck(): Observable<string> {
    return this.http.get(`${this.apiUrl}/health`, { responseType: 'text' });
  }
}
