package answer;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();

        Dish salade = dataRetriever.findDishById(1);
        System.out.println("1: " + salade.getName() + " price=" + salade.getPrice() + " cost=" + salade.getDishCost());
        System.out.println("2: grossMargin=" + salade.getGrossMargin());

        Dish riz = dataRetriever.findDishById(3);
        System.out.println("3: " + riz.getName() + " price=" + riz.getPrice() + " cost=" + riz.getDishCost());
        try {
            System.out.println("4: grossMargin=" + riz.getGrossMargin());
        } catch (RuntimeException e) {
            System.out.println("4: exception");
        }

        Dish poulet = dataRetriever.findDishById(2);
        poulet.setPrice(6500.0);
        Dish updatedPoulet = dataRetriever.saveDish(poulet);
        System.out.println("5: " + updatedPoulet.getName() + " price=" + updatedPoulet.getPrice() + " cost=" + updatedPoulet.getDishCost());
        System.out.println("6: grossMargin=" + updatedPoulet.getGrossMargin());

        Dish newDish = new Dish();
        newDish.setName("Soupe de légumes");
        newDish.setDishType(DishType.START);
        newDish.setPrice(3000.0);
        newDish.setIngredients(new ArrayList<Ingredient>());
        Dish savedSoup = dataRetriever.saveDish(newDish);
        System.out.println("7: " + savedSoup.getName() + " price=" + savedSoup.getPrice() + " cost=" + savedSoup.getDishCost());
        System.out.println("8: grossMargin=" + savedSoup.getGrossMargin());
    }

    private static String toIngredientNames(List<Ingredient> ingredients) {
        String result = "[";
        if (ingredients != null) {
            int index = 0;
            while (index < ingredients.size()) {
                Ingredient ingredient = ingredients.get(index);
                if (ingredient != null) {
                    result = result + ingredient.getName();
                    if (index < ingredients.size() - 1) {
                        result = result + ", ";
                    }
                }
                index = index + 1;
            }
        }
        result = result + "]";
        return result;
    }

    private static String toDishNames(List<Dish> dishes) {
        String result = "[";
        if (dishes != null) {
            int index = 0;
            while (index < dishes.size()) {
                Dish dish = dishes.get(index);
                if (dish != null) {
                    result = result + dish.getName();
                    if (index < dishes.size() - 1) {
                        result = result + ", ";
                    }
                }
                index = index + 1;
            }
        }
        result = result + "]";
        return result;
    }

    private static Ingredient findIngredientByName(String name, List<Ingredient> ingredients) {
        int index = 0;
        while (index < ingredients.size()) {
            Ingredient ingredient = ingredients.get(index);
            if (ingredient != null && ingredient.getName().equals(name)) {
                return ingredient;
            }
            index = index + 1;
        }
        return null;
    }
}
