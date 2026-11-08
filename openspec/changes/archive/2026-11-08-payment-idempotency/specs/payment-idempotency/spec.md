# Payment idempotency

## Purpose

Define retry behavior of the simulated payment module to avoid duplicate payment identities.

## ADDED Requirements

### Requirement: Retry returns original payment
The service SHALL return the original payment ID for an order retried with the same amount.

#### Scenario: Identical retry
- **WHEN** an order is submitted twice with the same amount
- **THEN** both responses have the same payment ID

### Requirement: Conflicting retry is rejected
The service SHALL reject an existing order ID used with a different amount.

#### Scenario: Changed amount
- **WHEN** an existing order ID is submitted with a different amount
- **THEN** the service rejects the request
