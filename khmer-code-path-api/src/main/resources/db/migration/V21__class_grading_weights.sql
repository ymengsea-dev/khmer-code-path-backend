-- Per-class score breakdown weights (defaults match lms.classes.grading-weights).

ALTER TABLE lms_classes
    ADD COLUMN IF NOT EXISTS weight_attendance INT NOT NULL DEFAULT 10,
    ADD COLUMN IF NOT EXISTS weight_assignment INT NOT NULL DEFAULT 10,
    ADD COLUMN IF NOT EXISTS weight_quiz INT NOT NULL DEFAULT 5,
    ADD COLUMN IF NOT EXISTS weight_midterm INT NOT NULL DEFAULT 25,
    ADD COLUMN IF NOT EXISTS weight_final_exam INT NOT NULL DEFAULT 50;
