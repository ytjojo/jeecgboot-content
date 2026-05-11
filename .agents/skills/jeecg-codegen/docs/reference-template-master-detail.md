## 适用场景

- 用户提到 `主子表`、`明细`、`一对多`、`订单+明细`。
- 需要主表和子表联动保存、联动编辑或独立维护。
- 需要在默认布局、ERP、内嵌子表、`vue3Native` 之间做分支选择。

## 必读前置

- 先读取 `docs/reference-template-single-table.md`，确认主表基础模板。
- 先确认是一对多，而不是树表或普通单表。
- 先确认前端布局风格，再进入 `C9`、`C11`、`C12` 或 `C13`。
- 先确认子表数量、一对一还是一对多混合、是否需要独立 CRUD。

## 常见误用

- 没区分默认布局、ERP、内嵌子表、`vue3Native`，直接混抄模板。
- 只生成主表，不补子表 Entity、Service、VO 和 API。
- 前端提交字段名与后端 Page VO 属性名不一致。
- 把 `JVxeTable` 方案和独立子表 CRUD 方案混用。

## C. 一对多模式差异

一对多在单表基础上有以下差异：

### C1. 子表 Entity

每个子表生成独立的 Entity，包含外键字段：
```java
    /**主表ID（外键）*/
    @Schema(description = "主表ID")
    private String {{mainEntityName_uncap}}Id;  // 外键字段名默认为 主表实体名(camelCase) + Id
```
注意：子表 Entity 的外键字段不加 `@Excel` 注解（导出时忽略）。

### C2. 子表 Mapper

```java
public interface {{subEntityName}}Mapper extends BaseMapper<{{subEntityName}}> {
    /**
     * 通过主表id删除子表数据
     */
    @Delete("DELETE FROM {{subTableName}} WHERE {{foreignKey}} = #{mainId}")
    boolean deleteByMainId(@Param("mainId") String mainId);

    /**
     * 通过主表id查询子表数据
     */
    List<{{subEntityName}}> selectByMainId(@Param("mainId") String mainId);
}
```

### C3. 主表 Service 接口

```java
public interface I{{entityName}}Service extends IService<{{entityName}}> {
    /**
     * 添加一对多
     */
    public void saveMain({{entityName}} entity, List<{{subEntityName}}> subList);

    /**
     * 修改一对多
     */
    public void updateMain({{entityName}} entity, List<{{subEntityName}}> subList);

    /**
     * 删除一对多
     */
    public void delMain(String id);

    /**
     * 批量删除一对多
     */
    public void delBatchMain(Collection<? extends Serializable> idList);
}
```

### C4. 主表 ServiceImpl

```java
@Service
public class {{entityName}}ServiceImpl extends ServiceImpl<{{entityName}}Mapper, {{entityName}}> implements I{{entityName}}Service {

    @Autowired
    private {{subEntityName}}Mapper {{subEntityName_uncap}}Mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMain({{entityName}} entity, List<{{subEntityName}}> subList) {
        baseMapper.insert(entity);
        if (subList != null && subList.size() > 0) {
            for ({{subEntityName}} sub : subList) {
                sub.set{{mainEntityName}}Id(entity.getId());  // 设置外键
                {{subEntityName_uncap}}Mapper.insert(sub);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMain({{entityName}} entity, List<{{subEntityName}}> subList) {
        baseMapper.updateById(entity);
        // 先删后增
        {{subEntityName_uncap}}Mapper.deleteByMainId(entity.getId());
        if (subList != null && subList.size() > 0) {
            for ({{subEntityName}} sub : subList) {
                sub.set{{mainEntityName}}Id(entity.getId());
                {{subEntityName_uncap}}Mapper.insert(sub);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delMain(String id) {
        {{subEntityName_uncap}}Mapper.deleteByMainId(id);
        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBatchMain(Collection<? extends Serializable> idList) {
        for (Serializable id : idList) {
            {{subEntityName_uncap}}Mapper.deleteByMainId(id.toString());
            baseMapper.deleteById(id);
        }
    }
}
```

### C5. Page VO（Excel导入导出用）

```java
package org.jeecg.modules.{{entityPackage}}.vo;

import org.jeecg.modules.{{entityPackage}}.entity.{{entityName}};
import org.jeecg.modules.{{entityPackage}}.entity.{{subEntityName}};
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.ExcelCollection;
import java.util.List;

@Data
public class {{entityName}}Page {
    // 主表字段（同 Entity，不含系统字段）
    // ...

    @ExcelCollection(name = "{{subDescription}}")
    private List<{{subEntityName}}> {{subEntityName_uncap}}List;
}
```

### C6. Controller 额外端点

```java
    /**
     * 查询子表数据
     */
    @GetMapping(value = "/query{{subEntityName}}ByMainId")
    public Result<List<{{subEntityName}}>> query{{subEntityName}}ByMainId(@RequestParam(name = "id", required = true) String id) {
        List<{{subEntityName}}> list = {{subEntityName_uncap}}Mapper.selectByMainId(id);
        return Result.OK(list);
    }
```

### C7. 前端差异 — api.ts

**vue3 封装风格：** 子表查询接口导出为 URL 字符串常量（不是函数），供 Modal 中 `defHttp.get` 或 `requestSubTableData` 使用：

```typescript
enum Api {
  // ... 主表 CRUD 接口
  {{subEntityName_uncap}}List = '/{{entityPackage}}/{{entityName_uncap}}/query{{subEntityName}}ByMainId',
}

// 子表接口导出为 URL 字符串（不是函数！）
export const {{subEntityName_uncap}}List = Api.{{subEntityName_uncap}}List;
```

**vue3Native 原生风格（C13）：** 子表查询接口导出为**函数**，且需要额外的 `queryDataById`。详见 C13-1。

### C8. 前端差异 — data.ts

一对多子表使用 `JVxeColumn[]`（**不是 FormSchema[]**），组件类型使用 `JVxeTypes` 枚举：

```typescript
import { JVxeTypes, JVxeColumn } from '/@/components/jeecg/JVxeTable/types';

export const {{subEntityName_uncap}}Columns: JVxeColumn[] = [
  {
    title: '字段标题',
    key: 'fieldName',        // 注意用 key 不是 field
    type: JVxeTypes.input,   // 注意用 JVxeTypes 不是 component 字符串
    width: '200px',          // 字符串带px
    placeholder: '请输入${title}',
    defaultValue: '',
  },
  // JVxeTypes 与 FormSchema component 的对应关系见 SKILL.md 的 JVxeTable 章节
];
```

**JVxeColumn 与 FormSchema 的关键区别：**
| 维度 | FormSchema | JVxeColumn |
|------|-----------|-----------|
| 字段名属性 | `field` | `key` |
| 组件类型属性 | `component: 'JDictSelectTag'` | `type: JVxeTypes.select` |
| 字典配置 | `componentProps: { dictCode: 'xxx' }` | 顶层属性 `dictCode: 'xxx'` |
| 下拉选项 | 自动从字典加载 | 需显式声明 `options: []` |
| 宽度 | `colProps: { span: 12 }` | `width: '200px'` |
| 校验规则 | `dynamicRules` 函数 | `validateRules` 数组 |
| 占位符 | `componentProps: { placeholder }` | 顶层属性 `placeholder`，支持 `${title}` |

一对一子表使用 `FormSchema[]`（与主表 formSchema 相同的写法）：

```typescript
export const {{subEntityName_uncap}}FormSchema: FormSchema[] = [
  // 与主表 formSchema 写法完全一致，使用 component/field/componentProps
];
```

### C9. 前端差异 — Modal.vue（核心模板）

#### Tab 风格核心特征

Tab-in-Modal 使用 **radio-group 标题栏** 切换主表/子表区域，一对一子表抽成**独立 Form.vue 组件**，通过 `useJvxeMethod` + `validateSubForm` 统一处理提交。

**关键模式：**
- Modal 标题栏用 `a-radio-group` + `a-radio-button`（不是 `a-tabs`）
- 各区域用 `v-show` 切换（不是 `a-tab-pane`），避免频繁销毁/重建
- `wrapClassName="j-cgform-tab-modal"` 启用专属样式
- 一对一子表抽成独立 `XxxForm.vue` 组件，暴露 `initFormData`/`getFormData`/`validateForm`/`resetFields` 方法
- 通过 `useJvxeMethod` 第6个参数 `validateSubForm` 校验所有一对一子表
- 不需要自写 `handleSubmit`，`useJvxeMethod` 返回的 `handleSubmit` 已整合所有校验和提交逻辑

#### 文件结构

Tab-in-Modal 风格比其他风格多一个一对一子表 Form 组件：

```
views/{{viewDir}}/
├── {{entityName}}List.vue                   # 主列表
├── {{entityName}}.api.ts                    # API（子表导出为URL字符串）
├── {{entityName}}.data.ts                   # 列/表单配置（JVxeColumn + FormSchema）
└── components/
    ├── {{entityName}}Modal.vue              # Modal（radio-group Tab 切换）
    └── {{detailEntityName}}Form.vue         # 一对一子表独立Form组件（仅有一对一子表时）
```

#### 一对一子表独立 Form 组件（{{detailEntityName}}Form.vue）

```html
<template>
    <BasicForm @register="registerForm" name="{{detailEntityName}}Form" class="basic-modal-form" />
</template>
<script lang="ts">
    import {defineComponent} from 'vue';
    import {BasicForm, useForm} from '/@/components/Form/index';
    import { {{detailEntityName_uncap}}FormSchema } from '../{{entityName}}.data';
    import {defHttp} from '/@/utils/http/axios';
    import { VALIDATE_FAILED } from '/@/utils/common/vxeUtils'

    export default defineComponent({
        name:"{{detailEntityName}}Form",
        components: {BasicForm},
        emits:['register'],
        props:{
            disabled: {
                type: Boolean,
                default: false
            }
        },
        setup(props,{emit}) {
            const [registerForm, { setProps, resetFields, setFieldsValue, getFieldsValue, validate, scrollToField }] = useForm({
                schemas: {{detailEntityName_uncap}}FormSchema,
                showActionButtonGroup: false,
                baseColProps: {span: 12}
            });
            // 初始化加载数据（url 是子表查询接口的 URL 字符串）
            function initFormData(url,id){
                if(id){
                     defHttp.get({url,params:{id}},{isTransformResponse:false}).then(res=>{
                       res.success && res.result && res.result.length > 0 && setFieldsValue({...res.result[0]});
                    })
                }
                setProps({disabled: props.disabled})
            }
            // 获取表单数据（返回数组，因为后端统一用 List<Entity> 接收）
            function getFormData(){
               let formData = getFieldsValue();
               Object.keys(formData).map(k=>{
                   if(formData[k] instanceof Array){
                       formData[k] = formData[k].join(',')
                   }
               });
               return [formData];
            }
            // 表单校验（index 参数对应 refKeys 数组中的位置）
            function validateForm(index){
                return new Promise((resolve, reject) => {
                    validate().then(()=>{
                        return resolve()
                    }).catch(({ errorFields }) => {
                      return reject({ error: VALIDATE_FAILED, index, errorFields: errorFields, scrollToField: scrollToField });
                    });
                })
            }
            return {
                registerForm,
                resetFields,
                initFormData,
                getFormData,
                validateForm
            }
        }
    })
</script>
<style lang="less" scoped>
  .basic-modal-form {
    overflow: auto;
    height: 340px;
  }
</style>
```

