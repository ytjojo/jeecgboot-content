# 文档腐化修正 — 过程复盘

> **复盘日期**: 2026-06-25
> **关联变更**: circle-13-growth-incentive + circle-13-growth-incentive-frontend
> **参考工作流**: `docs/prompts/documentation-rot-fix-workflow.md`
> **修正范围**: 10 个文档 + 4 个 spec + 2 个 PRD + 6 份 review 报告归档

---

## 一、背景

圈子成长激励体系（EPIC-13）的后端代码已完成多轮迭代，VO 字段大幅扩展。但前端 PRD、design.md、4 个 spec 文件中的 API 路径和 VO 字段描述停留在后端初版状态——这被称为**文档腐化**。

最致命的腐化类型是 **"暂不支持"错误声明**：spec 和 design.md 中声称后端未提供某字段、需前端降级/硬编码，但实际后端已完整提供。若前端按此文档实现，会导致功能缺失和多余降级代码。

## 二、做了什么

### 2.1 第一轮：按错误流程（已被推翻）

**做法**：只读工作流文档中的"已知错误对照表"，直接套用修改文档。

**结果**：恰好正确（因为对照表本身是准确的），但**方法论错误**——对照表可能过时。

**教训**：工作流文档再三强调"本文档中的表格仅作交叉验证用途，必须重新读取实际源码"，但仍被当作补丁执行。

### 2.2 第二轮：按正确流程重做

严格遵循工作流规范的 4 步顺序：

```
Step 1: 读 PRD 需求（2 份）
Step 2: 读 Flyway DDL SQL（1 份，6 张表）
Step 3: 读 Controller 源码（4 个）+ VO 源码（6 个）
Step 4: 建立 Truth Source 对照表 → 逐文件修正
```

**产出**：

| 文件 | 修正内容 |
|------|---------|
| 前端 PRD | API 路径 3 处、4 个 TypeScript interface 全部字段名重写 |
| 前端 design.md | D7/D9/D10 决策重写、VO 字段映射表补全、Q4/Risks 更新 |
| 前端 4 个 spec | 删除 20 处"暂不支持/降级/硬编码"错误声明 |
| 前端 tasks.md | API 函数 4→7、Promise.all 并发数修正 |
| 后端 design.md | Flyway 版本号 V3.9.1_63→67、VO 字段列表补全 |
| 后端 tasks.md | Flyway 版本号修正 |
| 6 份 review 报告 | 修正 benefits 类型错误（List\<String\>→List\<CircleBenefitVO\>）、添加状态标注、归档 |

### 2.3 第三轮：Review 报告清理

- 修正 review 报告中与实际代码矛盾的描述
- 综合 6 份报告提取未解决问题 → `PENDING-ISSUES.md`
- 已解决报告归档到 `archive-review/`，避免 agent 重复读取

## 三、避坑指南

### 坑 1：不看实际源码，直接套用文档中的对照表

**表现**：工作流文档（如 `documentation-rot-fix-workflow.md`）为了方便读者理解，通常会附上"已知错误对照表"。但代码持续迭代，表格可能已过时。

**后果**：对照表恰好准确时没问题，但一旦过时就会写出新错误。

**正确做法**：
```
❌ 读工作流文档的对照表 → 直接套用修改
✅ 读 PRD → 读 DDL → 读 Controller/VO → 自己建表 → 用工作流文档的表格交叉验证
```

### 坑 2：混淆三套成长体系

**表现**：全局用户成长（无 circleId）、圈子等级（只有 circleId）、圈子内成员成长（circleId+userId）三套体系的 API 前缀相似（`/user/growth/` 和 `/circle/growth/`），极易混淆。

**后果**：把圈子等级 API 路径写成 `/user/growth/level/`，前端请求时 404。

**正确做法**：**看表不看路径**——`circle_level` 表 = 圈子等级体系，`circle_member_growth` 表 = 成员成长体系，`content_user_*` 表 = 全局体系。每次修正路径前先确认 Controller 实际 `@RequestMapping`。

### 坑 3：凭"常识"判断字段是否存在

**表现**：看到 PRD 描述的字段名（如 `memberGap`），就假设 VO 里也是这样命名。不读源码就去改文档。

**后果**：PRD 的 `memberGap`（差距）和实际 VO 的 `memberScore`（得分）是完全不同含义的字段，错误映射导致前端理解偏差。

**正确做法**：**每个字段必须从源码中一一对应**：
```java
// 实际 VO 源码
@Schema(description = "成员规模得分")
private Integer memberScore;  // ← 这是"得分"，不是"差距"
```
差距信息在 `nextLevelConditions[].gap` 中。

