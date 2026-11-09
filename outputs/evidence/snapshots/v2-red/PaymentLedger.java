import java.util.UUID;
// Deliberately buggy, compilable starting fixture. Not a claimed TDD implementation.
public class PaymentLedger {
    private int executions;
    public String submit(String key, long amount, boolean failBeforeCommit) {
        if (key == null || key.isBlank() || amount <= 0) throw new IllegalArgumentException();
        if (failBeforeCommit) throw new IllegalStateException("provider unavailable");
        executions++;
        return UUID.randomUUID().toString();
    }
    public int executions() { return executions; }
}