**Form 组件关键点：**
- 使用 Options API（`defineComponent`），不是 `<script setup>`，因为需要通过 `return` 暴露方法给父组件 `ref` 调用
- `initFormData(url, id)` 中 `isTransformResponse: false` 返回完整响应 `{success, result}`，取 `result[0]`
- `getFormData()` 返回**数组** `[formData]`，与后端 `List<Entity>` 对应
- `validateForm(index)` 的 `index` 参数对应 `refKeys` 数组位置，校验失败时自动跳转到对应 Tab
- FormSchema 中必须包含隐藏的 `id` 字段：`{ label: '', field: 'id', component: 'Input', show: false }`

#### Modal.vue — 纯一对多（无一对一子表）

```html
<template>
  <BasicModal ref="modalRef" destroyOnClose wrapClassName="j-cgform-tab-modal" v-bind="$attrs" @register="registerModal" :maxHeight="500" :width="896" @ok="handleSubmit">
    <template #title>
        <div class="titleArea">
          <div class="title">{{ title }}</div>
          <div class="right">
            <a-radio-group v-model:value="activeKey">
              <template v-for="(item, index) in tabNav" :key="index">
                <a-radio-button :value="item.tableName">{{ item.tableTxt }}</a-radio-button>
              </template>
            </a-radio-group>
          </div>
        </div>
     </template>
     <div class="contentArea">
        <BasicForm @register="registerForm" ref="formRef" v-show="activeKey == refKeys[0]" name="{{entityName}}Form"/>
         <JVxeTable
           v-show="activeKey == '{{subEntityName_uncap}}'"
           keep-source resizable
           ref="{{subEntityName_uncap}}"
           :loading="{{subEntityName_uncap}}Table.loading"
           :columns="{{subEntityName_uncap}}Table.columns"
           :dataSource="{{subEntityName_uncap}}Table.dataSource"
           :height="340"
           :disabled="formDisabled"
           :rowNumber="true"
           :rowSelection="true"
           :toolbar="true"
           />
     </div>
  </BasicModal>
</template>

<script lang="ts" setup>
    import {ref, computed, unref, reactive} from 'vue';
    import {BasicModal, useModalInner} from '/@/components/Modal';
    import {BasicForm, useForm} from '/@/components/Form/index';
    import { useJvxeMethod } from '/@/hooks/system/useJvxeMethods.ts'
    import {formSchema, {{subEntityName_uncap}}Columns} from '../{{entityName}}.data';
    import {saveOrUpdate, {{subEntityName_uncap}}List} from '../{{entityName}}.api';

    const emit = defineEmits(['register','success']);
    const isUpdate = ref(true);
    const formDisabled = ref(false);
    const modalRef = ref();
    // refKeys[0] 是主表的 key，后面是各子表的 key
    const refKeys = ref(['{{entityName_uncap}}', '{{subEntityName_uncap}}']);
    const tabNav = ref<any>([
      { tableName: '{{entityName_uncap}}', tableTxt: '{{description}}' },
      { tableName: '{{subEntityName_uncap}}', tableTxt: '{{subDescription}}' },
    ]);
    const activeKey = ref('{{entityName_uncap}}');
    const {{subEntityName_uncap}} = ref();
    const tableRefs = { {{subEntityName_uncap}} };
    const {{subEntityName_uncap}}Table = reactive({
          loading: false,
          dataSource: [],
          columns: {{subEntityName_uncap}}Columns
    })
    const [registerForm, {setProps, resetFields, setFieldsValue, validate}] = useForm({
        schemas: formSchema,
        showActionButtonGroup: false,
        baseColProps: {span: 12}
    });
    const [registerModal, {setModalProps, closeModal}] = useModalInner(async (data) => {
        await reset();
        setModalProps({confirmLoading: false, showCancelBtn: data?.showFooter, showOkBtn: data?.showFooter});
        isUpdate.value = !!data?.isUpdate;
        formDisabled.value = !data?.showFooter;
        if (unref(isUpdate)) {
            await setFieldsValue({ ...data.record });
            requestSubTableData({{subEntityName_uncap}}List, {id: data?.record?.id}, {{subEntityName_uncap}}Table)
        }
        setProps({ disabled: !data?.showFooter })
    });
    const [handleChangeTabs, handleSubmit, requestSubTableData, formRef] = useJvxeMethod(requestAddOrEdit, classifyIntoFormData, tableRefs, activeKey, refKeys);

    const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(formDisabled) ? '编辑' : '详情'));
    async function reset(){
      await resetFields();
      activeKey.value = '{{entityName_uncap}}';
      {{subEntityName_uncap}}Table.dataSource = [];
    }
    function classifyIntoFormData(allValues) {
         let main = Object.assign({}, allValues.formValue)
         return {
           ...main,
           {{subEntityName_uncap}}List: allValues.tablesValue[0].tableData,
         }
    }
    async function requestAddOrEdit(values) {
        try {
            setModalProps({confirmLoading: true});
            await saveOrUpdate(values, isUpdate.value);
            closeModal();
            emit('success');
        } finally {
            setModalProps({confirmLoading: false});
        }
    }
</script>

<style lang="less" scoped>
  :deep(.ant-input-number) { width: 100%; }
  :deep(.ant-calendar-picker) { width: 100%; }
  .titleArea {
    display: flex;
    align-content: center;
    padding-right: 70px;
    .title { margin-right: 16px; line-height: 32px; }
    .right {
      overflow-x: auto; overflow-y: hidden; flex: 1; white-space: nowrap;
      .ant-radio-group { font-weight: normal; }
    }
  }
  html[data-theme='light'] {
    .right .ant-radio-group {
      :deep(.ant-radio-button-wrapper:not(.ant-radio-button-wrapper-checked)) { color: #555; }
    }
  }
</style>
<style lang="less">
.j-cgform-tab-modal {
   .contentArea { padding: 20px 1.5% 0; }
  .ant-modal .ant-modal-body > .scrollbar,
  .ant-tabs-nav .ant-tabs-tab { padding-top: 0; }
  .ant-tabs-top-bar { width: calc(100% - 55px); position: relative; left: -14px; }
  .ant-tabs .ant-tabs-top-content > .ant-tabs-tabpane { overflow: hidden auto; }
}
</style>
```

#### Modal.vue — 一对一 + 一对多混合（多个子表）

当同时存在一对一子表和一对多子表时，Modal 需额外引入一对一 Form 组件，并通过 `validateSubForm` 回调校验。

**与纯一对多的关键差异：**
- 多引入 `{{detailEntityName}}Form` 组件和 `VALIDATE_FAILED`
- `refKeys` 多一项一对一子表的 key
- `tabNav` 多一项一对一子表
- `useJvxeMethod` 第6个参数传入 `validateSubForm` 函数
- `reset()` 中额外调用 `{{detailEntityName_uncap}}Form.value.resetFields()`
- `classifyIntoFormData` 中调用 `{{detailEntityName_uncap}}Form.value.getFormData()` 获取一对一数据
- 编辑时通过 `{{detailEntityName_uncap}}Form.value.initFormData(url, id)` 加载一对一数据

