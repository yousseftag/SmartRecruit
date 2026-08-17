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
    BaseChartDirective,
  ],
  templateUrl: './dashboard.html',
})
export class Dashboard {
  // Mock KPIs
  kpis = {
    activeOffers: 12,
    newApplications: 45,
    topProfiles: 8,
    hiredCandidates: 3,
  };

  // Mock Top Offers (triées manuellement ici pour le mock: d'abord le max de 'new' puis de 'top')
  topOffers = [
    {
      id: 1,
      title: 'Développeur Java Senior',
      createdAt: new Date(new Date().setDate(new Date().getDate() - 2)),
      newCount: 12,
      topCount: 3,
    },
    {
      id: 2,
      title: 'Data Scientist',
      createdAt: new Date(new Date().setDate(new Date().getDate() - 5)),
      newCount: 8,
      topCount: 4,
    },
    {
      id: 3,
      title: 'Product Owner',
      createdAt: new Date(new Date().setDate(new Date().getDate() - 10)),
      newCount: 5,
      topCount: 1,
    },
    {
      id: 4,
      title: 'DevOps Engineer',
      createdAt: new Date(new Date().setDate(new Date().getDate() - 1)),
      newCount: 2,
      topCount: 0,
    },
  ];

  // Mock Recent Activities
  recentActivities = [
    {
      id: 1,
      type: 'CREATE_OFFER',
      description: 'a créé la nouvelle offre "Développeur React".',
      user: 'Ahmed',
      timeAgo: 'Il y a 2h',
    },
    {
      id: 2,
      type: 'STATUS_CHANGE',
      description: 'a déplacé Amine vers "Entretien".',
      user: 'Sarah',
      timeAgo: 'Il y a 4h',
    },
    {
      id: 3,
      type: 'STATUS_CHANGE',
      description: 'a présélectionné (Shortlisted) Amine.',
      user: 'Ahmed',
      timeAgo: 'Il y a 1j',
    },
    {
      id: 4,
      type: 'CREATE_OFFER',
      description: 'a publié l\'offre "Tech Lead".',
      user: 'Admin RH',
      timeAgo: 'Il y a 2j',
    },
  ];

  // Chart Config
  public barChartLegend = true;
  public barChartPlugins = [];

  public barChartData: ChartConfiguration<'bar'>['data'] = {
    labels: ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'],
    datasets: [
      {
        data: [12, 19, 15, 25, 22, 5, 2],
        label: 'Candidatures ',
        backgroundColor: '#3b82f6',
        borderRadius: 4,
      },
      {
        data: [3, 5, 2, 8, 6, 0, 0],
        label: 'Entretiens ',
        backgroundColor: '#10b981',
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
          font: {
            family: "'Inter', sans-serif",
          },
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        grid: {
          color: '#f1f5f9',
        },
        border: {
          dash: [4, 4],
        },
      },
      x: {
        grid: {
          display: false,
        },
      },
    },
  };
}
