-- Dev/demo attendance data for the teacher attendance management page.
-- Repeatable Flyway script: re-runs when this file changes.
-- Idempotent: uses ON CONFLICT DO NOTHING and conditional updates.

-- Optional demo class + enrollments when the app already has users but little class data.
INSERT INTO lms_classes (
    code, name, description, teacher_user_id, semester, academic_year,
    schedule, room_number, status, deleted, created_at, updated_at
)
SELECT
    'ATT-DEMO-2026',
    'Attendance Demo Class',
    'Seeded class for attendance management demos.',
    t.uuid,
    'Spring',
    EXTRACT(YEAR FROM CURRENT_DATE)::int,
    'Mon/Wed 09:00–10:30',
    'Lab 101',
    'ACTIVE',
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users t
WHERE t.role = 'TEACHER'
  AND t.deleted = false
  AND t.is_active = true
  AND NOT EXISTS (
      SELECT 1 FROM lms_classes lc
      WHERE lc.code = 'ATT-DEMO-2026' AND lc.deleted = false
  )
ORDER BY
    CASE WHEN t.email ILIKE '%mengsea%' OR t.username ILIKE '%mengsea%' THEN 0 ELSE 1 END,
    t.created_at NULLS LAST,
    t.uuid
LIMIT 1;

INSERT INTO class_enrollments (class_id, student_user_id)
SELECT c.id, s.uuid
FROM lms_classes c
JOIN LATERAL (
    SELECT u.uuid
    FROM users u
    WHERE u.role = 'STUDENT'
      AND u.deleted = false
      AND u.is_active = true
    ORDER BY u.created_at NULLS LAST, u.uuid
    LIMIT 12
) s ON true
WHERE c.code = 'ATT-DEMO-2026'
  AND c.deleted = false
ON CONFLICT (class_id, student_user_id) DO NOTHING;

-- Fill every active class so teachers never land on an empty roster in dev.
INSERT INTO class_enrollments (class_id, student_user_id)
SELECT c.id, s.uuid
FROM lms_classes c
JOIN users s ON s.role = 'STUDENT' AND s.deleted = false AND s.is_active = true
WHERE c.deleted = false
  AND c.status = 'ACTIVE'
ON CONFLICT (class_id, student_user_id) DO NOTHING;

-- Align class academic year with the calendar used by month filters.
UPDATE lms_classes
SET academic_year = EXTRACT(YEAR FROM CURRENT_DATE)::int
WHERE deleted = false
  AND (academic_year IS NULL OR academic_year <> EXTRACT(YEAR FROM CURRENT_DATE)::int);

-- Two class sessions per week from January through the current month.
INSERT INTO attendance_records (class_id, student_user_id, session_date, status)
SELECT
    e.class_id,
    e.student_user_id,
    sessions.session_date,
    CASE
        WHEN mod(
            e.id::bigint
                + EXTRACT(DOY FROM sessions.session_date)::bigint
                + EXTRACT(MONTH FROM sessions.session_date)::bigint,
            11
        ) = 0 THEN 'ABSENT'
        WHEN mod(
            e.id::bigint
                + EXTRACT(DOY FROM sessions.session_date)::bigint
                + EXTRACT(MONTH FROM sessions.session_date)::bigint,
            7
        ) = 0 THEN 'LATE'
        ELSE 'PRESENT'
    END AS status
FROM class_enrollments e
JOIN lms_classes c ON c.id = e.class_id AND c.deleted = false
CROSS JOIN LATERAL (
    SELECT gs::date AS session_date
    FROM generate_series(
        date_trunc('year', CURRENT_DATE)::date,
        (date_trunc('month', CURRENT_DATE) + INTERVAL '1 month' - INTERVAL '1 day')::date,
        INTERVAL '1 day'
    ) AS gs
    WHERE EXTRACT(ISODOW FROM gs) IN (2, 4)
) AS sessions
ON CONFLICT (class_id, student_user_id, session_date) DO NOTHING;

-- Flag one enrolled student per class as attendance-warned (for the warnings column).
UPDATE class_enrollments ce
SET attendance_warned = true,
    attendance_warned_at = COALESCE(ce.attendance_warned_at, CURRENT_TIMESTAMP)
FROM (
    SELECT DISTINCT ON (e.class_id) e.id
    FROM class_enrollments e
    JOIN lms_classes c ON c.id = e.class_id AND c.deleted = false
    ORDER BY e.class_id, e.id
) warned
WHERE ce.id = warned.id
  AND ce.attendance_warned = false;
