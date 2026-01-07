package answer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    public Dish findDishById(Integer id) {
        String sql = "SELECT id, name, dish_type FROM dish WHERE id = ?";
        try (Connection connection = DBConnection.getDBConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new RuntimeException("Dish not found");
                }
                int dishId = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String dishTypeText = resultSet.getString("dish_type");
                DishType dishType = DishType.valueOf(dishTypeText);

                Dish dish = new Dish();
                dish.setId(dishId);
                dish.setName(name);
                dish.setDishType(dishType);

                List<Ingredient> ingredients = findIngredientsForDish(connection, dishId, dish);
                dish.setIngredients(ingredients);
                return dish;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while finding dish by id");
        }
    }

    private List<Ingredient> findIngredientsForDish(Connection connection, int dishId, Dish dish) throws SQLException {
        String sql = "SELECT id, name, price, category FROM ingredient WHERE id_dish = ? ORDER BY id";
        List<Ingredient> ingredients = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, dishId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    Double price = resultSet.getDouble("price");
                    String categoryText = resultSet.getString("category");
                    Category category = Category.valueOf(categoryText);

                    Ingredient ingredient = new Ingredient(id, name, price, category, dish);
                    ingredients.add(ingredient);
                }
            }
        }
        return ingredients;
    }

    public List<Ingredient> findIngredients(int page, int size) {
        String sql = "SELECT i.id, i.name, i.price, i.category, i.id_dish, d.name AS dish_name, d.dish_type " +
                "FROM ingredient i LEFT JOIN dish d ON i.id_dish = d.id " +
                "ORDER BY i.id LIMIT ? OFFSET ?";
        List<Ingredient> ingredients = new ArrayList<>();
        try (Connection connection = DBConnection.getDBConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int offset = (page - 1) * size;
            statement.setInt(1, size);
            statement.setInt(2, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Ingredient ingredient = mapIngredientWithDish(resultSet);
                    ingredients.add(ingredient);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while finding ingredients");
        }
        return ingredients;
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        Connection connection = null;
        try {
            connection = DBConnection.getDBConnection();
            connection.setAutoCommit(false);

            List<Ingredient> created = new ArrayList<>();
            int index = 0;
            while (index < newIngredients.size()) {
                Ingredient ingredient = newIngredients.get(index);
                if (ingredientExists(connection, ingredient.getName())) {
                    connection.rollback();
                    throw new RuntimeException("Ingredient already exists");
                }

                String sql = "INSERT INTO ingredient(name, price, category, id_dish) VALUES (?, ?, CAST(? AS category_enum), ?) RETURNING id";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, ingredient.getName());
                    statement.setDouble(2, ingredient.getPrice());
                    statement.setString(3, ingredient.getCategory().name());
                    if (ingredient.getDish() == null) {
                        statement.setNull(4, java.sql.Types.INTEGER);
                    } else {
                        statement.setInt(4, ingredient.getDish().getId());
                    }
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            int id = resultSet.getInt(1);
                            Ingredient createdIngredient = new Ingredient(id, ingredient.getName(), ingredient.getPrice(), ingredient.getCategory(), ingredient.getDish());
                            created.add(createdIngredient);
                        }
                    }
                }

                index = index + 1;
            }

            connection.commit();
            return created;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            throw new RuntimeException("Error while creating ingredients");
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean ingredientExists(Connection connection, String name) throws SQLException {
        String sql = "SELECT id FROM ingredient WHERE name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public Dish saveDish(Dish dishToSave) {
        Connection connection = null;
        try {
            connection = DBConnection.getDBConnection();
            connection.setAutoCommit(false);

            int dishId = dishToSave.getId();
            if (dishId <= 0) {
                String insertSql = "INSERT INTO dish(name, dish_type) VALUES (?, CAST(? AS dish_type_enum)) RETURNING id";
                try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                    statement.setString(1, dishToSave.getName());
                    statement.setString(2, dishToSave.getDishType().name());
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            dishId = resultSet.getInt(1);
                            dishToSave.setId(dishId);
                        }
                    }
                }
            } else {
                String updateSql = "UPDATE dish SET name = ?, dish_type = CAST(? AS dish_type_enum) WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                    statement.setString(1, dishToSave.getName());
                    statement.setString(2, dishToSave.getDishType().name());
                    statement.setInt(3, dishId);
                    statement.executeUpdate();
                }
            }

            String clearSql = "UPDATE ingredient SET id_dish = NULL WHERE id_dish = ?";
            try (PreparedStatement statement = connection.prepareStatement(clearSql)) {
                statement.setInt(1, dishId);
                statement.executeUpdate();
            }

            List<Ingredient> ingredients = dishToSave.getIngredients();
            if (ingredients != null) {
                int index = 0;
                while (index < ingredients.size()) {
                    Ingredient ingredient = ingredients.get(index);
                    if (ingredient != null) {
                        String sql = "UPDATE ingredient SET id_dish = ? WHERE id = ?";
                        try (PreparedStatement statement = connection.prepareStatement(sql)) {
                            statement.setInt(1, dishId);
                            statement.setInt(2, ingredient.getId());
                            statement.executeUpdate();
                        }
                    }
                    index = index + 1;
                }
            }

            connection.commit();
            return findDishById(dishId);
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            throw new RuntimeException("Error while saving dish");
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Dish> findDishsByIngredientName(String ingredientName) {
        String sql = "SELECT DISTINCT d.id FROM dish d JOIN ingredient i ON d.id = i.id_dish WHERE i.name ILIKE ? ORDER BY d.id";
        List<Dish> dishes = new ArrayList<>();
        try (Connection connection = DBConnection.getDBConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + ingredientName + "%";
            statement.setString(1, pattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int dishId = resultSet.getInt("id");
                    Dish dish = findDishById(dishId);
                    dishes.add(dish);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while finding dishes by ingredient name");
        }
        return dishes;
    }

    public List<Ingredient> findIngredientsByCriteria(String ingredientName, Category category, String dishName, int page, int size) {
        String sql = "SELECT i.id, i.name, i.price, i.category, i.id_dish, d.name AS dish_name, d.dish_type " +
                "FROM ingredient i LEFT JOIN dish d ON i.id_dish = d.id WHERE 1 = 1";

        if (ingredientName != null) {
            sql = sql + " AND i.name ILIKE ?";
        }
        if (category != null) {
            sql = sql + " AND i.category = CAST(? AS category_enum)";
        }
        if (dishName != null) {
            sql = sql + " AND d.name ILIKE ?";
        }
        sql = sql + " ORDER BY i.id LIMIT ? OFFSET ?";

        List<Ingredient> ingredients = new ArrayList<>();
        try (Connection connection = DBConnection.getDBConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            if (ingredientName != null) {
                String pattern = "%" + ingredientName + "%";
                statement.setString(index, pattern);
                index = index + 1;
            }
            if (category != null) {
                statement.setString(index, category.name());
                index = index + 1;
            }
            if (dishName != null) {
                String pattern = "%" + dishName + "%";
                statement.setString(index, pattern);
                index = index + 1;
            }

            int offset = (page - 1) * size;
            statement.setInt(index, size);
            statement.setInt(index + 1, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Ingredient ingredient = mapIngredientWithDish(resultSet);
                    ingredients.add(ingredient);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while finding ingredients by criteria");
        }
        return ingredients;
    }

    private Ingredient mapIngredientWithDish(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        Double price = resultSet.getDouble("price");
        String categoryText = resultSet.getString("category");
        Category category = Category.valueOf(categoryText);

        int dishId = resultSet.getInt("id_dish");
        Dish dish = null;
        if (!resultSet.wasNull()) {
            String dishName = resultSet.getString("dish_name");
            String dishTypeText = resultSet.getString("dish_type");
            DishType dishType = DishType.valueOf(dishTypeText);
            dish = new Dish();
            dish.setId(dishId);
            dish.setName(dishName);
            dish.setDishType(dishType);
        }

        return new Ingredient(id, name, price, category, dish);
    }
}
