ALTER TABLE material_library_items
    ADD COLUMN IF NOT EXISTS file_pool BOOLEAN NOT NULL DEFAULT FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uq_material_library_file_pool_teacher
    ON material_library_items (teacher_user_id)
    WHERE file_pool = TRUE AND deleted = FALSE;
