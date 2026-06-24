# 圈子成长 API 路径约定

> **硬约束**: 圈子成长 (`/circle/growth/`) 和 用户成长 (`/user/growth/`) 是两套独立体系，API 路径前缀必须体现数据主体的差异。本条规则优先级高于所有 change 文档中的路径描述。

## 判断规则

```
接口是否需要 userId 作为必需参数?
  ├── 是 → 数据主体是「用户」 → /user/growth/
  └── 否 → 返回的数据是否与具体用户无关（所有人看到相同结果）?
            ├── 是 → 数据主体是「圈子」 → /circle/growth/
            └── 否（隐式依赖当前登录用户） → /user/growth/
```

**核心原则**: RESTful 路径前缀 = 数据主体（resource owner），不是"跟圈子和成长有关就用 circle"。

## 路径分配

### `/api/v1/content/circle/growth/` — 圈子成长

数据主体是**圈子**。查询参数仅需 `circleId`，返回的数据对所有查看者相同。

| Controller | 路径 | 参数 | 说明 |
|-----------|------|------|------|
| `CircleLevelController` | `/circle/growth/level/info` | `circleId` | 圈子等级信息（圈子属性） |
| `CircleLevelController` | `/circle/growth/level/benefit` | `circleId` | 圈子等级权益摘要 |
| `CircleLevelController` | `/circle/growth/level/config` | 无 | 等级门槛配置（全局） |

### `/api/v1/content/user/growth/` — 用户成长（成员成长）

数据主体是**用户/成员**。查询参数必须含 `userId`（或隐式从当前登录用户获取），返回的数据因人而异。

| Controller | 路径 | 参数 | 说明 |
|-----------|------|------|------|
| `MemberGrowthController` | `/user/growth/info` | `circleId`, `userId` | 成员经验值/贡献值/等级 |
| `MemberGrowthController` | `/user/growth/participation` | `circleId`, `userId` | 连续参与天数 |
| `AchievementController` | `/user/growth/achievement/list` | `circleId`, `userId` | 成员徽章列表 |
| `LeaderboardController` | `/user/growth/leaderboard` | `circleId`, `dimension`, `period`, `currentUserId` | 排行榜（用户排名） |

## 违规检查清单

做以下任一操作时，必须对照本规范：

- [ ] 新增 Controller：确认数据主体，选择正确的前缀
- [ ] 修改 `@RequestMapping` 路径：确认是否跨体系迁移
- [ ] 前端新增 API 调用：确认路径前缀与后端一致
- [ ] change 文档中写 API 路径：用本规范做最终校验

**常见错误**:
1. ❌ 看到"圈子"字样就把路径归入 `/circle/growth/` — 应看数据主体
2. ❌ 看到方法参数有 `circleId` 就归入 `/circle/growth/` — `circleId` 只是过滤条件，不决定主体
3. ❌ 凭 Controller 类名判断前缀 — 应以接口参数和数据返回内容为准

## 数据模型归属

| 实体 | 主体类型 | 说明 |
|------|---------|------|
| `CircleLevelVO` | 圈子 | growthScore 是圈子的聚合指标 |
| `MemberGrowthVO` | 用户 | expPoints/contributionPoints 是个人的累计值 |
| `AchievementVO` | 用户 | earned/earnedDate/progress 是个人维度的 |
| `LeaderboardEntryVO` | 用户 | 排名的核心是 userId→score 映射 |

## 两套等级体系

| 维度 | 圈子等级 (Circle Level) | 成员等级 (Member Level) |
|------|------------------------|------------------------|
| 主体 | 圈子 | 用户/成员 |
| API 前缀 | `/circle/growth/` | `/user/growth/` |
| 查询参数 | `circleId` | `circleId` + `userId` |
| 计算依据 | 成员规模 + 内容贡献 + 活跃互动 聚合 | 个人经验值累计 |
| 等级门槛 | L1(0)→L2(100)→L3(300)→L4(600)→L5(850) | L1(0)→L2(100)→L3(300)→L4(600)→L5(1000) |
| 更新频率 | 定时任务 30 分钟 | 行为触发实时 |
| 展示位置 | 圈子详情页、圈子列表卡片 | 个人成长信息页 |
| 是否降级 | 不降级 | 不降级（经验值可扣减，等级不变） |

## 变更记录

- 2026-06-25: 初始版本。从 circle-13-growth-incentive 前后端对接中总结，解决多次出现的 API 路径混淆问题。
