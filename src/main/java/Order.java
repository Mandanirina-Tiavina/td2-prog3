import java.time.Instant;
import java.util.List;

public class Order {
    private Integer id;
    private String reference;
    private Instant creationDatetime;
    private List<DishOrder> dishOrders;

    public Double getTotalAmountWithoutVAT() {
        double totalPrice = 0;
        if (dishOrders == null) {
            return totalPrice;
        }
        for (int i = 0; i < dishOrders.size(); i++) {
            totalPrice = totalPrice + dishOrders.get(i).getDishTotalAmountWithoutVAT();
        }
        return totalPrice;
    }

    public Double getTotalAmountWithVAT() {
        double totalPrice = 0;
        if (dishOrders == null) {
            return totalPrice;
        }
        for (int i = 0; i < dishOrders.size(); i++) {
            totalPrice = totalPrice + dishOrders.get(i).getDishTotalAmountWithVAT();
        }
        return totalPrice;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    public List<DishOrder> getDishOrders() {
        return dishOrders;
    }

    public void setDishOrders(List<DishOrder> dishOrders) {
        this.dishOrders = dishOrders;
    }
}
