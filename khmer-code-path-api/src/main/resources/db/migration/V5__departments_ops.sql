-- DEPT extended fields + OPS tables

ALTER TABLE departments
    ADD COLUMN IF NOT EXISTS head_of_dept VARCHAR(255),
    ADD COLUMN IF NOT EXISTS faculty_count INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS capacity_percent INT NOT NULL DEFAULT 50,
    ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN IF NOT EXISTS accent VARCHAR(32) NOT NULL DEFAULT 'VIOLET',
    ADD COLUMN IF NOT EXISTS hod_user_id VARCHAR(36) REFERENCES users (uuid);

UPDATE departments SET head_of_dept = 'Dr. SOK San', faculty_count = 24, capacity_percent = 85, accent = 'VIOLET'
WHERE name = 'Computer Science' AND head_of_dept IS NULL;

UPDATE departments SET head_of_dept = 'Prof. RATHA Keo', faculty_count = 12, capacity_percent = 40, accent = 'BLUE'
WHERE name = 'Mathematics' AND head_of_dept IS NULL;

CREATE TABLE IF NOT EXISTS physical_assets (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(500) NOT NULL,
    category      VARCHAR(128) NOT NULL,
    status        VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE',
    location      VARCHAR(255) NOT NULL,
    assigned_to   VARCHAR(255),
    deleted       BOOLEAN NOT NULL DEFAULT false,
    created_at    TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS faculty_requests (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(500) NOT NULL,
    requester_name  VARCHAR(255) NOT NULL,
    requester_user_id VARCHAR(36) REFERENCES users (uuid),
    detail          TEXT,
    icon_type       VARCHAR(32) NOT NULL DEFAULT 'VIDEO',
    status          VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    admin_comment   TEXT,
    deleted         BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS infrastructure_status (
    id            BIGSERIAL PRIMARY KEY,
    category      VARCHAR(32) NOT NULL,
    label         VARCHAR(255) NOT NULL,
    status_text   VARCHAR(128) NOT NULL,
    variant       VARCHAR(32) NOT NULL DEFAULT 'SUCCESS',
    sort_order    INT NOT NULL DEFAULT 0,
    deleted       BOOLEAN NOT NULL DEFAULT false
);

INSERT INTO physical_assets (name, category, status, location, assigned_to)
SELECT 'MacBook Pro M2 (Lab A)', 'Computer', 'AVAILABLE', 'Room 302', NULL
WHERE NOT EXISTS (SELECT 1 FROM physical_assets WHERE name = 'MacBook Pro M2 (Lab A)' AND deleted = false);

INSERT INTO physical_assets (name, category, status, location, assigned_to)
SELECT 'Epson Projector 4K', 'Visual', 'IN_USE', 'Main Hall', 'Prof. RATHA'
WHERE NOT EXISTS (SELECT 1 FROM physical_assets WHERE name = 'Epson Projector 4K' AND deleted = false);

INSERT INTO physical_assets (name, category, status, location, assigned_to)
SELECT 'iPad Air (Set 12)', 'Tablet', 'MAINTENANCE', 'IT Dept', NULL
WHERE NOT EXISTS (SELECT 1 FROM physical_assets WHERE name = 'iPad Air (Set 12)' AND deleted = false);

INSERT INTO faculty_requests (title, requester_name, detail, icon_type, status)
SELECT 'Extra Online Session Request', 'Dr. SOK San', 'Reason: Exam Review', 'VIDEO', 'PENDING'
WHERE NOT EXISTS (SELECT 1 FROM faculty_requests WHERE title = 'Extra Online Session Request' AND deleted = false);

INSERT INTO faculty_requests (title, requester_name, detail, icon_type, status)
SELECT 'Hardware Upgrade Request (Lab B)', 'Prof. RATHA', 'Item: 16GB RAM modules', 'LAPTOP', 'PENDING'
WHERE NOT EXISTS (SELECT 1 FROM faculty_requests WHERE title = 'Hardware Upgrade Request (Lab B)' AND deleted = false);

INSERT INTO faculty_requests (title, requester_name, detail, icon_type, status)
SELECT 'Smart Classroom Booking Extension', 'Dr. SOK San', 'Room 204 • +2 hours Friday', 'ROOM', 'PENDING'
WHERE NOT EXISTS (SELECT 1 FROM faculty_requests WHERE title = 'Smart Classroom Booking Extension' AND deleted = false);

INSERT INTO infrastructure_status (category, label, status_text, variant, sort_order)
SELECT 'SYSTEM', 'LMS Server', 'Online', 'SUCCESS', 1
WHERE NOT EXISTS (SELECT 1 FROM infrastructure_status WHERE category = 'SYSTEM' AND label = 'LMS Server');

INSERT INTO infrastructure_status (category, label, status_text, variant, sort_order)
SELECT 'SYSTEM', 'AI Processing Hub', 'Optimal', 'SUCCESS', 2
WHERE NOT EXISTS (SELECT 1 FROM infrastructure_status WHERE category = 'SYSTEM' AND label = 'AI Processing Hub');

INSERT INTO infrastructure_status (category, label, status_text, variant, sort_order)
SELECT 'SYSTEM', 'Database Cluster', 'High Load', 'WARNING', 3
WHERE NOT EXISTS (SELECT 1 FROM infrastructure_status WHERE category = 'SYSTEM' AND label = 'Database Cluster');

INSERT INTO infrastructure_status (category, label, status_text, variant, sort_order)
SELECT 'FACILITY', 'Smart Classroom 1', 'Available', 'SUCCESS', 1
WHERE NOT EXISTS (SELECT 1 FROM infrastructure_status WHERE category = 'FACILITY' AND label = 'Smart Classroom 1');

INSERT INTO infrastructure_status (category, label, status_text, variant, sort_order)
SELECT 'FACILITY', 'Smart Classroom 2', 'Booked', 'DANGER', 2
WHERE NOT EXISTS (SELECT 1 FROM infrastructure_status WHERE category = 'FACILITY' AND label = 'Smart Classroom 2');

INSERT INTO infrastructure_status (category, label, status_text, variant, sort_order)
SELECT 'FACILITY', 'IT Resource Center', 'Available', 'SUCCESS', 3
WHERE NOT EXISTS (SELECT 1 FROM infrastructure_status WHERE category = 'FACILITY' AND label = 'IT Resource Center');