```html
<template>
  <BasicModal ref="modalRef" destroyOnClose wrapClassName="j-cgform-tab-modal" v-bind="$attrs" @register="registerModal" :maxHeight="500" :width="896" @ok="handleSubmit">
    <template #title>
        <div class="titleArea">
          <div class="title">{{ title }}</div>
          <div class="right">
            <a-radio-group v-model:value="activeKey">
              <template v-for="(item, index) in tabNav" :key="index">
                <a-radio-button :value="item.tableName">{{ item.tableTxt }}</a-radio-button>
              </template>
            </a-radio-group>
          </div>
        </div>
     </template>
     <div class="contentArea">
        <BasicForm @register="registerForm" ref="formRef" v-show="activeKey == refKeys[0]" name="{{entityName}}Form"/>
         <JVxeTable
           v-show="activeKey == '{{subEntityName_uncap}}'"
           keep-source resizable
           ref="{{subEntityName_uncap}}"
           :loading="{{subEntityName_uncap}}Table.loading"
           :columns="{{subEntityName_uncap}}Table.columns"
           :dataSource="{{subEntityName_uncap}}Table.dataSource"
           :height="340"
           :disabled="formDisabled"
           :rowNumber="true" :rowSelection="true" :toolbar="true"
           />
         <{{detailEntityName}}Form ref="{{detailEntityName_uncap}}Form" :disabled="formDisabled" v-show="activeKey == '{{detailEntityName_uncap}}'"><\/{{detailEntityName}}Form>
     </div>
  </BasicModal>
</template>

<script lang="ts" setup>
    import {ref, computed, unref, reactive} from 'vue';
    import {BasicModal, useModalInner} from '/@/components/Modal';
    import {BasicForm, useForm} from '/@/components/Form/index';
    import { useJvxeMethod } from '/@/hooks/system/useJvxeMethods.ts'
    import {{detailEntityName}}Form from './{{detailEntityName}}Form.vue'
    import {formSchema, {{subEntityName_uncap}}Columns} from '../{{entityName}}.data';
    import {saveOrUpdate, {{subEntityName_uncap}}List, {{detailEntityName_uncap}}List} from '../{{entityName}}.api';
    import { VALIDATE_FAILED } from '/@/utils/common/vxeUtils'

    const emit = defineEmits(['register','success']);
    const isUpdate = ref(true);
    const formDisabled = ref(false);
    const modalRef = ref();
    const refKeys = ref(['{{entityName_uncap}}', '{{subEntityName_uncap}}', '{{detailEntityName_uncap}}']);
    const tabNav = ref<any>([
      { tableName: '{{entityName_uncap}}', tableTxt: '{{description}}' },
      { tableName: '{{subEntityName_uncap}}', tableTxt: '{{subDescription}}' },
      { tableName: '{{detailEntityName_uncap}}', tableTxt: '{{detailDescription}}' },
    ]);
    const activeKey = ref('{{entityName_uncap}}');
    const {{subEntityName_uncap}} = ref();
    const {{detailEntityName_uncap}}Form = ref();
    const tableRefs = { {{subEntityName_uncap}} };
    const {{subEntityName_uncap}}Table = reactive({
          loading: false,
          dataSource: [],
          columns: {{subEntityName_uncap}}Columns
    })
    const [registerForm, {setProps, resetFields, setFieldsValue, validate}] = useForm({
        schemas: formSchema,
        showActionButtonGroup: false,
        baseColProps: {span: 12}
    });
    const [registerModal, {setModalProps, closeModal}] = useModalInner(async (data) => {
        await reset();
        setModalProps({confirmLoading: false, showCancelBtn: data?.showFooter, showOkBtn: data?.showFooter});
        isUpdate.value = !!data?.isUpdate;
        formDisabled.value = !data?.showFooter;
        if (unref(isUpdate)) {
            await setFieldsValue({ ...data.record });
            {{detailEntityName_uncap}}Form.value.initFormData({{detailEntityName_uncap}}List, data?.record?.id)
            requestSubTableData({{subEntityName_uncap}}List, {id: data?.record?.id}, {{subEntityName_uncap}}Table)
        }
        setProps({ disabled: !data?.showFooter })
    });
    // 第6个参数 validateSubForm 用于校验一对一子表
    const [handleChangeTabs, handleSubmit, requestSubTableData, formRef] = useJvxeMethod(requestAddOrEdit, classifyIntoFormData, tableRefs, activeKey, refKeys, validateSubForm);

    const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(formDisabled) ? '编辑' : '详情'));
    async function reset(){
      await resetFields();
      activeKey.value = '{{entityName_uncap}}';
      {{subEntityName_uncap}}Table.dataSource = [];
      {{detailEntityName_uncap}}Form.value.resetFields();
    }
    function classifyIntoFormData(allValues) {
         let main = Object.assign({}, allValues.formValue)
         return {
           ...main,
           {{subEntityName_uncap}}List: allValues.tablesValue[0].tableData,
           {{detailEntityName_uncap}}List: {{detailEntityName_uncap}}Form.value.getFormData(),
         }
    }
    // 校验所有一对一子表表单
    function validateSubForm(allValues){
        return new Promise((resolve,reject)=>{
            Promise.all([
                 {{detailEntityName_uncap}}Form.value.validateForm(2),
            ]).then(() => {
                resolve(allValues)
            }).catch(e => {
                if (e.error === VALIDATE_FAILED) {
                    activeKey.value = e.index == null ? unref(activeKey) : refKeys.value[e.index]
                    if (e.errorFields) {
                      const firstField = e.errorFields[0];
                      if (firstField) {
                        e.scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
                      }
                    }
                } else {
                    console.error(e)
                }
            })
        })
    }
    async function requestAddOrEdit(values) {
        try {
            setModalProps({confirmLoading: true});
            await saveOrUpdate(values, isUpdate.value);
            closeModal();
            emit('success');
        } finally {
            setModalProps({confirmLoading: false});
        }
    }
</script>
<!-- 样式同纯一对多 Modal，包含 titleArea + j-cgform-tab-modal -->
```

**`validateSubForm` 中 `validateForm(2)` 的参数说明：**
- 参数 `2` 是一对一子表在 `refKeys` 数组中的索引（从0开始：0=主表, 1=一对多子表, 2=一对一子表）
- 校验失败时通过 `refKeys.value[e.index]` 自动切换到对应 Tab
- 如有多个一对一子表，在 `Promise.all` 中追加，每个传入不同的 index

### C10. 一对多技术要点和常见陷阱

| 要点 | 说明 |
|------|------|
| **三种 Modal 布局风格** | **Tab-in-Modal(C9)**：radio-group 标题栏切换，主表/子表同级用 `v-show`，需 `wrapClassName="j-cgform-tab-modal"`。**内嵌子表(C12)/ERP(C11)**：原始布局，上面主表 BasicForm，下面子表用 `a-tabs` 组织，不需要 radio-group 和 `j-cgform-tab-modal` |
| **一对一子表必须抽成独立 Form.vue** | 使用 Options API（`defineComponent`）暴露 `initFormData`/`getFormData`/`validateForm`/`resetFields` 方法，父组件通过 `ref` 调用 |
| **Form.vue 用 isTransformResponse:false** | `initFormData` 中 `defHttp.get({url,params:{id}},{isTransformResponse:false})` 返回完整 `{success,result}`，取 `result[0]` 赋值 |
| **Form.vue 的 getFormData 返回数组** | `return [formData]` 包装成数组，因为后端统一用 `List<Entity>` 接收 |
| **validateSubForm 是 useJvxeMethod 第6个参数** | 有一对一子表时，`useJvxeMethod(requestAddOrEdit, classifyIntoFormData, tableRefs, activeKey, refKeys, validateSubForm)`，第6个参数传入校验函数 |
| **validateForm(index) 的 index 对应 refKeys 位置** | 例如 `refKeys = ['main', 'subMany', 'subOne']`，一对一子表 `validateForm(2)` 对应 index=2，校验失败自动切换到该 Tab |
| **refKeys[0] 是主表 key** | `v-show="activeKey == refKeys[0]"` 控制主表显隐，`reset()` 中 `activeKey.value = refKeys[0]` 的值 |
| **JVxeTable ref 名与 refKeys/activeKey 一致** | `ref="aiControlSub"` 必须与 `refKeys` 中的 key 和 `v-show="activeKey == 'aiControlSub'"` 完全一致 |
| **JVxeTypes 不等于 FormSchema component** | 子表列定义必须用 `JVxeTypes.select` 而非 `'JDictSelectTag'`。没有 `JVxeTypes.selectDict` 这个类型！ |
| **select 必须声明 options:[]** | `JVxeTypes.select/selectMultiple` 必须显式设置 `options:[]`，配合 `dictCode` 自动加载 |
| **开关用 checkbox 不是 switch** | JVxeTable 中开关使用 `JVxeTypes.checkbox` + `customValue:['Y','N']`，没有 JSwitch 类型 |
| **文件/图片需要 token** | `JVxeTypes.file/image` 必须设置 `token:true, responseName:'message'` |
| **popup 用 popupCode** | JVxeTable 的 Popup 使用 `popupCode`（不是 `code`），多选通过 `props: { multi: true }` |
| **width 是字符串** | JVxeColumn 的 width 格式为 `'200px'`（字符串），不是数字 |
| **校验用 validateRules** | 使用 `validateRules` 数组（不是 `dynamicRules` 函数），pattern 支持正则字符串和特殊值（`'only'` = 唯一） |
| **子表 API 导出为 URL 字符串** | `export const xxxList = Api.xxxList`（导出字符串），不是 `export const xxxList = (params) => defHttp.get(...)` |
| **一对一 FormSchema 需要隐藏 id 字段** | `{ label: '', field: 'id', component: 'Input', show: false }` 必须加在 FormSchema 首位 |
| **多子表 Service 参数** | 有多个子表时 `saveMain/updateMain` 参数对应增加，ServiceImpl 中需分别处理每个子表的删除和插入 |

### C11. ERP 风格一对多（子表独立CRUD）

ERP 风格与 Tab-in-Modal 风格的核心区别：主表和子表**各自独立保存**，不再整体提交。

#### 架构对比

| 维度 | Tab-in-Modal 风格 | ERP 风格 |
|------|-------------------|---------|
| 主表列表选择 | 多选 `rowSelection` | **单选 `rowSelection: { type: 'radio' }` ** |
| 子表位置 | Modal 内 Tab 页签 | **主表列表下方 Tab 页签** |
| 子表数据传递 | useJvxeMethod + requestSubTableData | **provide/inject + watch** |
| 子表保存方式 | 随主表一起提交（saveMain/updateMain） | **子表独立 CRUD（各自 add/edit/delete）** |
| 主表 Modal | 包含主表 + 子表 Tab | **仅主表字段** |
| 后端主表接口 | `/add` (saveMain) `/edit` (updateMain) | **`/addMain` (save) `/editMain` (updateById)** |

#### 关键陷阱：主表编辑不能用 updateMain

**问题：** ERP 模式下主表 Modal 只提交主表字段，如果仍调用 `updateMain()`，该方法会先删除所有子表记录再重新插入空列表，导致**子表数据全部丢失**。

**解决：** 后端必须新增仅操作主表的接口：
```java
// ERP模式：仅操作主表，不影响子表
@PostMapping(value = "/addMain")
public Result<String> addMain(@RequestBody {{entityName}} {{entityName_uncap}}) {
    {{entityName_uncap}}Service.save({{entityName_uncap}});
    return Result.OK("添加成功！");
}

@RequestMapping(value = "/editMain", method = {RequestMethod.PUT, RequestMethod.POST})
public Result<String> editMain(@RequestBody {{entityName}} {{entityName_uncap}}) {
    {{entityName_uncap}}Service.updateById({{entityName_uncap}});
    return Result.OK("编辑成功!");
}
```
前端 api.ts 中主表的 save/edit 指向 `/addMain` 和 `/editMain`。

#### 子表独立 CRUD 接口（后端）

