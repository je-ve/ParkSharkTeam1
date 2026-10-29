# 03 · Create a parking lot

**Stories**: PL1, DIPL1 · **Priority**: Must-Have · **Depends on**: 01

## Stories
> **PL1** As a Manager I want to create a parking lot.
> - A parking lot has a name, a category, a (max) capacity, a contact person, and an address
> - A parking lot also has a price per hour for car allocation (parking your car)
> - The contact person has a name, a mobile phone number, a telephone number, an email, and an address
>     - A contact person should always have a (valid) email address.
>     - A contact person should always have a mobile phone number, or a telephone number. Both are allowed, but at least one is always required!
> - The address consists of a street name, street number, and a postal code
>     - A postal code consists of an actual postal code and a label (e.g. *3000, Leuven*)
> - A category is currently restricted to either *underground building* or *above ground building*. However, there can be more in the future (e.g. *street*, *open space*,...).

> **DIPL1** As a Parking lot I want to have a Division so that I know to what division I belong.
> - Every parking lot should have a division when being created; whenever a specific parking lot is returned, it should also include the information of the division.

## Acceptance criteria
- [ ] A manager can create a parking lot with all the data above and a division id → 201 with the created parking lot, including its id and its division.
- [ ] 400 with every invalid field when:
  - [ ] a required field is missing (name, category, capacity, price, address, contact person, division)
  - [ ] the contact person's email is missing or invalid
  - [ ] the contact person has neither a mobile nor a telephone number
  - [ ] the category is unknown
  - [ ] the capacity is not positive, the price is negative
- [ ] A division that does not exist → 404.
- [ ] A member → 403; no or wrong credentials → 401.
- [ ] Adding a new category later only needs a code change, no data migration of existing rows.

## Questions for the analyst
- Is a price of 0 allowed? How many decimals?
- Can one contact person be the contact for several parking lots?
- Is the street number free text (`12B`)?
- In which currency is the price? (Probably euro.)

## Hints

<details>
<summary>💡 Hint 1</summary>

- Count the addresses: the parking lot has one, the contact person has one. How do you reuse the address without copying its fields?
- "At least one of two fields" can't be checked by an annotation on a single field.
- Money is never a `double`.
- A category with "more in the future" but no data of its own: does it need a table?

</details>

<details>
<summary>🔍 Hint 2</summary>

- `Address` (with a nested `PostalCode`) as an `@Embeddable`. Give the contact person its own table: otherwise two addresses in `parking_lot` need `@AttributeOverrides`.
- `Category` as an enum stored with `@Enumerated(EnumType.STRING)` (never `ORDINAL`: inserting a value would shift the others).
- `price_per_hour numeric(10, 2)` ↔ `BigDecimal`.
- Cross-field rule: a method annotated `@AssertTrue(message = "A mobile or a telephone number is required")` on the contact person request record, or check it in the domain constructor and throw `IllegalArgumentException`.
- `@Valid` on nested DTOs, otherwise their constraints are not checked.
- Unknown category in the JSON → already turned into a 400 by `GlobalExceptionHandler` (malformed body).

</details>
