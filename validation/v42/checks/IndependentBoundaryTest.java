import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** Independent behavior oracles frozen in B_SPEC_FIRST_REVIEW.md, B01-B26. */
public final class IndependentBoundaryTest {
    private static int cases;
    private static int failures;

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void same(Object expected, Object actual, String message) {
        check(Objects.equals(expected, actual), message + ": expected=" + expected + ", actual=" + actual);
    }

    private static void delta(PaymentLedger ledger, int baseline, int expected) {
        same(expected, ledger.executions() - baseline, "execution delta");
    }

    private static void rejects(Class<? extends Throwable> expected, Runnable request) {
        try {
            request.run();
        } catch (Throwable actual) {
            check(expected.isInstance(actual), "expected " + expected.getName() + ", got " + actual);
            return;
        }
        throw new AssertionError("expected " + expected.getName() + " but request returned");
    }

    private static String commit(PaymentLedger ledger, String key, long amount) {
        int baseline = ledger.executions();
        String receipt = ledger.submit(key, amount, false);
        check(receipt != null, "missing receipt for " + key);
        delta(ledger, baseline, 1);
        return receipt;
    }

    private static void replay(PaymentLedger ledger, String key, long amount, boolean fault, String receipt) {
        int baseline = ledger.executions();
        same(receipt, ledger.submit(key, amount, fault), "replay receipt for " + key);
        delta(ledger, baseline, 0);
    }

    private static void rejected(PaymentLedger ledger, Class<? extends Throwable> type,
                                 String key, long amount, boolean fault) {
        int baseline = ledger.executions();
        rejects(type, () -> ledger.submit(key, amount, fault));
        delta(ledger, baseline, 0);
    }

    private static void test(String name, Runnable body) {
        cases++;
        try {
            body.run();
            System.out.println(Instant.now() + " PASS " + name);
        } catch (Throwable error) {
            failures++;
            System.out.println(Instant.now() + " FAIL " + name + ": " + error);
            error.printStackTrace(System.out);
        }
    }

