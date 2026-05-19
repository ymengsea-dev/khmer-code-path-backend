package com.mengsea.khmercodepath.api.lessons.service;

import com.mengsea.khmercodepath.api.lessons.payload.CopyLessonRequest;
import com.mengsea.khmercodepath.api.lessons.payload.CreateLessonRequest;
import com.mengsea.khmercodepath.api.lessons.payload.LessonDetailPayload;
import com.mengsea.khmercodepath.api.lessons.payload.LessonMaterialPayload;
import com.mengsea.khmercodepath.api.lessons.payload.LessonSummaryPayload;
import com.mengsea.khmercodepath.api.lessons.payload.UpdateLessonRequest;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LessonManagementService {

    List<LessonSummaryPayload> listLessons(Long classId, String teacherId);

    LessonDetailPayload getLesson(Long id);

    LessonDetailPayload createLesson(CreateLessonRequest request);

    LessonDetailPayload updateLesson(Long id, UpdateLessonRequest request);

    void deleteLesson(Long id);

    List<LessonMaterialPayload> uploadMaterials(Long lessonId, List<MultipartFile> files);

    void deleteMaterial(Long lessonId, Long materialId);

    LessonDetailPayload copyLesson(Long id, CopyLessonRequest request);

    Resource downloadMaterial(Long lessonId, Long materialId);
}
