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

        List<Table> freeAtT = dataRetriever.findAvailableTablesAt(t);
        String s1 = "Available tables at t=" + t + ": ";
        int i = 0;
        while (i < freeAtT.size()) {
            if (i > 0) s1 = s1 + ", ";
            s1 = s1 + freeAtT.get(i).getNumber();
            i = i + 1;
        }
        System.out.println(s1);
        
        Table selectedTable = null;
        int k = 0;
        while (k < freeAtT.size()) {
            if (freeAtT.get(k).getNumber() == 1) {
                selectedTable = freeAtT.get(k);
                break;
            }
            k = k + 1;
        }
        if (selectedTable == null && freeAtT.size() > 0) {
            selectedTable = freeAtT.get(0);
        }
        if (selectedTable == null) {
            System.out.println("No available table at t, skipping table tests.");
            return;
        }

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
        order.setCreationDatetime(t);

        order.setTable(selectedTable);

        Order savedOrder = new DataRetriever().saveOrder(order);
        System.out.println("Order reference=" + savedOrder.getReference());
        System.out.println("Order total HT=" + savedOrder.getTotalAmountWithoutVAT());
        System.out.println("Order total TTC=" + savedOrder.getTotalAmountWithVAT());

        List<Table> freeAfterSave = dataRetriever.findAvailableTablesAt(t);
        String s2 = "Available tables at t after save: ";
        int j = 0;
        while (j < freeAfterSave.size()) {
            if (j > 0) s2 = s2 + ", ";
            s2 = s2 + freeAfterSave.get(j).getNumber();
            j = j + 1;
        }
        System.out.println(s2);

        List<Table> alternatives = dataRetriever.findAvailableTablesAt(t);
        if (!alternatives.isEmpty()) {
            Table alt = alternatives.get(0);
            Order order2 = new Order();
            order2.setCreationDatetime(t);
            order2.setTable(alt);
            order2.setDishOrders(new ArrayList<>());
            Order savedOrder2 = new DataRetriever().saveOrder(order2);
            System.out.println("Second order on alternative table (" + alt.getNumber() + ") reference=" + savedOrder2.getReference());
        } else {
            System.out.println("No alternative table available at t");
        }

        Order loadedOrder = new DataRetriever().findOrderByReference(savedOrder.getReference());
        System.out.println("Loaded order reference=" + loadedOrder.getReference());
        System.out.println("Loaded order total HT=" + loadedOrder.getTotalAmountWithoutVAT());
        System.out.println("Loaded order total TTC=" + loadedOrder.getTotalAmountWithVAT());
    }
}
