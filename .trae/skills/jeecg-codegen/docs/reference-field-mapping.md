# 字段、字典与组件映射参考

## 智能字段推导

优先级：用户明确指定 > 字段语义推导 > DB 类型兜底映射。

| 语义关键词 | DB 类型 | Java 类型 | 默认控件 |
|---|---|---|---|
| 名称、标题、编码 | `varchar(100)` | `String` | `Input` |
| 金额、价格、费用 | `decimal(10,2)` | `BigDecimal` | `InputNumber` |
| 数量、序号、排序 | `int` | `Integer` | `InputNumber` |
| 状态、类型、级别 | `varchar(10)` | `String` | `JDictSelectTag` |
| 是否、开关 | `varchar(2)` | `String` | `JSwitch` |
| 日期 | `date` | `Date` | `DatePicker` |
| 日期时间 | `datetime` | `Date` | `DatePicker(showTime)` |
| 备注、描述、说明 | `text` | `String` | `InputTextArea` |
| 富文本、内容 | `text` | `String` | `JEditor` |
| 图片 | `varchar(1000)` | `String` | `JImageUpload` |
| 文件、附件 | `varchar(1000)` | `String` | `JUpload` |
| 用户、负责人 | `varchar(32)` | `String` | `JSelectUserByDept` |
| 部门、组织 | `varchar(32)` | `String` | `JSelectDept` |

## DB 类型兜底映射

| DB 列类型 | Java 类型 | 默认控件 |
|---|---|---|
| `varchar(n)` 且 `n <= 200` | `String` | `Input` |
| `varchar(n)` 且 `n > 200` | `String` | `InputTextArea` |
| `text` / `longtext` | `String` | `InputTextArea` |
| `int` / `tinyint` | `Integer` | `InputNumber` |
| `bigint` | `Long` | `InputNumber` |
| `decimal` / `double` / `float` | `BigDecimal` | `InputNumber` |
| `date` | `Date` | `DatePicker` |
| `datetime` / `timestamp` | `Date` | `DatePicker(showTime)` |

## 字典匹配

### 系统字典

- 数据源：`sys_dict` + `sys_dict_item`
- 常见场景：状态、类型、级别、优先级
- 前端组件：`JDictSelectTag`

### 分类字典

- 数据源：`sys_category`
- 常见场景：区域、分类、树形选择
- 前端组件：`JCategorySelect`

### 表字典

- 数据源：任意业务表
- 常见场景：部门、用户、归属关系
- 前端组件：`JDictSelectTag` 或 `JSearchSelect`

## searchFormSchema 规则

- 查询项的 `label` 必须与编辑表单保持一致。
- 多选字段查询统一用 `JSelectMultiple`，不要用 `JCheckbox`。
- 不适合查询的组件不要放进 `searchFormSchema`，如密码、多行文本、富文本、上传组件。
- 日期范围优先考虑 `RangePicker`，时间范围优先考虑 `RangeTime`。
- `JPopup` 放到查询条件时，必须使用 `formActionType` 写法。

## 何时继续深读模板总参考

出现以下情况时，继续读取拆分后的模板参考文件：

- 需要三种字典的完整写法：读取 `docs/reference-template-single-table.md`。
- 需要全组件示例：读取 `docs/reference-template-single-table.md`。
- 需要 `JVxeColumn` 规则：读取 `docs/reference-template-master-detail.md`。
- 需要树表、一对多、Native 风格的完整模板：按场景读取 `docs/reference-template-tree-table.md` 或 `docs/reference-template-master-detail.md`。
- 需要增量修改模板：读取 `docs/reference-template-incremental-change.md`。
