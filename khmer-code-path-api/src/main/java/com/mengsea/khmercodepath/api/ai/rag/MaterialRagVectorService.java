package com.mengsea.khmercodepath.api.ai.rag;

import com.mengsea.khmercodepath.api.ai.config.AiAvailabilityService;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
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

    private static final String INDEX_QUEUE_KEY = "khmer-code-path:material-rag:index-queue";

    private final MaterialRagIndexRepository ragIndexRepository;
    private final UploadStorage uploadStorage;
    private final MaterialDocumentExtractor documentExtractor;
    private final ObjectProvider<VectorStore> vectorStoreProvider;
    private final LlmGateway llmGateway;
    private final ObjectProvider<AiAvailabilityService> aiAvailabilityProvider;
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;

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
        ensureAiAvailable();
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
            vectorStoreProvider.getObject().add(enriched);
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
            aiAvailabilityProvider.ifAvailable(availability -> availability.markUnavailable(ex));
            index.setStatus(RagIndexStatus.FAILED);
            index.setErrorMessage(ex.getMessage());
            ragIndexRepository.save(index);
            if (isAiUnavailable(ex)) {
                throw new BusinessException(ExceptionCode.AI_SERVICE_UNAVAILABLE);
            }
            throw new BusinessException(ExceptionCode.MATERIAL_RAG_INDEX_FAILED);
        }
        return ragIndexRepository.save(index);
    }

    @Transactional
    public MaterialRagIndex queueIndex(MaterialSourceType sourceType, Long sourceId) {
        MaterialRagIndex index = ragIndexRepository.findBySourceTypeAndSourceId(sourceType, sourceId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND));
        if (index.getStatus() == RagIndexStatus.READY || index.getStatus() == RagIndexStatus.INDEXING) {
            return index;
        }
        index.setStatus(RagIndexStatus.QUEUED);
        index.setErrorMessage(null);
        ragIndexRepository.save(index);
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            redisTemplate.opsForList().rightPush(INDEX_QUEUE_KEY, sourceType.name() + ":" + sourceId);
        } else {
            Thread.ofVirtual().start(() -> ensureIndexed(sourceType, sourceId));
        }
        return index;
    }

    @Scheduled(fixedDelayString = "${lms.ai.index-queue-delay-ms:5000}")
    public void processQueuedIndexJob() {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return;
        }
        String job = redisTemplate.opsForList().leftPop(INDEX_QUEUE_KEY);
        if (job == null || job.isBlank()) {
            return;
        }
        String[] parts = job.split(":", 2);
        if (parts.length != 2) {
            log.warn("Skipping malformed material RAG index job: {}", job);
            return;
        }
        try {
            ensureIndexed(MaterialSourceType.valueOf(parts[0]), Long.valueOf(parts[1]));
        } catch (Exception ex) {
            log.warn("Queued material RAG index job failed: {}", job, ex);
        }
    }

    public String queryMaterial(MaterialSourceType sourceType, Long sourceId, String question, int topK) {
        ensureAiAvailable();
        SearchRequest searchRequest = materialSearchRequest(sourceId, question, topK);
        return llmGateway.completeRagQuery(question, searchRequest);
    }

    public List<Document> searchMaterial(MaterialSourceType sourceType, Long sourceId, String question, int topK) {
        ensureAiAvailable();
        ensureIndexed(sourceType, sourceId);
        return vectorStoreProvider.getObject().similaritySearch(materialSearchRequest(sourceId, question, topK));
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
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (vectorStore == null) {
            return;
        }
        try {
            Filter.Expression filter = new FilterExpressionBuilder()
                    .eq(MaterialRagMetadata.MATERIAL_ID, String.valueOf(materialId))
                    .build();
            vectorStore.delete(filter);
        } catch (Exception ex) {
            log.debug("No existing vectors to delete for material {}: {}", materialId, ex.getMessage());
        }
    }

    private SearchRequest materialSearchRequest(Long sourceId, String question, int topK) {
        Filter.Expression filter = new FilterExpressionBuilder()
                .eq(MaterialRagMetadata.MATERIAL_ID, String.valueOf(sourceId))
                .build();
        return SearchRequest.builder()
                .query(question)
                .topK(topK)
                .filterExpression(filter)
                .build();
    }

    private void ensureAiAvailable() {
        AiAvailabilityService availability = aiAvailabilityProvider.getIfAvailable();
        if (availability != null && !availability.isAvailable()) {
            throw new BusinessException(ExceptionCode.AI_SERVICE_UNAVAILABLE);
        }
        if (vectorStoreProvider.getIfAvailable() == null) {
            throw new BusinessException(ExceptionCode.AI_SERVICE_UNAVAILABLE);
        }
    }

    private static boolean isAiUnavailable(Throwable ex) {
        String message = ex.getMessage();
        if (message == null) {
            return false;
        }
        String lower = message.toLowerCase();
        return lower.contains("connection refused")
                || lower.contains("connect timed out")
                || lower.contains("failed to connect")
                || lower.contains("503")
                || lower.contains("404");
    }
}
