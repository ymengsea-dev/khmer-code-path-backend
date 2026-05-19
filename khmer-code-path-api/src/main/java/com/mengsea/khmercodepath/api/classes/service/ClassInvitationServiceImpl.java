package com.mengsea.khmercodepath.api.classes.service;

import com.mengsea.khmercodepath.api.classes.payload.ClassInvitationPayload;
import com.mengsea.khmercodepath.api.notifications.service.NotificationPublisher;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.InvitationStatus;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.ClassEnrollment;
import com.mengsea.khmercodepath.commons.domain.ClassInvitation;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.ClassEnrollmentRepository;
import com.mengsea.khmercodepath.commons.repository.ClassInvitationRepository;
import com.mengsea.khmercodepath.commons.repository.LmsClassRepository;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import com.mengsea.khmercodepath.commons.security.ClassAccessHelper;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClassInvitationServiceImpl implements ClassInvitationService {

    private final LmsClassRepository lmsClassRepository;
    private final ClassInvitationRepository classInvitationRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final UserRepository userRepository;
    private final ClassAccessHelper classAccessHelper;
    private final NotificationPublisher notificationPublisher;

    @Override
    @Transactional
    public void inviteStudents(Long classId, Set<String> studentIds) {
        LmsClass lmsClass = lmsClassRepository.findByIdAndDeletedFalse(classId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.CLASS_NOT_FOUND));
        classAccessHelper.assertCanManageClass(lmsClass);
        User inviter = SecurityUtils.requireCurrentUser();

        if (studentIds == null || studentIds.isEmpty()) {
            throw new BusinessException(ExceptionCode.STUDENT_NOT_FOUND);
        }

        List<User> students = loadStudents(studentIds);
        for (User student : students) {
            if (classEnrollmentRepository.existsByLmsClass_IdAndStudent_Uuid(classId, student.getUuid())) {
                continue;
            }
            if (classInvitationRepository.existsByLmsClass_IdAndStudent_UuidAndStatus(
                    classId, student.getUuid(), InvitationStatus.PENDING)) {
                continue;
            }

            ClassInvitation invitation = new ClassInvitation();
            invitation.setLmsClass(lmsClass);
            invitation.setStudent(student);
            invitation.setInvitedBy(inviter);
            invitation.setStatus(InvitationStatus.PENDING);
            classInvitationRepository.save(invitation);

            String teacherName = lmsClass.getTeacher() != null
                    ? lmsClass.getTeacher().getUsername()
                    : inviter.getUsername();
            notificationPublisher.onClassInvitation(
                    student.getUuid(),
                    lmsClass,
                    teacherName,
                    invitation.getId()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassInvitationPayload> listMyPendingInvitations() {
        User me = SecurityUtils.requireCurrentUser();
        if (me.getRole() != Role.STUDENT) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }
        return classInvitationRepository
                .findByStudent_UuidAndStatusOrderByCreatedAtDesc(me.getUuid(), InvitationStatus.PENDING)
                .stream()
                .map(this::toPayload)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassInvitationPayload> listPendingInvitationsForClass(Long classId) {
        LmsClass lmsClass = lmsClassRepository.findByIdAndDeletedFalse(classId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.CLASS_NOT_FOUND));
        classAccessHelper.assertCanManageClass(lmsClass);
        return classInvitationRepository
                .findByLmsClass_IdAndStatusOrderByCreatedAtDesc(classId, InvitationStatus.PENDING)
                .stream()
                .map(this::toPayload)
                .toList();
    }

    @Override
    @Transactional
    public ClassInvitationPayload acceptInvitation(Long invitationId) {
        User me = SecurityUtils.requireCurrentUser();
        if (me.getRole() != Role.STUDENT) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }

        ClassInvitation invitation = classInvitationRepository
                .findByIdAndStudent_Uuid(invitationId, me.getUuid())
                .orElseThrow(() -> new BusinessException(ExceptionCode.INVITATION_NOT_FOUND));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BusinessException(ExceptionCode.INVITATION_ALREADY_RESPONDED);
        }

        LmsClass lmsClass = invitation.getLmsClass();
        if (classEnrollmentRepository.existsByLmsClass_IdAndStudent_Uuid(lmsClass.getId(), me.getUuid())) {
            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitation.setRespondedAt(LocalDateTime.now());
            classInvitationRepository.save(invitation);
            return toPayload(invitation);
        }

        ClassEnrollment enrollment = new ClassEnrollment();
        enrollment.setLmsClass(lmsClass);
        enrollment.setStudent(me);
        classEnrollmentRepository.save(enrollment);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setRespondedAt(LocalDateTime.now());
        classInvitationRepository.save(invitation);

        return toPayload(invitation);
    }

    @Override
    @Transactional
    public ClassInvitationPayload declineInvitation(Long invitationId) {
        User me = SecurityUtils.requireCurrentUser();
        if (me.getRole() != Role.STUDENT) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }

        ClassInvitation invitation = classInvitationRepository
                .findByIdAndStudent_Uuid(invitationId, me.getUuid())
                .orElseThrow(() -> new BusinessException(ExceptionCode.INVITATION_NOT_FOUND));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BusinessException(ExceptionCode.INVITATION_ALREADY_RESPONDED);
        }

        invitation.setStatus(InvitationStatus.DECLINED);
        invitation.setRespondedAt(LocalDateTime.now());
        classInvitationRepository.save(invitation);
        return toPayload(invitation);
    }

    @Override
    @Transactional
    public void cancelPendingInvitations(Long classId, List<String> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return;
        }
        classInvitationRepository.cancelPendingForStudents(
                classId,
                studentIds,
                InvitationStatus.PENDING,
                InvitationStatus.CANCELLED
        );
    }

    private List<User> loadStudents(Set<String> ids) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String id : ids) {
            if (id != null && !id.isBlank()) {
                normalized.add(id.trim());
            }
        }
        if (normalized.isEmpty()) {
            throw new BusinessException(ExceptionCode.STUDENT_NOT_FOUND);
        }
        List<User> users = userRepository.findAllByUuidInAndDeletedFalse(normalized);
        if (users.size() != normalized.size()) {
            throw new BusinessException(ExceptionCode.STUDENT_NOT_FOUND);
        }
        for (User u : users) {
            if (u.getRole() != Role.STUDENT) {
                throw new BusinessException(ExceptionCode.STUDENT_NOT_FOUND);
            }
        }
        return users;
    }

    private ClassInvitationPayload toPayload(ClassInvitation invitation) {
        LmsClass lmsClass = invitation.getLmsClass();
        User student = invitation.getStudent();
        User teacher = lmsClass.getTeacher();
        return ClassInvitationPayload.builder()
                .id(invitation.getId())
                .classId(lmsClass.getId())
                .className(lmsClass.getName())
                .classCode(lmsClass.getCode())
                .teacherName(teacher != null ? teacher.getUsername() : null)
                .studentId(student.getUuid())
                .studentName(student.getUsername())
                .invitedByName(invitation.getInvitedBy().getUsername())
                .status(invitation.getStatus())
                .createdAt(invitation.getCreatedAt())
                .build();
    }
}
