-- V4__add_extraction_status_stalled.sql
-- Updates the extraction_status CHECK constraint on cv_file table to include 'STALLED'

ALTER TABLE cv_file DROP CONSTRAINT IF EXISTS cv_file_extraction_status_check;
ALTER TABLE cv_file ADD CONSTRAINT cv_file_extraction_status_check 
    CHECK (extraction_status IN ('PENDING', 'SUCCESS', 'FAILED', 'STALLED'));
