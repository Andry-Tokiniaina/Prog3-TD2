package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    public List<Ingredient> findDishIngredients(int dish_id){
        List<Ingredient> ingredients = new ArrayList<>();
        DBConnection dbConnection = new DBConnection();
        String queryIngredients = "select id, name, price, category, id_dish from ingredient where id_dish = ?";

        try {
            Connection conn = dbConnection.getDBConnection();
            PreparedStatement stmtIngredients = conn.prepareStatement(queryIngredients);
            stmtIngredients.setInt(1, dish_id);
            ResultSet rsIngredients = stmtIngredients.executeQuery();

            while (rsIngredients.next()) {
                ingredients.add(new Ingredient(
                        rsIngredients.getInt("id"),
                        rsIngredients.getString("name"),
                        rsIngredients.getDouble("price"),
                        CategoryEnum.valueOf(rsIngredients.getString("category")),
                        null
                ));
            }
            conn.close();
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Dish findDishById(int id) {
        DBConnection dbConnection = new DBConnection();
        String queryDish = "select id, name, dish_type from dish where id = ?";

        try {
            Connection conn = dbConnection.getDBConnection();

            List<Ingredient> ingredients = this.findDishIngredients(id);

            Dish dish = null;

            PreparedStatement stmt = conn.prepareStatement(queryDish);
            stmt.setInt(1,id);
            ResultSet rsDish = stmt.executeQuery();
            if(rsDish.next()){
                 dish = new Dish(
                        rsDish.getInt("id"),
                        rsDish.getString("name"),
                        DishTypeEnum.valueOf(rsDish.getString("dish_type")),
                        ingredients
                );
            }

            for(Ingredient ingredient : ingredients){
                ingredient.setDish(dish);
            }
            conn.close();
            return dish;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> findIngredients(int page, int size) throws SQLException {
        DBConnection dbConnection = new DBConnection();
        int offset = (page - 1) * size;
        String query = "select i.id, i.name, i.price, i.category, i.id_dish," +
                " d.id as dish_id, d.name as dish_name, d.dish_type as dish_type " +
                "from ingredient i " +
                "inner join dish d on d.id = i.id_dish " +
                "limit ? offset ?";
        try {
            Connection conn = dbConnection.getDBConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, size);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();

            List<Ingredient> ingredients = new ArrayList<>();

            while (rs.next()) {
                Dish dish = new Dish(
                        rs.getInt("dish_id"),
                        rs.getString("dish_name"),
                        DishTypeEnum.valueOf(rs.getString("dish_type")),
                        new ArrayList<>()
                );
                Ingredient ingredient = new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category")),
                        dish
                );
                dish.getIngredients().add(ingredient);
                ingredients.add(ingredient);
            }
            conn.close();
            return ingredients;
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> ingredients) throws SQLException {
        DBConnection dbConnection = new DBConnection();
        String query = "insert into ingredient values (?, ?, ?, ?, ?)";
        Connection conn = null;
        try  {
            conn = dbConnection.getDBConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            conn.setAutoCommit(false);
            for (Ingredient ingredient : ingredients) {
                stmt.setInt(1, ingredient.getId());
                stmt.setString(2, ingredient.getName());
                stmt.setDouble(3, ingredient.getPrice());
                stmt.setString(4, ingredient.getCategory().toString());
                stmt.setInt(5, ingredient.getDish().getId());
                stmt.executeUpdate();
            }
            conn.commit();
            conn.close();
            return ingredients;
        }
        catch (SQLException e) {
            try {
                conn.rollback();
                conn.close();
            }catch (SQLException e1){}
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("an ingredient already exists");
            }
            throw new RuntimeException(e);
        }
    }

    public Dish saveDish(Dish dishToSave) {
        DBConnection dbConnection = new DBConnection();
        String addQuery = "insert into dish values (?, ?, ?, ?)";
        String updateQuery = "update dish set name = ?, price = ?, dish_type = ? where id = ?";
        String selectQuery = "select id from dish";

        try {
            List<Integer> dish_ids = new ArrayList<>();
            Connection conn = dbConnection.getDBConnection();
            PreparedStatement stmt = conn.prepareStatement(selectQuery);
            ResultSet rs = stmt.executeQuery();

            while(rs.next()){
                dish_ids.add(rs.getInt("id"));
            }

            if (dish_ids.contains(dishToSave.getId())) {
                stmt = conn.prepareStatement(updateQuery);
                stmt.setString(1, dishToSave.getName());
                stmt.setDouble(2, dishToSave.getDishPrice());
                stmt.setString(3, dishToSave.getDishType().toString());
                stmt.setInt(4, dishToSave.getId());
            }
            else {
                stmt = conn.prepareStatement(addQuery);
                stmt.setInt(1, dishToSave.getId());
                stmt.setString(2, dishToSave.getName());
                stmt.setDouble(3, dishToSave.getDishPrice());
                stmt.setString(4, dishToSave.getDishType().toString());
            }
            int rs1 = stmt.executeUpdate();
            conn.close();
            return  dishToSave;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Dish> findDishByIngredientName(String ingredientName) {
        DBConnection dbConnection = new DBConnection();
        List<Dish> dishes = new ArrayList<>();
        String query = "select d.id, d.name, d.dish_type, i.id as ingredient_id, i.name as ingredient_name from dish d " +
                "inner join ingredient i on d.id = i.dish_id " +
                "where i.name ilike ?";
        try {
            Connection conn = dbConnection.getDBConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, "%"+ingredientName+"%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Dish dish = new Dish(
                    rs.getInt("id"),
                    rs.getString("name"),
                    DishTypeEnum.valueOf(rs.getString("dish_type")),
                    findDishIngredients(rs.getInt("id"))
                );
                dishes.add(dish);
            }
            conn.close();
            return dishes;
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category,
                                                      String dishName, int page, int size) {
        DBConnection dbConnection = new DBConnection();
        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;
        String query = "select i.id, i.name, i.price, i.category, d.id as dish_id, d.name as dish_name, d.dish_type " +
                "from ingredient i " +
                "inner join dish d " +
                "on d.id = i.id_dish " +
                "where (?::text is null or i.name ilike ?) " +
                "and (?::text is null or i.category ilike ? ) " +
                "and (?::text is null or d.name ilike ? ) "+
                "limit ? offset ?";
        try {
            Connection conn = dbConnection.getDBConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, ingredientName);
            stmt.setString(2, "%"+ingredientName+"%");
            stmt.setString(3, category.toString());
            stmt.setString(4, "%"+category.toString()+"%");
            stmt.setString(5, dishName);
            stmt.setString(6, "%"+dishName+"%");
            stmt.setInt(7, size);
            stmt.setInt(8, offset);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                Dish dish = new Dish(
                        rs.getInt("dish_id"),
                        rs.getString("dish_name"),
                        DishTypeEnum.valueOf(rs.getString("dish_type")),
                        findDishIngredients(rs.getInt("dish_id"))
                );
                Ingredient ingredient = new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        CategoryEnum.valueOf(rs.getString("category")),
                        dish
                );
                ingredients.add(ingredient);
            }
            conn.close();
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
