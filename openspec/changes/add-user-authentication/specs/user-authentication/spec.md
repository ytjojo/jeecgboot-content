## ADDED Requirements

### Requirement: Multi-method registration
The system SHALL allow a visitor to register a content community account by verified mobile phone, verified email plus password, or supported third-party identity.

#### Scenario: Mobile registration succeeds
- **WHEN** an unregistered visitor submits a valid mobile number and a correct SMS verification code within 5 minutes
- **THEN** the system creates the account, binds the mobile number, issues a login session, and returns the authenticated user summary

#### Scenario: Mobile registration rejects invalid field values
- **WHEN** a visitor requests an SMS code with a null mobile number, an empty mobile number, or a mobile number outside the supported length or format
- **THEN** the system MUST reject the request and MUST NOT send a verification code

#### Scenario: Mobile verification code is expired or over-attempted
- **WHEN** a visitor submits an expired SMS code or has already failed mobile code verification 3 times
- **THEN** the system rejects the registration, requires a new code, and applies the configured cooldown before another code can be requested

#### Scenario: Registered mobile cannot register again
- **WHEN** a visitor attempts mobile registration with a mobile number already bound to an account
- **THEN** the system rejects registration and returns a login guidance response

#### Scenario: Email registration succeeds after verification
- **WHEN** an unregistered visitor submits a valid email and a password that meets strength rules, then confirms the verification email within 24 hours
- **THEN** the system creates the account, binds the email, and allows login by email password

#### Scenario: Email registration rejects invalid field values
- **WHEN** a visitor submits a null email, empty email, invalid email format, null password, empty password, password shorter than 8 characters, or password without a number
- **THEN** the system rejects the registration and returns field-level validation errors

#### Scenario: Third-party registration or login succeeds
- **WHEN** a visitor completes authorization with WeChat, Apple, or Google and the provider identity is not yet bound
- **THEN** the system creates or links an account, stores the provider binding, issues a login session, and indicates whether profile completion is required

#### Scenario: Third-party authorization fails
- **WHEN** the provider returns cancellation, null identity, empty identity, invalid signature, or API error
- **THEN** the system rejects the login and offers alternative login methods without creating a partial binding

### Requirement: Login and session management
The system SHALL allow a registered user to log in by verification code, password, or bound third-party identity and SHALL manage active device sessions.

#### Scenario: Verification-code login succeeds
- **WHEN** a registered user submits a bound mobile number and a correct, unexpired code
- **THEN** the system issues an access token, refresh token, and device session record

#### Scenario: Verification-code login rejects missing or unknown mobile
- **WHEN** a user submits a null mobile number, empty mobile number, invalid mobile format, or mobile number not bound to any account
- **THEN** the system rejects login and returns the appropriate validation or registration guidance response

#### Scenario: Password login succeeds
- **WHEN** a registered user submits a bound mobile number or email and the correct password
- **THEN** the system issues an access token, refresh token, and device session record

#### Scenario: Password login protects credential details
- **WHEN** a user submits a null identifier, empty identifier, unknown identifier, null password, empty password, or wrong password
- **THEN** the system rejects login without revealing whether the identifier or password was incorrect

#### Scenario: Password failure lockout
- **WHEN** a user fails password login 5 consecutive times within the configured window
- **THEN** the system locks password login for that account for 15 minutes and recommends verification-code login or password reset

#### Scenario: Active device limit is enforced
- **WHEN** a successful login would create more than 5 active sessions for the same account
- **THEN** the system invalidates the earliest active non-current session and records the session replacement event

#### Scenario: User views and revokes sessions
- **WHEN** an authenticated user requests the device list and revokes another active device
- **THEN** the system returns device type, operating system, last login time, IP, location, and invalidates the selected device token

#### Scenario: Current device cannot be revoked from itself
- **WHEN** an authenticated user attempts to revoke the current session
- **THEN** the system rejects the operation and keeps the current token valid

### Requirement: Account binding and unbinding
The system SHALL allow an authenticated user to bind, replace, and unbind mobile numbers, emails, and third-party identities while preserving at least one usable login path.

#### Scenario: Bind mobile or email succeeds
- **WHEN** an authenticated user proves ownership of an unbound mobile number by SMS code or an unbound email by verification link
- **THEN** the system binds the contact method to the account and returns the updated account security state

#### Scenario: Binding rejects invalid or occupied contact values
- **WHEN** a user submits a null value, empty value, invalid mobile or email format, or a mobile or email already bound to another account
- **THEN** the system rejects the binding and does not change existing account security state

#### Scenario: Replace mobile or email requires old and new ownership proof
- **WHEN** a user replaces a mobile number or email and supplies valid proof for both the current contact method and the new contact method
- **THEN** the system atomically updates the binding and invalidates old verification tokens

#### Scenario: Unbind last contact method is rejected
- **WHEN** a user attempts to unbind the last mobile or email contact method
- **THEN** the system rejects the operation and instructs the user to bind another contact method first

#### Scenario: Bind third-party identity succeeds
- **WHEN** an authenticated user completes provider authorization for a third-party identity not bound to another account
- **THEN** the system binds the provider identity to the current account

