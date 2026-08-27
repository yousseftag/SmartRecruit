import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { shareReplay } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  DashboardStats,
  PriorityOffer,
  Activity,
  DailyApplicationStats,
} from '../models/dashboard.model';

interface CacheItem<T> {
  observable$: Observable<T>;
  timestamp: number;
}

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/dashboard`;

  private readonly TTL = 5 * 60 * 1000; // 5 minutes

  private statsCache?: CacheItem<DashboardStats>;
  private topOffersCache?: CacheItem<PriorityOffer[]>;
  private activitiesCache?: CacheItem<Activity[]>;
  private applicationStatsCache?: CacheItem<DailyApplicationStats[]>;

  private isCacheValid<T>(cache?: CacheItem<T>): boolean {
    return !!cache && Date.now() - cache.timestamp < this.TTL;
  }

  getStats(): Observable<DashboardStats> {
    if (!this.isCacheValid(this.statsCache)) {
      this.statsCache = {
        observable$: this.http.get<DashboardStats>(`${this.apiUrl}/stats`).pipe(shareReplay(1)),
        timestamp: Date.now(),
      };
    }
    return this.statsCache!.observable$;
  }

  getPriorityOffers(): Observable<PriorityOffer[]> {
    if (!this.isCacheValid(this.topOffersCache)) {
      this.topOffersCache = {
        observable$: this.http
          .get<PriorityOffer[]>(`${this.apiUrl}/priority-offers`)
          .pipe(shareReplay(1)),
        timestamp: Date.now(),
      };
    }
    return this.topOffersCache!.observable$;
  }

  getRecentActivities(): Observable<Activity[]> {
    if (!this.isCacheValid(this.activitiesCache)) {
      this.activitiesCache = {
        observable$: this.http
          .get<Activity[]>(`${this.apiUrl}/recent-activities`)
          .pipe(shareReplay(1)),
        timestamp: Date.now(),
      };
    }
    return this.activitiesCache!.observable$;
  }

  getApplicationsByDay(): Observable<DailyApplicationStats[]> {
    if (!this.isCacheValid(this.applicationStatsCache)) {
      this.applicationStatsCache = {
        observable$: this.http
          .get<DailyApplicationStats[]>(`${this.apiUrl}/applications-by-day`)
          .pipe(shareReplay(1)),
        timestamp: Date.now(),
      };
    }
    return this.applicationStatsCache!.observable$;
  }

  clearCache(): void {
    this.statsCache = undefined;
    this.topOffersCache = undefined;
    this.activitiesCache = undefined;
    this.applicationStatsCache = undefined;
  }
}
