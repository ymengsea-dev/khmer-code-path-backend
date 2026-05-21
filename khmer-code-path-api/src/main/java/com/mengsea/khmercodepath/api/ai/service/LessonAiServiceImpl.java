package com.mengsea.khmercodepath.api.ai.service;

import com.mengsea.khmercodepath.api.ai.payload.GenerateFromMaterialRequest;
import com.mengsea.khmercodepath.api.ai.payload.LessonSummaryGeneratePayload;
import com.mengsea.khmercodepath.api.ai.payload.MaterialRagStatusPayload;
import com.mengsea.khmercodepath.api.ai.payload.QuizGeneratePayload;
import com.mengsea.khmercodepath.api.ai.rag.MaterialRagVectorService;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.MaterialSourceType;
import com.mengsea.khmercodepath.commons.domain.Lesson;
import com.mengsea.khmercodepath.commons.domain.LessonMaterial;
import com.mengsea.khmercodepath.commons.domain.MaterialRagIndex;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.LessonMaterialRepository;
import com.mengsea.khmercodepath.commons.repository.LessonRepository;
import com.mengsea.khmercodepath.commons.security.ClassAccessHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LessonAiServiceImpl implements LessonAiService {

    private final LessonRepository lessonRepository;
    private final LessonMaterialRepository lessonMaterialRepository;
    private final ClassAccessHelper classAccessHelper;
    private final MaterialRagVectorService materialRagVectorService;

    @Override
    @Transactional(readOnly = true)
    public MaterialRagStatusPayload getMaterialRagStatus(Long lessonId, Long materialId) {
        LessonMaterial material = requireLessonMaterial(lessonId, materialId);
        MaterialRagIndex index = materialRagVectorService.getIndexStatus(
                MaterialSourceType.LESSON_MATERIAL, material.getId());
        return toStatusPayload(index);
    }

    @Override
    @Transactional
    public LessonSummaryGeneratePayload generateSummary(Long lessonId, Long materialId) {
        Lesson lesson = requireReadableLesson(lessonId);
        LessonMaterial material = requireLessonMaterial(lessonId, materialId);

        materialRagVectorService.ensureIndexed(MaterialSourceType.LESSON_MATERIAL, material.getId());

        boolean forTeacher = classAccessHelper.canManageClass(lesson.getLmsClass());
        String prompt = forTeacher
                ? """
                You are an expert instructor. Read the lesson material and write a clear, structured summary
                for students. Use headings and bullet points where helpful. Focus on key concepts only.
                Do not invent facts outside the material.
                """
                : """
                You are a study assistant. Read the lesson material and write a clear, student-friendly summary.
                Use short headings and bullet points. Explain key terms simply. Focus only on what appears in the material.
                Do not invent facts outside the material.
                """;

        String summary = materialRagVectorService.queryMaterial(
                MaterialSourceType.LESSON_MATERIAL,
                material.getId(),
                prompt,
                8
        );

        boolean persisted = false;
        if (forTeacher) {
            lesson.setSummary(summary);
            lessonRepository.save(lesson);
            persisted = true;
        }

        return LessonSummaryGeneratePayload.builder()
                .lessonId(lessonId)
                .materialId(materialId)
                .summary(summary)
                .sourceFileName(material.getFileName())
                .persisted(persisted)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public QuizGeneratePayload generateQuiz(Long lessonId, GenerateFromMaterialRequest request) {
        requireManageableLesson(lessonId);
        LessonMaterial material = requireLessonMaterial(lessonId, request.getMaterialId());

        materialRagVectorService.ensureIndexed(MaterialSourceType.LESSON_MATERIAL, material.getId());

        String prompt = """
                Generate %d multiple-choice quiz questions from this lesson material only.
                Difficulty: %s.
                Return valid JSON array with objects: question, options (array of 4 strings), correctIndex (0-3), explanation.
                Do not invent facts outside the material.
                """.formatted(request.getQuestionCount(), request.getDifficulty());

        String generated = materialRagVectorService.queryMaterial(
                MaterialSourceType.LESSON_MATERIAL,
                material.getId(),
                prompt,
                12
        );

        return QuizGeneratePayload.builder()
                .lessonId(lessonId)
                .materialId(material.getId())
                .sourceFileName(material.getFileName())
                .questionCount(request.getQuestionCount())
                .generatedContent(generated)
                .build();
    }

    private Lesson requireManageableLesson(Long lessonId) {
        Lesson lesson = requireReadableLesson(lessonId);
        classAccessHelper.assertCanManageClass(lesson.getLmsClass());
        return lesson;
    }

    private Lesson requireReadableLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findByIdAndDeletedFalseWithClass(lessonId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.LESSON_NOT_FOUND));
        classAccessHelper.assertCanRead(lesson.getLmsClass());
        return lesson;
    }

    private LessonMaterial requireLessonMaterial(Long lessonId, Long materialId) {
        LessonMaterial material = lessonMaterialRepository.findByIdAndDeletedFalse(materialId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND));
        if (!material.getLesson().getId().equals(lessonId)) {
            throw new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND);
        }
        classAccessHelper.assertCanRead(material.getLesson().getLmsClass());
        return material;
    }

    private static MaterialRagStatusPayload toStatusPayload(MaterialRagIndex index) {
        return MaterialRagStatusPayload.builder()
                .materialId(index.getSourceId())
                .lessonId(index.getLessonId())
                .status(index.getStatus())
                .chunkCount(index.getChunkCount())
                .indexedAt(index.getIndexedAt())
                .errorMessage(index.getErrorMessage())
                .build();
    }
}
