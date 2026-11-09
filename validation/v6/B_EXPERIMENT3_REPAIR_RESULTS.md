# B independent repair verification

The repaired candidate passed the **unchanged 26-case frozen B suite** and the **unchanged two-call minimal reproducer**. Actual compiler exit: **0**. Frozen-suite Java exit: **0**, output `CASES=26 FAILURES=0`. Minimal-reproducer Java exit: **0**. Outer execution-tool runner exit: **0**.

The previously demonstrated large-long replay failure is no longer reproduced by these checks. This conclusion comes from independently compiling and executing the candidate, not from Agent A's completion statement. No claim is made that all possible defects are absent.

## Actual behavior and evidence

The minimal reproducer submits `("large", 2147483648L, false)` twice to one new ledger. Expected: the second call returns the first receipt without an exception, with a total execution delta of 1. Actual output:

```text
INPUT_1 key=large amount=2147483648 failBeforeCommit=false
OBSERVED_1 receipt=336aa3ad-2dcc-495d-9999-a90d7e9a8ea4 executionDelta=1
INPUT_2 key=large amount=2147483648 failBeforeCommit=false
EXPECTED_2 receipt=336aa3ad-2dcc-495d-9999-a90d7e9a8ea4 executionDelta=1 no exception
OBSERVED_2 receipt=336aa3ad-2dcc-495d-9999-a90d7e9a8ea4 executionDelta=1
```

All frozen B01–B26 expectations remain intact, including B07/B08/B26 that failed against the preceding candidate: positive long endpoints, adjacent amounts above the integer range, and preservation after a long-maximum conflict now pass. The suite also checks validation, amount conflicts, failure/retry semantics, fault-enabled committed replays, precise String identity, distinct receipts and interleaved requests.

`B_EXPERIMENT3_REPAIR_RESULTS.json` contains complete executable paths and argument arrays, actual exits, UTC timestamps, output and before/after source SHA-256. Separate raw logs:

- `B-tests/experiment3-repair-compile.log`: empty, no compiler diagnostics.
- `B-tests/experiment3-repair-frozen.log`: all 26 cases PASS and zero failures.
- `B-tests/experiment3-repair-minimal.log`: the two-call output above.

## Execution identity and timing

Actual invocation, from `E:\标准化投标相关文件\AI机制实验\v41-validation\validation\v6`:

```powershell
& 'E:\标准化投标相关文件\AI机制实验\v41-validation\validation\v6\B-tests\Run-Experiment3Repair.ps1'
```

The runner verified the prior expected source hashes **before** compilation, then invoked the resolved `javac.exe` with `--release 17 -encoding UTF-8`, the candidate, frozen B suite and minimal reproducer. It wrote to a new `B-tests/experiment3-repair-classes` directory and invoked `java.exe -cp <that directory>` once for `IndependentBoundaryTest` and once for `MinimalReplayRepro`. Both resolved executables are under `C:\Program Files\Common Files\Oracle\Java\javapath`. JSON retains their exact paths and command arguments.

| Operation | Start UTC | End UTC | Duration, approximately | Exit |
| --- | --- | --- | --- | --- |
| Compile | 2026-11-09T01:23:35.3641324Z | 2026-11-09T01:23:37.3194123Z | 1.96 s | 0 |
| Frozen B suite | 2026-11-09T01:23:37.3336105Z | 2026-11-09T01:23:45.6500713Z | 8.32 s | 0 |
| Minimal reproducer | 2026-11-09T01:23:45.6628875Z | 2026-11-09T01:23:53.9223054Z | 8.26 s | 0 |

Durations include process startup/output handling and are not performance requirements. There was one compilation and one execution of each required test program; no reruns were performed.

| Input | SHA-256 |
| --- | --- |
| Repaired candidate | `1D8121FB86CC3F22EEBB7DC75B82610F0D14F06589C765C013AD3D90234C6ED6` |
| Original A suite | `B7E8C89BC725E47E06DC5E7E2E82ECFF137256AF2E94FF5BA2FD1762D61E32AB` |
| Frozen B suite | `CA9DCCC57D301DF35E6F8879BB20AE5E8613F500F2865B309653CEF8A7E1BE21` |
| Minimal reproducer | `414D51550CACAA6D6390515E1E385DEC55EBAD38B3339FAB151AC5E42C5514F1` |
| Confirmed spec | `EDD5697CBB1F2FB08248906E6699D266A52A84A2D97EDD1E72CBA6E25FED9A3C` |
| Spec-first design | `B942DFB86EC2D875F8EFE62FD33ED81299B0C680C1FFF9F921DC8F2C54E41BD0` |

The A, B, minimal-reproducer, spec and spec-first-design hashes match the previously recorded expectations. All seven observed input files, including the new repair-verification runner, had identical before/after hashes during this verification. The reviewer changed none of these existing inputs. Original A tests were checked for identity, not executed; no A-suite runtime result is claimed.

## Preserved assumptions and limits

The unanswered business questions in the frozen design remain unanswered: precise Unicode blank policy; receipt format/nonempty constraints; cross-instance uniqueness; whether failed attempts consume internal identifier sequence values; exception messages; reset/capacity/performance limits and counter overflow. No passing case silently resolves these questions or creates a new requirement. Constructor/call signatures and counter/receipt types were already observed in the prior phase; they do not resolve the remaining business choices.

Results apply to the tested single-threaded, in-memory Java ledger in one process. They do not establish durable recovery, machine-crash safety, concurrency correctness, distributed/multi-service consistency, authentication, real money transfer, or universal receipt uniqueness. The finite suite supports the confirmed synthetic rules for the exercised sequences and confirms the reported minimal failure no longer occurs.

No candidate-source inspection, controller/root evidence, other implementation, Git history/diff, or Agent A completion report was needed or inspected in this phase. Existing candidate bytes were read only by hashing and compilation. Prior phase artifacts were preserved; all new outputs use experiment-three paths. No dependency or agent was added. Independent repair verification is complete.
