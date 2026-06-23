package com.mengsea.khmercodepath.api.storage;

import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class MinioUploadStorage implements UploadStorage {

    private static final int MAX_FILES_PER_BATCH = 10;
    private static final int MIN_PART_SIZE_BYTES = 5 * 1024 * 1024;

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public int maxFilesPerBatch() {
        return MAX_FILES_PER_BATCH;
    }

    @Override
    public StoredFile store(String category, Long ownerId, MultipartFile file) {
        MaterialUploadValidator.validateFile(file);
        ensureBucketExists();
        String safeName = MaterialUploadValidator.sanitizeFileName(file.getOriginalFilename());
        String key = category + "/" + ownerId + "/" + UUID.randomUUID() + "_" + safeName;
        long size = resolveObjectSize(file);
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(key)
                            .stream(in, size, size < 0 ? MIN_PART_SIZE_BYTES : -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("MinIO upload failed for object {} in bucket {}", key,
                    minioProperties.getBucketName(), ex);
            throw new BusinessException(ExceptionCode.FILE_STORAGE_FAILED);
        }
        return new StoredFile(key, safeName, file.getContentType(), size >= 0 ? size : file.getSize());
    }

    @Override
    public StoredFile storeAvatar(String userId, MultipartFile file) {
        AvatarUploadValidator.validateFile(file);
        ensureBucketExists();
        String safeName = AvatarUploadValidator.sanitizeFileName(file.getOriginalFilename());
        String key = "avatars/" + userId + "/" + UUID.randomUUID() + "_" + safeName;
        long size = resolveObjectSize(file);
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(key)
                            .stream(in, size, size < 0 ? MIN_PART_SIZE_BYTES : -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("MinIO avatar upload failed for object {} in bucket {}", key,
                    minioProperties.getBucketName(), ex);
            throw new BusinessException(ExceptionCode.FILE_STORAGE_FAILED);
        }
        return new StoredFile(key, safeName, file.getContentType(), size >= 0 ? size : file.getSize());
    }

    @Override
    public StoredFile storeSchoolCover(Long schoolId, MultipartFile file) {
        return storeCoverImage("school-covers/" + schoolId, file, "school cover");
    }

    @Override
    public StoredFile storeFacultyCover(Long facultyId, MultipartFile file) {
        return storeCoverImage("faculty-covers/" + facultyId, file, "faculty cover");
    }

    private StoredFile storeCoverImage(String keyPrefix, MultipartFile file, String logLabel) {
        SchoolCoverUploadValidator.validateFile(file);
        ensureBucketExists();
        String safeName = SchoolCoverUploadValidator.sanitizeFileName(file.getOriginalFilename());
        String key = keyPrefix + "/" + UUID.randomUUID() + "_" + safeName;
        long size = resolveObjectSize(file);
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(key)
                            .stream(in, size, size < 0 ? MIN_PART_SIZE_BYTES : -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("MinIO {} upload failed for object {} in bucket {}", logLabel, key,
                    minioProperties.getBucketName(), ex);
            throw new BusinessException(ExceptionCode.FILE_STORAGE_FAILED);
        }
        return new StoredFile(key, safeName, file.getContentType(), size >= 0 ? size : file.getSize());
    }

    private void ensureBucketExists() {
        String bucket = minioProperties.getBucketName();
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created MinIO bucket: {}", bucket);
            }
        } catch (Exception ex) {
            log.error("MinIO bucket '{}' is not available", bucket, ex);
            throw new BusinessException(ExceptionCode.FILE_STORAGE_FAILED);
        }
    }

    private static long resolveObjectSize(MultipartFile file) {
        long size = file.getSize();
        if (size >= 0) {
            return size;
        }
        try {
            return file.getBytes().length;
        } catch (Exception ex) {
            return -1;
        }
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
