package com.mengsea.khmercodepath.api.assignmentsexams.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mengsea.khmercodepath.api.assignmentsexams.payload.TaskContentBlockDto;
import com.mengsea.khmercodepath.api.assignmentsexams.payload.TaskContentBlockInput;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.domain.MaterialLibraryItem;
import com.mengsea.khmercodepath.commons.domain.MaterialLibraryMaterial;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.MaterialLibraryItemRepository;
import com.mengsea.khmercodepath.commons.repository.MaterialLibraryMaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskContentBlockService {

    public static final String KIND_AI = "AI_QUESTIONS";
    public static final String KIND_FILE = "FILE";
    public static final String KIND_LIBRARY = "LIBRARY_SOURCE";

    private static final String TASK_CONTENT_CATEGORY = "task-content";

    private final ObjectMapper objectMapper;
    private final MaterialLibraryItemRepository materialLibraryItemRepository;
    private final MaterialLibraryMaterialRepository materialLibraryMaterialRepository;

    public static long ownerKeyForTeacher(String teacherUuid) {
        return Math.abs((long) teacherUuid.hashCode());
    }

    public static String expectedStoragePrefix(String teacherUuid) {
        return TASK_CONTENT_CATEGORY + "/" + ownerKeyForTeacher(teacherUuid) + "/";
    }

    public String serialize(List<TaskContentBlockInput> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(normalizeOrder(blocks));
        } catch (Exception e) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
    }

    public List<TaskContentBlockInput> deserialize(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Could not parse content blocks JSON: {}", e.getMessage());
            return List.of();
        }
    }

    public void validateForTeacher(List<TaskContentBlockInput> blocks, String teacherUuid) {
        if (blocks == null || blocks.isEmpty()) {
            return;
        }
        String prefix = expectedStoragePrefix(teacherUuid);
        for (TaskContentBlockInput block : blocks) {
            String kind = block.getKind();
            if (kind == null || kind.isBlank()) {
                throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
            }
            switch (kind) {
                case KIND_AI -> {
                    if (block.getGeneratedContent() == null || block.getGeneratedContent().isBlank()) {
                        throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
                    }
                    if (countQuestions(block.getGeneratedContent()) == 0) {
                        throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
                    }
                }
                case KIND_FILE -> {
                    if (block.getStorageKey() == null || !block.getStorageKey().startsWith(prefix)) {
                        throw new BusinessException(ExceptionCode.ACCESS_DENIED);
                    }
                    if (block.getFileName() == null || block.getFileName().isBlank()) {
                        throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
                    }
                }
                case KIND_LIBRARY -> validateLibraryBlock(block);
                default -> throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
            }
        }
    }

    public String mergeAiGeneratedContent(List<TaskContentBlockInput> blocks, String legacyContent) {
        List<Map<String, Object>> merged = new ArrayList<>();
        if (blocks != null) {
            for (TaskContentBlockInput block : blocks) {
                if (KIND_AI.equals(block.getKind())) {
                    merged.addAll(parseQuestionMaps(block.getGeneratedContent()));
                }
            }
        }
        if (merged.isEmpty() && legacyContent != null && !legacyContent.isBlank()) {
            merged.addAll(parseQuestionMaps(legacyContent));
        }
        if (merged.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(merged);
        } catch (Exception e) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
    }

    public int countQuestions(String generatedContent) {
        return parseQuestionMaps(generatedContent).size();
    }

    public List<TaskContentBlockDto> enrichBlocks(
            String json,
            Long assignmentId,
            Long examId,
            boolean includeAiAnswers
    ) {
        List<TaskContentBlockInput> blocks = deserialize(json);
        List<TaskContentBlockDto> result = new ArrayList<>();
        for (int i = 0; i < blocks.size(); i++) {
            TaskContentBlockInput block = blocks.get(i);
            int order = block.getOrderIndex() != null ? block.getOrderIndex() : i;
            TaskContentBlockDto.TaskContentBlockDtoBuilder builder = TaskContentBlockDto.builder()
                    .kind(block.getKind())
                    .orderIndex(order);

            switch (block.getKind()) {
                case KIND_AI -> {
                    builder.sourceLabel(block.getSourceLabel());
                    builder.questionCount(block.getQuestionCount());
                    builder.label(block.getSourceLabel() != null
                            ? "AI questions — " + block.getSourceLabel()
                            : "AI-generated questions");
                    builder.generatedContent(includeAiAnswers
                            ? block.getGeneratedContent()
                            : stripAiAnswers(block.getGeneratedContent()));
                }
                case KIND_FILE -> {
                    builder.label(block.getFileName());
                    builder.storageKey(block.getStorageKey());
                    builder.fileName(block.getFileName());
                    builder.contentType(block.getContentType());
                    builder.sizeBytes(block.getSizeBytes());
                    builder.downloadUrl(buildFileDownloadUrl(assignmentId, examId, block.getStorageKey()));
                }
                case KIND_LIBRARY -> enrichLibraryBlock(builder, block, assignmentId, examId);
                default -> { /* skip unknown */ }
            }
            result.add(builder.build());
        }
        return result;
    }

    public boolean referencesStorageKey(String json, String storageKey) {
        if (storageKey == null || json == null) {
            return false;
        }
        return deserialize(json).stream()
                .anyMatch(b -> KIND_FILE.equals(b.getKind()) && storageKey.equals(b.getStorageKey()));
    }

    public boolean referencesLibraryMaterial(String json, Long libraryItemId, Long materialId) {
        if (json == null) {
            return false;
        }
        return deserialize(json).stream()
                .anyMatch(b -> KIND_LIBRARY.equals(b.getKind())
                        && "library".equals(b.getSourceKind())
                        && libraryItemId.equals(b.getLibraryItemId())
                        && materialId.equals(b.getMaterialId()));
    }

    private void validateLibraryBlock(TaskContentBlockInput block) {
        String sourceKind = block.getSourceKind();
        if (sourceKind == null || sourceKind.isBlank()) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
        switch (sourceKind) {
            case "lesson" -> {
                if (block.getLessonId() == null || block.getMaterialId() == null) {
                    throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
                }
            }
            case "library" -> {
                if (block.getLibraryItemId() == null || block.getMaterialId() == null) {
                    throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
                }
                materialLibraryMaterialRepository.findByIdAndDeletedFalse(block.getMaterialId())
                        .filter(m -> m.getLibraryItem().getId().equals(block.getLibraryItemId()))
                        .orElseThrow(() -> new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND));
            }
            case "library-content" -> {
                if (block.getLibraryItemId() == null) {
                    throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
                }
                materialLibraryItemRepository.findByIdAndDeletedFalse(block.getLibraryItemId())
                        .orElseThrow(() -> new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND));
            }
            default -> throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
    }

    private void enrichLibraryBlock(
            TaskContentBlockDto.TaskContentBlockDtoBuilder builder,
            TaskContentBlockInput block,
            Long assignmentId,
            Long examId
    ) {
        builder.sourceKind(block.getSourceKind());
        builder.lessonId(block.getLessonId());
        builder.libraryItemId(block.getLibraryItemId());
        builder.materialId(block.getMaterialId());
        builder.label(block.getLabel() != null ? block.getLabel() : "Content Management source");

        String sourceKind = block.getSourceKind();
        if ("lesson".equals(sourceKind) && block.getLessonId() != null && block.getMaterialId() != null) {
            builder.downloadUrl("/api/v1/lessons/" + block.getLessonId()
                    + "/materials/" + block.getMaterialId() + "/download");
            return;
        }
        if ("library".equals(sourceKind) && block.getLibraryItemId() != null && block.getMaterialId() != null) {
            String base = "/api/v1/assignments-exams/content/library-download"
                    + "?libraryItemId=" + block.getLibraryItemId()
                    + "&materialId=" + block.getMaterialId();
            if (assignmentId != null) {
                base += "&assignmentId=" + assignmentId;
            } else if (examId != null) {
                base += "&examId=" + examId;
            }
            builder.downloadUrl(base);
            return;
        }
        if ("library-content".equals(sourceKind) && block.getLibraryItemId() != null) {
            materialLibraryItemRepository.findByIdAndDeletedFalse(block.getLibraryItemId())
                    .ifPresent(item -> builder.htmlContent(item.getDescription()));
        }
    }

    private String buildFileDownloadUrl(Long assignmentId, Long examId, String storageKey) {
        if (storageKey == null) {
            return null;
        }
        String base = "/api/v1/assignments-exams/content/file-download?storageKey="
                + urlEncode(storageKey);
        if (assignmentId != null) {
            return base + "&assignmentId=" + assignmentId;
        }
        if (examId != null) {
            return base + "&examId=" + examId;
        }
        return base;
    }

    private static String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    private String stripAiAnswers(String generatedContent) {
        List<Map<String, Object>> questions = parseQuestionMaps(generatedContent);
        List<Map<String, Object>> stripped = new ArrayList<>();
        for (Map<String, Object> q : questions) {
            Map<String, Object> copy = new LinkedHashMap<>(q);
            copy.remove("correctIndex");
            copy.remove("explanation");
            stripped.add(copy);
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(stripped);
        } catch (Exception e) {
            return generatedContent;
        }
    }

    private List<Map<String, Object>> parseQuestionMaps(String generatedContent) {
        if (generatedContent == null || generatedContent.isBlank()) {
            return List.of();
        }
        try {
            String cleaned = generatedContent
                    .replaceAll("(?m)^```[a-zA-Z]*$", "")
                    .replaceAll("(?m)^```$", "")
                    .trim();
            List<Map<String, Object>> parsed = objectMapper.readValue(cleaned, new TypeReference<>() {});
            return parsed != null ? parsed : List.of();
        } catch (Exception e) {
            log.warn("Could not parse AI question content: {}", e.getMessage());
            return List.of();
        }
    }

    private List<TaskContentBlockInput> normalizeOrder(List<TaskContentBlockInput> blocks) {
        List<TaskContentBlockInput> copy = new ArrayList<>(blocks);
        for (int i = 0; i < copy.size(); i++) {
            if (copy.get(i).getOrderIndex() == null) {
                copy.get(i).setOrderIndex(i);
            }
        }
        return copy;
    }

    public String resolveLibraryMaterialStorageKey(Long libraryItemId, Long materialId) {
        MaterialLibraryMaterial material = materialLibraryMaterialRepository
                .findByIdAndDeletedFalse(materialId)
                .filter(m -> m.getLibraryItem().getId().equals(libraryItemId))
                .orElseThrow(() -> new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND));
        return material.getStorageKey();
    }
}
