INSERT INTO dish (name, dish_type, price) VALUES ('Salade fraîche', 'START', 2000);
INSERT INTO dish (name, dish_type, price) VALUES ('Poulet grillé', 'MAIN', 6000);
INSERT INTO dish (name, dish_type, price) VALUES ('Riz aux légumes', 'MAIN', NULL);
INSERT INTO dish (name, dish_type, price) VALUES ('Gâteau au chocolat', 'DESSERT', NULL);
INSERT INTO dish (name, dish_type, price) VALUES ('Salade de fruits', 'DESSERT', NULL);

INSERT INTO ingredient (name, price, category, id_dish) VALUES ('Laitue', 800.00, 'VEGETABLE', 1);
INSERT INTO ingredient (name, price, category, id_dish) VALUES ('Tomate', 600.00, 'VEGETABLE', 1);
INSERT INTO ingredient (name, price, category, id_dish) VALUES ('Poulet', 4500.00, 'ANIMAL', 2);
INSERT INTO ingredient (name, price, category, id_dish) VALUES ('Chocolat', 3000.00, 'OTHER', 4);
INSERT INTO ingredient (name, price, category, id_dish) VALUES ('Beurre', 2500.00, 'DAIRY', 4);
