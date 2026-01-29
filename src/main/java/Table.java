import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Table {
    private Integer id;
    private Integer number;
    private List<Order> orders;

    public Table() {
    }

    public Table(Integer id, Integer number) {
        this.id = id;
        this.number = number;
    }

    public Integer getId() {
        return id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public boolean isAvailableAt(Instant instant) {
        if (orders == null || orders.isEmpty()) {
            return true;
        }

        return orders.stream().noneMatch(order -> {
            Instant arrive = order.getTableOrder().getClient_arrive_time();
            Instant leave  = order.getTableOrder().getClient_leave_time();

            if (arrive == null) {
                return false;
            }

            boolean hasNotLeft = leave == null || instant.isBefore(leave);
            boolean hasArrived = !instant.isBefore(arrive);

            return hasArrived && hasNotLeft;
        });
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Table table)) return false;
        return Objects.equals(getId(), table.getId()) && Objects.equals(getNumber(), table.getNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getNumber());
    }

    @Override
    public String toString() {
        return "Table{" +
                "id=" + id +
                ", number=" + number +
                '}';
    }
}
