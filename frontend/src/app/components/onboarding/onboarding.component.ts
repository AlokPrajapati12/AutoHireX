import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OnboardingService } from '../../services/onboarding.service';
import {
  Onboarding,
  OnboardingCreateRequest,
  DocumentUploadRequest,
  OnboardingUpdateRequest,
  OfferLetter,
  OnboardingDocument
} from '../../models/onboarding.model';

@Component({
  selector: 'app-onboarding',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './onboarding.component.html',
  styleUrls: ['./onboarding.component.css']
})
export class OnboardingComponent implements OnInit {
  // Data
  onboardings: Onboarding[] = [];
  eligibleCandidates: OfferLetter[] = [];
  selectedOnboarding: Onboarding | null = null;
  
  // UI State
  activeTab: 'eligible' | 'onboarding' = 'eligible';
  showCreateModal = false;
  showDetailsModal = false;
  showDocumentModal = false;
  showUpdateModal = false;
  
  // Filters
  statusFilter = 'ALL';
  stepFilter = 'ALL';
  searchTerm = '';
  
  // Form Data
  createForm: OnboardingCreateRequest = this.getEmptyCreateForm();
  selectedCandidate: OfferLetter | null = null;
  
  // Document Upload
  selectedDocument: OnboardingDocument | null = null;
  uploadingDocument = false;
  selectedFile: File | null = null;
  
  // Update Form
  updateForm: OnboardingUpdateRequest = {};
  
  // Loading States
  loading = false;
  error = '';
  success = '';

  constructor(public onboardingService: OnboardingService) {}

  ngOnInit(): void {
    this.loadEligibleCandidates();
    this.loadOnboardings();
  }

  // ==================== DATA LOADING ====================
  
