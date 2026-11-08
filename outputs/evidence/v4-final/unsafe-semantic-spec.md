# Lifecycle

## Purpose
Define the allowed states and input constraints for the isolated synthetic lifecycle experiment.

## Requirements

### Requirement: Payment lifecycle
The system SHALL support CREATED, PAID. The system SHALL reject non-positive amounts.

#### Scenario: Accepted state
- **WHEN** a payment uses a documented state
- **THEN** the state is accepted

#### Scenario: Invalid amount
- **WHEN** the amount is non-positive
- **THEN** it is rejected

#### Scenario: Review
- **WHEN** a payment is inspected
- **THEN** the state is handled