    public static void main(String[] args) {
        test("B01 first legal request", () -> {
            commit(new PaymentLedger(), "order-1", 100);
        });
        test("B02 repeated exact replay", () -> {
            var ledger = new PaymentLedger();
            int baseline = ledger.executions();
            String receipt = commit(ledger, "order-1", 100);
            for (int i = 0; i < 50; i++) replay(ledger, "order-1", 100, false, receipt);
            delta(ledger, baseline, 1);
        });
        test("B03 greater conflicting amount preserves receipt", () -> {
            var ledger = new PaymentLedger();
            String receipt = commit(ledger, "order-1", 100);
            rejected(ledger, IllegalArgumentException.class, "order-1", 101, false);
            replay(ledger, "order-1", 100, false, receipt);
        });
        test("B04 lower conflicting amount preserves receipt", () -> {
            var ledger = new PaymentLedger();
            String receipt = commit(ledger, "order-1", 100);
            rejected(ledger, IllegalArgumentException.class, "order-1", 99, false);
            replay(ledger, "order-1", 100, false, receipt);
        });
        test("B05 invalid key variants", () -> {
            for (String key : new String[] {null, "", " ", "\t\r\n"}) {
                rejected(new PaymentLedger(), IllegalArgumentException.class, key, 1, false);
            }
        });
        test("B06 nonpositive amount variants", () -> {
            for (long amount : new long[] {0, -1, Long.MIN_VALUE}) {
                rejected(new PaymentLedger(), IllegalArgumentException.class, "amount-check", amount, false);
            }
        });
        test("B07 positive long endpoints", () -> {
            var ledger = new PaymentLedger();
            String low = commit(ledger, "min-positive", 1);
            String high = commit(ledger, "max-positive", Long.MAX_VALUE);
            check(!low.equals(high), "distinct keys share a receipt");
            replay(ledger, "min-positive", 1, false, low);
            replay(ledger, "max-positive", Long.MAX_VALUE, false, high);
        });
        test("B08 adjacent amounts above integer maximum", () -> {
            var ledger = new PaymentLedger();
            String receipt = commit(ledger, "large", 2147483648L);
            rejected(ledger, IllegalArgumentException.class, "large", 2147483649L, false);
            replay(ledger, "large", 2147483648L, false, receipt);
        });
        test("B09 long amounts with identical low 32 bits", () -> {
            var ledger = new PaymentLedger();
            String receipt = commit(ledger, "wide", 1);
            rejected(ledger, IllegalArgumentException.class, "wide", 4294967297L, false);
            replay(ledger, "wide", 1, false, receipt);
        });
        test("B10 failure then successful retry", () -> {
            var ledger = new PaymentLedger();
            rejected(ledger, IllegalStateException.class, "retry", 100, true);
            String receipt = commit(ledger, "retry", 100);
            replay(ledger, "retry", 100, false, receipt);
        });
        test("B11 repeated provider failures then retry", () -> {
            var ledger = new PaymentLedger();
            int baseline = ledger.executions();
            for (int i = 0; i < 20; i++) rejected(ledger, IllegalStateException.class, "retry-many", 100, true);
            String receipt = commit(ledger, "retry-many", 100);
            replay(ledger, "retry-many", 100, false, receipt);
            delta(ledger, baseline, 1);
        });
        test("B12 failed attempt does not bind amount", () -> {
            var ledger = new PaymentLedger();
            rejected(ledger, IllegalStateException.class, "unbound", 100, true);
            String receipt = commit(ledger, "unbound", 200);
            replay(ledger, "unbound", 200, false, receipt);
            rejected(ledger, IllegalArgumentException.class, "unbound", 100, false);
            replay(ledger, "unbound", 200, false, receipt);
        });
        test("B13 committed replay bypasses provider fault", () -> {
            var ledger = new PaymentLedger();
            String receipt = commit(ledger, "replay-fault", 100);
            for (int i = 0; i < 10; i++) replay(ledger, "replay-fault", 100, true, receipt);
        });
        test("B14 conflict remains invalid under provider fault", () -> {
            var ledger = new PaymentLedger();
            String receipt = commit(ledger, "conflict-fault", 100);
            rejected(ledger, IllegalArgumentException.class, "conflict-fault", 101, true);
            replay(ledger, "conflict-fault", 100, false, receipt);
        });
        test("B15 validation takes precedence over provider fault", () -> {
            for (String key : new String[] {null, "", " ", "\t\r\n"}) {
                rejected(new PaymentLedger(), IllegalArgumentException.class, key, 1, true);
            }
            for (long amount : new long[] {0, -1, Long.MIN_VALUE}) {
                rejected(new PaymentLedger(), IllegalArgumentException.class, "amount-check", amount, true);
            }
        });
        test("B16 committed key still rejects invalid amounts", () -> {
            var ledger = new PaymentLedger();
            String receipt = commit(ledger, "existing", 100);
            for (boolean fault : new boolean[] {false, true}) {
                for (long amount : new long[] {0, -1}) {
                    rejected(ledger, IllegalArgumentException.class, "existing", amount, fault);
                    replay(ledger, "existing", 100, false, receipt);
                }
            }
        });
        test("B17 key case is significant", () -> distinctKeys("K", "k"));
        test("B18 nonblank key padding is significant", () -> distinctKeys("x", " x", "x "));
        test("B19 distinct String objects have value identity", () -> {
            var ledger = new PaymentLedger();
            String key1 = new String(new char[] {'s', 'a', 'm', 'e', '-', 'v', 'a', 'l', 'u', 'e'});
            String key2 = new String(key1.toCharArray());
            check(key1 != key2 && key1.equals(key2), "fixture must use equal distinct objects");
            String receipt = commit(ledger, key1, 100);
            replay(ledger, key2, 100, false, receipt);
        });
        test("B20 hash collisions retain distinct keys", () -> {
            same("Aa".hashCode(), "BB".hashCode(), "fixture hash collision");
            distinctKeys("Aa", "BB");
        });
        test("B21 Unicode normalization is not key identity", () -> distinctKeys("\u00E9", "e\u0301"));
        test("B22 numeric-looking identifiers stay exact", () -> distinctKeys("001", "1"));
        test("B23 mixed-key failure conflict replay sequence", () -> {
            var ledger = new PaymentLedger();
            int baseline = ledger.executions();
            String a = commit(ledger, "A", 10); delta(ledger, baseline, 1);
            rejected(ledger, IllegalStateException.class, "B", 20, true); delta(ledger, baseline, 1);
            replay(ledger, "A", 10, true, a); delta(ledger, baseline, 1);
            rejected(ledger, IllegalArgumentException.class, "A", 11, false); delta(ledger, baseline, 1);
            String b = commit(ledger, "B", 20); delta(ledger, baseline, 2);
            check(!a.equals(b), "A and B share receipt");
            replay(ledger, "A", 10, false, a); delta(ledger, baseline, 2);
            replay(ledger, "B", 20, false, b); delta(ledger, baseline, 2);
        });
        test("B24 receipt uniqueness is independent of amount", () -> {
            var ledger = new PaymentLedger();
            int baseline = ledger.executions();
            Set<String> receipts = new HashSet<>();
            String a = commit(ledger, "same-amount-A", 7); receipts.add(a);
            replay(ledger, "same-amount-A", 7, false, a);
            String b = commit(ledger, "same-amount-B", 7); receipts.add(b);
            replay(ledger, "same-amount-A", 7, true, a);
            String c = commit(ledger, "different-amount", 8); receipts.add(c);
            replay(ledger, "same-amount-B", 7, true, b);
            replay(ledger, "different-amount", 8, false, c);
            same(3, receipts.size(), "pairwise unique committed receipts");
            delta(ledger, baseline, 3);
        });
        test("B25 invalid first attempt does not poison key", () -> {
            var ledger = new PaymentLedger();
            rejected(ledger, IllegalArgumentException.class, "repairable", 0, false);
            String receipt = commit(ledger, "repairable", 1);
            replay(ledger, "repairable", 1, false, receipt);
        });
        test("B26 adjacent long-maximum conflict preserves receipt", () -> {
            var ledger = new PaymentLedger();
            String receipt = commit(ledger, "max", Long.MAX_VALUE);
            rejected(ledger, IllegalArgumentException.class, "max", Long.MAX_VALUE - 1, false);
            replay(ledger, "max", Long.MAX_VALUE, false, receipt);
        });
        System.out.println(Instant.now() + " CASES=" + cases + " FAILURES=" + failures);
        if (failures != 0) throw new AssertionError("Independent boundary cases failed: " + failures);
    }

    private static void distinctKeys(String... keys) {
        var ledger = new PaymentLedger();
        int baseline = ledger.executions();
        Set<String> receipts = new HashSet<>();
        for (String key : keys) {
            String receipt = commit(ledger, key, 100);
            check(receipts.add(receipt), "distinct key reused a receipt: " + key);
            replay(ledger, key, 100, false, receipt);
        }
        delta(ledger, baseline, keys.length);
    }
}
