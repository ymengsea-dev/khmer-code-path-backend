-- QUIZ-0600 · Extend quizzes table and add quiz_questions + quiz_submissions columns

-- Extend quizzes with AI-generated content fields
ALTER TABLE quizzes
    ADD COLUMN IF NOT EXISTS description       TEXT,
    ADD COLUMN IF NOT EXISTS generated_content TEXT,
    ADD COLUMN IF NOT EXISTS question_count    INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS duration_minutes  INT,
    ADD COLUMN IF NOT EXISTS due_at            TIMESTAMP WITHOUT TIME ZONE;

-- Normalised question rows (parsed from generated_content at publish time)
CREATE TABLE IF NOT EXISTS quiz_questions (
    id            BIGSERIAL PRIMARY KEY,
    quiz_id       BIGINT NOT NULL REFERENCES quizzes (id) ON DELETE CASCADE,
    order_index   INT    NOT NULL DEFAULT 0,
    question_text TEXT   NOT NULL,
    options_json  TEXT   NOT NULL, -- JSON array of 4 option strings
    correct_index INT    NOT NULL,
    explanation   TEXT
);

CREATE INDEX IF NOT EXISTS idx_quiz_questions_quiz ON quiz_questions (quiz_id);

-- Extend quiz_submissions with score / fail tracking
ALTER TABLE quiz_submissions
    ADD COLUMN IF NOT EXISTS score        INT,
    ADD COLUMN IF NOT EXISTS fail_reason  TEXT,
    ADD COLUMN IF NOT EXISTS answers_json TEXT; -- JSON {questionId: selectedOptionIndex}

-- Rename old 'COMPLETED' status to 'SUBMITTED' to align with frontend expectations
UPDATE quiz_submissions
SET status = 'SUBMITTED'
WHERE status = 'COMPLETED';
