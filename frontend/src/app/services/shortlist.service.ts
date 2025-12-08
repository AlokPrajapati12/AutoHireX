import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ShortlistRequest {
  jobId: string;
  jobDescription?: string;
  minScore?: number;
  maxCandidates?: number;
}

export interface ShortlistResponse {
  success: boolean;
  message: string;
  totalProcessed: number;
  shortlistedCount: number;
  rejectedCount: number;
  shortlistedCandidates: ShortlistedCandidateDTO[];
  jobId: string;
  jobTitle: string;
}

export interface ShortlistedCandidateDTO {
  id: string;
  applicationId: string;
  candidateName: string;
  candidateEmail: string;
  finalScore: number;
  skillMatchPercentage: number;
  matchedSkills: string[];
  missingSkills: string[];
  llmDecision: string;
  llmReasoning: string;
  rank: number;
  status: string;
}

export interface ShortlistedCandidate {
  id: string;
  applicationId: string;
  jobId: string;
  jobTitle: string;
  candidateName: string;
  candidateEmail: string;
  candidatePhone: string;
  finalScore: number;
  semanticSimilarity?: number;
  skillMatchPercentage: number;
  experienceMatch?: number;
  educationMatch?: number;
  llmScore?: number;
  matchedSkills: string[];
  missingSkills: string[];
  llmDecision?: string;
  llmReasoning?: string;
  interviewRecommendation?: string;
  keyStrengths?: string;
  developmentAreas?: string;
  rank: number;
  status: string;
  shortlistedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ShortlistService {
  private apiUrl = 'http://localhost:8080/api/shortlist';

  constructor(private http: HttpClient) {}

  /**
   * Trigger AI shortlisting process for a job
   */
  processJobShortlisting(request: ShortlistRequest): Observable<ShortlistResponse> {
    return this.http.post<ShortlistResponse>(`${this.apiUrl}/process`, request);
  }

  /**
   * Get all shortlisted candidates for a job
   */
  getShortlistedCandidates(jobId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/job/${jobId}`);
  }

  /**
   * Get a specific shortlisted candidate
   */
  getShortlistedCandidate(id: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }

  /**
   * Update candidate status
   */
  updateCandidateStatus(id: string, status: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/status`, { status });
  }

  /**
   * Get shortlist count for a job
   */
  getShortlistCount(jobId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/job/${jobId}/count`);
  }

  /**
   * Health check
   */
  healthCheck(): Observable<any> {
    return this.http.get(`${this.apiUrl}/health`);
  }
}
