package org.example;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
        DataRetriever dataRetriever = new DataRetriever();

        System.out.println(dataRetriever.findDishById(1));
        System.out.println(dataRetriever.findDishById(999));
        System.out.println(dataRetriever.findIngredients(3, 5));
        System.out.println(dataRetriever.findDishByIngredientName("eur"));
        System.out.println(dataRetriever.findIngredientsByCriteria(null, CategoryEnum.VEGETABLE, null, 1, 10));
        System.out.println(dataRetriever.findIngredientsByCriteria("cho", null, "Sal", 1, 10));
        System.out.println(dataRetriever.findIngredientsByCriteria("cho", null, "gâteau", 1, 10));
        System.out.println(dataRetriever.createIngredients(List.of(new Ingredient(6, "Fromage", 1200.00, CategoryEnum.DAIRY, null),
                new Ingredient(7, "Oignon", 500.00, CategoryEnum.VEGETABLE, null))));

    }
}