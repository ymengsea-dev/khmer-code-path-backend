-- SCH/DEPT — faculty → department → class hierarchy (school-scoped).

CREATE TABLE IF NOT EXISTS faculties (
    id          BIGSERIAL PRIMARY KEY,
    school_id   BIGINT NOT NULL REFERENCES schools (id),
    name        VARCHAR(255) NOT NULL,
    status      VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    deleted     BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_faculties_school_name
    ON faculties (school_id, name) WHERE deleted = false;

CREATE INDEX IF NOT EXISTS idx_faculties_school
    ON faculties (school_id) WHERE deleted = false;

ALTER TABLE departments ADD COLUMN IF NOT EXISTS school_id BIGINT REFERENCES schools (id);
ALTER TABLE departments ADD COLUMN IF NOT EXISTS faculty_id BIGINT REFERENCES faculties (id);

DO $$
DECLARE
    target_school_id BIGINT;
    general_faculty_id BIGINT;
    has_faculty_column BOOLEAN;
BEGIN
    SELECT id INTO target_school_id
    FROM schools
    WHERE deleted = false AND slug = 'default'
    LIMIT 1;

    IF target_school_id IS NULL THEN
        SELECT id INTO target_school_id
        FROM schools
        WHERE deleted = false
        ORDER BY id
        LIMIT 1;
    END IF;

    IF target_school_id IS NULL THEN
        RAISE EXCEPTION 'V29: no active school found — create a school before migrating departments';
    END IF;

    SELECT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'departments'
          AND column_name = 'faculty'
    ) INTO has_faculty_column;

    IF has_faculty_column THEN
        INSERT INTO faculties (school_id, name, status, deleted, created_at, updated_at)
        SELECT target_school_id, fac.name, 'ACTIVE', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        FROM (
            SELECT DISTINCT COALESCE(NULLIF(TRIM(faculty), ''), 'General Faculty') AS name
            FROM departments
        ) fac
        WHERE NOT EXISTS (
            SELECT 1 FROM faculties f
            WHERE f.school_id = target_school_id
              AND f.name = fac.name
              AND f.deleted = false
        );
    END IF;

    INSERT INTO faculties (school_id, name, status, deleted, created_at, updated_at)
    SELECT target_school_id, 'General Faculty', 'ACTIVE', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    WHERE NOT EXISTS (
        SELECT 1 FROM faculties f
        WHERE f.school_id = target_school_id
          AND f.name = 'General Faculty'
          AND f.deleted = false
    );

    SELECT id INTO general_faculty_id
    FROM faculties
    WHERE school_id = target_school_id
      AND name = 'General Faculty'
      AND deleted = false
    LIMIT 1;

    IF has_faculty_column THEN
        UPDATE departments d
        SET school_id = target_school_id,
            faculty_id = f.id
        FROM faculties f
        WHERE f.school_id = target_school_id
          AND f.name = COALESCE(NULLIF(TRIM(d.faculty), ''), 'General Faculty')
          AND (d.school_id IS NULL OR d.faculty_id IS NULL);
    END IF;

    UPDATE departments d
    SET school_id = target_school_id,
        faculty_id = COALESCE(d.faculty_id, general_faculty_id)
    WHERE d.school_id IS NULL OR d.faculty_id IS NULL;
END $$;

ALTER TABLE departments DROP COLUMN IF EXISTS faculty;

ALTER TABLE lms_classes ADD COLUMN IF NOT EXISTS department_id BIGINT REFERENCES departments (id);
ALTER TABLE users ADD COLUMN IF NOT EXISTS department_id BIGINT REFERENCES departments (id);

CREATE INDEX IF NOT EXISTS idx_departments_school
    ON departments (school_id) WHERE deleted = false;

CREATE INDEX IF NOT EXISTS idx_departments_faculty
    ON departments (faculty_id) WHERE deleted = false;

CREATE INDEX IF NOT EXISTS idx_lms_classes_department
    ON lms_classes (department_id) WHERE deleted = false;

CREATE INDEX IF NOT EXISTS idx_users_department
    ON users (department_id) WHERE deleted = false;

ALTER TABLE departments ALTER COLUMN school_id SET NOT NULL;
ALTER TABLE departments ALTER COLUMN faculty_id SET NOT NULL;
