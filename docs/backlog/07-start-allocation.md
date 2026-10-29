# 07 · Start a parking allocation

**Stories**: PSA1 · **Priority**: Must-Have · **Depends on**: 03, 05

## Story
> **PSA1** As a Member I want to start allocating a Parking spot so that I can enter the parking lot and park my car.
> - Required info: the member (id), the license plate number (of the actual car being parked), the parking lot (id)
> - Starting only succeeds if:
>     - the member is recognized by the system
>     - the license plate number is the same as the member's, unless the membership level is **Gold**
>     - the parking lot is recognized by the system
>     - the parking lot still has available capacity (it's not full)
> - The started allocation is uniquely identifiable and the member receives this id.
> - An allocation has a start time.

## Acceptance criteria
- [ ] A member starts an allocation with a license plate and a parking lot id → 201 with the allocation id and its start time.
- [ ] The start time is set by the server.
- [ ] Unknown parking lot → 404.
- [ ] Another license plate than their own → refused for bronze and silver, accepted for gold.
- [ ] Full parking lot (active allocations = capacity) → refused (409).
- [ ] A manager or an anonymous user cannot start an allocation (403 / 401).
- [ ] A member cannot start an allocation **on behalf of another member**.
- [ ] Each refusal has a clear message and is logged.

## Questions for the analyst
- Can a member have two active allocations at the same time? With the same plate?
- Status code for a wrong license plate: 400 or 403?
- Is the license plate comparison case-insensitive / ignoring spaces (`1-ABC-123` vs `1abc123`)?

## Hints

<details>
<summary>💡 Hint 1</summary>

- "The member (id)" is required, but you already know who is logged in. Which is safer to trust?
- Where does the "Gold may use another plate" rule belong: controller, service or domain?
- How do you know how many spots are taken? Do you need to store a counter?
- What happens if two members take the last spot at the same moment?

</details>

<details>
<summary>🔍 Hint 2</summary>

- Get the logged-in user with `@AuthenticationPrincipal AuthenticatedUser user` in the controller. Careful: `user.id()` is the **credentials** id; find the member by its credentials.
- `parking_allocation` table: `id`, `member_id`, `parking_lot_id`, `license_plate_number`, `start_time`, `stop_time` (null), `status`.
- Capacity: `countByParkingLotAndStatus(lot, ACTIVE) >= lot.getCapacity()` → `ParkingLotFullException extends ConflictException`.
- The plate rule in the domain, e.g. a method on `Member` or `MembershipLevel` (`allowsOtherLicensePlates()`), easy to unit test.
- Concurrency: lock the parking lot row while counting (`@Lock(LockModeType.PESSIMISTIC_WRITE)` on a repository method), or document the risk as a known limitation.
- `startTime = LocalDateTime.now(clock)`.

</details>
