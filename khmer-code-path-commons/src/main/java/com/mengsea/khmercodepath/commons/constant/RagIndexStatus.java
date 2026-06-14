package com.mengsea.khmercodepath.commons.constant;

public enum RagIndexStatus {
    /** Uploaded to object storage; vectors not built yet. */
    NOT_INDEXED,
    /** Waiting in the Redis indexing queue. */
    QUEUED,
    /** Chunking / embedding in progress. */
    INDEXING,
    /** Ready for quiz / summary / Q&A retrieval. */
    READY,
    /** Last indexing attempt failed. */
    FAILED,
    /** File changed or re-uploaded; must re-index on next AI call. */
    STALE
}
