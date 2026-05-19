package com.mengsea.khmercodepath.commons.security;

import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.ClassEnrollmentRepository;
import com.mengsea.khmercodepath.commons.repository.LmsClassRepository;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ClassAccessHelper {

    private final LmsClassRepository lmsClassRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final UserRepository userRepository;

    public LmsClass requireReadableClass(Long classId) {
        LmsClass entity = lmsClassRepository.findByIdAndDeletedFalse(classId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.CLASS_NOT_FOUND));
        assertCanRead(entity);
        return entity;
    }

    public void assertCanRead(LmsClass entity) {
        User me = SecurityUtils.requireCurrentUser();
        if (me.getRole() == Role.ADMIN) {
            return;
        }
        if (me.getRole() == Role.TEACHER
                && Objects.equals(me.getUuid(), entity.getTeacher().getUuid())) {
            return;
        }
        if (me.getRole() == Role.STUDENT
                && classEnrollmentRepository.existsByLmsClass_IdAndStudent_Uuid(
                entity.getId(), me.getUuid())) {
            return;
        }
        throw new BusinessException(ExceptionCode.ACCESS_DENIED);
    }

    public void assertCanManageClass(LmsClass entity) {
        User me = SecurityUtils.requireCurrentUser();
        if (me.getRole() == Role.ADMIN) {
            return;
        }
        if (me.getRole() == Role.TEACHER
                && Objects.equals(me.getUuid(), entity.getTeacher().getUuid())) {
            return;
        }
        throw new BusinessException(ExceptionCode.ACCESS_DENIED);
    }

    public User requireStudent(String studentUuid) {
        User student = userRepository.findByUuidAndDeletedFalse(studentUuid)
                .orElseThrow(() -> new BusinessException(ExceptionCode.STUDENT_NOT_FOUND));
        if (student.getRole() != Role.STUDENT) {
            throw new BusinessException(ExceptionCode.STUDENT_NOT_FOUND);
        }
        return student;
    }

    public void assertCanViewStudentProgress(String studentUuid) {
        User me = SecurityUtils.requireCurrentUser();
        if (me.getRole() == Role.ADMIN) {
            return;
        }
        if (me.getRole() == Role.STUDENT && Objects.equals(me.getUuid(), studentUuid)) {
            return;
        }
        if (me.getRole() == Role.TEACHER
                && classEnrollmentRepository.existsByStudent_UuidAndLmsClass_Teacher_Uuid(
                studentUuid, me.getUuid())) {
            return;
        }
        throw new BusinessException(ExceptionCode.ACCESS_DENIED);
    }
}
