package com.mengsea.khmercodepath.commons.security;

import com.mengsea.khmercodepath.commons.constant.LmsAuthority;
import com.mengsea.khmercodepath.commons.constant.Role;
import com.mengsea.khmercodepath.commons.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Maps {@link Role} to Spring Security authorities (ROLE_* + {@link LmsAuthority} scopes).
 */
public final class LmsAuthorities {

    private LmsAuthorities() {}

    public static List<GrantedAuthority> forUser(User user) {
        if (user.getRole() == null) {
            return Collections.emptyList();
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        switch (user.getRole()) {
            case ADMIN -> {
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.USR_MANAGE));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.CLS_READ));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.CLS_MANAGE));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.CRS_READ));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.CRS_MANAGE));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.OPS_MANAGE));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.AI_CHAT));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.AI_INGEST));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.DASH_READ));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.ATT_MANAGE));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.GRD_MANAGE));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.PROG_READ));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.LSN_MANAGE));
            }
            case TEACHER -> {
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.CLS_READ));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.CRS_READ));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.CRS_MANAGE));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.AI_CHAT));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.AI_INGEST));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.DASH_READ));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.ATT_MANAGE));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.GRD_MANAGE));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.PROG_READ));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.LSN_MANAGE));
            }
            case STUDENT -> {
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.CLS_READ));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.CRS_READ));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.AI_CHAT));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.DASH_READ));
                authorities.add(new SimpleGrantedAuthority(LmsAuthority.PROG_READ));
            }
        }
        return authorities;
    }
}
