create table credentials
(
    id            uuid         default gen_random_uuid() primary key,
    email         varchar(255) not null unique,
    password_hash varchar(255) not null,
    role          varchar(20)  not null,
    constraint chk_role_in_manager_or_member
        check (role in ('MANAGER', 'MEMBER'))
);