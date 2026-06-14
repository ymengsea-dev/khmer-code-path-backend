package com.mengsea.khmercodepath.api.profile.payload;

import com.mengsea.khmercodepath.api.dashboard.payload.StudentDashboardPayload;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class MyLearningPayload {
    StudentDashboardPayload dashboard;
    List<LearningClassPayload> learningClasses;
}
