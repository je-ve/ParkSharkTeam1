# 05 · Register as a member

**Stories**: ME1, ML1 · **Priority**: Must-Have · **Depends on**: 00

## Stories
> **ME1** As a Person I want to register myself as a Member so that I can park my car in a parking lot of ParkShark.
> - A member has a name (first, last), an address, a telephone number, an email address, a license plate (license plate number and issuing country) and a registration date.

> **ML1** As a Member I want to select a Membership level.
> - There are 3 membership levels: **bronze**, **silver** and **gold**, with the same options but different values:
>
>   | Level | Monthly cost | Price-per-hour reduction | Max allocation time |
>   |---|---|---|---|
>   | Bronze | €0 | 0% | 4 hours |
>   | Silver | €10 | 20% | 6 hours |
>   | Gold | €40 | 30% | 24 hours |
> - Upon registering, a member can choose a membership level. If none is provided, it is **bronze**.
> - New options or levels may come in the future; it's fine if they only take effect after a new deploy.

## Acceptance criteria
- [ ] **Anyone, without logging in**, can register → 201 with the member (without the password).
- [ ] The registration date is set by the server (today), not sent by the client.
- [ ] Without membership level → bronze; with one → that level; unknown level → 400.
- [ ] After registering, the person can authenticate with their email and password.
- [ ] An email already used (by a member or the manager), in any case (`Jane@Doe.be` vs `jane@doe.be`) → 409.
- [ ] Missing or invalid fields (name, address, telephone, email, license plate, password) → 400 listing every invalid field.
- [ ] The password is stored hashed, never returned, never logged.
- [ ] If saving the member fails, no orphan login account remains.

## Questions for the analyst
- Password rules (minimum length…)?
- Is the license plate unique per member?
- Can a member change their membership level after registering? (ML1 says "select": maybe a later endpoint.)

## Hints

<details>
<summary>💡 Hint 1</summary>

- Every endpoint currently requires authentication. How does an anonymous person reach this one?
- A member has a profile *and* a login. What exists already for the login part?
- The membership levels carry values but change only with a deploy: does that need a table?
- Testing "registration date = today" is easy only if you control "today".

</details>

<details>
<summary>🔍 Hint 2</summary>

- `SecurityConfig`: `.requestMatchers(HttpMethod.POST, "/members").permitAll()` before `.anyRequest().authenticated()`.
- `MemberService.register` (one `@Transactional` method) calls `CredentialsService.create(email, password, Role.MEMBER)`, then saves a `Member` with a `@OneToOne` to those `Credentials`. Keep the email only in `credentials`.
- `MembershipLevel` enum with fields: `BRONZE(BigDecimal.ZERO, 0, 4)`, …, stored as `EnumType.STRING`. Default to `BRONZE` when the request field is null.
- `LicensePlate` and `Address` as `@Embeddable` (reuse the `Address` of ticket 03).
- `registrationDate = LocalDate.now(clock)`; in tests, a `@Primary MutableClock` bean fixes the date.
- Password: `@Size(min = 8, max = 72)`: BCrypt ignores everything after 72 bytes.

</details>
