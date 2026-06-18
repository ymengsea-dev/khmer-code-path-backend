package com.mengsea.khmercodepath.api.attendance.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceExportResource {
    private byte[] content;
    private String filename;
}
