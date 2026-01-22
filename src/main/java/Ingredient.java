import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Ingredient {
    private Integer id;
    private String name;
    private CategoryEnum category;
    private Double price;
    private Dish dish;
    private Double quantity;
    private List<StockMovement> stockMovementList;

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public List<StockMovement> getStockMovementList() {
        return stockMovementList;
    }

    public void setStockMovementList(List<StockMovement> stockMovementList) {
        this.stockMovementList = stockMovementList;
    }

    public Ingredient() {
    }

    public Ingredient(Integer id) {
        this.id = id;
    }

    public Ingredient(Integer id, String name, CategoryEnum category, Double price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
    }

    public String getDishName() {
        return dish == null ? null : dish.getName();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public StockValue getStockValueAt(Instant t) {
        StockValue result = new StockValue();
        double quantityAtT = 0.0;
        Unit unit = null;

        if (stockMovementList != null) {
            for (int i = 0; i < stockMovementList.size(); i++) {
                StockMovement movement = stockMovementList.get(i);
                if (movement.getCreationDatetime() == null) {
                    continue;
                }
                if (movement.getCreationDatetime().isAfter(t)) {
                    continue;
                }

                StockValue value = movement.getValue();
                if (value == null) {
                    continue;
                }

                if (unit == null) {
                    unit = value.getUnit();
                }

                double q = value.getQuantity();
                if (movement.getType() == MovementTypeEnum.IN) {
                    quantityAtT = quantityAtT + q;
                } else if (movement.getType() == MovementTypeEnum.OUT) {
                    quantityAtT = quantityAtT - q;
                }
            }
        }

        if (unit == null) {
            unit = Unit.KG;
        }

        result.setQuantity(quantityAtT);
        result.setUnit(unit);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && category == that.category && Objects.equals(price, that.price) && Objects.equals(dish, that.dish);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, category, price, dish);
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", price=" + price +
                ", dishName=" + getDishName() +
                ", quantity=" + quantity +
                '}';
    }
}
