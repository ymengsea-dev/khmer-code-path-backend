package com.mengsea.khmercodepath.api.assignmentsexams.service;

import com.mengsea.khmercodepath.api.assignmentsexams.payload.TaskContentUploadResponse;
import com.mengsea.khmercodepath.api.storage.UploadStorage;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.domain.Assignment;
import com.mengsea.khmercodepath.commons.domain.Exam;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.AssignmentRepository;
import com.mengsea.khmercodepath.commons.repository.ClassEnrollmentRepository;
import com.mengsea.khmercodepath.commons.repository.ExamRepository;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AssignmentsExamsContentService {

    private final UploadStorage uploadStorage;
    private final TaskContentBlockService taskContentBlockService;
    private final AssignmentRepository assignmentRepository;
    private final ExamRepository examRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;

    @Transactional
    public TaskContentUploadResponse uploadTaskFile(MultipartFile file) {
        String teacherUuid = SecurityUtils.requireCurrentUser().getUuid();
        long ownerKey = TaskContentBlockService.ownerKeyForTeacher(teacherUuid);
        UploadStorage.StoredFile stored = uploadStorage.store("task-content", ownerKey, file);
        return TaskContentUploadResponse.builder()
                .storageKey(stored.storageKey())
                .fileName(stored.fileName())
                .contentType(stored.contentType())
                .sizeBytes(stored.sizeBytes())
                .build();
    }

    @Transactional(readOnly = true)
    public Resource downloadTaskFile(Long assignmentId, Long examId, String storageKey) {
        String userUuid = SecurityUtils.requireCurrentUser().getUuid();
        assertFileAccess(assignmentId, examId, storageKey, userUuid);
        return uploadStorage.loadAsResource(storageKey);
    }

    @Transactional(readOnly = true)
    public Resource downloadLibraryMaterial(
            Long assignmentId,
            Long examId,
            Long libraryItemId,
            Long materialId
    ) {
        String userUuid = SecurityUtils.requireCurrentUser().getUuid();
        assertLibraryAccess(assignmentId, examId, libraryItemId, materialId, userUuid);
        String storageKey = taskContentBlockService.resolveLibraryMaterialStorageKey(
                libraryItemId, materialId);
        return uploadStorage.loadAsResource(storageKey);
    }

    private void assertFileAccess(Long assignmentId, Long examId, String storageKey, String userUuid) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new BusinessException(ExceptionCode.MATERIAL_NOT_FOUND);
        }
        if (assignmentId != null) {
            Assignment assignment = assignmentRepository.findByIdAndDeletedFalse(assignmentId)
                    .orElseThrow(() -> new BusinessException(ExceptionCode.ASSIGNMENT_NOT_FOUND));
            assertEnrollmentOrTeacher(assignment.getLmsClass().getId(),
                    assignment.getLmsClass().getTeacher().getUuid(), userUuid);
            if (!taskContentBlockService.referencesStorageKey(
                    assignment.getContentBlocksJson(), storageKey)) {
                throw new BusinessException(ExceptionCode.ACCESS_DENIED);
            }
            return;
        }
        if (examId != null) {
            Exam exam = examRepository.findByIdAndDeletedFalse(examId)
                    .orElseThrow(() -> new BusinessException(ExceptionCode.EXAM_NOT_FOUND));
            assertEnrollmentOrTeacher(exam.getLmsClass().getId(),
                    exam.getLmsClass().getTeacher().getUuid(), userUuid);
            if (!taskContentBlockService.referencesStorageKey(exam.getContentBlocksJson(), storageKey)) {
                throw new BusinessException(ExceptionCode.ACCESS_DENIED);
            }
            return;
        }
        throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
    }

    private void assertLibraryAccess(
            Long assignmentId,
            Long examId,
            Long libraryItemId,
            Long materialId,
            String userUuid
    ) {
        if (assignmentId != null) {
            Assignment assignment = assignmentRepository.findByIdAndDeletedFalse(assignmentId)
                    .orElseThrow(() -> new BusinessException(ExceptionCode.ASSIGNMENT_NOT_FOUND));
            assertEnrollmentOrTeacher(assignment.getLmsClass().getId(),
                    assignment.getLmsClass().getTeacher().getUuid(), userUuid);
            if (!taskContentBlockService.referencesLibraryMaterial(
                    assignment.getContentBlocksJson(), libraryItemId, materialId)) {
                throw new BusinessException(ExceptionCode.ACCESS_DENIED);
            }
            return;
        }
        if (examId != null) {
            Exam exam = examRepository.findByIdAndDeletedFalse(examId)
                    .orElseThrow(() -> new BusinessException(ExceptionCode.EXAM_NOT_FOUND));
            assertEnrollmentOrTeacher(exam.getLmsClass().getId(),
                    exam.getLmsClass().getTeacher().getUuid(), userUuid);
            if (!taskContentBlockService.referencesLibraryMaterial(
                    exam.getContentBlocksJson(), libraryItemId, materialId)) {
                throw new BusinessException(ExceptionCode.ACCESS_DENIED);
            }
            return;
        }
        throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
    }

    private void assertEnrollmentOrTeacher(Long classId, String teacherUuid, String userUuid) {
        if (teacherUuid.equals(userUuid)) {
            return;
        }
        if (!classEnrollmentRepository.existsByLmsClass_IdAndStudent_Uuid(classId, userUuid)) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }
    }
}
