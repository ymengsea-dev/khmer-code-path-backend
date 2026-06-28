-- Combined content blocks (AI questions, uploaded files, CM library sources)

ALTER TABLE assignments
    ADD COLUMN IF NOT EXISTS content_blocks_json TEXT;

ALTER TABLE exams
    ADD COLUMN IF NOT EXISTS content_blocks_json TEXT;
