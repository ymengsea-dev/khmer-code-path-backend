package com.mengsea.khmercodepath.api.attendance.payload;

import com.mengsea.khmercodepath.commons.constant.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAttendanceRequest {

    @NotNull
    private AttendanceStatus status;
}
