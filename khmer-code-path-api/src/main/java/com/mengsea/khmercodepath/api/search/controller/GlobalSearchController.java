package com.mengsea.khmercodepath.api.search.controller;

import com.mengsea.khmercodepath.api.search.payload.GlobalSearchResultPayload;
import com.mengsea.khmercodepath.api.search.service.GlobalSearchService;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.config.SwaggerConfig;
import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Global Search", description = "Search classes, lessons, quizzes, and notebook notes")
@SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
@PreAuthorize("isAuthenticated()")
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GlobalSearchResultPayload>>> search(
            @RequestParam String q
    ) {
        List<GlobalSearchResultPayload> data = globalSearchService.search(q);
        return ResponseEntity.ok(ApiResponses.of("SRCH-0100", LmsStatusCode.SUCCESS, null, data));
    }
}
