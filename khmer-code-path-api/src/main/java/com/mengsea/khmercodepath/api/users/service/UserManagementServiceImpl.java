package com.mengsea.khmercodepath.api.users.service;

import com.mengsea.khmercodepath.api.users.mapper.UserAdminMapper;
import com.mengsea.khmercodepath.api.users.payload.CreateUserRequest;
import com.mengsea.khmercodepath.api.users.payload.UpdateUserRequest;
import com.mengsea.khmercodepath.api.users.payload.UserDetailPayload;
import com.mengsea.khmercodepath.api.users.payload.UserImportErrorPayload;
import com.mengsea.khmercodepath.api.users.payload.UserImportResultPayload;
import com.mengsea.khmercodepath.api.users.payload.UserPagePayload;
import com.mengsea.khmercodepath.api.users.payload.UserStatusRequest;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.Provider;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import com.mengsea.khmercodepath.commons.repository.UserSpecifications;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAdminMapper userAdminMapper;

    @Override
    @Transactional(readOnly = true)
    public UserPagePayload listUsers(String name, String email, Role role, Boolean isActive, boolean includeDeleted, Pageable pageable) {
        Specification<User> spec = Specification.where(UserSpecifications.deletedFlag(includeDeleted))
                .and(UserSpecifications.nameContains(name))
                .and(UserSpecifications.emailContains(email))
                .and(UserSpecifications.roleEquals(role))
                .and(UserSpecifications.activeEquals(isActive));

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
        User user = userRepository.findByUuidAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));
        return userAdminMapper.toDetail(user);
    }

    @Override
    @Transactional
    public UserDetailPayload createUser(CreateUserRequest request) {
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
        userRepository.save(user);
        return userAdminMapper.toDetail(user);
    }

    @Override
    @Transactional
    public UserDetailPayload updateUser(String id, UpdateUserRequest request) {
        User user = userRepository.findByUuidAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));

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
        if (request.getRole() != null) {
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
        User user = userRepository.findByUuidAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));
        user.setDeleted(true);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserDetailPayload updateStatus(String id, UserStatusRequest request) {
        User user = userRepository.findByUuidAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));
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

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
