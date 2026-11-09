# B: Independent spec-first review design

## Status and evidence boundary

Phase 1 only. The sole project source read for this design was `CONFIRMED_SPEC.md` in this directory. No implementation, original tests, logs, reports, or directory listings were inspected, and no tests were executed. An environment skill file (`C:/Users/wang_/.codex/skills/using-superpowers/SKILL.md`) was also read; it says dispatched subagents skip that skill and supplies no business requirements.

This document defines independent acceptance oracles and discriminating examples, not an implementation plan. All requirements below come from the confirmed synthetic fixture. They do not represent real business approval.

## Fixed requirements and observable model

The system is a single-threaded, in-memory Java 17 ledger in one process. Observe receipt results, exception types, and changes in simulated business execution count. Let `C` be the count immediately before a request, and let `M` be the abstract committed mapping from an exact key to its accepted amount and receipt. `M` is an oracle model, not an assertion about internal data structures.

| Input/state | Required result | Required state effect |
| --- | --- | --- |
| Invalid key (null or blank), or amount <= 0 | `IllegalArgumentException` | No business execution; existing committed behavior must remain usable |
| Legal uncommitted key, positive amount, fault switch off | Non-null receipt `r` | Exactly one execution: count becomes `C + 1`; commit that key/amount/receipt |
| Legal uncommitted key, positive amount, fault switch on | `IllegalStateException` | Count remains `C`; no receipt is committed for the request |
| Committed key, original amount, either fault-switch value | Original receipt | Count remains `C`; committed mapping remains the same |
| Committed key, different positive amount, either fault-switch value | `IllegalArgumentException` | Count remains `C`; original mapping remains the same |

An invalid amount is rejected even for an existing key. The switch is a provider fault for a *new legal* request; it does not replace validation or conflict rules. The fixed rules therefore determine the exception type for invalid inputs or committed conflicts with the switch enabled.

The important invariants are:

1. A committed key has one accepted amount and one stable receipt for the lifetime of this ledger instance.
2. Accepted exact replays cause zero additional simulated operations, regardless of provider fault state.
3. Rejected requests and provider failures cause zero operations. Observed count changes equal the number of successful first commits, not calls, attempts, or receipt allocations.
4. A conflict must not overwrite the original amount or receipt; a subsequent original replay proves preservation.
5. A failed new request binds neither a receipt nor a successful amount. A later legal request with that key can commit once.
6. Committed receipts of different legal keys are pairwise distinct, even when their amounts are equal or calls are interleaved with failures.
7. Key identity is the exact String value, not its trimmed, case-folded, parsed, or normalized form. Distinct String objects with the same characters represent the same identifier; object identity does not define the request key.
8. Every positive Java `long` is in the stated amount domain, including `Long.MAX_VALUE`; no invented cap or narrowing conversion is permitted.

## Independent concrete cases and their oracles

Cases are expressed in behavior-level notation because no API signature has been inspected. `submit(key, amount, fault)` denotes one request, and count checks are deltas from an observed baseline. Fresh ledgers isolate unrelated scenarios. The examples do not assume a receipt format or sequence.

