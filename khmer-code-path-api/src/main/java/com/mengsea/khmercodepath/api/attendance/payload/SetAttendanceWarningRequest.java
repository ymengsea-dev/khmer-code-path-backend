package com.mengsea.khmercodepath.api.attendance.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetAttendanceWarningRequest {
    @NotNull
    private Boolean warned;
}
