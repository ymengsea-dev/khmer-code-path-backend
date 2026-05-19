-- Dashboard metrics + class discussion comments

CREATE TABLE IF NOT EXISTS departments (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    faculty     VARCHAR(255),
    deleted     BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS class_comments (
    id              BIGSERIAL PRIMARY KEY,
    class_id        BIGINT NOT NULL REFERENCES lms_classes (id) ON DELETE CASCADE,
    author_user_id  VARCHAR(36) NOT NULL REFERENCES users (uuid),
    body            TEXT NOT NULL,
    deleted         BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_class_comments_class ON class_comments (class_id) WHERE deleted = false;

CREATE TABLE IF NOT EXISTS quizzes (
    id          BIGSERIAL PRIMARY KEY,
    class_id    BIGINT NOT NULL REFERENCES lms_classes (id) ON DELETE CASCADE,
    title       VARCHAR(500) NOT NULL,
    status      VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
    deleted     BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_quizzes_class ON quizzes (class_id) WHERE deleted = false;

CREATE TABLE IF NOT EXISTS quiz_submissions (
    id                BIGSERIAL PRIMARY KEY,
    quiz_id           BIGINT NOT NULL REFERENCES quizzes (id) ON DELETE CASCADE,
    student_user_id   VARCHAR(36) NOT NULL REFERENCES users (uuid) ON DELETE CASCADE,
    status            VARCHAR(32) NOT NULL DEFAULT 'COMPLETED',
    submitted_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_quiz_submission UNIQUE (quiz_id, student_user_id)
);

CREATE TABLE IF NOT EXISTS attendance_records (
    id                BIGSERIAL PRIMARY KEY,
    class_id          BIGINT NOT NULL REFERENCES lms_classes (id) ON DELETE CASCADE,
    student_user_id   VARCHAR(36) NOT NULL REFERENCES users (uuid) ON DELETE CASCADE,
    session_date      DATE NOT NULL,
    status            VARCHAR(16) NOT NULL DEFAULT 'PRESENT',
    CONSTRAINT uk_attendance UNIQUE (class_id, student_user_id, session_date)
);

CREATE TABLE IF NOT EXISTS student_grades (
    id                BIGSERIAL PRIMARY KEY,
    class_id          BIGINT NOT NULL REFERENCES lms_classes (id) ON DELETE CASCADE,
    student_user_id   VARCHAR(36) NOT NULL REFERENCES users (uuid) ON DELETE CASCADE,
    numeric_grade     DECIMAL(5, 2),
    letter_grade      VARCHAR(4),
    created_at        TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE class_enrollments
    ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP WITHOUT TIME ZONE;

INSERT INTO departments (name, faculty, deleted)
SELECT 'Computer Science', 'Faculty of Engineering', false
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Computer Science' AND deleted = false);

INSERT INTO departments (name, faculty, deleted)
SELECT 'Mathematics', 'Faculty of Science', false
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = 'Mathematics' AND deleted = false);
