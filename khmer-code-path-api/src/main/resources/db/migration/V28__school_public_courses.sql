-- SCH — school-level toggle for public course self-enrollment.

ALTER TABLE schools ADD COLUMN IF NOT EXISTS public_courses_enabled BOOLEAN NOT NULL DEFAULT false;

UPDATE schools SET public_courses_enabled = false WHERE public_courses_enabled IS NULL;
