package com.mengsea.khmercodepath.api.lessons.controller;

import com.mengsea.khmercodepath.api.lessons.payload.AssignLibraryItemRequest;
import com.mengsea.khmercodepath.api.lessons.payload.CreateLibraryItemRequest;
import com.mengsea.khmercodepath.api.lessons.payload.LessonDetailPayload;
import com.mengsea.khmercodepath.api.ai.payload.GenerateFromContentRequest;
import com.mengsea.khmercodepath.api.ai.payload.GenerateFromMaterialRequest;
import com.mengsea.khmercodepath.api.ai.payload.LessonSummaryGeneratePayload;
import com.mengsea.khmercodepath.api.ai.payload.QuizGeneratePayload;
import com.mengsea.khmercodepath.api.ai.service.LibraryAiService;
import com.mengsea.khmercodepath.api.lessons.payload.LinkLibraryMaterialsRequest;
import com.mengsea.khmercodepath.api.lessons.payload.LibraryMaterialPayload;
import com.mengsea.khmercodepath.api.lessons.payload.MaterialLibraryConfigPayload;
import com.mengsea.khmercodepath.api.lessons.payload.MaterialLibraryItemPayload;
import com.mengsea.khmercodepath.api.lessons.payload.UpdateLibraryItemRequest;
import com.mengsea.khmercodepath.api.lessons.service.MaterialLibraryService;
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
@RequestMapping("/api/v1/materials/library")
@RequiredArgsConstructor
@Tag(name = "Material Library", description = "LSN-0480 — reusable lesson templates")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
@PreAuthorize("hasAuthority('" + LmsAuthority.LSN_MANAGE + "')")
public class MaterialLibraryController {

    private final MaterialLibraryService materialLibraryService;
    private final LibraryAiService libraryAiService;

