package answer;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();

        Dish dish1 = dataRetriever.findDishById(1);
        System.out.println("7a: " + dish1.getName() + " ingredients=" + toIngredientNames(dish1.getIngredients()));

        try {
            dataRetriever.findDishById(999);
            System.out.println("7b: no exception");
        } catch (RuntimeException e) {
            System.out.println("7b: exception");
        }

        List<Ingredient> page2size2 = dataRetriever.findIngredients(2, 2);
        System.out.println("7c: " + toIngredientNames(page2size2));

        List<Ingredient> page3size5 = dataRetriever.findIngredients(3, 5);
        System.out.println("7d: " + toIngredientNames(page3size5));

        List<Dish> dishesByIngredientName = dataRetriever.findDishsByIngredientName("eur");
        System.out.println("7e: " + toDishNames(dishesByIngredientName));

        List<Ingredient> vegetables = dataRetriever.findIngredientsByCriteria(null, Category.VEGETABLE, null, 1, 10);
        System.out.println("7f: " + toIngredientNames(vegetables));

        List<Ingredient> emptyList = dataRetriever.findIngredientsByCriteria("cho", null, "Sal", 1, 10);
        System.out.println("7g: " + toIngredientNames(emptyList));

        List<Ingredient> chocolatList = dataRetriever.findIngredientsByCriteria("cho", null, "gâteau", 1, 10);
        System.out.println("7h: " + toIngredientNames(chocolatList));

        List<Ingredient> newIngredients = new ArrayList<>();
        newIngredients.add(new Ingredient(0, "Fromage", 1200.0, Category.DAIRY, null));
        newIngredients.add(new Ingredient(0, "Oignon", 500.0, Category.VEGETABLE, null));
        List<Ingredient> createdIngredients = dataRetriever.createIngredients(newIngredients);
        System.out.println("7i: " + toIngredientNames(createdIngredients));

        List<Ingredient> newIngredientsWithExisting = new ArrayList<>();
        newIngredientsWithExisting.add(new Ingredient(0, "Carotte", 2000.0, Category.VEGETABLE, null));
        newIngredientsWithExisting.add(new Ingredient(0, "Laitue", 2000.0, Category.VEGETABLE, null));
        try {
            dataRetriever.createIngredients(newIngredientsWithExisting);
            System.out.println("7j: no exception");
        } catch (RuntimeException e) {
            System.out.println("7j: exception");
        }

        Ingredient oignon = findIngredientByName("Oignon", createdIngredients);
        List<Ingredient> ingredientsForSoup = new ArrayList<>();
        ingredientsForSoup.add(oignon);
        Dish newDish = new Dish();
        newDish.setName("Soupe de légumes");
        newDish.setDishType(DishType.START);
        newDish.setIngredients(ingredientsForSoup);
        Dish savedSoup = dataRetriever.saveDish(newDish);
        System.out.println("7k: " + savedSoup.getName() + " ingredients=" + toIngredientNames(savedSoup.getIngredients()));

        Dish dishToUpdate = dataRetriever.findDishById(1);
        Ingredient fromage = findIngredientByName("Fromage", createdIngredients);
        List<Ingredient> updatedIngredients = new ArrayList<>();
        updatedIngredients.add(oignon);
        int index = 0;
        List<Ingredient> oldIngredients = dishToUpdate.getIngredients();
        while (index < oldIngredients.size()) {
            updatedIngredients.add(oldIngredients.get(index));
            index = index + 1;
        }
        updatedIngredients.add(fromage);
        dishToUpdate.setIngredients(updatedIngredients);
        Dish updatedDish = dataRetriever.saveDish(dishToUpdate);
        System.out.println("7l: " + updatedDish.getName() + " ingredients=" + toIngredientNames(updatedDish.getIngredients()));

        Dish dishToUpdateAgain = dataRetriever.findDishById(1);
        List<Ingredient> onlyFromage = new ArrayList<>();
        onlyFromage.add(fromage);
        dishToUpdateAgain.setIngredients(onlyFromage);
        Dish updatedDishAgain = dataRetriever.saveDish(dishToUpdateAgain);
        System.out.println("7m: " + updatedDishAgain.getName() + " ingredients=" + toIngredientNames(updatedDishAgain.getIngredients()));
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
