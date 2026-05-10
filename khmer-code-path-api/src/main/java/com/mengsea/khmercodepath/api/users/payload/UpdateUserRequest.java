package com.mengsea.khmercodepath.api.users.payload;

import com.mengsea.khmercodepath.commons.constant.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Schema(description = "Display name")
    private String name;

    @Email
    private String email;

    @Size(min = 4, max = 128)
    private String password;

    private Role role;

    @Schema(description = "Ignored if studentId was already set at creation")
    private String studentId;

    @Schema(description = "Ignored if teacherId was already set at creation")
    private String teacherId;
}
