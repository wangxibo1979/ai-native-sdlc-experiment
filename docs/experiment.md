# Experiment protocol

Synthetic rules: positive CNY amounts only. Payment creation returns a stable payment ID for a repeated order ID; reusing an order ID with a different amount is rejected. Order sends orderId and amount to payment. No real money, storage or network.

Baseline: a valid payment can be created and zero amount is rejected. Change: idempotent retries and conflict detection. Test baseline before the change; observe red then green. Use a feature branch and PR; Actions runs the same tests independently. Shared-contract modification requires owner review; no actual second human is simulated as approval.
