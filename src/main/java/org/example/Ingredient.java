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
    public Ingredient(int id, String name, Double price, CategoryEnum category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }
    public Ingredient(String name, Double price, CategoryEnum category, Dish dish) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.dish = dish;
    }
    public Ingredient(String name, Double price, CategoryEnum category) {
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public String getDishName() {
        return dish == null? null: dish.getName();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public Double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category=" + category +
                ", dish=" + getDishName() +
                '}';
    }
}
