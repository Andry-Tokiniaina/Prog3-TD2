import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataRetriever {

    Order findOrderByReference(String reference) {
        DBConnection dbConnection = new DBConnection();
        try (Connection connection = dbConnection.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    select id, reference, creation_datetime from "order" where reference like ?""");
            preparedStatement.setString(1, reference);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Order order = new Order();
                Integer idOrder = resultSet.getInt("id");
                order.setId(idOrder);
                order.setReference(resultSet.getString("reference"));
                order.setCreationDatetime(resultSet.getTimestamp("creation_datetime").toInstant());
                order.setDishOrders(findDishOrderByIdOrder(idOrder));
                return order;
            }
            throw new RuntimeException("Order not found with reference " + reference);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<DishOrder> findDishOrderByIdOrder(Integer idOrder) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<DishOrder> dishOrders = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            select id, id_dish, quantity from dish_order where dish_order.id_order = ?
                            """);
            preparedStatement.setInt(1, idOrder);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Dish dish = findDishById(resultSet.getInt("id_dish"));
                DishOrder dishOrder = new DishOrder();
                dishOrder.setId(resultSet.getInt("id"));
                dishOrder.setQuantity(resultSet.getInt("quantity"));
                dishOrder.setDish(dish);
                dishOrders.add(dishOrder);
            }
            dbConnection.closeConnection(connection);
            return dishOrders;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    Dish findDishById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            select dish.id as dish_id, dish.name as dish_name, dish_type, dish.selling_price as dish_price
                            from dish
                            where dish.id = ?;
                            """);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Dish dish = new Dish();
                dish.setId(resultSet.getInt("dish_id"));
                dish.setName(resultSet.getString("dish_name"));
                dish.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));
                dish.setPrice(resultSet.getObject("dish_price") == null
                        ? null : resultSet.getDouble("dish_price"));
                dish.setDishIngredients(findIngredientByDishId(id));
                return dish;
            }
            dbConnection.closeConnection(connection);
            throw new RuntimeException("Dish not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    Ingredient saveIngredient(Ingredient toSave) {
        String upsertIngredientSql = """
                    INSERT INTO ingredient (id, name, price, category)
                    VALUES (?, ?, ?, ?::dish_type)
                    ON CONFLICT (id) DO UPDATE
                    SET name = EXCLUDED.name,
                        category = EXCLUDED.category,
                        price = EXCLUDED.price
                    RETURNING id
                """;

        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);
            Integer ingredientId;
            try (PreparedStatement ps = conn.prepareStatement(upsertIngredientSql)) {
                if (toSave.getId() != null) {
                    ps.setInt(1, toSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "ingredient", "id"));
                }
                if (toSave.getPrice() != null) {
                    ps.setDouble(2, toSave.getPrice());
                } else {
                    ps.setNull(2, Types.DOUBLE);
                }
                ps.setString(3, toSave.getName());
                ps.setString(4, toSave.getCategory().name());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    ingredientId = rs.getInt(1);
                }
            }

            insertIngredientStockMovements(conn, toSave);

            conn.commit();
            return findIngredientById(ingredientId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertIngredientStockMovements(Connection conn, Ingredient ingredient) {
        List<StockMovement> stockMovementList = ingredient.getStockMovementList();
        String sql = """
                insert into stock_movement(id, id_ingredient, quantity, type, unit, creation_datetime)
                values (?, ?, ?, ?::movement_type, ?::unit, ?)
                on conflict (id) do nothing
                """;
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            for (StockMovement stockMovement : stockMovementList) {
                if (ingredient.getId() != null) {
                    preparedStatement.setInt(1, ingredient.getId());
                } else {
                    preparedStatement.setInt(1, getNextSerialValue(conn, "stock_movement", "id"));
                }
                preparedStatement.setInt(2, ingredient.getId());
                preparedStatement.setDouble(3, stockMovement.getValue().getQuantity());
                preparedStatement.setObject(4, stockMovement.getType());
                preparedStatement.setObject(5, stockMovement.getValue().getUnit());
                preparedStatement.setTimestamp(6, Timestamp.from(stockMovement.getCreationDatetime()));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    Ingredient findIngredientById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        try (Connection connection = dbConnection.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("select id, name, price, category from ingredient where id = ?;");
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int idIngredient = resultSet.getInt("id");
                String name = resultSet.getString("name");
                CategoryEnum category = CategoryEnum.valueOf(resultSet.getString("category"));
                Double price = resultSet.getDouble("price");
                return new Ingredient(idIngredient, name, category, price, findStockMovementsByIngredientId(idIngredient));
            }
            throw new RuntimeException("Ingredient not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    List<StockMovement> findStockMovementsByIngredientId(Integer id) {

        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<StockMovement> stockMovementList = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            select id, quantity, unit, type, creation_datetime
                            from stock_movement
                            where stock_movement.id_ingredient = ?;
                            """);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                StockMovement stockMovement = new StockMovement();
                stockMovement.setId(resultSet.getInt("id"));
                stockMovement.setType(MovementTypeEnum.valueOf(resultSet.getString("type")));
                stockMovement.setCreationDatetime(resultSet.getTimestamp("creation_datetime").toInstant());

                StockValue stockValue = new StockValue();
                stockValue.setQuantity(resultSet.getDouble("quantity"));
                stockValue.setUnit(Unit.valueOf(resultSet.getString("unit")));
                stockMovement.setValue(stockValue);

                stockMovementList.add(stockMovement);
            }
            dbConnection.closeConnection(connection);
            return stockMovementList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    Dish saveDish(Dish toSave) {
        String upsertDishSql = """
                    INSERT INTO dish (id, selling_price, name, dish_type)
                    VALUES (?, ?, ?, ?::dish_type)
                    ON CONFLICT (id) DO UPDATE
                    SET name = EXCLUDED.name,
                        dish_type = EXCLUDED.dish_type,
                        selling_price = EXCLUDED.selling_price
                    RETURNING id
                """;

        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);
            Integer dishId;
            try (PreparedStatement ps = conn.prepareStatement(upsertDishSql)) {
                if (toSave.getId() != null) {
                    ps.setInt(1, toSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "dish", "id"));
                }
                if (toSave.getPrice() != null) {
                    ps.setDouble(2, toSave.getPrice());
                } else {
                    ps.setNull(2, Types.DOUBLE);
                }
                ps.setString(3, toSave.getName());
                ps.setString(4, toSave.getDishType().name());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    dishId = rs.getInt(1);
                }
            }

            List<DishIngredient> newDishIngredients = toSave.getDishIngredients();
            detachIngredients(conn, newDishIngredients);
            attachIngredients(conn, newDishIngredients);

            conn.commit();
            return findDishById(dishId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        if (newIngredients == null || newIngredients.isEmpty()) {
            return List.of();
        }
        List<Ingredient> savedIngredients = new ArrayList<>();
        DBConnection dbConnection = new DBConnection();
        Connection conn = dbConnection.getConnection();
        try {
            conn.setAutoCommit(false);
            String insertSql = """
                        INSERT INTO ingredient (id, name, category, price)
                        VALUES (?, ?, ?::ingredient_category, ?)
                        RETURNING id
                    """;
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Ingredient ingredient : newIngredients) {
                    if (ingredient.getId() != null) {
                        ps.setInt(1, ingredient.getId());
                    } else {
                        ps.setInt(1, getNextSerialValue(conn, "ingredient", "id"));
                    }
                    ps.setString(2, ingredient.getName());
                    ps.setString(3, ingredient.getCategory().name());
                    ps.setDouble(4, ingredient.getPrice());

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        int generatedId = rs.getInt(1);
                        ingredient.setId(generatedId);
                        savedIngredients.add(ingredient);
                    }
                }
                conn.commit();
                return savedIngredients;
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }


    private void detachIngredients(Connection conn, List<DishIngredient> dishIngredients) {
        Map<Integer, List<DishIngredient>> dishIngredientsGroupByDishId = dishIngredients.stream()
                .collect(Collectors.groupingBy(dishIngredient -> dishIngredient.getDish().getId()));
        dishIngredientsGroupByDishId.forEach((dishId, dishIngredientList) -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM dish_ingredient where id_dish = ?")) {
                ps.setInt(1, dishId);
                ps.executeUpdate(); // TODO: must be a grouped by batch
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void attachIngredients(Connection conn, List<DishIngredient> ingredients)
            throws SQLException {

        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }
        String attachSql = """
                    insert into dish_ingredient (id, id_ingredient, id_dish, required_quantity, unit)
                    values (?, ?, ?, ?, ?::unit)
                """;

        try (PreparedStatement ps = conn.prepareStatement(attachSql)) {
            for (DishIngredient dishIngredient : ingredients) {
                ps.setInt(1, getNextSerialValue(conn, "dish_ingredient", "id"));
                ps.setInt(2, dishIngredient.getIngredient().getId());
                ps.setInt(3, dishIngredient.getDish().getId());
                ps.setDouble(4, dishIngredient.getQuantity());
                ps.setObject(5, dishIngredient.getUnit());
                ps.addBatch(); // Can be substitute ps.executeUpdate() but bad performance
            }
            ps.executeBatch();
        }
    }

    private List<DishIngredient> findIngredientByDishId(Integer idDish) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<DishIngredient> dishIngredients = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            select ingredient.id, ingredient.name, ingredient.price, ingredient.category, di.required_quantity, di.unit
                            from ingredient join dish_ingredient di on di.id_ingredient = ingredient.id where id_dish = ?;
                            """);
            preparedStatement.setInt(1, idDish);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));

                DishIngredient dishIngredient = new DishIngredient();
                dishIngredient.setIngredient(ingredient);
                dishIngredient.setQuantity(resultSet.getObject("required_quantity") == null ? null : resultSet.getDouble("required_quantity"));
                dishIngredient.setUnit(Unit.valueOf(resultSet.getString("unit")));

                dishIngredients.add(dishIngredient);
            }
            dbConnection.closeConnection(connection);
            return dishIngredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private String getSerialSequenceName(Connection conn, String tableName, String columnName)
            throws SQLException {

        String sql = "SELECT pg_get_serial_sequence(?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    private int getNextSerialValue(Connection conn, String tableName, String columnName)
            throws SQLException {

        String sequenceName = getSerialSequenceName(conn, tableName, columnName);
        if (sequenceName == null) {
            throw new IllegalArgumentException(
                    "Any sequence found for " + tableName + "." + columnName
            );
        }
        updateSequenceNextValue(conn, tableName, columnName, sequenceName);

        String nextValSql = "SELECT nextval(?)";

        try (PreparedStatement ps = conn.prepareStatement(nextValSql)) {
            ps.setString(1, sequenceName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void updateSequenceNextValue(Connection conn, String tableName, String columnName, String sequenceName) throws SQLException {
        String setValSql = String.format(
                "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 0) FROM %s))",
                sequenceName, columnName, tableName
        );

        try (PreparedStatement ps = conn.prepareStatement(setValSql)) {
            ps.executeQuery();
        }
    }
    Order saveOrder(Order toSave) {
        DBConnection dbConnection = new DBConnection();

        String insertOrderSql = """
        INSERT INTO "order" (id, reference, creation_datetime, id_table, client_arrive_datetime)
        VALUES (?, ?, ?, ?, ?)
        RETURNING id;
    """;

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);

            Map<Integer, StockValue> requiredIngredients = new HashMap<>();
            if (toSave.getTableOrder().getTable() == null){
                throw new RuntimeException("no table given");
            }

            for (DishOrder dishOrder : toSave.getDishOrders()) {
                for (DishIngredient dishIngredient : dishOrder.getDish().getDishIngredients()) {

                    int ingredientId = dishIngredient.getIngredient().getId();
                    double qtyNeeded =
                            dishIngredient.getQuantity() * dishOrder.getQuantity();

                    StockValue current = requiredIngredients.get(ingredientId);

                    if (current == null) {
                        requiredIngredients.put(
                                ingredientId,
                                new StockValue(qtyNeeded, dishIngredient.getUnit())
                        );
                    } else {
                        current.setQuantity(current.getQuantity() + qtyNeeded);
                    }
                }
            }

            for (Map.Entry<Integer, StockValue> entry : requiredIngredients.entrySet()) {
                int ingredientId = entry.getKey();
                StockValue required = entry.getValue();

                Ingredient ingredient = findIngredientById(ingredientId);
                StockValue available =
                        ingredient.getStockValueAt(toSave.getCreationDatetime());

                if (available.getQuantity() < required.getQuantity()) {
                    throw new RuntimeException(
                            "insufficient stock for: " + ingredient.getName()
                    );
                }
            }

            Table requestedTable = toSave.getTableOrder().getTable();
            Instant time = toSave.getCreationDatetime();

            if (!requestedTable.isAvailableAt(time)) {
                List<Table> alternatives = findAvailableTablesAt(time);

                throw new RuntimeException(
                        "Table " + requestedTable +
                                " is not available. Available tables: " +
                                alternatives.stream()
                                        .filter(t -> t.isAvailableAt(toSave.getCreationDatetime()))
                                        .map(Table::toString)
                                        .collect(Collectors.joining(", "))
                );
            }

            int orderId;
            try (PreparedStatement ps = conn.prepareStatement(insertOrderSql)) {
                ps.setInt(1,
                        toSave.getId() != null
                                ? toSave.getId()
                                : getNextSerialValue(conn, "order", "id")
                );
                ps.setString(2, toSave.getReference());
                ps.setTimestamp(3, Timestamp.from(toSave.getCreationDatetime()));
                ps.setInt(4, toSave.getTableOrder().getTable().getId());
                ps.setTimestamp(5, Timestamp.from(toSave.getCreationDatetime()));

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    orderId = rs.getInt(1);
                }
            }

            attachDishOrder(conn, orderId, toSave.getDishOrders());

            for (Map.Entry<Integer, StockValue> entry : requiredIngredients.entrySet()) {
                int ingredientId = entry.getKey();
                StockValue value = entry.getValue();

                StockMovement movement = new StockMovement();
                movement.setType(MovementTypeEnum.OUT);
                movement.setCreationDatetime(toSave.getCreationDatetime());
                movement.setValue(value);

                attachStockMovement(conn, ingredientId, List.of(movement));
            }

            conn.commit();
            toSave.setId(orderId);
            return toSave;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Table> findAllTables() {
        DBConnection dbConnection = new DBConnection();

        String sql = """
        SELECT 
            t.id   AS t_id,
            t.number AS t_number,
            o.id   AS o_id,
            o.reference,
            o.creation_datetime,
            o.client_arrive_datetime,
            o.client_leave_datetime
        FROM table t
        LEFT JOIN "order" o ON o.id_table = t.id
        ORDER BY t.number;
    """;

        Map<Integer, Table> tables = new HashMap<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int tableId = rs.getInt("t_id");

                Table table = tables.get(tableId);
                if (table == null) {
                    table = new Table();
                    table.setId(tableId);
                    table.setNumber(rs.getInt("t_number"));
                    table.setOrders(new ArrayList<>());
                    tables.put(tableId, table);
                }

                int orderId = rs.getInt("o_id");
                if (!rs.wasNull()) {
                    Order order = new Order();
                    order.setId(orderId);
                    order.setReference(rs.getString("reference"));
                    order.setCreationDatetime(
                            rs.getTimestamp("creation_datetime").toInstant()
                    );

                    TableOrder tableOrder = new TableOrder();
                    tableOrder.setTable(table);
                    tableOrder.setClient_arrive_time(
                            rs.getTimestamp("client_arrive_datetime").toInstant()
                    );
                    Timestamp leaveTs = rs.getTimestamp("client_leave_datetime");
                    tableOrder.setClient_leave_time(
                            leaveTs != null ? leaveTs.toInstant() : null
                    );

                    order.setTableOrder(tableOrder);
                    table.getOrders().add(order);
                }
            }

            return new ArrayList<>(tables.values());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Table> findAvailableTablesAt(Instant instant) {
        List<Table> tables = findAllTables();

        return tables.stream()
                .filter(table -> table.isAvailableAt(instant))
                .toList();
    }


    public void attachDishOrder(Connection conn, int idOrder, List<DishOrder> dishOrders) throws SQLException {
        if (dishOrders == null || dishOrders.isEmpty()) {
            return;
        }

        String attachSql = """
                    INSERT INTO dish_order (id, id_order, id_dish, quantity)
                    VALUES (?,?,?,?)
                    ON CONFLICT (id) DO NOTHING;
                """;

        try (PreparedStatement ps = conn.prepareStatement(attachSql)) {
            for (DishOrder dishOrder : dishOrders) {
                ps.setInt(1, dishOrder.getId());
                ps.setInt(2, idOrder);
                ps.setInt(3, dishOrder.getDish().getId());
                ps.setInt(4, dishOrder.getQuantity());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void attachStockMovement(Connection conn, int ingredientId, List<StockMovement> stockMovements)
            throws SQLException {

        if (stockMovements == null || stockMovements.isEmpty()) {
            return;
        }

        String attachSql = """
                    INSERT INTO stock_movement (id, id_ingredient, quantity, type, unit, creation_datetime)
                    VALUES (?,?,?,?::movement_type,?::unit_type,?)
                    ON CONFLICT (id) DO NOTHING;
                """;

        try (PreparedStatement ps = conn.prepareStatement(attachSql)) {
            for (StockMovement stockMovement : stockMovements) {
                if ( stockMovement.getId() != null){
                    ps.setInt(1, stockMovement.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "stock_movement", "id"));
                }
                ps.setInt(2, ingredientId);
                ps.setDouble(3, stockMovement.getValue().getQuantity());
                ps.setString(4, stockMovement.getType().name());
                ps.setString(5, stockMovement.getValue().getUnit().name());
                ps.setTimestamp(6, Timestamp.from(stockMovement.getCreationDatetime()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

}
