package com.mengsea.khmercodepath.api.grades.service;

import com.mengsea.khmercodepath.api.grades.payload.CreateGradeRequest;
import com.mengsea.khmercodepath.api.grades.payload.FinalGradePayload;
import com.mengsea.khmercodepath.api.grades.payload.GradePayload;
import com.mengsea.khmercodepath.api.grades.payload.GradebookPayload;
import com.mengsea.khmercodepath.api.grades.payload.UpdateGradeRequest;

import java.util.List;

public interface GradeManagementService {

    GradePayload createGrade(CreateGradeRequest request);

    GradePayload updateGrade(Long gradeId, UpdateGradeRequest request);

    GradebookPayload getGradebook(Long classId);

    List<GradePayload> getStudentGrades(Long classId, String studentId);

    FinalGradePayload calculateFinalGrade(Long classId, String studentId);
}
