-- SCH — school portal branding (cover image, tagline).

ALTER TABLE schools ADD COLUMN IF NOT EXISTS cover_storage_key VARCHAR(512);
ALTER TABLE schools ADD COLUMN IF NOT EXISTS tagline VARCHAR(512);
