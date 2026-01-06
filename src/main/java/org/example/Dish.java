package org.example;

import java.util.List;

public class Dish {
    private Integer id;
    private String name;
    private DishTypeEnum dishType;
    List<Ingredient> ingredients;
    public Dish( int id, String name, DishTypeEnum dishType, List<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.ingredients = ingredients;
    }
    public Dish(String name, DishTypeEnum dishType, List<Ingredient> ingredients) {
        this.name = name;
        this.dishType = dishType;
    }

    public Double getDishPrice() {
        return ingredients == null ? null : ingredients.stream().mapToDouble(Ingredient::getPrice).sum();
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public Integer getId() {
        return id;
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", ingredients=" + ingredients +
                '}';
    }
}
