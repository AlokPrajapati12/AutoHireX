export interface Job {
  benefits: never[];
  id?: string;
  companyName: string;
  jobTitle: string;
  location: string;
  employmentType: string;
  minSalary?: number;
  maxSalary?: number;
  experienceLevel: string;
  keySkills?: string;

  /** ADD THIS **/
  description?: string;

  jobDescription: string;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
}


export interface JobRequest {
  title: string;
  description: string;
  company: string;
  location: string;
  employmentType: string;
  experienceLevel: string;
  requiredSkills: string;
  salaryRange?: string;
}

export interface JobApplication {
  id: string;
  jobId: string;
  candidateEmail: string;
  candidateName: string;
  description?: string;
  status: string;
  coverLetter?: string;
  resumeUrl?: string;
  appliedAt: string;
  updatedAt?: string;
}

export interface ApplicationRequest {
  jobId: string;
  coverLetter: string;
}
