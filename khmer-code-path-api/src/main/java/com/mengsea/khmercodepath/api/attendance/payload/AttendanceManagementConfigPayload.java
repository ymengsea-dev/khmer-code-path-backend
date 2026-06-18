package com.mengsea.khmercodepath.api.attendance.payload;

import com.mengsea.khmercodepath.api.users.payload.ClassFilterPayload;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AttendanceManagementConfigPayload {
    private String pageTitle;
    private String pageDescription;
    private List<ClassFilterPayload> classFilters;
    private List<AttendanceMonthFilterPayload> monthFilters;
    private String defaultMonthId;
    private String defaultClassId;
    private boolean canManageWarnings;
    private boolean canExport;
}
