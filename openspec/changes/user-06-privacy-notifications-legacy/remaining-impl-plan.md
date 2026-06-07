# EPIC-06 剩余功能实施计划

> **TDD 驱动，SubAgent 并行执行**

---

## 前置假设

1. SQL 字段追加到现有 `V3.9.1_58__content_privacy_notifications.sql` 和回滚文件，不新建迁移文件
2. 第三方授权详情端点放在现有 `ContentUserThirdPartyAuthController`（已有列表和撤销端点）
3. `canViewActivity` 复用 `canViewField` 的可见性枚举逻辑，不需要新的可见性枚举值
4. `shouldNoindexProfile` 放在 `IContentUserVisibilityPolicyService` 中
5. WebMvc 测试补充到现有 `ContentUserSettingsControllerWebMvcTest`

---

## 任务拆分

### Task 1: 三个可见性字段全链路（P0）

**范围**: `browse_history_visibility` / `like_activity_visibility` / `favorite_visibility`

**涉及文件**:
- `V3.9.1_58__content_privacy_notifications.sql` — 追加 ALTER TABLE
- `V3.9.1_58__content_privacy_notifications_rollback.sql` — 追加 DROP COLUMN
- `ContentUserPrivacySetting.java` — 追加 3 个字段
- `ContentUserPrivacyUpdateReq.java` — 追加 3 个带 @Pattern 的请求字段
- `ContentUserProfileServiceImpl.java` — `applyPrivacyUpdate` 和 `defaultPrivacy` 中追加处理

**TDD 步骤**:
1. 先写/扩展 `ContentUserProfileServiceTest` 中的测试：验证新字段能被设置和读取
2. 运行测试，确认失败（字段不存在）
3. 修改 SQL、Entity、Req、Service
4. 运行测试，确认通过

---

### Task 2: 活动可见性方法（P1）

**范围**: `canViewActivity` 方法

**涉及文件**:
- `IContentUserVisibilityPolicyService.java` — 新增接口方法
- `ContentUserVisibilityPolicyServiceImpl.java` — 实现方法
- `ContentUserVisibilityPolicyServiceTest.java` — 新增测试

**TDD 步骤**:
1. 先在测试中写 `shouldAllowActivityViewWhenPublic` / `shouldRejectActivityViewWhenPrivate`
2. 运行测试，确认编译失败（接口方法不存在）
3. 添加接口方法和实现
4. 运行测试，确认通过

---

### Task 3: Noindex 决策方法（P1）

**范围**: `shouldNoindexProfile`

**涉及文件**:
- `IContentUserVisibilityPolicyService.java` — 新增方法
- `ContentUserVisibilityPolicyServiceImpl.java` — 实现
- `ContentUserVisibilityPolicyServiceTest.java` — 新增测试

**TDD 步骤**:
1. 先写测试 `shouldReturnNoindexWhenSearchEngineDisabled` / `shouldNotNoindexWhenEnabled`
2. 运行测试，确认失败
3. 实现方法
4. 运行测试，确认通过

---

### Task 4: Token 撤销端口（P0）

**范围**: 端口接口 + Noop 实现 + 集成到 revokeAuth

**涉及文件**（新建）:
- `ContentThirdPartyTokenRevocationPort.java` — 端口接口
- `ContentNoopThirdPartyTokenRevocationPort.java` — Noop 实现

**涉及文件**（修改）:
- `ContentUserThirdPartyAuthServiceImpl.java` — 注入端口，revokeAuth 中调用
- `ContentUserThirdPartyAuthServiceTest.java` — 新增测试

**TDD 步骤**:
1. 先写测试：验证 revokeAuth 调用 token 撤销端口
2. 运行测试，确认编译失败
3. 创建端口接口和 Noop 实现，注入到 Service
4. 运行测试，确认通过

---

### Task 5: 第三方授权详情端点和 VO（P1）

