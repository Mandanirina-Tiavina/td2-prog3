create type movement_type as enum ('IN', 'OUT');

create table stock_movement
(
    id                serial primary key,
    id_ingredient     int references ingredient (id),
    quantity          numeric(10, 2),
    type              movement_type,
    unit              unit_type,
    creation_datetime timestamp
);

insert into stock_movement (id, id_ingredient, quantity, type, unit, creation_datetime)
values (1, 1, 5.0, 'IN', 'KG', '2024-01-05 08:00:00'),
       (2, 1, 0.2, 'OUT', 'KG', '2024-01-06 12:00:00'),
       (3, 2, 4.0, 'IN', 'KG', '2024-01-05 08:00:00'),
       (4, 2, 0.15, 'OUT', 'KG', '2024-01-06 12:00:00'),
       (5, 3, 10.0, 'IN', 'KG', '2024-01-04 09:00:00'),
       (6, 3, 1.0, 'OUT', 'KG', '2024-01-06 12:00:00'),
       (7, 4, 3.0, 'IN', 'KG', '2024-01-05 10:00:00'),
       (8, 4, 0.3, 'OUT', 'KG', '2024-01-06 12:00:00'),
       (9, 5, 2.5, 'IN', 'KG', '2024-01-05 10:00:00'),
       (10, 5, 0.2, 'OUT', 'KG', '2024-01-06 12:00:00');
