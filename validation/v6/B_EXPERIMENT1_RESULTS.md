# B independent review: experiment one

## Measured outcome

The candidate passed the 26 executable cases B01–B26 derived from the frozen spec-first design. Actual compiler exit: **0**. Actual Java test-process exit: **0**. Actual outer successful runner exit returned by the execution tool: **0**. Test output ends with `CASES=26 FAILURES=0`.

No fixed-rule implementation defect was reproduced. This result covers the tested single-process, single-threaded synthetic ledger behaviors only. It does not settle the unresolved assumptions in the original design, prove receipt uniqueness for all future invocations, or support excluded real payment/distributed/recovery properties.

The machine-readable evidence is `B_EXPERIMENT1_RESULTS.json`, containing exact executable paths, argument arrays, before/after source SHA-256 values, UTC timestamps, exit codes and full test output. Raw output is in `B-tests/experiment1-compile.log` (empty: no compiler diagnostics) and `B-tests/experiment1-test.log`.

| Operation | UTC start | UTC end | Duration from recorded endpoints | Actual exit |
| --- | --- | --- | --- | --- |
| Compilation | 2026-11-08T15:49:35.2555421Z | 2026-11-08T15:49:36.7329442Z | 1.4774021 seconds | 0 |
| Java test subprocess | 2026-11-08T15:49:36.7349687Z | 2026-11-08T15:49:45.0355168Z | 8.3005481 seconds | 0 |

The test interval includes Java process startup and output handling. Within the JVM, the first case's completion was logged at 15:49:44.939183300Z and the summary at 15:49:44.984832400Z; their difference is not the full test duration. Recorded timestamps are measurements, not performance requirements.

## Actual commands and environment

Working directory: `E:\标准化投标相关文件\AI机制实验\v41-validation\validation\v6`.

The successful invocation was run directly in the execution tool's existing PowerShell shell:

```powershell
& 'E:\标准化投标相关文件\AI机制实验\v41-validation\validation\v6\B-tests\Run-Experiment1.ps1'
```

The JSON's `runnerCommand` field is the script's generated reproduction suggestion (`powershell -NoProfile -ExecutionPolicy Bypass -File ...`), not the actual successful shell invocation above. Exact child-process commands are represented faithfully by JSON `executable` and `arguments` fields. In expanded PowerShell form:

```powershell
& 'C:\Program Files\Common Files\Oracle\Java\javapath\javac.exe' --release 17 -encoding UTF-8 -d 'E:\标准化投标相关文件\AI机制实验\v41-validation\validation\v6\B-tests\experiment1-classes' 'E:\标准化投标相关文件\AI机制实验\v41-validation\validation\v6\candidate\PaymentLedger.java' 'E:\标准化投标相关文件\AI机制实验\v41-validation\validation\v6\B-tests\IndependentBoundaryTest.java'
& 'C:\Program Files\Common Files\Oracle\Java\javapath\java.exe' -cp 'E:\标准化投标相关文件\AI机制实验\v41-validation\validation\v6\B-tests\experiment1-classes' IndependentBoundaryTest
```

Measured versions: `javac 17.0.8`; Java `17.0.8`, Java(TM) SE Runtime Environment build `17.0.8+9-LTS-211`. Both version commands exited 0. No new dependencies were installed. Only the candidate and B test source were passed to the compiler. A tests were read for coverage comparison but were neither changed nor run by this reviewer.

## Source identity and preservation

All six observed input files had matching before/after SHA-256 in the successful run:

| File | SHA-256 |
| --- | --- |
| `CONFIRMED_SPEC.md` | `EDD5697CBB1F2FB08248906E6699D266A52A84A2D97EDD1E72CBA6E25FED9A3C` |
| `B_SPEC_FIRST_REVIEW.md` | `B942DFB86EC2D875F8EFE62FD33ED81299B0C680C1FFF9F921DC8F2C54E41BD0` |
| `candidate/PaymentLedger.java` | `8ADFF48B463E8550403BC7D7794CBED1FB14B1ABE056D8BAF984475027F1D0C0` |
| `A-tests/PaymentLedgerTest.java` | `B7E8C89BC725E47E06DC5E7E2E82ECFF137256AF2E94FF5BA2FD1762D61E32AB` |
| `B-tests/IndependentBoundaryTest.java` | `CA9DCCC57D301DF35E6F8879BB20AE5E8613F500F2865B309653CEF8A7E1BE21` |
| `B-tests/Run-Experiment1.ps1` | `C1F882C1BE40AB895BCA299BBB046700587F86CA162D02E77384B4CD41966AE2` |