每个子表需要 5 个接口：list（分页）、add、edit、delete、deleteBatch：
```java
// 子表分页列表（支持 BasicTable 分页参数）
@GetMapping(value = "/list{{subEntityName}}ByMainId")
public Result<IPage<{{subEntityName}}>> list{{subEntityName}}ByMainId(
        {{subEntityName}} {{subEntityName_uncap}},
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
        HttpServletRequest req) {
    QueryWrapper<{{subEntityName}}> queryWrapper = QueryGenerator.initQueryWrapper({{subEntityName_uncap}}, req.getParameterMap());
    Page<{{subEntityName}}> page = new Page<>(pageNo, pageSize);
    IPage<{{subEntityName}}> pageList = {{subEntityName_uncap}}Service.page(page, queryWrapper);
    return Result.OK(pageList);
}

@PostMapping(value = "/add{{subEntityName_suffix}}")
public Result<String> add{{subEntityName_suffix}}(@RequestBody {{subEntityName}} entity) {
    {{subEntityName_uncap}}Service.save(entity);
    return Result.OK("添加成功！");
}

@RequestMapping(value = "/edit{{subEntityName_suffix}}", method = {RequestMethod.PUT, RequestMethod.POST})
public Result<String> edit{{subEntityName_suffix}}(@RequestBody {{subEntityName}} entity) {
    {{subEntityName_uncap}}Service.updateById(entity);
    return Result.OK("编辑成功!");
}

@DeleteMapping(value = "/delete{{subEntityName_suffix}}")
public Result<String> delete{{subEntityName_suffix}}(@RequestParam(name = "id") String id) {
    {{subEntityName_uncap}}Service.removeById(id);
    return Result.OK("删除成功!");
}

@DeleteMapping(value = "/deleteBatch{{subEntityName_suffix}}")
public Result<String> deleteBatch{{subEntityName_suffix}}(@RequestParam(name = "ids") String ids) {
    {{subEntityName_uncap}}Service.removeByIds(Arrays.asList(ids.split(",")));
    return Result.OK("批量删除成功!");
}
```

#### 前端文件结构

```
views/{{viewDir}}/
├── {{entityName}}List.vue                   # ERP主页面（主表 + 下方Tab子表）
├── {{subEntityName}}List.vue                # 子表列表组件（每个子表一个）
├── {{entityName}}.api.ts                    # 全部API（主表+各子表CRUD）
├── {{entityName}}.data.ts                   # 全部列/表单配置
└── components/
    ├── {{entityName}}Modal.vue              # 主表Modal（仅主表字段）
    └── {{subEntityName}}Modal.vue           # 子表Modal（每个子表一个）
```

#### 前端核心模式 — 主表 provide + 子表 inject

**主表 List.vue：**
```typescript
import { computed, unref, provide } from 'vue';

// 单选模式
rowSelection: { type: 'radio' },

// 计算选中的主表ID，下发给子表
const {{mainEntityName_uncap}}Id = computed(() =>
  unref(selectedRowKeys).length > 0 ? unref(selectedRowKeys)[0] : ''
);
provide('{{mainEntityName_uncap}}Id', {{mainEntityName_uncap}}Id);

// handleSuccess 只 reload，不清空选中（否则子表会触发 watch 变空）
function handleSuccess() {
  reload();
}
```

**子表 List.vue：**
```typescript
import type { ComputedRef } from 'vue';
import { ref, computed, unref, watch, inject, reactive } from 'vue';
import { isEmpty } from '/@/utils/is';
import { useMessage } from '/@/hooks/web/useMessage';

// 注入主表ID
const {{mainEntityName_uncap}}Id = inject<ComputedRef<string>>(
  '{{mainEntityName_uncap}}Id',
  computed(() => '')
);
const $message = useMessage();
// ⚠️ 必须用 reactive，否则 BasicTable 的 :searchInfo 绑定可能不触发重新请求
const searchInfo = reactive<Record<string, any>>({});

// 监听主表ID变化，重新加载子表
watch({{mainEntityName_uncap}}Id, () => {
  searchInfo.{{mainEntityName_uncap}}Id = unref({{mainEntityName_uncap}}Id);
  reload();
  setSelectedRowKeys([]);
});

// 包装API：主表未选中时返回空数组
async function getSubList(params) {
  let { {{mainEntityName_uncap}}Id } = params;
  if ({{mainEntityName_uncap}}Id == null || isEmpty({{mainEntityName_uncap}}Id)) {
    return [];
  }
  return await subListApi(params);
}

// 新增前检查是否选中主表
function handleCreate() {
  if (isEmpty(unref({{mainEntityName_uncap}}Id))) {
    $message.createMessage.warning('请选择一条主表记录');
    return;
  }
  openModal(true, { isUpdate: false, showFooter: true });
}
```

**子表 Modal.vue：**
```typescript
import { inject } from 'vue';

// 注入主表ID
const {{mainEntityName_uncap}}Id = inject('{{mainEntityName_uncap}}Id') || '';

// 提交时自动关联主表ID
async function handleSubmit() {
  const values = await validate();
  if (unref({{mainEntityName_uncap}}Id)) {
    values.{{mainEntityName_uncap}}Id = unref({{mainEntityName_uncap}}Id);
  }
  await saveOrUpdateSub(values, isUpdate.value);
  closeModal();
  emit('success');
}
```

#### data.ts 差异

ERP 模式下子表使用 `BasicColumn[]`（普通列表列）+ `FormSchema[]`（Modal 表单），**不使用 JVxeColumn[]**：
```typescript
// ERP 模式：子表用 BasicColumn 列表展示
export const {{subEntityName_uncap}}Columns: BasicColumn[] = [
  { title: '字段名', align: 'center', dataIndex: 'fieldName' },
  { title: '字典字段', align: 'center', dataIndex: 'dictField_dictText' },
];

// ERP 模式：子表用 FormSchema 弹窗编辑（需包含隐藏的 id 和外键字段）
export const {{subEntityName_uncap}}FormSchema: FormSchema[] = [
  { label: '', field: 'id', component: 'Input', show: false },
  { label: '', field: '{{mainEntityName_uncap}}Id', component: 'Input', show: false },
  // ... 业务字段
];
```

#### 一对一子表特殊处理

一对一关系的子表需要限制只能有一条记录：

```typescript
// 子表 List.vue — 一对一限制
const hasDetailRecord = ref(false);

// useListPage tableProps 中添加 afterFetch
afterFetch: (data) => {
  // 一对一：检查是否已有记录
  hasDetailRecord.value = data && data.length > 0;
  return data;
},

// 新增按钮添加 :disabled="hasDetailRecord"
// <a-button type="primary" @click="handleCreate" :disabled="hasDetailRecord">新增</a-button>

// handleCreate 增加双重检查
function handleCreate() {
  if (isEmpty(unref({{mainEntityName_uncap}}Id))) {
    $message.createMessage.warning('请选择一条主表记录');
    return;
  }
  if (hasDetailRecord.value) {
    $message.createMessage.warning('一对一子表只能有一条记录，请编辑现有记录');
    return;
  }
  openModal(true, { isUpdate: false, showFooter: true });
}

// 包装API中也要重置标记
async function getSubList(params) {
  let { {{mainEntityName_uncap}}Id } = params;
  if ({{mainEntityName_uncap}}Id == null || isEmpty({{mainEntityName_uncap}}Id)) {
    hasDetailRecord.value = false;
    return [];
  }
  return await subListApi(params);
}
```

---

### C12. 内嵌子表风格（expandedRowRender + Tab-in-Modal）

内嵌子表风格是 Tab-in-Modal 的增强版：**Modal 内编辑子表数据**（与 C9 相同），同时在**列表页通过行展开（expandedRowRender）直接查看子表数据**，无需打开 Modal。

#### 架构对比

| 维度 | Tab-in-Modal 风格（C9） | 内嵌子表风格（C12） | ERP 风格（C11） |
|------|------------------------|-------------------|----------------|
| 列表页子表展示 | 无（需点编辑/详情才能看） | **行展开 expandedRowRender** | 主表下方 Tab 页签 |
| Modal 布局 | **radio-group 标题栏切换**（主表/子表同级） | **上面主表 + 下面子表 a-tabs**（原始布局） | 仅主表字段 |
| 保存方式 | 整体提交（saveMain） | 整体提交（同C9） | 子表独立保存 |
| 子表数据传递 | useJvxeMethod | useJvxeMethod + SubTable组件 | provide/inject |

#### 前端文件结构

```
views/{{viewDir}}/
├── {{entityName}}List.vue                   # 主列表（含 expandedRowRender）
├── {{entityName}}.api.ts                    # API（主表CRUD + 子表双重导出）
├── {{entityName}}.data.ts                   # 列/表单配置（子表三种导出）
├── components/
│   └── {{entityName}}Modal.vue              # Modal（主表 + 子表 Tab 编辑，同C9）
└── subTables/
    ├── {{subEntityName}}SubTable.vue        # 子表展示组件（每个子表一个）
    └── {{detailEntityName}}SubTable.vue     # 一对一子表展示组件
```

#### data.ts — 子表三种数据导出

每个子表需要导出**两种或三种**列定义，分别用于不同场景：

```typescript
import { JVxeTypes, JVxeColumn } from '/@/components/jeecg/JVxeTable/types';

// ===== 一对多子表 =====

// 1. BasicColumn[] — 供 SubTable 组件展示（行展开时的只读列表）
export const {{subEntityName_uncap}}Columns: BasicColumn[] = [
  { title: '字段标题', align: 'center', dataIndex: 'fieldName' },
  { title: '字典字段', align: 'center', dataIndex: 'dictField_dictText' },
];

// 2. JVxeColumn[] — 供 Modal 内 JVxeTable 编辑（可编辑表格）
export const {{subEntityName_uncap}}JVxeColumns: JVxeColumn[] = [
  { title: '字段标题', key: 'fieldName', type: JVxeTypes.input, width: '200px',
    placeholder: '请输入${title}', defaultValue: '' },
  { title: '字典字段', key: 'dictField', type: JVxeTypes.select, width: '200px',
    dictCode: 'dict_code', placeholder: '请选择${title}' },
];

// ===== 一对一子表 =====

// 1. BasicColumn[] — 供 SubTable 组件展示
export const {{detailEntityName_uncap}}Columns: BasicColumn[] = [
  { title: '字段标题', align: 'center', dataIndex: 'fieldName' },
];

// 2. FormSchema[] — 供 Modal 内 BasicForm 编辑（一对一不用 JVxeTable）
export const {{detailEntityName_uncap}}FormSchema: FormSchema[] = [
  { label: '字段标题', field: 'fieldName', component: 'Input' },
];
// 注意：一对一的 FormSchema 不需要隐藏的 id 和外键字段（与 ERP 模式不同）
```

#### api.ts — 子表双重导出

