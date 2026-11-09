# Verification trust

## Purpose
Keep requirement coverage, actual execution, source revision and business qualification distinct in a bounded synthetic experiment.

## ADDED Requirements

### Requirement: Blocking coverage prevents qualification
The verifier SHALL reject qualification when a confirmed blocking target lacks effective evidence, even if selected tests passed.

#### Scenario: Passing partial suite
- **WHEN** all selected legacy checks pass but a blocking target is PARTIAL or GAP
- **THEN** the conclusion is NOT QUALIFIED

### Requirement: Incomplete execution prevents qualification
The verifier SHALL not qualify missing, failing, skipped, empty or stale required checks.

#### Scenario: Skipped mandatory check
- **WHEN** a mandatory check is skipped
- **THEN** its status is SKIPPED and qualification is rejected

### Requirement: Requiredness is independent of coverage
All twenty confirmed ledger business targets SHALL retain blocking YES regardless of current coverage or passing tests. No requirement in this synthetic scope authorizes a NO exception.

#### Scenario: Covered mandatory target
- **WHEN** a target is COVERED and its checks pass
- **THEN** its blocking attribute remains YES

#### Scenario: Mandatory target weakened in the map
- **WHEN** any confirmed target is labeled nonblocking
- **THEN** the map is not acceptable and qualification is rejected
