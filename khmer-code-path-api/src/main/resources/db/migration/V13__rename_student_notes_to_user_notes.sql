-- Rename legacy table if V12 was applied under the old student_notes name

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'student_notes'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'user_notes'
    ) THEN
        ALTER TABLE student_notes RENAME TO user_notes;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = 'i' AND c.relname = 'idx_student_notes_user'
    ) THEN
        ALTER INDEX idx_student_notes_user RENAME TO idx_user_notes_user;
    END IF;
END $$;
