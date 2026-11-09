import java.util.HashMap;
import java.util.Map;
import java.math.BigInteger;
public class PaymentLedger {
    private record Receipt(long amount, String id) {}
    private final Map<String, Receipt> receipts = new HashMap<>();
    private int executions;
    private BigInteger receiptSequence = BigInteger.ZERO;
    public String submit(String key, long amount, boolean failBeforeCommit) {
        if (key == null || key.isBlank() || amount <= 0) throw new IllegalArgumentException();
        Receipt previous = receipts.get(key);
        if (previous != null) {
            if (previous.amount() != amount) throw new IllegalArgumentException("conflicting parameters");
            return previous.id();
        }
        if (failBeforeCommit) throw new IllegalStateException("provider unavailable");
        executions++;
        receiptSequence = receiptSequence.add(BigInteger.ONE);
        Receipt created = new Receipt(amount, receiptSequence.toString());
        receipts.put(key, created);
        return created.id();
    }
    public int executions() { return executions; }
}
