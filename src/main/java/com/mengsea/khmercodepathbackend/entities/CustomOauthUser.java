package com.mengsea.khmercodepathbackend.entities;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
public class CustomOauthUser implements OAuth2User {
    private final User user;
    private final Map<String, Object> attributes;

    public CustomOauthUser(User user, Map<String, Object> attributes){
        this.user = user;
        this.attributes = attributes;
    }
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.getRole() == null){
            return Collections.emptyList();
        }

        return Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()));

    }

    @Override
    public String getName() {
        return user.getEmail();
    }
}