```typescript
enum Api {
  // 主表 CRUD
  list = '/{{entityPackagePath}}/{{entityName_uncap}}/list',
  save = '/{{entityPackagePath}}/{{entityName_uncap}}/add',
  edit = '/{{entityPackagePath}}/{{entityName_uncap}}/edit',
  deleteOne = '/{{entityPackagePath}}/{{entityName_uncap}}/delete',
  deleteBatch = '/{{entityPackagePath}}/{{entityName_uncap}}/deleteBatch',
  importExcel = '/{{entityPackagePath}}/{{entityName_uncap}}/importExcel',
  exportXls = '/{{entityPackagePath}}/{{entityName_uncap}}/exportXls',
  // 子表查询（非分页接口，返回 Result<List>）
  {{subEntityName_uncap}}List = '/{{entityPackagePath}}/{{entityName_uncap}}/query{{subEntityName}}ByMainId',
  {{detailEntityName_uncap}}List = '/{{entityPackagePath}}/{{entityName_uncap}}/query{{detailEntityName}}ByMainId',
}

// === 导出方式1：URL 字符串（供 Modal 中 requestSubTableData / defHttp.get 使用） ===
export const query{{subEntityName}}ByMainId = Api.{{subEntityName_uncap}}List;
export const query{{detailEntityName}}ByMainId = Api.{{detailEntityName_uncap}}List;

// === 导出方式2：函数（供 SubTable 组件直接调用，isTransformResponse:false 返回完整响应） ===
export const {{subEntityName_uncap}}ListApi = (params) =>
  defHttp.get({ url: Api.{{subEntityName_uncap}}List, params }, { isTransformResponse: false });
export const {{detailEntityName_uncap}}ListApi = (params) =>
  defHttp.get({ url: Api.{{detailEntityName_uncap}}List, params }, { isTransformResponse: false });
```

**为什么需要双重导出？**
- **URL 字符串**：Modal 中 `requestSubTableData(urlString, params, table)` 和 `defHttp.get({ url: urlString })` 需要纯 URL
- **函数导出**：SubTable 组件需要完整的 `{ success, result, message }` 响应对象来判断请求是否成功，所以设 `isTransformResponse: false`

#### SubTable 组件模板（subTables/{{subEntityName}}SubTable.vue）

```html
<template>
  <div>
    <BasicTable bordered size="middle" :loading="loading" rowKey="id" :canResize="true"
      :columns="{{subEntityName_uncap}}Columns" :dataSource="dataSource" :pagination="false">
      <!--字段回显插槽-->
      <template v-slot:bodyCell="{ column, record, index, text }">
        <!-- 文件字段 -->
        <template v-if="column.dataIndex==='fileField'">
          <span v-if="!text" style="font-size: 12px;font-style: italic;">无文件</span>
          <a-button v-else :ghost="true" type="primary" preIcon="ant-design:download-outlined"
            size="small" @click="downloadFile(text)">下载</a-button>
        </template>
        <!-- 富文本字段 -->
        <template v-if="column.dataIndex==='richTextField'">
          <div v-html="text"></div>
        </template>
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" setup>
  import { ref, watchEffect } from 'vue';
  import { BasicTable } from '/@/components/Table';
  import { {{subEntityName_uncap}}Columns } from '../{{entityName}}.data';
  import { {{subEntityName_uncap}}ListApi } from '../{{entityName}}.api';
  // import { downloadFile } from '/@/utils/common/renderUtils'; // 有文件字段时需要

  const props = defineProps({
    id: {
      type: String,
      default: '',
    },
  });

  const loading = ref(false);
  const dataSource = ref([]);

  watchEffect(() => {
    props.id && loadData(props.id);
  });

  function loadData(id) {
    dataSource.value = [];
    loading.value = true;
    {{subEntityName_uncap}}ListApi({ id })
      .then((res) => {
        if (res.success) {
          dataSource.value = res.result;
        }
      })
      .finally(() => {
        loading.value = false;
      });
  }
</script>
```

**SubTable 关键点：**
- `watchEffect` 监听 `props.id` 变化自动重新加载
- API 使用函数导出（`xxxListApi`），返回完整响应 `{ success, result }`
- `dataSource.value = res.result` — 非分页接口直接返回数组
- `:pagination="false"` — 子表不分页

#### List.vue — expandedRowRender 模板

```html
<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection"
      :expandedRowKeys="expandedRowKeys" @expand="handleExpand">
      <!-- 内嵌table区域 begin -->
      <template #expandedRowRender="{ record }">
        <a-tabs tabPosition="top">
          <a-tab-pane tab="{{detailDescription}}" key="{{detailEntityName_uncap}}" forceRender>
            <{{detailEntityName}}SubTable v-if="expandedRowKeys.includes(record.id)" :id="record.id" />
          </a-tab-pane>
          <a-tab-pane tab="{{subDescription}}" key="{{subEntityName_uncap}}" forceRender>
            <{{subEntityName}}SubTable v-if="expandedRowKeys.includes(record.id)" :id="record.id" />
          </a-tab-pane>
        </a-tabs>
      </template>
      <!-- 内嵌table区域 end -->
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">新增</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls">导出</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined" />
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>批量操作<Icon icon="mdi:chevron-down" /></a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="{ column, record, index, text }">
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <{{entityName}}Modal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  import {{entityName}}Modal from './components/{{entityName}}Modal.vue';
  import {{detailEntityName}}SubTable from './subTables/{{detailEntityName}}SubTable.vue';
  import {{subEntityName}}SubTable from './subTables/{{subEntityName}}SubTable.vue';
  import { columns, searchFormSchema } from './{{entityName}}.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './{{entityName}}.api';

  const [registerModal, { openModal }] = useModal();
  // 展开key — 控制哪些行展开
  const expandedRowKeys = ref<any[]>([]);

  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: '{{description}}',
      api: list,
      columns,
      canResize: false,
      formConfig: {
        schemas: searchFormSchema,
        autoSubmitOnEnter: true,
      },
      actionColumn: {
        width: 120,
        fixed: 'right',
      },
    },
    exportConfig: { name: '{{description}}', url: getExportUrl },
    importConfig: { url: getImportUrl, success: handleSuccess },
  });

  const [registerTable, { reload, collapseAll, updateTableDataRecord, deleteTableDataRecord },
    { rowSelection, selectedRowKeys, selectedRows }] = tableContext;

  /**
   * 展开事件 — 同时只展开一行
   */
  function handleExpand(expanded, record) {
    expandedRowKeys.value = [];
    if (expanded === true) {
      expandedRowKeys.value.push(record.id);
    }
  }

  function handleAdd() {
    openModal(true, { isUpdate: false, showFooter: true });
  }

  function handleEdit(record: Recordable) {
    openModal(true, { record, isUpdate: true, showFooter: true });
  }

  function handleDetail(record: Recordable) {
    openModal(true, { record, isUpdate: true, showFooter: false });
  }

  async function handleDelete(record) {
    await deleteOne({ id: record.id }, handleSuccess);
  }

  async function batchHandleDelete() {
    await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
  }

  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }

  function getTableAction(record) {
    return [{ label: '编辑', onClick: handleEdit.bind(null, record) }];
  }

  function getDropDownAction(record) {
    return [
      { label: '详情', onClick: handleDetail.bind(null, record) },
      { label: '删除', popConfirm: { title: '是否确认删除', confirm: handleDelete.bind(null, record), placement: 'topLeft' } },
    ];
  }
</script>
```

**List.vue 关键点：**
- `expandedRowKeys` + `handleExpand`：同时只展开一行，避免多行展开时数据混乱
- `v-if="expandedRowKeys.includes(record.id)"`：仅在展开时渲染 SubTable，避免不必要的 API 请求
- `handleSuccess` 使用 `(selectedRowKeys.value = []) && reload()` 模式（标准风格，非 ERP 的 `reload()` 不清空选中）
- 不使用 `provide/inject`，子表数据通过 SubTable 的 `id` prop 传递

#### Modal.vue — 原始布局（上面主表，下面子表 Tabs）

**内嵌子表风格和 ERP 风格的 Modal 不使用 radio-group 标题栏切换**，而是采用原始布局：
- 上方：主表 BasicForm（全宽展示）
- 下方：子表区域，多个子表用 `a-tabs` 组织（注意是 **内容区的 a-tabs**，不是标题栏的 radio-group）
- 不需要 `wrapClassName="j-cgform-tab-modal"`
- 不需要 `v-show` 切换（主表始终可见）

**只有 Tab-in-Modal 风格（C9）才使用 radio-group 标题栏切换！**

##### 纯一对多 Modal

```html
<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="896" @ok="handleSubmit">
    <!-- 主表表单 -->
    <BasicForm @register="registerForm" ref="formRef" name="{{entityName}}Form"/>
    <!-- 子表区域 -->
    <a-tabs v-model:activeKey="activeKey">
      <a-tab-pane tab="{{subDescription}}" key="{{subEntityName_uncap}}" forceRender>
        <JVxeTable
          keep-source resizable
          ref="{{subEntityName_uncap}}"
          :loading="{{subEntityName_uncap}}Table.loading"
          :columns="{{subEntityName_uncap}}Table.columns"
          :dataSource="{{subEntityName_uncap}}Table.dataSource"
          :height="340"
          :disabled="formDisabled"
          :rowNumber="true" :rowSelection="true" :toolbar="true"
        />
      </a-tab-pane>
    </a-tabs>
  </BasicModal>
</template>

<script lang="ts" setup>
    import {ref, computed, unref, reactive} from 'vue';
    import {BasicModal, useModalInner} from '/@/components/Modal';
    import {BasicForm, useForm} from '/@/components/Form/index';
    import { useJvxeMethod } from '/@/hooks/system/useJvxeMethods.ts'
    import {formSchema, {{subEntityName_uncap}}JVxeColumns} from '../{{entityName}}.data';
    import {saveOrUpdate, query{{subEntityName}}ByMainId} from '../{{entityName}}.api';

    const emit = defineEmits(['register','success']);
    const isUpdate = ref(true);
    const formDisabled = ref(false);
    const refKeys = ref(['{{subEntityName_uncap}}']);
    const activeKey = ref('{{subEntityName_uncap}}');
    const {{subEntityName_uncap}} = ref();
    const tableRefs = { {{subEntityName_uncap}} };
    const {{subEntityName_uncap}}Table = reactive({
          loading: false,
          dataSource: [],
          columns: {{subEntityName_uncap}}JVxeColumns
    })
    const [registerForm, {setProps, resetFields, setFieldsValue, validate}] = useForm({
        schemas: formSchema,
        showActionButtonGroup: false,
        baseColProps: {span: 12}
    });
    const [registerModal, {setModalProps, closeModal}] = useModalInner(async (data) => {
        await reset();
        setModalProps({confirmLoading: false, showCancelBtn: data?.showFooter, showOkBtn: data?.showFooter});
        isUpdate.value = !!data?.isUpdate;
        formDisabled.value = !data?.showFooter;
        if (unref(isUpdate)) {
            await setFieldsValue({ ...data.record });
            requestSubTableData(query{{subEntityName}}ByMainId, {id: data?.record?.id}, {{subEntityName_uncap}}Table)
        }
        setProps({ disabled: !data?.showFooter })
    });
    const [handleChangeTabs, handleSubmit, requestSubTableData, formRef] = useJvxeMethod(requestAddOrEdit, classifyIntoFormData, tableRefs, activeKey, refKeys);

    const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(formDisabled) ? '编辑' : '详情'));
    async function reset(){
      await resetFields();
      activeKey.value = '{{subEntityName_uncap}}';
      {{subEntityName_uncap}}Table.dataSource = [];
    }
    function classifyIntoFormData(allValues) {
         let main = Object.assign({}, allValues.formValue)
         return {
           ...main,
           {{subEntityName_uncap}}List: allValues.tablesValue[0].tableData,
         }
    }
    async function requestAddOrEdit(values) {
        try {
            setModalProps({confirmLoading: true});
            await saveOrUpdate(values, isUpdate.value);
            closeModal();
            emit('success');
        } finally {
            setModalProps({confirmLoading: false});
        }
    }
</script>

<style lang="less" scoped>
  :deep(.ant-input-number) { width: 100%; }
  :deep(.ant-calendar-picker) { width: 100%; }
</style>
```

