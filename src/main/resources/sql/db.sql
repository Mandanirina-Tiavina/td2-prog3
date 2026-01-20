create database "mini_dish_db";

create user "mini_dish_db_manager" with password '123456';

-- Grant all privileges
GRANT ALL PRIVILEGES ON DATABASE "mini_dish_db" TO "mini_dish_db_manager";