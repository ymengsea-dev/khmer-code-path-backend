package com.mengsea.khmercodepath.api.classes.payload;

import com.mengsea.khmercodepath.commons.constant.ClassStatus;
import com.mengsea.khmercodepath.commons.constant.ClassVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassSummaryPayload {
    private Long id;
    private String code;
    private String name;
    private String teacherId;
    private String teacherName;
    private String semester;
    private Integer academicYear;
    private ClassStatus status;
    private ClassVisibility visibility;
    private String visibilityLabel;
    private String statusLabel;
    private String semesterLabel;
    private String cardGradient;
    private long enrolledCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
