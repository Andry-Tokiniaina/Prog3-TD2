import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Order {
    private Integer id;
    private String reference;
    private Instant creationDatetime;
    private List<DishOrder> dishOrders;
    private TableOrder tableOrder;

    public Order() {
    }

    public Order(int id, String reference, Instant creationDatetime) {
        this.id = id;
        this.reference = reference;
        this.creationDatetime = creationDatetime;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTableOrder(TableOrder tableOrder) {
        this.tableOrder = tableOrder;
    }

    public TableOrder getTableOrder() {
        return tableOrder;
    }

    public Integer getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public List<DishOrder> getDishOrders() {
        return dishOrders;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    public void setDishOrders(List<DishOrder> dishOrders) {
        this.dishOrders = dishOrders;
    }

    private Double getTotalAmountWithoutVAT(){
        return dishOrders.stream()
                .map(dishOrder -> dishOrder.getDish().getGrossMargin())
                .reduce(0.0, Double::sum);
    }
    private Double getTotalAmountWithVAT(){
        throw  new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Order order)) return false;
        return getId() == order.getId() && Objects.equals(getReference(), order.getReference()) && Objects.equals(getCreationDatetime(), order.getCreationDatetime()) && Objects.equals(getDishOrders(), order.getDishOrders());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getReference(), getCreationDatetime(), getDishOrders());
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", creationDatetime=" + creationDatetime +
                ", dishOrders=" + dishOrders +
                '}';
    }
}
