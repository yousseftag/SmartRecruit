package com.smartrecruit.backend.integration.storage;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

  private final MinioClient minioClient;

  @Value("${minio.bucket}")
  private String bucketName;

  @PostConstruct
  public void initBucket() {
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

  /** Uploads a file to MinIO and returns the storage key. */
  public String uploadFile(MultipartFile file, String storageKey) {
    try {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucketName).object(storageKey).stream(
                  file.getInputStream(), file.getSize(), -1)
              .contentType(file.getContentType())
              .build());
      log.info("Successfully uploaded file to MinIO: {}", storageKey);
      return storageKey;
    } catch (Exception e) {
      log.error("Error uploading file to MinIO with key: {}", storageKey, e);
      throw new RuntimeException("Failed to upload file to storage", e);
    }
  }

  /** Downloads a file from MinIO. */
  public byte[] downloadFile(String storageKey) {
    try (InputStream stream =
        minioClient.getObject(
            GetObjectArgs.builder().bucket(bucketName).object(storageKey).build())) {
      return stream.readAllBytes();
    } catch (Exception e) {
      log.error("Error downloading file from MinIO with key: {}", storageKey, e);
      throw new RuntimeException("Failed to download file from storage", e);
    }
  }
}
