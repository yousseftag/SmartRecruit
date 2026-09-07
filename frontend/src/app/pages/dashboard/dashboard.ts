import { Component, OnInit, OnDestroy, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
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
  LucideFileCheck,
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
    RouterLink,
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
    LucideFileCheck,
    BaseChartDirective,
  ],
  templateUrl: './dashboard.html',
})
export class Dashboard implements OnInit, OnDestroy {
  private dashboardService = inject(DashboardService);
  private cdr = inject(ChangeDetectorRef);
  private themeObserver?: MutationObserver;

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
          color: '#64748b',
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        grid: { color: '#f1f5f9' },
        border: { dash: [4, 4] },
        ticks: { precision: 0, color: '#64748b' },
      },
      x: {
        grid: { display: false },
        ticks: { color: '#64748b' },
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
    this.updateChartTheme();
    this.initThemeObserver();
    this.loadStats();
    this.loadTopOffers();
    this.loadRecentActivities();
    this.loadChartData();
  }

  ngOnDestroy() {
    this.themeObserver?.disconnect();
  }

  private initThemeObserver() {
    if (typeof MutationObserver !== 'undefined') {
      this.themeObserver = new MutationObserver(() => {
        this.updateChartTheme();
      });
      this.themeObserver.observe(document.documentElement, {
        attributes: true,
        attributeFilter: ['class'],
      });
    }
  }

  private updateChartTheme() {
    const isDark = document.documentElement.classList.contains('dark');
    const tickColor = isDark ? '#94a3b8' : '#64748b';
    const gridColor = isDark ? '#334155' : '#f1f5f9';

    if (this.barChartOptions?.scales) {
      if (this.barChartOptions.scales['y']) {
        this.barChartOptions.scales['y'].grid = { color: gridColor };
        this.barChartOptions.scales['y'].ticks = { precision: 0, color: tickColor };
      }
      if (this.barChartOptions.scales['x']) {
        this.barChartOptions.scales['x'].ticks = { color: tickColor };
      }
    }
    if (this.barChartOptions?.plugins?.legend?.labels) {
      this.barChartOptions.plugins.legend.labels.color = tickColor;
    }
    this.barChartOptions = { ...this.barChartOptions };
    this.cdr.detectChanges();
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
