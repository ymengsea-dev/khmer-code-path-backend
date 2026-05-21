package com.mengsea.khmercodepath.api.ai.rag;

import com.mengsea.khmercodepath.api.ai.gateway.LlmGateway;
import com.mengsea.khmercodepath.api.storage.UploadStorage;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.MaterialSourceType;
import com.mengsea.khmercodepath.commons.constant.RagIndexStatus;
import com.mengsea.khmercodepath.commons.domain.MaterialRagIndex;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.MaterialRagIndexRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * On-demand RAG: load a single material from MinIO, index into pgvector when AI is invoked,
 * retrieve with metadata filters scoped to that material id.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialRagVectorService {

    private final MaterialRagIndexRepository ragIndexRepository;
    private final UploadStorage uploadStorage;
    private final MaterialDocumentExtractor documentExtractor;
    private final VectorStore vectorStore;
    private final LlmGateway llmGateway;

    @Transactional
    public MaterialRagIndex registerLessonMaterial(
            Long materialId,
            Long lessonId,
            String storageKey,
            String fileName,
            String contentType
    ) {
        MaterialRagIndex index = ragIndexRepository
                .findBySourceTypeAndSourceId(MaterialSourceType.LESSON_MATERIAL, materialId)
                .orElseGet(MaterialRagIndex::new);
        index.setSourceType(MaterialSourceType.LESSON_MATERIAL);
        index.setSourceId(materialId);
        index.setLessonId(lessonId);
        index.setStorageKey(storageKey);
        index.setFileName(fileName);
        index.setContentType(contentType);
        index.setStatus(RagIndexStatus.NOT_INDEXED);
        index.setChunkCount(0);
        index.setIndexedAt(null);
        index.setErrorMessage(null);
        return ragIndexRepository.save(index);
    }

    @Transactional
    public MaterialRagIndex registerLibraryMaterial(
            Long materialId,
            String storageKey,
            String fileName,
            String contentType
    ) {
        MaterialRagIndex index = ragIndexRepository
                .findBySourceTypeAndSourceId(MaterialSourceType.LIBRARY_MATERIAL, materialId)
                .orElseGet(MaterialRagIndex::new);
        index.setSourceType(MaterialSourceType.LIBRARY_MATERIAL);
        index.setSourceId(materialId);
        index.setLessonId(null);
        index.setStorageKey(storageKey);
        index.setFileName(fileName);
        index.setContentType(contentType);
        index.setStatus(RagIndexStatus.NOT_INDEXED);
        return ragIndexRepository.save(index);
    }

    /**
     * Loads file from object storage and builds vectors only when needed (quiz / summary / Q&A).
     */
    public MaterialRagIndex ensureIndexed(MaterialSourceType sourceType, Long sourceId) {
        MaterialRagIndex index = ragIndexRepository.findBySourceTypeAndSourceId(sourceType, sourceId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND));

        if (index.getStatus() == RagIndexStatus.READY) {
            return index;
        }
        if (index.getStatus() == RagIndexStatus.INDEXING) {
            throw new BusinessException(ExceptionCode.MATERIAL_RAG_NOT_READY);
        }

        index.setStatus(RagIndexStatus.INDEXING);
        index.setErrorMessage(null);
        ragIndexRepository.save(index);

        try {
            deleteVectorsForMaterial(sourceId);
            List<Document> rawDocs;
            try (var stream = uploadStorage.openStream(index.getStorageKey())) {
                rawDocs = documentExtractor.extract(stream, index.getFileName());
            }
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> chunks = splitter.apply(rawDocs);
            String materialIdStr = String.valueOf(sourceId);
            String lessonIdStr = index.getLessonId() != null ? String.valueOf(index.getLessonId()) : "";
            List<Document> enriched = new java.util.ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                Document chunk = chunks.get(i);
                Map<String, Object> meta = new HashMap<>(chunk.getMetadata());
                meta.put(MaterialRagMetadata.MATERIAL_ID, materialIdStr);
                meta.put(MaterialRagMetadata.SOURCE_TYPE, sourceType.name());
                meta.put(MaterialRagMetadata.FILE_NAME, index.getFileName());
                meta.put(MaterialRagMetadata.CHUNK_INDEX, i);
                if (!lessonIdStr.isBlank()) {
                    meta.put(MaterialRagMetadata.LESSON_ID, lessonIdStr);
                }
                enriched.add(new Document(chunk.getText(), meta));
            }
            vectorStore.add(enriched);
            index.setStatus(RagIndexStatus.READY);
            index.setChunkCount(enriched.size());
            index.setIndexedAt(LocalDateTime.now());
            log.info("Indexed material {} ({} chunks) from MinIO key {}", sourceId, enriched.size(), index.getStorageKey());
        } catch (BusinessException ex) {
            index.setStatus(RagIndexStatus.FAILED);
            index.setErrorMessage(ex.getExceptionCode().getMessage());
            ragIndexRepository.save(index);
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to index material {}", sourceId, ex);
            index.setStatus(RagIndexStatus.FAILED);
            index.setErrorMessage(ex.getMessage());
            ragIndexRepository.save(index);
            throw new BusinessException(ExceptionCode.MATERIAL_RAG_INDEX_FAILED);
        }
        return ragIndexRepository.save(index);
    }

    public String queryMaterial(MaterialSourceType sourceType, Long sourceId, String question, int topK) {
        ensureIndexed(sourceType, sourceId);
        Filter.Expression filter = new FilterExpressionBuilder()
                .eq(MaterialRagMetadata.MATERIAL_ID, String.valueOf(sourceId))
                .build();
        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(topK)
                .filterExpression(filter)
                .build();
        return llmGateway.completeRagQuery(question, searchRequest);
    }

    @Transactional
    public void removeIndex(MaterialSourceType sourceType, Long sourceId, String storageKey) {
        deleteVectorsForMaterial(sourceId);
        ragIndexRepository.findBySourceTypeAndSourceId(sourceType, sourceId)
                .ifPresent(ragIndexRepository::delete);
        if (storageKey != null && !storageKey.isBlank()) {
            uploadStorage.delete(storageKey);
        }
    }

    @Transactional(readOnly = true)
    public MaterialRagIndex getIndexStatus(MaterialSourceType sourceType, Long sourceId) {
        return ragIndexRepository.findBySourceTypeAndSourceId(sourceType, sourceId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND));
    }

    private void deleteVectorsForMaterial(Long materialId) {
        try {
            Filter.Expression filter = new FilterExpressionBuilder()
                    .eq(MaterialRagMetadata.MATERIAL_ID, String.valueOf(materialId))
                    .build();
            vectorStore.delete(filter);
        } catch (Exception ex) {
            log.debug("No existing vectors to delete for material {}: {}", materialId, ex.getMessage());
        }
    }
}
