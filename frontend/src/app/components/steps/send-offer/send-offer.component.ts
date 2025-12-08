import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OfferLetterService } from '../../../services/offer-letter.service';
import { InterviewService } from '../../../services/interview.service';
import { 
  OfferLetterRequest, 
  OfferLetterResponse,
  EligibleCandidate 
} from '../../../models/offer-letter.model';

@Component({
  selector: 'app-send-offer',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './send-offer.component.html',
  styleUrls: ['./send-offer.component.scss']
})
export class SendOfferComponent implements OnInit {
  
  // Lists
  eligibleCandidates: EligibleCandidate[] = [];
  offerLetters: OfferLetterResponse[] = [];
  
  // Selected Candidate
  selectedCandidate: EligibleCandidate | null = null;
  
  // Offer Letter Form
  offerLetterForm: OfferLetterRequest = {
    candidateId: '',
    applicationId: '',
    jobId: '',
    interviewId: '',
    joiningDate: '',
    employmentType: 'FULL_TIME',
    workLocation: 'ONSITE',
    officeLocation: '',
    annualCtc: 0,
    basicSalary: 0,
    hra: 0,
    specialAllowance: 0,
    performanceBonus: 0,
    benefits: 'Health Insurance, Provident Fund, Gratuity',
    paidLeaves: 24,
    additionalBenefits: '',
    probationPeriod: 3,
    noticePeriod: 30,
    reportingManager: '',
    reportingManagerDesignation: '',
    additionalNotes: '',
    generatedBy: 'HR Admin',
    approvedBy: '',
    expiryDate: ''
  };
  
  // UI States
  loading = false;
  showGenerateModal = false;
  showViewModal = false;
  showSuccessMessage = false;
  successMessage = '';
  errorMessage = '';
  activeTab: 'eligible' | 'generated' = 'eligible';
  
  // Selected Offer Letter for viewing
  selectedOfferLetter: OfferLetterResponse | null = null;
  
  // Filter
  filterStatus = 'ALL';
  
  constructor(
    private offerLetterService: OfferLetterService,
    private interviewService: InterviewService
  ) { }
  
  ngOnInit(): void {
    this.loadEligibleCandidates();
    this.loadGeneratedOfferLetters();
  }
  
  /**
   * Load candidates eligible for offer letter (cleared HR round)
   */
  loadEligibleCandidates(): void {
    this.loading = true;
    this.offerLetterService.getEligibleCandidates().subscribe({
      next: (candidates) => {
        this.eligibleCandidates = candidates;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading eligible candidates:', error);
        this.errorMessage = 'Failed to load eligible candidates';
        this.loading = false;
      }
    });
  }
  
  /**
   * Load all generated offer letters
   */
  loadGeneratedOfferLetters(): void {
    this.loading = true;
    this.offerLetterService.getAllOfferLetters().subscribe({
      next: (offers) => {
        this.offerLetters = offers;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading offer letters:', error);
        this.loading = false;
      }
    });
  }
  
  /**
   * Open modal to generate offer letter for selected candidate
   */
  openGenerateModal(candidate: EligibleCandidate): void {
    this.selectedCandidate = candidate;
    this.showGenerateModal = true;
    
    // Pre-fill form with candidate data
    this.offerLetterForm.candidateId = candidate.id;
    this.offerLetterForm.applicationId = candidate.applicationId;
    this.offerLetterForm.jobId = candidate.jobId;
    
    // Get HR round interview ID
    this.getHRRoundInterviewId(candidate.id);
    
    // Set default joining date (30 days from now)
    const joiningDate = new Date();
    joiningDate.setDate(joiningDate.getDate() + 30);
    this.offerLetterForm.joiningDate = joiningDate.toISOString().split('T')[0];
    
    // Set expiry date (15 days from now)
    const expiryDate = new Date();
    expiryDate.setDate(expiryDate.getDate() + 15);
    this.offerLetterForm.expiryDate = expiryDate.toISOString().split('T')[0];
  }
  
  /**
   * Get HR round interview ID for candidate
   */
  getHRRoundInterviewId(candidateId: string): void {
    this.interviewService.getInterviewsByCandidate(candidateId).subscribe({
      next: (interviews) => {
        const hrInterview = interviews.find(i => i.interviewRound === 'HR_ROUND');
        if (hrInterview) {
          this.offerLetterForm.interviewId = hrInterview.id || '';
        }
      },
      error: (error) => {
        console.error('Error fetching interviews:', error);
      }
    });
  }
  
