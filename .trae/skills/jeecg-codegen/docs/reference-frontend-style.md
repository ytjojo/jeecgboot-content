# 前端风格参考

## vue3 封装风格

适用条件：标准 JeecgBoot CRUD，优先配置化生成。

- 核心组件：`BasicTable`、`BasicForm`、`BasicModal`。
- 核心文件：`*.api.ts`、`*.data.ts`、`*List.vue`、`*Modal.vue`。
- 查询、编辑、列表大多依赖 Schema 配置。
- 一对多默认使用 `useJvxeMethod` 和 `JVxeTable` 体系。

## vue3Native 原生风格

适用条件：用户明确要求 `vue3Native`，或页面需要更原生、更细粒度控制。

- 核心组件：`a-form`、`JModal`、原生模板结构。
- 通常多一个 `*Form.vue` 作为核心表单组件。
- 一对多场景的 `Form.vue` 是主入口，和 vue3 封装风格不能混用。

## 选择建议

- 用户未指定时，默认 `vue3`。
- 用户明确要原生模板、特殊交互、与旧页面保持一致时，选 `vue3Native`。
- 同一个模块内不要混搭两套风格。

## 模板入口

- 单表 vue3：`docs/reference-template-single-table.md` 中的 `A8`、`A9`、`A10`。
- 单表 vue3Native：`docs/reference-template-single-table.md` 中的 `A8N`、`A9N`、`A10N`、`A11N`。
- 一对多 vue3：`docs/reference-template-master-detail.md` 中的 `C9`、`C11`、`C12`。
- 一对多 vue3Native：`docs/reference-template-master-detail.md` 中的 `C13`。
