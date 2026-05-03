# Content User PRD Backend Coverage Audit Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 生成一份 `内容社区用户域 PRD` 对 `content/user` Java 后端的覆盖审计报告，明确已实现、部分实现、未实现与测试缺口，并给出后续分步实现优先级。

**Architecture:** 先基于已批准的审计设计文档构建统一覆盖矩阵模板，再按五个独立子域执行并行核查，最后合并结论形成总报告。审计阶段不直接改业务代码，只补充审计与计划文档，为后续按子域实施提供输入。

**Tech Stack:** Markdown, PRD 文档, Java Spring Boot source tree, JUnit/MockMvc test tree, Trae subagent workflow

---

### Task 1: 固化审计输入与模板

**Files:**
- Read: `docs/requirements/prd/内容社区-用户域-PRD.md`
- Read: `docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-design.md`
- Create: `docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md`

- [ ] **Step 1: 创建覆盖报告骨架**

在 `docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md` 写入以下初始结构：

```md
# 内容社区用户域 PRD 后端覆盖审计报告

- 审计范围：`docs/requirements/prd/内容社区-用户域-PRD.md`
- 代码范围：`jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user`
- 测试范围：`jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user`
- 审计依据：`docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-design.md`

## 1. 总结论

## 2. 总览矩阵

| 子域 | 已实现 | 部分实现 | 未实现 | 待确认 | 测试缺口 |
| --- | --- | --- | --- | --- | --- |

## 3. 账号安全域

## 4. 资料与隐私域

## 5. 成长激励域

## 6. 关系订阅域

## 7. 支持治理域

## 8. 未实现需求优先级

## 9. 后续实现建议
```

- [ ] **Step 2: 核对模板字段与设计文档一致**

人工检查以下字段已被覆盖：

```text
PRD 编号
需求摘要
子域
实现状态
代码证据
测试证据
缺口说明
实现建议
优先级
```

- [ ] **Step 3: 保存模板并准备审计**

Run:

```bash
test -f /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md && echo OK
```

Expected: 输出 `OK`

- [ ] **Step 4: Commit**

```bash
git add docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md
git commit -m "docs: add user domain backend coverage report scaffold"
```

### Task 2: 分域执行并行审计

**Files:**
- Read: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/**/*.java`
- Read: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/**/*.java`
- Modify: `docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md`

- [ ] **Step 1: 派发账号安全域子代理**

向子代理提供以下任务说明：

```text
审计 PRD 用户故事 6-26 在 content/user Java 后端中的实现覆盖。
只看账号安全相关 controller/service/entity/req/vo/test。
输出：已实现、部分实现、未实现、待确认、测试缺口。
每条结论必须附文件路径。
不要改代码。
```

- [ ] **Step 2: 派发资料与隐私域子代理**

向子代理提供以下任务说明：

```text
审计 PRD 用户故事 27-33、74-80 在 content/user Java 后端中的实现覆盖。
只看资料、隐私、通知、可见性相关 controller/service/entity/req/vo/test。
输出：已实现、部分实现、未实现、待确认、测试缺口。
每条结论必须附文件路径。
不要改代码。
```

- [ ] **Step 3: 派发成长激励域子代理**

向子代理提供以下任务说明：

```text
审计 PRD 用户故事 34-46、81-87 在 content/user Java 后端中的实现覆盖。
只看成长、积分、勋章相关 controller/service/entity/req/vo/test。
输出：已实现、部分实现、未实现、待确认、测试缺口。
每条结论必须附文件路径。
不要改代码。
```

- [ ] **Step 4: 派发关系订阅域子代理**

向子代理提供以下任务说明：

```text
审计 PRD 用户故事 47-73、88-94 在 content/user Java 后端中的实现覆盖。
只看关系、关注、分组、拉黑、屏蔽、订阅相关 controller/service/entity/req/vo/test。
输出：已实现、部分实现、未实现、待确认、测试缺口。
每条结论必须附文件路径。
不要改代码。
```

- [ ] **Step 5: 派发支持治理域子代理**

