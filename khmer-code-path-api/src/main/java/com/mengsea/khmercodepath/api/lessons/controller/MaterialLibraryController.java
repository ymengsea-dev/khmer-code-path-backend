package com.mengsea.khmercodepath.api.lessons.controller;

import com.mengsea.khmercodepath.api.lessons.payload.AssignLibraryItemRequest;
import com.mengsea.khmercodepath.api.lessons.payload.CreateLibraryItemRequest;
import com.mengsea.khmercodepath.api.lessons.payload.LessonDetailPayload;
import com.mengsea.khmercodepath.api.ai.payload.GenerateFromMaterialRequest;
import com.mengsea.khmercodepath.api.ai.payload.QuizGeneratePayload;
import com.mengsea.khmercodepath.api.ai.service.LibraryAiService;
import com.mengsea.khmercodepath.api.lessons.payload.LibraryMaterialPayload;
import com.mengsea.khmercodepath.api.lessons.payload.MaterialLibraryItemPayload;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
