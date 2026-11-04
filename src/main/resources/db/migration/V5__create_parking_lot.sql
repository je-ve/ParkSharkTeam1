create table contact_person
(
    id               uuid default gen_random_uuid() primary key,
    name             varchar(255) not null,
    mobile_phone     varchar(50),
    telephone_number varchar(50),
    email            varchar(255) not null unique,
    street_name      varchar(255) not null,
    street_number    varchar(20)  not null,
    postal_code      varchar(20)  not null,
    city             varchar(255) not null,
    constraint chk_contact_person_has_a_phone_number
        check (telephone_number is not null or mobile_phone is not null)
);

create table parking_lot
(
    id                uuid default gen_random_uuid() primary key,
    name              varchar(255)   not null,
    category          varchar(50)    not null,
    capacity          integer        not null,
    street_name       varchar(255)   not null,
    street_number     varchar(20)    not null,
    postal_code       varchar(20)    not null,
    city              varchar(255)   not null,
    price_per_hour    numeric(10, 2) not null,
    contact_person_id uuid           not null references contact_person (id),
    division_id       uuid           not null references division (id),
    constraint chk_parking_lot_capacity_is_positive
        check (capacity > 0),
    constraint chk_parking_lot_price_per_hour_is_not_negative
        check (price_per_hour >= 0)
);
