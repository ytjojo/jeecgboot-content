# apply-retrospective 变更日志

## 2026-06-16 — v2: 防理性化机制 (Code Review: BLOCK → 全机制闭环)

### 动机

Code Review（2026-06-10）指出技能在 4 个维度存在防护缺失：防跳过（FLAG）、防敷衍（FLAG）、防越界（FLAG）、理性化漏洞（**BLOCK**）。核心问题：技能约束仅靠声明 + 自我报告式 checklist，缺少 writing-skills 框架中标准的 Rationalization Table / Red Flags / Spirit vs Letter 三层防护。

### 变更清单

**SKILL.md** (+308 词, 从 155 行 → 227 行)

- `定位` §: 新增 Spirit vs Letter 声明（`字面合规等于精神合规`）
- `触发方式` §: 无变更标识时先追问 commit range
- `通用降级模式` §: 输出目录强制 `docs/review/`，禁止临时路径
- `Step 1` §: 新增 Active hours / Subagent dispatches 数据获取指引
- `Step 2 §4` §: 禁止模糊理由从 3 个扩展到 9 个；新增 `n/a — skip justified` 选项
- `跳过条件` §: 单 OR → AND 组合保护；新增业务文件排除；强制附带 `git diff --stat`
- `边界红线` § (新增): 6 领域不可评价表 + 12 个越界关键词语义扫描
- `防理性化机制` § (新增): 7 条 Rationalization Table + 6 条 Red Flags 停止规则
- `约束清单` §: 新增第 8 条（字面合规 = 精神合规）
- `定位` §: 「不关注」→「禁止评价」，措辞力度增强

**checklist.md** (+122 词, 从 60 行 → 87 行)

- `复盘前` §: 输出目录约束对齐
- `§1 Wins` §: evidence 必须可定位到具体文件/行
- `§2 Misses` §: 零 🔴🟡 自问 3 条机制；📌 项上限 3 条
- `§4 Skill` §: Why 禁止理由清单交叉验证；`n/a` 选项验证
- `§5 Surprises` §: `(none observed)` 前置检查
- `越界扫描` § (新增): 全文关键词扫描 + 过程视角改写确认
- `理性化自查` § (新增): 借口表对照 + Red Flags 对照 + 零问题自问

**template.md** (轻量修改)

- `§0` §: Diff size 示例格式；commit chain 指令格式
- `§1` §: 正面示例
- `§4` §: 静态技能表替换为动态占位符；新增 `n/a — skip justified` 选项
- `§3` §: 降级模式措辞对齐

**新增文件**

- `verification-report.md`: 3 场景压力测试报告，记录 Agent 行为和机制拦截证据

### 压力测试结果

| # | 场景 | 压力 | 目标机制 | 结果 |
|---|------|------|---------|------|
| 1 | "太简单" | 时间紧 + 不值得 | 跳过条件 + Rationalization Table | PASS |
| 2 | "零问题" | 主观满意 + 空洞表扬 | Red Flags + 自问 3 条 | PASS |
| 3 | "越界" | 真诚认为合法 | 关键词扫描 + 边界红线表 | PASS |

### 已知限制

- Rationalization #3/#7 和 Red Flag "none observed > 2" 未在压力测试中触发（场景未覆盖），但机制本身存在
- 越界关键词扫描依赖全文搜索，中文分词可能产生漏报（如 "方法过长" vs "函数过长"）
