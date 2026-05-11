# jeecg-codegen 使用指南

## 这份文档怎么用

- 如果你想知道“应该怎么提需求”，先看 `按提问方式分流`。
- 如果你已经知道自己是什么场景，直接跳到对应示例。
- 如果你要追模板细节，再进入后面的文档入口。

## 按提问方式分流

### 1. 你已经有真实表

适合这样问：

```text
根据表 biz_order 生成完整 JeecgBoot 代码，前端用 vue3。
```

这类提问会进入：

- `docs/scenario-existing-table.md`
- `codegen-reference.md`
- 视情况再进入单表、树表或一对多模板

### 2. 你还没有表，只想描述业务

适合这样问：

```text
帮我生成一个商品管理模块，包含商品名称、价格、库存、状态、图片、描述。
```

这类提问会进入：

- `docs/scenario-new-table.md`
- `docs/reference-field-mapping.md`
- `codegen-reference.md`

### 3. 你只想给现有模块加字段、删字段、改字段

适合这样问：

```text
给商品管理加一个备注字段，并同步修改前后端和 Flyway SQL。
```

这类提问会进入：

- `docs/scenario-incremental-change.md`
- `docs/reference-template-incremental-change.md`

### 4. 你要做一对多

适合这样问：

```text
生成一个采购单模块，主表是采购单，子表是采购明细，前端用 ERP 风格。
```

这类提问最好额外说明：

- 是默认布局、ERP 风格、内嵌子表，还是 `vue3Native`
- 子表有几张
- 是否包含一对一子表

会重点进入：

- `docs/reference-table-patterns.md`
- `docs/reference-template-master-detail.md`

### 5. 你明确要 vue3Native

适合这样问：

```text
根据表 biz_goods 生成 vue3Native 风格页面，包含 Form.vue。
```

这类提问会重点进入：

- `docs/reference-frontend-style.md`
- 单表时读 `docs/reference-template-single-table.md`
- 一对多时读 `docs/reference-template-master-detail.md`

## 适用范围

这个技能用于 JeecgBoot 代码生成与增量修改，覆盖三类核心场景：

- 现有表反向生成
- 新建表全量生成
- 已有模块增量改字段

## 文档入口

- 主技能入口：`SKILL.md`
- 现有表场景：`docs/scenario-existing-table.md`
- 新建表场景：`docs/scenario-new-table.md`
- 增量修改场景：`docs/scenario-incremental-change.md`
- 表类型与布局：`docs/reference-table-patterns.md`
- 前端风格：`docs/reference-frontend-style.md`
- 字段与控件：`docs/reference-field-mapping.md`
- 模板总索引：`codegen-reference.md`
- 单表模板：`docs/reference-template-single-table.md`
- 树表模板：`docs/reference-template-tree-table.md`
- 一对多模板：`docs/reference-template-master-detail.md`
- 字段与 DDL 模板：`docs/reference-template-field-ddl.md`
- 增量修改模板：`docs/reference-template-incremental-change.md`

## 推荐提问方式

### 1. 现有表反向生成

```text
根据表 biz_order 生成完整 JeecgBoot 代码，前端用 vue3。
```

补充得更完整的问法：

```text
根据表 biz_order 生成完整 JeecgBoot 代码，目标模块是 jeecg-system-biz，前端用 vue3，需要自动匹配字典。
```

### 2. 新建表全量生成

```text
帮我生成一个商品管理模块，包含商品名称、价格、库存、状态、图片、描述。
```

补充得更完整的问法：

```text
帮我生成一个商品管理模块，表名 biz_goods，前端用 vue3Native，字段包含商品名称、价格、库存、状态、图片、描述。
```

### 3. 增量修改

```text
给商品管理加一个备注字段，并同步修改前后端和 Flyway SQL。
```

补充得更完整的问法：

```text
给 biz_goods 增加备注字段，类型是 varchar(200)，前端用 InputTextArea，并生成 ALTER TABLE Flyway SQL。
```

### 4. 一对多

```text
生成一个采购单模块，主表字段有单号、供应商、日期，子表字段有商品、数量、单价、小计，前端用默认一对多布局。
```

### 5. vue3Native

```text
根据表 biz_goods 生成 vue3Native 风格的完整 CRUD，包含 List.vue、Modal.vue、Form.vue。
```

## 提问建议清单

- 有真实表时，优先直接给表名。
- 新建表时，尽量给出字段中文名和业务含义。
- 增量修改时，尽量说清楚是加字段、删字段还是改字段。
- 一对多时，必须说明布局风格。
- 需要复用字典时，最好直接给字典编码。
- 涉及数据库操作时，准备好目标数据库名。

## 使用建议

- 说清楚表名、模块名、前端风格，能减少来回确认。
- 需要复用字典时，尽量直接给出字典编码。
- 涉及一对多时，最好说明是否要默认布局、ERP 风格或内嵌子表。
- 涉及数据库操作时，准备好目标数据库名，便于确认和执行。
