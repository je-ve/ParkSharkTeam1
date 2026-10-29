create table members
(
    id              uuid        primary key,
    first_name      varchar(50) not null,
    last_name       varchar(50) not null,
    city            varchar(50) not null,
    street          varchar(50) not null,
    registration_date   date    not null,
    credentials_id  uuid        not null unique references credentials (id)
);