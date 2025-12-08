import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-interview-schedule-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  template: `
    <div class="schedule-dialog">
      <h2 mat-dialog-title>
        <mat-icon>{{ data.interviewType === 'MANUAL' ? 'person' : 'smart_toy' }}</mat-icon>
        Schedule {{ data.interviewType === 'MANUAL' ? 'Manual' : 'Voice AI' }} Interview
      </h2>
      
      <mat-dialog-content>
        <div class="dialog-content">
          <!-- Candidates Summary -->
          <div class="candidates-summary">
            <p class="summary-text">
              <mat-icon>people</mat-icon>
              Scheduling interviews for <strong>{{ data.candidates.length }}</strong> shortlisted candidates
            </p>
          </div>

          <!-- Interview Type Info -->
          <div class="interview-info" [ngClass]="data.interviewType === 'VOICE_AI' ? 'ai-info' : 'manual-info'">
            <mat-icon>{{ data.interviewType === 'MANUAL' ? 'info' : 'psychology' }}</mat-icon>
            <div>
              <h4>{{ data.interviewType === 'MANUAL' ? 'Manual HR Interview' : 'Automated AI Interview' }}</h4>
              <p *ngIf="data.interviewType === 'MANUAL'">
                You will conduct interviews personally. Candidates will receive calendar invites with meeting links.
              </p>
              <p *ngIf="data.interviewType === 'VOICE_AI'">
                AI will conduct 3 automated rounds: Technical, Behavioral, and HR. Results will be available in real-time.
              </p>
            </div>
          </div>

          <!-- Date Picker -->
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>
              <mat-icon>calendar_today</mat-icon>
              Interview Date
            </mat-label>
            <input 
              matInput 
              [matDatepicker]="picker" 
              [(ngModel)]="selectedDate"
              [min]="minDate"
              placeholder="Select interview date"
              required>
            <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
            <mat-datepicker #picker></mat-datepicker>
          </mat-form-field>

          <!-- Time Input -->
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>
              <mat-icon>schedule</mat-icon>
              Interview Time
            </mat-label>
            <input 
              matInput 
              type="time" 
              [(ngModel)]="selectedTime"
              placeholder="Select interview time"
              required>
          </mat-form-field>

          <!-- Additional Notes -->
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>
              <mat-icon>note</mat-icon>
              Additional Notes (Optional)
            </mat-label>
            <textarea 
              matInput 
              [(ngModel)]="additionalNotes"
              rows="3"
              placeholder="Add any special instructions or notes for candidates...">
            </textarea>
          </mat-form-field>

          <!-- AI Interview Rounds Preview -->
          <div class="ai-rounds-preview" *ngIf="data.interviewType === 'VOICE_AI'">
            <h4>
              <mat-icon>view_list</mat-icon>
              Interview Rounds
            </h4>
            <div class="round-item">
              <mat-icon class="round-icon">code</mat-icon>
              <div>
                <strong>Round 1: Technical Assessment</strong>
                <p>AI evaluates technical skills and problem-solving abilities</p>
              </div>
            </div>
            <div class="round-item">
              <mat-icon class="round-icon">psychology</mat-icon>
              <div>
                <strong>Round 2: Behavioral Analysis</strong>
                <p>AI assesses communication, teamwork, and cultural fit</p>
              </div>
            </div>
            <div class="round-item">
              <mat-icon class="round-icon">business</mat-icon>
              <div>
                <strong>Round 3: HR Evaluation</strong>
                <p>AI reviews expectations, availability, and final alignment</p>
              </div>
            </div>
          </div>
        </div>
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button (click)="onCancel()">
          <mat-icon>close</mat-icon>
          Cancel
        </button>
        <button 
          mat-raised-button 
          [color]="data.interviewType === 'MANUAL' ? 'primary' : 'accent'"
          (click)="onSchedule()"
          [disabled]="!selectedDate || !selectedTime">
          <mat-icon>{{ data.interviewType === 'MANUAL' ? 'send' : 'rocket_launch' }}</mat-icon>
          {{ data.interviewType === 'MANUAL' ? 'Send Invites' : 'Start AI Interviews' }}
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .schedule-dialog {
      max-height: 90vh;
      overflow-y: auto;
    }

    h2[mat-dialog-title] {
      display: flex;
      align-items: center;
      gap: 12px;
      margin: 0 0 20px 0;
      padding-bottom: 16px;
      border-bottom: 2px solid #e0e0e0;
      font-size: 24px;
      color: #1976d2;

      mat-icon {
        font-size: 32px;
        width: 32px;
        height: 32px;
      }
    }

    mat-dialog-content {
      padding: 0 24px;
      margin-bottom: 20px;
    }

    .dialog-content {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .candidates-summary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 16px;
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

      .summary-text {
        margin: 0;
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 16px;

        mat-icon {
          font-size: 24px;
          width: 24px;
          height: 24px;
        }

        strong {
          font-size: 20px;
          font-weight: 700;
        }
      }
    }

    .interview-info {
      display: flex;
      gap: 12px;
      padding: 16px;
      border-radius: 8px;
      border-left: 4px solid #1976d2;
      
      &.manual-info {
        background: #e3f2fd;
        border-left-color: #1976d2;
      }

      &.ai-info {
        background: #f3e5f5;
        border-left-color: #9c27b0;
      }

      mat-icon {
        font-size: 32px;
        width: 32px;
        height: 32px;
        color: #1976d2;
      }

      &.ai-info mat-icon {
        color: #9c27b0;
      }

      h4 {
        margin: 0 0 8px 0;
        font-size: 16px;
        font-weight: 600;
      }

      p {
        margin: 0;
        font-size: 14px;
        color: #666;
      }
    }

    .full-width {
      width: 100%;
    }

    mat-form-field {
      mat-label {
        display: flex;
        align-items: center;
        gap: 6px;

        mat-icon {
          font-size: 18px;
          width: 18px;
          height: 18px;
        }
      }
    }

    .ai-rounds-preview {
      background: #f5f5f5;
      padding: 16px;
      border-radius: 8px;
      margin-top: 8px;

      h4 {
        display: flex;
        align-items: center;
        gap: 8px;
        margin: 0 0 16px 0;
        font-size: 16px;
        color: #333;

        mat-icon {
          color: #9c27b0;
        }
      }

      .round-item {
        display: flex;
        gap: 12px;
        padding: 12px;
        background: white;
        border-radius: 6px;
        margin-bottom: 8px;
        border-left: 3px solid #9c27b0;

        &:last-child {
          margin-bottom: 0;
        }

        .round-icon {
          font-size: 28px;
          width: 28px;
          height: 28px;
          color: #9c27b0;
          flex-shrink: 0;
        }

        strong {
          display: block;
          font-size: 14px;
          font-weight: 600;
          color: #333;
          margin-bottom: 4px;
        }

        p {
          margin: 0;
          font-size: 13px;
          color: #666;
        }
      }
    }

    mat-dialog-actions {
      padding: 16px 24px;
      margin: 0;
      border-top: 1px solid #e0e0e0;
      
      button {
        display: flex;
        align-items: center;
        gap: 6px;
        font-weight: 500;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }
    }
  `]
})
export class InterviewScheduleDialogComponent {
  selectedDate: Date | null = null;
  selectedTime: string = '';
  additionalNotes: string = '';
  minDate: Date = new Date();

  constructor(
    public dialogRef: MatDialogRef<InterviewScheduleDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

  onCancel(): void {
    this.dialogRef.close();
  }

  onSchedule(): void {
    if (!this.selectedDate || !this.selectedTime) {
      return;
    }

    this.dialogRef.close({
      date: this.selectedDate,
      time: this.selectedTime,
      notes: this.additionalNotes
    });
  }
}
