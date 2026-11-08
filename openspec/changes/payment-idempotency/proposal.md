# Proposal

## Why
Retries after an uncertain response currently create duplicate payments.

## What Changes
- Reuse a payment for identical retries and reject conflicting amounts.

## Capabilities

### New Capabilities
- `payment-idempotency`: stable payment identity on retries.

## Impact
- Payment module, order caller, contract, and tests.
