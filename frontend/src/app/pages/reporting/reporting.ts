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

interface CampaignKpiData {
  totalApplications: number;
  screenedApplications: number;
  screenedRate: number;
  averageScore: number;
  maxScore: number;
  qualifiedCount: number;
  qualificationRate: number;
  hiredCount: number;
  conversionRate: number;
  rejectedCount: number;
}

interface CandidatePreview {
  id: string;
  rank: number;
  name: string;
  email: string;
  phone: string;
  offerTitle: string;
  score: number;
  isAdmissible: boolean;
  status: 'NEW' | 'SHORTLISTED' | 'INTERVIEWING' | 'HIRED' | 'REJECTED';
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
  private themeObserver?: MutationObserver;

  // Filter State
  selectedOfferId = signal<string>('all');
  selectedPeriod = signal<string>('all');
  isOfferDropdownOpen = signal<boolean>(false);
  isPeriodDropdownOpen = signal<boolean>(false);

  // Export & Feedback State
  isExportingExcel = signal<boolean>(false);
  isExportingPdf = signal<boolean>(false);
  toastMessage = signal<string | null>(null);
  toastTimeout: any = null;

  // Available Offers for Filter
  readonly offers = [
    { id: 'all', title: 'Toutes les offres (Consolidé)' },
    { id: '1', title: 'Développeur Fullstack Java / Angular' },
    { id: '2', title: 'Data Engineer & MLOps' },
    { id: '3', title: 'Tech Lead DevOps Cloud' },
  ];

  // Available Periods for Filter
  readonly periods = [
    { id: 'all', label: "Tout l'historique" },
    { id: '30d', label: '30 derniers jours' },
    { id: '90d', label: '90 derniers jours' },
    { id: '1y', label: 'Cette année (2026)' },
  ];

