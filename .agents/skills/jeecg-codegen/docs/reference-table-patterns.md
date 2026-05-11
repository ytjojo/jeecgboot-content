# 表类型与布局参考

## 单表

适用条件：只有一张业务表，不涉及树层级和主子表。

- 直接使用 `docs/reference-template-single-table.md`。
- 前端与后端均按标准 CRUD 模板生成。

## 树表

适用条件：用户提到 `分类`、`层级`、`树`、`上下级`。

- 重点字段：`pid`、`has_child`。
- 前端必须使用树表专用列表和弹窗模式。
- 后端必须补齐根节点、子节点和批量加载接口。
- 读取 `docs/reference-template-tree-table.md`。

## 一对多

适用条件：用户提到 `主子表`、`明细`、`一对多`、`订单+明细`。

- 主表和子表都要有完整后端结构。
- 前端布局需要继续分支判断。
- 读取 `docs/reference-template-master-detail.md`。

## 一对多前端布局分支

### 默认布局

- 列表页：标准 `BasicTable`。
- 弹窗：上方主表，下面 `a-tabs` 放子表。
- 模板入口：`docs/reference-template-master-detail.md` 中的 `C9` 模板。

### ERP 风格

- 主表和子表独立 CRUD。
- 主表列表通常单选，子表在下方独立列表或 Tab 中维护。
- 模板入口：`docs/reference-template-master-detail.md` 中的 `C11` 模板。

### 内嵌子表

- 列表页使用 `expandedRowRender` 直接展示子表。
- 弹窗仍使用主表在上、子表在下的编辑结构。
- 模板入口：`docs/reference-template-master-detail.md` 中的 `C12` 模板。

### vue3Native 一对多

- 架构与 vue3 封装风格不同，核心在 `Form.vue`。
- 模板入口：`docs/reference-template-master-detail.md` 中的 `C13` 模板。

## 选择顺序

1. 先判断是单表、树表还是一对多。
2. 若是一对多，再判断默认布局、ERP 风格、内嵌子表或 vue3Native。
3. 最后再读取 `docs/reference-frontend-style.md` 确认具体前端实现差异。
