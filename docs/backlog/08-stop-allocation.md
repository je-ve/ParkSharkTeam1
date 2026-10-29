# 08 · Stop a parking allocation

**Stories**: PSA2 · **Priority**: Must-Have · **Depends on**: 07

## Story
> **PSA2** As a Member I want to stop allocating a Parking spot so that I can leave the parking lot.
> - Required info: the parking allocation id, the member (id) (if you use security, check it based on the logged-in user)
> - An allocation can only be stopped if it's active.
> - An allocation can only be stopped if the member is the owner of the allocation.
> - When stopped, a stop time is added.
> - Every allocation has an explicit status: active or stopped (for now).

## Acceptance criteria
- [ ] The owner stops their active allocation → 200 with the allocation, its stop time and status `STOPPED`.
- [ ] The stop time is set by the server and is after the start time.
- [ ] Unknown allocation id → 404.
- [ ] Another member's allocation → refused (403), the allocation stays active.
- [ ] An allocation already stopped → 409, the stop time does not change.
- [ ] The spot is free again: the parking lot's capacity counts it as available.
- [ ] A manager or an anonymous user cannot stop an allocation (403 / 401).

## Questions for the analyst
- Should a manager be able to force-stop an allocation? (Not in the stories.)

## Hints

<details>
<summary>💡 Hint 1</summary>

- "Make it explicit": a status that can only change in allowed ways. Who should guard those transitions?
- Status and stop time must never disagree.
- Ticket 10 will add a third status.

</details>

<details>
<summary>🔍 Hint 2</summary>

- An enum `AllocationStatus { ACTIVE, STOPPED }` stored as `EnumType.STRING`.
- A domain method `allocation.stop(LocalDateTime now)` that throws if not active and sets both `stopTime` and `status`: no public setters, so the rule can't be bypassed. Unit test it.
- Ownership check in the service: compare the allocation's member with the member of the logged-in `AuthenticatedUser`; a `ConflictException` subclass for "already stopped".
- Endpoint idea: `POST /parking-allocations/{id}/stop` (an action) or `PATCH /parking-allocations/{id}`: pick one as a team.

</details>
