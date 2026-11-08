# Lifecycle

## Purpose
Define the allowed states and input constraints for the isolated synthetic lifecycle experiment.

## Requirements

### Requirement: Payment lifecycle
The system SHALL support CREATED, PAID, REVIEW. The system SHALL reject non-positive amounts.

#### Scenario: Accepted state
- **WHEN** a payment uses a documented state
- **THEN** the state is accepted

#### Scenario: Review
- **WHEN** a payment uses REVIEW
- **THEN** REVIEW is accepted

#### Scenario: Invalid amount
- **WHEN** the amount is non-positive
- **THEN** it is rejected
