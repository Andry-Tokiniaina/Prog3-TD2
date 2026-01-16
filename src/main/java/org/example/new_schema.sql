ALTER TABLE ingredient DROP CONSTRAINT fk_dish;
ALTER TABLE ingredient DROP COLUMN id_dish;

CREATE TYPE unit_type AS ENUM (
    'PCS', 'KG', 'L'
);

CREATE TABLE dish_ingredient(
    id serial primary key,
    id_dish int NOT NULL,
    id_ingredient int NOT NULL,
    quantity_required numeric NOT NULL,
    unit unit_type NOT NULL,

    CONSTRAINT fk_dish FOREIGN KEY (id_dish) REFERENCES dish(id),
    CONSTRAINT fk_ingredient FOREIGN KEY (id_ingredient) REFERENCES ingredient(id)
);

INSERT INTO dish_ingredient values (1,1,1,0.2,'KG'),
                                  (2,1,2,0.15,'KG'),
                                  (3,2,3,1.00,'KG'),
                                  (4,4,4,0.30,'KG'),
                                  (5,4,5,0.20,'KG');

UPDATE dish SET price = 3500.00 WHERE id = 1;
UPDATE dish SET price = 12000.00 WHERE id = 2;
UPDATE dish SET price = NULL WHERE id = 3;
UPDATE dish SET price = 8000.00 WHERE id = 4;
UPDATE dish SET price = NULL WHERE id = 5.