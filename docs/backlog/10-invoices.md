# 10 · Invoices

**Stories**: INV1, INV2, INV3 · **Priority**: Nice-To-Have · **Depends on**: 08

## Stories
> **INV1** As a Member I want to get my monthly Invoice so that I can pay it.
> - The invoice is generated on the fly. All allocations with status **stopped** become **Invoice Items** and get status **invoiced**.
> - Item price: `whole hours parked, rounded up × price per hour of the parking lot`, minus the membership reduction. Above the membership's maximum allocation time, a fine of €2.5 per hour is added.
> - An invoice has a unique reference number, a creation date, an expiration date, its items and a status (Open or Closed; Closed has a date of payment).
> - The total = sum of the items (+ fines) + the membership monthly cost.

> **INV2** As a Manager I want to get an overview of all Invoices.

> **INV3** As a Manager I want to mark an Invoice as closed (by its unique reference); the status becomes closed.

## Acceptance criteria
- [ ] *(INV1)* A member gets an invoice with one item per stopped allocation, the right prices and total → 201 (it's created).
- [ ] Those allocations become `INVOICED` and are never invoiced twice.
- [ ] Parked 2h20 → charged 3h; the reduction of the member's level is applied.
- [ ] Over the maximum time → fine added; under it → no fine.
- [ ] No stopped allocation → an invoice with only the monthly cost (or agree otherwise with the analyst).
- [ ] *(INV2)* A manager gets all invoices.
- [ ] *(INV3)* A manager closes an open invoice → status closed, date of payment set; already closed → 409; unknown reference → 404.

## Questions for the analyst
- The fine: €2.5 per hour **above** the maximum, or for every hour of the allocation?
- Is the reduction applied before or after the fine? To the fine itself?
- Expiration date = creation date + how many days?
- Format of the reference number?
- Rounding of prices: 2 decimals, which rounding mode?

## Hints

<details>
<summary>💡 Hint 1</summary>

- Write the price calculation as a pure function first, with a table of unit test cases, before any endpoint.
- "On the fly" + "status invoiced" = several writes that must all succeed or all fail.
- Prices change over time: should an item store its price, or recompute it?

</details>

<details>
<summary>🔍 Hint 2</summary>

- Add `INVOICED` to `AllocationStatus` and a domain method `allocation.invoice()` allowed only from `STOPPED`.
- Hours rounded up: `(duration.toMinutes() + 59) / 60`, or `Math.ceilDiv(minutes, 60)`.
- `BigDecimal` everywhere, `setScale(2, RoundingMode.HALF_UP)` at the end only.
- Store the calculated price on `invoice_item`: the invoice must not change if the parking lot's price changes later.
- `invoice` ──< `invoice_item` ── `parking_allocation` (one-to-one, unique).
- Reference: a UUID, or a readable `INV-2026-000123` from a database sequence.

</details>
