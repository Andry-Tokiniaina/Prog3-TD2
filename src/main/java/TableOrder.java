import java.time.Instant;

public class TableOrder {
    private Table table;
    private Instant client_arrive_time;
    private Instant client_leave_time;

    public TableOrder(Table table, Instant client_arrive_time, Instant client_leave_time) {
        this.table = table;
        this.client_arrive_time = client_arrive_time;
        this.client_leave_time = client_leave_time;
    }

    public TableOrder() {
    }

    public Table getTable() {
        return table;
    }

    public Instant getClient_arrive_time() {
        return client_arrive_time;
    }

    public Instant getClient_leave_time() {
        return client_leave_time;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public void setClient_arrive_time(Instant client_arrive_time) {
        this.client_arrive_time = client_arrive_time;
    }

    public void setClient_leave_time(Instant client_leave_time) {
        this.client_leave_time = client_leave_time;
    }
}