### 坑 4：忽视 Open Questions 状态变更

**表现**：design.md 的 Q1/Q3/Q4/Q5 在原报告创建时是问题，但后端已实现给出了答案。文档未更新。

**后果**：后续读者看到"待确认"会浪费时间重复确认或做错误假设。

**正确做法**：修正文档时同步更新 Open Questions 状态（已确认 → 标注结论，仍待确认 → 保留）。

### 坑 5：spec 中的降级策略未随代码迭代更新

**表现**：spec 写了"后端未提供 X 字段，前端硬编码"，后来后端补充了 X 字段，但 spec 未更新。

**后果**：这是**最严重的腐化类型**。前端按 spec 实现降级逻辑 → 功能缺失 → 后期需要返工。本轮修正中 20 处"暂不支持"全是这种。

**正确做法**：
- 每次后端 VO 新增字段后，**必须**同步更新前端 design.md/specs
- spec 中每个"暂不支持"声明都应该带条件（"截至 X 日期后端未提供"），方便后续 grep 定位

### 坑 6：报告文件堆积不归档

**表现**：多轮 review 产生了 6 份报告（verify-report、review-report、spec-review-report、verification-review、backend-issues），部分报告结论已被后续代码迭代推翻。

**后果**：Agent 读取时消耗 token，且可能将过时结论当作当前状态。本轮归档省掉了 ~800 行/token 的无效读取。

**正确做法**：
- 新一轮修正完成后，旧 review 报告移入 `archive-review/`
- 综合所有报告生成一份 `PENDING-ISSUES.md`
- 已解决的标注确认，未解决的集中追踪

### 坑 7：Edit 工具对特殊字符的匹配陷阱

**表现**：文件中有 tab 缩进的 Java 代码块，Edit 工具的 `old_string` 传 `	List<String>` 匹配不到。

**原因**：tab 字符在不同渲染环境下可能被转换为空格。

**正确做法**：
- 先 `Read` 目标行确认精确文本（不要凭记忆）
- 对 tab 缩进的代码块，优先用 `sed` 兜底
- `grep -rn` 全量搜索验证修改完整性

## 四、有效模式

### 4.1 Truth Source 优先级

```
PRD 需求描述 + 数据库 DDL     ← 第一真理（不可修改）
        ↓
Controller @RequestMapping   ← API 路径真相
VO 类字段                     ← 出参真相
        ↓
openspec 文档                  ← 待修正对象（对齐上面所有来源）
```

### 4.2 修正顺序

先后端后前端，先 API 路径后字段映射后降级策略：

```
1. 后端 design.md / tasks.md     ← 版本号、表名
2. 后端 specs/                    ← API 路径
3. 前端 PRD                       ← API 路径 + interface
4. 前端 design.md                 ← 决策 + 字段映射表 + 降级策略
5. 前端 specs/                    ← "暂不支持"声明逐条验证
6. 前端 tasks.md                  ← 函数数量、文件路径
7. 旧报告                         ← 归档 + 生成 PENDING-ISSUES
```

### 4.3 验证清单

每次修正完毕执行：

```
□ grep -rn "List<String> benefits" → 应返回空（实际是 List<CircleBenefitVO>）
□ grep -rn "/user/growth/level"    → 应仅在正确上下文中出现
□ grep -rn "暂不支持\|暂不展示\|硬编码" → 逐条验证仍有依据
□ grep -rn "V3.9.1_6[0-6]"        → 版本号应全为 V3.9.1_67
□ grep -rn "nextLevelExp"          → 应全为 nextLevelThreshold
```

## 五、关键数据

| 指标 | 数值 |
|------|------|
| 修正文件数 | 16 个 |
| 删除"暂不支持"错误声明 | 20 处 |
| 修正 API 路径 | 3 处 |
| 修正 VO 字段名 | 60+ 处（4 个 interface 完全重写） |
| 修正版本号 | 8 处 |
| 修正 benefits 类型错误 | 12 处（`List<String>` → `List<CircleBenefitVO>`） |
| 归档报告 | 6 份 |
| 提取未解决问题 | 7 个（见 PENDING-ISSUES.md） |
| 总 commit | 3 个 |

## 六、后续改进建议

1. **CI 检查**：在 CI 中加入 spec vs Controller 的路径一致性检查脚本
2. **VO 变更通知**：后端 VO 字段变更时，自动在 frontend change 下生成 issue
3. **"暂不支持"标记规范**：统一为 `> ⚠️ 截至 YYYY-MM-DD，后端未提供 X 字段`，方便 grep 和定期审查
4. **review 报告生命周期**：明确 review 报告的归档时机（代码变更后，原有 review 即视为过期）
