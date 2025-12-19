package org.example;

public class Ingredient {
    private int id;
    private String name;
    private  Double price;
    private CategoryEnum category;
    private Dish dish;

    public Ingredient(int id, String name, Double price, CategoryEnum category, Dish dish) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.dish = dish;
    }

    public String getDishName() {
        return dish == null? null: dish.getName();
    }

    public Double getPrice() {
        return price;
    }
}
