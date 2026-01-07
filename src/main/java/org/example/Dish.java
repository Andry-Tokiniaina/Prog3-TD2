package org.example;

import java.util.List;

public class Dish {
    private Integer id;
    private String name;
    private DishTypeEnum dishType;
    private Double price;
    List<Ingredient> ingredients;

    public Dish( int id, String name, DishTypeEnum dishType, Double price, List<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.price = price;
        this.ingredients = ingredients;
    }
    public Dish(String name, DishTypeEnum dishType, Double price, List<Ingredient> ingredients) {
        this.name = name;
        this.dishType = dishType;
        this.price = price;
        this.ingredients = ingredients;
    }
    public Dish(String name, DishTypeEnum dishType, List<Ingredient> ingredients) {
        this.name = name;
        this.dishType = dishType;
        this.ingredients = ingredients;
    }
    public Dish(int id, String name, DishTypeEnum dishType, List<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.ingredients = ingredients;
    }

    public Double getDishCost() {
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

    public void setId(Integer id) {this.id = id; }

    public Double getPrice() {
        return price;
    }

    public Double getGrossMargin() {
        try {
            if (this.getPrice()==0 || this.getPrice()==null){
                throw new RuntimeException("the sell value is not yet defined");
            }
            return this.getPrice()-this.getDishCost();
        }catch (Exception e){
            throw new RuntimeException("the sell value is not yet defined");
        }
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", price=" + price +
                ", ingredients=" + ingredients +
                '}';
    }
}
