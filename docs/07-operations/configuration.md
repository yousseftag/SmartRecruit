# Configuration

Configuration is centralized in `.env` at the project root (copy from `.env.example`). Spring Boot also imports `.env` as optional properties. Environment variables override the defaults in `application.yml`.

## Application

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8080` | Backend HTTP port |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200,http://localhost:8080` | Allowed frontend origins |
| `AI_SIMULATION_ENABLED` | `true` | Run in-JVM AI simulators |
| `CV_STALL_THRESHOLD_MINUTES` | `5` | CV pending before `STALLED` |
| `OFFER_STALL_THRESHOLD_MINUTES` | `2` | Offer pending before `STALLED` |
| `APP_SECURITY_PASSWORD_LENGTH` | `12` | Generated user password length |

## Database

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/smartrecruit` | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | DB password |
| `POSTGRES_HOST` | `localhost` | Used to compose the default JDBC URL |
| `POSTGRES_DB` | `smartrecruit` | Database name |

## RabbitMQ

| Variable | Default | Description |
|----------|---------|-------------|
| `RABBITMQ_HOST` | `localhost` | Broker host |
| `RABBITMQ_USER` | `guest` | Broker user |
| `RABBITMQ_PASS` | `guest` | Broker password |

## MinIO

| Variable | Default | Description |
|----------|---------|-------------|
| `MINIO_URL` | `http://localhost:9000` | S3 endpoint |
| `MINIO_ROOT_USER` | `minioadmin` | Access key |
| `MINIO_ROOT_PASSWORD` | `minioadmin` | Secret key |

Bucket: `resumes` (fixed in `application.yml`).

## Keycloak

| Variable | Default | Description |
|----------|---------|-------------|
| `KEYCLOAK_URL` | `http://localhost:8081` | Realm base URL |
| `KEYCLOAK_SERVER_URL` | `http://localhost:8081` | Admin API base URL |
| `KEYCLOAK_BACKEND_CLIENT_ID` | `smartrecruit-backend` | Service account client |
| `KEYCLOAK_BACKEND_SECRET` | `smartrecruit-backend-secret` | Service account secret |

## Mail (SMTP)

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_MAIL_HOST` | `localhost` | SMTP host (Mailpit) |
| `SPRING_MAIL_PORT` | `1025` | SMTP port |
| `SPRING_MAIL_FROM` | `noreply@smartrecruit.com` | Sender address |

## Frontend

The Angular app is configured in `src/environments/environment.ts`:

| Setting | Default |
|---------|---------|
| Keycloak URL / realm / client | `http://localhost:8081` / `smartrecruit` / `smartrecruit-frontend` |
| API URL | `http://localhost:8080` |
| Polling interval / backoff / max | 3 s / ×1.5 / 15 s |
| Stalled after attempts | 10 |

## Related

- [Getting Started](../02-getting-started.md)
- [Migrations](migrations.md)
- [Keycloak Integration](../05-integrations/keycloak.md)