  loadEligibleCandidates(): void {
    this.loading = true;
    this.onboardingService.getEligibleCandidates().subscribe({
      next: (response) => {
        if (response.success) {
          this.eligibleCandidates = response.data;
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load eligible candidates';
        console.error(err);
        this.loading = false;
      }
    });
  }

  loadOnboardings(): void {
    this.loading = true;
    this.onboardingService.getAllOnboardings().subscribe({
      next: (response) => {
        if (response.success) {
          this.onboardings = response.data;
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load onboardings';
        console.error(err);
        this.loading = false;
      }
    });
  }

  // ==================== CREATE ONBOARDING ====================
  
  openCreateModal(candidate: OfferLetter): void {
    this.selectedCandidate = candidate;
    this.createForm = {
      candidateId: candidate.candidateId,
      offerLetterId: candidate.id,
      applicationId: '', // Will be filled by user or auto-populated
      jobId: candidate.jobId,
      candidateName: candidate.candidateName,
      candidateEmail: candidate.candidateEmail,
      candidatePhone: candidate.candidatePhone,
      personalEmail: '',
      jobTitle: candidate.jobTitle,
      department: candidate.department,
      designation: candidate.jobTitle,
      reportingManager: '',
      workLocation: '',
      joiningDate: candidate.joiningDate,
      actualJoiningDate: null,
      emergencyContactName: '',
      emergencyContactPhone: '',
      emergencyContactRelation: '',
      bloodGroup: null,
      onboardingCoordinator: 'HR Team',
      probationPeriod: 3,
      additionalNotes: null
    };
    this.showCreateModal = true;
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
    this.selectedCandidate = null;
    this.createForm = this.getEmptyCreateForm();
  }

  createOnboarding(): void {
    if (!this.validateCreateForm()) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';

    this.onboardingService.createOnboarding(this.createForm).subscribe({
      next: (response) => {
        if (response.success) {
          this.success = 'Onboarding created successfully!';
          this.loadOnboardings();
          this.loadEligibleCandidates();
          this.closeCreateModal();
          setTimeout(() => this.success = '', 3000);
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to create onboarding';
        console.error(err);
        this.loading = false;
      }
    });
  }

  validateCreateForm(): boolean {
    if (!this.createForm.candidateName || !this.createForm.candidateEmail) {
      this.error = 'Please fill all required fields';
      return false;
    }
    if (!this.createForm.emergencyContactName || !this.createForm.emergencyContactPhone) {
      this.error = 'Emergency contact details are required';
      return false;
    }
    return true;
  }

  // ==================== VIEW DETAILS ====================
  
  viewDetails(onboarding: Onboarding): void {
    this.selectedOnboarding = onboarding;
    this.showDetailsModal = true;
  }

  closeDetailsModal(): void {
    this.showDetailsModal = false;
    this.selectedOnboarding = null;
  }

  // ==================== DOCUMENT UPLOAD ====================
  
  openDocumentModal(onboarding: Onboarding, document: OnboardingDocument): void {
    this.selectedOnboarding = onboarding;
    this.selectedDocument = document;
    this.showDocumentModal = true;
  }

  closeDocumentModal(): void {
    this.showDocumentModal = false;
    this.selectedOnboarding = null;
    this.selectedDocument = null;
    this.selectedFile = null;
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      // Validate file size (5MB limit)
      if (file.size > 5 * 1024 * 1024) {
        this.error = 'File size must be less than 5MB';
        return;
      }
      this.selectedFile = file;
    }
  }

  async uploadDocument(): Promise<void> {
    if (!this.selectedFile || !this.selectedOnboarding || !this.selectedDocument) {
      this.error = 'Please select a file';
      return;
    }

    try {
      this.uploadingDocument = true;
      this.error = '';
      
      // Convert file to base64
      const base64 = await this.onboardingService.convertFileToBase64(this.selectedFile);

      const request: DocumentUploadRequest = {
        onboardingId: this.selectedOnboarding.id,
        documentType: this.selectedDocument.documentType,
        documentName: this.selectedFile.name,
        documentUrl: base64,
        fileType: this.selectedFile.type,
        fileSize: this.selectedFile.size,
        remarks: null
      };

      this.onboardingService.uploadDocument(request).subscribe({
        next: (response) => {
          if (response.success) {
            this.success = 'Document uploaded successfully!';
            this.loadOnboardings();
            this.closeDocumentModal();
            setTimeout(() => this.success = '', 3000);
          }
          this.uploadingDocument = false;
        },
        error: (err) => {
          this.error = err.error?.message || 'Failed to upload document';
          console.error(err);
          this.uploadingDocument = false;
        }
      });
    } catch (err) {
      this.error = 'Failed to process file';
      console.error(err);
      this.uploadingDocument = false;
    }
  }

  verifyDocument(onboarding: Onboarding, document: OnboardingDocument): void {
    if (!confirm(`Are you sure you want to verify ${document.documentName}?`)) {
      return;
    }

    this.loading = true;
    this.onboardingService.verifyDocument(
      onboarding.id,
      document.documentType,
      'HR Admin', // Replace with actual user
      'Verified'
    ).subscribe({
      next: (response) => {
        if (response.success) {
          this.success = 'Document verified successfully!';
          this.loadOnboardings();
          setTimeout(() => this.success = '', 3000);
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to verify document';
        console.error(err);
        this.loading = false;
      }
    });
  }

  // ==================== UPDATE ONBOARDING ====================
  
  openUpdateModal(onboarding: Onboarding): void {
    this.selectedOnboarding = onboarding;
    this.updateForm = {
      status: onboarding.status,
      currentStep: onboarding.currentStep,
      emailAccountCreated: onboarding.emailAccountCreated,
      systemAccessProvided: onboarding.systemAccessProvided,
      idCardIssued: onboarding.idCardIssued,
      workstationAssigned: onboarding.workstationAssigned,
      workstationNumber: onboarding.workstationNumber || '',
      orientationCompleted: onboarding.orientationCompleted,
      backgroundVerificationStatus: onboarding.backgroundVerificationStatus,
      hrRemarks: onboarding.hrRemarks || ''
    };
    this.showUpdateModal = true;
  }

  closeUpdateModal(): void {
    this.showUpdateModal = false;
    this.selectedOnboarding = null;
    this.updateForm = {};
  }

  updateOnboarding(): void {
    if (!this.selectedOnboarding) return;

    this.loading = true;
    this.error = '';
    
    this.onboardingService.updateOnboarding(this.selectedOnboarding.id, this.updateForm).subscribe({
      next: (response) => {
        if (response.success) {
          this.success = 'Onboarding updated successfully!';
          this.loadOnboardings();
          this.closeUpdateModal();
          setTimeout(() => this.success = '', 3000);
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to update onboarding';
        console.error(err);
        this.loading = false;
      }
    });
  }

  completeOnboarding(onboarding: Onboarding): void {
    if (!confirm(`Complete onboarding for ${onboarding.candidateName}?`)) {
      return;
    }

    this.loading = true;
    this.onboardingService.completeOnboarding(onboarding.id, 'HR Admin').subscribe({
      next: (response) => {
        if (response.success) {
          this.success = 'Onboarding completed successfully!';
          this.loadOnboardings();
          setTimeout(() => this.success = '', 3000);
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to complete onboarding';
        console.error(err);
        this.loading = false;
      }
    });
  }

  // ==================== FILTERING ====================
  
  get filteredOnboardings(): Onboarding[] {
    return this.onboardings.filter(onboarding => {
      const matchesStatus = this.statusFilter === 'ALL' || onboarding.status === this.statusFilter;
      const matchesStep = this.stepFilter === 'ALL' || onboarding.currentStep === this.stepFilter;
      const matchesSearch = !this.searchTerm || 
        onboarding.candidateName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        onboarding.employeeId.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        onboarding.jobTitle.toLowerCase().includes(this.searchTerm.toLowerCase());
      
      return matchesStatus && matchesStep && matchesSearch;
    });
  }

  get filteredEligibleCandidates(): OfferLetter[] {
    return this.eligibleCandidates.filter(candidate => {
      const matchesSearch = !this.searchTerm || 
        candidate.candidateName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        candidate.jobTitle.toLowerCase().includes(this.searchTerm.toLowerCase());
      return matchesSearch;
    });
  }

  // ==================== HELPERS ====================
  
  getEmptyCreateForm(): OnboardingCreateRequest {
    return {
      candidateId: '',
      offerLetterId: '',
      applicationId: '',
      jobId: '',
      candidateName: '',
      candidateEmail: '',
      candidatePhone: '',
      personalEmail: '',
      jobTitle: '',
      department: '',
      designation: '',
      reportingManager: '',
      workLocation: '',
      joiningDate: '',
      actualJoiningDate: null,
      emergencyContactName: '',
      emergencyContactPhone: '',
      emergencyContactRelation: '',
      bloodGroup: null,
      onboardingCoordinator: 'HR Team',
      probationPeriod: 3,
      additionalNotes: null
    };
  }

  getRequiredDocuments(onboarding: Onboarding): OnboardingDocument[] {
    return onboarding.documents.filter(doc => doc.isRequired);
  }

  getOptionalDocuments(onboarding: Onboarding): OnboardingDocument[] {
    return onboarding.documents.filter(doc => !doc.isRequired);
  }

  getRequiredDocumentsProgress(onboarding: Onboarding): { submitted: number; verified: number; total: number } {
    const required = this.getRequiredDocuments(onboarding);
    return {
      total: required.length,
      submitted: required.filter(doc => doc.isSubmitted).length,
      verified: required.filter(doc => doc.isVerified).length
    };
  }
}
