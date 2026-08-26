export interface DashboardStats {
  activeOffers: number;
  newApplications: number;
  aiValidationRate: number;
  cvExtractionRate: number;
  activeCandidates: number;
  hiredCandidates: number;
  hiringSuccessRate: number;
  rejectedCandidates: number;
}

export interface PriorityOffer {
  id: string;
  title: string;
  createdAt: string;
  newCount: number;
  aiPassedCount: number;
}

export interface Activity {
  type: string;
  user: string;
  description: string;
  occurredAt: string;
}

export interface DailyApplicationStats {
  date: string;
  count: number;
}
