# Architecture

This page explains the boilerplate that already exists, so you know what you get for free and what you should not rewrite.

## Package structure

```
com.switchfully.spring.parkshark
├── ParksharkApplication          entry point
├── common/                       cross-cutting code, used by every feature
│   ├── api/                      GlobalExceptionHandler: exceptions → HTTP responses
│   ├── config/                   ClockConfig: the injectable Clock
│   ├── exception/                NotFoundException (404), ConflictException (409)
│   ├── openapi/                  Swagger / OpenAPI configuration
│   └── startup/                  checks the DB_* configuration at startup
└── feature/                      one package per business feature
    └── security/
        ├── authentication/       Spring Security's view of a user (UserDetails)
        ├── config/               security filter chain, password encoder
        ├── domain/               Credentials entity + repository, Role, Feature
        ├── exception/            EmailAlreadyUsedException
        └── service/              CredentialsService
```

We package **by feature**, not by layer: everything about parking lots lives in `feature/parkinglot/`, not spread over a global `controllers/`, `services/` and `repositories/`.

## Request lifecycle

```
HTTP request
  → Spring Security: HTTP Basic authentication (stateless, no session)
  → Controller (feature/xxx/api): validates the DTO with @Valid, checks @Secured
  → Service (feature/xxx/service): business logic, @Transactional
  → Repository (feature/xxx/domain): Spring Data JPA
  → PostgreSQL

Any exception thrown on the way → GlobalExceptionHandler → ProblemDetail JSON
```

## Configuration and startup

| Piece | What it does |
|---|---|
| `application.properties` | Reads `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` from the environment or from the `.env` file (`spring.config.import=optional:file:.env[.properties]`). |
| `RequiredConfigurationEnvironmentPostProcessor` | Runs before anything else. If a `DB_*` key is missing or blank, stops the app right away. |
| `MissingConfigurationFailureAnalyzer` | Turns that failure into a readable "APPLICATION FAILED TO START" message telling you to copy `.env.example`. |
| `META-INF/spring.factories` | Registers the two classes above (they run before the Spring context exists, so they can't be normal beans). |

## Database

- **Flyway owns the schema.** Every table is created or changed by a SQL file in `src/main/resources/db/migration`.
- `spring.jpa.hibernate.ddl-auto=validate`: Hibernate **never** creates tables. It only checks at startup that your `@Entity` classes match the tables. If they don't, the app won't start: write a migration.
- `spring.jpa.open-in-view=false`: the DB session closes when the service method returns. Lazy relations must be loaded inside the `@Transactional` service, not in the controller. Map entities to DTOs in the service.
- Tests run on H2 in PostgreSQL mode (`src/test/resources/application.properties`) with the same Flyway migrations.
- Migrations in `db/vendor/postgresql` only run on PostgreSQL (`spring.flyway.locations=classpath:db/migration,classpath:db/vendor/{vendor}`), e.g. `V2__insert_manager.sql`, which uses `pgcrypto`. They are skipped on H2.

## Error handling

`GlobalExceptionHandler` turns every exception into a [ProblemDetail](https://www.rfc-editor.org/rfc/rfc9457) JSON body:

| Thrown | Status | Body `detail` |
|---|---|---|
| `NotFoundException` (or a subclass) | 404 | the exception message |
| `ConflictException` (or a subclass, e.g. `EmailAlreadyUsedException`) | 409 | the exception message |
| `OptimisticLockingFailureException` | 409 | "please retry" |
| `IllegalArgumentException` | 400 | the exception message |
| `@Valid` failure on a request body | 400 | "Validation failed" + an `errors` map: field → sorted messages |
| Malformed JSON / wrong type | 400 | names the faulty field, e.g. `lines[1].amount` |
| Bad or missing credentials | 401 | "Invalid credentials" (never says why) |
| `@Secured` check fails | 403 | names the user and the missing feature |
| Anything else | 500 | generic message; the real error is only logged |

Example validation error:
```json
{
  "status": 400,
  "detail": "Validation failed",
  "errors": { "email": ["Email must be valid"] }
}
```

## Security

- **HTTP Basic, stateless.** Every request sends `Authorization: Basic base64(email:password)`. No session, no cookie, so CSRF is disabled.
- **Everything requires authentication**, except the API docs (`/docs`, `/swagger-ui/**`, `/v3/api-docs/**`). An anonymous request to an unknown URL gets 401, not 404, so it doesn't reveal which URLs exist.
- **Users** are stored in the `credentials` table (email, BCrypt hash, role). `CredentialsUserDetailsService` loads them by email.
- **Roles grant features.** A `Role` (e.g. `MANAGER`) holds a set of `Feature`s (e.g. `CREATE_PARKING_LOT`). A logged-in user's authorities are the **feature names**, not the role. Endpoints check features with `@Secured("CREATE_PARKING_LOT")`. Want to give a role more rights? Change the enum, not the controllers.
- `Role` and `Feature` are still empty: add them as the stories need them (see [CONVENTIONS](CONVENTIONS.md#security)).
- Emails are normalized (trimmed, lowercased) with `Credentials.normalizeEmail` before saving and before lookup.
- `CredentialsService.create` refuses a duplicate email (`EmailAlreadyUsedException`, 409). The unique constraint in the DB catches the race when two requests arrive at the same time.

## API documentation (OpenAPI)

Swagger UI is at `/docs`. Most of it is generated for you:

- `OpenApiConfig` adds the `ProblemDetail` schema and, per endpoint, the standard 400 / 401 / 403 / 404 responses based on its parameters and `@Secured`.
- `SecurityOpenApiConfig` adds to each endpoint's description which feature it needs and which roles have it.

So you usually only need `@Operation(summary = "...")` on a controller method.

## Time

`ClockConfig` exposes a `java.time.Clock` bean. Inject it and use `LocalDate.now(clock)` instead of `LocalDate.now()`. In tests, `MutableClock` (test sources) lets you fix and move the time.

## Null safety

`package-info.java` with `@NullMarked` (JSpecify) means: in this package, nothing is null unless annotated `@Nullable`. It's on the `authentication` package. You may add it to new packages.
