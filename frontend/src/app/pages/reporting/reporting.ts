import { Component, OnInit, OnDestroy, inject, ChangeDetectorRef, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import {
  LucideDownload,
  LucideFileText,
  LucideSparkles,
  LucideUsers,
  LucideTrophy,
  LucideBriefcase,
  LucideChevronDown,
  LucideCheck,
  LucideCircleCheck,
  LucideClock,
  LucideChevronRight,
} from '@lucide/angular';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';
import { ReportingService } from '../../core/services/reporting.service';
import { OfferService } from '../../core/services/offer.service';
import { CampaignStats, CandidateReportRow } from '../../core/models/reporting.model';

export interface CandidatePreview {
  id: string;
  rank: number;
  name: string;
  email: string;
  phone: string;
  offerTitle: string;
  score: number;
  isAdmissible: boolean;
  status: string;
  statusLabel: string;
  appliedDate: string;
}

@Component({
  selector: 'app-reporting',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    BaseChartDirective,
    LucideDownload,
    LucideFileText,
    LucideSparkles,
    LucideUsers,
    LucideTrophy,
    LucideBriefcase,
    LucideChevronDown,
    LucideCheck,
    LucideCircleCheck,
    LucideClock,
    LucideChevronRight,
  ],
  templateUrl: './reporting.html',
})
export class Reporting implements OnInit, OnDestroy {
  private cdr = inject(ChangeDetectorRef);
  private reportingService = inject(ReportingService);
  private offerService = inject(OfferService);
  private themeObserver?: MutationObserver;

  // Filter State
  selectedOfferId = signal<string>('all');
  selectedPeriod = signal<string>('all');
  isOfferDropdownOpen = signal<boolean>(false);
  isPeriodDropdownOpen = signal<boolean>(false);

  // Loading & Error State
  isLoading = signal<boolean>(true);
  errorMessage = signal<string | null>(null);

  // Export & Feedback State
  isExportingExcel = signal<boolean>(false);
  isExportingPdf = signal<boolean>(false);
  toastMessage = signal<string | null>(null);
  toastTimeout: ReturnType<typeof setTimeout> | null = null;

  // Available Offers for Filter (populated dynamically from OfferService)
  offers: Array<{ id: string; title: string }> = [
    { id: 'all', title: 'Toutes les offres (Consolidé)' },
  ];

  // Available Periods for Filter
  readonly periods = [
    { id: 'all', label: "Tout l'historique" },
    { id: '30d', label: '30 derniers jours' },
    { id: '90d', label: '90 derniers jours' },
    { id: '1y', label: 'Cette année (2026)' },
  ];

  // Current Active Metrics
  currentKpis: CampaignStats = {
    totalApplications: 0,
    screenedApplications: 0,
    screenedRate: 0,
    averageScore: 0,
    maxScore: 0,
    qualifiedCount: 0,
    qualificationRate: 0,
    hiredCount: 0,
    conversionRate: 0,
    rejectedCount: 0,
  };

