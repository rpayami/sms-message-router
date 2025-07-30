drop table if exists "SMSMessage";
drop table if exists "OptOut";

create table if not exists "SMSMessage"(
    id serial primary key,
    destination_number varchar(20) not null,
    content varchar(255) not null,
    type VARCHAR(20),
    status VARCHAR(20),
    carrier VARCHAR(20),
    created_at timestamp
);

create table if not exists "OptOut"(
    id serial primary key,
    phone_number varchar(20) not null
);

