-- CLS — classes, enrollments, minimal lessons table for delete rules (LSN will extend later).

CREATE TABLE IF NOT EXISTS lms_classes (
    id                BIGSERIAL PRIMARY KEY,
    code              VARCHAR(128) NOT NULL,
    name              VARCHAR(500) NOT NULL,
    description       TEXT,
    teacher_user_id   VARCHAR(36) NOT NULL REFERENCES users (uuid),
    semester          VARCHAR(128),
    academic_year     INT,
    schedule          VARCHAR(255),
    room_number       VARCHAR(128),
    status            VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    deleted           BOOLEAN NOT NULL DEFAULT false,
    created_at        TIMESTAMP WITHOUT TIME ZONE,
    updated_at        TIMESTAMP WITHOUT TIME ZONE
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_lms_classes_code_active
    ON lms_classes (code) WHERE deleted = false;

CREATE INDEX IF NOT EXISTS idx_lms_classes_teacher ON lms_classes (teacher_user_id) WHERE deleted = false;

CREATE TABLE IF NOT EXISTS class_enrollments (
    id                BIGSERIAL PRIMARY KEY,
    class_id          BIGINT NOT NULL REFERENCES lms_classes (id) ON DELETE CASCADE,
    student_user_id   VARCHAR(36) NOT NULL REFERENCES users (uuid) ON DELETE CASCADE,
    enrolled_at       TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_class_enrollment UNIQUE (class_id, student_user_id)
);

CREATE INDEX IF NOT EXISTS idx_class_enrollments_class ON class_enrollments (class_id);

CREATE TABLE IF NOT EXISTS lessons (
    id       BIGSERIAL PRIMARY KEY,
    class_id BIGINT NOT NULL REFERENCES lms_classes (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_lessons_class ON lessons (class_id);
