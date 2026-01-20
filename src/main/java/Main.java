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
    }
}
