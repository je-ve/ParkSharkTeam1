# 04 · List parking lots

**Stories**: PL2 (+ PL3 Nice-To-Have) · **Priority**: Must-Have · **Depends on**: 03

## Stories
> **PL2** As a Manager I want to get an overview of all parking lots.
> - The returned parking lots should include the id, name, capacity and the contact person's email + telephone (nothing else)

> **PL3** *(Nice-To-Have)* As a Manager I want to get a single, specified, parking lot.
> - The returned parking lot should include all the data we have of a single parking lot, including its division (DIPL1).

## Acceptance criteria
- [ ] A manager gets all parking lots → 200, each with **only** id, name, capacity, contact email and contact telephone.
- [ ] No parking lots → 200 with an empty list.
- [ ] A member → 403; no or wrong credentials → 401.
- [ ] *(PL3)* A manager gets one parking lot by id with all its data and its division → 200; unknown id → 404.

## Questions for the analyst
- "Telephone": the telephone number, or the mobile when there's no telephone? (The contact may have only a mobile.)
- In what order should parking lots be listed?

## Hints

<details>
<summary>💡 Hint 1</summary>

- "Nothing else" means the overview and the detail need different response shapes.
- Count the SQL queries in the logs when you list 10 parking lots.

</details>

<details>
<summary>🔍 Hint 2</summary>

- Two response records: `ParkingLotSummaryResponse` (PL2) and `ParkingLotResponse` (PL3).
- Turn on `spring.jpa.show-sql=true` locally: one query per parking lot for its contact person is the N+1 problem. Fix it with a `@Query("select p from ParkingLot p join fetch p.contactPerson")` or an `@EntityGraph`.
- Map to DTOs inside the `@Transactional(readOnly = true)` service.

</details>