##### 一对一 + 一对多混合 Modal

```html
<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="896" @ok="handleSubmit">
    <!-- 主表表单 -->
    <BasicForm @register="registerForm" ref="formRef" name="{{entityName}}Form"/>
    <!-- 子表区域 -->
    <a-tabs v-model:activeKey="activeKey">
      <a-tab-pane tab="{{subDescription}}" key="{{subEntityName_uncap}}" forceRender>
        <JVxeTable
          keep-source resizable
          ref="{{subEntityName_uncap}}"
          :loading="{{subEntityName_uncap}}Table.loading"
          :columns="{{subEntityName_uncap}}Table.columns"
          :dataSource="{{subEntityName_uncap}}Table.dataSource"
          :height="340"
          :disabled="formDisabled"
          :rowNumber="true" :rowSelection="true" :toolbar="true"
        />
      </a-tab-pane>
      <a-tab-pane tab="{{detailDescription}}" key="{{detailEntityName_uncap}}" forceRender>
        <{{detailEntityName}}Form ref="{{detailEntityName_uncap}}Form" :disabled="formDisabled" />
      </a-tab-pane>
    </a-tabs>
  </BasicModal>
</template>

<script lang="ts" setup>
    import {ref, computed, unref, reactive} from 'vue';
    import {BasicModal, useModalInner} from '/@/components/Modal';
    import {BasicForm, useForm} from '/@/components/Form/index';
    import { useJvxeMethod } from '/@/hooks/system/useJvxeMethods.ts'
    import {{detailEntityName}}Form from './{{detailEntityName}}Form.vue'
    import {formSchema, {{subEntityName_uncap}}JVxeColumns} from '../{{entityName}}.data';
    import {saveOrUpdate, query{{subEntityName}}ByMainId, query{{detailEntityName}}ByMainId} from '../{{entityName}}.api';
    import { VALIDATE_FAILED } from '/@/utils/common/vxeUtils'

    const emit = defineEmits(['register','success']);
    const isUpdate = ref(true);
    const formDisabled = ref(false);
    const refKeys = ref(['{{subEntityName_uncap}}', '{{detailEntityName_uncap}}']);
    const activeKey = ref('{{subEntityName_uncap}}');
    const {{subEntityName_uncap}} = ref();
    const {{detailEntityName_uncap}}Form = ref();
    const tableRefs = { {{subEntityName_uncap}} };
    const {{subEntityName_uncap}}Table = reactive({
          loading: false,
          dataSource: [],
          columns: {{subEntityName_uncap}}JVxeColumns
    })
    const [registerForm, {setProps, resetFields, setFieldsValue, validate}] = useForm({
        schemas: formSchema,
        showActionButtonGroup: false,
        baseColProps: {span: 12}
    });
    const [registerModal, {setModalProps, closeModal}] = useModalInner(async (data) => {
        await reset();
        setModalProps({confirmLoading: false, showCancelBtn: data?.showFooter, showOkBtn: data?.showFooter});
        isUpdate.value = !!data?.isUpdate;
        formDisabled.value = !data?.showFooter;
        if (unref(isUpdate)) {
            await setFieldsValue({ ...data.record });
            {{detailEntityName_uncap}}Form.value.initFormData(query{{detailEntityName}}ByMainId, data?.record?.id)
            requestSubTableData(query{{subEntityName}}ByMainId, {id: data?.record?.id}, {{subEntityName_uncap}}Table)
        }
        setProps({ disabled: !data?.showFooter })
    });
    // 第6个参数 validateSubForm 用于校验一对一子表
    const [handleChangeTabs, handleSubmit, requestSubTableData, formRef] = useJvxeMethod(requestAddOrEdit, classifyIntoFormData, tableRefs, activeKey, refKeys, validateSubForm);

    const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(formDisabled) ? '编辑' : '详情'));
    async function reset(){
      await resetFields();
      activeKey.value = '{{subEntityName_uncap}}';
      {{subEntityName_uncap}}Table.dataSource = [];
      {{detailEntityName_uncap}}Form.value.resetFields();
    }
    function classifyIntoFormData(allValues) {
         let main = Object.assign({}, allValues.formValue)
         return {
           ...main,
           {{subEntityName_uncap}}List: allValues.tablesValue[0].tableData,
           {{detailEntityName_uncap}}List: {{detailEntityName_uncap}}Form.value.getFormData(),
         }
    }
    function validateSubForm(allValues){
        return new Promise((resolve,reject)=>{
            Promise.all([
                 {{detailEntityName_uncap}}Form.value.validateForm(1),
            ]).then(() => {
                resolve(allValues)
            }).catch(e => {
                if (e.error === VALIDATE_FAILED) {
                    activeKey.value = e.index == null ? unref(activeKey) : refKeys.value[e.index]
                } else {
                    console.error(e)
                }
            })
        })
    }
    async function requestAddOrEdit(values) {
        try {
            setModalProps({confirmLoading: true});
            await saveOrUpdate(values, isUpdate.value);
            closeModal();
            emit('success');
        } finally {
            setModalProps({confirmLoading: false});
        }
    }
</script>

<style lang="less" scoped>
  :deep(.ant-input-number) { width: 100%; }
  :deep(.ant-calendar-picker) { width: 100%; }
</style>
```

**原始布局 Modal 与 Tab-in-Modal (C9) 的关键区别：**
- **无 `wrapClassName="j-cgform-tab-modal"`**，无 `.titleArea`/`.contentArea` 样式
- **无 `#title` 插槽**，标题用普通 `:title="title"` 属性
- **无 radio-group**，主表 BasicForm 始终显示在上方
- 子表区域使用 **`a-tabs`**（内容区的标签页），而非标题栏的 radio-group
- `refKeys` **不包含主表 key**（只有子表 key），因为主表不参与 Tab 切换
- `validateSubForm` 中 `validateForm(1)` 的 index 对应 `refKeys` 中一对一子表的位置（从0开始，0=一对多，1=一对一）

#### 内嵌子表与其他风格的生成差异速查

| 生成项 | 内嵌子表（C12） | Tab-in-Modal（C9） | ERP（C11） |
|--------|---------------|-------------------|-----------|
| SubTable 组件 | **需要生成** | 不需要 | 不需要 |
| subTables/ 目录 | **需要创建** | 不需要 | 不需要 |
| api.ts 子表导出 | **URL字符串 + 函数双重导出** | 仅 URL 字符串 | 仅函数导出 |
| data.ts 子表列 | **BasicColumn[] + JVxeColumn[]** | 仅 JVxeColumn[] | 仅 BasicColumn[] + FormSchema[] |
| List.vue expandedRowRender | **需要** | 不需要 | 不需要 |
| List.vue rowSelection | 多选（默认） | 多选（默认） | 单选 radio |
| Modal 布局 | **原始布局**（上面主表 + 下面子表 a-tabs） | **radio-group 标题栏**切换 | 仅主表字段 |
| Modal wrapClassName | 无 | `j-cgform-tab-modal` | 无 |
| Modal refKeys | **只有子表 key** | 包含主表 + 子表 key | 无 |
| 后端接口 | `/add`(saveMain) `/edit`(updateMain) | 同左 | `/addMain`(save) `/editMain`(updateById) |

---

### C13. vue3Native 原生风格一对多（默认布局）

vue3Native 一对多与 vue3 封装风格（C9/C12）的架构**完全不同**。核心区别：**Form 组件（不是 Modal）** 包含主表表单和子表 Tabs，Modal 只是一个薄包装器。

#### 架构对比

| 维度 | vue3 封装风格（C9 Tab-in-Modal） | vue3Native 原生风格 |
|------|-------------------------------|-------------------|
| **Modal 职责** | 包含 BasicForm + JVxeTable + SubForm，负责数据编排和提交 | **薄包装器**，只调 `formComponent.submitForm()/edit()/add()` |
| **Form 职责** | 仅主表字段（单表场景才有 Form.vue） | **核心组件**，包含主表 a-form + 子表 a-tabs + 数据提交逻辑 |
| **数据收集 hook** | `useJvxeMethod` | `useValidateAntFormAndTable` |
| **主表表单** | `BasicForm` + `useForm` from `/@/components/Form/index` | 原生 `a-form` + `Form.useForm` from `ant-design-vue` |
| **一对一子表** | `BasicForm` + `useForm`，`initFormData(url, id)` 传 URL 字符串 | 原生 `a-form` + `Form.useForm`，`initFormData(mainId)` 传主表 ID |
| **子表 API 导出** | URL 字符串（`export const xxxList = Api.xxxList`） | **函数导出**（`export const queryXxxByMainId = (id) => defHttp.get({...})`） |
| **saveOrUpdate** | 普通 `defHttp.post({url, params})` | 同左，**不需要** `isTransformResponse: false` |
| **data.ts** | 有 formSchema（主表）+ JVxeColumn[]（子表）+ FormSchema[]（一对一子表） | **无 formSchema**（主表在模板中写），只有 JVxeColumn[]（子表），**无 FormSchema[]**（一对一子表也在模板中写） |
| **List.vue** | `useModal` + `openModal(true, {...})` | 同左 |
| **refKeys** | 包含主表 key + 子表 key | **不需要 refKeys** |

#### 文件结构

