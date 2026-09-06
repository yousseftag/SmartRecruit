import { WorkflowStatus } from './application.model';

export type ReportingPeriod = 'ALL' | 'LAST_30_DAYS' | 'LAST_90_DAYS' | 'THIS_YEAR';

/** Analytical KPI summary for a campaign or consolidated recruitment history. */
export interface CampaignStats {
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

/** Milestone stage within the recruitment conversion funnel. */
export interface FunnelStage {
  stage: string;
  count: number;
  percentage: number;
}

/** Distribution counts across the 4 AI qualification tiers. */
export interface ScoreDistribution {
  excellentCount: number;
  qualifiedCount: number;
  moderateCount: number;
  insufficientCount: number;
}

/** Candidate row representation within the ranked leaderboard and export table. */
export interface CandidateReportRow {
  rank: number;
  candidateId: string;
  fullName: string;
  email: string;
  phone: string;
  offerTitle: string | null;
  totalScore: number | null;
  isAdmissible: boolean;
  status: WorkflowStatus;
  appliedAt: string;
  categoryScores?: Record<string, number | string | Record<string, unknown>> | null;
}

/** Consolidated payload returned by GET /api/v1/reporting/stats. */
export interface ReportingDashboardResponse {
  kpis: CampaignStats;
  funnel: FunnelStage[];
  scoreDistribution: ScoreDistribution;
  topCandidates: CandidateReportRow[];
}

/** Filter options for reporting queries. */
export interface ReportingFilterParams {
  offerId?: string;
  period?: string;
}
