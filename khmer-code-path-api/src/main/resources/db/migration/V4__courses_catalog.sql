-- CRS — course catalog (learning paths) + optional student enrollment progress

CREATE TABLE IF NOT EXISTS courses (
    id                  BIGSERIAL PRIMARY KEY,
    title               VARCHAR(500) NOT NULL,
    institution         VARCHAR(255) NOT NULL,
    institution_logo    VARCHAR(64),
    institution_color   VARCHAR(16) NOT NULL DEFAULT '#8b5cf6',
    level               VARCHAR(32) NOT NULL DEFAULT 'BEGINNER',
    pts                 INT NOT NULL DEFAULT 150,
    bg_color            VARCHAR(128) NOT NULL DEFAULT 'from-slate-900 to-slate-700',
    image_url           TEXT,
    description         TEXT,
    technologies_json   TEXT NOT NULL DEFAULT '[]',
    prerequisite        VARCHAR(500),
    achievement         VARCHAR(255),
    locked              BOOLEAN NOT NULL DEFAULT false,
    published           BOOLEAN NOT NULL DEFAULT true,
    deleted             BOOLEAN NOT NULL DEFAULT false,
    created_by          VARCHAR(36) REFERENCES users (uuid),
    created_at          TIMESTAMP WITHOUT TIME ZONE,
    updated_at          TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_courses_published ON courses (published) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_courses_created_by ON courses (created_by) WHERE deleted = false;

CREATE TABLE IF NOT EXISTS course_enrollments (
    id                BIGSERIAL PRIMARY KEY,
    course_id         BIGINT NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    student_user_id   VARCHAR(36) NOT NULL REFERENCES users (uuid) ON DELETE CASCADE,
    progress_pct      INT NOT NULL DEFAULT 0,
    enrolled_at       TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_course_enrollment UNIQUE (course_id, student_user_id)
);

CREATE INDEX IF NOT EXISTS idx_course_enrollments_student ON course_enrollments (student_user_id);

-- Seed catalog aligned with frontend demo data
INSERT INTO courses (
    title, institution, institution_logo, institution_color, level, pts, bg_color, image_url,
    description, technologies_json, prerequisite, achievement, locked, published
) VALUES
(
    'Introduction to Python Programming',
    'MIT', 'MIT', '#A31F34', 'BEGINNER', 150, 'from-slate-900 to-slate-700',
    'https://quantumzeitgeist.com/wp-content/uploads/python.jpg',
    'Learn the fundamentals of Python programming, from basic syntax to data structures and algorithms.',
    '[{"name":"Python","color":"#3776AB"},{"name":"Jupyter","color":"#F37626"}]',
    NULL, 'Python Developer', false, true
),
(
    'AI Engineer Certification',
    'Stanford', 'S', '#8C1515', 'INTERMEDIATE', 150, 'from-blue-950 to-indigo-800',
    'https://www.imperial-overseas.com/blog/wp-content/uploads/2023/09/MAUK-01.jpg',
    'Comprehensive AI engineering track with hands-on projects and industry-aligned outcomes.',
    '[{"name":"Python","color":"#3776AB"},{"name":"PyTorch","color":"#EE4C2C"},{"name":"AWS","color":"#FF9900"},{"name":"Git","color":"#F05032"}]',
    'Data Structures and Algorithms', 'Certified AI Specialist', false, true
),
(
    'Full Stack Web Development',
    'Meta', 'M', '#0082FB', 'ADVANCED', 150, 'from-purple-950 to-blue-900',
    'https://www.htmlpanda.com/blog/wp-content/uploads/2022/03/Comprehensive-Guide-to-Full-Stack-Web-Development-2.png',
    'Master advanced full-stack development with React, Node.js, and cloud deployment.',
    '[{"name":"React","color":"#61DAFB"},{"name":"Node.js","color":"#339933"},{"name":"PostgreSQL","color":"#336791"}]',
    'AI Engineer Certification', 'Full Stack Engineer', false, true
);
