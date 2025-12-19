package org.example;

import java.util.List;

public class Dish {
    private Integer id;
    private String name;
    private DishTypeEnum dishType;
    List<Ingredient> ingredients;
    public Dish( int id, String name, DishTypeEnum dishType) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
    }

    public Double getDishPrice() {
        return ingredients == null ? null : ingredients.stream().mapToDouble(Ingredient::getPrice).sum();
    }

    public String getName() {
        return name;
    }
}
