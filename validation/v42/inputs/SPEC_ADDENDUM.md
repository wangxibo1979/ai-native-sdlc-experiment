# Confirmed v4.2 synthetic-spec addendum

Human confirmation on 2026-11-09 (Asia/Shanghai): for this synthetic Java 17 ledger, “blank” means Java 17 String.isBlank(). This resolves Q1 only. It does not change the original historical specification or authorize real business rules.

Null still throws IllegalArgumentException. All other original outcomes, including no operation for invalid input and exact identity of nonblank keys, remain applicable.
