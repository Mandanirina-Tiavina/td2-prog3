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