package com.mengsea.khmercodepath.api.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Object storage for lesson / library materials (MinIO in production, local disk in tests).
 */
public interface UploadStorage {

    int maxFilesPerBatch();

    StoredFile store(String category, Long ownerId, MultipartFile file);

    Resource loadAsResource(String storageKey);

    InputStream openStream(String storageKey);

    void copyStorageFile(String sourceKey, String targetKey);

    void delete(String storageKey);

    record StoredFile(String storageKey, String fileName, String contentType, long sizeBytes) {}
}