The implementation, A tests, confirmed spec, and spec-first design were not edited. B tests are frozen at the above hash for later blinded review.

## Coverage and changes after code reading

All 26 original design cases have executable counterparts with the same B identifiers. They check first commit and repeated replay; greater/lower conflicts with receipt preservation; invalid keys and amounts; both positive long endpoints and long-comparison narrowing counterexamples; one/many provider failures; retry with an amount different from an uncommitted failed attempt; committed replay and conflict with fault enabled; validation precedence; exact key identity (case, padding, equal distinct String objects, hash collisions, Unicode normalization differences, numeric-looking strings); interleaved keys; receipt uniqueness independent of amount; invalid-request recovery; and a conflict adjacent to `Long.MAX_VALUE`.

Reading the permitted code exposed the actual API: `String submit(String, long, boolean)` and `int executions()`, with a public no-argument constructor. The test adapter uses that API and receipt value equality. No business oracle was relaxed or strengthened based on the implementation.

Post-reading concretizations and small extensions are explicit:

- B02 uses 50 replays; B11 uses 20 failed attempts; B13 uses 10 fault-enabled replays. These are finite samples of the original repeated-call cases, not new load/performance thresholds.
- B09 adds an original-amount replay after the narrowing-collision conflict; B12 adds another accepted-amount replay after the final conflict. These apply the original preservation invariant.
- B16 checks illegal amounts 0 and -1 under both switch values, rather than only the switch-off sequence shown in its table row. The original validation-precedence invariant already specifies these outcomes.
- B19 explicitly constructs and checks two distinct String objects with equal character content; B20 verifies the fixture's equal hash codes before using the distinct keys.
- Counter assertions use observed deltas, preserving the phase-1 decision not to invent an initial-counter API requirement. Every expected conflict/provider exception checks zero count change; subsequent replays externally check committed receipt preservation.

The A suite contains five broad cases. Its conflict case does not replay the original request afterward; it does not test committed replay with provider fault true, failed-attempt amount rebinding, long boundaries, exact key distinctions, or hash collisions. These observations are based on reading the allowed A test source, not trusting completion statements. The B suite supplies these independent checks without changing A.

## Defects versus unconfirmed assumptions

No reproduced defect was found in the fixed requirements. The implementation uses `String.isBlank()` for validation; the exact Unicode blank policy remains an unresolved specification choice. U+2003/U+00A0 acceptance was not promoted into a pass/fail oracle. B21 tests two nonblank unequal strings whose normalization may match, which follows the fixed exact-String rule.

No receipt format, nonempty receipt text, cross-instance uniqueness, gapless sequence, exception message, capacity, counter-overflow limit, or new persistence/concurrency behavior was assumed. The original unanswered questions remain unanswered; successful selected tests do not resolve them.

## Tooling attempts and evidence limits

Earlier attempts and their limits are preserved in `B-tests/experiment1-prior-attempts.md`. The first runner failed before compilation because Windows PowerShell decoded a UTF-8 Chinese path literal incorrectly. The runner was corrected to derive the base directory from `$PSScriptRoot`; neither Java source was changed. An earlier successful-looking test log lacked captured subprocess exit evidence in its outer attempt, so it was not treated as proof of exit 0. The reported outcome above relies on the later JSON's actual compiler/test exits and the execution tool's returned runner exit.

The review did not read root reports, evidence snapshots, other implementations, controller files, or project directory listings. Aside from allowed source inputs, only this reviewer's generated tests, runner, logs and result JSON were inspected. A read-only OS process diagnostic was attempted during delayed output collection; Win32 process querying returned access denied, and no escalation was requested. A short basic process inventory was also queried; no project source was obtained from it.

Experiment one is complete. No later candidate has been inspected or tested in this phase.
