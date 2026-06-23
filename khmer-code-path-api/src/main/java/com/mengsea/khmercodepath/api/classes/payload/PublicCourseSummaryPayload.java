package com.mengsea.khmercodepath.api.classes.payload;

import com.mengsea.khmercodepath.commons.constant.ClassStatus;
import com.mengsea.khmercodepath.commons.constant.ClassVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicCourseSummaryPayload {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String teacherId;
    private String teacherName;
    private String semester;
    private Integer academicYear;
    private String semesterLabel;
    private ClassStatus status;
    private ClassVisibility visibility;
    private String cardGradient;
    private long enrolledCount;
    private boolean enrolled;
}
