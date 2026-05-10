package com.mengsea.khmercodepath.api.users.payload;

import com.mengsea.khmercodepath.commons.constant.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank
    @Schema(example = "Sok Dara")
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 4, max = 128)
    private String password;

    @NotNull
    private Role role;

    @Schema(description = "Required uniqueness when present; for students")
    private String studentId;

    @Schema(description = "Required uniqueness when present; for teachers")
    private String teacherId;
}
