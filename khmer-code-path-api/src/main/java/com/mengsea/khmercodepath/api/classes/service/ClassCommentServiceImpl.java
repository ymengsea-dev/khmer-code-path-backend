package com.mengsea.khmercodepath.api.classes.service;

import com.mengsea.khmercodepath.api.classes.payload.ClassCommentPayload;
import com.mengsea.khmercodepath.api.classes.payload.CreateClassCommentRequest;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.ClassComment;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.ClassCommentRepository;
import com.mengsea.khmercodepath.commons.repository.ClassEnrollmentRepository;
import com.mengsea.khmercodepath.commons.repository.LmsClassRepository;
import com.mengsea.khmercodepath.api.notifications.service.NotificationPublisher;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassCommentServiceImpl implements ClassCommentService {

    private final LmsClassRepository lmsClassRepository;
    private final ClassCommentRepository classCommentRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final NotificationPublisher notificationPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<ClassCommentPayload> listComments(Long classId) {
        LmsClass lmsClass = requireReadableClass(classId);
        assertCanReadComments(lmsClass);
        return classCommentRepository.findByLmsClass_IdAndDeletedFalseOrderByCreatedAtDesc(classId)
                .stream()
                .map(this::toPayload)
                .toList();
    }

    @Override
    @Transactional
    public ClassCommentPayload createComment(Long classId, CreateClassCommentRequest request) {
        LmsClass lmsClass = requireReadableClass(classId);
        User me = SecurityUtils.requireCurrentUser();
        assertCanPostComment(lmsClass, me);

        ClassComment comment = new ClassComment();
        comment.setLmsClass(lmsClass);
        comment.setAuthor(me);
        comment.setBody(request.getBody().trim());
        ClassComment saved = classCommentRepository.save(comment);
        notificationPublisher.onClassQuestion(classId, me, saved.getBody());
        return toPayload(saved);
    }

    @Override
    public ClassCommentPayload toPayload(ClassComment comment) {
        User author = comment.getAuthor();
        LmsClass lmsClass = comment.getLmsClass();
        return ClassCommentPayload.builder()
                .id(comment.getId())
                .classId(lmsClass.getId())
                .className(lmsClass.getName())
                .authorId(author.getUuid())
                .authorName(author.getUsername())
                .authorRole(author.getRole() != null ? author.getRole().name() : null)
                .body(comment.getBody())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private LmsClass requireReadableClass(Long classId) {
        return lmsClassRepository.findByIdAndDeletedFalse(classId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.CLASS_NOT_FOUND));
    }

    private void assertCanReadComments(LmsClass lmsClass) {
        User me = SecurityUtils.requireCurrentUser();
        if (me.getRole() == Role.ADMIN) {
            return;
        }
        if (me.getRole() == Role.TEACHER && lmsClass.getTeacher().getUuid().equals(me.getUuid())) {
            return;
        }
        if (me.getRole() == Role.STUDENT
                && classEnrollmentRepository.existsByLmsClass_IdAndStudent_Uuid(lmsClass.getId(), me.getUuid())) {
            return;
        }
        throw new BusinessException(ExceptionCode.ACCESS_DENIED);
    }

    private void assertCanPostComment(LmsClass lmsClass, User me) {
        if (me.getRole() == Role.ADMIN) {
            return;
        }
        if (me.getRole() == Role.TEACHER && lmsClass.getTeacher().getUuid().equals(me.getUuid())) {
            return;
        }
        if (me.getRole() == Role.STUDENT
                && classEnrollmentRepository.existsByLmsClass_IdAndStudent_Uuid(lmsClass.getId(), me.getUuid())) {
            return;
        }
        throw new BusinessException(ExceptionCode.ACCESS_DENIED);
    }
}
