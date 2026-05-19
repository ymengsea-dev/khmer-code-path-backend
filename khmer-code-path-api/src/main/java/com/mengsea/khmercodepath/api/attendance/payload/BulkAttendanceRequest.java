package com.mengsea.khmercodepath.api.attendance.payload;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkAttendanceRequest {

    @NotEmpty
    @Valid
    private List<RecordAttendanceRequest> records;
}
