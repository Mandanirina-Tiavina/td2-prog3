import java.time.Instant;

public class Table {
    private Integer id;
    private Integer number;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public boolean isAvailableAt(Instant at) {
        if (id == null || at == null) {
            return false;
        }
        return new DataRetriever().isTableAvailableAt(id, at);
    }
}
