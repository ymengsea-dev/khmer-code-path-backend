-- SCH Phase 4 — school-scoped permission overrides for teachers.

CREATE TABLE IF NOT EXISTS user_permission_overrides (
    id          BIGSERIAL PRIMARY KEY,
    school_id   BIGINT NOT NULL REFERENCES schools (id) ON DELETE CASCADE,
    user_uuid   VARCHAR(36) NOT NULL REFERENCES users (uuid) ON DELETE CASCADE,
    authority   VARCHAR(64) NOT NULL,
    granted     BOOLEAN NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_permission_overrides
    ON user_permission_overrides (school_id, user_uuid, authority);

CREATE INDEX IF NOT EXISTS idx_user_permission_overrides_user
    ON user_permission_overrides (user_uuid, school_id);
