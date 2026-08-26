import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  DashboardStats,
  PriorityOffer,
  Activity,
  DailyApplicationStats,
} from '../models/dashboard.model';

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/dashboard`;

  getStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/stats`);
  }

  getPriorityOffers(): Observable<PriorityOffer[]> {
    return this.http.get<PriorityOffer[]>(`${this.apiUrl}/priority-offers`);
  }

  getRecentActivities(): Observable<Activity[]> {
    return this.http.get<Activity[]>(`${this.apiUrl}/recent-activities`);
  }

  getApplicationsByDay(): Observable<DailyApplicationStats[]> {
    return this.http.get<DailyApplicationStats[]>(`${this.apiUrl}/applications-by-day`);
  }
}
