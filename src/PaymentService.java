import java.util.*;
public class PaymentService {
    public String create(String orderId, long amountFen) {
        if (amountFen <= 0) throw new IllegalArgumentException("amount must be positive");
        return UUID.randomUUID().toString();
    }
}