  // Funnel Chart (Horizontal Bar)
  public funnelChartLegend = false;
  public funnelChartPlugins = [];
  public funnelChartOptions: ChartConfiguration<'bar'>['options'] = {
    indexAxis: 'y',
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: (ctx) => {
            const val = ctx.parsed?.x ?? 0;
            const total = this.currentKpis?.totalApplications || 0;
            const pct = total > 0 ? ((val / total) * 100).toFixed(1) : '0.0';
            return ` ${val} candidats (${pct}%)`;
          },
        },
      },
    },
    scales: {
      x: {
        beginAtZero: true,
        grid: { color: '#f1f5f9' },
        ticks: { color: '#64748b', precision: 0 },
      },
      y: {
        grid: { display: false },
        ticks: { color: '#64748b', font: { family: "'Inter', sans-serif", weight: 'bold' } },
      },
    },
  };

  public funnelChartData: ChartConfiguration<'bar'>['data'] = {
    labels: ['Reçues', 'Admissibles IA', 'Présélectionnés', 'Entretiens', 'Recrutés'],
    datasets: [
      {
        data: [0, 0, 0, 0, 0],
        backgroundColor: ['#3b82f6', '#6366f1', '#8b5cf6', '#a855f7', '#10b981'],
        borderRadius: 6,
      },
    ],
  };

  // Score Distribution Chart (Doughnut)
  public scoreChartLegend = true;
  public scoreChartPlugins = [];
  public scoreChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '68%',
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          usePointStyle: true,
          boxWidth: 8,
          padding: 14,
          font: { family: "'Inter', sans-serif", size: 11 },
          color: '#64748b',
        },
      },
    },
  };

  public scoreChartData: ChartConfiguration<'doughnut'>['data'] = {
    labels: [
      'Excellents (>= 85%)',
      'Qualifiés (70 - 84%)',
      'Moyens (50 - 69%)',
      'Insuffisants (< 50%)',
    ],
    datasets: [
      {
        data: [0, 0, 0, 0],
        backgroundColor: ['#059669', '#2563eb', '#d97706', '#ef4444'],
        borderWidth: 2,
        borderColor: '#ffffff',
      },
    ],
  };

  // Ranked Candidates List
  candidateRankings: CandidatePreview[] = [];

  ngOnInit() {
    this.loadOffers();
    this.loadDashboardData();
    this.updateChartTheme();
    this.initThemeObserver();
  }

  ngOnDestroy() {
    this.themeObserver?.disconnect();
    if (this.toastTimeout) {
      clearTimeout(this.toastTimeout);
    }
  }

  private loadOffers() {
    this.offerService.getOfferTitles().subscribe({
      next: (titles) => {
        this.offers = [
          { id: 'all', title: 'Toutes les offres (Consolidé)' },
          ...titles.map((t) => ({ id: t.id, title: t.title })),
        ];
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load active offer titles', err);
      },
    });
  }

  loadDashboardData() {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    const offerId = this.selectedOfferId() !== 'all' ? this.selectedOfferId() : undefined;
    const period = this.selectedPeriod();

    this.reportingService.getDashboardStats(offerId, period).subscribe({
      next: (data) => {
        if (data.kpis) {
          this.currentKpis = data.kpis;
        }

        // Refresh Funnel Chart
        if (data.funnel && data.funnel.length > 0) {
          this.funnelChartData.labels = data.funnel.map((f) => f.stage);
          this.funnelChartData.datasets[0].data = data.funnel.map((f) => f.count);
        } else {
          this.funnelChartData.datasets[0].data = [0, 0, 0, 0, 0];
        }
        this.funnelChartData = { ...this.funnelChartData };

        // Refresh Score Distribution Chart
        if (data.scoreDistribution) {
          this.scoreChartData.datasets[0].data = [
            data.scoreDistribution.excellentCount,
            data.scoreDistribution.qualifiedCount,
            data.scoreDistribution.moderateCount,
            data.scoreDistribution.insufficientCount,
          ];
        } else {
          this.scoreChartData.datasets[0].data = [0, 0, 0, 0];
        }
        this.scoreChartData = { ...this.scoreChartData };

        // Map candidate rankings
        this.candidateRankings = (data.topCandidates || []).map((c) => this.mapToPreview(c));

        this.isLoading.set(false);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load reporting dashboard data', err);
        this.errorMessage.set('Erreur lors du chargement des statistiques de reporting.');
        this.isLoading.set(false);
        this.cdr.detectChanges();
      },
    });
  }

  private mapToPreview(row: CandidateReportRow): CandidatePreview {
    return {
      id: row.candidateId,
      rank: row.rank,
      name: row.fullName || '-',
      email: row.email || '-',
      phone: row.phone || '-',
      offerTitle: row.offerTitle || '-',
      score: row.totalScore != null ? Math.round(row.totalScore * 10) / 10 : 0,
      isAdmissible: row.isAdmissible,
      status: row.status,
      statusLabel: this.formatStatusLabel(row.status),
      appliedDate: this.formatAppliedDate(row.appliedAt),
    };
  }

  private formatStatusLabel(status: string): string {
    switch (status) {
      case 'NEW':
        return 'Nouveau';
      case 'SHORTLISTED':
        return 'Présélectionné';
      case 'INTERVIEWING':
        return 'En Entretien';
      case 'FOLLOW_UP':
        return 'Relance';
      case 'HIRED':
        return 'Recruté';
      case 'REJECTED':
        return 'Rejeté';
      case 'ARCHIVED':
        return 'Archivé';
      default:
        return status || '-';
    }
  }

  private formatAppliedDate(isoDateString?: string): string {
    if (!isoDateString) return '-';
    try {
      const d = new Date(isoDateString);
      const day = String(d.getDate()).padStart(2, '0');
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const year = d.getFullYear();
      return `${day}/${month}/${year}`;
    } catch {
      return isoDateString;
    }
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
    const doughnutBorder = isDark ? '#1e293b' : '#ffffff';

    // Funnel Chart
    if (this.funnelChartOptions?.scales) {
      if (this.funnelChartOptions.scales['x']) {
        this.funnelChartOptions.scales['x'].grid = { color: gridColor };
        this.funnelChartOptions.scales['x'].ticks = { color: tickColor, precision: 0 };
      }
      if (this.funnelChartOptions.scales['y']) {
        this.funnelChartOptions.scales['y'].ticks = {
          color: tickColor,
          font: { family: "'Inter', sans-serif", weight: 'bold' },
        };
      }
    }
    this.funnelChartOptions = { ...this.funnelChartOptions };

    // Doughnut Chart
    if (this.scoreChartOptions?.plugins?.legend?.labels) {
      this.scoreChartOptions.plugins.legend.labels.color = tickColor;
    }
    if (this.scoreChartData.datasets[0]) {
      this.scoreChartData.datasets[0].borderColor = doughnutBorder;
    }
    this.scoreChartOptions = { ...this.scoreChartOptions };
    this.scoreChartData = { ...this.scoreChartData };

    this.cdr.detectChanges();
  }

  // Filter Selection Handlers
  selectOffer(offerId: string) {
    this.selectedOfferId.set(offerId);
    this.isOfferDropdownOpen.set(false);
    this.loadDashboardData();
  }

  selectPeriod(periodId: string) {
    this.selectedPeriod.set(periodId);
    this.isPeriodDropdownOpen.set(false);
    this.loadDashboardData();
  }

  getSelectedOfferTitle(): string {
    const found = this.offers.find((o) => o.id === this.selectedOfferId());
    return found ? found.title : 'Toutes les offres';
  }

  getSelectedPeriodLabel(): string {
    const found = this.periods.find((p) => p.id === this.selectedPeriod());
    return found ? found.label : "Tout l'historique";
  }

  // Interactive Export Triggers (To be fully connected in Commit 3)
  triggerExportExcel() {
    if (this.isExportingExcel() || this.isExportingPdf()) return;
    this.isExportingExcel.set(true);

    setTimeout(() => {
      this.isExportingExcel.set(false);
      this.showToast(
        `Classement Excel exporté avec succès pour "${this.getSelectedOfferTitle()}" !`,
      );
    }, 1200);
  }

  triggerExportPdf() {
    if (this.isExportingExcel() || this.isExportingPdf()) return;
    this.isExportingPdf.set(true);

    setTimeout(() => {
      this.isExportingPdf.set(false);
      this.showToast(
        `Rapport de synthèse PDF généré avec succès pour "${this.getSelectedOfferTitle()}" !`,
      );
    }, 1400);
  }

  private showToast(msg: string) {
    this.toastMessage.set(msg);
    if (this.toastTimeout) clearTimeout(this.toastTimeout);
    this.toastTimeout = setTimeout(() => {
      this.toastMessage.set(null);
      this.cdr.detectChanges();
    }, 3500);
    this.cdr.detectChanges();
  }
}
