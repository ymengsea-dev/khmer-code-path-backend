package com.mengsea.khmercodepath.api.profile.service;

import com.mengsea.khmercodepath.api.auth.mapper.UserMapper;
import com.mengsea.khmercodepath.api.auth.payload.UserResponse;
import com.mengsea.khmercodepath.api.storage.AvatarUploadValidator;
import com.mengsea.khmercodepath.api.storage.UploadStorage;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfileAvatarService {

    private final UserRepository userRepository;
    private final UploadStorage uploadStorage;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse uploadAvatar(String email, MultipartFile file) {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));

        String previousKey = user.getAvatarStorageKey();
        UploadStorage.StoredFile stored = uploadStorage.storeAvatar(user.getUuid(), file);

        user.setAvatarStorageKey(stored.storageKey());
        userRepository.save(user);

        if (previousKey != null && !previousKey.isBlank() && !previousKey.equals(stored.storageKey())) {
            try {
                uploadStorage.delete(previousKey);
            } catch (RuntimeException ignored) {
                /* best-effort cleanup */
            }
        }

        return userMapper.toResponse(user);
    }

    public AvatarResource getAvatar(String userId) {
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));

        String storageKey = user.getAvatarStorageKey();
        if (storageKey == null || storageKey.isBlank()) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }

        Resource resource = uploadStorage.loadAsResource(storageKey);
        return new AvatarResource(resource, contentTypeForKey(storageKey));
    }

    private static MediaType contentTypeForKey(String key) {
        return switch (AvatarUploadValidator.extension(key)) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    public record AvatarResource(Resource resource, MediaType contentType) {}
}
