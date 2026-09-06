-- V6__add_offer_ai_status.sql
-- Adds AI processing lifecycle tracking to the offer table.
-- offer_ai_status: PENDING (dispatched/in-flight) | SUCCESS (criteria vectorized) | FAILED (AI error)

ALTER TABLE offer
    ADD COLUMN offer_ai_status VARCHAR(50) NOT NULL DEFAULT 'PENDING'
        CHECK (offer_ai_status IN ('PENDING', 'STALLED', 'SUCCESS', 'FAILED'));

-- Back-fill: existing ACTIVE and CLOSED offers from seed data are considered pre-processed
UPDATE offer
SET offer_ai_status = 'SUCCESS'
WHERE status IN ('ACTIVE', 'CLOSED');

-- Partial index for fast query of offers still awaiting AI pre-processing
CREATE INDEX idx_offer_ai_pending ON offer(id)
    WHERE offer_ai_status = 'PENDING';
