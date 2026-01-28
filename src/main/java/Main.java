import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();

        // Salade fraîche (id = 1)
        Dish salade = dataRetriever.findDishById(1);
        System.out.println("Salade fraîche cost=" + salade.getDishCost());
        System.out.println("Salade fraîche margin=" + salade.getGrossMargin());

        // Poulet grillé (id = 2)
        Dish poulet = dataRetriever.findDishById(2);
        System.out.println("Poulet grillé cost=" + poulet.getDishCost());
        System.out.println("Poulet grillé margin=" + poulet.getGrossMargin());

        // Riz aux légumes (id = 3) : price NULL -> exception pour la marge
        Dish riz = dataRetriever.findDishById(3);
        System.out.println("Riz aux légumes cost=" + riz.getDishCost());
        try {
            System.out.println("Riz aux légumes margin=" + riz.getGrossMargin());
        } catch (RuntimeException e) {
            System.out.println("Riz aux légumes margin=ERREUR: " + e.getMessage());
        }

        // Gâteau au chocolat (id = 4)
        Dish gateau = dataRetriever.findDishById(4);
        System.out.println("Gâteau au chocolat cost=" + gateau.getDishCost());
        System.out.println("Gâteau au chocolat margin=" + gateau.getGrossMargin());

        // Salade de fruits (id = 5) : price NULL -> exception pour la marge
        Dish saladeFruits = dataRetriever.findDishById(5);
        System.out.println("Salade de fruits cost=" + saladeFruits.getDishCost());
        try {
            System.out.println("Salade de fruits margin=" + saladeFruits.getGrossMargin());
        } catch (RuntimeException e) {
            System.out.println("Salade de fruits margin=ERREUR: " + e.getMessage());
        }

        Instant t = Instant.parse("2024-01-06T12:00:00Z");

        Ingredient laitue = dataRetriever.findIngredientById(1);
        StockValue laitueStock = laitue.getStockValueAt(t);
        System.out.println("Laitue stock=" + laitueStock.getQuantity() + " " + laitueStock.getUnit());

        Ingredient tomate = dataRetriever.findIngredientById(2);
        StockValue tomateStock = tomate.getStockValueAt(t);
        System.out.println("Tomate stock=" + tomateStock.getQuantity() + " " + tomateStock.getUnit());

        Ingredient pouletIng = dataRetriever.findIngredientById(3);
        StockValue pouletStock = pouletIng.getStockValueAt(t);
        System.out.println("Poulet stock=" + pouletStock.getQuantity() + " " + pouletStock.getUnit());

        Ingredient chocolat = dataRetriever.findIngredientById(4);
        StockValue chocolatStock = chocolat.getStockValueAt(t);
        System.out.println("Chocolat stock=" + chocolatStock.getQuantity() + " " + chocolatStock.getUnit());

        Ingredient beurre = dataRetriever.findIngredientById(5);
        StockValue beurreStock = beurre.getStockValueAt(t);
        System.out.println("Beurre stock=" + beurreStock.getQuantity() + " " + beurreStock.getUnit());

        Order order = new Order();

        List<DishOrder> dishOrders = new ArrayList<>();

        DishOrder line1 = new DishOrder();
        Dish dish1 = dataRetriever.findDishById(1);
        line1.setDish(dish1);
        line1.setQuantity(2);
        dishOrders.add(line1);

        DishOrder line2 = new DishOrder();
        Dish dish2 = dataRetriever.findDishById(2);
        line2.setDish(dish2);
        line2.setQuantity(1);
        dishOrders.add(line2);

        order.setDishOrders(dishOrders);

        Order savedOrder = new DataRetriever().saveOrder(order);
        System.out.println("Order reference=" + savedOrder.getReference());
        System.out.println("Order total HT=" + savedOrder.getTotalAmountWithoutVAT());
        System.out.println("Order total TTC=" + savedOrder.getTotalAmountWithVAT());

        Order loadedOrder = new DataRetriever().findOrderByReference(savedOrder.getReference());
        System.out.println("Loaded order reference=" + loadedOrder.getReference());
        System.out.println("Loaded order total HT=" + loadedOrder.getTotalAmountWithoutVAT());
        System.out.println("Loaded order total TTC=" + loadedOrder.getTotalAmountWithVAT());
    }
}
