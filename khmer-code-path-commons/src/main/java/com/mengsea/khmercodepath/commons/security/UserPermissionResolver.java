package com.mengsea.khmercodepath.commons.security;

import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.SchoolRolePermission;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.repository.SchoolRolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserPermissionResolver {

    private final SchoolRolePermissionRepository schoolRolePermissionRepository;

    @Transactional(readOnly = true)
    public List<GrantedAuthority> resolve(User user) {
        List<GrantedAuthority> base = new ArrayList<>(LmsAuthorities.forUser(user));
        if (user.getSchool() == null) {
            return base;
        }
        if (user.getRole() != Role.TEACHER && user.getRole() != Role.STUDENT) {
            return base;
        }

        Set<String> authorityKeys = base.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<SchoolRolePermission> overrides =
                schoolRolePermissionRepository.findBySchool_IdAndRole(
                        user.getSchool().getId(),
                        user.getRole()
                );

        for (SchoolRolePermission override : overrides) {
            if (override.isGranted()) {
                authorityKeys.add(override.getAuthority());
            } else {
                authorityKeys.remove(override.getAuthority());
            }
        }

        return authorityKeys.stream()
                .map(SimpleGrantedAuthority::new)
                .map(a -> (GrantedAuthority) a)
                .toList();
    }
}
