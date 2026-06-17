package com.mengsea.khmercodepath.api.profile.controller;

import com.mengsea.khmercodepath.api.auth.payload.UserResponse;
import com.mengsea.khmercodepath.api.profile.service.ProfileAvatarService;
import com.mengsea.khmercodepath.commons.api.ApiResponse;
import com.mengsea.khmercodepath.commons.api.ApiResponses;
import com.mengsea.khmercodepath.commons.config.SwaggerConfig;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.LmsStatusCode;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/profile/avatar")
@RequiredArgsConstructor
@Tag(name = "Profile Avatar", description = "Profile picture upload and retrieval")
public class ProfileAvatarController {

    private final ProfileAvatarService profileAvatarService;

    @Operation(summary = "SET-1250 · Upload profile avatar")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
    public ResponseEntity<ApiResponse<UserResponse>> uploadAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        UserResponse user = profileAvatarService.uploadAvatar(authentication.getName(), file);
        return ResponseEntity.ok(ApiResponses.of("SET-1250", LmsStatusCode.SUCCESS, null, user));
    }

    @Operation(summary = "GET profile avatar image")
    @GetMapping("/{userId}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String userId) {
        ProfileAvatarService.AvatarResource avatar = profileAvatarService.getAvatar(userId);
        try {
            byte[] bytes = avatar.resource().getInputStream().readAllBytes();
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noCache())
                    .contentType(avatar.contentType())
                    .body(bytes);
        } catch (java.io.IOException ex) {
            throw new BusinessException(ExceptionCode.FILE_STORAGE_FAILED);
        }
    }
}
