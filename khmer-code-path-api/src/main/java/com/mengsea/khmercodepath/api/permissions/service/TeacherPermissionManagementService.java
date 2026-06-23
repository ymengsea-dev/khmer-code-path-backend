package com.mengsea.khmercodepath.api.permissions.service;

import com.mengsea.khmercodepath.api.permissions.config.PermissionsProperties;
import com.mengsea.khmercodepath.api.permissions.payload.AssignableRolePayload;
import com.mengsea.khmercodepath.api.permissions.payload.GrantablePermissionPayload;
import com.mengsea.khmercodepath.api.permissions.payload.GrantableStudentPermissionPayload;
import com.mengsea.khmercodepath.api.permissions.payload.PermissionHighlightPayload;
import com.mengsea.khmercodepath.api.permissions.payload.PermissionsConfigPayload;
import com.mengsea.khmercodepath.api.permissions.payload.PermissionsTabPayload;
import com.mengsea.khmercodepath.api.permissions.payload.RolePermissionsPayload;
import com.mengsea.khmercodepath.api.permissions.payload.RoleSummaryPayload;
import com.mengsea.khmercodepath.api.permissions.payload.TeacherPermissionStatePayload;
import com.mengsea.khmercodepath.api.permissions.payload.UpdateRolePermissionsRequest;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.School;
import com.mengsea.khmercodepath.commons.domain.SchoolRolePermission;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.api.permissions.payload.SchoolFeaturesPayload;
import com.mengsea.khmercodepath.api.permissions.payload.UpdateSchoolFeaturesRequest;
import com.mengsea.khmercodepath.commons.repository.SchoolRepository;
import com.mengsea.khmercodepath.commons.repository.SchoolRolePermissionRepository;
import com.mengsea.khmercodepath.commons.security.SchoolAccessHelper;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TeacherPermissionManagementService {

    private final PermissionsProperties permissionsProperties;
    private final SchoolRolePermissionRepository schoolRolePermissionRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolAccessHelper schoolAccessHelper;

    @Transactional(readOnly = true)
    public PermissionsConfigPayload getConfig() {
        schoolAccessHelper.assertSchoolAdmin(SecurityUtils.requireCurrentUser());
        return PermissionsConfigPayload.builder()
                .pageTitle(permissionsProperties.getPageTitle())
                .pageDescription(permissionsProperties.getPageDescription())
                .selectTeacherLabel(permissionsProperties.getSelectTeacherLabel())
                .saveButtonLabel(permissionsProperties.getSaveButtonLabel())
                .teacherSectionTitle(permissionsProperties.getTeacherSectionTitle())
                .teacherSectionDescription(permissionsProperties.getTeacherSectionDescription())
                .studentSectionTitle(permissionsProperties.getStudentSectionTitle())
                .studentSectionDescription(permissionsProperties.getStudentSectionDescription())
                .rolesSectionTitle(permissionsProperties.getRolesSectionTitle())
                .rolesSectionDescription(permissionsProperties.getRolesSectionDescription())
                .roleColumnLabel(permissionsProperties.getRoleColumnLabel())
                .statusColumnLabel(permissionsProperties.getStatusColumnLabel())
                .schoolFeaturesSectionTitle(permissionsProperties.getSchoolFeaturesSectionTitle())
                .schoolFeaturesSectionDescription(permissionsProperties.getSchoolFeaturesSectionDescription())
                .publicCoursesFeatureLabel(permissionsProperties.getPublicCoursesFeatureLabel())
                .publicCoursesFeatureDescription(permissionsProperties.getPublicCoursesFeatureDescription())
                .tabs(permissionsProperties.getTabs().stream()
                        .map(t -> PermissionsTabPayload.builder()
                                .id(t.getId())
                                .label(t.getLabel())
                                .build())
                        .toList())
                .assignableRoles(permissionsProperties.getAssignableRoles().stream()
                        .map(r -> AssignableRolePayload.builder()
                                .role(r.getRole())
                                .label(r.getLabel())
                                .build())
                        .toList())
                .roleSummaries(permissionsProperties.getRoleSummaries().stream()
                        .map(r -> RoleSummaryPayload.builder()
                                .role(r.getRole())
                                .title(r.getTitle())
                                .description(r.getDescription())
                                .highlights(r.getHighlights().stream()
                                        .map(h -> PermissionHighlightPayload.builder()
                                                .label(h.getLabel())
                                                .granted(h.isGranted())
                                                .build())
                                        .toList())
                                .build())
                        .toList())
                .grantablePermissions(permissionsProperties.getGrantable().stream()
                        .map(g -> GrantablePermissionPayload.builder()
                                .authority(g.getAuthority())
                                .label(g.getLabel())
                                .description(g.getDescription())
                                .defaultForTeacher(g.isDefaultForTeacher())
                                .build())
                        .toList())
                .grantableStudentPermissions(permissionsProperties.getGrantableStudents().stream()
                        .map(g -> GrantableStudentPermissionPayload.builder()
                                .authority(g.getAuthority())
                                .label(g.getLabel())
                                .description(g.getDescription())
                                .defaultForStudent(g.isDefaultForStudent())
                                .build())
                        .toList())
                .build();
    }

    @Transactional(readOnly = true)
    public RolePermissionsPayload getTeacherPermissions() {
        return getRolePermissions(Role.TEACHER);
    }

    @Transactional
    public RolePermissionsPayload updateTeacherPermissions(UpdateRolePermissionsRequest request) {
        return updateRolePermissions(Role.TEACHER, request);
    }

    @Transactional(readOnly = true)
    public RolePermissionsPayload getStudentPermissions() {
        return getRolePermissions(Role.STUDENT);
    }

    @Transactional
    public RolePermissionsPayload updateStudentPermissions(UpdateRolePermissionsRequest request) {
        return updateRolePermissions(Role.STUDENT, request);
    }

    @Transactional(readOnly = true)
    public SchoolFeaturesPayload getSchoolFeatures() {
        User actor = SecurityUtils.requireCurrentUser();
        schoolAccessHelper.assertSchoolAdmin(actor);
        School school = requireManagedSchool(actor);
        return SchoolFeaturesPayload.builder()
                .publicCoursesEnabled(school.isPublicCoursesEnabled())
                .build();
    }

    @Transactional
    public SchoolFeaturesPayload updateSchoolFeatures(UpdateSchoolFeaturesRequest request) {
        User actor = SecurityUtils.requireCurrentUser();
        schoolAccessHelper.assertSchoolAdmin(actor);
        School school = requireManagedSchool(actor);
        school.setPublicCoursesEnabled(request.isPublicCoursesEnabled());
        schoolRepository.save(school);
        return SchoolFeaturesPayload.builder()
                .publicCoursesEnabled(school.isPublicCoursesEnabled())
                .build();
    }

    private School requireManagedSchool(User actor) {
        return schoolRepository.findById(schoolAccessHelper.requireSchoolId(actor))
                .orElseThrow(() -> new BusinessException(ExceptionCode.SCHOOL_NOT_FOUND));
    }

    private RolePermissionsPayload getRolePermissions(Role role) {
        User actor = SecurityUtils.requireCurrentUser();
        schoolAccessHelper.assertSchoolAdmin(actor);
        School school = schoolAccessHelper.requireSchool(actor);

        Map<String, SchoolRolePermission> overrides = loadOverrideMap(school.getId(), role);
        List<TeacherPermissionStatePayload> permissions = switch (role) {
            case TEACHER -> permissionsProperties.getGrantable().stream()
                    .map(g -> toTeacherState(g, overrides.get(g.getAuthority())))
                    .toList();
            case STUDENT -> permissionsProperties.getGrantableStudents().stream()
                    .map(g -> toStudentState(g, overrides.get(g.getAuthority())))
                    .toList();
            default -> List.of();
        };

        return RolePermissionsPayload.builder()
                .role(role)
                .permissions(permissions)
                .build();
    }

    private RolePermissionsPayload updateRolePermissions(Role role, UpdateRolePermissionsRequest request) {
        User actor = SecurityUtils.requireCurrentUser();
        schoolAccessHelper.assertSchoolAdmin(actor);
        School school = schoolAccessHelper.requireSchool(actor);

        Set<String> grantableAuthorities = grantableAuthoritySet(role);
        Set<String> seen = new HashSet<>();
        for (UpdateRolePermissionsRequest.PermissionGrantPayload item : request.getPermissions()) {
            String authority = item.getAuthority() != null ? item.getAuthority().trim() : "";
            if (authority.isBlank() || !grantableAuthorities.contains(authority)) {
                throw new BusinessException(ExceptionCode.PERMISSION_NOT_GRANTABLE);
            }
            if (!seen.add(authority)) {
                continue;
            }

            boolean defaultGranted = defaultGrantedFor(role, authority);
            boolean requested = item.isGranted();

            if (requested == defaultGranted) {
                schoolRolePermissionRepository.deleteBySchool_IdAndRoleAndAuthority(
                        school.getId(),
                        role,
                        authority
                );
                continue;
            }

            SchoolRolePermission override = schoolRolePermissionRepository
                    .findBySchool_IdAndRoleAndAuthority(school.getId(), role, authority)
                    .orElseGet(() -> {
                        SchoolRolePermission created = new SchoolRolePermission();
                        created.setSchool(school);
                        created.setRole(role);
                        created.setAuthority(authority);
                        return created;
                    });
            override.setGranted(requested);
            schoolRolePermissionRepository.save(override);
        }

        return getRolePermissions(role);
    }

    private Map<String, SchoolRolePermission> loadOverrideMap(Long schoolId, Role role) {
        Map<String, SchoolRolePermission> map = new HashMap<>();
        for (SchoolRolePermission override :
                schoolRolePermissionRepository.findBySchool_IdAndRole(schoolId, role)) {
            map.put(override.getAuthority(), override);
        }
        return map;
    }

    private TeacherPermissionStatePayload toTeacherState(
            PermissionsProperties.GrantableEntry definition,
            SchoolRolePermission override
    ) {
        return toState(
                definition.getAuthority(),
                definition.getLabel(),
                definition.getDescription(),
                definition.isDefaultForTeacher(),
                override
        );
    }

    private TeacherPermissionStatePayload toStudentState(
            PermissionsProperties.StudentGrantableEntry definition,
            SchoolRolePermission override
    ) {
        return toState(
                definition.getAuthority(),
                definition.getLabel(),
                definition.getDescription(),
                definition.isDefaultForStudent(),
                override
        );
    }

    private TeacherPermissionStatePayload toState(
            String authority,
            String label,
            String description,
            boolean defaultGranted,
            SchoolRolePermission override
    ) {
        boolean granted = defaultGranted;
        boolean overridden = false;
        if (override != null) {
            granted = override.isGranted();
            overridden = true;
        }
        return TeacherPermissionStatePayload.builder()
                .authority(authority)
                .label(label)
                .description(description)
                .defaultGranted(defaultGranted)
                .granted(granted)
                .overridden(overridden)
                .build();
    }

    private Set<String> grantableAuthoritySet(Role role) {
        return switch (role) {
            case TEACHER -> permissionsProperties.getGrantable().stream()
                    .map(PermissionsProperties.GrantableEntry::getAuthority)
                    .collect(java.util.stream.Collectors.toSet());
            case STUDENT -> permissionsProperties.getGrantableStudents().stream()
                    .map(PermissionsProperties.StudentGrantableEntry::getAuthority)
                    .collect(java.util.stream.Collectors.toSet());
            default -> Set.of();
        };
    }

    private boolean defaultGrantedFor(Role role, String authority) {
        if (role == Role.TEACHER) {
            return permissionsProperties.getGrantable().stream()
                    .filter(g -> authority.equals(g.getAuthority()))
                    .findFirst()
                    .map(PermissionsProperties.GrantableEntry::isDefaultForTeacher)
                    .orElseThrow(() -> new BusinessException(ExceptionCode.PERMISSION_NOT_GRANTABLE));
        }
        if (role == Role.STUDENT) {
            return permissionsProperties.getGrantableStudents().stream()
                    .filter(g -> authority.equals(g.getAuthority()))
                    .findFirst()
                    .map(PermissionsProperties.StudentGrantableEntry::isDefaultForStudent)
                    .orElseThrow(() -> new BusinessException(ExceptionCode.PERMISSION_NOT_GRANTABLE));
        }
        throw new BusinessException(ExceptionCode.PERMISSION_NOT_GRANTABLE);
    }
}
