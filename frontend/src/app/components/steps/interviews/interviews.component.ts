import { Component, ViewChild, ElementRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { InterviewService, ScheduleInterviewRequest, Interview, InterviewFeedbackRequest } from '../../../services/interview.service';
import { ShortlistService } from '../../../services/shortlist.service';

@Component({
  selector: 'app-interviews',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './interviews.component.html',
  styleUrls: ['./interviews.component.scss']
})
export class InterviewsComponent implements OnInit {

  @ViewChild('audioPlayer') audioPlayer!: ElementRef<HTMLAudioElement>;

  // -------------------------
  // Voice Interview Variables
  // -------------------------
  private ws!: WebSocket;
  private mediaRecorder!: MediaRecorder;

  isRecording = false;
  isListening = false;
  isSpeaking = false;

  transcript: { sender: 'user' | 'ai', text: string }[] = [];
  lastAIText = "";

  // -------------------------
  // NEW: Interview Management Variables
  // -------------------------
  jobId: string = '';
  shortlistedCandidates: any[] = [];
  scheduledInterviews: Interview[] = [];
  selectedCandidate: any = null;
  selectedInterview: Interview | null = null;
  
  // UI State
  showScheduleModal = false;
  showFeedbackModal = false;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  
  // Filter
  selectedInterviewStatus = '';
  filteredInterviews: Interview[] = [];
  
  // Schedule Form
  scheduleForm: ScheduleInterviewRequest = {
    shortlistedCandidateId: '',
    applicationId: '',
    jobId: '',
    interviewRound: 'ROUND_1',
    scheduledDate: '',
    scheduledTime: '',
    interviewMode: 'ONLINE',
    meetingLink: '',
    venue: '',
    interviewerNames: [],
    interviewerEmails: [],
    interviewPanel: 'Technical Panel',
    notes: '',
    sendNotification: true
  };
  
  // Feedback Form
  feedbackForm: InterviewFeedbackRequest = {
    interviewId: '',
    feedback: '',
    technicalScore: 0,
    communicationScore: 0,
    overallRating: 0,
    decision: 'SELECTED',
    interviewerRemarks: '',
    nextRound: ''
  };
  
  // Stats
  stats = {
    totalScheduled: 0,
    totalCompleted: 0,
    totalCancelled: 0
  };

  constructor(
    private route: ActivatedRoute,
    private interviewService: InterviewService,
    private shortlistService: ShortlistService
  ) {}

  ngOnInit() {
    // Get jobId from route params
    this.route.queryParams.subscribe(params => {
      this.jobId = params['jobId'] || '';
      if (this.jobId) {
        this.loadShortlistedCandidates();
        this.loadInterviews();
      }
    });
  }

  // -------------------------
  // LOAD DATA
  // -------------------------
  
  loadShortlistedCandidates() {
    this.isLoading = true;
    this.shortlistService.getShortlistedCandidates(this.jobId).subscribe({
      next: (response) => {
        if (response.success) {
          this.shortlistedCandidates = response.candidates || [];
          console.log('Loaded shortlisted candidates:', this.shortlistedCandidates.length);
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading candidates:', error);
        this.errorMessage = 'Failed to load candidates';
        this.isLoading = false;
      }
    });
  }
  
  loadInterviews() {
    this.isLoading = true;
    this.interviewService.getInterviewsByJob(this.jobId).subscribe({
      next: (response) => {
        if (response.success) {
          this.scheduledInterviews = response.interviews || [];
          this.filteredInterviews = [...this.scheduledInterviews];
          this.stats.totalScheduled = response.totalScheduled || 0;
          this.stats.totalCompleted = response.totalCompleted || 0;
          this.stats.totalCancelled = response.totalCancelled || 0;
          console.log('Loaded interviews:', this.scheduledInterviews.length);
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading interviews:', error);
        this.errorMessage = 'Failed to load interviews';
        this.isLoading = false;
      }
    });
  }

  // -------------------------
  // SCHEDULE INTERVIEW
  // -------------------------
  
  openScheduleModal(candidate: any) {
    this.selectedCandidate = candidate;
    this.scheduleForm = {
      shortlistedCandidateId: candidate.id,
      applicationId: candidate.applicationId,
      jobId: this.jobId,
      interviewRound: 'ROUND_1',
      scheduledDate: '',
      scheduledTime: '10:00 AM - 11:00 AM',
      interviewMode: 'ONLINE',
      meetingLink: '',
      venue: '',
      interviewerNames: [],
      interviewerEmails: [],
      interviewPanel: 'Technical Panel',
      notes: '',
      sendNotification: true
    };
    this.showScheduleModal = true;
  }
  
  scheduleInterview() {
    this.isLoading = true;
    this.errorMessage = '';
    
    // Convert date to ISO format
    const scheduledDate = new Date(this.scheduleForm.scheduledDate).toISOString();
    const request = { ...this.scheduleForm, scheduledDate };
    
    this.interviewService.scheduleInterview(request).subscribe({
      next: (response) => {
        if (response.success) {
          this.successMessage = 'Interview scheduled successfully!';
          this.showScheduleModal = false;
          this.loadInterviews();
          this.loadShortlistedCandidates();
          setTimeout(() => this.successMessage = '', 3000);
        } else {
          this.errorMessage = response.message;
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error scheduling interview:', error);
        this.errorMessage = 'Failed to schedule interview';
        this.isLoading = false;
      }
    });
  }

  // -------------------------
  // FEEDBACK & EVALUATION
  // -------------------------
  
  openFeedbackModal(interview: Interview) {
    this.selectedInterview = interview;
    this.feedbackForm = {
      interviewId: interview.id,
      feedback: '',
      technicalScore: 0,
      communicationScore: 0,
      overallRating: 0,
      decision: 'SELECTED',
      interviewerRemarks: '',
      nextRound: interview.roundNumber === 1 ? 'ROUND_2' : 'HR_ROUND'
    };
    this.showFeedbackModal = true;
  }
  
  submitFeedback() {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.interviewService.submitFeedback(this.feedbackForm).subscribe({
      next: (response) => {
        if (response.success) {
          this.successMessage = 'Feedback submitted successfully!';
          this.showFeedbackModal = false;
          this.loadInterviews();
          setTimeout(() => this.successMessage = '', 3000);
        } else {
          this.errorMessage = response.message;
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error submitting feedback:', error);
        this.errorMessage = 'Failed to submit feedback';
        this.isLoading = false;
      }
    });
  }

  // -------------------------
  // FILTERS & STATUS
  // -------------------------
  
  filterByStatus() {
    if (!this.selectedInterviewStatus) {
      this.filteredInterviews = [...this.scheduledInterviews];
      return;
    }
    this.filteredInterviews = this.scheduledInterviews.filter(i =>
      i.status === this.selectedInterviewStatus
    );
  }
  
  getInterviewStatusClass(status: string) {
    const statusMap: any = {
      'SCHEDULED': 'scheduled',
      'RESCHEDULED': 'scheduled',
      'COMPLETED': 'completed',
      'CANCELLED': 'cancelled',
      'NO_SHOW': 'no-show'
    };
    return statusMap[status] || 'scheduled';
  }
  
  getRoundBadgeClass(round: string) {
    const roundMap: any = {
      'ROUND_1': 'round-1',
      'ROUND_2': 'round-2',
      'HR_ROUND': 'hr-round'
    };
    return roundMap[round] || 'round-1';
  }

  // -------------------------
  // MODAL CONTROLS
  // -------------------------
  
  closeScheduleModal() {
    this.showScheduleModal = false;
    this.selectedCandidate = null;
  }
  
  closeFeedbackModal() {
    this.showFeedbackModal = false;
    this.selectedInterview = null;
  }

  // -------------------------
  // VOICE INTERVIEW (EXISTING)
  // -------------------------
  
  startVoiceInterview() {
    this.ws = new WebSocket("ws://localhost:8000/ws/interview");
    this.ws.binaryType = "arraybuffer";

    this.ws.onopen = () => {
      console.log("Connected to Voice Agent");
      this.startRecording();
    };

    this.ws.onmessage = (event) => {
      this.isSpeaking = true;

      const aiAudio = new Blob([event.data], { type: "audio/wav" });
      const url = URL.createObjectURL(aiAudio);

      this.audioPlayer.nativeElement.src = url;
      this.audioPlayer.nativeElement.play();

      if (this.lastAIText.trim() !== "") {
        this.transcript.push({ sender: 'ai', text: this.lastAIText });
        this.lastAIText = "";
      }

      setTimeout(() => this.isSpeaking = false, 700);
    };
  }

  async startRecording() {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });

      this.mediaRecorder = new MediaRecorder(stream, {
        mimeType: "audio/webm"
      });

      this.mediaRecorder.onstart = () => {
        this.isRecording = true;
      };

      this.mediaRecorder.ondataavailable = (event) => {
        this.isListening = true;
        this.ws.send(event.data);
        setTimeout(() => this.isListening = false, 500);
      };

      this.mediaRecorder.start(300);

    } catch (error) {
      console.error("Microphone Error:", error);
    }
  }

  stopVoiceInterview() {
    this.mediaRecorder?.stop();
    this.ws?.close();
    this.isRecording = false;
  }

  addUserTranscript(text: string) {
    this.transcript.push({ sender: 'user', text });
  }

  // -------------------------
  // UTILITY METHODS
  // -------------------------
  
  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
  
  getRoundName(round: string): string {
    const roundNames: any = {
      'ROUND_1': 'Round 1',
      'ROUND_2': 'Round 2',
      'HR_ROUND': 'HR Round'
    };
    return roundNames[round] || round;
  }
}
