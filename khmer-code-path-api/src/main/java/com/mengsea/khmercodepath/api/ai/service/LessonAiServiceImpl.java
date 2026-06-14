package com.mengsea.khmercodepath.api.ai.service;

import com.mengsea.khmercodepath.api.ai.gateway.LlmGateway;
import com.mengsea.khmercodepath.api.ai.payload.GenerateFromMaterialRequest;
import com.mengsea.khmercodepath.api.ai.payload.LessonAnswerPayload;
import com.mengsea.khmercodepath.api.ai.payload.LessonCitationPayload;
import com.mengsea.khmercodepath.api.ai.payload.LessonImprovePayload;
import com.mengsea.khmercodepath.api.ai.payload.LessonImproveRequest;
import com.mengsea.khmercodepath.api.ai.payload.LessonSummaryGeneratePayload;
import com.mengsea.khmercodepath.api.ai.payload.MaterialRagStatusPayload;
import com.mengsea.khmercodepath.api.ai.payload.QuizGeneratePayload;
import com.mengsea.khmercodepath.api.ai.rag.MaterialRagMetadata;
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
import org.springframework.ai.document.Document;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LessonAiServiceImpl implements LessonAiService {

    private final LessonRepository lessonRepository;
    private final LessonMaterialRepository lessonMaterialRepository;
    private final ClassAccessHelper classAccessHelper;
    private final MaterialRagVectorService materialRagVectorService;
    private final LlmGateway llmGateway;

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
    public MaterialRagStatusPayload queueMaterialIndex(Long lessonId, Long materialId) {
        Lesson lesson = requireManageableLesson(lessonId);
        LessonMaterial material = requireLessonMaterial(lesson.getId(), materialId);
        MaterialRagIndex index = materialRagVectorService.queueIndex(
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

    @Override
    @Transactional
    public LessonSummaryGeneratePayload generateSummaryFromContent(Long lessonId) {
        Lesson lesson = requireReadableLesson(lessonId);
        String plainText = stripHtml(lesson.getDescription());

        if (plainText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "This lesson has no written notes. Add content in the lesson editor first.");
        }

        boolean forTeacher = classAccessHelper.canManageClass(lesson.getLmsClass());
        String systemPrompt = forTeacher
                ? """
                You are an expert instructor. Read the lesson content and write a clear, structured summary
                for students. Use this format:
                Key Points:
                - 3 to 5 concise bullet points
                Quick Review:
                - 1 short sentence
                Keep the summary shorter than the original content. Focus on key concepts only.
                Do not invent facts outside the provided content.
                """
                : """
                You are a study assistant. Read the lesson content and write a clear, student-friendly summary.
                Use this exact format:
                Key Points:
                - 3 to 5 concise bullet points
                Quick Review:
                - 1 short sentence
                Keep the summary shorter than the original content. Explain key terms simply only if needed.
                Focus only on what appears in the content. Do not invent facts outside it.
                """;

        String summary = llmGateway.completeWithContent(systemPrompt, plainText);

        boolean persisted = false;
        if (forTeacher) {
            lesson.setSummary(summary);
            lessonRepository.save(lesson);
            persisted = true;
        }

        return LessonSummaryGeneratePayload.builder()
                .lessonId(lessonId)
                .materialId(null)
                .summary(summary)
                .sourceFileName(lesson.getTitle() + " (written notes)")
                .persisted(persisted)
                .build();
    }

    @Override
    @Transactional
    public LessonAnswerPayload answerWithCitations(Long lessonId, Long materialId, String question) {
        Lesson lesson = requireReadableLesson(lessonId);
        String trimmedQuestion = question == null ? "" : question.trim();
        if (trimmedQuestion.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question is required.");
        }

        List<LessonCitationPayload> citations = new ArrayList<>();
        String context;
        if (materialId != null) {
            LessonMaterial material = requireLessonMaterial(lessonId, materialId);
            List<Document> chunks = materialRagVectorService.searchMaterial(
                    MaterialSourceType.LESSON_MATERIAL,
                    material.getId(),
                    trimmedQuestion,
                    5
            );
            context = chunksToContext(chunks);
            citations.addAll(chunks.stream()
                    .map(chunk -> toCitation(chunk, material.getId(), material.getFileName()))
                    .toList());
        } else {
            String plainText = stripHtml(lesson.getDescription());
            if (plainText.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "This lesson has no written notes to cite.");
            }
            context = plainText;
            citations.add(LessonCitationPayload.builder()
                    .sourceType("LESSON_NOTES")
                    .materialId(null)
                    .sourceName(lesson.getTitle() + " notes")
                    .chunkIndex(null)
                    .excerpt(truncate(plainText, 600))
                    .build());
        }

        String prompt = """
                Answer the student's question using only the cited lesson context.
                If the answer is not in the context, say the lesson does not cover it clearly.
                Keep the answer concise and student-friendly.

                Question: %s
                """.formatted(trimmedQuestion);
        String answer = llmGateway.completeWithContent(prompt, context);
        return LessonAnswerPayload.builder()
                .lessonId(lessonId)
                .answer(answer)
                .citations(citations)
                .build();
    }

    @Override
    @Transactional
    public LessonImprovePayload improveLesson(Long lessonId, LessonImproveRequest request) {
        Lesson lesson = requireManageableLesson(lessonId);
        String plainText = stripHtml(lesson.getDescription());
        if (plainText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "This lesson has no written notes to improve.");
        }

        String goal = request.getGoal() == null || request.getGoal().isBlank()
                ? "Improve clarity, add objectives, examples, and short practice exercises."
                : request.getGoal().trim();
        String prompt = """
                You are helping a teacher improve lesson content.
                Rewrite the lesson in clean HTML with headings, short paragraphs, examples, and practice exercises.
                Preserve the original meaning. Do not invent advanced topics unrelated to the lesson.
                Teacher goal: %s
                """.formatted(goal);
        String improved = llmGateway.completeWithContent(prompt, plainText);
        boolean persisted = request.isPersist();
        if (persisted) {
            lesson.setDescription(improved);
            lessonRepository.save(lesson);
        }
        return LessonImprovePayload.builder()
                .lessonId(lessonId)
                .improvedContent(improved)
                .persisted(persisted)
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

    private static String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", " ").replaceAll("&[a-zA-Z]+;", " ").replaceAll("\\s+", " ").trim();
    }

    private static String chunksToContext(List<Document> chunks) {
        return chunks.stream()
                .map(chunk -> {
                    Map<String, Object> metadata = chunk.getMetadata();
                    Object sourceName = metadata.get(MaterialRagMetadata.FILE_NAME);
                    Object chunkIndex = metadata.get(MaterialRagMetadata.CHUNK_INDEX);
                    return "[%s chunk %s]\n%s".formatted(sourceName, chunkIndex, chunk.getText());
                })
                .reduce("", (left, right) -> left + "\n\n" + right);
    }

    private static LessonCitationPayload toCitation(Document chunk, Long materialId, String fallbackFileName) {
        Map<String, Object> metadata = chunk.getMetadata();
        Object chunkIndex = metadata.get(MaterialRagMetadata.CHUNK_INDEX);
        return LessonCitationPayload.builder()
                .sourceType("LESSON_MATERIAL")
                .materialId(materialId)
                .sourceName(String.valueOf(metadata.getOrDefault(MaterialRagMetadata.FILE_NAME, fallbackFileName)))
                .chunkIndex(chunkIndex instanceof Number n ? n.intValue() : null)
                .excerpt(truncate(chunk.getText(), 600))
                .build();
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        String cleaned = text.replaceAll("\\s+", " ").trim();
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength - 3) + "...";
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
