# pack-rat-backend

Backend API for Pack Rat, an inventory/storage tracking app for keeping tabs on the stuff you own.

Built with Spring Boot (Web, Data JPA, Security, Validation) and PostgreSQL. Local Postgres is provisioned automatically via Docker Compose (`compose.yaml`) on startup — see `HELP.md` for details.

## Requirements

- Java 21
- Docker Desktop (running, for the local Postgres container)

## Running

```
./mvnw spring-boot:run
```
