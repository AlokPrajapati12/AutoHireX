import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { 
  OfferLetter, 
  OfferLetterRequest, 
  OfferLetterResponse,
  EligibleCandidate 
} from '../models/offer-letter.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class OfferLetterService {
  private apiUrl = `${environment.apiUrl}/offer-letters`;

  constructor(private http: HttpClient) { }

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json'
    });
  }

  /**
   * Generate new offer letter
   */
  generateOfferLetter(request: OfferLetterRequest): Observable<OfferLetterResponse> {
    return this.http.post<OfferLetterResponse>(
      `${this.apiUrl}/generate`, 
      request,
      { headers: this.getHeaders() }
    );
  }

  /**
   * Get all offer letters
   */
  getAllOfferLetters(): Observable<OfferLetterResponse[]> {
    return this.http.get<OfferLetterResponse[]>(this.apiUrl);
  }

  /**
   * Get offer letter by ID
   */
  getOfferLetterById(id: string): Observable<OfferLetterResponse> {
    return this.http.get<OfferLetterResponse>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get offer letter by candidate ID
   */
  getOfferLetterByCandidateId(candidateId: string): Observable<OfferLetterResponse> {
    return this.http.get<OfferLetterResponse>(`${this.apiUrl}/candidate/${candidateId}`);
  }

  /**
   * Get offer letters by status
   */
  getOfferLettersByStatus(status: string): Observable<OfferLetterResponse[]> {
    return this.http.get<OfferLetterResponse[]>(`${this.apiUrl}/status/${status}`);
  }

  /**
   * Send offer letter to candidate
   */
  sendOfferLetter(id: string): Observable<OfferLetterResponse> {
    return this.http.post<OfferLetterResponse>(
      `${this.apiUrl}/${id}/send`,
      {},
      { headers: this.getHeaders() }
    );
  }

  /**
   * Accept offer letter
   */
  acceptOfferLetter(id: string, acceptanceMethod?: string): Observable<OfferLetterResponse> {
    return this.http.post<OfferLetterResponse>(
      `${this.apiUrl}/${id}/accept`,
      { acceptanceMethod: acceptanceMethod || 'PORTAL' },
      { headers: this.getHeaders() }
    );
  }

  /**
   * Reject offer letter
   */
  rejectOfferLetter(id: string, rejectionReason: string): Observable<OfferLetterResponse> {
    return this.http.post<OfferLetterResponse>(
      `${this.apiUrl}/${id}/reject`,
      { rejectionReason },
      { headers: this.getHeaders() }
    );
  }

  /**
   * Download offer letter
   */
  downloadOfferLetter(id: string): Observable<OfferLetterResponse> {
    return this.http.get<OfferLetterResponse>(`${this.apiUrl}/${id}/download`);
  }

  /**
   * Get candidates eligible for offer letter
   */
  getEligibleCandidates(): Observable<EligibleCandidate[]> {
    return this.http.get<EligibleCandidate[]>(`${this.apiUrl}/eligible-candidates`);
  }

  /**
   * Calculate salary breakdown based on CTC
   */
  calculateSalaryBreakdown(ctc: number): {
    basic: number,
    hra: number,
    specialAllowance: number,
    performanceBonus: number
  } {
    const basic = Math.round(ctc * 0.40); // 40% of CTC
    const hra = Math.round(ctc * 0.20); // 20% of CTC
    const specialAllowance = Math.round(ctc * 0.30); // 30% of CTC
    const performanceBonus = Math.round(ctc * 0.10); // 10% of CTC
    
    return {
      basic,
      hra,
      specialAllowance,
      performanceBonus
    };
  }

  /**
   * Format currency
   */
  formatCurrency(amount: number, currency: string = 'INR'): string {
    if (currency === 'INR') {
      return `₹${amount.toLocaleString('en-IN')}`;
    }
    return `${currency} ${amount.toLocaleString()}`;
  }

  /**
   * Get status badge class
   */
  getStatusBadgeClass(status: string): string {
    const statusMap: { [key: string]: string } = {
      'GENERATED': 'badge-info',
      'SENT': 'badge-primary',
      'ACCEPTED': 'badge-success',
      'REJECTED': 'badge-danger',
      'EXPIRED': 'badge-warning',
      'WITHDRAWN': 'badge-secondary'
    };
    return statusMap[status] || 'badge-secondary';
  }
}
