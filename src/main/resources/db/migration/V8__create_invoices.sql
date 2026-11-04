alter table allocation drop constraint chk_status_in_ongoing_or_stopped;
alter table allocation
    add constraint chk_allocation_status
        CHECK (status IN ('ONGOING', 'STOPPED', 'INVOICED'));


create table invoice_item
(
    id            uuid default gen_random_uuid() primary key,
    invoice_id    uuid           not null references invoice (id),
    allocation_id uuid           not null unique references allocation (id),
    hours_parked  integer        not null,
    price         numeric(10, 2) not null,
    fine          numeric(10, 2) not null,
    constraint chk_invoice_item_hours_not_negative check (hours_parked >= 0),
    constraint chk_invoice_item_price_not_negative check (price >= 0),
    constraint chk_invoice_item_fine_not_negative check (fine >= 0)
);
create table invoice
(
    id                      uuid default gen_random_uuid() primary key,
    reference               bigint generated always as identity unique not null unique,
    member_id               uuid                                       not null references members (id),
    creation_date           date                                       not null,
    expiration_date         date                                       not null,
    payment_date            date,
    status                  varchar(20)                                not null,
    membership_monthly_cost numeric(10, 2)                             not null
        check (status in ('OPEN', 'CLOSED')),
    constraint chk_invoice_payment_date_only_when_closed
        check ((status = 'CLOSED') = (payment_date is not null)),
    constraint chk_invoice_expiration_after_creation
        check (expiration_date >= creation_date),
    constraint chk_invoice_monthly_cost_not_negative
        check (membership_monthly_cost >= 0)
);