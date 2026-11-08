import java.util.*;
public class PaymentService {
    private record Payment(long amount, String id) {}
    private final Map<String, Payment> payments = new HashMap<>();
    public String create(String orderId, long amountFen) {
        if (amountFen <= 0) throw new IllegalArgumentException("amount must be positive");
        Payment existing = payments.get(orderId);
        if (existing != null) {
            if (existing.amount() != amountFen) throw new IllegalArgumentException("conflicting amount");
            return existing.id();
        }
        Payment created = new Payment(amountFen, UUID.randomUUID().toString());
        payments.put(orderId, created);
        return created.id();
    }
}
