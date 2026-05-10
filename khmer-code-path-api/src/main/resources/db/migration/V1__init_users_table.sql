-- Users table (aligned with JPA entity User). Single entry-point migration for Flyway "from scratch":
--  * New database: CREATE TABLE defines the full schema.
--  * Existing DB (e.g. created earlier by Hibernate): CREATE is skipped; ALTER adds any missing columns.
-- Safe to run once per environment; Flyway records version 1 after success.

CREATE TABLE IF NOT EXISTS users (
    uuid         varchar(36) PRIMARY KEY,
    username     varchar(255),
    email        varchar(255) NOT NULL,
    password     varchar(255),
    is_active    boolean NOT NULL DEFAULT true,
    role         varchar(32),
    provider     varchar(32),
    student_id   varchar(255),
    teacher_id   varchar(255),
    deleted      boolean NOT NULL DEFAULT false,
    created_at   timestamp without time zone,
    updated_at   timestamp without time zone
);

ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted boolean NOT NULL DEFAULT false;
ALTER TABLE users ADD COLUMN IF NOT EXISTS student_id varchar(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS teacher_id varchar(255);

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email ON users (email);
CREATE UNIQUE INDEX IF NOT EXISTS uk_users_student_id ON users (student_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_users_teacher_id ON users (teacher_id);
