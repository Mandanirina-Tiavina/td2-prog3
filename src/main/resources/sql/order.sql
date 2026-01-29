create table "order"
(
    id serial primary key,
    reference varchar (100),
    creation_datetime timestamp
);

create table dish_order
(
    id serial primary key,
    id_order int references "order"(id),
    id_dish int references dish(id),
    quantity numeric(10, 2)
);

alter table "order" add column if not exists id_table int references "table"(id);
alter table "order" add column if not exists arrival_datetime timestamp;
alter table "order" add column if not exists departure_datetime timestamp;