package com.mengsea.khmercodepath.api.lessons.controller;

import com.mengsea.khmercodepath.api.lessons.payload.CopyLessonRequest;
import com.mengsea.khmercodepath.api.lessons.payload.CreateLessonRequest;
import com.mengsea.khmercodepath.api.lessons.payload.LessonDetailPayload;
import com.mengsea.khmercodepath.api.lessons.payload.LessonMaterialPayload;
import com.mengsea.khmercodepath.api.lessons.payload.LessonSummaryPayload;
import com.mengsea.khmercodepath.api.lessons.payload.UpdateLessonRequest;
import com.mengsea.khmercodepath.api.lessons.service.LessonManagementService;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.config.SwaggerConfig;
import com.mengsea.khmercodepath.commons.constant.LmsAuthority;
import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
@Tag(name = "Lesson Management", description = "LSN — lessons and materials (enrollment-based access)")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class LessonManagementController {

    private final LessonManagementService lessonManagementService;

    @Operation(summary = "LSN-0400 · List lessons")
    @GetMapping
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<List<LessonSummaryPayload>>> listLessons(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String teacherId
    ) {
        List<LessonSummaryPayload> data = lessonManagementService.listLessons(classId, teacherId);
        return ResponseEntity.ok(ApiResponses.of("LSN-0400", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "LSN-0410 · Get lesson details")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<ApiResponse<LessonDetailPayload>> getLesson(@PathVariable Long id) {
        LessonDetailPayload data = lessonManagementService.getLesson(id);
        return ResponseEntity.ok(ApiResponses.of("LSN-0410", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "LSN-0420 · Create lesson")
    @PostMapping
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<LessonDetailPayload>> createLesson(
            @Valid @RequestBody CreateLessonRequest request
    ) {
        LessonDetailPayload data = lessonManagementService.createLesson(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("LSN-0420", LmsStatusCode.CREATED, null, data));
    }

    @Operation(summary = "LSN-0430 · Update lesson")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<LessonDetailPayload>> updateLesson(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLessonRequest request
    ) {
        LessonDetailPayload data = lessonManagementService.updateLesson(id, request);
        return ResponseEntity.ok(ApiResponses.of("LSN-0430", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "LSN-0440 · Delete lesson")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(@PathVariable Long id) {
        lessonManagementService.deleteLesson(id);
        return ResponseEntity.ok(ApiResponses.of("LSN-0440", LmsStatusCode.SUCCESS, null, null));
    }

    @Operation(summary = "LSN-0450 · Upload lesson materials")
    @PostMapping(value = "/{id}/materials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<List<LessonMaterialPayload>>> uploadMaterials(
            @PathVariable Long id,
            @RequestPart("files") List<MultipartFile> files
    ) {
        List<LessonMaterialPayload> data = lessonManagementService.uploadMaterials(id, files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("LSN-0450", LmsStatusCode.CREATED, null, data));
    }

    @Operation(summary = "LSN-0460 · Delete lesson material")
    @DeleteMapping("/{lessonId}/materials/{materialId}")
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<Void>> deleteMaterial(
            @PathVariable Long lessonId,
            @PathVariable Long materialId
    ) {
        lessonManagementService.deleteMaterial(lessonId, materialId);
        return ResponseEntity.ok(ApiResponses.of("LSN-0460", LmsStatusCode.SUCCESS, null, null));
    }

    @Operation(summary = "LSN-0470 · Copy lesson to class")
    @PostMapping("/{id}/copy")
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<LessonDetailPayload>> copyLesson(
            @PathVariable Long id,
            @Valid @RequestBody CopyLessonRequest request
    ) {
        LessonDetailPayload data = lessonManagementService.copyLesson(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("LSN-0470", LmsStatusCode.CREATED, null, data));
    }

    @Operation(summary = "Download lesson material")
    @GetMapping("/{lessonId}/materials/{materialId}/download")
    @PreAuthorize("hasAuthority('" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<Resource> downloadMaterial(
            @PathVariable Long lessonId,
            @PathVariable Long materialId
    ) {
        Resource resource = lessonManagementService.downloadMaterial(lessonId, materialId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .body(resource);
    }
}
