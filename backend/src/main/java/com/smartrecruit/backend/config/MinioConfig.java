package com.smartrecruit.backend.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MinioConfig {

  @Value("${minio.url}")
  private String url;

  @Value("${minio.access-key}")
  private String accessKey;

  @Value("${minio.secret-key}")
  private String secretKey;

  @Value("${minio.bucket}")
  private String bucketName;

  @Bean
  public MinioClient minioClient() {
    MinioClient client =
        MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build();
    initBucket(client);
    return client;
  }

  private void initBucket(MinioClient minioClient) {
    try {
      boolean found =
          minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
      if (!found) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        log.info("Created MinIO bucket: {}", bucketName);
      } else {
        log.info("MinIO bucket '{}' already exists.", bucketName);
      }
    } catch (Exception e) {
      log.error("Failed to initialize MinIO bucket '{}'", bucketName, e);
      throw new RuntimeException("MinIO initialization failed", e);
    }
  }
}
