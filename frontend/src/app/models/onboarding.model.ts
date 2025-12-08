export interface OnboardingDocument {
  documentType: string;
  documentName: string;
  documentUrl: string | null;
  isRequired: boolean;
  isSubmitted: boolean;
  isVerified: boolean;
  verifiedBy: string | null;
  remarks: string | null;
  fileSize: number | null;
  fileType: string | null;
}

export interface Onboarding {
  id: string;
  
  // References
  candidateId: string;
  offerLetterId: string;
  applicationId: string;
  jobId: string;
  
  // Employee Information
  employeeId: string;
  candidateName: string;
  candidateEmail: string;
  candidatePhone: string;
  personalEmail: string;
  
  // Job Details
  jobTitle: string;
  department: string;
  designation: string;
  reportingManager: string;
  workLocation: string;
  
  // Onboarding Dates
  joiningDate: string;
  actualJoiningDate: string | null;
  onboardingStartDate: string;
  onboardingCompletionDate: string | null;
  
  // Onboarding Status
  status: string;
  currentStep: string;
  completionPercentage: number;
  
  // Required Documents
  documents: OnboardingDocument[];
  
  // System Setup
  emailAccountCreated: boolean;
  systemAccessProvided: boolean;
  idCardIssued: boolean;
  workstationAssigned: boolean;
  workstationNumber: string | null;
  
  // Orientation Details
  orientationCompleted: boolean;
  orientationDate: string | null;
  orientationConductedBy: string | null;
  orientationRemarks: string | null;
  
  // HR Details
  onboardingCoordinator: string;
  hrRemarks: string | null;
  approvedBy: string | null;
  
  // Background Verification
  backgroundVerificationRequired: boolean;
  backgroundVerificationStatus: string;
  backgroundVerificationDate: string | null;
  backgroundVerificationRemarks: string | null;
  
  // Probation Details
  probationPeriod: number;
  probationEndDate: string | null;
  
  // Additional Information
  emergencyContactName: string;
  emergencyContactPhone: string;
  emergencyContactRelation: string;
  bloodGroup: string | null;
  additionalNotes: string | null;
}

export interface OnboardingCreateRequest {
  candidateId: string;
  offerLetterId: string;
  applicationId: string;
  jobId: string;
  candidateName: string;
  candidateEmail: string;
  candidatePhone: string;
  personalEmail: string;
  jobTitle: string;
  department: string;
  designation: string;
  reportingManager: string;
  workLocation: string;
  joiningDate: string;
  actualJoiningDate: string | null;
  emergencyContactName: string;
  emergencyContactPhone: string;
  emergencyContactRelation: string;
  bloodGroup: string | null;
  onboardingCoordinator: string;
  probationPeriod: number;
  additionalNotes: string | null;
}

export interface DocumentUploadRequest {
  onboardingId: string;
  documentType: string;
  documentName: string;
  documentUrl: string;
  fileType: string;
  fileSize: number;
  remarks: string | null;
}

export interface OnboardingUpdateRequest {
  status?: string;
  currentStep?: string;
  emailAccountCreated?: boolean;
  systemAccessProvided?: boolean;
  idCardIssued?: boolean;
  workstationAssigned?: boolean;
  workstationNumber?: string;
  orientationCompleted?: boolean;
  orientationDate?: string;
  orientationConductedBy?: string;
  orientationRemarks?: string;
  backgroundVerificationStatus?: string;
  backgroundVerificationDate?: string;
  backgroundVerificationRemarks?: string;
  actualJoiningDate?: string;
  hrRemarks?: string;
  approvedBy?: string;
  additionalNotes?: string;
}

export interface OfferLetter {
  id: string;
  candidateId: string;
  candidateName: string;
  candidateEmail: string;
  candidatePhone: string;
  jobId: string;
  jobTitle: string;
  department: string;
  offerLetterNumber: string;
  joiningDate: string;
  annualCtc: number;
  status: string;
  isAccepted: boolean;
}
