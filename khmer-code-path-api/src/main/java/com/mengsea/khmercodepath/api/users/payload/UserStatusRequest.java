package com.mengsea.khmercodepath.api.users.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusRequest {
    @NotNull
    private Boolean isActive;
}
