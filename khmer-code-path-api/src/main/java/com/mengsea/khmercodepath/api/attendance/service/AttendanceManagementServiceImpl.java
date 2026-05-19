package com.mengsea.khmercodepath.api.attendance.service;

import com.mengsea.khmercodepath.api.attendance.AttendanceSessionIds;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceRecordPayload;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceSessionPayload;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceStatisticsPayload;
import com.mengsea.khmercodepath.api.attendance.payload.BulkAttendanceRequest;
import com.mengsea.khmercodepath.api.attendance.payload.RecordAttendanceRequest;
import com.mengsea.khmercodepath.api.attendance.payload.UpdateAttendanceRequest;
import com.mengsea.khmercodepath.commons.constant.AttendanceStatus;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.AttendanceRecord;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.AttendanceRecordRepository;
import com.mengsea.khmercodepath.commons.repository.ClassEnrollmentRepository;
import com.mengsea.khmercodepath.api.notifications.service.NotificationPublisher;
import com.mengsea.khmercodepath.commons.security.ClassAccessHelper;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceManagementServiceImpl implements AttendanceManagementService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final ClassAccessHelper classAccessHelper;
    private final NotificationPublisher notificationPublisher;

    @Override
    @Transactional
    public AttendanceRecordPayload recordAttendance(String sessionId, RecordAttendanceRequest request) {
        AttendanceSessionIds.Parsed session = AttendanceSessionIds.parse(sessionId);
        LmsClass lmsClass = classAccessHelper.requireReadableClass(session.classId());
        classAccessHelper.assertCanManageClass(lmsClass);
        return upsertRecord(lmsClass, session.sessionDate(), request);
    }

    @Override
    @Transactional
    public List<AttendanceRecordPayload> bulkRecordAttendance(String sessionId, BulkAttendanceRequest request) {
        AttendanceSessionIds.Parsed session = AttendanceSessionIds.parse(sessionId);
        LmsClass lmsClass = classAccessHelper.requireReadableClass(session.classId());
        classAccessHelper.assertCanManageClass(lmsClass);
        List<AttendanceRecordPayload> results = new ArrayList<>();
        for (RecordAttendanceRequest row : request.getRecords()) {
            results.add(upsertRecord(lmsClass, session.sessionDate(), row));
        }
        return results;
    }

    @Override
    @Transactional
    public AttendanceRecordPayload updateAttendance(Long recordId, UpdateAttendanceRequest request) {
        AttendanceRecord entity = attendanceRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ExceptionCode.ATTENDANCE_NOT_FOUND));
        classAccessHelper.assertCanManageClass(entity.getLmsClass());
        entity.setStatus(request.getStatus().name());
        attendanceRecordRepository.save(entity);
        notificationPublisher.onAttendanceRecorded(
                entity.getLmsClass(),
                entity.getStudent(),
                entity.getStatus(),
                entity.getSessionDate()
        );
        return toPayload(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceSessionPayload getSessionAttendance(String sessionId) {
        AttendanceSessionIds.Parsed session = AttendanceSessionIds.parse(sessionId);
        classAccessHelper.requireReadableClass(session.classId());
        List<AttendanceRecordPayload> records = attendanceRecordRepository
                .findByLmsClass_IdAndSessionDateOrderByStudent_UsernameAsc(
                        session.classId(),
                        session.sessionDate()
                )
                .stream()
                .map(this::toPayload)
                .toList();
        return AttendanceSessionPayload.builder()
                .sessionId(sessionId)
                .classId(session.classId())
                .sessionDate(session.sessionDate())
                .records(records)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRecordPayload> getStudentAttendance(String studentId, Long classId) {
        classAccessHelper.assertCanViewStudentProgress(studentId);
        User me = SecurityUtils.requireCurrentUser();
        if (me.getRole() == Role.STUDENT && classId != null) {
            classAccessHelper.requireReadableClass(classId);
        }
        List<AttendanceRecord> rows = classId != null
                ? attendanceRecordRepository.findByStudent_UuidAndLmsClass_IdOrderBySessionDateDesc(
                studentId, classId)
                : attendanceRecordRepository.findByStudent_UuidOrderBySessionDateDesc(studentId);
        return rows.stream().map(this::toPayload).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceStatisticsPayload getStatistics(Long classId, String studentId) {
        if (studentId != null) {
            classAccessHelper.assertCanViewStudentProgress(studentId);
        }
        if (classId != null) {
            classAccessHelper.requireReadableClass(classId);
        }
        long present = attendanceRecordRepository.countByFiltersAndStatus(
                classId, studentId, AttendanceStatus.PRESENT.name());
        long late = attendanceRecordRepository.countByFiltersAndStatus(
                classId, studentId, AttendanceStatus.LATE.name());
        long absent = attendanceRecordRepository.countByFiltersAndStatus(
                classId, studentId, AttendanceStatus.ABSENT.name());
        long total = present + late + absent;
        double rate = total == 0
                ? 0.0
                : BigDecimal.valueOf((present + late) * 100.0 / total)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
        return AttendanceStatisticsPayload.builder()
                .present(present)
                .late(late)
                .absent(absent)
                .total(total)
                .attendanceRate(rate)
                .build();
    }

    private AttendanceRecordPayload upsertRecord(
            LmsClass lmsClass,
            LocalDate sessionDate,
            RecordAttendanceRequest request
    ) {
        User student = classAccessHelper.requireStudent(request.getStudentId().trim());
        if (!classEnrollmentRepository.existsByLmsClass_IdAndStudent_Uuid(
                lmsClass.getId(), student.getUuid())) {
            throw new BusinessException(ExceptionCode.STUDENT_NOT_FOUND);
        }
        AttendanceRecord entity = attendanceRecordRepository
                .findByLmsClass_IdAndStudent_UuidAndSessionDate(
                        lmsClass.getId(), student.getUuid(), sessionDate)
                .orElseGet(() -> {
                    AttendanceRecord created = new AttendanceRecord();
                    created.setLmsClass(lmsClass);
                    created.setStudent(student);
                    created.setSessionDate(sessionDate);
                    return created;
                });
        entity.setStatus(request.getStatus().name());
        attendanceRecordRepository.save(entity);
        notificationPublisher.onAttendanceRecorded(
                lmsClass,
                student,
                entity.getStatus(),
                sessionDate
        );
        return toPayload(entity);
    }

    private AttendanceRecordPayload toPayload(AttendanceRecord entity) {
        return AttendanceRecordPayload.builder()
                .id(entity.getId())
                .classId(entity.getLmsClass().getId())
                .studentId(entity.getStudent().getUuid())
                .studentName(entity.getStudent().getUsername())
                .sessionDate(entity.getSessionDate())
                .status(AttendanceStatus.valueOf(entity.getStatus()))
                .build();
    }
}
