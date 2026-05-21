ALTER TABLE user_notes
    ADD COLUMN IF NOT EXISTS favorite BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS share_token VARCHAR(36),
    ADD COLUMN IF NOT EXISTS share_enabled BOOLEAN NOT NULL DEFAULT false;

CREATE UNIQUE INDEX IF NOT EXISTS idx_user_notes_share_token
    ON user_notes (share_token)
    WHERE share_token IS NOT NULL AND deleted = false;
