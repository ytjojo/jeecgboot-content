## ADDED Requirements

### Requirement: 频道基础数据模型

系统 SHALL 支持统一的频道数据模型，包含基础字段和差异化字段，支持 system/personal/organization 三种类型。

#### Scenario: 创建频道时必填字段校验
- **WHEN** 创建任意类型频道
- **THEN** 系统要求填写名称、简介、类型、隐私设置、归属分类，缺失任一字段 SHALL 拒绝创建

#### Scenario: 频道类型字段不可修改
- **WHEN** 频道已创建成功
- **THEN** 频道类型值 SHALL 为 system/personal/organization 之一，且创建后不可直接修改

#### Scenario: 组织频道必须绑定组织
- **WHEN** 创建组织频道
- **THEN** 系统 SHALL 要求绑定组织 ID 且不可为空

#### Scenario: 系统频道可设置置顶权重
- **WHEN** 创建系统频道
- **THEN** 系统 SHALL 允许设置置顶权重字段，默认值为 0

---

### Requirement: 频道状态枚举定义

系统 SHALL 支持频道状态枚举，包含 Draft(0)、PendingReview(1)、Active(2)、Rejected(3)、DeleteCooling(4)、Deleted(5) 六种基础状态。

#### Scenario: 新建频道初始状态
- **WHEN** 系统频道创建成功
- **THEN** 频道状态 SHALL 直接为 Active(2)

#### Scenario: 用户频道创建后待审核
- **WHEN** 个人或组织频道创建成功
- **THEN** 频道状态 SHALL 为 PendingReview(1)

---

### Requirement: 频道表必备审计字段

每条频道记录 SHALL 包含 created_time、updated_time 审计字段，支持软删除（del_flag）。

#### Scenario: 频道创建时自动填充时间
- **WHEN** 频道记录插入数据库
- **THEN** created_time 和 updated_time SHALL 自动填充为当前时间

#### Scenario: 频道更新时自动更新时间
- **WHEN** 频道记录被修改
- **THEN** updated_time SHALL 自动更新为当前时间