```
views/{{viewDir}}/
├── {{entityName}}List.vue                   # 主列表（useModal + openModal）
├── {{entityName}}.api.ts                    # API（子表导出为函数，需要 queryDataById）
├── {{entityName}}.data.ts                   # 仅 columns + superQuerySchema + JVxeColumn[]（无 formSchema）
└── components/
    ├── {{entityName}}Modal.vue              # 薄包装器（BasicModal + useModalInner）
    ├── {{entityName}}Form.vue               # 核心组件（主表 a-form + 子表 a-tabs + useValidateAntFormAndTable）
    └── {{detailEntityName}}Form.vue         # 一对一子表原生 Form（仅有一对一子表时）
```

#### C13-1. api.ts 差异

```typescript
import { defHttp } from '/@/utils/http/axios';

enum Api {
  // ... 主表 CRUD 接口
  queryDataById = '/{{entityPackagePath}}/{{entityName_uncap}}/queryById',
  {{subEntityName_uncap}}List = '/{{entityPackagePath}}/{{entityName_uncap}}/query{{subEntityName}}ByMainId',
  {{detailEntityName_uncap}}List = '/{{entityPackagePath}}/{{entityName_uncap}}/query{{detailEntityName}}ByMainId',
}

// ✅ saveOrUpdate 不需要 isTransformResponse: false
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};

// ✅ 需要 queryDataById（Form.vue 编辑时重新查询主表数据）
export const queryDataById = (id) => defHttp.get({ url: Api.queryDataById, params: { id } });

// ✅ 子表导出为函数（不是 URL 字符串！）
export const query{{subEntityName}}ListByMainId = (id) => defHttp.get({ url: Api.{{subEntityName_uncap}}List, params: { id } });
export const query{{detailEntityName}}ListByMainId = (id) => defHttp.get({ url: Api.{{detailEntityName_uncap}}List, params: { id } });
```

**与 vue3 封装风格的关键差异：**
- `saveOrUpdate` **不用** `{ isTransformResponse: false }`
- 子表查询导出为**函数**（`(id) => defHttp.get({...})`），不是 URL 字符串
- 需要额外的 `queryDataById` 函数（Form 编辑时 `queryMainData(id)` 调用）

#### C13-2. data.ts 差异

vue3Native 的 data.ts **只包含 columns、superQuerySchema 和 JVxeColumn[]**，不包含 formSchema 和一对一子表的 FormSchema：

```typescript
import { BasicColumn } from '/@/components/Table';
import { JVxeTypes, JVxeColumn } from '/@/components/jeecg/JVxeTable/types';

// 主表列定义（与单表相同）
export const columns: BasicColumn[] = [ /* ... */ ];

// 高级查询（与单表相同）
export const superQuerySchema = { /* ... */ };

// 一对多子表列定义（JVxeColumn[] 与 vue3 封装风格相同）
export const {{subEntityName_uncap}}Columns: JVxeColumn[] = [ /* ... */ ];

// ❌ 不需要 formSchema（主表表单在 Form.vue 模板中直接写 a-form）
// ❌ 不需要 {{detailEntityName_uncap}}FormSchema（一对一子表在 DetailForm.vue 模板中写 a-form）
```

#### C13-3. Modal.vue — 薄包装器

```html
<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" maxHeight="500px" :width="800" @ok="handleSubmit">
    <{{entityName_uncap}}-form ref="formComponent" :formDisabled="formDisabled" :formBpm="false" @success="submitSuccess"><\/{{entityName_uncap}}-form>
  </BasicModal>
</template>

<script lang="ts">
  import { ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import {{entityName}}Form from './{{entityName}}Form.vue';

  export default {
    name: "{{entityName}}Modal",
    components:{ BasicModal, {{entityName}}Form },
    emits:['register','success'],
    setup(_p, {emit}){
      const formComponent = ref()
      const isUpdate = ref(true);
      const formDisabled = ref(false);
      const title = ref('')

      const [registerModal, {setModalProps, closeModal}] = useModalInner(async (data) => {
        setModalProps({confirmLoading: false, showCancelBtn: data?.showFooter, showOkBtn: data?.showFooter});
        isUpdate.value = !!data?.isUpdate;
        formDisabled.value = !data?.showFooter;
        title.value = data?.isUpdate ? (unref(formDisabled) ? '详情' : '编辑') : '新增';
        if (unref(isUpdate)) {
          formComponent.value.edit(data.record)
        } else {
          formComponent.value.add()
        }
      });

      function handleSubmit() {
        formComponent.value.submitForm();
      }

      function submitSuccess(){
        emit('success');
        closeModal();
      }
      return { registerModal, title, formComponent, formDisabled, handleSubmit, submitSuccess }
    }
  }
</script>
```

**关键点：**
- **不用** `<script setup>`，使用 Options API（`export default {...}`）
- Modal 只做三件事：(1) 新增时调 `formComponent.value.add()`，(2) 编辑时调 `formComponent.value.edit(data.record)`，(3) 提交时调 `formComponent.value.submitForm()`
- `submitSuccess` 由 Form 组件 `emit('success')` 触发，然后 Modal 关闭并通知 List 刷新

#### C13-4. Form.vue — 核心组件（主表 + 子表 + 提交逻辑）

```html
<template>
  <a-spin :spinning="loading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form v-bind="formItemLayout" name="{{entityName}}Form" ref="formRef" class="jeecg-native-form">
          <a-row>
            <!-- 主表字段，与单表 Form.vue 相同 -->
            <a-col :span="24">
              <a-form-item label="字段名" v-bind="validateInfos.fieldName" id="{{entityName}}Form-fieldName" name="fieldName">
                <a-input v-model:value="formData.fieldName" placeholder="请输入" allow-clear />
              </a-form-item>
            </a-col>
            <!-- ... 其他主表字段 ... -->
          </a-row>
        </a-form>
      </template>
    </JFormContainer>

    <!-- ✅ 子表区域在 Form 中，不是在 Modal 中 -->
    <a-tabs v-model:activeKey="activeKey" animated style="overflow:hidden;" class="jeecg-native-tab">
      <!-- 一对多子表 Tab -->
      <a-tab-pane tab="{{subDescription}}" key="{{subEntityName_uncap}}" :forceRender="true">
        <j-vxe-table
          :keep-source="true" resizable
          ref="{{subEntityName_uncap}}TableRef"
          :loading="{{subEntityName_uncap}}Table.loading"
          :columns="{{subEntityName_uncap}}Table.columns"
          :dataSource="{{subEntityName_uncap}}Table.dataSource"
          :height="340" :disabled="disabled"
          :rowNumber="true" :rowSelection="true" :toolbar="true"/>
      </a-tab-pane>
      <!-- 一对一子表 Tab -->
      <a-tab-pane class="sub-one-form" tab="{{detailDescription}}" key="{{detailEntityName_uncap}}" :forceRender="true">
        <{{detailEntityName_uncap}}-form ref="{{detailEntityName_uncap}}FormRef" :disabled="disabled"><\/{{detailEntityName_uncap}}-form>
      </a-tab-pane>
    </a-tabs>
    <!-- 流程表单提交按钮（仅 BPM 模式可见） -->
    <div v-if="showFlowSubmitButton" :span="24" style="width: 100%;text-align: center;margin-top: 10px">
      <a-button preIcon="ant-design:check-outlined" style="width: 126px" type="primary" @click="submitForm">提 交</a-button>
    </div>
  </a-spin>
</template>

<script lang="ts">
  import { defineComponent, ref, reactive, computed, toRaw, onMounted } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { useValidateAntFormAndTable } from '/@/hooks/system/useJvxeMethods';
  import { query{{subEntityName}}ListByMainId, queryDataById, saveOrUpdate } from '../{{entityName}}.api';
  import {{detailEntityName}}Form from './{{detailEntityName}}Form.vue';
  import { {{subEntityName_uncap}}Columns } from '../{{entityName}}.data';
  // ... import 表单控件组件 ...
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  import { Form } from 'ant-design-vue';
  const useForm = Form.useForm;

  export default defineComponent({
    name: "{{entityName}}Form",
    components:{ JFormContainer, {{detailEntityName}}Form, /* ... 其他控件 ... */ },
    props:{
      formDisabled: { type: Boolean, default: false },
      formData: { type: Object, default: ()=>{} },
      formBpm: { type: Boolean, default: true },
      showSubmitButton: { type: Boolean, default: true },
    },
    emits:['success'],
    setup(props, {emit}) {
      const loading = ref(false);
      const formRef = ref();
      const {{detailEntityName_uncap}}FormRef = ref();
      const {{subEntityName_uncap}}TableRef = ref();
      const {{subEntityName_uncap}}Table = reactive<Record<string, any>>({
        loading: false,
        columns: {{subEntityName_uncap}}Columns,
        dataSource: []
      });
      const activeKey = ref('{{subEntityName_uncap}}');
      const formData = reactive<Record<string, any>>({
        id: '',
        // ... 主表字段
      });

      const validatorRules = reactive({ /* 校验规则 */ });
      const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });
      const dbData = {};
      const formItemLayout = {
        labelCol: { xs: { span: 24 }, sm: { span: 5 } },
        wrapperCol: { xs: { span: 24 }, sm: { span: 16 } },
      };

      const disabled = computed(() => {
        if (props.formBpm === true) {
          return props.formData.disabled !== false;
        }
        return props.formDisabled;
      });

      // ✅ 使用 useValidateAntFormAndTable（不是 useJvxeMethod）
      // key 与 a-tabs 的 activeKey 对应，value 是子组件的 ref
      const { getSubFormAndTableData, transformData } = useValidateAntFormAndTable(activeKey, {
        '{{detailEntityName_uncap}}': {{detailEntityName_uncap}}FormRef,  // 一对一子表（isForm = true）
        '{{subEntityName_uncap}}': {{subEntityName_uncap}}TableRef,       // 一对多子表（JVxeTable ref）
      });

      function add() {
        resetFields();
        {{detailEntityName_uncap}}FormRef.value.initFormData();  // 无参数 = 新增
        {{subEntityName_uncap}}Table.dataSource = [];
      }

      async function edit(row) {
        // 1. 主表数据
        await queryMainData(row.id);
        // 2. 一对一子表数据
        await {{detailEntityName_uncap}}FormRef.value.initFormData(row['id']);
        // 3. 一对多子表数据
        const subDataList = await query{{subEntityName}}ListByMainId(row['id']);
        {{subEntityName_uncap}}Table.dataSource = [...subDataList];
      }

      async function queryMainData(id) {
        const row = await queryDataById(id);
        resetFields();
        const tmpData = {};
        Object.keys(formData).forEach((key) => {
          if (row.hasOwnProperty(key)) {
            tmpData[key] = row[key];
          }
        });
        Object.assign(formData, tmpData);
      }

      async function getFormData() {
        try {
          await validate();
        } catch ({ errorFields }) {
          if (errorFields) {
            const firstField = errorFields[0];
            if (firstField) {
              formRef.value.scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
            }
          }
          return Promise.reject(errorFields);
        }
        return transformData(toRaw(formData));
      }

      // ✅ 提交逻辑在 Form 中，不在 Modal 中
      async function submitForm() {
        const mainData = await getFormData();
        const subData = await getSubFormAndTableData();
        const values = Object.assign({}, dbData, mainData, subData);
        const isUpdate = values.id ? true : false;
        await saveOrUpdate(values, isUpdate);
        emit('success');
      }

      function setFieldsValue(values) {
        if (values) {
          Object.keys(values).map(k => { formData[k] = values[k]; });
        }
      }

      // 流程表单 BPM 相关（与单表 Form 相同）
      onMounted(() => { /* initFormData for BPM */ });
      const showFlowSubmitButton = computed(() => {
        return props.formBpm === true && props.showSubmitButton === true && props.formData.disabled === false;
      });

      return {
        {{detailEntityName_uncap}}FormRef, {{subEntityName_uncap}}TableRef, {{subEntityName_uncap}}Table,
        validatorRules, validateInfos, activeKey, loading, formData,
        setFieldsValue, formItemLayout, disabled, showFlowSubmitButton,
        getFormData, submitForm, add, edit, formRef,
      }
    }
  });
</script>
<style lang="less" scoped>
  .ant-tabs-tabpane.sub-one-form { max-height: 340px; overflow: auto; }
  .jeecg-native-form, .jeecg-native-tab { padding: 0 20px; }
</style>
```

