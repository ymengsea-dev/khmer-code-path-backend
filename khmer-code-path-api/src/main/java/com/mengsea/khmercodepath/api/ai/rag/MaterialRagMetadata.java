package com.mengsea.khmercodepath.api.ai.rag;

/**
 * Metadata keys stored on each vector chunk in pgvector (used for filtered retrieval).
 */
public final class MaterialRagMetadata {

    public static final String MATERIAL_ID = "materialId";
    public static final String LESSON_ID = "lessonId";
    public static final String SOURCE_TYPE = "sourceType";
    public static final String FILE_NAME = "fileName";
    public static final String CHUNK_INDEX = "chunkIndex";

    private MaterialRagMetadata() {}
}
