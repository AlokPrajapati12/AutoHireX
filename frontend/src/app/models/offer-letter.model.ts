export interface OfferLetter {
  id?: string;
  offerLetterNumber?: string;
  
  // References
  candidateId: string;
  applicationId: string;
  jobId: string;
  interviewId: string;
  
  // Candidate Information
  candidateName: string;
  candidateEmail: string;
  candidatePhone?: string;
  candidateAddress?: string;
  
  // Job Information
  jobTitle: string;
  department?: string;
  company: string;
  companyAddress?: string;
  
  // Offer Details
  offerDate?: Date;
  joiningDate: Date;
  employmentType: string; // FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP
  workLocation: string; // REMOTE, ONSITE, HYBRID
  officeLocation?: string;
  
  // Compensation Details
  annualCtc: number;
  basicSalary?: number;
  hra?: number; // House Rent Allowance
  specialAllowance?: number;
  performanceBonus?: number;
  otherAllowances?: number;
  currency?: string;
  
  // Benefits
  benefits?: string;
  paidLeaves?: number;
  additionalBenefits?: string;
  
  // Offer Terms
  probationPeriod?: number; // In months
  noticePeriod?: number; // In days
  reportingManager?: string;
  reportingManagerDesignation?: string;
  
  // Status Tracking
  status?: string;
  generatedAt?: Date;
  sentAt?: Date;
  respondedAt?: Date;
  expiryDate?: Date;
  
  // Acceptance Details
  isAccepted?: boolean;
  acceptanceMethod?: string;
  acceptanceRemarks?: string;
  rejectionReason?: string;
  
  // Document Details
  offerLetterPdfUrl?: string;
  isDownloaded?: boolean;
  downloadCount?: number;
  
  // HR Details
  generatedBy?: string;
  approvedBy?: string;
  hrRemarks?: string;
  
  // Additional Information
  additionalNotes?: string;
  updatedAt?: Date;
}

export interface OfferLetterRequest {
  candidateId: string;
  applicationId: string;
  jobId: string;
  interviewId: string;
  
  joiningDate: string; // Date in ISO format
  employmentType: string;
  workLocation: string;
  officeLocation?: string;
  
  annualCtc: number;
  basicSalary?: number;
  hra?: number;
  specialAllowance?: number;
  performanceBonus?: number;
  otherAllowances?: number;
  
  benefits?: string;
  paidLeaves?: number;
  additionalBenefits?: string;
  
  probationPeriod?: number;
  noticePeriod?: number;
  reportingManager?: string;
  reportingManagerDesignation?: string;
  
  additionalNotes?: string;
  generatedBy?: string;
  approvedBy?: string;
  expiryDate?: string;
}

export interface OfferLetterResponse {
  id: string;
  offerLetterNumber: string;
  candidateName: string;
  candidateEmail: string;
  jobTitle: string;
  company: string;
  offerDate: Date;
  joiningDate: Date;
  employmentType: string;
  workLocation: string;
  annualCtc: number;
  currency: string;
  status: string;
  generatedAt: Date;
  sentAt?: Date;
  isAccepted: boolean;
  offerLetterPdfUrl?: string;
  isDownloaded: boolean;
  message?: string;
}

export interface EligibleCandidate {
  id: string;
  candidateName: string;
  candidateEmail: string;
  candidatePhone: string;
  jobTitle: string;
  company: string;
  applicationId: string;
  jobId: string;
  finalScore: number;
  offerLetterGenerated?: boolean;
}
