create table if not exists "table"
(
    id     serial primary key,
    number int
);

insert into "table"(number) values (1), (2), (3)
on conflict do nothing;
