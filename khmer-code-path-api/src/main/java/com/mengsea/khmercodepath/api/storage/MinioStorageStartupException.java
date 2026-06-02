package com.mengsea.khmercodepath.api.storage;

/**
 * Thrown when the API starts without a reachable MinIO bucket (dev / prod).
 */
public class MinioStorageStartupException extends IllegalStateException {

    public MinioStorageStartupException(String message, Throwable cause) {
        super(message, cause);
    }
}
