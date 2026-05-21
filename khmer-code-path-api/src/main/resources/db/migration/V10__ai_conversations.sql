-- AI chat conversations and messages (persistent memory per section/thread)

CREATE TABLE IF NOT EXISTS ai_conversations (
    id              VARCHAR(36) PRIMARY KEY,
    user_id         VARCHAR(36) NOT NULL REFERENCES users (uuid) ON DELETE CASCADE,
    section_type    VARCHAR(32) NOT NULL DEFAULT 'GENERAL',
    section_ref     VARCHAR(128),
    title           VARCHAR(500) NOT NULL,
    deleted         BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ai_conversations_user
    ON ai_conversations (user_id, updated_at DESC)
    WHERE deleted = false;

CREATE INDEX IF NOT EXISTS idx_ai_conversations_section
    ON ai_conversations (user_id, section_type, section_ref)
    WHERE deleted = false;

CREATE TABLE IF NOT EXISTS ai_chat_messages (
    id                BIGSERIAL PRIMARY KEY,
    conversation_id   VARCHAR(36) NOT NULL REFERENCES ai_conversations (id) ON DELETE CASCADE,
    role              VARCHAR(16) NOT NULL,
    content           TEXT NOT NULL,
    created_at        TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ai_chat_messages_conversation
    ON ai_chat_messages (conversation_id, created_at ASC);
