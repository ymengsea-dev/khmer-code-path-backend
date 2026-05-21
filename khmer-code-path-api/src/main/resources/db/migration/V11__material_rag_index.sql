-- On-demand RAG: track vector index state per uploaded material (MinIO object)

CREATE TABLE IF NOT EXISTS material_rag_index (
    id                BIGSERIAL PRIMARY KEY,
    source_type       VARCHAR(32) NOT NULL,
    source_id         BIGINT NOT NULL,
    lesson_id         BIGINT,
    storage_key       VARCHAR(1024) NOT NULL,
    file_name         VARCHAR(500) NOT NULL,
    content_type      VARCHAR(128),
    status            VARCHAR(32) NOT NULL DEFAULT 'NOT_INDEXED',
    chunk_count       INT NOT NULL DEFAULT 0,
    indexed_at        TIMESTAMP WITHOUT TIME ZONE,
    error_message     TEXT,
    created_at        TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_material_rag_source UNIQUE (source_type, source_id)
);

CREATE INDEX IF NOT EXISTS idx_material_rag_lesson
    ON material_rag_index (lesson_id)
    WHERE lesson_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_material_rag_status
    ON material_rag_index (status);
