package com.mengsea.khmercodepath.api.classes.service;

import com.mengsea.khmercodepath.api.classes.payload.AssignStudentsRequest;
import com.mengsea.khmercodepath.api.classes.payload.ClassDetailPayload;
import com.mengsea.khmercodepath.api.classes.payload.ClassPagePayload;
import com.mengsea.khmercodepath.api.classes.payload.ClassSummaryPayload;
import com.mengsea.khmercodepath.api.classes.payload.CreateClassRequest;
import com.mengsea.khmercodepath.api.classes.payload.EnrollmentCountsPayload;
import com.mengsea.khmercodepath.api.classes.payload.LessonsSummaryPayload;
import com.mengsea.khmercodepath.api.classes.payload.RemoveStudentsRequest;
import com.mengsea.khmercodepath.api.classes.payload.UpdateClassRequest;
import com.mengsea.khmercodepath.api.users.mapper.UserAdminMapper;
import com.mengsea.khmercodepath.api.users.payload.UserDetailPayload;
import com.mengsea.khmercodepath.commons.constant.ClassStatus;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.ClassEnrollment;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.ClassEnrollmentRepository;
import com.mengsea.khmercodepath.commons.repository.LessonRepository;
import com.mengsea.khmercodepath.commons.repository.LmsClassRepository;
import com.mengsea.khmercodepath.commons.repository.LmsClassSpecifications;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import com.mengsea.khmercodepath.commons.security.ClassAccessHelper;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClassManagementServiceImpl implements ClassManagementService {

    private final LmsClassRepository lmsClassRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final UserAdminMapper userAdminMapper;
    private final ClassInvitationService classInvitationService;
    private final ClassAccessHelper classAccessHelper;

    @Override
    @Transactional(readOnly = true)
    public ClassPagePayload listClasses(
            String search,
            String teacherId,
            String semester,
            Integer academicYear,
            ClassStatus status,
            Pageable pageable
    ) {
        User me = SecurityUtils.requireCurrentUser();
        Specification<LmsClass> spec = Specification.where(LmsClassSpecifications.notDeleted())
                .and(LmsClassSpecifications.searchContains(search))
                .and(LmsClassSpecifications.semesterEquals(semester))
                .and(LmsClassSpecifications.academicYearEquals(academicYear))
                .and(LmsClassSpecifications.statusEquals(status));

        if (me.getRole() == Role.TEACHER) {
            spec = spec.and(LmsClassSpecifications.teacherUuidEquals(me.getUuid()));
        } else if (me.getRole() == Role.STUDENT) {
            spec = spec.and(LmsClassSpecifications.studentEnrolledEquals(me.getUuid()));
        } else if (me.getRole() == Role.ADMIN && teacherId != null && !teacherId.isBlank()) {
            spec = spec.and(LmsClassSpecifications.teacherUuidEquals(teacherId.trim()));
        }

        Page<LmsClass> page = lmsClassRepository.findAll(spec, pageable);
        List<ClassSummaryPayload> items = page.getContent().stream()
                .map(this::toSummary)
                .toList();

        return ClassPagePayload.builder()
                .items(items)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ClassDetailPayload getClass(Long id) {
        LmsClass entity = lmsClassRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.CLASS_NOT_FOUND));
        assertCanRead(entity);
        return toDetail(entity);
    }

    @Override
    @Transactional
    public ClassDetailPayload createClass(CreateClassRequest request) {
        String code = request.getCode().trim();
        if (lmsClassRepository.existsByCodeIgnoreCaseAndDeletedFalse(code)) {
            throw new BusinessException(ExceptionCode.CLASS_CODE_CONFLICT);
        }
        User teacher = requireTeacherUser(request.getTeacherId().trim());
        LmsClass entity = new LmsClass();
        entity.setCode(code);
        entity.setName(request.getName().trim());
        entity.setDescription(blankToNull(request.getDescription()));
        entity.setTeacher(teacher);
        entity.setSemester(blankToNull(request.getSemester()));
        entity.setAcademicYear(request.getAcademicYear());
        entity.setSchedule(blankToNull(request.getSchedule()));
        entity.setRoomNumber(blankToNull(request.getRoomNumber()));
        entity.setStatus(request.getStatus() != null ? request.getStatus() : ClassStatus.ACTIVE);
        entity.setDeleted(false);
        lmsClassRepository.save(entity);
        return toDetail(reloadWithTeacher(entity.getId()));
    }

    @Override
    @Transactional
    public ClassDetailPayload updateClass(Long id, UpdateClassRequest request) {
        LmsClass entity = lmsClassRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.CLASS_NOT_FOUND));

        if (request.getCode() != null && !request.getCode().isBlank()) {
            String newCode = request.getCode().trim();
            if (!newCode.equalsIgnoreCase(entity.getCode())
                    && lmsClassRepository.existsByCodeIgnoreCaseAndDeletedFalseAndIdNot(newCode, id)) {
                throw new BusinessException(ExceptionCode.CLASS_CODE_CONFLICT);
            }
            entity.setCode(newCode);
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            entity.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            entity.setDescription(blankToNull(request.getDescription()));
        }
        if (request.getTeacherId() != null && !request.getTeacherId().isBlank()) {
            entity.setTeacher(requireTeacherUser(request.getTeacherId().trim()));
        }
        if (request.getSemester() != null) {
            entity.setSemester(blankToNull(request.getSemester()));
        }
        if (request.getAcademicYear() != null) {
            entity.setAcademicYear(request.getAcademicYear());
        }
        if (request.getSchedule() != null) {
            entity.setSchedule(blankToNull(request.getSchedule()));
        }
        if (request.getRoomNumber() != null) {
            entity.setRoomNumber(blankToNull(request.getRoomNumber()));
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }

        lmsClassRepository.save(entity);
        return toDetail(reloadWithTeacher(id));
    }

    @Override
    @Transactional
    public void deleteClass(Long id) {
        LmsClass entity = lmsClassRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.CLASS_NOT_FOUND));
        long enrolled = classEnrollmentRepository.countByLmsClass_Id(id);
        long lessons = lessonRepository.countByLmsClass_IdAndDeletedFalse(id);
        if (enrolled > 0 || lessons > 0) {
            throw new BusinessException(ExceptionCode.CLASS_DELETE_NOT_ALLOWED);
        }
        entity.setDeleted(true);
        lmsClassRepository.save(entity);
    }

    @Override
    @Transactional
    public void assignStudents(Long id, AssignStudentsRequest request) {
        Set<String> ids = new LinkedHashSet<>();
        for (String sid : request.getStudentIds()) {
            if (sid != null && !sid.isBlank()) {
                ids.add(sid.trim());
            }
        }
        classInvitationService.inviteStudents(id, ids);
    }

    @Override
    @Transactional
    public void removeStudents(Long id, RemoveStudentsRequest request) {
        LmsClass entity = lmsClassRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.CLASS_NOT_FOUND));
        classAccessHelper.assertCanManageClass(entity);
        classEnrollmentRepository.deleteByLmsClass_IdAndStudent_UuidIn(id, request.getStudentIds());
        classInvitationService.cancelPendingInvitations(id, request.getStudentIds());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDetailPayload> listClassStudents(Long id) {
        LmsClass entity = lmsClassRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.CLASS_NOT_FOUND));
        assertCanRead(entity);
        return classEnrollmentRepository.findByLmsClass_IdOrderByEnrolledAtAsc(id).stream()
                .map(ClassEnrollment::getStudent)
                .map(userAdminMapper::toDetail)
                .toList();
    }

    private LmsClass reloadWithTeacher(Long id) {
        return lmsClassRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.CLASS_NOT_FOUND));
    }

    private void assertCanRead(LmsClass entity) {
        User me = SecurityUtils.requireCurrentUser();
        if (me.getRole() == Role.ADMIN) {
            return;
        }
        if (me.getRole() == Role.TEACHER && Objects.equals(me.getUuid(), entity.getTeacher().getUuid())) {
            return;
        }
        if (me.getRole() == Role.STUDENT
                && classEnrollmentRepository.existsByLmsClass_IdAndStudent_Uuid(entity.getId(), me.getUuid())) {
            return;
        }
        throw new BusinessException(ExceptionCode.ACCESS_DENIED);
    }

    private User requireTeacherUser(String uuid) {
        User u = userRepository.findByUuidAndDeletedFalse(uuid)
                .orElseThrow(() -> new BusinessException(ExceptionCode.TEACHER_NOT_FOUND));
        if (u.getRole() != Role.TEACHER) {
            throw new BusinessException(ExceptionCode.TEACHER_NOT_FOUND);
        }
        return u;
    }

    private List<User> loadStudents(Set<String> ids) {
        if (ids.isEmpty()) {
            throw new BusinessException(ExceptionCode.STUDENT_NOT_FOUND);
        }
        List<User> users = userRepository.findAllByUuidInAndDeletedFalse(ids);
        if (users.size() != ids.size()) {
            throw new BusinessException(ExceptionCode.STUDENT_NOT_FOUND);
        }
        for (User u : users) {
            if (u.getRole() != Role.STUDENT) {
                throw new BusinessException(ExceptionCode.STUDENT_NOT_FOUND);
            }
        }
        return users;
    }

    private ClassSummaryPayload toSummary(LmsClass c) {
        User t = c.getTeacher();
        long enrolled = classEnrollmentRepository.countByLmsClass_Id(c.getId());
        return ClassSummaryPayload.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .teacherId(t.getUuid())
                .teacherName(t.getUsername())
                .semester(c.getSemester())
                .academicYear(c.getAcademicYear())
                .status(c.getStatus())
                .enrolledCount(enrolled)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private ClassDetailPayload toDetail(LmsClass c) {
        long enrolled = classEnrollmentRepository.countByLmsClass_Id(c.getId());
        long lessonTotal = lessonRepository.countByLmsClass_IdAndDeletedFalse(c.getId());
        return ClassDetailPayload.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .description(c.getDescription())
                .teacher(userAdminMapper.toDetail(c.getTeacher()))
                .semester(c.getSemester())
                .academicYear(c.getAcademicYear())
                .schedule(c.getSchedule())
                .roomNumber(c.getRoomNumber())
                .status(c.getStatus())
                .enrollment(EnrollmentCountsPayload.builder().enrolled(enrolled).build())
                .lessons(LessonsSummaryPayload.builder().total(lessonTotal).build())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
