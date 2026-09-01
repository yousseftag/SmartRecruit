-- Add passed_min_score for AI performance KPI
ALTER TABLE application
    ADD COLUMN passed_min_score BOOLEAN;

-- Add updated_by to track who modified the offer (Recent Activity)
ALTER TABLE offer
    ADD COLUMN updated_by UUID REFERENCES app_user(id) ON DELETE SET NULL;

-- Index to optimize Dashboard queries
CREATE INDEX idx_application_ai_passed ON application(passed_min_score) WHERE status = 'NEW';
