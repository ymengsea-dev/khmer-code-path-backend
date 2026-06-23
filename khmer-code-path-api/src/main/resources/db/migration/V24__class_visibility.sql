-- CLS Phase 3 — public / private class visibility for school-scoped self-enroll.

ALTER TABLE lms_classes ADD COLUMN IF NOT EXISTS visibility VARCHAR(32) NOT NULL DEFAULT 'PRIVATE';

UPDATE lms_classes SET visibility = 'PRIVATE' WHERE visibility IS NULL;

CREATE INDEX IF NOT EXISTS idx_lms_classes_public_school
    ON lms_classes (school_id, visibility, status)
    WHERE deleted = false;
