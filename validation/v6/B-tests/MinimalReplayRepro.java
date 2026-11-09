public final class MinimalReplayRepro {
    public static void main(String[] args) {
        PaymentLedger ledger = new PaymentLedger();
        int baseline = ledger.executions();
        long amount = 2147483648L;
        String receipt = ledger.submit("large", amount, false);
        System.out.println("INPUT_1 key=large amount=" + amount + " failBeforeCommit=false");
        System.out.println("OBSERVED_1 receipt=" + receipt + " executionDelta=" + (ledger.executions() - baseline));
        System.out.println("INPUT_2 key=large amount=" + amount + " failBeforeCommit=false");
        System.out.println("EXPECTED_2 receipt=" + receipt + " executionDelta=1 no exception");
        try {
            String replay = ledger.submit("large", amount, false);
            System.out.println("OBSERVED_2 receipt=" + replay + " executionDelta=" + (ledger.executions() - baseline));
            if (!receipt.equals(replay) || ledger.executions() - baseline != 1) {
                throw new AssertionError("Replay changed result or execution count");
            }
        } catch (RuntimeException actual) {
            System.out.println("OBSERVED_2 exception=" + actual + " executionDelta=" + (ledger.executions() - baseline));
            throw new AssertionError("Legal identical replay must return original receipt", actual);
        }
    }
}
