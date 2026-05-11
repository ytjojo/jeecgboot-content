# JeecgBoot 代码生成模板总索引

这个文件不再承载全部模板细节，改为模板入口索引。

## 使用规则

- 正式生成代码前，必须先判断当前场景。
- 按场景只读取必要的模板参考文件，不要默认整包通读。
- 若场景涉及多种表类型或前端风格，再组合读取多个模板参考。

## 模板入口

| 文档 | 用途 |
|---|---|
| `docs/reference-template-single-table.md` | 单表模板总参考，包含变量说明、后端模板、`vue3` 与 `vue3Native` 单表前端模板、菜单 SQL |
| `docs/reference-template-tree-table.md` | 树表差异模板，包含树表后端、前端和关键规则 |
| `docs/reference-template-master-detail.md` | 一对多模板总参考，包含默认布局、ERP、内嵌子表、`vue3Native` |
| `docs/reference-template-field-ddl.md` | 字段类型映射速查表和建表 DDL 模板 |
| `docs/reference-template-incremental-change.md` | 增量字段修改模板，包含加字段、删字段、改字段检查清单 |

## 场景到模板的映射

### 现有表全量生成

- 基础模板：`docs/reference-template-single-table.md`
- 树表补充：`docs/reference-template-tree-table.md`
- 一对多补充：`docs/reference-template-master-detail.md`
- 字段映射补充：`docs/reference-template-field-ddl.md`

### 新建表全量生成

- 单表模板：`docs/reference-template-single-table.md`
- 树表模板：`docs/reference-template-tree-table.md`
- 一对多模板：`docs/reference-template-master-detail.md`
- 建表 DDL：`docs/reference-template-field-ddl.md`

### 增量修改

- 主入口：`docs/reference-template-incremental-change.md`
- 字段映射补充：`docs/reference-template-field-ddl.md`
- 若涉及树表或一对多，再补读对应模板文档。

## 维护约定

- 单表模板集中维护在 `docs/reference-template-single-table.md`。
- 树表模板集中维护在 `docs/reference-template-tree-table.md`。
- 一对多模板集中维护在 `docs/reference-template-master-detail.md`。
- 字段与 DDL 模板集中维护在 `docs/reference-template-field-ddl.md`。
- 增量修改模板集中维护在 `docs/reference-template-incremental-change.md`。

