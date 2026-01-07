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
        System.out.println(dataRetriever.findIngredientsByCriteria("cho", null, "gateau", 1, 10));

        //Création de Fromage et de Oignon
        try{
            System.out.println(dataRetriever.createIngredients(List.of(new Ingredient( "Fromage", 1200.00, CategoryEnum.DAIRY),
                new Ingredient( "Oignon", 500.00, CategoryEnum.VEGETABLE))));
        }
        catch(Exception e){
            System.out.println("error");
        }

        //erreur Laitue existe déjà
        try{
            System.out.println(dataRetriever.createIngredients(List.of(new Ingredient("Carotte", 2000.00, CategoryEnum.VEGETABLE),
               new Ingredient("Laitue", 2000.00, CategoryEnum.VEGETABLE))));
        }catch(Exception e){
            System.out.println("error");
        }

        //création de Soupe de légumes
        System.out.println(dataRetriever.saveDish(new Dish("Soupe de légumes", DishTypeEnum.STARTER, List.of(dataRetriever.findIngredientByName("Oignon")))));

        //Mise à joour de salade fraîche
        System.out.println(dataRetriever.saveDish(new Dish(1, "Salade fraîche", DishTypeEnum.STARTER,
                List.of(dataRetriever.findIngredientByName("Oignon"), dataRetriever.findIngredientByName("Laitue"),
                        dataRetriever.findIngredientByName("Tomate"),  dataRetriever.findIngredientByName("Fromage")))));

        /* ****************************************************************************************************************************** */

        //ajout de salade de fromage à l'index 1
        System.out.println(dataRetriever.saveDish(new Dish(1, "Salade de fromage", DishTypeEnum.STARTER,
                 List.of(dataRetriever.findIngredientByName("Fromage")))).getGrossMargin());

        //mise à jour de salade de fromage avec prix
        System.out.println(dataRetriever.saveDish(new Dish(1, "Salade de fromage", DishTypeEnum.STARTER, 1200.00,
                List.of(dataRetriever.findIngredientByName("Fromage")))).getGrossMargin());

        //getGrossMargin sans price renvoie une erreur
        try {
            System.out.println(dataRetriever.findDishById(6).getGrossMargin());
        }catch(Exception e){
            System.out.println("error");
        }

        //getGrossMargin avec price retourne la différence
        try {
            System.out.println(dataRetriever.findDishById(2).getGrossMargin());
        }catch(Exception e){
            System.out.println("error");
        }
    }
}