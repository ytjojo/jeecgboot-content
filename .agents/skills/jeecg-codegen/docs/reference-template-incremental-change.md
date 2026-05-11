## 适用场景

- 已有模块需要加字段、删字段、改字段。
- 希望做最小修改，而不是重新生成整个模块。
- 需要同步调整前后端和 `ALTER TABLE` SQL。

## 必读前置

- 先确认目标模块、实体名、表名和前端目录。
- 先读取现有 `Entity.java`、`*.data.ts`、`*List.vue`、`*Modal.vue`，必要时补读 `*Form.vue`。
- 先输出变更摘要，确认后再落盘。
- 若模块是树表或一对多，还需补读对应模板文档。

## 常见误用

- 只修改实体或只修改前端，遗漏另一侧和 SQL。
- 未扫描现有代码就按“标准模板”硬改，覆盖人工定制逻辑。
- 把增量改动当成全量重生成，导致无关文件被改动。

## F. 增量字段修改（加字段/删字段/改字段）

### F1. 定位已有代码文件

增量修改时，必须先找到并读取所有相关文件：

```
后端文件（在后端根目录搜索）：
  - **/entity/{EntityName}.java          → 实体类
  - **/controller/{EntityName}Controller.java  → 控制器（通常不需要改）
  - **/service/I{EntityName}Service.java       → Service接口（通常不需要改）
  - **/service/impl/{EntityName}ServiceImpl.java → Service实现（通常不需要改）

前端文件（在前端 src/views/ 下搜索）：
  - **/{EntityName}.data.ts              → 列定义 + 表单Schema
  - **/{EntityName}List.vue              → 列表页（通常不需要改）
  - **/{EntityName}Modal.vue             → 弹窗（通常不需要改）
  - **/{EntityName}Form.vue              → 表单（vue3Native风格，需要改）
```

### F2. 加字段 — 需要修改的位置

**每加一个字段，需要修改以下文件：**

#### 1) Entity.java — 在业务字段区域末尾追加

根据字段类型选择对应的注解模式（参考 A1 节的业务字段模板）：

```java
// String 字段
@Excel(name = "字段注释", width = 15)
@Schema(description = "字段注释")
private String fieldName;

// 带字典的 String 字段
@Excel(name = "字段注释", width = 15, dicCode = "dict_code")
@Dict(dicCode = "dict_code")
@Schema(description = "字段注释")
private String fieldName;

// Integer 字段
@Excel(name = "字段注释", width = 15)
@Schema(description = "字段注释")
private Integer fieldName;

// BigDecimal 字段（需确认 import java.math.BigDecimal 已存在）
@Excel(name = "字段注释", width = 15)
@Schema(description = "字段注释")
private BigDecimal fieldName;

// Date 字段（需确认 import java.util.Date + JsonFormat + DateTimeFormat 已导入）
@Excel(name = "字段注释", width = 15, format = "yyyy-MM-dd")
@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
@Schema(description = "字段注释")
private Date fieldName;

// DateTime 字段
@Excel(name = "字段注释", width = 20, format = "yyyy-MM-dd HH:mm:ss")
@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@Schema(description = "字段注释")
private Date fieldName;
```

**注意：** 检查是否需要新增 import 语句（如 BigDecimal、Date、JsonFormat、Dict 等）。

#### 2) *.data.ts — 三处追加

**a) columns 数组末尾追加列定义：**
```typescript
// 普通列
{
  title: '字段名称',
  align: 'center',
  dataIndex: 'fieldName',
},
// 字典列（dataIndex 加 _dictText 后缀）
{
  title: '状态',
  align: 'center',
  dataIndex: 'status_dictText',
},
// 图片列
{
  title: '图片',
  align: 'center',
  dataIndex: 'imageField',
  customRender: render.renderImage,
},
```

**b) searchFormSchema 数组追加查询条件（仅常用查询字段需要）：**
```typescript
{
  label: '字段名称',
  field: 'fieldName',
  component: 'JInput',  // 或 JDictSelectTag 等
  colProps: { span: 6 },
},
```

**c) formSchema 数组末尾追加（在最后一个字段 `}` 后、`];` 前）：**
```typescript
{
  label: '字段名称',
  field: 'fieldName',
  component: 'Input',  // 根据字段类型选择组件
  componentProps: { placeholder: '请输入字段名称' },
},
```

**d) superQuerySchema 数组追加（如果存在）：**
```typescript
{ title: '字段名称', value: 'fieldName', type: 'string' },
```

#### 3) *Form.vue — 仅 vue3Native 风格需要修改

在 `<a-form>` 中追加表单项：
```vue
<a-form-item label="字段名称" v-bind="validatorRules.fieldName" name="fieldName">
  <a-input v-model:value="formData.fieldName" placeholder="请输入字段名称" />
</a-form-item>
```

在 `formData` reactive 对象中追加初始值：
```typescript
fieldName: '',
```

#### 4) Flyway SQL — 生成 ALTER TABLE

```sql
ALTER TABLE `{{tableName}}` ADD COLUMN `column_name` varchar(100) DEFAULT NULL COMMENT '字段注释';
```

多个字段可合并为一条 ALTER：
```sql
ALTER TABLE `{{tableName}}`
  ADD COLUMN `field1` varchar(100) DEFAULT NULL COMMENT '注释1',
  ADD COLUMN `field2` int DEFAULT NULL COMMENT '注释2';
```

### F3. 删字段 — 需要修改的位置

**从以下位置移除字段相关代码：**

1. **Entity.java** — 删除字段声明及其注解（@Excel、@Dict、@Schema、@JsonFormat 等）
2. ***.data.ts** — 删除 columns 中对应列、searchFormSchema 中对应项、formSchema 中对应项、superQuerySchema 中对应项
3. ***Form.vue**（vue3Native）— 删除 `<a-form-item>` 和 formData 中对应属性
4. **Flyway SQL** — 生成 `ALTER TABLE \`{{tableName}}\` DROP COLUMN \`column_name\`;`

**注意：** 删除 Entity 字段后检查是否有不再使用的 import（如删除了唯一的 BigDecimal 字段，则移除 BigDecimal import）。

### F4. 改字段 — 需要修改的位置

根据修改内容，可能需要改动：

- **改类型**：Entity 字段类型 + data.ts 组件类型 + Form.vue 控件 + ALTER TABLE MODIFY
- **改注释/标题**：Entity @Excel name + @Schema description + data.ts title/label
- **加/改字典**：Entity @Dict + data.ts 组件改为 JDictSelectTag + columns dataIndex 加 _dictText
- **改必填**：data.ts formSchema 中 required 属性

Flyway SQL 示例：
```sql
ALTER TABLE `{{tableName}}` MODIFY COLUMN `column_name` decimal(10,2) DEFAULT NULL COMMENT '新注释';
```

### F5. 增量修改检查清单

每次增量修改完成后，确认：
- [ ] Entity.java — 字段声明 + 注解 + import
- [ ] *.data.ts — columns + searchFormSchema(如需) + formSchema + superQuerySchema(如存在)
- [ ] *Form.vue — 表单控件 + formData 初始值（仅 vue3Native）
- [ ] Flyway SQL — ALTER TABLE 语句
- [ ] 无遗漏的 import 增删
