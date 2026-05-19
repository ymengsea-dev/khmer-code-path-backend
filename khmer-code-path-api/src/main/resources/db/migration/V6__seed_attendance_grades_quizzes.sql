-- Sample academic data for V3 tables (when classes and students exist)

INSERT INTO quizzes (class_id, title, status, deleted)
SELECT c.id, 'Week 1 Quiz', 'PUBLISHED', false
FROM lms_classes c
WHERE c.deleted = false
  AND NOT EXISTS (
      SELECT 1 FROM quizzes q WHERE q.class_id = c.id AND q.deleted = false LIMIT 1
  )
LIMIT 1;

INSERT INTO quiz_submissions (quiz_id, student_user_id, status)
SELECT q.id, e.student_user_id, 'COMPLETED'
FROM quizzes q
JOIN class_enrollments e ON e.class_id = q.class_id
WHERE NOT EXISTS (
    SELECT 1 FROM quiz_submissions s
    WHERE s.quiz_id = q.id AND s.student_user_id = e.student_user_id
)
LIMIT 20;

INSERT INTO attendance_records (class_id, student_user_id, session_date, status)
SELECT e.class_id, e.student_user_id, CURRENT_DATE - INTERVAL '7 days', 'PRESENT'
FROM class_enrollments e
WHERE NOT EXISTS (
    SELECT 1 FROM attendance_records a
    WHERE a.class_id = e.class_id
      AND a.student_user_id = e.student_user_id
      AND a.session_date = CURRENT_DATE - INTERVAL '7 days'
)
LIMIT 30;

INSERT INTO attendance_records (class_id, student_user_id, session_date, status)
SELECT e.class_id, e.student_user_id, CURRENT_DATE, 'PRESENT'
FROM class_enrollments e
WHERE NOT EXISTS (
    SELECT 1 FROM attendance_records a
    WHERE a.class_id = e.class_id
      AND a.student_user_id = e.student_user_id
      AND a.session_date = CURRENT_DATE
)
LIMIT 30;

INSERT INTO student_grades (class_id, student_user_id, numeric_grade, letter_grade)
SELECT e.class_id, e.student_user_id, 88.50, 'B+'
FROM class_enrollments e
WHERE NOT EXISTS (
    SELECT 1 FROM student_grades g
    WHERE g.class_id = e.class_id AND g.student_user_id = e.student_user_id
)
LIMIT 30;
