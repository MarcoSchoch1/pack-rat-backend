# pack-rat-backend

Backend API for Pack Rat, an inventory/storage tracking app for keeping tabs on the stuff you own.

Built with Spring Boot (Web, Data JPA, Security, Validation) and PostgreSQL. Local Postgres is provisioned automatically via Docker Compose (`compose.yaml`) on startup — see `HELP.md` for details.

## Requirements

- Java 21
- Docker Desktop (running, for the local Postgres container)

## Configuration

Local config lives in `src/main/resources/application-local.properties` (gitignored). Create it from the example:

```
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

Then set:

- `jwt.secret`: a base64-encoded 256-bit secret, e.g. from `openssl rand -base64 32`
- `app.cors.allowed-origins`: comma-separated frontend origins (default `http://localhost:4200`)

## Running

```
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Production

The `prod` profile reads its config from environment variables:

| Variable | Purpose |
|---|---|
| `DATABASE_URL` | JDBC URL of the Postgres database |
| `POSTGRES_USER` | Database user |
| `POSTGRES_PASSWORD` | Database password |
| `JWT_SECRET` | Base64-encoded 256-bit JWT signing secret |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed frontend origins |
