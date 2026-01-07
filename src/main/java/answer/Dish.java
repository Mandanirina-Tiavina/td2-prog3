package answer;

import java.util.List;
import java.util.ArrayList;

public class Dish {
    private int  id;
    private String name;
    private DishType dishType;
    private List<Ingredient> ingredients;

    public Dish(int id, String name, DishType dishType, List<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.ingredients = ingredients;
    }

    public Dish() {
        this.ingredients = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DishType getDishType() {
        return dishType;
    }

    public void setDishType(DishType dishType) {
        this.dishType = dishType;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void addIngredient(Ingredient ingredient) {
        if (ingredients == null) {
            ingredients = new ArrayList<>();
        }
        ingredients.add(ingredient);
    }

    public Double getDishPrice() {
        double total = 0.0;
        if (ingredients == null) {
            return 0.0;
        }
        int i = 0;
        while (i < ingredients.size()) {
            Ingredient ingredient = ingredients.get(i);
            if (ingredient != null) {
                total = total + ingredient.getPrice();
            }
            i = i + 1;
        }
        return total;
    }
}
