# Storage (MinIO)

## Purpose

MinIO is the S3-compatible object store for CV documents. The backend is the only service that writes to it; the FastAPI engine may read documents using the `storage_key` it receives.

## Configuration

Endpoint, credentials, and bucket (`resumes`) are configured through the `MINIO_*` environment variables and the `minio.*` keys. See [Configuration](../07-operations/configuration.md#minio) for the full list.

The bucket is created once at startup by `MinioConfig` (a `@Bean` for `MinioClient` runs `bucketExists` + `makeBucket`), avoiding per-upload existence checks.

## Object keys

Files are stored as `resumes/{uuid}.{ext}`. The key is persisted on `cv_file.storage_key` and included in the CV processing message so the engine can download the file directly.

## Deduplication

`cv_file.checksum_sha256` is `UNIQUE` and enforced at the database level. On upload the backend computes the SHA-256 hash:

- **New hash** — upload the binary to MinIO, create the `cv_file`.
- **Existing hash** — skip the MinIO upload and reuse the stored document.

Extraction is reused across applications; scoring is always recomputed per application against the offer's criteria. See [CV Ingestion](../04-features/cv-ingestion.md).

## Access pattern

CVs are streamed to authenticated users via `GET /api/v1/applications/{id}/cv` with an inline content disposition. There are no public/presigned URLs.

## Related

- [CV Ingestion](../04-features/cv-ingestion.md)
- [Configuration](../07-operations/configuration.md)
- [Database](../03-architecture/database.md)