  /**
   * Calculate salary breakdown when CTC changes
   */
  onCtcChange(): void {
    if (this.offerLetterForm.annualCtc > 0) {
      const breakdown = this.offerLetterService.calculateSalaryBreakdown(
        this.offerLetterForm.annualCtc
      );
      this.offerLetterForm.basicSalary = breakdown.basic;
      this.offerLetterForm.hra = breakdown.hra;
      this.offerLetterForm.specialAllowance = breakdown.specialAllowance;
      this.offerLetterForm.performanceBonus = breakdown.performanceBonus;
    }
  }
  
  /**
   * Generate offer letter
   */
  generateOfferLetter(): void {
    if (!this.validateForm()) {
      this.errorMessage = 'Please fill all required fields';
      return;
    }
    
    this.loading = true;
    this.errorMessage = '';
    
    this.offerLetterService.generateOfferLetter(this.offerLetterForm).subscribe({
      next: (response) => {
        this.successMessage = 'Offer letter generated successfully!';
        this.showSuccessMessage = true;
        this.showGenerateModal = false;
        this.loading = false;
        
        // Reload data
        this.loadEligibleCandidates();
        this.loadGeneratedOfferLetters();
        
        // Auto-hide success message
        setTimeout(() => {
          this.showSuccessMessage = false;
        }, 3000);
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Failed to generate offer letter';
        this.loading = false;
      }
    });
  }
  
  /**
   * Validate form
   */
  validateForm(): boolean {
    return !!(
      this.offerLetterForm.candidateId &&
      this.offerLetterForm.applicationId &&
      this.offerLetterForm.jobId &&
      this.offerLetterForm.interviewId &&
      this.offerLetterForm.joiningDate &&
      this.offerLetterForm.annualCtc > 0
    );
  }
  
  /**
   * View offer letter details
   */
  viewOfferLetter(offer: OfferLetterResponse): void {
    this.selectedOfferLetter = offer;
    this.showViewModal = true;
  }
  
  /**
   * Send offer letter to candidate
   */
  sendOfferLetter(offerId: string): void {
    if (confirm('Are you sure you want to send this offer letter to the candidate?')) {
      this.loading = true;
      this.offerLetterService.sendOfferLetter(offerId).subscribe({
        next: (response) => {
          this.successMessage = 'Offer letter sent successfully!';
          this.showSuccessMessage = true;
          this.loadGeneratedOfferLetters();
          this.loading = false;
          
          setTimeout(() => {
            this.showSuccessMessage = false;
          }, 3000);
        },
        error: (error) => {
          this.errorMessage = 'Failed to send offer letter';
          this.loading = false;
        }
      });
    }
  }
  
  /**
   * Download offer letter
   */
  downloadOfferLetter(offerId: string): void {
    this.offerLetterService.downloadOfferLetter(offerId).subscribe({
      next: (response) => {
        // TODO: Implement actual PDF download
        alert('Offer letter download feature coming soon!');
        this.loadGeneratedOfferLetters();
      },
      error: (error) => {
        this.errorMessage = 'Failed to download offer letter';
      }
    });
  }
  
  /**
   * Get filtered offer letters based on status
   */
  getFilteredOfferLetters(): OfferLetterResponse[] {
    if (this.filterStatus === 'ALL') {
      return this.offerLetters;
    }
    return this.offerLetters.filter(ol => ol.status === this.filterStatus);
  }
  
  /**
   * Get status badge class
   */
  getStatusBadgeClass(status: string): string {
    return this.offerLetterService.getStatusBadgeClass(status);
  }
  
  /**
   * Format currency
   */
  formatCurrency(amount: number, currency: string = 'INR'): string {
    return this.offerLetterService.formatCurrency(amount, currency);
  }
  
  /**
   * Close modals
   */
  closeModal(): void {
    this.showGenerateModal = false;
    this.showViewModal = false;
    this.selectedCandidate = null;
    this.selectedOfferLetter = null;
    this.errorMessage = '';
  }
  
  /**
   * Switch tabs
   */
  switchTab(tab: 'eligible' | 'generated'): void {
    this.activeTab = tab;
  }
}
