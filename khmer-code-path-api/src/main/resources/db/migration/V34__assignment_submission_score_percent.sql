-- Numeric score (0–100) for assignment submissions — used in weighted class grades.

ALTER TABLE assignment_submissions
    ADD COLUMN IF NOT EXISTS score_percent NUMERIC(5, 2);

UPDATE assignment_submissions
SET score_percent = 100
WHERE score_percent IS NULL
  AND status = 'SUBMITTED';
