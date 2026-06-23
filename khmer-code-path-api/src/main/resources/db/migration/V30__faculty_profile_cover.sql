-- FAC — per-faculty profile tagline and cover image.

ALTER TABLE faculties ADD COLUMN IF NOT EXISTS tagline VARCHAR(512);
ALTER TABLE faculties ADD COLUMN IF NOT EXISTS cover_storage_key VARCHAR(512);
