# v4.2 closure map correction

The previous interpretation of blocking NO as "no current coverage gap" was incorrect. Coverage and requiredness are independent. All twenty confirmed business targets are mandatory; both current profiles now retain YES for every row, regardless of COVERED, PARTIAL, GAP or a passing execution. There are no authorized NO exceptions. Initial maps and the previous report remain unchanged in before/ and Git history.

The independent spec-first policy is INDEPENDENT_ACCEPTANCE_POLICY.md. POLICY regression checks enforce the reviewed target set and both maps; verify.py independently rejects any downgraded row. A complete map with blocking PARTIAL/GAP remains MAP READY but cannot qualify; a policy-inconsistent map is MAP NOT READY.

Agent B's first simulated review is REQUEST_CHANGES. T20 was previously overstated as AUTO/COVERED: randomly generated UUIDs without collision handling are not a guarantee of distinct receipts. The original controlled-random-source probe, old implementation output and exit 1 remain under agent-b-evidence/. review1-map.json records T20 as PARTIAL before repair. This is a reproducible controlled counterexample, not a claim that a random collision occurred naturally.

The repaired ledger uses an arbitrary-precision BigInteger sequence scoped to the ledger, incremented only on the legal new-commit path. It does not truncate or wrap the receipt sequence. The confirmed specification requires no UUID format, randomness or cross-instance uniqueness. Replays retain the stored receipt; invalid input and provider faults precede assignment. This does not introduce a new external business rule.

T20 is now COMBINED: explicit independent-derived repeated-random-source check C plus the monotonic assignment and replay control flow. It is a required full-profile check. The old 26-case B suite and original A suite remain unchanged. The original probe is preserved; checks/CollisionProbe.java only adds a real one-case/failure summary for the existing actual comparison.

For the amended PaymentLedger.java, blank/amount validation is L10; the provider fault throws at L16; operations increment at L17; the receipt sequence increments at L18; receipt creation is L19; the sole map write is L20. These positions replace historical source-line commentary for T09/T10/T15; test-file line links remain unchanged. This is static control-flow support within the single-threaded, single-process model, not a proof of durable or distributed behavior.

The cumulative executions counter remains the pre-existing int observation API. Counter overflow/resource exhaustion policies were not confirmed and are not silently resolved by this receipt fix; they remain a declared boundary for real project acceptance. Neither finite testing nor the local mechanism claims unlimited physical resources or all-domain formal verification.

The final closure report and independent re-review determine whether the amended evidence suffices; this correction note is not an approval or a second task plan.
