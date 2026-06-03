## ADDED Requirements

### Requirement: Provide account security settings entry
The system SHALL provide a unified account security settings entry for registered users, including device management, password change, two-step verification, and login reminder controls.

#### Scenario: Show account security entries
- GIVEN: a registered user opens account security settings
- WHEN: the settings are loaded
- THEN: the system returns entries for device management, password change, two-step verification, and login reminders

#### Scenario: Navigate to existing device management
- GIVEN: a registered user opens account security settings
- WHEN: the user selects device management
- THEN: the system routes the user to the existing device management flow

#### Scenario: Navigate to existing password change
- GIVEN: a registered user opens account security settings
- WHEN: the user selects password change
- THEN: the system routes the user to the password change flow that requires old and new password verification

### Requirement: Manage login reminder setting
The system SHALL allow a registered user to enable or disable login reminders for new device login events.

#### Scenario: Enable login reminders
- GIVEN: a registered user has disabled login reminders
- WHEN: the user enables login reminders and saves
- THEN: the system sends a reminder for subsequent new device login events

#### Scenario: Disable login reminders
- GIVEN: a registered user has enabled login reminders
- WHEN: the user disables login reminders and saves
- THEN: the system does not send non-mandatory login reminders for subsequent new device login events