#### Scenario: Unbind last login method is rejected
- **WHEN** a user attempts to unbind a third-party identity and no other password, code, or third-party login method remains available
- **THEN** the system rejects the operation and keeps the provider binding

### Requirement: Password recovery and abnormal login detection
The system SHALL allow users to recover passwords through verified contact methods and SHALL detect new-device or unusual-location logins.

#### Scenario: Reset password by mobile succeeds
- **WHEN** a user submits a bound mobile number, correct SMS code, and a new password that meets strength rules
- **THEN** the system updates the password, invalidates the reset token, and sends a security notification

#### Scenario: Reset password by email succeeds
- **WHEN** a user opens a valid reset link within 24 hours and submits a new password that meets strength rules
- **THEN** the system updates the password, invalidates the reset token, and sends a security notification

#### Scenario: Password reset rejects invalid field values
- **WHEN** a user submits a null identifier, empty identifier, expired token, reused token, null password, empty password, password shorter than 8 characters, password without letters or numbers, or one of the previous 3 passwords
- **THEN** the system rejects the reset and does not change the stored password

#### Scenario: High-risk password recovery requires extra verification
- **WHEN** password recovery is initiated from a new device, unusual location, or risky behavior pattern
- **THEN** the system requires configured supplemental verification before allowing password reset

#### Scenario: Abnormal login notification is sent
- **WHEN** a user logs in from a new device or unusual location
- **THEN** the system creates an in-app notification containing login time, location, device information, and confirmation actions

#### Scenario: User denies abnormal login
- **WHEN** a user marks an abnormal login as not performed by them
- **THEN** the system invalidates that device session and guides the user to reset the password

#### Scenario: User trusts abnormal login
- **WHEN** a user confirms an abnormal login as their own operation
- **THEN** the system marks the device as trusted and suppresses repeated alerts for the same trusted device unless risk signals change

### Requirement: Risk control and abuse prevention
The system SHALL identify and block frequent failed attempts, brute-force login, and batch registration without blocking verified normal users unnecessarily.

#### Scenario: Frequent login failure requires human verification
- **WHEN** an account, IP, or device fingerprint reaches 10 failed login attempts within the configured window
- **THEN** the system requires image captcha or slider verification before the next login attempt

#### Scenario: Excessive login failure locks temporarily
- **WHEN** an account, IP, or device fingerprint reaches 20 failed login attempts within the configured window
- **THEN** the system temporarily locks related login attempts for 30 minutes and records a risk event

#### Scenario: Risk counters reject invalid keys
- **WHEN** a risk decision is requested with null account identifier, empty IP, empty device fingerprint, or malformed device fingerprint beyond allowed length
- **THEN** the system rejects or degrades the decision according to configured safe defaults and records the missing signal

#### Scenario: Batch registration is challenged
- **WHEN** the same IP creates more than 10 accounts within 1 hour or matches configured machine-like registration patterns
- **THEN** the system requires additional human verification and limits registration frequency

#### Scenario: Malicious batch registration is blocked
- **WHEN** a registration request matches configured malicious batch-registration rules after challenge evaluation
- **THEN** the system blocks registration, records the risk event, and keeps an audit trail for review

#### Scenario: False-positive handling restores access
- **WHEN** a normal user completes the configured verification or an appeal is approved
- **THEN** the system removes the temporary restriction and records the case for rule tuning

### Requirement: Account cancellation and cooling-off period
The system SHALL allow users to request account cancellation with prerequisite checks, a configurable cooling-off period, cancellation reversal, and irreversible finalization.

#### Scenario: Cancellation request succeeds
- **WHEN** an authenticated user with no pending appeal, violation handling, unsettled order, or unsettled points confirms cancellation
- **THEN** the system creates a cancellation request, marks the account as in cooling-off, and sends confirmation by email or bound contact method

#### Scenario: Cancellation request rejects invalid or blocked state
- **WHEN** the user is unauthenticated, account id is null or empty, confirmation flag is false, or the account has pending appeal, violation handling, unsettled order, or unsettled points
- **THEN** the system rejects the cancellation request and leaves the account active

#### Scenario: Cooling-off login can cancel deletion
- **WHEN** a user in the cooling-off period logs in and confirms cancellation reversal
- **THEN** the system restores the account to normal state and clears the pending cancellation marker

#### Scenario: Cooling-off period is configurable and bounded
- **WHEN** an administrator configures a cooling-off period below 7 days, above 30 days, or as an empty value
- **THEN** the system rejects the out-of-range value or falls back to the configured default within 7 to 30 days

#### Scenario: Final cancellation is irreversible
- **WHEN** the cooling-off period ends and the finalization job runs
- **THEN** the system anonymizes or deletes personal data, marks the account as cancelled, invalidates all active sessions, and retains audit logs for 3 years

#### Scenario: Cancelled account cannot log in
- **WHEN** a user attempts to log in with an account that has already been finalized as cancelled
- **THEN** the system rejects login and returns the cancelled-account response
