## ADDED Requirements

### Requirement: User can view authorized third-party applications
The system SHALL display a list of all third-party applications authorized by the user, including application name, authorization date, and scope of access.

#### Scenario: View authorized applications list
- GIVEN: 用户进入"第三方授权"页面
- WHEN: 页面加载
- THEN: 用户看到所有已授权的应用列表（应用名称、授权时间、授权范围）

#### Scenario: View application details
- GIVEN: 用户想了解某个应用的权限
- WHEN: 用户点击查看详情
- THEN: 用户看到该应用可以访问的数据范围（如个人资料、发布内容等）

### Requirement: User can revoke third-party authorization
The system SHALL allow users to revoke authorization for any third-party application, immediately invalidating its access token.

#### Scenario: Revoke application authorization
- GIVEN: 用户想撤销某个应用的授权
- WHEN: 用户点击"撤销授权"并确认
- THEN: 系统立即使该应用的 Access Token 失效

#### Scenario: Access denied after revocation
- GIVEN: 用户撤销了授权
- WHEN: 该应用尝试访问用户数据
- THEN: 系统拒绝访问并返回"授权已撤销"错误

#### Scenario: Re-authorization flow
- GIVEN: 用户想重新授权
- WHEN: 用户再次使用该应用
- THEN: 系统引导用户重新进行 OAuth 授权流程
