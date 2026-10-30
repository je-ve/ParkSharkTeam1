# Code review: `feat/divisions` (DI1 + DI2)

Reviewed on 2026-10-30, at commit `2d9eeab`, against `main`.
16 commits, 16 files, +477 / -3.
Read-only review: no code was changed. Tests were not run (Maven could not be downloaded in the review environment), so the test findings come from reading the code.

References: `docs/backlog/01-divisions.md`, `docs/CONVENTIONS.md`.

## Verdict

**Needs changes before merge.** The feature works and the structure is clean, and the mapper is now called from the service as agreed. But one test can fail depending on execution order, the service has no `@Transactional` (so the mapping refactor doesn't protect anything yet), and two acceptance criteria are not tested.

## Acceptance criteria (DI1 + DI2)

| Criterion | Code | Test |
|---|---|---|
| Manager creates a division (name, originalName, director) → 201 with id | ✅ | ✅ |
| Missing or blank field → 400, listing every invalid field | ⚠️ works, but default messages | ❌ none |
| Manager gets all divisions → 200 | ✅ | ✅ |
| Member → 403 on both endpoints | ✅ | ⚠️ only with a fake authority, never a real member |
| No or wrong credentials → 401 | ✅ | ⚠️ only "no credentials" |
| Both endpoints in Swagger (`/docs`) | ⚠️ visible, but no `@Operation` | ❌ |
| Duplicate name → 409 (team decision) | ✅ | ⚠️ unit test only, no HTTP test |

---

## Must fix

### 1. Tests depend on execution order (shared H2 database)
`DivisionControllerTest` and `DivisionControllerIntegrationTest` use the same Spring context, so the same in-memory H2 database.

- `DivisionControllerIntegrationTest` creates `"MyCompany"` and never cleans up.
- `DivisionControllerTest.TestingOkCreated` calls `divisionRepository.deleteAll()` before each test, then the GET test creates `"MyCompany"`, `"MyGreatCompany"` and `"MyGreatestCompany"`, which stay in the database afterwards.

If the GET test runs before `DivisionControllerIntegrationTest`, the integration test gets **409 instead of 201**. If the integration test runs first, its row is wiped by `deleteAll()`. The result depends on which class JUnit runs first.

`deleteAll()` is also a trap for later: once `parking_lot` has a foreign key to `division` (story DIPL1), it will fail or wipe data other tests rely on.

**Fix:** the convention is "each test sets up its own data, unique values per test". Use unique names (`"Division-" + UUID.randomUUID()`) and drop `deleteAll()`. For the GET test, assert that the list *contains* the divisions you created (`hasItems(...)`), not `hasSize(3)`.

### 2. `DivisionService` has no `@Transactional`
The conventions require `@Transactional` on service classes. It also matters for the mapper refactor: the mapping now happens in the service, but **without a transaction it isn't inside one**. `findAll()` and `save()` each open and close their own transaction, and the mapping runs after. Today that's harmless, but as soon as DI4 adds a lazy `parent`, mapping it will throw `LazyInitializationException` (`open-in-view=false`).

**Fix:** `@Transactional` on the class, and `@Transactional(readOnly = true)` on `listAllDivisions`.

### 3. Missing tests for the acceptance criteria
- **400**: blank or missing `name`, `originalName`, `director` → 400 with the `errors` map listing each field.
- **403 for a member**: the test uses `CREATE_THE_GREATER_DIVISION`, an authority that doesn't exist. Use the real member authorities, i.e. none, since `Role.MEMBER` has no features: `@WithMockUser` with no authorities, or a real member created through `CredentialsService`.
- **409** over HTTP: POST the same name twice.
- **401 with wrong credentials**: only "no credentials" is tested.
- **Unit test** for `listAllDivisions`.

### 4. Unused and broken code: `DivisionOverviewDto` and `mapToDto(current, parent, children)`
Nothing calls them. They also don't work as written: `mapToDto(parent)` throws a `NullPointerException` for a top-level division (`parent == null`), and the DTO has no `id`. This belongs to DI4 (subdivisions); build it in that story with its own tests, or remove it from this branch.

### 5. Validation without messages, and no length checks
`CreateDivisionRequest` has `@NotBlank` without `message`, which the conventions require: the client will see Hibernate's default English text. There's also no `@Size`, while the columns are `VARCHAR(50)` (name, original_name) and `VARCHAR(100)` (director). A longer value reaches the database and comes back as **500** instead of 400.

```java
public record CreateDivisionRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 50, message = "Name must be at most 50 characters")
        String name,
        ...
```

---

## Should fix

### 6. `DivisionConflictException.duplicateDivisionException` throws instead of returning
```java
public static DivisionConflictException duplicateDivisionException(String divisionName) {
    throw new DivisionConflictException(...);   // should be: return new ...
}
```
It works by accident: the exception is thrown inside the factory, before the caller's `throw`. A factory method should `return` the exception.

### 7. The service unit test checks the mock, not the code
In `givenRequest_whenCallingCreateDivision_thenADivisionObjectIsReturned`, the mapper is mocked to return a DTO with `"MyCompany"`, then the test asserts the DTO contains `"MyCompany"`. It would pass even if the service passed the wrong fields to `new Division(...)`. Use the real `DivisionMapper` (`new DivisionMapper()`, it has no dependencies), or capture the saved `Division` with an `ArgumentCaptor` and check its fields.
The test also declares `throws InstantiationException, IllegalAccessException`, which is left over and can be removed.

### 8. `parent_division_id` is a bare `UUID`, with no foreign key
The backlog hint for DI1 says to think about subdivisions but not to build them yet. When DI4 lands, the parent should be a `@ManyToOne(fetch = LAZY)` with a real foreign key, so this column will need another migration anyway. Simplest: remove it from V3 and add it properly in DI4. V3 isn't on `main` yet, so it can still be edited.

### 9. Duplicate name check can race
Between `existsByName` and `save`, two simultaneous requests can both pass. The second hits the UNIQUE constraint, gets `DataIntegrityViolationException`, and returns 500. `CredentialsService.create` already shows the pattern to follow: catch `DataIntegrityViolationException` and throw the conflict exception.

### 10. List order is undefined
`findAll()` returns rows in whatever order the database chooses. Ordering is an open analyst question in the backlog; until it's answered, sorting by name is a reasonable default (`findAllByOrderByNameAsc()` or `findAll(Sort.by("name"))`).

### 11. Swagger documentation
Add `@Operation(summary = "Create a division")` and `@Operation(summary = "Get all divisions")`. The DI1 criterion asks for both endpoints in `/docs`.

### 12. Name uniqueness: document the decision
"Must a division name be unique?" is an open analyst question. The branch makes `name` unique (DB constraint + 409). That's fine, but note the decision in the backlog file or the PR description.

---

## Nits

- `DivisionRepository`: unused import `java.util.List`; `@Repository` is not needed on a Spring Data interface.
- `DivisionController.getDivisions`: `@ResponseStatus(HttpStatus.OK)` is the default and can go.
- `Division`: the constructor assigns fields in a different order from its parameters, and `this.parentDivisionId = null` is redundant.
- `@Column(name = "NAME")` is uppercase while the SQL is lowercase. It works, but lowercase (or no `@Column` at all, since the names already match) is clearer.
- `DivisionControllerTest`:
  - unused import `org.mockito.Mock`;
  - `givenNoCredentials_whenGetAllDivisions_thenReturns401` sits outside the `TestUnauthorized` nested class;
  - status checked two or three times per test (`andExpect(status().isForbidden())` + `assertThat(...getStatus())` + `HttpStatus.valueOf(...)`): one `andExpect` is enough;
  - it's a `@SpringBootTest`, so it's an integration test like `DivisionControllerIntegrationTest`. Merge both into one class (the POST happy path is tested twice).
- `DivisionControllerIntegrationTest` is `public` and ends with two blank lines.
- The conventions describe end-to-end tests with RestAssured on `RANDOM_PORT`; these use MockMvc with `@WithMockUser`. MockMvc is acceptable, but `@WithMockUser` skips real authentication, so nothing proves a real manager can log in and call the endpoint. Agree as a team which style to use.
- Several commit messages don't follow `feat(name-of-story) description` ("Controller Tests", "fix test errors", "fix test divisions").
- `V3__create_divisions.sql` has no final newline.
- Table naming: `division` (singular) here, `members` (plural) on `feature/register-as-member`. Pick one before more tables arrive.

## What's good

- Package layout follows the conventions: `api`, `api/dto`, `api/request`, `domain`, `service`, `exception`.
- The controller is thin and the service returns DTOs: the mapper refactor discussed with the team is done.
- Security uses features, not roles: `CREATE_DIVISION` and the new `VIEW_DIVISIONS`, both given to `MANAGER`.
- Request naming follows the new team convention: `CreateDivisionRequest` in, `DivisionDto` out.
- Duplicate names return 409 through a `ConflictException` subclass.
- The database enforces the rules too: `NOT NULL`, `UNIQUE (name)`, and a `default gen_random_uuid()` id.
- Test names follow `given_when_then`, and the 401/403/201/200 paths are covered.

## Suggested order of work

1. Fix test isolation (unique names, no `deleteAll`).
2. Add `@Transactional` to the service.
3. Add validation messages and `@Size`, then the 400, 409, real-member 403 and wrong-credentials 401 tests.
4. Remove `DivisionOverviewDto` and the 3-argument mapper method, or move them to DI4.
5. Run `./mvnw verify` (80% coverage) and open the PR.
