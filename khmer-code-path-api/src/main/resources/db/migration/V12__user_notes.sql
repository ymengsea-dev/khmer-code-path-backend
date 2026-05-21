-- Digital notebook — per-user notes (students, teachers, admins)

CREATE TABLE IF NOT EXISTS user_notes (
    id              BIGSERIAL PRIMARY KEY,
    user_id         VARCHAR(36) NOT NULL REFERENCES users (uuid) ON DELETE CASCADE,
    title           VARCHAR(500) NOT NULL,
    body_html       TEXT NOT NULL DEFAULT '',
    preview         VARCHAR(500) NOT NULL DEFAULT '',
    source_label    VARCHAR(255),
    lesson_id       BIGINT REFERENCES lessons (id) ON DELETE SET NULL,
    material_id     BIGINT,
    tags            VARCHAR(500) NOT NULL DEFAULT '',
    deleted         BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_notes_user
    ON user_notes (user_id, updated_at DESC)
    WHERE deleted = false;
