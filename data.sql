INSERT INTO dish (name, dish_type) VALUES ('Salade fraîche', 'START');
INSERT INTO dish (name, dish_type) VALUES ('Poulet grillé', 'MAIN');
INSERT INTO dish (name, dish_type) VALUES ('Riz aux légumes', 'MAIN');
INSERT INTO dish (name, dish_type) VALUES ('Gâteau au chocolat', 'DESSERT');
INSERT INTO dish (name, dish_type) VALUES ('Salade de fruits', 'DESSERT');

INSERT INTO ingredient (name, price, category, id_dish) VALUES ('Laitue', 800.00, 'VEGETABLE', 1);
INSERT INTO ingredient (name, price, category, id_dish) VALUES ('Tomate', 600.00, 'VEGETABLE', 1);
INSERT INTO ingredient (name, price, category, id_dish) VALUES ('Poulet', 4500.00, 'ANIMAL', 2);
INSERT INTO ingredient (name, price, category, id_dish) VALUES ('Chocolat', 3000.00, 'OTHER', 4);
INSERT INTO ingredient (name, price, category, id_dish) VALUES ('Beurre', 2500.00, 'DAIRY', 4);
