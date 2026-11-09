import java.security.Provider;
import java.security.Security;
import java.security.SecureRandomSpi;
import java.util.Arrays;

/** Isolated random-source control, not a claim about a naturally observed collision. */
public final class CollisionProbe {
    public static final class RepeatedBytes extends SecureRandomSpi {
        protected void engineSetSeed(byte[] seed) {}
        protected void engineNextBytes(byte[] bytes) { Arrays.fill(bytes, (byte) 0); }
        protected byte[] engineGenerateSeed(int size) { return new byte[size]; }
    }
    public static final class ProbeProvider extends Provider {
        public ProbeProvider() {
            super("AgentBCollisionProbe", "1.0", "Isolated repeated UUID random bytes");
            put("SecureRandom.RepeatedBytes", RepeatedBytes.class.getName());
        }
    }
    public static void main(String[] args) {
        Security.insertProviderAt(new ProbeProvider(), 1);
        var ledger = new PaymentLedger();
        String a = ledger.submit("distinct-A", 7, false);
        String b = ledger.submit("distinct-B", 7, false);
        System.out.println("JAVA=" + System.getProperty("java.version"));
        System.out.println("CONTROL=first SecureRandom provider emits repeated zero bytes; UUID implementation unchanged");
        System.out.println("A=" + a + " B=" + b + " EXECUTIONS=" + ledger.executions());
        System.out.println("T20_DISTINCT=" + !a.equals(b));
        if (a.equals(b)) System.exit(1);
    }
}
