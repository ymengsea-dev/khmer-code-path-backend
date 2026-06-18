package com.mengsea.khmercodepath.api.attendance.service;

import com.mengsea.khmercodepath.api.attendance.AttendanceSessionIds;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceExportResource;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceManagementConfigPayload;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceMonthFilterPayload;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceQualityLevelPayload;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceRecordPayload;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceRosterPayload;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceRosterRowPayload;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceSessionPayload;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceStatisticsPayload;
import com.mengsea.khmercodepath.api.attendance.payload.BulkAttendanceRequest;
import com.mengsea.khmercodepath.api.attendance.payload.RecordAttendanceRequest;
import com.mengsea.khmercodepath.api.attendance.payload.UpdateAttendanceRequest;
import com.mengsea.khmercodepath.api.users.payload.ClassFilterPayload;
import com.mengsea.khmercodepath.commons.constant.ClassStatus;
import com.mengsea.khmercodepath.commons.constant.AttendanceStatus;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.AttendanceRecord;
import com.mengsea.khmercodepath.commons.domain.ClassEnrollment;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.AttendanceRecordRepository;
import com.mengsea.khmercodepath.commons.repository.ClassEnrollmentRepository;
import com.mengsea.khmercodepath.commons.repository.LmsClassRepository;
import com.mengsea.khmercodepath.commons.repository.LmsClassSpecifications;
import com.mengsea.khmercodepath.api.notifications.service.NotificationPublisher;
import com.mengsea.khmercodepath.commons.security.ClassAccessHelper;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AttendanceManagementServiceImpl implements AttendanceManagementService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final LmsClassRepository lmsClassRepository;
    private final ClassAccessHelper classAccessHelper;
    private final NotificationPublisher notificationPublisher;
    private final AttendanceExcelExporter attendanceExcelExporter;

    private static final List<AttendanceQualityLevelPayload> QUALITY_BANDS = List.of(
            AttendanceQualityLevelPayload.builder().id("excellent").label("Excellent").minRate(90.0).build(),
            AttendanceQualityLevelPayload.builder().id("good").label("Good").minRate(75.0).build(),
            AttendanceQualityLevelPayload.builder().id("fair").label("Fair").minRate(60.0).build(),
            AttendanceQualityLevelPayload.builder().id("poor").label("Poor").minRate(0.0).build(),
            AttendanceQualityLevelPayload.builder().id("no_data").label("No records").build()
    );

    private static final DateTimeFormatter MONTH_ID_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter MONTH_LABEL_FMT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    private record MonthRange(LocalDate from, LocalDate to) {
        private static MonthRange all() {
            return new MonthRange(null, null);
        }

        private boolean isAll() {
            return from == null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceManagementConfigPayload getManagementConfig(Long classId) {
        User me = SecurityUtils.requireCurrentUser();
        if (me.getRole() != Role.ADMIN && me.getRole() != Role.TEACHER) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }
        String defaultMonthId = YearMonth.now().format(MONTH_ID_FMT);
        List<AttendanceMonthFilterPayload> monthFilters = List.of();
        if (classId != null) {
            LmsClass lmsClass = classAccessHelper.requireReadableClass(classId);
            monthFilters = buildMonthFilters(lmsClass);
            defaultMonthId = resolveDefaultMonthId(lmsClass);
        }
        List<ClassFilterPayload> classFilters = buildClassFilters(me);
        return AttendanceManagementConfigPayload.builder()
                .pageTitle("Attendance Management")
                .pageDescription("Review student attendance by month and issue warnings when needed.")
                .classFilters(classFilters)
                .monthFilters(monthFilters)
                .defaultMonthId(defaultMonthId)
                .defaultClassId(resolveDefaultClassId(classFilters))
                .canManageWarnings(me.getRole() == Role.ADMIN || me.getRole() == Role.TEACHER)
                .canExport(me.getRole() == Role.ADMIN || me.getRole() == Role.TEACHER)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceRosterPayload getRoster(Long classId, String search, String monthFilter) {
        LmsClass lmsClass = classAccessHelper.requireReadableClass(classId);
        MonthRange monthRange = parseMonthFilter(monthFilter);
        List<AttendanceRosterRowPayload> rows = new ArrayList<>();

        for (ClassEnrollment enrollment : classEnrollmentRepository.findByLmsClass_IdOrderByEnrolledAtAsc(classId)) {
            User student = enrollment.getStudent();
            if (student == null || student.isDeleted()) {
                continue;
            }
            AttendanceStatisticsPayload stats = getStatistics(
                    classId,
                    student.getUuid(),
                    monthRange.from(),
                    monthRange.to()
            );
            AttendanceQualityLevelPayload quality = resolveQuality(stats);
            AttendanceRosterRowPayload row = AttendanceRosterRowPayload.builder()
                    .studentId(student.getUuid())
                    .studentName(student.getUsername())
                    .studentCode(student.getStudentId())
                    .avatarUrl(resolveAvatarUrl(student))
                    .present(stats.getPresent())
                    .late(stats.getLate())
                    .absent(stats.getAbsent())
                    .total(stats.getTotal())
                    .attendanceRate(stats.getTotal() == 0 ? null : stats.getAttendanceRate())
                    .qualityId(quality.getId())
                    .qualityLabel(quality.getLabel())
                    .warned(enrollment.isAttendanceWarned())
                    .build();
            if (!matchesRosterSearch(row, search)) {
                continue;
            }
            rows.add(row);
        }

        rows.sort(Comparator.comparing(AttendanceRosterRowPayload::getStudentName, String.CASE_INSENSITIVE_ORDER));

        double avg = rows.stream()
                .map(AttendanceRosterRowPayload::getAttendanceRate)
                .filter(rate -> rate != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(Double.NaN);

        return AttendanceRosterPayload.builder()
                .classId(classId)
                .className(lmsClass.getName())
                .rows(rows)
                .warnedCount(rows.stream().filter(AttendanceRosterRowPayload::isWarned).count())
                .classAverageRate(Double.isNaN(avg) ? null : roundRate(avg))
                .build();
    }

    @Override
    @Transactional
    public AttendanceRosterPayload setAttendanceWarning(Long classId, String studentId, boolean warned) {
        LmsClass lmsClass = classAccessHelper.requireReadableClass(classId);
        classAccessHelper.assertCanManageClass(lmsClass);
        ClassEnrollment enrollment = classEnrollmentRepository
                .findByLmsClass_IdAndStudent_Uuid(classId, studentId.trim())
                .orElseThrow(() -> new BusinessException(ExceptionCode.STUDENT_NOT_FOUND));
        enrollment.setAttendanceWarned(warned);
        enrollment.setAttendanceWarnedAt(warned ? LocalDateTime.now() : null);
        classEnrollmentRepository.save(enrollment);
        if (warned) {
            notificationPublisher.onAttendanceWarning(lmsClass, enrollment.getStudent());
        }
        return getRoster(classId, null, YearMonth.now().format(MONTH_ID_FMT));
    }

    private List<AttendanceMonthFilterPayload> buildMonthFilters(LmsClass lmsClass) {
        int year = resolveFilterYear(lmsClass);
        List<AttendanceMonthFilterPayload> filters = new ArrayList<>();
        filters.add(AttendanceMonthFilterPayload.builder().id("all").label("All months").build());
        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(year, month);
            filters.add(AttendanceMonthFilterPayload.builder()
                    .id(yearMonth.format(MONTH_ID_FMT))
                    .label(yearMonth.format(MONTH_LABEL_FMT))
                    .build());
        }
        return filters;
    }

    private int resolveFilterYear(LmsClass lmsClass) {
        if (lmsClass.getAcademicYear() != null) {
            return lmsClass.getAcademicYear();
        }
        return YearMonth.now().getYear();
    }

    private String resolveDefaultMonthId(LmsClass lmsClass) {
        int year = resolveFilterYear(lmsClass);
        YearMonth now = YearMonth.now();
        if (now.getYear() == year) {
            return now.format(MONTH_ID_FMT);
        }
        return YearMonth.of(year, 1).format(MONTH_ID_FMT);
    }

    private String resolveDefaultClassId(List<ClassFilterPayload> classFilters) {
        if (classFilters == null || classFilters.isEmpty()) {
            return null;
        }
        return classFilters.getFirst().getValue();
    }

    private MonthRange parseMonthFilter(String monthFilter) {
        if (monthFilter == null || monthFilter.isBlank() || "all".equalsIgnoreCase(monthFilter)) {
            return MonthRange.all();
        }
        try {
            YearMonth month = YearMonth.parse(monthFilter, MONTH_ID_FMT);
            return new MonthRange(month.atDay(1), month.atEndOfMonth());
        } catch (DateTimeParseException ex) {
            return MonthRange.all();
        }
    }

    private List<ClassFilterPayload> buildClassFilters(User me) {
        Specification<LmsClass> spec = Specification.where(LmsClassSpecifications.notDeleted())
                .and(LmsClassSpecifications.statusEquals(ClassStatus.ACTIVE));
        if (me.getRole() == Role.TEACHER) {
            spec = spec.and(LmsClassSpecifications.teacherUuidEquals(me.getUuid()));
        }
        return lmsClassRepository.findAll(spec).stream()
                .sorted(Comparator
                        .comparing((LmsClass c) -> classEnrollmentRepository.countByLmsClass_Id(c.getId()))
                        .reversed()
                        .thenComparing(c -> c.getCode() != null && c.getCode().startsWith("ATT-DEMO") ? 0 : 1)
                        .thenComparing(LmsClass::getName, String.CASE_INSENSITIVE_ORDER))
                .map(c -> ClassFilterPayload.builder()
                        .value(String.valueOf(c.getId()))
                        .label(c.getName())
                        .build())
                .toList();
    }

    private AttendanceQualityLevelPayload resolveQuality(AttendanceStatisticsPayload stats) {
        if (stats.getTotal() == 0) {
            return QUALITY_BANDS.get(4);
        }
        double rate = stats.getAttendanceRate();
        for (AttendanceQualityLevelPayload band : QUALITY_BANDS) {
            if (band.getMinRate() != null && rate >= band.getMinRate()) {
                return band;
            }
        }
        return QUALITY_BANDS.get(3);
    }

    private boolean matchesRosterSearch(AttendanceRosterRowPayload row, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String needle = search.trim().toLowerCase(Locale.ROOT);
        return row.getStudentName().toLowerCase(Locale.ROOT).contains(needle)
                || (row.getStudentCode() != null
                && row.getStudentCode().toLowerCase(Locale.ROOT).contains(needle));
    }

    private double roundRate(double rate) {
        return BigDecimal.valueOf(rate).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

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
        return getStatistics(classId, studentId, null, null);
    }

    private AttendanceStatisticsPayload getStatistics(
            Long classId,
            String studentId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        if (studentId != null) {
            classAccessHelper.assertCanViewStudentProgress(studentId);
        }
        if (classId != null) {
            classAccessHelper.requireReadableClass(classId);
        }
        long present = countByStatus(classId, studentId, AttendanceStatus.PRESENT.name(), dateFrom, dateTo);
        long late = countByStatus(classId, studentId, AttendanceStatus.LATE.name(), dateFrom, dateTo);
        long absent = countByStatus(classId, studentId, AttendanceStatus.ABSENT.name(), dateFrom, dateTo);
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

    private long countByStatus(
            Long classId,
            String studentUuid,
            String status,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        if (dateFrom == null && dateTo == null) {
            return attendanceRecordRepository.countByFiltersAndStatus(classId, studentUuid, status);
        }
        return attendanceRecordRepository.countByClassStudentStatusAndSessionDateBetween(
                classId,
                studentUuid,
                status,
                dateFrom,
                dateTo
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceExportResource exportAttendanceExcel(
            Long classId,
            String search,
            String monthFilter
    ) {
        LmsClass lmsClass = classAccessHelper.requireReadableClass(classId);
        MonthRange monthRange = parseMonthFilter(monthFilter);
        AttendanceRosterPayload roster = getRoster(classId, search, monthFilter);
        List<AttendanceRecord> records = monthRange.isAll()
                ? attendanceRecordRepository.findByLmsClass_IdOrderBySessionDateAscStudent_UsernameAsc(classId)
                : attendanceRecordRepository.findByClassIdAndSessionDateBetween(
                        classId,
                        monthRange.from(),
                        monthRange.to()
                );
        byte[] content = attendanceExcelExporter.export(roster, records);
        String safeName = lmsClass.getName().replaceAll("[^a-zA-Z0-9._-]+", "_");
        if (safeName.isBlank()) {
            safeName = "class-" + classId;
        }
        String monthSuffix = monthRange.isAll() ? "" : "-" + monthFilter;
        String filename = "attendance-" + safeName + monthSuffix + ".xlsx";
        return AttendanceExportResource.builder()
                .content(content)
                .filename(filename)
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

    private static String resolveAvatarUrl(User user) {
        if (user.getAvatarStorageKey() == null || user.getAvatarStorageKey().isBlank()) {
            return null;
        }
        return "/api/v1/profile/avatar/" + user.getUuid();
    }
}
