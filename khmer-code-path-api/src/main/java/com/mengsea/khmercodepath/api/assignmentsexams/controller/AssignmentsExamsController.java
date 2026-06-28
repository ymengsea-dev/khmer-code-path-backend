package com.mengsea.khmercodepath.api.assignmentsexams.controller;

import com.mengsea.khmercodepath.api.assignmentsexams.payload.TaskContentUploadResponse;
import com.mengsea.khmercodepath.api.assignmentsexams.service.AssignmentsExamsContentService;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.config.SwaggerConfig;
import com.mengsea.khmercodepath.commons.constant.LmsAuthority;
import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/assignments-exams")
@RequiredArgsConstructor
@Tag(name = "Assignments & Exams", description = "Task content upload and download")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
public class AssignmentsExamsController {

    private final AssignmentsExamsContentService assignmentsExamsContentService;

    @Operation(summary = "ASGN-0701 · Upload supplemental file for assignment/exam content")
    @PostMapping(value = "/content/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
    public ResponseEntity<ApiResponse<TaskContentUploadResponse>> uploadContentFile(
            @RequestPart("file") MultipartFile file
    ) {
        TaskContentUploadResponse data = assignmentsExamsContentService.uploadTaskFile(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("ASGN-0701", LmsStatusCode.CREATED, null, data));
    }

    @Operation(summary = "ASGN-0702 · Download uploaded task content file")
    @GetMapping("/content/file-download")
    @PreAuthorize("hasAnyAuthority('" + LmsAuthority.LSN_MANAGE + "', '" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<Resource> downloadContentFile(
            @RequestParam String storageKey,
            @RequestParam(required = false) Long assignmentId,
            @RequestParam(required = false) Long examId
    ) {
        Resource resource = assignmentsExamsContentService.downloadTaskFile(
                assignmentId, examId, storageKey);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .body(resource);
    }

    @Operation(summary = "ASGN-0703 · Download library material referenced in task content")
    @GetMapping("/content/library-download")
    @PreAuthorize("hasAnyAuthority('" + LmsAuthority.LSN_MANAGE + "', '" + LmsAuthority.CLS_READ + "')")
    public ResponseEntity<Resource> downloadLibraryContent(
            @RequestParam Long libraryItemId,
            @RequestParam Long materialId,
            @RequestParam(required = false) Long assignmentId,
            @RequestParam(required = false) Long examId
    ) {
        Resource resource = assignmentsExamsContentService.downloadLibraryMaterial(
                assignmentId, examId, libraryItemId, materialId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .body(resource);
    }
}
