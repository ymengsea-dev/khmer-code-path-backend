package com.mengsea.khmercodepath.api.classes.service;

import com.mengsea.khmercodepath.api.classes.config.ClassUiConstants;
import com.mengsea.khmercodepath.api.classes.payload.PublicCourseSummaryPayload;
import com.mengsea.khmercodepath.api.classes.payload.PublicCoursesConfigPayload;
import com.mengsea.khmercodepath.api.classes.payload.PublicCoursesPagePayload;
import com.mengsea.khmercodepath.commons.constant.ClassStatus;
import com.mengsea.khmercodepath.commons.constant.ClassVisibility;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.ClassEnrollment;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.School;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.ClassEnrollmentRepository;
import com.mengsea.khmercodepath.commons.repository.LmsClassRepository;
import com.mengsea.khmercodepath.commons.repository.LmsClassSpecifications;
import com.mengsea.khmercodepath.commons.repository.SchoolRepository;
import com.mengsea.khmercodepath.commons.security.SchoolAccessHelper;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PublicCoursesServiceImpl implements PublicCoursesService {

    private final LmsClassRepository lmsClassRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final SchoolAccessHelper schoolAccessHelper;
    private final SchoolRepository schoolRepository;

    @Override
    @Transactional(readOnly = true)
    public PublicCoursesConfigPayload getConfig() {
        User me = SecurityUtils.requireCurrentUser();
        School school = schoolRepository.findById(schoolAccessHelper.requireSchoolId(me))
                .orElseThrow(() -> new BusinessException(ExceptionCode.SCHOOL_NOT_FOUND));
        return PublicCoursesConfigPayload.builder()
                .enabled(school.isPublicCoursesEnabled())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PublicCoursesPagePayload listPublicCourses(String search, Pageable pageable) {
        User me = SecurityUtils.requireCurrentUser();
        if (me.getRole() != Role.STUDENT) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }
        Long schoolId = schoolAccessHelper.requireSchoolId(me);
        assertPublicCoursesEnabled(schoolId);

        Specification<LmsClass> spec = Specification
                .where(LmsClassSpecifications.publicActiveInSchool(schoolId))
                .and(LmsClassSpecifications.searchContains(search));

        Page<LmsClass> page = lmsClassRepository.findAll(spec, pageable);
        Set<Long> enrolledClassIds = loadEnrolledClassIds(me.getUuid(), page.getContent());

        List<PublicCourseSummaryPayload> items = new ArrayList<>();
        int index = 0;
        for (LmsClass entity : page.getContent()) {
            items.add(toSummary(entity, index++, enrolledClassIds.contains(entity.getId())));
        }

        return PublicCoursesPagePayload.builder()
                .items(items)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .build();
    }

    @Override
    @Transactional
    public void selfEnroll(Long classId) {
        User me = SecurityUtils.requireCurrentUser();
        if (me.getRole() != Role.STUDENT) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }

        LmsClass lmsClass = lmsClassRepository.findByIdAndDeletedFalse(classId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.CLASS_NOT_FOUND));

        schoolAccessHelper.assertSameSchool(me, lmsClass);
        assertPublicCoursesEnabled(lmsClass.getSchool().getId());

        if (lmsClass.getVisibility() != ClassVisibility.PUBLIC) {
            throw new BusinessException(ExceptionCode.CLASS_NOT_PUBLIC);
        }
        if (lmsClass.getStatus() != ClassStatus.ACTIVE) {
            throw new BusinessException(ExceptionCode.CLASS_NOT_PUBLIC);
        }

        if (classEnrollmentRepository.existsByLmsClass_IdAndStudent_Uuid(classId, me.getUuid())) {
            throw new BusinessException(ExceptionCode.STUDENT_ALREADY_ENROLLED);
        }

        ClassEnrollment enrollment = new ClassEnrollment();
        enrollment.setLmsClass(lmsClass);
        enrollment.setStudent(me);
        classEnrollmentRepository.save(enrollment);
    }

    private void assertPublicCoursesEnabled(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.SCHOOL_NOT_FOUND));
        if (!school.isPublicCoursesEnabled()) {
            throw new BusinessException(ExceptionCode.PUBLIC_COURSES_DISABLED);
        }
    }

    private Set<Long> loadEnrolledClassIds(String studentUuid, List<LmsClass> classes) {
        Set<Long> ids = new HashSet<>();
        if (classes.isEmpty()) {
            return ids;
        }
        for (LmsClass c : classes) {
            if (classEnrollmentRepository.existsByLmsClass_IdAndStudent_Uuid(c.getId(), studentUuid)) {
                ids.add(c.getId());
            }
        }
        return ids;
    }

    private PublicCourseSummaryPayload toSummary(LmsClass c, int cardIndex, boolean enrolled) {
        User t = c.getTeacher();
        long enrolledCount = classEnrollmentRepository.countByLmsClass_Id(c.getId());
        List<String> gradients = ClassUiConstants.CARD_GRADIENTS;
        String gradient = gradients.isEmpty()
                ? "from-violet-600 to-fuchsia-700"
                : gradients.get(Math.floorMod(cardIndex, gradients.size()));

        return PublicCourseSummaryPayload.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .description(c.getDescription())
                .teacherId(t.getUuid())
                .teacherName(t.getUsername())
                .semester(c.getSemester())
                .academicYear(c.getAcademicYear())
                .semesterLabel(formatSemesterLabel(c.getSemester(), c.getAcademicYear()))
                .status(c.getStatus())
                .visibility(c.getVisibility())
                .cardGradient(gradient)
                .enrolledCount(enrolledCount)
                .enrolled(enrolled)
                .build();
    }

    private static String formatSemesterLabel(String semester, Integer academicYear) {
        if (semester == null || semester.isBlank()) {
            return academicYear != null ? String.valueOf(academicYear) : "—";
        }
        if (academicYear != null) {
            return semester.trim() + ", " + academicYear;
        }
        return semester.trim();
    }
}
