# 02 · Subdivisions

**Stories**: DI4 (+ DI3 Nice-To-Have) · **Priority**: Must-Have · **Depends on**: 01

## Stories
> **DI4** As a Manager I want to create a division that is a subdivision of an already existing division.
> Sometimes, a company that was bought by ParkShark, bought some companies itself. We want to be able to express that in our application by creating subdivisions.
> - A subdivision is the same as a division, it simply has another division that functions as its parent.

> **DI3** *(Nice-To-Have)* As a Manager I want to get a single, specific, division.

## Acceptance criteria
- [ ] A manager can create a division with a parent division → 201; the response shows the parent.
- [ ] Creating a division without a parent still works as in ticket 01.
- [ ] A parent that does not exist → 404 (or 400, agree as a team) with a clear message.
- [ ] Listing divisions (DI2) shows, for each division, its parent if it has one.
- [ ] *(DI3)* A manager can get one division by id → 200; unknown id → 404.

## Questions for the analyst
- Can a subdivision itself have subdivisions (several levels)?
- When getting a division, should its subdivisions be included?
- Can a division change parent later? (Not in the stories.)

## Hints

<details>
<summary>💡 Hint 1</summary>

- A subdivision is a division: same table, same entity. What links a division to another one?
- It's still a relation, even inside one table: which side holds the foreign key?
- How much of the parent should the response contain?

</details>

<details>
<summary>🔍 Hint 2</summary>

- New migration: `alter table division add column parent_id uuid references division (id);` (nullable: null = top-level division).
- In the entity: `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "parent_id") private Division parent;`. The `@OneToMany` side is only needed if a story lists subdivisions.
- The request DTO takes an optional `parentId`; the response DTO returns `parentId`, not the whole parent (avoids `LazyInitializationException` with `open-in-view=false`, and infinite JSON).
- Unknown parent: a `DivisionNotFoundException extends NotFoundException`.

</details>
