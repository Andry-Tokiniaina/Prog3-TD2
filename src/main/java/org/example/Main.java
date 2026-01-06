package org.example;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
        DataRetriever dataRetriever = new DataRetriever();

        System.out.println(dataRetriever.findDishById(1));
        System.out.println(dataRetriever.findDishById(999));
        System.out.println(dataRetriever.findIngredients(2,2));
        System.out.println(dataRetriever.findIngredients(3, 5));
        System.out.println(dataRetriever.findDishByIngredientName("eur"));
        System.out.println(dataRetriever.findIngredientsByCriteria(null, CategoryEnum.VEGETABLE, null, 1, 10));
        System.out.println(dataRetriever.findIngredientsByCriteria("cho", null, "Sal", 1, 10));
        System.out.println(dataRetriever.findIngredientsByCriteria("cho", null, "gâteau", 1, 10));
        System.out.println(dataRetriever.createIngredients(List.of(new Ingredient( "Fromage", 1200.00, CategoryEnum.DAIRY),
                new Ingredient( "Oignon", 500.00, CategoryEnum.VEGETABLE, null))));
        System.out.println(dataRetriever.createIngredients(List.of(new Ingredient("Carotte", 2000.00, CategoryEnum.VEGETABLE),
                new Ingredient("Laitue", 2000.00, CategoryEnum.VEGETABLE))));
        System.out.println(dataRetriever.saveDish(new Dish("Soupe de légumes", DishTypeEnum.STARTER, List.of())));

    }
}