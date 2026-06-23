package com.mengsea.khmercodepath.api.departments.service;

import com.mengsea.khmercodepath.api.departments.payload.CreateDepartmentRequest;
import com.mengsea.khmercodepath.api.departments.payload.DepartmentDetailPayload;
import com.mengsea.khmercodepath.api.departments.payload.DepartmentOptionPayload;
import com.mengsea.khmercodepath.api.departments.payload.DepartmentSummaryPayload;
import com.mengsea.khmercodepath.api.departments.payload.UpdateDepartmentRequest;
import com.mengsea.khmercodepath.commons.constant.DepartmentAccent;
import com.mengsea.khmercodepath.commons.constant.DepartmentStatus;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.domain.Department;
import com.mengsea.khmercodepath.commons.domain.Faculty;
import com.mengsea.khmercodepath.commons.domain.School;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.DepartmentRepository;
import com.mengsea.khmercodepath.commons.repository.FacultyRepository;
import com.mengsea.khmercodepath.commons.repository.LmsClassRepository;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import com.mengsea.khmercodepath.commons.security.SchoolAccessHelper;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentManagementServiceImpl implements DepartmentManagementService {

    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;
    private final LmsClassRepository lmsClassRepository;
    private final SchoolAccessHelper schoolAccessHelper;

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentSummaryPayload> listDepartments() {
        School school = schoolAccessHelper.requireSchool(SecurityUtils.requireCurrentUser());
        return departmentRepository.findBySchool_IdAndDeletedFalseOrderByNameAsc(school.getId()).stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentOptionPayload> listDepartmentOptions() {
        School school = schoolAccessHelper.requireSchool(SecurityUtils.requireCurrentUser());
        return departmentRepository.findBySchool_IdAndDeletedFalseOrderByNameAsc(school.getId()).stream()
                .filter(d -> d.getStatus() == DepartmentStatus.ACTIVE)
                .map(d -> DepartmentOptionPayload.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .facultyId(d.getFacultyEntity().getId())
                        .facultyName(d.getFacultyEntity().getName())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDetailPayload getDepartment(Long id) {
        Department dept = requireDepartment(id);
        return DepartmentDetailPayload.builder()
                .department(toSummary(dept))
                .assignedTeachers(listTeachersForDepartment(dept))
                .build();
    }

    @Override
    @Transactional
    public DepartmentSummaryPayload createDepartment(CreateDepartmentRequest request) {
        User me = SecurityUtils.requireCurrentUser();
        schoolAccessHelper.assertSchoolAdmin(me);
        School school = schoolAccessHelper.requireSchool(me);
        Faculty faculty = requireFacultyForSchool(request.getFacultyId(), school);

        Department entity = new Department();
        entity.setSchool(school);
        entity.setFacultyEntity(faculty);
        applyCreate(entity, request);
        entity.setDeleted(false);
        departmentRepository.save(entity);
        return toSummary(entity);
    }

    @Override
    @Transactional
    public DepartmentSummaryPayload updateDepartment(Long id, UpdateDepartmentRequest request) {
        User me = SecurityUtils.requireCurrentUser();
        schoolAccessHelper.assertSchoolAdmin(me);
        School school = schoolAccessHelper.requireSchool(me);
        Department entity = requireDepartment(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            entity.setName(request.getName().trim());
        }
        if (request.getFacultyId() != null) {
            entity.setFacultyEntity(requireFacultyForSchool(request.getFacultyId(), school));
        }
        if (request.getHeadOfDept() != null) {
            entity.setHeadOfDept(blankToNull(request.getHeadOfDept()));
        }
        if (request.getHodId() != null) {
            entity.setHodUser(resolveHodUser(request.getHodId()));
        }
        if (request.getCapacityPercent() != null) {
            entity.setCapacityPercent(request.getCapacityPercent());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getAccent() != null) {
            entity.setAccent(request.getAccent());
        }
        departmentRepository.save(entity);
        return toSummary(entity);
    }

    private Faculty requireFacultyForSchool(Long facultyId, School school) {
        return facultyRepository.findByIdAndSchool_IdAndDeletedFalse(facultyId, school.getId())
                .orElseThrow(() -> new BusinessException(ExceptionCode.FACULTY_NOT_FOUND));
    }

    @Override
    public Department requireDepartmentForSchool(Long departmentId, School school) {
        return departmentRepository.findByIdAndSchool_IdAndDeletedFalse(departmentId, school.getId())
                .orElseThrow(() -> new BusinessException(ExceptionCode.DEPARTMENT_NOT_FOUND));
    }

    private Department requireDepartment(Long id) {
        User me = SecurityUtils.requireCurrentUser();
        School school = schoolAccessHelper.requireSchool(me);
        return requireDepartmentForSchool(id, school);
    }

    private void applyCreate(Department entity, CreateDepartmentRequest request) {
        entity.setName(request.getName().trim());
        entity.setHeadOfDept(blankToNull(request.getHeadOfDept()));
        entity.setHodUser(resolveHodUser(request.getHodId()));
        entity.setCapacityPercent(
                request.getCapacityPercent() != null ? request.getCapacityPercent() : 50
        );
        entity.setStatus(request.getStatus() != null ? request.getStatus() : DepartmentStatus.ACTIVE);
        entity.setAccent(request.getAccent() != null ? request.getAccent() : DepartmentAccent.VIOLET);
    }

    private User resolveHodUser(String hodId) {
        if (hodId == null || hodId.isBlank()) {
            return null;
        }
        return userRepository.findByUuidAndDeletedFalse(hodId.trim()).orElse(null);
    }

    private List<String> listTeachersForDepartment(Department dept) {
        return userRepository.findByDepartment_IdAndDeletedFalseOrderByUsernameAsc(dept.getId()).stream()
                .map(User::getUsername)
                .toList();
    }

    private DepartmentSummaryPayload toSummary(Department dept) {
        String head = dept.getHeadOfDept();
        if ((head == null || head.isBlank()) && dept.getHodUser() != null) {
            head = dept.getHodUser().getUsername();
        }
        int teacherCount = (int) userRepository.countByDepartment_IdAndDeletedFalse(dept.getId());
        int classCount = (int) lmsClassRepository.countByDepartment_IdAndDeletedFalse(dept.getId());
        Faculty faculty = dept.getFacultyEntity();
        return DepartmentSummaryPayload.builder()
                .id(dept.getId())
                .name(dept.getName())
                .facultyId(faculty != null ? faculty.getId() : null)
                .facultyName(faculty != null ? faculty.getName() : null)
                .headOfDept(head != null ? head : "—")
                .teacherCount(teacherCount)
                .classCount(classCount)
                .capacityPercent(dept.getCapacityPercent())
                .status(dept.getStatus())
                .accent(dept.getAccent())
                .build();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
