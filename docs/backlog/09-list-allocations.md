# 09 · List parking allocations

**Stories**: PSA3 (+ PSA4, PSA5, PSA6 Nice-To-Have) · **Priority**: Must-Have · **Depends on**: 07

## Stories
> **PSA3** As a Manager I want to get an overview of all parking allocations.
> - By default all allocations are returned, ordered on start time ascending
> - Filters: limit the number of results (e.g. 100), filter on status (all / active / stopped), order descending or ascending

> **PSA4** *(Nice-To-Have)* … all parking allocations for a given Member, with the status filter.
> **PSA5** *(Nice-To-Have)* … all parking allocations for a given Parking lot, with the status filter.
> **PSA6** *(Nice-To-Have)* … each allocation also includes its duration in `hh:mm:ss` (stop time − start time; the current time if not stopped).

## Acceptance criteria
- [ ] A manager gets all allocations, ordered by start time ascending → 200.
- [ ] `limit` returns at most that many allocations; 0, negative or non-numeric → 400.
- [ ] Status filter: all (default), active, stopped; an unknown status → 400.
- [ ] The order can be set to descending.
- [ ] The filters can be combined.
- [ ] A member → 403; no or wrong credentials → 401.
- [ ] *(PSA4 / PSA5)* Same filters for one member / one parking lot; unknown id → 404.
- [ ] *(PSA6)* Each allocation has a `duration` like `02:15:07`, also for active allocations.

## Questions for the analyst
- What does an allocation in the list contain (member id? parking lot name?)
- Is there a maximum limit?
- A duration over 24 hours: `27:00:00` or with days?

## Hints

<details>
<summary>💡 Hint 1</summary>

- Filters are query parameters, all optional.
- Avoid loading every allocation to filter and cut it in Java.
- PSA4 and PSA5 are PSA3 with one more filter.
- PSA6: "the current time" again… and tested how?

</details>

<details>
<summary>🔍 Hint 2</summary>

- `GET /parking-allocations?limit=100&status=ACTIVE&order=DESC` with `@RequestParam(required = false)`; an enum parameter is converted by Spring and a bad value is already a 400.
- Spring Data: `findByStatus(status, PageRequest.of(0, limit, Sort.by(direction, "startTime")))`, or `Limit.of(limit)` + `Sort`. No status → `findAll(...)` with the same `Pageable`.
- `@Positive` on `limit` needs `@Validated` on the controller.
- PSA4 / PSA5: `/members/{id}/parking-allocations` and `/parking-lots/{id}/parking-allocations`, reusing the same service method.
- PSA6: `Duration.between(start, stop != null ? stop : LocalDateTime.now(clock))`, formatted with `String.format("%02d:%02d:%02d", d.toHours(), d.toMinutesPart(), d.toSecondsPart())`.

</details>
