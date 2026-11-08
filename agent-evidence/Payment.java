import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Payment {
    private record Entry(long amount, String id) {}
    private final Map<String, Entry> orders = new HashMap<>();

    public synchronized String submit(String orderId, long amount) {
        Entry existing = orders.get(orderId);
        if (existing != null) {
            if (existing.amount() != amount) {
                throw new IllegalArgumentException("Order already has a different amount");
            }
            return existing.id();
        }
        String id = UUID.randomUUID().toString();
        orders.put(orderId, new Entry(amount, id));
        return id;
    }
}
