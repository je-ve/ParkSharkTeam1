# Backlog

The ParkShark stories, grouped into tickets. Each ticket is one **vertical slice**: migration → entity → service → endpoint → tests, so one pair can own it from start to finish.

## How to use a ticket
1. Read the story and the **acceptance criteria**: that's the definition of "done" for this ticket.
2. Ask the open **questions** to the functional analyst before coding.
3. Design it **yourselves first** (domain model on the whiteboard).
4. Stuck, or want to check your idea? Open the hints, one level at a time:
   - 💡 **Hint 1**: a direction
   - 🔍 **Hint 2**: concrete, close to the solution

The hints are suggestions, not the only right answer. A different design is fine if you can explain why.

## Tickets

| # | Ticket | Stories | Priority | Depends on |
|---|---|---|---|---|
| 00 | [Manager account & roles](00-tech-manager-account.md) | tech | Must | – |
| 01 | [Create and list divisions](01-divisions.md) | DI1, DI2 | Must | 00 |
| 02 | [Subdivisions](02-subdivisions.md) | DI4 (+ DI3) | Must | 01 |
| 03 | [Create a parking lot](03-create-parking-lot.md) | PL1, DIPL1 | Must | 01 |
| 04 | [List parking lots](04-list-parking-lots.md) | PL2 (+ PL3) | Must | 03 |
| 05 | [Register as a member](05-register-member.md) | ME1, ML1 | Must | 00 |
| 06 | [List members](06-list-members.md) | ME2 (+ ME3) | Must | 05 |
| 07 | [Start a parking allocation](07-start-allocation.md) | PSA1 | Must | 03, 05 |
| 08 | [Stop a parking allocation](08-stop-allocation.md) | PSA2 | Must | 07 |
| 09 | [List parking allocations](09-list-allocations.md) | PSA3 (+ PSA4, PSA5, PSA6) | Must | 07 |
| 10 | [Invoices](10-invoices.md) | INV1, INV2, INV3 | Nice | 08 |

(+ …) = Nice-To-Have part of the ticket: do it after the Must-Haves of every ticket are done.

## Dependencies

```
00 ─┬─ 01 ─┬─ 02
    │      └─ 03 ─┬─ 04
    │             │
    └─ 05 ─┬─ 06  │
           └──────┴─ 07 ─┬─ 08 ── 10
                         └─ 09
```

00, then 01 and 05 can be done in parallel; 02, 03 and 06 too.

## Suggested sprints
- **Sprint 1**: 00, 01, 02, 03, 05
- **Sprint 2**: 04, 06, 07, 08, 09
- **Bonus**: the Nice-To-Have parts, then 10

## For every ticket
- [ ] A new Flyway migration (never edit one that is already on `main`)
- [ ] Endpoints secured with `@Secured("FEATURE")`, the feature added to the right `Role`
- [ ] Request and response DTOs, never an `@Entity` in the API
- [ ] Errors through exceptions (`NotFoundException`, `ConflictException`, `IllegalArgumentException`, `@Valid`)
- [ ] Tests: unit tests for the rules, end-to-end tests for each status code
- [ ] `./mvnw verify` green, PR reviewed together

See [CONVENTIONS](../CONVENTIONS.md) for the details.
