## ADDED Requirements

### Requirement: List third-party authorizations
The system SHALL allow a registered user to view all third-party applications currently authorized to access the user's data.

#### Scenario: Show authorization list
- GIVEN: a registered user has one or more third-party authorizations
- WHEN: the user opens third-party authorization management
- THEN: the system returns each application name, authorization time, status, and authorization scopes

#### Scenario: Show empty authorization list
- GIVEN: a registered user has no third-party authorizations
- WHEN: the user opens third-party authorization management
- THEN: the system returns an empty list without error

### Requirement: Show authorization scope details
The system SHALL allow a registered user to inspect the data access scopes granted to a third-party application.

#### Scenario: Show granted scope details
- GIVEN: a registered user has authorized a third-party application
- WHEN: the user views that authorization's detail
- THEN: the system returns the granted scopes in user-understandable categories

### Requirement: Revoke third-party authorization
The system MUST allow a registered user to revoke a third-party authorization and MUST make related access tokens unusable immediately.

#### Scenario: Revoke an active authorization
- GIVEN: a registered user has an active third-party authorization
- WHEN: the user confirms revocation
- THEN: the system marks the authorization revoked and invalidates related access tokens

#### Scenario: Reject access after revocation
- GIVEN: a third-party application's authorization has been revoked
- WHEN: the application attempts to access the user's protected data
- THEN: the system rejects the request as revoked authorization

#### Scenario: Require reauthorization after revocation
- GIVEN: a registered user previously revoked a third-party authorization
- WHEN: the user uses that application again
- THEN: the system requires a new authorization flow before protected data access is granted
