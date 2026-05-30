## ADDED Requirements

### Requirement: 个人频道转让

个人频道主 SHALL 能够将频道所有权转让给其他符合条件的用户，需双方确认。

#### Scenario: 发起转让请求
- **WHEN** 个人频道主发起转让并选择目标用户
- **THEN** 系统 SHALL 向目标用户发送确认请求，创建转让记录（状态 Pending）

#### Scenario: 目标用户确认转让
- **WHEN** 目标用户确认接受转让
- **THEN** 原频道主降为管理员，目标用户成为新频道主，转让记录状态变为 Accepted

#### Scenario: 目标用户拒绝转让
- **WHEN** 目标用户拒绝转让请求
- **THEN** 频道所有权保持不变，转让记录状态变为 Rejected

#### Scenario: 转让请求超时
- **WHEN** 转让请求超过 7 天未确认
- **THEN** 转让记录状态 SHALL 变为 Expired，频道所有权保持不变

---

### Requirement: 组织频道转让限制

组织频道 SHALL 仅可在组织管理员间转移管理权，不可转让给组织外的个人用户。

#### Scenario: 组织频道转让给组织外个人拦截
- **WHEN** 尝试将组织频道转让给组织外的个人用户
- **THEN** 系统 SHALL 拒绝并提示"组织频道仅可在组织管理员间转移管理权"

#### Scenario: 组织频道转让给组织内管理员成功
- **WHEN** 将组织频道转让给同一组织的其他管理员
- **THEN** 系统 SHALL 允许进入转让流程

---

### Requirement: 系统频道不可转让

系统频道 SHALL 不可转让所有权。

#### Scenario: 尝试转让系统频道
- **WHEN** 尝试转让系统频道
- **THEN** 系统 SHALL 拒绝并提示"系统频道不可转让"

---

### Requirement: 频道删除前置条件校验

频道删除 SHALL 需要满足前置条件：内容已清理或转移、无未了结的付费订阅、操作者具备删除权限。

#### Scenario: 前置条件不满足拦截
- **WHEN** 频道存在未清理内容、未完成转移或未了结的付费订阅
- **THEN** 系统 SHALL 拒绝进入删除确认并提示阻塞原因

#### Scenario: 前置条件满足进入二次确认
- **WHEN** 频道主申请删除频道且所有前置条件满足
- **THEN** 系统 SHALL 进入二次确认流程，展示影响范围和是否可撤销

---

### Requirement: 删除冷静期机制

频道删除确认后 SHALL 进入 7 天冷静期，期间可撤销，冷静期结束后进入 Deleted 状态。

#### Scenario: 确认删除进入冷静期
- **WHEN** 用户确认删除频道
- **THEN** 频道状态 SHALL 变为 DeleteCooling，记录 delete_cooling_end_time

#### Scenario: 冷静期内撤销删除
- **WHEN** 频道主在冷静期内撤销删除
- **THEN** 频道状态 SHALL 回退到 Active

#### Scenario: 冷静期到期自动删除
- **WHEN** 冷静期结束（定时任务扫描）
- **THEN** 频道状态 SHALL 变为 Deleted，用户不可再访问管理入口

---

### Requirement: 组织频道删除需最高管理员确认

组织频道删除 SHALL 需要组织最高管理员确认。

#### Scenario: 组织频道删除确认
- **WHEN** 组织最高管理员确认删除组织频道且满足前置条件
- **THEN** 系统 SHALL 执行删除流程，进入冷静期

---

### Requirement: 系统频道不可删除

系统频道 SHALL 不可由用户删除，仅平台可归档或关闭。

#### Scenario: 尝试删除系统频道
- **WHEN** 尝试删除系统频道
- **THEN** 系统 SHALL 拒绝并提示"系统频道仅平台可管理"
