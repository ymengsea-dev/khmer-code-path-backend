package com.mengsea.khmercodepath.api.storage;

import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "lms.storage.provider", havingValue = "minio", matchIfMissing = true)
@RequiredArgsConstructor
public class MinioUploadStorage implements UploadStorage {

    private static final int MAX_FILES_PER_BATCH = 10;

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public int maxFilesPerBatch() {
        return MAX_FILES_PER_BATCH;
    }

    @Override
    public StoredFile store(String category, Long ownerId, MultipartFile file) {
        LocalUploadStorage.validateFile(file);
        String safeName = LocalUploadStorage.sanitizeFileName(file.getOriginalFilename());
        String key = category + "/" + ownerId + "/" + UUID.randomUUID() + "_" + safeName;
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(key)
                            .stream(in, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception ex) {
            throw new BusinessException(ExceptionCode.INTERNAL_SERVER_ERROR);
        }
        return new StoredFile(key, safeName, file.getContentType(), file.getSize());
    }

    @Override
    public Resource loadAsResource(String storageKey) {
        return new InputStreamResource(openStream(storageKey)) {
            @Override
            public String getFilename() {
                return storageKey;
            }
        };
    }

    @Override
    public InputStream openStream(String storageKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(storageKey)
                            .build()
            );
        } catch (Exception ex) {
            throw new BusinessException(ExceptionCode.VALIDATION_ERROR);
        }
    }

    @Override
    public void copyStorageFile(String sourceKey, String targetKey) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(targetKey)
                            .source(CopySource.builder()
                                    .bucket(minioProperties.getBucketName())
                                    .object(sourceKey)
                                    .build())
                            .build()
            );
        } catch (Exception ex) {
            throw new BusinessException(ExceptionCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(storageKey)
                            .build()
            );
        } catch (Exception ex) {
            throw new BusinessException(ExceptionCode.INTERNAL_SERVER_ERROR);
        }
    }
}
