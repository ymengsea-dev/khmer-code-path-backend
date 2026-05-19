package com.mengsea.khmercodepath.api.attendance.payload;

import com.mengsea.khmercodepath.commons.constant.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecordAttendanceRequest {

    @NotBlank
    private String studentId;

    @NotNull
    private AttendanceStatus status;
}
