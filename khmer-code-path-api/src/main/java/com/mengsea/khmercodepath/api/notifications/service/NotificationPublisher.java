package com.mengsea.khmercodepath.api.notifications.service;

import com.mengsea.khmercodepath.api.notifications.payload.NotificationPayload;
import com.mengsea.khmercodepath.api.notifications.sse.NotificationSseHub;
import com.mengsea.khmercodepath.commons.constant.NotificationType;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.ClassEnrollment;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.repository.ClassEnrollmentRepository;
import com.mengsea.khmercodepath.commons.repository.LmsClassRepository;
import com.mengsea.khmercodepath.commons.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Persists notifications and pushes them to connected SSE clients.
 */
@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final NotificationSseHub notificationSseHub;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final LmsClassRepository lmsClassRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyUser(
            String userUuid,
            NotificationType type,
            String title,
            String message,
            Long classId,
            String resourceType,
            Long resourceId
    ) {
        NotificationPayload payload = notificationService.createForUser(
                userUuid,
                type,
                title,
                message,
                classId,
                resourceType,
                resourceId
        );
        long unread = notificationRepository.countByUser_UuidAndDeletedFalseAndReadFalse(userUuid);
        notificationSseHub.publishNotification(userUuid, payload, unread);
    }

    public void notifyClassStudents(
            Long classId,
            String excludeUserUuid,
            NotificationType type,
            String title,
            String message,
            String resourceType,
            Long resourceId
    ) {
        List<ClassEnrollment> enrollments = classEnrollmentRepository.findByLmsClass_IdOrderByEnrolledAtAsc(classId);
        for (ClassEnrollment enrollment : enrollments) {
            String studentUuid = enrollment.getStudent().getUuid();
            if (excludeUserUuid != null && excludeUserUuid.equals(studentUuid)) {
                continue;
            }
            notifyUser(studentUuid, type, title, message, classId, resourceType, resourceId);
        }
    }

    public void notifyClassTeacher(
            LmsClass lmsClass,
            String excludeUserUuid,
            NotificationType type,
            String title,
            String message,
            String resourceType,
            Long resourceId
    ) {
        User teacher = lmsClass.getTeacher();
        if (teacher == null) {
            return;
        }
        String teacherUuid = teacher.getUuid();
        if (excludeUserUuid != null && excludeUserUuid.equals(teacherUuid)) {
            return;
        }
        notifyUser(teacherUuid, type, title, message, lmsClass.getId(), resourceType, resourceId);
    }

    public void onGradePosted(LmsClass lmsClass, User student, String letterGrade, java.math.BigDecimal numericGrade) {
        String gradeLabel = letterGrade != null && !letterGrade.isBlank()
                ? letterGrade
                : numericGrade != null ? numericGrade.toPlainString() : "updated";
        notifyUser(
                student.getUuid(),
                NotificationType.GRADE_POSTED,
                "Grade posted",
                "Your grade for " + lmsClass.getName() + " was updated to " + gradeLabel + ".",
                lmsClass.getId(),
                "grade",
                null
        );
    }

    public void onAttendanceRecorded(LmsClass lmsClass, User student, String status, java.time.LocalDate sessionDate) {
        notifyUser(
                student.getUuid(),
                NotificationType.ATTENDANCE_RECORDED,
                "Attendance recorded",
                "Attendance for " + lmsClass.getName() + " on " + sessionDate + " was marked as " + status + ".",
                lmsClass.getId(),
                "attendance",
                null
        );
    }

    public void onClassQuestion(Long classId, User author, String bodyPreview) {
        LmsClass lmsClass = lmsClassRepository.findByIdAndDeletedFalse(classId).orElse(null);
        if (lmsClass == null) {
            return;
        }
        String preview = bodyPreview.length() > 120 ? bodyPreview.substring(0, 117) + "..." : bodyPreview;
        if (author.getRole() == Role.STUDENT) {
            notifyClassTeacher(
                    lmsClass,
                    author.getUuid(),
                    NotificationType.CLASS_QUESTION,
                    "New class question",
                    author.getUsername() + " asked: \"" + preview + "\"",
                    "comment",
                    null
            );
        } else {
            notifyClassStudents(
                    lmsClass.getId(),
                    author.getUuid(),
                    NotificationType.CLASS_QUESTION,
                    "New class announcement",
                    author.getUsername() + " posted: \"" + preview + "\"",
                    "comment",
                    null
            );
        }
    }

    public void onClassInvitation(
            String studentUuid,
            LmsClass lmsClass,
            String teacherName,
            Long invitationId
    ) {
        notifyUser(
                studentUuid,
                NotificationType.CLASS_INVITATION,
                "Class invitation",
                teacherName + " invited you to join " + lmsClass.getName() + " (" + lmsClass.getCode() + ").",
                lmsClass.getId(),
                "class_invitation",
                invitationId
        );
    }

    public void onLessonPublished(LmsClass lmsClass, String lessonTitle, Long lessonId, String excludeUserUuid) {
        notifyClassStudents(
                lmsClass.getId(),
                excludeUserUuid,
                NotificationType.LESSON_PUBLISHED,
                "New lesson available",
                "Lesson \"" + lessonTitle + "\" is now available in " + lmsClass.getName() + ".",
                "lesson",
                lessonId
        );
    }
}