向子代理提供以下任务说明：

```text
审计 PRD 用户故事 95-113 在 content/user Java 后端中的实现覆盖。
只看举报、申诉、帮助中心、客服、状态治理、审计相关 controller/service/entity/req/vo/test。
输出：已实现、部分实现、未实现、待确认、测试缺口。
每条结论必须附文件路径。
不要改代码。
```

- [ ] **Step 6: 收集五个子代理结果并去重**

合并时使用以下规则：

```text
同一需求若不同子代理结论冲突，以“更保守”的状态为准。
已实现 < 部分实现 < 待确认 < 未实现
测试缺口独立记录，不覆盖实现状态。
```

- [ ] **Step 7: 把分域结论写入覆盖报告**

每个子域至少补全：

```md
### 已实现
- PRD-xx：需求摘要
  - 代码证据：...
  - 测试证据：...

### 部分实现

### 未实现

### 待确认

### 测试缺口
```

- [ ] **Step 8: Commit**

```bash
git add docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md
git commit -m "docs: audit user domain backend coverage by subdomains"
```

### Task 3: 汇总优先级与分步实施建议

**Files:**
- Modify: `docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md`

- [ ] **Step 1: 填写总览矩阵**

按五个子域补全表格：

```md
| 子域 | 已实现 | 部分实现 | 未实现 | 待确认 | 测试缺口 |
| --- | --- | --- | --- | --- | --- |
| 账号安全域 | x | x | x | x | x |
| 资料与隐私域 | x | x | x | x | x |
| 成长激励域 | x | x | x | x | x |
| 关系订阅域 | x | x | x | x | x |
| 支持治理域 | x | x | x | x | x |
```

- [ ] **Step 2: 生成未实现需求优先级列表**

在报告中写入以下结构：

```md
## 8. 未实现需求优先级

### P0
- PRD-xx：...

### P1
- PRD-xx：...

### P2
- PRD-xx：...
```

- [ ] **Step 3: 生成后续实现建议**

在报告中写入以下结构：

```md
## 9. 后续实现建议

1. 第一阶段：支持治理域补齐
2. 第二阶段：账号安全域补齐
3. 第三阶段：关系订阅域补齐
4. 第四阶段：资料与隐私域补齐
5. 第五阶段：成长激励域补齐
```

- [ ] **Step 4: 自检报告完整性**

逐项核对：

```text
是否五个子域都有内容
是否每条结论有代码证据
是否区分实现状态与测试缺口
是否给出优先级
是否给出下一阶段建议
```

- [ ] **Step 5: Commit**

```bash
git add docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md
git commit -m "docs: summarize user domain backend audit priorities"
```

### Task 4: 产出首个实现子域的实施入口

**Files:**
- Read: `docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md`
- Create: `docs/superpowers/plans/2026-05-02-content-user-support-governance-gap-plan.md`

- [ ] **Step 1: 从报告中选择最高优先级且相对闭环的子域**

选择规则：

```text
优先 P0
优先已有部分实现基础
优先可在 content/user 目录内闭环
优先回归范围可控
```

- [ ] **Step 2: 为首个子域创建单独实现计划骨架**

在 `docs/superpowers/plans/2026-05-02-content-user-support-governance-gap-plan.md` 中写入以下结构：

```md
# Content User Support Governance Gap Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 补齐审计报告中支持治理域的 P0/P1 缺口

**Architecture:** 基于现有 support/governance controller 与 service 做最小增量实现，不扩展到无关模块。

**Tech Stack:** Spring Boot 3, JeecgBoot, MyBatis-Plus, JUnit 5, Mockito, MockMvc
```

- [ ] **Step 3: 记录下一阶段启动条件**

在计划文件末尾写入：

```md
## 启动条件

- 覆盖审计报告已确认
- 目标缺口已锁定
- 未提交本地改动影响已确认
```

- [ ] **Step 4: Commit**

```bash
git add docs/superpowers/plans/2026-05-02-content-user-support-governance-gap-plan.md
git commit -m "docs: add first gap implementation plan scaffold"
```