    @Operation(summary = "Library UI config (tabs, create defaults, upload accept)")
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<MaterialLibraryConfigPayload>> getConfig() {
        MaterialLibraryConfigPayload data = materialLibraryService.getLibraryConfig();
        return ResponseEntity.ok(ApiResponses.of("LSN-0489", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "List standalone files in teacher file pool (not tied to a lesson template)")
    @GetMapping("/files")
    public ResponseEntity<ApiResponse<List<LibraryMaterialPayload>>> listPoolFiles(
            @RequestParam(required = false) String search
    ) {
        List<LibraryMaterialPayload> data = materialLibraryService.listPoolFiles(search);
        return ResponseEntity.ok(ApiResponses.of("LSN-0491", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "Upload files to teacher file pool (Course Content Library storage)")
    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> uploadPoolFiles(
            @RequestPart("files") List<MultipartFile> files
    ) {
        materialLibraryService.uploadPoolFiles(files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("LSN-0492", LmsStatusCode.CREATED, null, null));
    }

    @Operation(summary = "Delete a file from teacher file pool")
    @DeleteMapping("/files/{materialId}")
    public ResponseEntity<ApiResponse<Void>> deletePoolFile(@PathVariable Long materialId) {
        materialLibraryService.deletePoolFile(materialId);
        return ResponseEntity.ok(ApiResponses.of("LSN-0493", LmsStatusCode.SUCCESS, null, null));
    }

    @Operation(summary = "LSN-0480 · List material library")
    @GetMapping
    public ResponseEntity<ApiResponse<List<MaterialLibraryItemPayload>>> listLibrary(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String moduleTag
    ) {
        List<MaterialLibraryItemPayload> data = materialLibraryService.listLibrary(search, moduleTag);
        return ResponseEntity.ok(ApiResponses.of("LSN-0480", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "Create library template")
    @PostMapping
    public ResponseEntity<ApiResponse<MaterialLibraryItemPayload>> createItem(
            @Valid @RequestBody CreateLibraryItemRequest request
    ) {
        MaterialLibraryItemPayload data = materialLibraryService.createLibraryItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("LSN-0481", LmsStatusCode.CREATED, null, data));
    }

    @Operation(summary = "Get library template")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MaterialLibraryItemPayload>> getItem(@PathVariable Long id) {
        MaterialLibraryItemPayload data = materialLibraryService.getLibraryItem(id);
        return ResponseEntity.ok(ApiResponses.of("LSN-0485", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "Update library template")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MaterialLibraryItemPayload>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLibraryItemRequest request
    ) {
        MaterialLibraryItemPayload data = materialLibraryService.updateLibraryItem(id, request);
        return ResponseEntity.ok(ApiResponses.of("LSN-0486", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "Delete library template")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        materialLibraryService.deleteLibraryItem(id);
        return ResponseEntity.ok(ApiResponses.of("LSN-0487", LmsStatusCode.SUCCESS, null, null));
    }

    @Operation(summary = "Delete file from library template")
    @DeleteMapping("/{id}/materials/{materialId}")
    public ResponseEntity<ApiResponse<Void>> deleteMaterial(
            @PathVariable Long id,
            @PathVariable Long materialId
    ) {
        materialLibraryService.deleteLibraryMaterial(id, materialId);
        return ResponseEntity.ok(ApiResponses.of("LSN-0488", LmsStatusCode.SUCCESS, null, null));
    }

    @Operation(summary = "Attach existing library files to another template (copy)")
    @PostMapping("/{id}/materials/link")
    public ResponseEntity<ApiResponse<List<LibraryMaterialPayload>>> linkMaterials(
            @PathVariable Long id,
            @Valid @RequestBody LinkLibraryMaterialsRequest request
    ) {
        List<LibraryMaterialPayload> data = materialLibraryService.linkLibraryMaterials(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("LSN-0490", LmsStatusCode.CREATED, null, data));
    }

    @Operation(summary = "Upload library materials")
    @PostMapping(value = "/{id}/materials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> uploadMaterials(
            @PathVariable Long id,
            @RequestPart("files") List<MultipartFile> files
    ) {
        materialLibraryService.uploadLibraryMaterials(id, files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("LSN-0482", LmsStatusCode.CREATED, null, null));
    }

    @Operation(summary = "List files uploaded to a library template")
    @GetMapping("/{id}/materials")
    public ResponseEntity<ApiResponse<List<LibraryMaterialPayload>>> listMaterials(
            @PathVariable Long id
    ) {
        List<LibraryMaterialPayload> data = materialLibraryService.listLibraryMaterials(id);
        return ResponseEntity.ok(ApiResponses.of("LSN-0484", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "QUIZ-0500 · Generate quiz from library template material (on-demand RAG)")
    @PostMapping("/{id}/quizzes/generate")
    @PreAuthorize("hasAuthority('" + LmsAuthority.AI_INGEST + "')")
    public ResponseEntity<ApiResponse<QuizGeneratePayload>> generateQuiz(
            @PathVariable Long id,
            @Valid @RequestBody GenerateFromMaterialRequest request
    ) {
        QuizGeneratePayload data = libraryAiService.generateQuiz(id, request);
        return ResponseEntity.ok(ApiResponses.of("QUIZ-0500", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "QUIZ-0501 · Generate quiz from template written notes (no uploaded file needed)")
    @PostMapping("/{id}/quizzes/generate-from-content")
    @PreAuthorize("hasAuthority('" + LmsAuthority.AI_INGEST + "')")
    public ResponseEntity<ApiResponse<QuizGeneratePayload>> generateQuizFromContent(
            @PathVariable Long id,
            @Valid @RequestBody GenerateFromContentRequest request
    ) {
        QuizGeneratePayload data = libraryAiService.generateQuizFromContent(id, request);
        return ResponseEntity.ok(ApiResponses.of("QUIZ-0501", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "SUM-0601 · Generate summary from template written notes (no uploaded file needed)")
    @PostMapping("/{id}/summary/from-content")
    @PreAuthorize("hasAuthority('" + LmsAuthority.AI_INGEST + "')")
    public ResponseEntity<ApiResponse<LessonSummaryGeneratePayload>> generateSummaryFromContent(
            @PathVariable Long id
    ) {
        LessonSummaryGeneratePayload data = libraryAiService.generateSummaryFromContent(id);
        return ResponseEntity.ok(ApiResponses.of("SUM-0601", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "Assign library template to class")
    @PostMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<LessonDetailPayload>> assignToClass(
            @PathVariable Long id,
            @Valid @RequestBody AssignLibraryItemRequest request
    ) {
        LessonDetailPayload data = materialLibraryService.assignToClass(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponses.of("LSN-0483", LmsStatusCode.CREATED, null, data));
    }
}
