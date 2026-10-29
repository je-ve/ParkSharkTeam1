# 06 · List members

**Stories**: ME2 (+ ME3 Nice-To-Have) · **Priority**: Must-Have · **Depends on**: 05

## Stories
> **ME2** As a Manager I want to get an overview of all Members.
> - The returned members should contain the id, name, license plate (just the number), telephone number, email address and registration date (nothing else)

> **ME3** *(Nice-To-Have)* As a Manager I want to get a single, identified, Member.
> - The returned member should include all the data we have of a single member

## Acceptance criteria
- [ ] A manager gets all members → 200, each with **only** id, name, license plate number, telephone, email and registration date.
- [ ] No password or hash anywhere in the response.
- [ ] A member → 403; no or wrong credentials → 401.
- [ ] *(ME3)* A manager gets one member by id with all their data (including membership level) → 200; unknown id → 404.

## Questions for the analyst
- Name as one field (`"Jane Doe"`) or first and last name separately?
- Should a member be able to see their own profile? (Not in the stories.)

## Hints

<details>
<summary>💡 Hint 1</summary>

- Where is the member's email stored? How do you get it without loading things one by one?
- Overview and detail: same situation as ticket 04.

</details>

<details>
<summary>🔍 Hint 2</summary>

- If the email lives in `credentials`, fetch members with their credentials in one query: `join fetch m.credentials`.
- `MemberSummaryResponse` (ME2) and `MemberResponse` (ME3) records; never put `Credentials` itself in a DTO.
- Feature `VIEW_MEMBERS` for the manager.

</details>