**范围**: 单条授权详情查询

**涉及文件**（新建）:
- `ContentThirdPartyAuthorizationDetailVO.java` — 详情 VO

**涉及文件**（修改）:
- `IContentUserThirdPartyAuthService.java` — 新增 `getAuthDetail` 方法
- `ContentUserThirdPartyAuthServiceImpl.java` — 实现详情查询
- `ContentUserThirdPartyAuthController.java` — 新增 GET `/{authId}` 端点
- `ContentUserThirdPartyAuthServiceTest.java` — 新增详情测试

**TDD 步骤**:
1. 先写测试：验证详情查询返回正确 VO、不存在时抛异常、非 owner 拒绝
2. 运行测试，确认失败
3. 创建 VO、添加接口方法和实现、添加端点
4. 运行测试，确认通过

---

### Task 6: 迁移合同测试（P0）

**范围**: `ContentPrivacyNotificationsMigrationTest`

**涉及文件**（新建）:
- `ContentPrivacyNotificationsMigrationTest.java`

**测试内容**:
- 验证迁移 SQL 文件存在
- 验证回滚 SQL 文件存在
- 验证迁移 SQL 包含 `browse_history_visibility` / `like_activity_visibility` / `favorite_visibility` / `online_status_visibility`
- 验证迁移 SQL 包含 `content_user_third_party_auth` 表
- 验证迁移 SQL 包含 `content_notification_audit_log` 表
- 验证回滚 SQL 包含对应 DROP 语句

---

### Task 7: Controller WebMvc 测试补充（P2）

**范围**: 补充 `ContentUserSettingsControllerWebMvcTest` 覆盖

**涉及文件**:
- `ContentUserSettingsControllerWebMvcTest.java`

**新增测试**:
- 隐私更新端点 — 200 + 验证请求转发
- 通知查询端点 — 200 + VO 字段
- 通知更新端点 — 200 + VO 字段
- 关注流查询端点 — 200
- 关注流更新端点 — 200
- 可见性检查端点 — 200 + boolean 值
- 安全设置端点 — 200 + VO 字段

---

## 执行顺序

```
Task 1 (3个字段) ──→ Task 2 (canViewActivity) ──→ Task 3 (shouldNoindex)
                                                                      ↓
Task 4 (Token撤销) ──→ Task 5 (详情端点)                      Task 6 (迁移测试)
                                                                      ↓
                                                             Task 7 (WebMvc测试)
```

- Task 1/4 可并行（无依赖）
- Task 2/3 依赖 Task 1（因为字段先要存在）
- Task 5 依赖 Task 4（详情端点可能需要 token 撤销逻辑）
- Task 6/7 最后执行（验证全链路）

## Maven 测试命令

```bash
# Task 1-3
/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserProfileServiceTest,ContentUserVisibilityPolicyServiceTest test"

# Task 4-5
/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserThirdPartyAuthServiceTest test"

# Task 6
/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentPrivacyNotificationsMigrationTest test"

# Task 7
/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSettingsControllerWebMvcTest test"

# 全量验证
/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserProfileServiceTest,ContentUserVisibilityPolicyServiceTest,ContentUserThirdPartyAuthServiceTest,ContentPrivacyNotificationsMigrationTest,ContentUserSettingsControllerWebMvcTest test"
```

## 验收标准

1. 所有新增/修改的测试通过
2. `V3.9.1_58` SQL 包含 6 个可见性字段（原有 1 个 + 新增 3 个 + 2 个已有）
3. `ContentThirdPartyTokenRevocationPort` 接口存在，Noop 实现注入到 Service
4. 第三方授权详情端点 `GET /api/v1/content/user/auth/third-party/{authId}` 可用
5. `canViewActivity` 和 `shouldNoindexProfile` 方法在接口和实现中存在
6. WebMvc 测试覆盖所有 8 个 Settings 端点 + 2 个第三方授权端点
