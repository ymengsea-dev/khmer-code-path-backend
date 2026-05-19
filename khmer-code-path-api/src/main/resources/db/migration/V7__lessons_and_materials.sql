-- LSN — lessons, lesson materials, teacher material library

ALTER TABLE lessons
    ADD COLUMN IF NOT EXISTS title VARCHAR(500),
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS summary TEXT,
    ADD COLUMN IF NOT EXISTS module_tag VARCHAR(128),
    ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS library_item_id BIGINT,
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP;

UPDATE lessons SET title = 'Lesson ' || id WHERE title IS NULL OR title = '';

ALTER TABLE lessons ALTER COLUMN title SET NOT NULL;

CREATE TABLE IF NOT EXISTS material_library_items (
    id              BIGSERIAL PRIMARY KEY,
    teacher_user_id VARCHAR(36) NOT NULL REFERENCES users (uuid),
    title           VARCHAR(500) NOT NULL,
    module_tag      VARCHAR(128),
    description     TEXT,
    icon_type       VARCHAR(32) NOT NULL DEFAULT 'SLIDES',
    gradient        VARCHAR(128) NOT NULL DEFAULT 'from-violet-800 to-violet-600',
    deleted         BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_library_items_teacher
    ON material_library_items (teacher_user_id) WHERE deleted = false;

CREATE TABLE IF NOT EXISTS material_library_materials (
    id                BIGSERIAL PRIMARY KEY,
    library_item_id   BIGINT NOT NULL REFERENCES material_library_items (id) ON DELETE CASCADE,
    file_name         VARCHAR(500) NOT NULL,
    content_type      VARCHAR(128),
    file_size_bytes   BIGINT NOT NULL DEFAULT 0,
    storage_key       VARCHAR(1024) NOT NULL,
    deleted           BOOLEAN NOT NULL DEFAULT false,
    created_at        TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_library_materials_item
    ON material_library_materials (library_item_id) WHERE deleted = false;

CREATE TABLE IF NOT EXISTS lesson_materials (
    id                BIGSERIAL PRIMARY KEY,
    lesson_id         BIGINT NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    file_name         VARCHAR(500) NOT NULL,
    content_type      VARCHAR(128),
    file_size_bytes   BIGINT NOT NULL DEFAULT 0,
    storage_key       VARCHAR(1024) NOT NULL,
    deleted           BOOLEAN NOT NULL DEFAULT false,
    created_at        TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_lesson_materials_lesson
    ON lesson_materials (lesson_id) WHERE deleted = false;

-- Sample lesson for first active class (when enrollments exist)
INSERT INTO lessons (class_id, title, description, module_tag, sort_order, deleted)
SELECT c.id,
       'Introduction',
       'Welcome to this class. Review the materials and use AI tools when available.',
       COALESCE(c.semester, 'General'),
       0,
       false
FROM lms_classes c
WHERE c.deleted = false
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.class_id = c.id AND l.deleted = false)
LIMIT 1;
