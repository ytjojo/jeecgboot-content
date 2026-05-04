---
name: jeecg-codegen
description: Generates or adjusts JeecgBoot CRUD modules. Invoke when user asks for 代码生成、创建模块、建表、加字段、修改字段、删除字段, or wants code from an existing table.
---

# JeecgBoot 代码生成技能

将自然语言需求转换为 JeecgBoot CRUD 代码、Flyway SQL 和前端页面，也适用于已有模块的字段级增量修改。

## 何时使用

- 用户要求生成 JeecgBoot 单表、树表、一对多模块。
- 用户给了表名，希望按现有表反向生成代码。
- 用户要求给已有模块加字段、删字段、改字段。
- 用户提到 `代码生成`、`创建模块`、`建表`、`加字段`、`修改字段`、`删除字段`。

## 核心硬规则

- 主数据遵循先查后建：字典、角色、用户、部门等优先复用，必要时使用 `jeecg-system` 技能处理。
- 执行数据库查询或 SQL 前，必须先确认目标数据库名，不能自行假设。
- 现有表场景必须先读取真实 DDL，再推导主键、系统字段、控件和模板。
- 新建表场景必须先给出结构摘要，用户确认后再生成文件或 SQL。
- 增量修改必须先扫描现有代码并展示修改摘要，确认后再落盘。
- 正式生成前必须先读取 `codegen-reference.md`，再按索引进入对应模板文件。

## 场景分流

### Step 1：判断操作类型

- 现有表全量生成：用户给了真实表名，或明确说“按这个表生成代码”。
- 新建表全量生成：用户用自然语言描述业务和字段，希望直接生成模块。
- 增量修改：用户说“加字段 / 删字段 / 改字段 / 给某模块加一个字段”。

### Step 2：读取对应文档

- 现有表全量生成：读取 `docs/scenario-existing-table.md`
- 新建表全量生成：读取 `docs/scenario-new-table.md`
- 增量修改：读取 `docs/scenario-incremental-change.md`

### Step 3：补充读取参考文档

- 单表 / 树表 / 一对多判断：读取 `docs/reference-table-patterns.md`
- `vue3` / `vue3Native` 判断：读取 `docs/reference-frontend-style.md`
- 字段、字典、控件映射：读取 `docs/reference-field-mapping.md`

## 标准执行顺序

1. 判断场景并读取对应引用文档。
2. 补齐必要选项：后端模块、前端风格、视图目录、是否读取系统字典。
3. 输出生成摘要或修改摘要，等待用户确认。
4. 读取 `codegen-reference.md` 并进入对应模板文件生成或修改代码。
5. 输出文件清单、SQL 处理结果和后续动作。

## 文档地图

| 文档 | 适用场景 |
|---|---|
| `docs/scenario-existing-table.md` | 现有表反向生成 |
| `docs/scenario-new-table.md` | 新建表全量生成 |
| `docs/scenario-incremental-change.md` | 已有模块增量修改 |
| `docs/reference-table-patterns.md` | 单表、树表、一对多与布局判断 |
| `docs/reference-frontend-style.md` | `vue3` 与 `vue3Native` 差异 |
| `docs/reference-field-mapping.md` | 字段推导、字典匹配、控件映射 |
| `codegen-reference.md` | 模板总索引，生成前必须读取 |
| `docs/skill-usage-guide.md` | 面向人的使用说明和示例 |

## 输出要求

- 生成前明确说明识别出的场景、表形态和前端风格。
- 摘要至少包含表名、字段、控件、字典、目标模块和 SQL 计划。
- 结果里列出所有新增或修改的文件路径。
- 本地数据库自动执行菜单 SQL 时，明确标注执行成功或回退为手工执行。