**Form.vue 关键点：**
1. **必须用 `defineComponent`**（Options API），不能用 `<script setup>`，因为需要 `return` 暴露方法给 Modal 通过 `ref` 调用
2. **`useValidateAntFormAndTable`** 是核心 hook，它根据子组件是否有 `isForm = true` 属性来区分一对一表单和 JVxeTable
3. **`getSubFormAndTableData()`** 自动收集所有子表数据：一对一子表调用 `getFormData()` 取数据，一对多子表从 JVxeTable 取 `tableData`
4. **`transformData(toRaw(formData))`** 处理主表数据中的数组字段（逗号拼接）
5. 编辑时通过 `queryDataById(id)` 重新查询主表数据，不直接用 record（确保字段完整）
6. 子表数据加载通过**函数调用**（不是 `defHttp.get({url: urlString, ...})`）

#### C13-5. 一对一子表 Form（原生 a-form 风格）

```html
<template>
  <a-spin :spinning="loading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form v-bind="formItemLayout" name="{{detailEntityName}}Form" ref="formRef" class="antd-modal-form">
          <a-row>
            <!-- 一对一子表字段 -->
            <a-col :span="24">
              <a-form-item label="字段名" v-bind="validateInfos.fieldName" id="{{detailEntityName}}Form-fieldName" name="fieldName">
                <a-input v-model:value="formData.fieldName" placeholder="请输入" allow-clear />
              </a-form-item>
            </a-col>
            <!-- ... -->
          </a-row>
        </a-form>
      </template>
    </JFormContainer>
  </a-spin>
</template>

<script lang="ts">
  import { defineComponent, ref, reactive, toRaw } from 'vue';
  import { query{{detailEntityName}}ListByMainId } from '../{{entityName}}.api';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  import { Form } from 'ant-design-vue';
  const useForm = Form.useForm;

  export default defineComponent({
    name: '{{detailEntityName}}Form',
    components:{ JFormContainer },
    props:{
      disabled: { type: Boolean, default: false }
    },
    setup(){
      const isForm = true;  // ✅ 关键标记！useValidateAntFormAndTable 用此判断为表单而非 JVxeTable
      const loading = ref(false);
      const formRef = ref();
      const formData = reactive<Record<string, any>>({
        id: '',
        // ... 一对一子表字段
      });
      const validatorRules = reactive({ /* 校验规则 */ });
      const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });
      const formItemLayout = {
        labelCol: { xs: { span: 24 }, sm: { span: 5 } },
        wrapperCol: { xs: { span: 24 }, sm: { span: 16 } },
      };

      // ✅ 接收 mainId（不是 url + id），直接调用 API 函数
      async function initFormData(mainId) {
        resetFields();
        let tmpData = {};
        if (mainId) {
          let list = await query{{detailEntityName}}ListByMainId(mainId);
          if (list && list.length > 0) {
            let temp = list[0];
            Object.keys(formData).forEach((key) => {
              if (temp.hasOwnProperty(key)) {
                tmpData[key] = temp[key];
              }
            });
          }
        }
        Object.assign(formData, tmpData);
      }

      // ✅ getFormData 返回 toRaw(formData)（不是数组！）
      async function getFormData() {
        try {
          await validate();
        } catch ({ errorFields }) {
          if (errorFields) {
            const firstField = errorFields[0];
            if (firstField) {
              formRef.value.scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
            }
          }
          return Promise.reject(errorFields);
        }
        let subFormData = toRaw(formData);
        if (Object.keys(subFormData).length > 0) {
          return subFormData;
        }
        return false;
      }

      function setFieldsValue(values) {
        if (values) {
          Object.keys(values).map(k => { formData[k] = values[k]; });
        }
      }

      return {
        loading, formData, formItemLayout, initFormData, getFormData,
        setFieldsValue, isForm, validateInfos, formRef,
      }
    }
  });
</script>
```

**一对一子表 Form（vue3Native）与 vue3 封装风格的关键差异：**

| 维度 | vue3 封装风格 (C9) | vue3Native 原生风格 (C13) |
|------|-------------------|-------------------------|
| 模板 | `<BasicForm @register="registerForm" />` | 原生 `<a-form>` + `<JFormContainer>` + 手写每个 `<a-form-item>` |
| useForm 来源 | `import { useForm } from '/@/components/Form/index'` | `import { Form } from 'ant-design-vue'; Form.useForm` |
| `initFormData` 参数 | `(url, id)` — 传 URL 字符串 + 主表 ID | `(mainId)` — 只传主表 ID，内部直接调用 API 函数 |
| `initFormData` 实现 | `defHttp.get({url, params:{id}}, {isTransformResponse:false})` 取 `res.result[0]` | `await queryXxxByMainId(mainId)` 取 `list[0]` |
| `getFormData` 返回值 | **数组** `[formData]` | **对象** `toRaw(formData)`（`useValidateAntFormAndTable` 内部处理包装） |
| 暴露 `isForm` 属性 | 不需要 | **必须** `const isForm = true` 并 return |
| 暴露 `validateForm` | 需要（`useJvxeMethod` 的 `validateSubForm` 回调会调用） | 不需要（`useValidateAntFormAndTable` 内部自动校验） |

#### C13-6. List.vue 差异

vue3Native 一对多的 List.vue 使用 `useModal` + `openModal(true, {...})` 模式（与 vue3 封装风格**相同**）：

```typescript
import { useModal } from '/@/components/Modal';
import EduCourseModal from './components/EduCourseModal.vue';

const [registerModal, { openModal }] = useModal();

function handleAdd() {
  openModal(true, { isUpdate: false, showFooter: true });
}
function handleEdit(record) {
  openModal(true, { record, isUpdate: true, showFooter: true });
}
function handleDetail(record) {
  openModal(true, { record, isUpdate: true, showFooter: false });
}
```

模板中使用 `@register="registerModal"`：
```html
<EduCourseModal @register="registerModal" @success="handleSuccess"></EduCourseModal>
```

**注意：** 不要用 `ref` + `registerModal.value.add()` 模式（那是 `j-modal` 风格），vue3Native 一对多使用 `BasicModal` + `useModal/useModalInner` 配对。

#### C13-7. vue3Native 一对多技术要点和常见陷阱

| 要点 | 说明 |
|------|------|
| **Form.vue 必须用 `defineComponent`** | 不能用 `<script setup>`，因为 Modal 需要通过 `ref` 调用 `add()/edit()/submitForm()` 方法，`defineComponent` + `return` 自动暴露 |
| **Modal 是薄包装器** | Modal 不包含子表，不做数据收集，只负责打开/关闭和调用 Form 方法 |
| **`useValidateAntFormAndTable` 替代 `useJvxeMethod`** | 原生风格使用 `useValidateAntFormAndTable(activeKey, refsMap)` 而非 `useJvxeMethod`，它根据 `isForm` 属性自动区分表单和 JVxeTable |
| **一对一子表必须暴露 `isForm = true`** | `useValidateAntFormAndTable` 通过检查 ref 组件上的 `isForm` 属性来判断是调用 `getFormData()` 还是 `getTableData()` |
| **一对一子表 `getFormData()` 返回对象** | 返回 `toRaw(formData)`（对象），不是数组 `[formData]`；`useValidateAntFormAndTable` 内部会自动包装 |
| **一对一子表 `initFormData(mainId)` 只传 ID** | 直接调用 API 函数获取数据，不传 URL 字符串 |
| **子表 API 必须是函数导出** | `export const queryXxxByMainId = (id) => defHttp.get({...})`，不是 URL 字符串 |
| **`saveOrUpdate` 不用 `isTransformResponse: false`** | 原生风格的 `saveOrUpdate` 直接 `defHttp.post({url, params})`，不需要手动解析响应 |
| **需要 `queryDataById` 函数** | Form 编辑时通过 `queryDataById(id)` 重新查询主表完整数据，不直接用 record（避免字段缺失） |
| **activeKey 与 a-tabs 对应** | `useValidateAntFormAndTable` 的第一个参数 `activeKey` 与 `<a-tabs v-model:activeKey>` 一致 |
| **refsMap 的 key 与 tab key 一致** | `useValidateAntFormAndTable` 的第二个参数中，key 必须与 `<a-tab-pane key="xxx">` 的 key 一致 |
| **数据提交在 Form 中完成** | `submitForm()` 在 Form.vue 中，调用 `getFormData()` + `getSubFormAndTableData()` 后 `saveOrUpdate` |
| **`getSubFormAndTableData()` 自动组装子表字段名** | 返回对象的 key 格式为 `{tabKey}List`（如 `eduCourseChapterList`），与后端 Page VO 属性名一致 |

---
