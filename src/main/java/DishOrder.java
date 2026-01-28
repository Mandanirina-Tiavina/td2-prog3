public class DishOrder {
    private Integer id;
    private Dish dish;
    private Integer quantity;

    public Double getDishTotalAmountWithoutVAT() {
        if (dish == null || dish.getPrice() == null || quantity == null) {
            return 0.0;
        }
        return dish.getPrice() * quantity;
    }

    public Double getDishTotalAmountWithVAT() {
        double vatRate = 0.2;
        return getDishTotalAmountWithoutVAT() * (1 + vatRate);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
