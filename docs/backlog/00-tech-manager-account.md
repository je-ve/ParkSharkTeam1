# 00 · Manager account & roles

**Type**: technical · **Priority**: Must-Have · **Depends on**: – · **Blocks**: every Manager story

## Why
Most stories start with *"As a Manager…"*. Nobody can register as a manager, so a manager account must exist as soon as the application runs, on every developer's machine and in the tests.

## Acceptance criteria
- [ ] A `MANAGER` and a `MEMBER` role exist.
- [ ] When the application starts on an empty database, a manager account exists, without any manual SQL.
- [ ] Restarting the application does not create a second manager and does not crash.
- [ ] The manager's password is **not** in Git (neither in code nor in a migration).
- [ ] The manager can authenticate with HTTP Basic; a wrong password gets 401.
- [ ] If the manager configuration is missing, the app refuses to start with a clear message (like the `DB_*` keys).
- [ ] Tests can log in as the manager.

## Questions for the analyst
- One manager or several? Can a manager create other managers? (Not in the stories: keep it to one for now.)

## Hints

<details>
<summary>💡 Hint 1</summary>

- The account is just a row in `credentials` with the right role: you don't need a new entity.
- "Not in Git" and "different on each machine" smells like configuration, same as `DB_PASSWORD`.
- Spring can run code once the application has started.

</details>

<details>
<summary>🔍 Hint 2</summary>

- Add `MANAGER_EMAIL` and `MANAGER_PASSWORD` to `.env.example`, to `REQUIRED_KEYS` in `RequiredConfigurationEnvironmentPostProcessor`, and to `src/test/resources/application.properties`.
- An `ApplicationRunner` bean reads them and calls `CredentialsService.create(email, password, Role.MANAGER)` only if `existsByEmail` is false.
- `Role` and `Feature` are already wired into Spring Security: a user's authorities are the features of their role.

</details>
