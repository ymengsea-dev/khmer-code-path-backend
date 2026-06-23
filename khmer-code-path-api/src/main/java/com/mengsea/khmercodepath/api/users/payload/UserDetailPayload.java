package com.mengsea.khmercodepath.api.users.payload;

import com.mengsea.khmercodepath.commons.constant.Provider;
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
public class UserDetailPayload {
    private String id;
    private String name;
    private String email;
    private Role role;
    private boolean isActive;
    private String avatarUrl;
    private String studentId;
    private String teacherId;
    private Provider provider;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long schoolId;
    private String schoolName;
}
