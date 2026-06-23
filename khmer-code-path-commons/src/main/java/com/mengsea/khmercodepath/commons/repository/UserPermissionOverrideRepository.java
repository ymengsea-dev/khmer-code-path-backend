package com.mengsea.khmercodepath.commons.repository;

import com.mengsea.khmercodepath.commons.domain.UserPermissionOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPermissionOverrideRepository extends JpaRepository<UserPermissionOverride, Long> {

    List<UserPermissionOverride> findByUser_UuidAndSchool_Id(String userUuid, Long schoolId);

    Optional<UserPermissionOverride> findByUser_UuidAndSchool_IdAndAuthority(
            String userUuid,
            Long schoolId,
            String authority
    );

    void deleteByUser_UuidAndSchool_IdAndAuthority(String userUuid, Long schoolId, String authority);
}
