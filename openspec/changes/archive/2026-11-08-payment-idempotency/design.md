# Design

A process-local map binds order ID to amount and payment ID. A repeated identical request returns the stored ID; a different amount fails. This synthetic implementation has no persistence, concurrency protection or network service and is not production-ready.
