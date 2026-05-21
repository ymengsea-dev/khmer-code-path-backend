package com.mengsea.khmercodepath.api.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ConditionalOnProperty(name = "lms.storage.provider", havingValue = "minio", matchIfMissing = true)
@EnableConfigurationProperties(MinioProperties.class)
@RequiredArgsConstructor
@Slf4j
public class MinioConfig {

    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.getUrl())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    @Bean
    @Profile("!test")
    public MinioBucketInitializer minioBucketInitializer(MinioClient minioClient) {
        return new MinioBucketInitializer(minioClient, minioProperties);
    }

    @RequiredArgsConstructor
    static class MinioBucketInitializer {
        private final MinioClient minioClient;
        private final MinioProperties properties;

        @jakarta.annotation.PostConstruct
        void ensureBucket() {
            try {
                String bucket = properties.getBucketName();
                boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
                if (!exists) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                    log.info("Created MinIO bucket: {}", bucket);
                }
            } catch (Exception ex) {
                log.warn("Could not verify MinIO bucket (is MinIO running?): {}", ex.getMessage());
            }
        }
    }
}
