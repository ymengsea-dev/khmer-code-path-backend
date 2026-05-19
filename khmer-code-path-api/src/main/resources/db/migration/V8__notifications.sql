-- In-app notifications (NOTIF module)

CREATE TABLE IF NOT EXISTS notifications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         VARCHAR(36) NOT NULL REFERENCES users (uuid) ON DELETE CASCADE,
    type            VARCHAR(64) NOT NULL,
    title           VARCHAR(500) NOT NULL,
    message         TEXT,
    class_id        BIGINT REFERENCES lms_classes (id) ON DELETE SET NULL,
    resource_type   VARCHAR(64),
    resource_id     BIGINT,
    read            BOOLEAN NOT NULL DEFAULT false,
    deleted         BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_created
    ON notifications (user_id, created_at DESC)
    WHERE deleted = false;

CREATE INDEX IF NOT EXISTS idx_notifications_user_unread
    ON notifications (user_id)
    WHERE deleted = false AND read = false;
