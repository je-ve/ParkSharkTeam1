# 01 · Create and list divisions

**Stories**: DI1, DI2 · **Priority**: Must-Have · **Depends on**: 00

## Stories
> **DI1** As a Manager I want to create a division.
> ParkShark became the company it is by doing takeovers of competing companies. These companies were never fully merged with ParkShark, they became divisions.
> - A division has a name, an original name (the original name of the bought company) and a director

> **DI2** As a Manager I want to get an overview of all divisions.

## Acceptance criteria
- [ ] A manager can create a division with a name, an original name and a director → 201 with the created division, including its id.
- [ ] A missing or blank field → 400, listing every invalid field.
- [ ] A manager can get all divisions → 200.
- [ ] A member → 403 on both endpoints; no or wrong credentials → 401.
- [ ] Both endpoints appear in Swagger (`/docs`).

## Questions for the analyst
- Must a division name be unique? The original name?
- Is the director a user of the application, or just a name?
- In what order should divisions be listed?

## Hints

<details>
<summary>💡 Hint 1</summary>

- Start with the end-to-end test of the happy path, then the 400, 401 and 403 cases.
- The director is never described further than "a director": keep it simple until the analyst says otherwise.
- Think about how a subdivision (ticket 02) will be stored, but don't build it yet.

</details>

<details>
<summary>🔍 Hint 2</summary>

- Migration: one `division` table (`id uuid`, `name`, `original_name`, `director`, all `not null`).
- `feature/division/` with `api`, `domain`, `service`; request and response DTOs as records, `@NotBlank(message = ...)` on each field.
- Features `CREATE_DIVISION` and `VIEW_DIVISIONS`, given to `MANAGER`.
- `POST /divisions` returns 201 (`@ResponseStatus(HttpStatus.CREATED)`), `GET /divisions` returns a list of response DTOs.

</details>
