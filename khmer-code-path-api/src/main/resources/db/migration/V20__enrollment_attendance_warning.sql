ALTER TABLE class_enrollments
    ADD COLUMN IF NOT EXISTS attendance_warned BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS attendance_warned_at TIMESTAMP;
