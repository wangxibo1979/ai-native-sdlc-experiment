# Validation evidence

## Purpose
Record reproducible outcomes and explicit limits of isolated synthetic development experiments.

## ADDED Requirements

### Requirement: Evidence preserves actual outcomes
The experiment SHALL distinguish process failures, business assertion failures, and missing approval.

#### Scenario: Missing second reviewer
- **WHEN** no real second reviewer has approved
- **THEN** the full approval gate is BLOCKED
