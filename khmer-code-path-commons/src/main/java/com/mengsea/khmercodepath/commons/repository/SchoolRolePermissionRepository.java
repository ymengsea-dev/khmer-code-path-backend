package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.SchoolRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolRolePermissionRepository extends JpaRepository<SchoolRolePermission, Long> {

    List<SchoolRolePermission> findBySchool_IdAndRole(Long schoolId, Role role);

    Optional<SchoolRolePermission> findBySchool_IdAndRoleAndAuthority(
            Long schoolId,
            Role role,
            String authority
    );

    void deleteBySchool_IdAndRoleAndAuthority(Long schoolId, Role role, String authority);
}
