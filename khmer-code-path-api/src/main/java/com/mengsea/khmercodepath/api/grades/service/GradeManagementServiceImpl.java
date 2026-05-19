package com.mengsea.khmercodepath.api.grades.service;

import com.mengsea.khmercodepath.api.grades.GradeLetterUtil;
import com.mengsea.khmercodepath.api.grades.payload.CreateGradeRequest;
import com.mengsea.khmercodepath.api.grades.payload.FinalGradePayload;
import com.mengsea.khmercodepath.api.grades.payload.GradePayload;
import com.mengsea.khmercodepath.api.grades.payload.GradebookPayload;
import com.mengsea.khmercodepath.api.grades.payload.GradebookRowPayload;
import com.mengsea.khmercodepath.api.grades.payload.UpdateGradeRequest;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.domain.ClassEnrollment;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.StudentGrade;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.ClassEnrollmentRepository;
import com.mengsea.khmercodepath.commons.repository.StudentGradeRepository;
import com.mengsea.khmercodepath.api.notifications.service.NotificationPublisher;
import com.mengsea.khmercodepath.commons.security.ClassAccessHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeManagementServiceImpl implements GradeManagementService {

    private final StudentGradeRepository studentGradeRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final ClassAccessHelper classAccessHelper;
    private final NotificationPublisher notificationPublisher;

    @Override
    @Transactional
    public GradePayload createGrade(CreateGradeRequest request) {
        LmsClass lmsClass = classAccessHelper.requireReadableClass(request.getClassId());
        classAccessHelper.assertCanManageClass(lmsClass);
        User student = classAccessHelper.requireStudent(request.getStudentId().trim());
        if (!classEnrollmentRepository.existsByLmsClass_IdAndStudent_Uuid(
                lmsClass.getId(), student.getUuid())) {
            throw new BusinessException(ExceptionCode.STUDENT_NOT_FOUND);
        }
        StudentGrade entity = studentGradeRepository
                .findFirstByLmsClass_IdAndStudent_UuidOrderByCreatedAtDesc(
                        lmsClass.getId(), student.getUuid())
                .orElseGet(StudentGrade::new);
        entity.setLmsClass(lmsClass);
        entity.setStudent(student);
        applyNumericGrade(entity, request.getNumericGrade(), request.getLetterGrade());
        studentGradeRepository.save(entity);
        notificationPublisher.onGradePosted(lmsClass, student, entity.getLetterGrade(), entity.getNumericGrade());
        return toPayload(entity);
    }

    @Override
    @Transactional
    public GradePayload updateGrade(Long gradeId, UpdateGradeRequest request) {
        StudentGrade entity = studentGradeRepository.findById(gradeId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.GRADE_NOT_FOUND));
        classAccessHelper.assertCanManageClass(entity.getLmsClass());
        if (request.getNumericGrade() != null || request.getLetterGrade() != null) {
            applyNumericGrade(
                    entity,
                    request.getNumericGrade() != null ? request.getNumericGrade() : entity.getNumericGrade(),
                    request.getLetterGrade()
            );
        }
        studentGradeRepository.save(entity);
        notificationPublisher.onGradePosted(
                entity.getLmsClass(),
                entity.getStudent(),
                entity.getLetterGrade(),
                entity.getNumericGrade()
        );
        return toPayload(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public GradebookPayload getGradebook(Long classId) {
        LmsClass lmsClass = classAccessHelper.requireReadableClass(classId);
        List<GradebookRowPayload> rows = classEnrollmentRepository
                .findByLmsClass_IdOrderByEnrolledAtAsc(classId)
                .stream()
                .map(enrollment -> toGradebookRow(enrollment, classId))
                .toList();
        return GradebookPayload.builder()
                .classId(classId)
                .className(lmsClass.getName())
                .rows(rows)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradePayload> getStudentGrades(Long classId, String studentId) {
        classAccessHelper.assertCanViewStudentProgress(studentId);
        classAccessHelper.requireReadableClass(classId);
        return studentGradeRepository
                .findByStudent_UuidAndLmsClass_IdOrderByCreatedAtDesc(studentId, classId)
                .stream()
                .map(this::toPayload)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FinalGradePayload calculateFinalGrade(Long classId, String studentId) {
        classAccessHelper.assertCanViewStudentProgress(studentId);
        LmsClass lmsClass = classAccessHelper.requireReadableClass(classId);
        StudentGrade grade = studentGradeRepository
                .findFirstByLmsClass_IdAndStudent_UuidOrderByCreatedAtDesc(classId, studentId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.GRADE_NOT_FOUND));
        return FinalGradePayload.builder()
                .classId(classId)
                .studentId(studentId)
                .numericGrade(grade.getNumericGrade())
                .letterGrade(grade.getLetterGrade())
                .build();
    }

    private GradebookRowPayload toGradebookRow(ClassEnrollment enrollment, Long classId) {
        User student = enrollment.getStudent();
        return studentGradeRepository
                .findFirstByLmsClass_IdAndStudent_UuidOrderByCreatedAtDesc(classId, student.getUuid())
                .map(grade -> GradebookRowPayload.builder()
                        .studentId(student.getUuid())
                        .studentName(student.getUsername())
                        .gradeId(grade.getId())
                        .numericGrade(grade.getNumericGrade())
                        .letterGrade(grade.getLetterGrade())
                        .build())
                .orElseGet(() -> GradebookRowPayload.builder()
                        .studentId(student.getUuid())
                        .studentName(student.getUsername())
                        .build());
    }

    private void applyNumericGrade(StudentGrade entity, java.math.BigDecimal numeric, String letter) {
        entity.setNumericGrade(numeric);
        if (letter != null && !letter.isBlank()) {
            entity.setLetterGrade(letter.trim());
        } else {
            entity.setLetterGrade(GradeLetterUtil.toLetter(numeric));
        }
    }

    private GradePayload toPayload(StudentGrade entity) {
        return GradePayload.builder()
                .id(entity.getId())
                .classId(entity.getLmsClass().getId())
                .className(entity.getLmsClass().getName())
                .studentId(entity.getStudent().getUuid())
                .studentName(entity.getStudent().getUsername())
                .numericGrade(entity.getNumericGrade())
                .letterGrade(entity.getLetterGrade())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
