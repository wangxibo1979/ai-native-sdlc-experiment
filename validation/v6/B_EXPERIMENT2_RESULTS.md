# B independent blinded candidate review: experiment two

## Finding

**Confirmed implementation defect:** a legal positive long amount above `Integer.MAX_VALUE` is truncated when committed, causing an identical replay to throw `IllegalArgumentException` instead of returning the original receipt. This violates the fixed amount domain and replay rule, not an unresolved business assumption.

The unchanged, previously frozen B suite ran against the updated candidate **before this reviewer inspected the updated source**. Compilation exited **0**; the actual Java test subprocess exited **1**; the outer runner exited **1**. Results: **26 cases, 23 passed, 3 failed** (B07, B08, B26). This was detected by the original spec-first cases, without knowing any change location or description.

Full actual command argument arrays, timestamps, source hashes, stack traces and output are in `B_EXPERIMENT2_RESULTS.json`. Raw logs are `B-tests/experiment2-compile.log` and `B-tests/experiment2-test.log`. The compiler emitted no diagnostics.

## Expected versus observed

| Frozen case | Actual sequence relevant to failure | Fixed-rule expectation | Actual observation |
| --- | --- | --- | --- |
| B07 | Commit `min-positive` at 1 and `max-positive` at `Long.MAX_VALUE`; replay each | Both original receipts returned; no extra operations | Replay of `max-positive` throws `IllegalArgumentException: conflicting parameters` |
| B08 | Commit `large` at `2147483648L`; reject conflict at `2147483649L`; replay `2147483648L` | Conflict preserves original amount and receipt; original replay succeeds | Original-amount replay throws `IllegalArgumentException: conflicting parameters` |
| B26 | Commit `max` at `Long.MAX_VALUE`; reject `Long.MAX_VALUE - 1`; replay max | Original receipt returned with no extra execution | Original-amount replay throws `IllegalArgumentException: conflicting parameters` |

The suite's replay helper throws before its post-call count assertion in these failing cases. The separate minimal reproducer below directly confirms the observed count after the exception; count preservation must not be inferred solely from a failed replay assertion.

## Executed minimal reproducer

The minimal behavior sequence is two identical requests on one new ledger:

```java
PaymentLedger ledger = new PaymentLedger();
String receipt = ledger.submit("large", 2147483648L, false);
String replay = ledger.submit("large", 2147483648L, false);
// Required: replay equals receipt; total execution delta remains 1.
```

This amount is the smallest positive long above `Integer.MAX_VALUE`. `B-tests/MinimalReplayRepro.java` executes those two calls and prints the actual result and count. It was added after the frozen suite failure solely to reduce and confirm the already-detected defect; the frozen B test source was not changed.

Actual output:

```text
INPUT_1 key=large amount=2147483648 failBeforeCommit=false
OBSERVED_1 receipt=7e083099-21a2-4f5d-b009-639ad2763fe1 executionDelta=1
INPUT_2 key=large amount=2147483648 failBeforeCommit=false
EXPECTED_2 receipt=7e083099-21a2-4f5d-b009-639ad2763fe1 executionDelta=1 no exception
OBSERVED_2 exception=java.lang.IllegalArgumentException: conflicting parameters executionDelta=1
```

The reproducer then raises an assertion for the incorrect exception. Actual reproducer compilation exit: **0**; actual reproducer process exit: **1**. Its full commands, source SHA-256, candidate hash before/after and outputs are appended under JSON `minimalReproducer`; raw logs are `B-tests/experiment2-minimal-compile.log` and `B-tests/experiment2-minimal-test.log`.

## Source-level explanation and original A coverage

Only after the frozen suite reported failures, the permitted current candidate source was read. At `candidate/PaymentLedger.java:17`, it creates the stored receipt with:

```java
Receipt created = new Receipt((int) amount, UUID.randomUUID().toString());
```

The explicit cast loses high bits before the value is widened into the record's long component. For the executed reproducer, `2147483648L` becomes `-2147483648L` in the stored amount. The comparison at line 12 therefore rejects the original legal long amount on replay. There is no confirmed business upper bound authorizing this narrowing.

The original A test source has no coverage of this boundary: its legal commit/replay amounts are 100, its conflicting amount is 101, and it also tests invalid 0. It contains no legal amount above `Integer.MAX_VALUE` or at `Long.MAX_VALUE`. That is a source-verified coverage gap, not an inferred claim about A test execution. This reviewer did **not** run A tests in this phase and does not claim an actual A-suite pass. The A source hash remains identical to phase 2.

## Commands, timing and identity

Working directory: `E:\标准化投标相关文件\AI机制实验\v41-validation\validation\v6`.

Actual frozen-suite invocation:

```powershell
& 'E:\标准化投标相关文件\AI机制实验\v41-validation\validation\v6\B-tests\Run-Experiment2.ps1'
```

The runner resolves `javac` and `java` to `C:\Program Files\Common Files\Oracle\Java\javapath\javac.exe` and `java.exe`. It compiles with `--release 17 -encoding UTF-8`, supplying only the candidate and frozen B test, writing classes to `B-tests/experiment2-classes`, then runs `java -cp <that directory> IndependentBoundaryTest`. The minimal reproducer separately compiles candidate plus `MinimalReplayRepro.java` with those same options and runs `MinimalReplayRepro`. Exact absolute paths and argument arrays are retained in JSON for both runs.

| Operation | Start UTC | End UTC | Measured duration | Exit |
| --- | --- | --- | --- | --- |
| Frozen-suite compile | 2026-11-08T15:53:50.8711342Z | 2026-11-08T15:53:52.2802153Z | 1.4090811 s | 0 |
| Frozen-suite Java process | 2026-11-08T15:53:52.2812425Z | 2026-11-08T15:54:00.5894145Z | 8.3081720 s | 1 |
| Minimal compile | 2026-11-08T15:55:16.9793483Z | 2026-11-08T15:55:18.0476070Z | 1.0682587 s | 0 |
| Minimal Java process | 2026-11-08T15:55:18.0522772Z | 2026-11-08T15:55:26.3254349Z | 8.2731577 s | 1 |

These are subprocess intervals including startup/output handling, not performance assertions.

| Source | SHA-256 |
| --- | --- |
| Current candidate | `EDADD8FBB86407ECC944EA386069476CB34B134EE9C7DE30AC492B7FE1FD128D` |
| Frozen B suite | `CA9DCCC57D301DF35E6F8879BB20AE5E8613F500F2865B309653CEF8A7E1BE21` |
| Original A suite | `B7E8C89BC725E47E06DC5E7E2E82ECFF137256AF2E94FF5BA2FD1762D61E32AB` |
| Minimal reproducer | `414D51550CACAA6D6390515E1E385DEC55EBAD38B3339FAB151AC5E42C5514F1` |

Before/after SHA-256 match for all recorded frozen-suite input files, including the spec, spec-first design, candidate, A suite, frozen B suite and new experiment-two runner. The minimal run independently confirms unchanged candidate identity. The frozen B source also matches the prior experiment's known hash. No candidate, business requirement, A test, spec-first design or frozen B test was modified. All phase-2 artifacts were preserved; experiment two uses separate class/log/result paths.

No root report, evidence snapshot, controller file, mutation metadata, Git diff/history or other implementation was inspected. No new dependency or agent was used. Unspecified Unicode blank policy and other phase-1 questions remain outside this finding. The definite fixed-rule violation above is sufficient to reject this candidate; this phase stops after reporting it and does not make a broader absence-of-defects claim.
