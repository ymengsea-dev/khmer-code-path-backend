-- SCH Phase 4b — school-wide permission overrides per role (all teachers / all students).

CREATE TABLE IF NOT EXISTS school_role_permissions (
    id          BIGSERIAL PRIMARY KEY,
    school_id   BIGINT NOT NULL REFERENCES schools (id) ON DELETE CASCADE,
    role        VARCHAR(16) NOT NULL,
    authority   VARCHAR(64) NOT NULL,
    granted     BOOLEAN NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_school_role_permissions
    ON school_role_permissions (school_id, role, authority);

CREATE INDEX IF NOT EXISTS idx_school_role_permissions_school_role
    ON school_role_permissions (school_id, role);
