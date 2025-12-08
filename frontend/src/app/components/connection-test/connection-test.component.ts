import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AiService } from '../../services/ai.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-connection-test',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div style="padding: 2rem; font-family: Arial;">
      <h1>🔍 Connection Test</h1>
      
      <div style="margin: 2rem 0;">
        <h2>Environment</h2>
        <p>API URL: {{ apiUrl }}</p>
        <p>Environment: {{ production ? 'Production' : 'Development' }}</p>
      </div>

      <div style="margin: 2rem 0;">
        <h2>Tests</h2>
        
        <button (click)="testBackend()" style="margin: 0.5rem; padding: 0.5rem 1rem; cursor: pointer;">
          Test Backend Connection
        </button>
        
        <button (click)="testAIHealth()" style="margin: 0.5rem; padding: 0.5rem 1rem; cursor: pointer;">
          Test AI Service Health
        </button>
        
        <button (click)="testJobs()" style="margin: 0.5rem; padding: 0.5rem 1rem; cursor: pointer;">
          Test Jobs Endpoint
        </button>
      </div>

      <div style="margin: 2rem 0; padding: 1rem; background: #f5f5f5; border-radius: 8px;">
        <h2>Results</h2>
        <pre style="white-space: pre-wrap; word-wrap: break-word;">{{ results }}</pre>
      </div>
    </div>
  `
})
export class ConnectionTestComponent implements OnInit {
  apiUrl = environment.apiUrl;
  production = environment.production;
  results = 'Click a button to test...';

  constructor(
    private http: HttpClient,
    private aiService: AiService
  ) {}

  ngOnInit(): void {
    console.log('🔧 Connection Test Component Initialized');
    console.log('API URL:', this.apiUrl);
  }

  testBackend() {
    this.results = '⏳ Testing backend connection...';
    console.log('Testing backend:', `${this.apiUrl}/jobs/health`);
    
    this.http.get(`${this.apiUrl}/jobs/health`, { responseType: 'text' })
      .subscribe({
        next: (response) => {
          this.results = `✅ Backend Connected!\n\nResponse: ${response}`;
          console.log('✅ Backend test successful:', response);
        },
        error: (error) => {
          this.results = `❌ Backend Connection Failed!\n\nError: ${JSON.stringify(error, null, 2)}`;
          console.error('❌ Backend test failed:', error);
        }
      });
  }

  testAIHealth() {
    this.results = '⏳ Testing AI service health...';
    console.log('Testing AI health:', `${this.apiUrl}/ai/status`);
    
    this.aiService.checkAIHealth()
      .subscribe({
        next: (response) => {
          this.results = `✅ AI Service Status:\n\n${JSON.stringify(response, null, 2)}`;
          console.log('✅ AI health test successful:', response);
        },
        error: (error) => {
          this.results = `❌ AI Service Test Failed!\n\nError: ${JSON.stringify(error, null, 2)}`;
          console.error('❌ AI health test failed:', error);
        }
      });
  }

  testJobs() {
    this.results = '⏳ Testing jobs endpoint...';
    console.log('Testing jobs:', `${this.apiUrl}/jobs/public`);
    
    this.http.get(`${this.apiUrl}/jobs/public`)
      .subscribe({
        next: (response: any) => {
          this.results = `✅ Jobs Endpoint Working!\n\nFound ${response.length} jobs:\n\n${JSON.stringify(response, null, 2)}`;
          console.log('✅ Jobs test successful:', response);
        },
        error: (error) => {
          this.results = `❌ Jobs Endpoint Failed!\n\nError: ${JSON.stringify(error, null, 2)}`;
          console.error('❌ Jobs test failed:', error);
        }
      });
  }
}
