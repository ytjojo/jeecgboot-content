## 验证报告: circle-13-growth-incentive

### 摘要

| 维度 | 状态 |
|------|------|
| Completeness | 39/39 任务完成, 24 个 requirements |
| Correctness | 21/24 requirements 已覆盖, **2 CRITICAL 差异** |
| Coherence | 设计一致 |

---

### 三体系架构（修复前必读）

本变更涉及两套独立的成长等级体系，另有第三套全局体系需要明确区分：

| 体系 | 数据库表 | API 前缀 | circleId | userId | 所属变更 |
|------|---------|---------|----------|--------|---------|
| **全局内容社区用户成长** | `content_user_level_config`、`content_user_level_benefit_config` 等 `content_user_*` 系列 | `/api/v1/content/user/growth/` | ❌ 无 | ✅ 有 | `user-03-badges-points-growth` (EPIC-03) |
| **圈子等级** | `circle_level` | `/api/v1/content/circle/growth/level/` | ✅ 有 | ❌ 无 | `circle-13-growth-incentive` (EPIC-13) |
| **圈子内成员成长** | `circle_member_growth`、`circle_growth_log`、`circle_achievement`、`circle_member_achievement`、`circle_leaderboard_snapshot` | `/api/v1/content/user/growth/` | ✅ 有 | ✅ 有 | `circle-13-growth-incentive` (EPIC-13) |

> **关键区分**：「圈子内成员成长」和「全局内容社区用户成长」共享 `/api/v1/content/user/growth/` 前缀，但前者必须带 `circleId`，后者不带 `circleId`。两者是完全独立的数据库表和业务逻辑。

---

### CRITICAL 问题 (归档前必须修复)

#### CRITICAL #1: CircleLevelController 中 `levelBenefit()` 和 `levelConfigs()` 属于错误的体系 [已澄清]

**文件**: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/controller/CircleLevelController.java:26-49`

**问题**: `levelBenefit()` 和 `levelConfigs()` 两个方法被放置在 `CircleLevelController`（圈子等级 Controller，API 前缀 `/api/v1/content/circle/growth/level/`）中，但它们实际属于**全局内容社区用户成长体系**（EPIC-03），不是圈子等级体系（EPIC-13）。

**证据链**:

| 证据项 | `levelBenefit()` | `levelConfigs()` | `getLevelInfo()`（正确） |
|--------|-----------------|------------------|--------------------------|
| 调用服务 | `IContentUserLevelBenefitService` | `IContentUserLevelConfigService` | `ICircleLevelService` |
| 数据库表 | `content_user_level_benefit_config` | `content_user_level_config` | `circle_level` |
| Flyway 迁移 | `V3.9.1_55`（EPIC-03） | `V3.9.1_55`（EPIC-03） | `V3.9.1_67`（EPIC-13） |
| 查询参数 | `userId`（无 circleId） | 无（纯配置查询） | `circleId` |
| 对应 Spec | `user-03`: "Level benefits and distribution weight" | `user-03`: "Growth value and level" | `circle-13`: "Circle Level Benefits" |
| 对应 PRD | EPIC-03 §3.3.3 等级标识特权与功能特权 | EPIC-03 §3.3.1 等级阈值配置 | EPIC-13 §4 圈子等级系统 |

**数据库表对应**:

```
content_user_level_config (V3.9.1_55, EPIC-03)
  └── level, level_name, growth_threshold, badge_style_key, enabled
  └── 对应 PRD EPIC-03 §3.3.1 "等级阈值配置化"

content_user_level_benefit_config (V3.9.1_55, EPIC-03)
  └── level, benefit_key, benefit_value, benefit_config_json, enabled
  └── 对应 PRD EPIC-03 §3.3.3 "上传大小上限/高清视频/话题上限/客服优先级"

circle_level (V3.9.1_67, EPIC-13)
  └── circle_id, level, growth_score, member_score, content_score, activity_score
  └── 对应 PRD EPIC-13 §4 "圈子等级系统"