| ID | Concrete scenario | Required observations / discriminating counterexample |
| --- | --- | --- |
| B01 | Submit `("order-1", 100, false)` on a fresh ledger | Non-null receipt; count delta exactly +1. A non-null receipt without an operation fails. |
| B02 | Repeat B01 once, then many times | Every result equals the first receipt; total delta remains +1. A result cache that still invokes the provider fails. |
| B03 | Commit `("order-1", 100, false)`; submit `("order-1", 101, false)`; replay amount 100 | Conflict throws `IllegalArgumentException`; no count increase; original replay returns original receipt. Overwrite-before-reject fails. |
| B04 | Commit `("order-1", 100, false)`; submit amount 99; then replay 100 | Same preservation oracle; conflict rejection must work in both amount directions. |
| B05 | Submit `(null, 1, false)`, `("", 1, false)`, `(" ", 1, false)`, and `("\t\r\n", 1, false)` on isolated instances | Each throws `IllegalArgumentException`; each count delta is 0. An empty-only check misses nonempty blank keys. |
| B06 | With key `"amount-check"`, submit amounts 0, -1, and `Long.MIN_VALUE` on isolated instances | Each throws `IllegalArgumentException`; delta 0. Signed overflow or absolute-value handling must not make a negative legal. |
| B07 | Submit `("min-positive", 1, false)` and `("max-positive", Long.MAX_VALUE, false)` | Both succeed, each +1, with different non-null receipts; replays add zero operations. Arbitrary payment caps or signed narrowing fail. |
| B08 | Commit `("large", 2147483648L, false)`; submit `("large", 2147483649L, false)`; replay first amount | Conflict, no extra execution, original receipt preserved. Comparison must not lose long precision. |
| B09 | Commit `("wide", 1, false)`; submit `("wide", 4294967297L, false)` | Conflict, delta 0. These amounts collide after common 32-bit narrowing. |
| B10 | Submit `("retry", 100, true)`; then `("retry", 100, false)`; then replay | First call throws `IllegalStateException` and delta 0; second succeeds +1; third returns its receipt with delta 0. Poisoned failed-key entries fail. |
| B11 | Fail `("retry-many", 100, true)` repeatedly, then turn the switch off and submit/replay | Every failure adds zero; exactly one eventual operation and one stable committed receipt. Failure counters must not inflate business executions. |
| B12 | Fail `("unbound", 100, true)`; submit `("unbound", 200, false)`; replay 200; then request 100 | 200 can commit once, its replay is stable, then 100 conflicts. A provider failure must not establish the first attempted amount as committed state. |
| B13 | Commit `("replay-fault", 100, false)`; replay with fault true multiple times | All replays return the original receipt, throw no provider fault, and add zero operations. Fault checking before replay recognition fails. |
| B14 | Commit `("conflict-fault", 100, false)`; request 101 with fault true; replay 100 | `IllegalArgumentException`, zero extra operations, original receipt preserved. The provider fault must not hide a committed conflict. |
| B15 | For each invalid example in B05/B06, enable fault true | Still `IllegalArgumentException`, zero operations. Invalid new requests must not become `IllegalStateException` solely due to the switch. |
| B16 | Commit `("existing", 100, false)`; request same key with 0, -1, then replay 100 | Each invalid call throws `IllegalArgumentException`, count unchanged, original receipt retained. A key-only replay shortcut must not accept illegal amounts. |
| B17 | Commit `("K", 100, false)` and `("k", 100, false)` | Two successful operations and distinct receipts; each replay is stable. Case-insensitive identity fails. |
| B18 | Commit `("x", 100, false)`, `(" x", 100, false)`, and `("x ", 100, false)` | Three successful operations and three pairwise-distinct receipts; replays are independent. Trimming a nonblank key fails. |
| B19 | Submit two separately constructed String objects containing `"same-value"`, both at amount 100 | Second call is a replay, original receipt, total delta +1. Reference-identity maps fail. |
| B20 | Submit `("Aa", 100, false)` and `("BB", 100, false)` | Both commit independently with distinct receipts despite equal standard Java String hash codes. A hash code alone is not a key. |
| B21 | Submit `("\u00E9", 100, false)` and `("e\u0301", 100, false)` | Exact strings differ; both commit with distinct receipts. Unrequested Unicode normalization fails. These are nonblank keys. |
| B22 | Submit `("001", 100, false)` and `("1", 100, false)` | Distinct receipts and two operations. Parsing identifiers as numbers fails. |
| B23 | Commit A at 10; fail B at 20; replay A with fault true; reject A at 11; commit B at 20; replay both | Count progression relative to start: 1, 1, 1, 1, 2, 2, 2. A receipt and amount survive all intervening requests; B receipt differs from A. |
| B24 | Commit three legal keys with amounts 7, 7, 8, interspersed with exact replays | Three pairwise-distinct receipts; delta exactly +3. Receipt uniqueness cannot depend solely on amount. |
| B25 | Reject `("repairable", 0, false)`; submit `("repairable", 1, false)`; replay | Invalid input has not poisoned the key; one eventual successful commit and stable replay. |
| B26 | Commit `("max", Long.MAX_VALUE, false)`; request `Long.MAX_VALUE - 1`; replay max | Conflict with zero new operations and original receipt retained. Extreme valid amounts do not change conflict rules. |

