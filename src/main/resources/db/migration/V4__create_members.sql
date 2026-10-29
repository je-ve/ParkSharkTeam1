create table members
(
    id                    uuid         primary key,
    first_name            varchar(255) not null,
    last_name             varchar(255) not null,
    street_name           varchar(255) not null,
    street_number         varchar(20)  not null,
    postal_code           varchar(20)  not null,
    city                  varchar(255) not null,
    telephone             varchar(50)  not null,
    license_plate_number  varchar(20)  not null,
    license_plate_country varchar(50)  not null,
    registration_date     date         not null,
    membership_level      varchar(20)  not null,
    credentials_id        uuid         not null unique references credentials (id)
);
