package com.mengsea.khmercodepath.api.users.service;

import com.mengsea.khmercodepath.api.users.mapper.UserAdminMapper;
import com.mengsea.khmercodepath.api.users.payload.ClassFilterPayload;
import com.mengsea.khmercodepath.api.users.payload.CreateUserRequest;
import com.mengsea.khmercodepath.api.users.payload.StudentDetailPayload;
import com.mengsea.khmercodepath.api.users.payload.StudentPagePayload;
import com.mengsea.khmercodepath.api.users.payload.StudentSummaryPayload;
import com.mengsea.khmercodepath.api.users.payload.StatusFilterPayload;
import com.mengsea.khmercodepath.api.users.payload.UserManagementActionsPayload;
import com.mengsea.khmercodepath.api.users.payload.UserManagementConfigPayload;
import com.mengsea.khmercodepath.api.users.payload.UserTabPayload;
import com.mengsea.khmercodepath.api.users.payload.UpdateUserRequest;
import com.mengsea.khmercodepath.api.users.payload.UserDetailPayload;
import com.mengsea.khmercodepath.api.users.payload.UserImportErrorPayload;
import com.mengsea.khmercodepath.api.users.payload.UserImportResultPayload;
import com.mengsea.khmercodepath.api.users.payload.UserPagePayload;
import com.mengsea.khmercodepath.api.users.payload.UserStatusRequest;
import com.mengsea.khmercodepath.commons.constant.ClassStatus;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.Provider;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.ClassEnrollment;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.ClassEnrollmentRepository;
import com.mengsea.khmercodepath.commons.repository.LmsClassRepository;
import com.mengsea.khmercodepath.commons.repository.LmsClassSpecifications;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import com.mengsea.khmercodepath.commons.repository.UserSpecifications;
import com.mengsea.khmercodepath.commons.security.SchoolAccessHelper;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAdminMapper userAdminMapper;
    private final LmsClassRepository lmsClassRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final SchoolAccessHelper schoolAccessHelper;

    private static final List<StatusFilterPayload> STATUS_FILTERS = List.of(
            StatusFilterPayload.builder().value("all").label("All Status").build(),
            StatusFilterPayload.builder().value("active").label("Active").build(),
            StatusFilterPayload.builder().value("inactive").label("Inactive").build()
    );

    private static final List<UserTabPayload> ADMIN_TABS = List.of(
            UserTabPayload.builder().id("all").label("All Users").build(),
            UserTabPayload.builder().id("students").label("Students").build(),
            UserTabPayload.builder().id("teachers").label("Teachers").build(),
            UserTabPayload.builder().id("admins").label("Administrators").build()
    );

    private static final List<String> CARD_GRADIENTS = List.of(
            "from-violet-500 to-indigo-600",
            "from-sky-500 to-blue-600",
            "from-emerald-500 to-teal-600",
            "from-fuchsia-500 to-purple-600",
            "from-amber-500 to-orange-600",
            "from-rose-500 to-pink-600"
    );

    @Override
    @Transactional(readOnly = true)
    public UserManagementConfigPayload getConfig() {
        User me = SecurityUtils.requireCurrentUser();
        boolean isAdmin = me.getRole() == Role.ADMIN;
        List<ClassFilterPayload> classFilters = buildClassFilters(me);

        if (isAdmin) {
            return UserManagementConfigPayload.builder()
                    .pageTitle("Student Management")
                    .pageDescription("Manage students, teachers, and administrators at your school.")
                    .tabs(ADMIN_TABS)
                    .statusFilters(STATUS_FILTERS)
                    .classFilters(classFilters)
                    .cardGradients(CARD_GRADIENTS)
                    .actions(UserManagementActionsPayload.builder()
                            .canAdd(true)
                            .canImport(true)
                            .canEditStatus(true)
                            .build())
                    .build();
        }

        return UserManagementConfigPayload.builder()
                .pageTitle("Student Management")
                .pageDescription("Students enrolled in your classes.")
                .tabs(List.of())
                .statusFilters(STATUS_FILTERS)
                .classFilters(classFilters)
                .cardGradients(CARD_GRADIENTS)
                .actions(UserManagementActionsPayload.builder()
                        .canAdd(false)
                        .canImport(false)
                        .canEditStatus(false)
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentPagePayload listStudents(String classIdParam, String search, Boolean isActive) {
        User me = SecurityUtils.requireCurrentUser();
        if (me.getRole() != Role.ADMIN && me.getRole() != Role.TEACHER) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }

        Long classId = parseClassId(classIdParam);
        List<LmsClass> classes = resolveAccessibleClasses(me, classId);
        Map<String, StudentAccumulator> students = new LinkedHashMap<>();

        for (LmsClass lmsClass : classes) {
            List<ClassEnrollment> enrollments =
                    classEnrollmentRepository.findByLmsClass_IdOrderByEnrolledAtAsc(lmsClass.getId());
            for (ClassEnrollment enrollment : enrollments) {
                User student = enrollment.getStudent();
                if (student == null || student.isDeleted() || student.getRole() != Role.STUDENT) {
                    continue;
                }
                StudentAccumulator acc = students.computeIfAbsent(
                        student.getUuid(),
                        id -> StudentAccumulator.from(student)
                );
                acc.addClass(lmsClass);
            }
        }

        if (me.getRole() == Role.ADMIN && classId == null) {
            Specification<User> spec = Specification.where(UserSpecifications.deletedFlag(false))
                    .and(UserSpecifications.roleEquals(Role.STUDENT))
                    .and(UserSpecifications.activeEquals(isActive))
                    .and(UserSpecifications.schoolIdEquals(schoolAccessHelper.requireSchoolId(me)));
            for (User student : userRepository.findAll(spec)) {
                students.computeIfAbsent(student.getUuid(), id -> StudentAccumulator.from(student));
            }
        }

        List<StudentSummaryPayload> items = students.values().stream()
                .map(StudentAccumulator::toPayload)
                .filter(row -> matchesStudentSearch(row, search))
                .filter(row -> matchesStudentActive(row, isActive))
                .sorted(Comparator.comparing(StudentSummaryPayload::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return StudentPagePayload.builder()
                .items(items)
                .totalElements(items.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDetailPayload getStudent(String id) {
        User me = SecurityUtils.requireCurrentUser();
        if (me.getRole() != Role.ADMIN && me.getRole() != Role.TEACHER) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }

        User student = userRepository.findByUuidAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));
        if (student.getRole() != Role.STUDENT) {
            throw new BusinessException(ExceptionCode.STUDENT_NOT_FOUND);
        }

        if (me.getRole() == Role.ADMIN) {
            schoolAccessHelper.assertSameSchool(me, student);
        }

        if (me.getRole() == Role.TEACHER
                && !classEnrollmentRepository.existsByStudent_UuidAndLmsClass_Teacher_Uuid(
                id, me.getUuid())) {
            throw new BusinessException(ExceptionCode.ACCESS_DENIED);
        }

        StudentAccumulator acc = StudentAccumulator.from(student);
        List<ClassEnrollment> enrollments =
                classEnrollmentRepository.findByStudent_UuidOrderByEnrolledAtDesc(id);
        for (ClassEnrollment enrollment : enrollments) {
            LmsClass lmsClass = enrollment.getLmsClass();
            if (lmsClass == null || lmsClass.isDeleted()) {
                continue;
            }
            if (me.getRole() == Role.TEACHER
                    && (lmsClass.getTeacher() == null
                    || !me.getUuid().equals(lmsClass.getTeacher().getUuid()))) {
                continue;
            }
            acc.addClass(lmsClass);
        }

        StudentSummaryPayload summary = acc.toPayload();
        return StudentDetailPayload.builder()
                .id(summary.getId())
                .name(summary.getName())
                .email(summary.getEmail())
                .studentId(summary.getStudentId())
                .isActive(summary.isActive())
                .bio(student.getBio())
                .avatarUrl(resolveAvatarUrl(student))
                .enrolledClasses(summary.getEnrolledClasses())
                .enrolledClassIds(summary.getEnrolledClassIds())
                .memberSince(student.getCreatedAt())
                .build();
    }

    private static String resolveAvatarUrl(User user) {
        if (user.getAvatarStorageKey() == null || user.getAvatarStorageKey().isBlank()) {
            return null;
        }
        return "/api/v1/profile/avatar/" + user.getUuid();
    }

    private List<ClassFilterPayload> buildClassFilters(User me) {
        List<ClassFilterPayload> filters = new ArrayList<>();
        filters.add(ClassFilterPayload.builder().value("all").label("All Classes").build());
        for (LmsClass lmsClass : resolveAccessibleClasses(me, null)) {
            filters.add(ClassFilterPayload.builder()
                    .value(String.valueOf(lmsClass.getId()))
                    .label(lmsClass.getName())
                    .build());
        }
        return filters;
    }

    private List<LmsClass> resolveAccessibleClasses(User me, Long classId) {
        Specification<LmsClass> spec = Specification.where(LmsClassSpecifications.notDeleted())
                .and(LmsClassSpecifications.statusEquals(ClassStatus.ACTIVE));
        if (me.getRole() == Role.TEACHER) {
            spec = spec.and(LmsClassSpecifications.teacherUuidEquals(me.getUuid()));
        } else if (me.getRole() == Role.ADMIN) {
            spec = spec.and(LmsClassSpecifications.schoolIdEquals(schoolAccessHelper.requireSchoolId(me)));
        }
        if (classId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("id"), classId));
        }
        return lmsClassRepository.findAll(
                spec,
                org.springframework.data.domain.Sort.by("name").ascending()
        );
    }

    private Long parseClassId(String classIdParam) {
        if (classIdParam == null || classIdParam.isBlank() || "all".equalsIgnoreCase(classIdParam)) {
            return null;
        }
        try {
            return Long.parseLong(classIdParam.trim());
        } catch (NumberFormatException ex) {
            throw new BusinessException(ExceptionCode.CLASS_NOT_FOUND);
        }
    }

    private boolean matchesStudentSearch(StudentSummaryPayload row, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String needle = search.trim().toLowerCase(Locale.ROOT);
        return row.getName().toLowerCase(Locale.ROOT).contains(needle)
                || row.getEmail().toLowerCase(Locale.ROOT).contains(needle)
                || (row.getStudentId() != null
                && row.getStudentId().toLowerCase(Locale.ROOT).contains(needle));
    }

    private boolean matchesStudentActive(StudentSummaryPayload row, Boolean isActive) {
        if (isActive == null) {
            return true;
        }
        return row.isActive() == isActive;
    }

    private static final class StudentAccumulator {
        private final String id;
        private final String name;
        private final String email;
        private final String studentId;
        private final boolean active;
        private final String avatarUrl;
        private final Set<String> classNames = new LinkedHashSet<>();
        private final List<String> classIds = new ArrayList<>();

        private StudentAccumulator(User student) {
            this.id = student.getUuid();
            this.name = student.getUsername();
            this.email = student.getEmail();
            this.studentId = student.getStudentId();
            this.active = student.isActive();
            this.avatarUrl = resolveAvatarUrl(student);
        }

        static StudentAccumulator from(User student) {
            return new StudentAccumulator(student);
        }

        void addClass(LmsClass lmsClass) {
            classNames.add(lmsClass.getName());
            classIds.add(String.valueOf(lmsClass.getId()));
        }

        StudentSummaryPayload toPayload() {
            return StudentSummaryPayload.builder()
                    .id(id)
                    .name(name)
                    .email(email)
                    .studentId(studentId)
                    .isActive(active)
                    .avatarUrl(avatarUrl)
                    .enrolledClasses(classNames.stream().collect(Collectors.joining(", ")))
                    .enrolledClassIds(List.copyOf(classIds))
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserPagePayload listUsers(String name, String email, Role role, Boolean isActive, boolean includeDeleted, Pageable pageable) {
        User me = SecurityUtils.requireCurrentUser();
        Specification<User> spec = Specification.where(UserSpecifications.deletedFlag(includeDeleted))
                .and(UserSpecifications.nameContains(name))
                .and(UserSpecifications.emailContains(email))
                .and(UserSpecifications.roleEquals(role))
                .and(UserSpecifications.activeEquals(isActive));
        if (me.getRole() == Role.ADMIN) {
            spec = spec.and(UserSpecifications.schoolIdEquals(schoolAccessHelper.requireSchoolId(me)));
        }

        Page<User> page = userRepository.findAll(spec, pageable);
        List<UserDetailPayload> items = page.getContent().stream().map(userAdminMapper::toDetail).toList();
        return UserPagePayload.builder()
                .items(items)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailPayload getUser(String id) {
        User me = SecurityUtils.requireCurrentUser();
        User user = userRepository.findByUuidAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));
        if (me.getRole() == Role.ADMIN) {
            schoolAccessHelper.assertSameSchool(me, user);
        }
        return userAdminMapper.toDetail(user);
    }

    @Override
    @Transactional
    public UserDetailPayload createUser(CreateUserRequest request) {
        User me = SecurityUtils.requireCurrentUser();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        assertEmailAvailable(email);
        String studentId = blankToNull(request.getStudentId());
        String teacherId = blankToNull(request.getTeacherId());
        assertRoleIdsAvailable(null, studentId, teacherId);

        User user = buildNewUser(
                request.getName().trim(),
                email,
                request.getPassword(),
                request.getRole(),
                studentId,
                teacherId
        );
        if (me.getRole() == Role.ADMIN) {
            user.setSchool(schoolAccessHelper.requireSchool(me));
        }
        userRepository.save(user);
        return userAdminMapper.toDetail(user);
    }

    @Override
    @Transactional
    public UserDetailPayload updateUser(String id, UpdateUserRequest request) {
        User me = SecurityUtils.requireCurrentUser();
        User user = userRepository.findByUuidAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));
        if (me.getRole() == Role.ADMIN) {
            schoolAccessHelper.assertSameSchool(me, user);
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setUsername(request.getName().trim());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);
            if (!newEmail.equalsIgnoreCase(user.getEmail())
                    && userRepository.findByEmail(newEmail).filter(u -> !u.getUuid().equals(user.getUuid())).isPresent()) {
                throw new BusinessException(ExceptionCode.USER_ALREADY_EXISTS);
            }
            user.setEmail(newEmail);
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null && request.getRole() != user.getRole()) {
            assertRoleChangeAllowed(me, user, request.getRole());
            user.setRole(request.getRole());
        }

        String reqStudentId = blankToNull(request.getStudentId());
        if (reqStudentId != null) {
            if (user.getStudentId() != null && !user.getStudentId().equals(reqStudentId)) {
                throw new BusinessException(ExceptionCode.IMMUTABLE_USER_FIELD);
            }
            if (user.getStudentId() == null) {
                if (userRepository.existsByStudentIdAndDeletedFalseAndUuidNot(reqStudentId, user.getUuid())) {
                    throw new BusinessException(ExceptionCode.DUPLICATE_STUDENT_ID);
                }
                user.setStudentId(reqStudentId);
            }
        }

        String reqTeacherId = blankToNull(request.getTeacherId());
        if (reqTeacherId != null) {
            if (user.getTeacherId() != null && !user.getTeacherId().equals(reqTeacherId)) {
                throw new BusinessException(ExceptionCode.IMMUTABLE_USER_FIELD);
            }
            if (user.getTeacherId() == null) {
                if (userRepository.existsByTeacherIdAndDeletedFalseAndUuidNot(reqTeacherId, user.getUuid())) {
                    throw new BusinessException(ExceptionCode.DUPLICATE_TEACHER_ID);
                }
                user.setTeacherId(reqTeacherId);
            }
        }

        userRepository.save(user);
        return userAdminMapper.toDetail(user);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        User me = SecurityUtils.requireCurrentUser();
        User user = userRepository.findByUuidAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));
        if (me.getRole() == Role.ADMIN) {
            schoolAccessHelper.assertSameSchool(me, user);
        }
        user.setDeleted(true);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserDetailPayload updateStatus(String id, UserStatusRequest request) {
        User me = SecurityUtils.requireCurrentUser();
        User user = userRepository.findByUuidAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));
        if (me.getRole() == Role.ADMIN) {
            schoolAccessHelper.assertSameSchool(me, user);
        }
        user.setActive(Boolean.TRUE.equals(request.getIsActive()));
        userRepository.save(user);
        return userAdminMapper.toDetail(user);
    }

    @Override
    public UserImportResultPayload importUsers(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ExceptionCode.IMPORT_FILE_EMPTY);
        }
        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);
        if (filename.endsWith(".csv")) {
            return importCsv(file);
        }
        if (filename.endsWith(".xlsx")) {
            return importXlsx(file);
        }
        throw new BusinessException(ExceptionCode.UNSUPPORTED_IMPORT_FORMAT);
    }

    private UserImportResultPayload importCsv(MultipartFile file) {
        List<UserImportErrorPayload> errors = new ArrayList<>();
        int created = 0;
        try (CSVParser parser = CSVParser.parse(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreEmptyLines(true).build())) {
            for (CSVRecord record : parser) {
                int row = (int) record.getRecordNumber() + 1;
                try {
                    Map<String, String> m = toLowerKeyMap(record);
                    created += importOneRow(m, row) ? 1 : 0;
                } catch (BusinessException e) {
                    errors.add(UserImportErrorPayload.builder().row(row).message(e.getMessage()).build());
                } catch (RuntimeException e) {
                    errors.add(UserImportErrorPayload.builder().row(row).message(e.getMessage()).build());
                }
            }
        } catch (Exception e) {
            throw new BusinessException(ExceptionCode.IMPORT_FILE_EMPTY);
        }
        return UserImportResultPayload.builder()
                .created(created)
                .failed(errors.size())
                .errors(errors)
                .build();
    }

    private UserImportResultPayload importXlsx(MultipartFile file) {
        List<UserImportErrorPayload> errors = new ArrayList<>();
        int created = 0;
        DataFormatter formatter = new DataFormatter();
        try (XSSFWorkbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : null;
            if (sheet == null) {
                throw new BusinessException(ExceptionCode.IMPORT_FILE_EMPTY);
            }
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BusinessException(ExceptionCode.IMPORT_FILE_EMPTY);
            }
            Map<String, Integer> col = new HashMap<>();
            for (Cell c : headerRow) {
                String key = normKey(formatter.formatCellValue(c));
                if (!key.isEmpty()) {
                    col.put(key, c.getColumnIndex());
                }
            }
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                int rowNum = r + 1;
                Map<String, String> m = new HashMap<>();
                for (Map.Entry<String, Integer> e : col.entrySet()) {
                    Cell cell = row.getCell(e.getValue());
                    m.put(e.getKey(), cell == null ? "" : formatter.formatCellValue(cell).trim());
                }
                if (m.values().stream().allMatch(String::isBlank)) {
                    continue;
                }
                try {
                    if (importOneRow(m, rowNum)) {
                        created++;
                    }
                } catch (BusinessException e) {
                    errors.add(UserImportErrorPayload.builder().row(rowNum).message(e.getMessage()).build());
                } catch (RuntimeException e) {
                    errors.add(UserImportErrorPayload.builder().row(rowNum).message(e.getMessage()).build());
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ExceptionCode.IMPORT_FILE_EMPTY);
        }
        return UserImportResultPayload.builder()
                .created(created)
                .failed(errors.size())
                .errors(errors)
                .build();
    }

    private Map<String, String> toLowerKeyMap(CSVRecord record) {
        Map<String, String> m = new HashMap<>();
        for (Map.Entry<String, String> e : record.toMap().entrySet()) {
            m.put(normKey(e.getKey()), e.getValue() != null ? e.getValue().trim() : "");
        }
        return m;
    }

    private static String normKey(String s) {
        return s.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    /**
     * @return true if a user row was created, false if row was empty / skipped
     */
    private boolean importOneRow(Map<String, String> m, int rowNum) {
        String name = get(m, "name");
        String email = get(m, "email");
        String password = get(m, "password");
        String roleStr = get(m, "role");
        String studentId = blankToNull(get(m, "studentid"));
        String teacherId = blankToNull(get(m, "teacherid"));

        if (name.isBlank() && email.isBlank() && password.isBlank() && roleStr.isBlank()) {
            return false;
        }

        if (email.isBlank() || password.isBlank() || roleStr.isBlank()) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }

        Role role = Role.valueOf(roleStr.trim().toUpperCase(Locale.ROOT));
        CreateUserRequest req = new CreateUserRequest();
        req.setName(name.isBlank() ? email : name);
        req.setEmail(email.trim().toLowerCase(Locale.ROOT));
        req.setPassword(password);
        req.setRole(role);
        req.setStudentId(studentId);
        req.setTeacherId(teacherId);

        try {
            createUser(req);
            return true;
        } catch (BusinessException e) {
            throw e;
        }
    }

    private static String get(Map<String, String> m, String key) {
        return m.getOrDefault(key, "").trim();
    }

    private void assertEmailAvailable(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(ExceptionCode.USER_ALREADY_EXISTS);
        }
    }

    private void assertRoleIdsAvailable(String excludeUuid, String studentId, String teacherId) {
        if (studentId != null) {
            if (excludeUuid == null) {
                if (userRepository.existsByStudentIdAndDeletedFalse(studentId)) {
                    throw new BusinessException(ExceptionCode.DUPLICATE_STUDENT_ID);
                }
            } else if (userRepository.existsByStudentIdAndDeletedFalseAndUuidNot(studentId, excludeUuid)) {
                throw new BusinessException(ExceptionCode.DUPLICATE_STUDENT_ID);
            }
        }
        if (teacherId != null) {
            if (excludeUuid == null) {
                if (userRepository.existsByTeacherIdAndDeletedFalse(teacherId)) {
                    throw new BusinessException(ExceptionCode.DUPLICATE_TEACHER_ID);
                }
            } else if (userRepository.existsByTeacherIdAndDeletedFalseAndUuidNot(teacherId, excludeUuid)) {
                throw new BusinessException(ExceptionCode.DUPLICATE_TEACHER_ID);
            }
        }
    }

    private User buildNewUser(String name, String email, String rawPassword, Role role, String studentId, String teacherId) {
        User user = new User();
        user.setUsername(name);
        user.setEmail(email.trim().toLowerCase(Locale.ROOT));
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setProvider(Provider.LOCAL);
        user.setActive(true);
        user.setDeleted(false);
        user.setStudentId(studentId);
        user.setTeacherId(teacherId);
        return user;
    }

    private void assertRoleChangeAllowed(User actor, User target, Role newRole) {
        if (actor.getUuid().equals(target.getUuid())) {
            throw new BusinessException(ExceptionCode.ROLE_SELF_CHANGE);
        }
        if (target.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
            Long schoolId = target.getSchool() != null ? target.getSchool().getId() : null;
            if (schoolId != null) {
                long adminCount = userRepository.countBySchool_IdAndRoleAndDeletedFalse(schoolId, Role.ADMIN);
                if (adminCount <= 1) {
                    throw new BusinessException(ExceptionCode.LAST_ADMIN_REQUIRED);
                }
            }
        }
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
