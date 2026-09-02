package com.smartrecruit.backend.integration.storage;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
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

  /** Uploads a file to MinIO and returns the storage key. */
  public String uploadFile(MultipartFile file, String storageKey) {
    try {
      return uploadFile(file.getInputStream(), file.getSize(), file.getContentType(), storageKey);
    } catch (Exception e) {
      log.error("Error reading file to upload with key: {}", storageKey, e);
      throw new RuntimeException("Failed to read file", e);
    }
  }

  public String uploadFile(InputStream stream, long size, String contentType, String storageKey) {
    try {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucketName).object(storageKey).stream(stream, size, -1)
              .contentType(contentType != null ? contentType : "application/octet-stream")
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
