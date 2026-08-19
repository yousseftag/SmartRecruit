import { Component } from '@angular/core';
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
  LucideEdit,
} from '@lucide/angular';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';

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
    LucideEdit,
    BaseChartDirective,
  ],
  templateUrl: './dashboard.html',
})
export class Dashboard {
  // Mock KPIs (5 KPIs)
  kpis = {
    activeOffers: 12,
    newApplications: 45,

    // Performance IA
    aiValidationRate: 62,
    cvExtractionRate: 95,

    // Candidatures en Processus
    activeCandidates: 18,

    // Recrutements & Rejets
    hiredCandidates: 3,
    hiringSuccessRate: 21,
    rejectedCandidates: 11,
  };

  // Mock Top Offers — sorted by newCount DESC only
  topOffers = [
    {
      id: 1,
      title: 'Développeur Java Senior',
      createdAt: new Date(new Date().setDate(new Date().getDate() - 2)),
      newCount: 12,
      aiPassedCount: 7,
    },
    {
      id: 2,
      title: 'Data Scientist',
      createdAt: new Date(new Date().setDate(new Date().getDate() - 5)),
      newCount: 8,
      aiPassedCount: 5,
    },
    {
      id: 3,
      title: 'Product Owner',
      createdAt: new Date(new Date().setDate(new Date().getDate() - 10)),
      newCount: 5,
      aiPassedCount: 2,
    },
    {
      id: 4,
      title: 'DevOps Engineer',
      createdAt: new Date(new Date().setDate(new Date().getDate() - 1)),
      newCount: 2,
      aiPassedCount: 0,
    },
  ];

  // Mock Recent Activities — 10 entries with scrollable timeline
  recentActivities = [
    {
      id: 1,
      type: 'STATUS_CHANGE',
      user: 'Sarah',
      description: 'a déplacé Omar vers "Shortlisted".',
      timeAgo: 'Il y a 1h',
    },
    {
      id: 2,
      type: 'UPDATE_OFFER',
      user: 'Admin',
      description: 'a modifié l\'offre "Développeur Java Senior".',
      timeAgo: 'Il y a 1h30',
    },
    {
      id: 3,
      type: 'CREATE_OFFER',
      user: 'Ahmed',
      description: 'a créé l\'offre "Développeur React".',
      timeAgo: 'Il y a 2h',
    },
    {
      id: 4,
      type: 'STATUS_CHANGE',
      user: 'Admin',
      description: 'a rejeté le dossier de Karim.',
      timeAgo: 'Il y a 3h',
    },
    {
      id: 5,
      type: 'STATUS_CHANGE',
      user: 'Sarah',
      description: 'a déplacé Youssef vers "Entretien".',
      timeAgo: 'Il y a 4h',
    },
    {
      id: 6,
      type: 'STATUS_CHANGE',
      user: 'Ahmed',
      description: 'a présélectionné Amine.',
      timeAgo: 'Il y a 6h',
    },
    {
      id: 7,
      type: 'CREATE_OFFER',
      user: 'Admin',
      description: 'a publié l\'offre "Tech Lead".',
      timeAgo: 'Il y a 1j',
    },
    {
      id: 8,
      type: 'STATUS_CHANGE',
      user: 'Sarah',
      description: 'a embauché Fatima.',
      timeAgo: 'Il y a 1j',
    },
    {
      id: 9,
      type: 'STATUS_CHANGE',
      user: 'Ahmed',
      description: 'a mis Nour en "Follow-up".',
      timeAgo: 'Il y a 2j',
    },
    {
      id: 10,
      type: 'STATUS_CHANGE',
      user: 'Admin',
      description: 'a rejeté le dossier de Mehdi.',
      timeAgo: 'Il y a 3j',
    },
  ];

  // Generates the last 7 days as DD/MM labels (sliding window, not fixed week)
  private getLast7DaysLabels(): string[] {
    const labels: string[] = [];
    for (let i = 6; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      labels.push(
        `${d.getDate().toString().padStart(2, '0')}/${(d.getMonth() + 1).toString().padStart(2, '0')}`,
      );
    }
    return labels;
  }

  // Chart Config — single series: applications over the last 7 days
  public barChartLegend = true;
  public barChartPlugins = [];

  public barChartData: ChartConfiguration<'bar'>['data'] = {
    labels: this.getLast7DaysLabels(),
    datasets: [
      {
        data: [5, 12, 8, 15, 3, 0, 2],
        label: 'Candidatures reçues',
        backgroundColor: '#3b82f6',
        borderRadius: 4,
      },
    ],
  };

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
      },
      x: {
        grid: { display: false },
      },
    },
  };
}