For every negative/fault scenario that follows an existing successful request, a later replay of that existing request checks preservation externally. No internal-map inspection is necessary. Repeating checks under interleaving protects against accidental global state shared between unrelated keys.

## Boundaries and exceptions that must remain explicit

- Validation is the exception to accepting a first request: first occurrence alone does not make null, blank, zero, or negative input legal.
- An already committed identical replay is the explicit exception to exercising the provider fault switch. It must succeed under fault true.
- Different amounts do not represent a second permissible business operation for a committed key, even if a provider fault is enabled.
- A provider failure is not a completed first request. A later valid retry can establish the sole committed result, and its amount need not equal an uncommitted failed attempt (B12).
- String padding around nonblank text changes the identifier; an entirely whitespace key remains invalid. Whitespace validation and identity normalization must not be conflated.
- `Long.MAX_VALUE` is legal. Receipt generation and amount comparisons must not introduce an unstated upper bound.

## Unknown choices recorded as questions, not invented requirements

These questions are explicitly unresolved and do not block the fixed-rule cases above:

1. Which precise Unicode definition of “blank” is intended? ASCII space, tab, CR, and LF cases above are unambiguous ordinary blanks. Java `String.isBlank()`, `trim().isEmpty()`, and other predicates differ on characters such as U+2003 and U+00A0; those cases require a specified oracle before being scored.
2. What are the public constructor, method, switch-control, counter-access, and receipt types? The eventual adapter must use the provided API without changing these behavioral oracles.
3. What count is exposed immediately after construction? The design uses deltas and does not impose an unspecified initialization representation.
4. Is receipt equality value-based for a declared receipt type, and is any format/nonempty constraint required? Only a non-null original receipt and distinct receipts for distinct keys are specified. Reference identity, numeric order, UUID shape, nonempty text, and consecutive numbering are not required.
5. May failed attempts consume receipt sequence values internally? No receipt or operation may be committed on failure; sequence gaps are unobservable business choices unless another requirement exposes them. Do not fail an implementation merely for a skipped identifier.
6. Is uniqueness required across different ledger instances or process restarts? The specified ledger is in one process and in memory; this design requires uniqueness among committed distinct keys within the observed instance. It does not invent cross-instance durability or uniqueness.
7. How is `failBeforeCommit` configured (per call or mutable provider setting)? The cases describe the switch state at each invocation, without assuming configuration persistence or reset semantics.
8. Are exception messages or extra exception metadata contractual? Only the specified exception classes are fixed here; messages are not acceptance criteria.
9. What performance, capacity, reset, and counter-overflow limits apply? None are specified, so large-input performance thresholds, reset behavior, and billions of executions are not invented tests.

No concurrency, distributed consistency, restart/recovery, real financial transfer, authentication, or machine-crash assertion belongs to this review. Their exclusion is a scope limit, not evidence that such behavior is safe.

## Review outcome criteria

A future implementation review should distinguish a fixed-rule violation from an unresolved-choice observation. A concrete sequence with actual result, expected receipt/exception, and count delta is evidence for a fixed-rule violation; an internal design preference is not. Passing the selected cases would support the stated model only, not prove all possible executions or any excluded real payment property.

Phase 1 deliverable is this frozen spec-first design. Implementation inspection, original-test inspection, executable tests, and an implementation verdict remain intentionally unperformed pending the next phase.