  // Current Active Metrics
  currentKpis: CampaignKpiData = {
    totalApplications: 48,
    screenedApplications: 45,
    screenedRate: 93.8,
    averageScore: 74.2,
    maxScore: 94.0,
    qualifiedCount: 18,
    qualificationRate: 37.5,
    hiredCount: 3,
    conversionRate: 6.3,
    rejectedCount: 12,
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
            const total = this.currentKpis?.totalApplications || 48;
            return ` ${val} candidats (${((val / total) * 100).toFixed(1)}%)`;
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
        data: [48, 18, 10, 6, 3],
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
        data: [6, 12, 17, 10],
        backgroundColor: ['#059669', '#2563eb', '#d97706', '#ef4444'],
        borderWidth: 2,
        borderColor: '#ffffff',
      },
    ],
  };

  // Ranked Candidates List
  candidateRankings: CandidatePreview[] = [];

  ngOnInit() {
    this.applyMockData(this.selectedOfferId(), this.selectedPeriod());
    this.updateChartTheme();
    this.initThemeObserver();
  }

  ngOnDestroy() {
    this.themeObserver?.disconnect();
    if (this.toastTimeout) {
      clearTimeout(this.toastTimeout);
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

    // Funnel
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

    // Doughnut
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
    this.applyMockData(offerId, this.selectedPeriod());
  }

  selectPeriod(periodId: string) {
    this.selectedPeriod.set(periodId);
    this.isPeriodDropdownOpen.set(false);
    this.applyMockData(this.selectedOfferId(), periodId);
  }

  getSelectedOfferTitle(): string {
    const found = this.offers.find((o) => o.id === this.selectedOfferId());
    return found ? found.title : 'Toutes les offres';
  }

  getSelectedPeriodLabel(): string {
    const found = this.periods.find((p) => p.id === this.selectedPeriod());
    return found ? found.label : "Tout l'historique";
  }

  // Dynamic Mock Data Swapping based on Filter
  private applyMockData(offerId: string, _periodId: string) {
    if (offerId === '1') {
      // Développeur Fullstack
      this.currentKpis = {
        totalApplications: 24,
        screenedApplications: 24,
        screenedRate: 100,
        averageScore: 76.5,
        maxScore: 94.0,
        qualifiedCount: 10,
        qualificationRate: 41.7,
        hiredCount: 2,
        conversionRate: 8.3,
        rejectedCount: 6,
      };
      this.funnelChartData.datasets[0].data = [24, 10, 6, 4, 2];
      this.scoreChartData.datasets[0].data = [4, 6, 9, 5];
      this.candidateRankings = this.getAllMockCandidates().filter((c) =>
        c.offerTitle.includes('Fullstack'),
      );
    } else if (offerId === '2') {
      // Data Engineer
      this.currentKpis = {
        totalApplications: 14,
        screenedApplications: 13,
        screenedRate: 92.9,
        averageScore: 72.1,
        maxScore: 89.0,
        qualifiedCount: 5,
        qualificationRate: 35.7,
        hiredCount: 1,
        conversionRate: 7.1,
        rejectedCount: 4,
      };
      this.funnelChartData.datasets[0].data = [14, 5, 3, 2, 1];
      this.scoreChartData.datasets[0].data = [1, 4, 6, 3];
      this.candidateRankings = this.getAllMockCandidates().filter((c) =>
        c.offerTitle.includes('Data'),
      );
    } else if (offerId === '3') {
      // Tech Lead DevOps
      this.currentKpis = {
        totalApplications: 10,
        screenedApplications: 8,
        screenedRate: 80.0,
        averageScore: 71.0,
        maxScore: 86.0,
        qualifiedCount: 3,
        qualificationRate: 30.0,
        hiredCount: 0,
        conversionRate: 0.0,
        rejectedCount: 2,
      };
      this.funnelChartData.datasets[0].data = [10, 3, 1, 0, 0];
      this.scoreChartData.datasets[0].data = [1, 2, 5, 2];
      this.candidateRankings = this.getAllMockCandidates().filter((c) =>
        c.offerTitle.includes('DevOps'),
      );
    } else {
      // Consolidated
      this.currentKpis = {
        totalApplications: 48,
        screenedApplications: 45,
        screenedRate: 93.8,
        averageScore: 74.2,
        maxScore: 94.0,
        qualifiedCount: 18,
        qualificationRate: 37.5,
        hiredCount: 3,
        conversionRate: 6.3,
        rejectedCount: 12,
      };
      this.funnelChartData.datasets[0].data = [48, 18, 10, 6, 3];
      this.scoreChartData.datasets[0].data = [6, 12, 17, 10];
      this.candidateRankings = this.getAllMockCandidates();
    }

    // Refresh charts
    this.funnelChartData = { ...this.funnelChartData };
    this.scoreChartData = { ...this.scoreChartData };
    this.cdr.detectChanges();
  }

  private getAllMockCandidates(): CandidatePreview[] {
    return [
      {
        id: 'c1',
        rank: 1,
        name: 'Sarah Mansouri',
        email: 'sarah.mansouri@email.com',
        phone: '+212 6 12 34 56 78',
        offerTitle: 'Développeur Fullstack Java / Angular',
        score: 94,
        isAdmissible: true,
        status: 'INTERVIEWING',
        statusLabel: 'En Entretien',
        appliedDate: '28/08/2026',
      },
      {
        id: 'c2',
        rank: 2,
        name: 'Karim Benjelloun',
        email: 'k.benjelloun@email.com',
        phone: '+212 6 98 76 54 32',
        offerTitle: 'Développeur Fullstack Java / Angular',
        score: 89,
        isAdmissible: true,
        status: 'HIRED',
        statusLabel: 'Recruté',
        appliedDate: '26/08/2026',
      },
      {
        id: 'c3',
        rank: 3,
        name: 'Youssef El Amrani',
        email: 'y.amrani@email.com',
        phone: '+212 6 55 44 33 22',
        offerTitle: 'Data Engineer & MLOps',
        score: 86,
        isAdmissible: true,
        status: 'SHORTLISTED',
        statusLabel: 'Présélectionné',
        appliedDate: '29/08/2026',
      },
      {
        id: 'c4',
        rank: 4,
        name: 'Imane Tazi',
        email: 'imane.tazi@email.com',
        phone: '+212 6 11 22 33 44',
        offerTitle: 'Tech Lead DevOps Cloud',
        score: 82,
        isAdmissible: true,
        status: 'INTERVIEWING',
        statusLabel: 'En Entretien',
        appliedDate: '25/08/2026',
      },
      {
        id: 'c5',
        rank: 5,
        name: 'Amine Chraibi',
        email: 'amine.c@email.com',
        phone: '+212 6 77 88 99 00',
        offerTitle: 'Développeur Fullstack Java / Angular',
        score: 78,
        isAdmissible: true,
        status: 'SHORTLISTED',
        statusLabel: 'Présélectionné',
        appliedDate: '30/08/2026',
      },
      {
        id: 'c6',
        rank: 6,
        name: 'Mehdi Bennani',
        email: 'mehdi.b@email.com',
        phone: '+212 6 33 22 11 00',
        offerTitle: 'Data Engineer & MLOps',
        score: 64,
        isAdmissible: false,
        status: 'REJECTED',
        statusLabel: 'Rejeté',
        appliedDate: '27/08/2026',
      },
    ];
  }

  // Interactive Export Triggers (Simulated workflow)
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
