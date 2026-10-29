# ParkSharkTeam1
Switchfully parkshark project: a Spring Boot back-end to manage parking lots.

## Rules
  - Daily standup
  - 1 branch per ticket/story
  - branch names: feat/story-title or  fix, refactor, perf/description
  - commits: feat(name-of-story) description

## Definition of Done
  - 80% test coverage (enforced by `mvnw verify`, see below)
  - all checks done
  - code review together before merge

## Getting started

### Prerequisites
- JDK 17 or newer
- A local PostgreSQL database (each developer has their own, never share one)
- No Maven install needed: use the wrapper (`mvnw` / `mvnw.cmd`)

### 1. Configure your database
Copy `.env.example` to `.env` and fill in your own values:

```properties
DB_URL=jdbc:postgresql://localhost:5432/parkshark
DB_USERNAME=postgres
DB_PASSWORD=secret
PASSWORD_MANAGER=managerSecret
```

`PASSWORD_MANAGER` is the password of the `manager@parkshark.com` account, created by the migration `db/vendor/postgresql/V2__insert_manager.sql`.

- `.env` is git-ignored: never commit it.
- It uses the `.properties` format: write values **without quotes**.
- If a key is missing, the app refuses to start and tells you which one.

### 2. Run the application
```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```
On startup Flyway creates or updates the tables from `src/main/resources/db/migration`.

API documentation (Swagger UI): http://localhost:8080/docs.
Use the **Authorize** button with your email and password (HTTP Basic).

### 3. Run the tests
```bash
./mvnw test      # tests only
./mvnw verify    # tests + coverage check (fails below 80% line coverage)
```
Tests use an in-memory H2 database: no PostgreSQL needed.
The coverage report is in `target/site/jacoco/index.html`.

> **IntelliJ error `No implementation for ArtifactHandlerManager was bound`?**
> Your IntelliJ does not support the Maven version of the wrapper.
> Go to *Settings → Build Tools → Maven → Maven home path* and pick **Bundled (Maven 3)**, or update IntelliJ.

## Documentation
- [Architecture](docs/ARCHITECTURE.md): what the boilerplate does and why
- [Conventions & best practices](docs/CONVENTIONS.md): how to add a feature, errors, security, migrations, tests, git

## Tech stack
Spring Boot 4 (Web MVC, Data JPA, Security, Validation) · PostgreSQL · Flyway · springdoc-openapi · JUnit 5, AssertJ, RestAssured, H2 · JaCoCo
