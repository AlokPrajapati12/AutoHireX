import { Injectable } from '@angular/core';
import { Candidate } from '../models/candidate.interface';

@Injectable({ providedIn: 'root' })
export class CandidateService {
  private candidates: Candidate[] = [];

  getCandidates(): Candidate[] {
    return this.candidates;
  }

  addCandidate(candidate: Candidate) {
    this.candidates.push(candidate);
  }
}
