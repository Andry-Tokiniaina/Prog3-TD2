CREATE  TYPE  dish_type as enum ('STARTER', 'MAIN', 'DESSERT');

CREATE TABLE Dish(
     id serial PRIMARY KEY,
     name VARCHAR(255) NOT NULL UNIQUE,
     dish_type dish_type
);

CREATE TYPE ingredient_type AS ENUM ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER') ;

CREATE TABLE Ingredient(
    id serial PRIMARY KEY,
    name varchar(255) NOT NULL UNIQUE,
    price NUMERIC NOT NULL ,
    category ingredient_type,
    id_dish int,
    CONSTRAINT fk_dish FOREIGN KEY (id_dish) REFERENCES dish(id)
);
