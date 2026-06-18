package com.mengsea.khmercodepath.api.classes.payload;

import com.mengsea.khmercodepath.api.users.payload.UserDetailPayload;
import com.mengsea.khmercodepath.commons.constant.ClassStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassDetailPayload {
    private Long id;
    private String code;
    private String name;
    private String description;
    private UserDetailPayload teacher;
    private String semester;
    private Integer academicYear;
    private String schedule;
    private String roomNumber;
    private ClassStatus status;
    private EnrollmentCountsPayload enrollment;
    private LessonsSummaryPayload lessons;
    private GradingWeightsPayload gradingWeights;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