```

**修复方案**: 从 `CircleLevelController` 中移除 `levelBenefit()` 和 `levelConfigs()` 两个方法，将它们迁移到 `content/user/` 包下新建的独立 Controller（如 `UserLevelController`，路径 `/api/v1/content/user/growth/level/`），作为 `user-03-badges-points-growth` 变更的一部分。

**修复涉及**:
1. 删除 `CircleLevelController.java:26-49` 的 `levelBenefit()`、`levelConfigs()`、`toLevelConfigVO()` 方法
2. 删除因此不再需要的 import 和字段声明
3. （可选，如需完整迁移）在 `content/user/controller/` 下新建 `UserLevelController`，将这两个方法迁入

---

#### CRITICAL #2: 成员等级名称、门槛、自动升级 — 三合一问题 [已澄清]

本问题实际包含 3 个子问题，根因相同：**缺少独立的 `MemberLevelEnum`，错误复用了 `CircleLevelEnum`**。

---

##### 子问题 2a: 等级名称错误

**文件**: `MemberGrowthServiceImpl.java:113`

```java
vo.setLevelName(CircleLevelEnum.ofLevel(growth.getLevel() != null ? growth.getLevel() : 1).getName());
```

`CircleLevelEnum.getName()` 返回的是**圈子等级名称**，不是成员等级名称。

**spec 定义的成员等级名称** (`member-experience/spec.md`):

| Level | 经验值门槛 | 成员等级名称（spec） | 实际输出（`CircleLevelEnum.getName()`） |
|-------|-----------|---------------------|--------------------------------------|
| L1 | 0 | **初来乍到** | ❌ 新芽圈 |
| L2 | 100 | **小有所成** | ❌ 活跃圈 |
| L3 | 300 | **圈内达人** | ❌ 优质圈 |
| L4 | 600 | **资深成员** | ❌ 热门圈 |
| L5 | 1000 | **圈中领袖** | ❌ 标杆圈 |

**涉及文件**: `MemberGrowthServiceImpl.java:113`

---

##### 子问题 2b: L5 经验值门槛与 spec 不一致

**文件**: `GrowthConstant.java:13`

```java
public static final int[] LEVEL_THRESHOLDS = {0, 100, 300, 600, 850};
```

| 体系 | L1 | L2 | L3 | L4 | L5 | 来源 |
|------|----|----|----|----|----|------|
| 圈子等级（成长分） | 0 | 100 | 300 | 600 | **850** | `CircleLevelEnum` |
| 成员等级（经验值） | 0 | 100 | 300 | 600 | **1000** | `spec` + `design.md D5.5` |

`LEVEL_THRESHOLDS[4] = 850` 来自 `CircleLevelEnum.L5.threshold`（圈子等级 L5 门槛），但被 `MemberGrowthServiceImpl.java:126-128` 用于成员等级进度计算：

```java
// MemberGrowthServiceImpl.java:126-128
int currentThreshold = GrowthConstant.LEVEL_THRESHOLDS[currentLevel - 1];  // L4: 600
int nextThreshold = GrowthConstant.LEVEL_THRESHOLDS[currentLevel];          // L5: 850 ← 应为 1000
```

导致成员在 850 经验值时进度显示 100%（应达到 1000 才满）。

**涉及文件**: `GrowthConstant.java:13`, `MemberGrowthServiceImpl.java:126-128`

---

##### 子问题 2c: 成员 `level` 字段从未根据经验值自动更新

**文件**: `MemberGrowthServiceImpl.java` 全量代码

代码追踪结果：

| 方法 | 行号 | 操作 | 是否更新 `level` 字段 |
|------|------|------|----------------------|
| `getOrCreateGrowth()` | 218 | 新建记录 | ✅ 初始化为 `1` |
| `addExperience()` | 42-72 | 增加经验值 | ❌ 未更新 |
| `revokeExperience()` | 76-103 | 回退经验值 | ❌ 未更新（正确，spec 要求不降级） |
| `getGrowthInfo()` | 106-179 | 读取展示 | ❌ 仅读取，不计算 |

**结果**: 成员的 `level` 字段始终为初始值 `1`，经验值增长后等级不会自动提升。

**spec 要求** (`member-experience/spec.md`):
> #### Scenario: Member level calculated from experience points
> - **WHEN** 成员的经验值达到某等级门槛
> - **THEN** 成员等级 SHALL 自动提升至对应等级

> #### Scenario: Level does not downgrade
> - **WHEN** 经验值因内容删除/违规被扣减至低于当前等级门槛
> - **THEN** 成员等级 SHALL NOT 降级，仅经验值数值减少

**涉及文件**: `MemberGrowthServiceImpl.java:42-72` (addExperience)

---

##### 统一修复方案

根因是 `CircleLevelEnum` 被两套体系共用。修复方案：创建独立的 `MemberLevelEnum` + 分离常量 + 在 `addExperience()` 中计算并更新等级。

**新建文件**: `enums/MemberLevelEnum.java`

```java
@Getter
@RequiredArgsConstructor
public enum MemberLevelEnum {
    L1(1, "初来乍到", 0),
    L2(2, "小有所成", 100),
    L3(3, "圈内达人", 300),
    L4(4, "资深成员", 600),
    L5(5, "圈中领袖", 1000);

