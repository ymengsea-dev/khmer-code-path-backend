package com.mengsea.khmercodepath.api.dashboard.payload;

import com.mengsea.khmercodepath.api.classes.payload.ClassCommentPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDashboardPayload {
    private long activeClasses;
    private long quizzes;
    private long students;
    private long studentQuestions;
    private List<ClassCommentPayload> recentQuestions;
}
