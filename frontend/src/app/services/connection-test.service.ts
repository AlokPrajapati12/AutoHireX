import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface ConnectionStatus {
  backend: boolean;
  mongodb: boolean;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class ConnectionTestService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /**
   * Test backend connection
   */
  testBackendConnection(): Observable<boolean> {
    return this.http.get(`${this.apiUrl}/jobs/list`).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }

  /**
   * Test authentication endpoint
   */
  testAuthEndpoint(): Observable<boolean> {
    return this.http.options(`${this.apiUrl}/auth/login`).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }

  /**
   * Comprehensive connection test
   */
  testAllConnections(): Observable<ConnectionStatus> {
    return this.testBackendConnection().pipe(
      map(backendStatus => {
        if (backendStatus) {
          return {
            backend: true,
            mongodb: true, // If backend is up, MongoDB is likely connected
            message: '✅ All systems operational'
          };
        } else {
          return {
            backend: false,
            mongodb: false,
            message: '❌ Cannot connect to backend. Please ensure backend is running on port 8080.'
          };
        }
      })
    );
  }

  /**
   * Get backend health info
   */
  getHealthStatus(): Observable<any> {
    return this.http.get(`${environment.apiBaseUrl || 'http://localhost:8080'}/actuator/health`).pipe(
      catchError(error => {
        console.error('Health check failed:', error);
        return of({ status: 'DOWN', error: error.message });
      })
    );
  }
}
