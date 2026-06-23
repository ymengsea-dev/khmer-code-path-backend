-- SCH Phase 2 — registration domains for school-scoped student signup.

CREATE TABLE IF NOT EXISTS registration_domains (
    id              BIGSERIAL PRIMARY KEY,
    school_id       BIGINT NOT NULL REFERENCES schools (id) ON DELETE CASCADE,
    domain          VARCHAR(255) NOT NULL,
    auto_approve    BOOLEAN NOT NULL DEFAULT true,
    default_role    VARCHAR(32) NOT NULL DEFAULT 'STUDENT',
    deleted         BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMP WITHOUT TIME ZONE,
    updated_at      TIMESTAMP WITHOUT TIME ZONE
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_registration_domains_domain_active
    ON registration_domains (LOWER(domain)) WHERE deleted = false;

CREATE INDEX IF NOT EXISTS idx_registration_domains_school
    ON registration_domains (school_id) WHERE deleted = false;
