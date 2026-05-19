-- CLS — class invitations (teacher invites → student accepts → enrollment)

CREATE TABLE IF NOT EXISTS class_invitations (
    id                  BIGSERIAL PRIMARY KEY,
    class_id            BIGINT NOT NULL REFERENCES lms_classes (id) ON DELETE CASCADE,
    student_user_id     VARCHAR(36) NOT NULL REFERENCES users (uuid) ON DELETE CASCADE,
    invited_by_user_id  VARCHAR(36) NOT NULL REFERENCES users (uuid) ON DELETE CASCADE,
    status              VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    responded_at        TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_class_invitations_student_status
    ON class_invitations (student_user_id, status);

CREATE INDEX IF NOT EXISTS idx_class_invitations_class_status
    ON class_invitations (class_id, status);

CREATE UNIQUE INDEX IF NOT EXISTS uk_class_invitation_pending
    ON class_invitations (class_id, student_user_id)
    WHERE status = 'PENDING';
