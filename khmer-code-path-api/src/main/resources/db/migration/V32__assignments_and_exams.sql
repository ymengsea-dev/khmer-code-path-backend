-- Assignments & Exams module (separate from quizzes)

CREATE TABLE IF NOT EXISTS assignments (
    id           BIGSERIAL PRIMARY KEY,
    class_id     BIGINT NOT NULL REFERENCES lms_classes (id) ON DELETE CASCADE,
    title        VARCHAR(500) NOT NULL,
    description  TEXT,
    instructions TEXT,
    due_at       TIMESTAMP WITHOUT TIME ZONE,
    status       VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
    deleted      BOOLEAN NOT NULL DEFAULT false,
    created_at   TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_assignments_class ON assignments (class_id) WHERE deleted = false;

CREATE TABLE IF NOT EXISTS assignment_submissions (
    id               BIGSERIAL PRIMARY KEY,
    assignment_id    BIGINT NOT NULL REFERENCES assignments (id) ON DELETE CASCADE,
    student_user_id  VARCHAR(36) NOT NULL REFERENCES users (uuid) ON DELETE CASCADE,
    content          TEXT,
    status           VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
    submitted_at     TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    feedback         TEXT,
    grade            VARCHAR(64),
    CONSTRAINT uk_assignment_submission UNIQUE (assignment_id, student_user_id)
);

CREATE INDEX IF NOT EXISTS idx_assignment_submissions_assignment
    ON assignment_submissions (assignment_id);

CREATE TABLE IF NOT EXISTS exams (
    id                BIGSERIAL PRIMARY KEY,
    class_id          BIGINT NOT NULL REFERENCES lms_classes (id) ON DELETE CASCADE,
    title             VARCHAR(500) NOT NULL,
    description       TEXT,
    due_at            TIMESTAMP WITHOUT TIME ZONE,
    duration_minutes  INT,
    generated_content TEXT,
    question_count    INT NOT NULL DEFAULT 0,
    status            VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
    deleted           BOOLEAN NOT NULL DEFAULT false,
    created_at        TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_exams_class ON exams (class_id) WHERE deleted = false;

CREATE TABLE IF NOT EXISTS exam_questions (
    id            BIGSERIAL PRIMARY KEY,
    exam_id       BIGINT NOT NULL REFERENCES exams (id) ON DELETE CASCADE,
    order_index   INT    NOT NULL DEFAULT 0,
    question_text TEXT   NOT NULL,
    options_json  TEXT   NOT NULL,
    correct_index INT    NOT NULL,
    explanation   TEXT
);

CREATE INDEX IF NOT EXISTS idx_exam_questions_exam ON exam_questions (exam_id);

CREATE TABLE IF NOT EXISTS exam_submissions (
    id               BIGSERIAL PRIMARY KEY,
    exam_id          BIGINT NOT NULL REFERENCES exams (id) ON DELETE CASCADE,
    student_user_id  VARCHAR(36) NOT NULL REFERENCES users (uuid) ON DELETE CASCADE,
    status           VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
    score            INT,
    fail_reason      TEXT,
    answers_json     TEXT,
    submitted_at     TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_exam_submission UNIQUE (exam_id, student_user_id)
);

CREATE INDEX IF NOT EXISTS idx_exam_submissions_exam ON exam_submissions (exam_id);
