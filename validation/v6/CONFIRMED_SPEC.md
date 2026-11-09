# Confirmed synthetic specification: V41-IDEMPOTENCY

Scope: a single-threaded, in-memory Java 17 payment ledger in one process. The request key is an exact String identifier; null or blank keys are invalid. Amount is a Java long and must be positive; no additional business upper bound is specified. This is not distributed payment, durable recovery, or a concurrent service.

- First legal key/amount request produces a non-null receipt ID and executes exactly one simulated business operation.
- Repeating the exact key and amount returns the original receipt ID, with no extra operation.
- Reusing a key with a different amount throws IllegalArgumentException, preserving the previous receipt and operation count.
- Invalid key or non-positive amount throws IllegalArgumentException without a business operation.
- failBeforeCommit is a synthetic provider-fault switch: on a new request it throws IllegalStateException before committing any receipt or operation; a later legal retry can succeed once. An already committed identical replay requires no provider operation.
- Different legal keys produce distinct receipt IDs.

The failure switch, executions count and receipt ID are the observable experiment model. Persistence, machine crashes, authentication, money transfer, concurrent invocations and multi-service consistency are NOT claimed.

Test outcomes must follow these fixed rules. Unspecified real business choices must be recorded as questions rather than silently invented. No real human business approval is represented by this synthetic fixture.