    private final int level;
    private final String name;
    private final int threshold;

    /** 根据经验值计算当前等级 */
    public static MemberLevelEnum ofExp(int expPoints) {
        MemberLevelEnum result = L1;
        for (MemberLevelEnum e : values()) {
            if (expPoints >= e.getThreshold()) result = e;
        }
        return result;
    }

    /** 根据 level 数值查找（用于 DB 存储的 level 字段） */
    public static MemberLevelEnum ofLevel(int level) {
        for (MemberLevelEnum e : values()) {
            if (e.getLevel() == level) return e;
        }
        return L1;
    }
}
```

**修改文件**:

| 文件 | 修改内容 |
|------|---------|
| `GrowthConstant.java` | 新增 `MEMBER_LEVEL_THRESHOLDS = {0, 100, 300, 600, 1000}`，将 `LEVEL_THRESHOLDS` 重命名为 `CIRCLE_LEVEL_THRESHOLDS` 避免混淆 |
| `MemberGrowthServiceImpl.java:42-72` | `addExperience()` 末尾增加等级计算：`MemberLevelEnum newLevel = MemberLevelEnum.ofExp(growth.getExpPoints()); if (newLevel.getLevel() > growth.getLevel()) { growth.setLevel(newLevel.getLevel()); }` |
| `MemberGrowthServiceImpl.java:113` | 改为 `MemberLevelEnum.ofExp(growth.getExpPoints()).getName()` 或 `MemberLevelEnum.ofLevel(growth.getLevel()).getName()` |
| `MemberGrowthServiceImpl.java:126-128` | 改用 `GrowthConstant.MEMBER_LEVEL_THRESHOLDS` |
| `CircleLevelServiceImpl.java` | `LEVEL_THRESHOLDS` 引用改为 `CIRCLE_LEVEL_THRESHOLDS`（如适用） |

---

### WARNING 问题 (应该修复)

#### WARNING #1: MemberGrowthVO 字段命名与 spec 不一致 [已澄清]

**命名链路分析**:

| 层级 | 名称 | 说明 |
|------|------|------|
| 数据库 `circle_member_growth` | *(无此列)* | 计算字段，不存储 |
| Entity `CircleMemberGrowth` | *(无此字段)* | 计算字段，不在 Entity 中 |
| 常量 `GrowthConstant` | `LEVEL_THRESHOLDS` | "thresholds" 是领域术语 |
| `CircleLevelVO` | `nextLevelThreshold` | 圈子等级 VO 已使用 Threshold 命名 |
| `MemberGrowthVO` (代码) | `nextLevelThreshold` | 当前实现 |
| `design.md` (L44) | `nextLevelThreshold` | 设计文档与代码一致 |
| `design.md` (L150) | `nextLevelExp` | 设计文档内部笔误（Entity 并无此字段，写错了位置） |
| `spec` (L20, L96, L100) | `nextLevelExp` | 唯一不一致的地方 |

**结论: 保持代码 `nextLevelThreshold`，修正 spec**。

理由：
1. **零代码变更** — Entity 和数据库都没有此字段，改代码无意义
2. **`Threshold` 是领域一致术语** — `LEVEL_THRESHOLDS`（常量）、`CircleLevelEnum.threshold`（枚举字段）、`CircleLevelVO.nextLevelThreshold`（圈子VO）全部使用 Threshold
3. **`Exp` 缩写不匹配任何 Entity/DB 名称** — 数据库用 `exp_points`，Entity 用 `expPoints`，从未单独使用 `Exp`
4. **spec 中文描述就是「门槛」** — "下一等级门槛（nextLevelExp）"，中文与英文不匹配，应统一为 `nextLevelThreshold`

**修复**: 将 `member-experience/spec.md` 中 3 处 `nextLevelExp` 改为 `nextLevelThreshold`。代码不变。

---

#### WARNING #2: `revokeExperience()` 不降级逻辑正确，已整合到 CRITICAL #2 修复中

`MemberGrowthServiceImpl.revokeExperience()` 正确实现了经验值扣减但不降级（只减 expPoints，level 保持不变），与 spec「Level does not downgrade」一致。此逻辑与 CRITICAL #2 子问题 2c 中 `addExperience()` 的修复互补——`addExperience()` 升级，`revokeExperience()` 不降级。统一在 CRITICAL #2 修复方案中处理。

---

### SUGGESTION 问题 (建议改进)

#### SUGGESTION #1: plan.md 与 tasks.md 的 Flyway 版本号不一致

- `plan.md`: 使用 `V3.9.1_63`
- `tasks.md` 和实际文件: 使用 `V3.9.1_67`

版本号在实现过程中从 63 演进到 67 是正常的，但 `plan.md` 未同步更新。

**建议**: 将 `plan.md` 中的版本号更新为 `V3.9.1_67` 以保持文档一致性。

---

#### SUGGESTION #2: 排行榜周期常量硬编码

`LeaderboardServiceImpl.refreshSnapshot()` 硬编码了三个周期 `{"WEEK", "MONTH", "ALL"}`，与排行榜 spec 的 `Leaderboard Period` requirement 一致。但如果未来添加新周期，需要修改多处。

**建议**: 将周期常量提取到 `GrowthConstant` 或枚举中。

---

#### SUGGESTION #3: 前端 change 待跟进

`circle-13-growth-incentive-frontend`（0/41 tasks）尚未开始。后端 change 归档后可独立推进前端实现。

---

### 修复清单（供下一步代码变更参考）

按修复顺序排列：

1. **[CRITICAL #2 子问题 2b+2c 联动]** 新建 `MemberLevelEnum.java`，含正确名称和门槛 {0, 100, 300, 600, 1000}
2. **[CRITICAL #2 子问题 2b]** `GrowthConstant` 新增 `MEMBER_LEVEL_THRESHOLDS = {0, 100, 300, 600, 1000}`，将 `LEVEL_THRESHOLDS` 重命名为 `CIRCLE_LEVEL_THRESHOLDS`
3. **[CRITICAL #2 子问题 2c]** `MemberGrowthServiceImpl.addExperience()` 末尾增加等级自动升级逻辑
4. **[CRITICAL #2 子问题 2a]** `MemberGrowthServiceImpl.getGrowthInfo()` 改用 `MemberLevelEnum` 获取等级名称
5. **[CRITICAL #2 子问题 2b]** `MemberGrowthServiceImpl.getGrowthInfo()` 进度计算改用 `MEMBER_LEVEL_THRESHOLDS`
6. **[CRITICAL #1]** `CircleLevelController` 移除 `levelBenefit()`、`levelConfigs()`、`toLevelConfigVO()` 及关联 import
7. **[CRITICAL #1 可选]** 在 `content/user/controller/` 下新建 `UserLevelController` 迁入上述方法
8. **[WARNING #1]** 将 `member-experience/spec.md` 中 3 处 `nextLevelExp` 改为 `nextLevelThreshold`，代码不变
9. **[SUGGESTION #1]** 同步 `plan.md` 中的 Flyway 版本号
10. 运行全量单元测试确认修复通过

---

### 最终评估

**2 个 CRITICAL 问题必须修复后才能归档。**（CRITICAL #3 已并入 CRITICAL #2 统一修复）

修复后验证项：
- [ ] `MemberGrowthVO.levelName` 输出「初来乍到」而非「新芽圈」
- [ ] 成员 L5 门槛为 1000 经验值
- [ ] `addExperience()` 在经验值跨过门槛时自动更新 `level` 字段
- [ ] `revokeExperience()` 扣减经验值但不降级
- [ ] `CircleLevelController` 仅包含圈子等级相关方法，编译通过
- [ ] 全量单元测试通过
