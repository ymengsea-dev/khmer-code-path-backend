package com.mengsea.khmercodepath.api.notes.controller;

import com.mengsea.khmercodepath.api.notes.payload.CreateNoteRequest;
import com.mengsea.khmercodepath.api.notes.payload.NoteListPayload;
import com.mengsea.khmercodepath.api.notes.payload.NotePayload;
import com.mengsea.khmercodepath.api.notes.payload.UpdateNoteRequest;
import com.mengsea.khmercodepath.api.notes.service.NoteService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
@Tag(name = "Notes", description = "NOTE — per-user digital notebook (student & teacher)")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
@PreAuthorize("hasAuthority('" + LmsAuthority.AI_CHAT + "')")
public class NoteController {

    private final NoteService noteService;

    @Operation(summary = "NOTE-0900 · List notes")
    @GetMapping
    public ResponseEntity<ApiResponse<NoteListPayload>> list(
            @RequestParam(required = false) String search
    ) {
        NoteListPayload data = noteService.list(search);
        return ResponseEntity.ok(ApiResponses.of("NOTE-0900", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "NOTE-0910 · Get note")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotePayload>> get(@PathVariable Long id) {
        NotePayload data = noteService.get(id);
        return ResponseEntity.ok(ApiResponses.of("NOTE-0910", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "NOTE-0920 · Create note")
    @PostMapping
    public ResponseEntity<ApiResponse<NotePayload>> create(
            @Valid @RequestBody CreateNoteRequest request
    ) {
        NotePayload data = noteService.create(request);
        return ResponseEntity.ok(ApiResponses.of("NOTE-0920", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "NOTE-0940 · Update note")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NotePayload>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNoteRequest request
    ) {
        NotePayload data = noteService.update(id, request);
        return ResponseEntity.ok(ApiResponses.of("NOTE-0940", LmsStatusCode.SUCCESS, null, data));
    }

    @Operation(summary = "NOTE-0950 · Delete note")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        noteService.delete(id);
        return ResponseEntity.ok(ApiResponses.of("NOTE-0950", LmsStatusCode.SUCCESS, null, null));
    }
}
