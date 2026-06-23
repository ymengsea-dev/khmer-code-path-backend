package com.mengsea.khmercodepath.api.schools.payload;

import com.mengsea.khmercodepath.commons.constant.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDomainPayload {
    private Long id;
    private String domain;
    private boolean autoApprove;
    private Role defaultRole;
    private LocalDateTime createdAt;
}
