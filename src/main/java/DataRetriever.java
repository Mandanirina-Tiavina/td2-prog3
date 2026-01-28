import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataRetriever {
    Dish findDishById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        try {
            String sql = "select dish.id as dish_id, dish.name as dish_name, dish_type, dish.price as dish_price from dish where dish.id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Dish dish = new Dish();
                dish.setId(resultSet.getInt("dish_id"));
                dish.setName(resultSet.getString("dish_name"));
                dish.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));
                dish.setPrice(resultSet.getObject("dish_price") == null
                        ? null : resultSet.getDouble("dish_price"));
                dish.setIngredients(findIngredientByDishId(id));
                return dish;
            }
            dbConnection.closeConnection(connection);
            throw new RuntimeException("Dish not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    Order findOrderByReference(String reference) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        try {
            String sql = "select o.id as order_id, o.reference, o.creation_datetime, od.id as dish_order_id, od.quantity, d.id as dish_id, d.name as dish_name, d.dish_type, d.price as dish_price from \"order\" o left join dish_order od on o.id = od.id_order left join dish d on od.id_dish = d.id where o.reference = ? order by od.id;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, reference);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                dbConnection.closeConnection(connection);
                throw new RuntimeException("Order not found " + reference);
            }

            Order order = new Order();
            order.setId(resultSet.getInt("order_id"));
            order.setReference(resultSet.getString("reference"));
            Timestamp ts = resultSet.getTimestamp("creation_datetime");
            if (ts != null) {
                order.setCreationDatetime(ts.toInstant());
            }

            List<DishOrder> dishOrders = new ArrayList<>();

            do {
                Object dishOrderIdObj = resultSet.getObject("dish_order_id");
                if (dishOrderIdObj == null) {
                    continue;
                }

                DishOrder dishOrder = new DishOrder();
                dishOrder.setId(resultSet.getInt("dish_order_id"));
                dishOrder.setQuantity(resultSet.getInt("quantity"));

                Dish dish = new Dish();
                dish.setId(resultSet.getInt("dish_id"));
                dish.setName(resultSet.getString("dish_name"));
                dish.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));
                Object dishPriceObj = resultSet.getObject("dish_price");
                dish.setPrice(dishPriceObj == null ? null : resultSet.getDouble("dish_price"));

                dishOrder.setDish(dish);

                dishOrders.add(dishOrder);
            } while (resultSet.next());

            order.setDishOrders(dishOrders);
            dbConnection.closeConnection(connection);
            return order;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    Ingredient findIngredientById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        try {
            String sql = "select id, name, price, category from ingredient where id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));
                ingredient.setStockMovementList(findStockMovementByIngredientId(id));
                dbConnection.closeConnection(connection);
                return ingredient;
            }
            dbConnection.closeConnection(connection);
            throw new RuntimeException("Ingredient not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    Dish saveDish(Dish toSave) {
        String upsertDishSql = "INSERT INTO dish (id, price, name, dish_type) VALUES (?, ?, ?, ?::dish_type) ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, dish_type = EXCLUDED.dish_type RETURNING id";

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

            List<Ingredient> newIngredients = toSave.getIngredients();
            detachIngredients(conn, dishId);
            attachIngredients(conn, dishId, newIngredients);

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
            String insertSql = "INSERT INTO ingredient (id, name, category, price, required_quantity) VALUES (?, ?, ?::ingredient_category, ?, ?) RETURNING id";
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
                    if (ingredient.getQuantity() != null) {
                        ps.setDouble(5, ingredient.getQuantity());
                    }else {
                        ps.setNull(5, Types.DOUBLE);
                    }

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

    Ingredient saveIngredient(Ingredient toSave) {
        String upsertIngredientSql = "INSERT INTO ingredient (id, name, price, category) VALUES (?, ?, ?, ?::ingredient_category) ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, price = EXCLUDED.price, category = EXCLUDED.category RETURNING id";

        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);

            Integer ingredientId;
            try (PreparedStatement ps = conn.prepareStatement(upsertIngredientSql)) {
                if (toSave.getId() != null) {
                    ps.setInt(1, toSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "ingredient", "id"));
                }

                ps.setString(2, toSave.getName());

                if (toSave.getPrice() != null) {
                    ps.setDouble(3, toSave.getPrice());
                } else {
                    ps.setNull(3, Types.DOUBLE);
                }

                ps.setString(4, toSave.getCategory().name());

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    ingredientId = rs.getInt(1);
                }
            }

            List<StockMovement> movements = toSave.getStockMovementList();
            if (movements != null && !movements.isEmpty()) {
                String insertSql = "INSERT INTO stock_movement (id_ingredient, quantity, type, unit, creation_datetime) VALUES (?, ?, ?::movement_type, ?::unit_type, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    for (StockMovement movement : movements) {
                        if (movement.getId() != null) {
                            // Existing movement: do nothing (no update, no delete)
                            continue;
                        }
                        if (movement.getValue() == null) {
                            continue;
                        }

                        ps.setInt(1, ingredientId);
                        ps.setDouble(2, movement.getValue().getQuantity());
                        ps.setString(3, movement.getType().name());
                        ps.setString(4, movement.getValue().getUnit().name());
                        ps.setTimestamp(5, Timestamp.from(movement.getCreationDatetime()));
                        ps.executeUpdate();

                        try (ResultSet rsKeys = ps.getGeneratedKeys()) {
                            if (rsKeys.next()) {
                                movement.setId(rsKeys.getInt(1));
                            }
                        }
                    }
                }
            }

            conn.commit();
            return findIngredientById(ingredientId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    Order saveOrder(Order orderToSave) {
        if (orderToSave == null) {
            throw new RuntimeException("Order is null");
        }

        Instant orderTime = orderToSave.getCreationDatetime();
        if (orderTime == null) {
            orderTime = Instant.now();
            orderToSave.setCreationDatetime(orderTime);
        }

        checkStockForOrder(orderToSave, orderTime);

        String upsertOrderSql = "INSERT INTO \"order\" (id, reference, creation_datetime) VALUES (?, ?, ?) ON CONFLICT (id) DO UPDATE SET reference = EXCLUDED.reference, creation_datetime = EXCLUDED.creation_datetime RETURNING id";

        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);

            String reference = orderToSave.getReference();
            if (reference == null) {
                reference = generateNextOrderReference(conn);
                orderToSave.setReference(reference);
            }

            Integer orderId;
            try (PreparedStatement ps = conn.prepareStatement(upsertOrderSql)) {
                if (orderToSave.getId() != null) {
                    ps.setInt(1, orderToSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "\"order\"", "id"));
                }
                ps.setString(2, reference);
                ps.setTimestamp(3, Timestamp.from(orderTime));

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    orderId = rs.getInt(1);
                }
            }

            detachDishOrders(conn, orderId);
            attachDishOrders(conn, orderId, orderToSave.getDishOrders());

            conn.commit();
            return findOrderByReference(reference);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkStockForOrder(Order orderToSave, Instant orderTime) {
        List<DishOrder> dishOrders = orderToSave.getDishOrders();
        if (dishOrders == null || dishOrders.isEmpty()) {
            return;
        }

        List<Integer> ingredientIds = new ArrayList<>();
        List<Double> requiredQuantities = new ArrayList<>();

        for (int i = 0; i < dishOrders.size(); i++) {
            DishOrder dishOrder = dishOrders.get(i);
            Dish dish = dishOrder.getDish();
            if (dish == null || dish.getId() == null) {
                continue;
            }

            List<Ingredient> recipeIngredients = findIngredientByDishId(dish.getId());
            for (int j = 0; j < recipeIngredients.size(); j++) {
                Ingredient ingredient = recipeIngredients.get(j);
                if (ingredient.getQuantity() == null) {
                    continue;
                }

                double requiredForLine = ingredient.getQuantity() * dishOrder.getQuantity();

                int index = -1;
                for (int k = 0; k < ingredientIds.size(); k++) {
                    if (ingredientIds.get(k).equals(ingredient.getId())) {
                        index = k;
                        break;
                    }
                }
                if (index == -1) {
                    ingredientIds.add(ingredient.getId());
                    requiredQuantities.add(requiredForLine);
                } else {
                    double current = requiredQuantities.get(index);
                    requiredQuantities.set(index, current + requiredForLine);
                }
            }
        }

        for (int i = 0; i < ingredientIds.size(); i++) {
            Integer ingredientId = ingredientIds.get(i);
            double required = requiredQuantities.get(i);

            Ingredient ingredient = findIngredientById(ingredientId);
            StockValue stockValue = ingredient.getStockValueAt(orderTime);
            double available = stockValue.getQuantity();

            if (available < required) {
                throw new RuntimeException("Stock insuffisant pour l'ingrédient " + ingredient.getName());
            }
        }
    }
    private void detachIngredients(Connection conn, Integer dishId)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM dish_ingredient WHERE id_dish = ?")) {
            ps.setInt(1, dishId);
            ps.executeUpdate();
        }
    }

    private void attachIngredients(Connection conn, Integer dishId, List<Ingredient> ingredients)
            throws SQLException {

        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }

        String attachSql = "INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit) VALUES (?, ?, ?, ?::unit_type)";

        try (PreparedStatement ps = conn.prepareStatement(attachSql)) {
            for (Ingredient ingredient : ingredients) {
                ps.setInt(1, dishId);
                ps.setInt(2, ingredient.getId());
                if (ingredient.getQuantity() == null) {
                    ps.setNull(3, Types.NUMERIC);
                } else {
                    ps.setDouble(3, ingredient.getQuantity());
                }
                ps.setString(4, "KG");
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void detachDishOrders(Connection conn, Integer orderId)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM dish_order WHERE id_order = ?")) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
    }

    private void attachDishOrders(Connection conn, Integer orderId, List<DishOrder> dishOrders)
            throws SQLException {

        if (dishOrders == null || dishOrders.isEmpty()) {
            return;
        }

        String attachSql = "INSERT INTO dish_order (id_order, id_dish, quantity) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(attachSql)) {
            for (int i = 0; i < dishOrders.size(); i++) {
                DishOrder dishOrder = dishOrders.get(i);
                if (dishOrder.getDish() == null || dishOrder.getDish().getId() == null) {
                    continue;
                }
                ps.setInt(1, orderId);
                ps.setInt(2, dishOrder.getDish().getId());
                ps.setInt(3, dishOrder.getQuantity());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<Ingredient> findIngredientByDishId(Integer idDish) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<Ingredient> ingredients = new ArrayList<>();
        try {
            String sql = "select i.id, i.name, i.price, i.category, di.quantity_required from dish_ingredient di join ingredient i on di.id_ingredient = i.id where di.id_dish = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, idDish);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(resultSet.getInt("id"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("category")));
                Object requiredQuantity = resultSet.getObject("quantity_required");
                ingredient.setQuantity(requiredQuantity == null ? null : resultSet.getDouble("quantity_required"));
                ingredients.add(ingredient);
            }
            dbConnection.closeConnection(connection);
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<StockMovement> findStockMovementByIngredientId(Integer idIngredient) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<StockMovement> movements = new ArrayList<>();
        try {
            String sql = "select id, quantity, type, unit, creation_datetime from stock_movement where id_ingredient = ? order by id;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, idIngredient);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                StockMovement movement = new StockMovement();
                movement.setId(resultSet.getInt("id"));

                StockValue value = new StockValue();
                value.setQuantity(resultSet.getDouble("quantity"));
                value.setUnit(Unit.valueOf(resultSet.getString("unit")));
                movement.setValue(value);

                movement.setType(MovementTypeEnum.valueOf(resultSet.getString("type")));
                movement.setCreationDatetime(resultSet.getTimestamp("creation_datetime").toInstant());

                movements.add(movement);
            }
            dbConnection.closeConnection(connection);
            return movements;
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

    private String generateNextOrderReference(Connection conn) throws SQLException {
        String sql = "select max(reference) as max_ref from \"order\"";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                String maxRef = null;
                if (rs.next()) {
                    maxRef = rs.getString("max_ref");
                }

                int nextNumber = 1;
                if (maxRef != null && maxRef.startsWith("ORD") && maxRef.length() > 3) {
                    String numberPart = maxRef.substring(3);
                    try {
                        nextNumber = Integer.parseInt(numberPart) + 1;
                    } catch (NumberFormatException e) {
                        nextNumber = 1;
                    }
                }

                String pattern = "%05d";
                String formattedNumber = String.format(pattern, nextNumber);
                return "ORD" + formattedNumber;
            }
        }
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
        String maxSql = String.format(
                "SELECT COALESCE(MAX(%s), 0) FROM %s",
                columnName, tableName
        );

        try (PreparedStatement psMax = conn.prepareStatement(maxSql)) {
            try (ResultSet rs = psMax.executeQuery()) {
                if (rs.next()) {
                    long maxValue = rs.getLong(1);

                    if (maxValue == 0L) {
                        return;
                    }

                    String setValSql = String.format(
                            "SELECT setval('%s', %d)",
                            sequenceName, maxValue
                    );

                    try (PreparedStatement psSet = conn.prepareStatement(setValSql)) {
                        psSet.executeQuery();
                    }
                }
            }
        }
    }
}
