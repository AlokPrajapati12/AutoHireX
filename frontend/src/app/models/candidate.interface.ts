export interface Candidate {
id: number;
  name: string;
  title: string;
  score: number;
  status: 'Applied' | 'Under Review' | 'Shortlisted' | 'Rejected' | 'Interviewed';
  interviewScore: number;
  appliedDate?: string;
  stage: string;
  location?: string;
  experience?: string;
  avatar?: string;
  education?: string; 
}
