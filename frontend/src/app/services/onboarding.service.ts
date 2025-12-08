import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Onboarding,
  OnboardingCreateRequest,
  DocumentUploadRequest,
  OnboardingUpdateRequest,
  OfferLetter
} from '../models/onboarding.model';

@Injectable({
  providedIn: 'root'
})
export class OnboardingService {
  private apiUrl = 'http://localhost:8080/api/onboarding';

  constructor(private http: HttpClient) {}

  /**
   * Create new onboarding
   */
  createOnboarding(request: OnboardingCreateRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}`, request);
  }

  /**
   * Get all onboardings
   */
  getAllOnboardings(): Observable<any> {
    return this.http.get(`${this.apiUrl}`);
  }

  /**
   * Get onboarding by ID
   */
  getOnboardingById(id: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }

  /**
   * Get onboarding by candidate ID
   */
  getOnboardingByCandidateId(candidateId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/candidate/${candidateId}`);
  }

  /**
   * Get onboarding by employee ID
   */
  getOnboardingByEmployeeId(employeeId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/employee/${employeeId}`);
  }

  /**
   * Get onboardings by status
   */
  getOnboardingsByStatus(status: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/status/${status}`);
  }

  /**
   * Get onboardings by current step
   */
  getOnboardingsByStep(step: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/step/${step}`);
  }

  /**
   * Get candidates eligible for onboarding
   */
  getEligibleCandidates(): Observable<any> {
    return this.http.get(`${this.apiUrl}/eligible-candidates`);
  }

  /**
   * Upload document
   */
  uploadDocument(request: DocumentUploadRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/upload-document`, request);
  }

  /**
   * Verify document
   */
  verifyDocument(
    onboardingId: string,
    documentType: string,
    verifiedBy: string,
    remarks?: string
  ): Observable<any> {
    const params: any = { documentType, verifiedBy };
    if (remarks) {
      params.remarks = remarks;
    }
    return this.http.post(`${this.apiUrl}/${onboardingId}/verify-document`, null, { params });
  }

  /**
   * Update onboarding
   */
  updateOnboarding(id: string, request: OnboardingUpdateRequest): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Complete onboarding
   */
  completeOnboarding(id: string, approvedBy: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/complete`, null, { params: { approvedBy } });
  }

  /**
   * Delete onboarding
   */
  deleteOnboarding(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  /**
   * Convert file to base64
   */
  convertFileToBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result as string);
      reader.onerror = error => reject(error);
    });
  }

  /**
   * Get status badge color
   */
  getStatusColor(status: string): string {
    const colors: { [key: string]: string } = {
      'PENDING': 'bg-yellow-100 text-yellow-800',
      'DOCUMENTS_SUBMITTED': 'bg-blue-100 text-blue-800',
      'VERIFIED': 'bg-purple-100 text-purple-800',
      'APPROVED': 'bg-green-100 text-green-800',
      'COMPLETED': 'bg-green-600 text-white',
      'REJECTED': 'bg-red-100 text-red-800'
    };
    return colors[status] || 'bg-gray-100 text-gray-800';
  }

  /**
   * Get step badge color
   */
  getStepColor(step: string): string {
    const colors: { [key: string]: string } = {
      'DOCUMENT_COLLECTION': 'bg-blue-100 text-blue-800',
      'VERIFICATION': 'bg-purple-100 text-purple-800',
      'SYSTEM_SETUP': 'bg-indigo-100 text-indigo-800',
      'ORIENTATION': 'bg-teal-100 text-teal-800',
      'COMPLETED': 'bg-green-600 text-white'
    };
    return colors[step] || 'bg-gray-100 text-gray-800';
  }

  /**
   * Format status text
   */
  formatStatus(status: string): string {
    return status.replace(/_/g, ' ');
  }

  /**
   * Get completion percentage color
   */
  getCompletionColor(percentage: number): string {
    if (percentage < 25) return 'bg-red-500';
    if (percentage < 50) return 'bg-yellow-500';
    if (percentage < 75) return 'bg-blue-500';
    if (percentage < 100) return 'bg-purple-500';
    return 'bg-green-500';
  }
}
