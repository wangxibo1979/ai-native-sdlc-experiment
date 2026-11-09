import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class PaymentLedger {
    private final Map<String, Long> amounts = new HashMap<>();
    private final Map<String, String> ids = new HashMap<>();
    private int executions;
    public String submit(String key, long amount, boolean failBeforeCommit) {
        if (key == null || key.isBlank() || amount <= 0) throw new IllegalArgumentException();
        if (ids.containsKey(key)) {
            if (amounts.get(key) != amount) throw new IllegalArgumentException("conflicting parameters");
            return ids.get(key);
        }
        if (failBeforeCommit) throw new IllegalStateException("provider unavailable");
        executions++;
        String id = UUID.randomUUID().toString();
        amounts.put(key, amount); ids.put(key, id);
        return id;
    }
    public int executions() { return executions; }
}
