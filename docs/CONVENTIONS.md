# Conventions & best practices

Read [ARCHITECTURE](ARCHITECTURE.md) first for what the existing code does.

## Adding a feature: checklist

Example: story "a manager can create a parking lot".

1. **Branch**: `feat/create-parking-lot` from an up-to-date `main`.
2. **Migration**: a new file in `db/migration`, e.g. `V2__create_parking_lot.sql` (see [Database](#database-migrations)).
3. **Domain**: in `feature/parkinglot/domain/`, an `@Entity ParkingLot` and a `ParkingLotRepository extends JpaRepository`.
4. **Security**: add a `CREATE_PARKING_LOT` value to `Feature` and give it to the right `Role`.
5. **Service**: in `feature/parkinglot/service/`, a `@Service @Transactional` class with the business logic.
6. **API**: in `feature/parkinglot/api/`, a `@RestController` with request/response DTOs (records).
7. **Tests**: unit tests for the domain and service, an end-to-end test for the endpoint.
8. **Check**: `./mvnw verify` is green, then open a PR and review it together.

## Package layout of a feature

```
feature/parkinglot/
├── api/          ParkingLotController, CreateParkingLotRequest, ParkingLotResponse
├── domain/       ParkingLot (entity), ParkingLotRepository, value objects, enums
├── service/      ParkingLotService
└── exception/    ParkingLotNotFoundException extends NotFoundException
```

- Code shared by several features goes in `common/`, not into another feature.
- A feature may use another feature's **service**, not reach into its repository.

## Controllers

```java
@RestController
@RequestMapping("/parking-lots")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    public ParkingLotController(ParkingLotService parkingLotService) {
        this.parkingLotService = parkingLotService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Secured("CREATE_PARKING_LOT")
    @Operation(summary = "Create a parking lot")
    public ParkingLotResponse create(@Valid @RequestBody CreateParkingLotRequest request) {
        return parkingLotService.create(request);
    }
}
```

- **Thin controllers**: no business logic, no repository access, no `try/catch`. Throw exceptions and let `GlobalExceptionHandler` answer.
- **Never return or accept an `@Entity`**: use DTOs (`record`s). It keeps internal fields (e.g. `passwordHash`) out of the API.
- **Validate input** with Bean Validation on the DTO, and give every constraint a message:
  ```java
  public record CreateParkingLotRequest(
          @NotBlank(message = "Name is required") String name,
          @Positive(message = "Capacity must be positive") int capacity) {
  }
  ```
- URLs: plural nouns in kebab-case (`/parking-lots/{id}`), HTTP verbs for actions. Return 201 on creation.
- Constructor injection only (no `@Autowired` on fields).

## Services and domain

- Put `@Transactional` on the service class (`readOnly = true` on read-only services).
- Business rules belong in the entity or the service, not in the controller. An entity that protects its own invariants (constructor checks, no public setters) is easier to test.
- Signal errors with exceptions:
  - not found → a subclass of `NotFoundException` (404)
  - duplicate / conflicting state → a subclass of `ConflictException` (409)
  - invalid argument that `@Valid` can't catch → `IllegalArgumentException` (400)
- Create one exception class per business case (e.g. `ParkingLotNotFoundException`) with a clear message: that message is what the client sees.
- Use `Clock` for the current date and time, never `LocalDate.now()` directly.
- Normalize emails with `Credentials.normalizeEmail` before storing or searching.

## Security

- Every endpoint is authenticated by default. Add `@Secured("FEATURE_NAME")` on each controller method to restrict it to a feature.
- **Check features, not roles**: `@Secured("CREATE_PARKING_LOT")`, never `@Secured("MANAGER")`.
- Add features and roles in the enums:
  ```java
  public enum Feature {
      CREATE_PARKING_LOT
  }

  public enum Role {
      MANAGER(Feature.CREATE_PARKING_LOT),
      MEMBER();
      ...
  }
  ```
- Create users through `CredentialsService.create(email, rawPassword, role)`: it hashes the password and refuses duplicate emails. Never store a raw password.
- Never log passwords, hashes or full request bodies containing them.

## Database migrations

- **Never modify a migration that is already on `main`.** Flyway stores a checksum of each applied file; changing it breaks every database that already ran it. Write a new migration instead.
- Naming: `V<version>__<description>.sql`, with **two** underscores, e.g. `V2__create_parking_lot.sql`.
- **Avoid version collisions between branches.** Two branches that both add a `V2` will conflict at merge time. Either announce the number you take in the team chat, or use a timestamp as version: `V202610281430__create_parking_lot.sql` (it sorts after `V1`).
- Keep the SQL portable: the tests run the same migrations on H2 (PostgreSQL mode). Prefer standard types (`uuid`, `varchar`, `integer`, `numeric`, `timestamp`, `boolean`).
- Use constraints (`not null`, `unique`, foreign keys) in the DB, not only in Java.

### Switching branches with different migrations
Your local database remembers the migrations it already ran (table `flyway_schema_history`), whatever branch you are on. If you switch to a branch with different migrations, the app refuses to start with a Flyway validation error. Nothing is deleted. Your local DB is disposable, so either:
- reset it: `DROP SCHEMA public CASCADE; CREATE SCHEMA public;` and restart the app, Flyway replays the branch's migrations, or
- use one schema per branch in your `.env`: `DB_URL=jdbc:postgresql://localhost:5432/parkshark?currentSchema=my_branch`

## Tests

- Test names: `given<Context>_when<Action>_then<Result>`, body in three blocks (given / when / then), as in the existing tests.
- Assertions with AssertJ (`assertThat`); HTTP tests with RestAssured.
- Which test where:
  - **Unit test** (plain JUnit, no Spring) for domain logic, services with simple fakes, and anything with edge cases. Fast, write many.
  - **End-to-end test** (`@SpringBootTest(webEnvironment = RANDOM_PORT)` + RestAssured) per endpoint: happy path, validation error, 401, 403, 404/409. See `AuthenticationEndToEndTest`.
- Each test sets up its own data; never rely on another test's data or on execution order.
- To control time, give the context a `MutableClock`:
  ```java
  @TestConfiguration
  static class TestClockConfig {
      @Bean
      @Primary
      MutableClock testClock() {
          return new MutableClock(ZonedDateTime.of(2026, 1, 1, 9, 0, 0, 0, ZoneId.of("Europe/Brussels")));
      }
  }
  ```
- `./mvnw verify` fails under 80% line coverage. Aim for meaningful tests, not just coverage: test the error cases too.

## Git workflow

- Start from an up-to-date `main`: `git switch main && git pull`, then `git switch -c feat/my-story`.
- Commit messages: `feat(create-parking-lot) add capacity validation` (see the README rules).
- Before opening a PR: `git pull origin main` into your branch (or rebase), fix conflicts, run `./mvnw verify`.
- Never commit `.env`, `target/` or IDE files (already in `.gitignore`).
- Never force-push to `main`.
