-- SCH — multi-school foundation (Phase 1): schools, user/class school scoping, default school seed.

CREATE TABLE IF NOT EXISTS schools (
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    slug              VARCHAR(128) NOT NULL,
    status            VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    registration_open BOOLEAN NOT NULL DEFAULT true,
    deleted           BOOLEAN NOT NULL DEFAULT false,
    created_at        TIMESTAMP WITHOUT TIME ZONE,
    updated_at        TIMESTAMP WITHOUT TIME ZONE
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_schools_slug_active
    ON schools (slug) WHERE deleted = false;

INSERT INTO schools (name, slug, status, registration_open, deleted, created_at, updated_at)
SELECT 'Default School', 'default', 'ACTIVE', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM schools WHERE slug = 'default' AND deleted = false);

ALTER TABLE users ADD COLUMN IF NOT EXISTS school_id BIGINT REFERENCES schools (id);

UPDATE users u
SET school_id = s.id
FROM schools s
WHERE u.school_id IS NULL
  AND s.slug = 'default'
  AND s.deleted = false;

ALTER TABLE lms_classes ADD COLUMN IF NOT EXISTS school_id BIGINT REFERENCES schools (id);

UPDATE lms_classes c
SET school_id = u.school_id
FROM users u
WHERE c.teacher_user_id = u.uuid
  AND c.school_id IS NULL
  AND u.school_id IS NOT NULL;

UPDATE lms_classes c
SET school_id = s.id
FROM schools s
WHERE c.school_id IS NULL
  AND s.slug = 'default'
  AND s.deleted = false;

CREATE INDEX IF NOT EXISTS idx_users_school ON users (school_id) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_lms_classes_school ON lms_classes (school_id) WHERE deleted = false;
