package com.mengsea.khmercodepath.api.ai.service;

import com.mengsea.khmercodepath.api.ai.payload.GenerateFromMaterialRequest;
import com.mengsea.khmercodepath.api.ai.payload.QuizGeneratePayload;
import com.mengsea.khmercodepath.api.ai.rag.MaterialRagVectorService;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.MaterialSourceType;
import com.mengsea.khmercodepath.commons.domain.MaterialLibraryItem;
import com.mengsea.khmercodepath.commons.domain.MaterialLibraryMaterial;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.MaterialLibraryItemRepository;
import com.mengsea.khmercodepath.commons.repository.MaterialLibraryMaterialRepository;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LibraryAiServiceImpl implements LibraryAiService {

    private final MaterialLibraryItemRepository materialLibraryItemRepository;
    private final MaterialLibraryMaterialRepository materialLibraryMaterialRepository;
    private final MaterialRagVectorService materialRagVectorService;

    @Override
    @Transactional(readOnly = true)
    public QuizGeneratePayload generateQuiz(Long libraryItemId, GenerateFromMaterialRequest request) {
        String teacherUuid = SecurityUtils.requireCurrentUser().getUuid();
        MaterialLibraryItem item = materialLibraryItemRepository
                .findByIdAndTeacher_UuidAndDeletedFalse(libraryItemId, teacherUuid)
                .orElseThrow(() -> new BusinessException(ExceptionCode.LIBRARY_ITEM_NOT_FOUND));

        MaterialLibraryMaterial material = materialLibraryMaterialRepository
                .findByIdAndDeletedFalse(request.getMaterialId())
                .orElseThrow(() -> new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND));
        if (!material.getLibraryItem().getId().equals(libraryItemId)) {
            throw new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND);
        }

        materialRagVectorService.ensureIndexed(MaterialSourceType.LIBRARY_MATERIAL, material.getId());

        String prompt = """
                Generate %d multiple-choice quiz questions from this lesson material only.
                Difficulty: %s.
                Return valid JSON array with objects: question, options (array of 4 strings), correctIndex (0-3), explanation.
                Do not invent facts outside the material.
                """.formatted(request.getQuestionCount(), request.getDifficulty());

        String generated = materialRagVectorService.queryMaterial(
                MaterialSourceType.LIBRARY_MATERIAL,
                material.getId(),
                prompt,
                12
        );

        return QuizGeneratePayload.builder()
                .lessonId(null)
                .materialId(material.getId())
                .sourceFileName(material.getFileName())
                .questionCount(request.getQuestionCount())
                .generatedContent(generated)
                .build();
    }
}
