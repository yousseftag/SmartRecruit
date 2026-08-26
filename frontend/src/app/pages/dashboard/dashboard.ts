import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  LucideBriefcase,
  LucideInbox,
  LucideSparkles,
  LucideTrophy,
  LucideClock,
  LucideChevronRight,
  LucideCirclePlus,
  LucideUser,
  LucideUsers,
  LucidePencil,
} from '@lucide/angular';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';
import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardStats, PriorityOffer, Activity } from '../../core/models/dashboard.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    LucideBriefcase,
    LucideInbox,
    LucideSparkles,
    LucideTrophy,
    LucideClock,
    LucideChevronRight,
    LucideCirclePlus,
    LucideUser,
    LucideUsers,
    LucidePencil,
    BaseChartDirective,
  ],
  templateUrl: './dashboard.html',
})
export class Dashboard implements OnInit {
  private dashboardService = inject(DashboardService);
  private cdr = inject(ChangeDetectorRef);

  kpis: DashboardStats | null = null;
  topOffers: PriorityOffer[] = [];
  recentActivities: Activity[] = [];

  isLoadingKpis = true;
  isLoadingOffers = true;
  isLoadingActivities = true;
  isLoadingChart = true;

  public barChartLegend = true;
  public barChartPlugins = [];
  public barChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
        labels: {
          usePointStyle: true,
          boxWidth: 8,
          font: { family: "'Inter', sans-serif" },
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        grid: { color: '#f1f5f9' },
        border: { dash: [4, 4] },
        ticks: { precision: 0 },
      },
      x: {
        grid: { display: false },
      },
    },
  };

  public barChartData: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Candidatures reçues',
        backgroundColor: '#3b82f6',
        borderRadius: 4,
      },
    ],
  };

  ngOnInit() {
    this.loadStats();
    this.loadTopOffers();
    this.loadRecentActivities();
    this.loadChartData();
  }

  private loadStats() {
    this.dashboardService.getStats().subscribe({
      next: (data) => {
        this.kpis = data;
        this.isLoadingKpis = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoadingKpis = false;
        this.cdr.detectChanges();
      },
    });
  }

  private loadTopOffers() {
    this.dashboardService.getPriorityOffers().subscribe({
      next: (data) => {
        this.topOffers = data;
        this.isLoadingOffers = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoadingOffers = false;
        this.cdr.detectChanges();
      },
    });
  }

  private loadRecentActivities() {
    this.dashboardService.getRecentActivities().subscribe({
      next: (data) => {
        this.recentActivities = data;
        this.isLoadingActivities = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoadingActivities = false;
        this.cdr.detectChanges();
      },
    });
  }

  private loadChartData() {
    this.dashboardService.getApplicationsByDay().subscribe({
      next: (data) => {
        this.barChartData = {
          labels: data.map((d) => {
            const parts = d.date.split('-');
            if (parts.length === 3) return `${parts[2]}/${parts[1]}`;
            return d.date;
          }),
          datasets: [
            {
              data: data.map((d) => d.count),
              label: 'Candidatures reçues',
              backgroundColor: '#3b82f6',
              borderRadius: 4,
            },
          ],
        };
        this.isLoadingChart = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoadingChart = false;
        this.cdr.detectChanges();
      },
    });
  }
}
