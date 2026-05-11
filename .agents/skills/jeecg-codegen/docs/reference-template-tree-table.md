## 适用场景

- 用户提到 `分类`、`层级`、`树`、`上下级`。
- 当前模块存在 `pid`、`has_child` 等树结构字段。
- 需要树表专用后端接口、懒加载和前端展开逻辑。

## 必读前置

- 先读取 `docs/reference-template-single-table.md`，把单表基础模板作为底座。
- 先确认主显示字段、父子字段和是否懒加载。
- 先确认当前不是一对多场景，避免误套树表模板。

## 常见误用

- 只补 `pid` 字段，却漏掉 `has_child`、树接口和前端刷新逻辑。
- 把树表弹窗按普通单表弹窗处理，遗漏 `JTreeSelect` 规则。
- Mapper、XML、前端刷新策略没有整体对齐，导致树展开异常。

## B. 树表模式差异

树表在单表基础上有以下差异，相比单表多了树结构管理（父子节点关系、懒加载、展开状态维护）。

### B1. Entity 额外字段

在 Entity 类中追加以下树结构字段（放在系统字段之前）：

```java
    // ==================== 树结构字段 ====================

    /**父ID*/
    @Schema(description = "父ID")
    private String pid;

    /**是否有子节点*/
    @Excel(name = "是否有子节点", width = 15, dicCode = "yn")
    @Dict(dicCode = "yn")
    @Schema(description = "是否有子节点")
    private String hasChild;
```

### B2. Mapper 额外方法

Mapper 接口中增加树节点状态更新和子节点查询方法：

```java
public interface {{entityName}}Mapper extends BaseMapper<{{entityName}}> {

    /**
     * 编辑节点状态
     */
    void updateTreeNodeStatus(@Param("pid") String pid, @Param("hasChild") String hasChild);

    /**
     * 根据pid查询子节点（返回SelectTreeModel用于树选择组件）
     */
    List<SelectTreeModel> queryListByPid(@Param("parentId") String parentId, @Param("installCondition") String installCondition);
}
```

对应 Mapper XML（注意 `{{displayField}}` 为树节点显示字段，如 name）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.jeecg.modules.{{entityPackage}}.mapper.{{entityName}}Mapper">

    <update id="updateTreeNodeStatus">
        update {{tableName}} set has_child = #{hasChild} where id = #{pid}
    </update>

    <select id="queryListByPid" parameterType="string" resultType="org.jeecg.common.system.vo.SelectTreeModel">
        select id as "key", {{displayField}} as "title", pid as "parentId",
               (case when has_child = '1' then 0 else 1 end) as "isLeaf"
        from {{tableName}} where pid = #{parentId}
    </select>

</mapper>
```

### B3. Service 接口额外方法

```java
import org.jeecg.common.system.vo.SelectTreeModel;

public interface I{{entityName}}Service extends IService<{{entityName}}> {

    /**根节点父ID的值*/
    public static final String ROOT_PID_VALUE = "0";
    /**树节点有子节点状态值*/
    public static final String HASCHILD = "1";
    /**树节点无子节点状态值*/
    public static final String NOCHILD = "0";

    /**新增节点*/
    void add{{entityName}}({{entityName}} entity);
    /**修改节点*/
    void update{{entityName}}({{entityName}} entity);
    /**删除节点（含递归删除子节点）*/
    void delete{{entityName}}(String id) throws JeecgBootException;
    /**查询所有数据，无分页（搜索时递归找到根节点）*/
    List<{{entityName}}> queryTreeListNoPage(QueryWrapper<{{entityName}}> queryWrapper);
    /**根据父级编码加载分类字典的数据*/
    List<SelectTreeModel> queryListByCode(String parentCode);
    /**根据pid查询子节点集合*/
    List<SelectTreeModel> queryListByPid(String pid);
}
```

### B4. ServiceImpl 核心逻辑

```java
@Service
public class {{entityName}}ServiceImpl extends ServiceImpl<{{entityName}}Mapper, {{entityName}}> implements I{{entityName}}Service {

    @Override
    public void add{{entityName}}({{entityName}} entity) {
        // 新增时设置hasChild为0
        entity.setHasChild(I{{entityName}}Service.NOCHILD);
        if (oConvertUtils.isEmpty(entity.getPid())) {
            entity.setPid(I{{entityName}}Service.ROOT_PID_VALUE);
        } else {
            // 如果当前节点父ID不为空 则设置父节点的hasChild为1
            {{entityName}} parent = baseMapper.selectById(entity.getPid());
            if (parent != null && !"1".equals(parent.getHasChild())) {
                parent.setHasChild("1");
                baseMapper.updateById(parent);
            }
        }
        baseMapper.insert(entity);
    }

    @Override
    public void update{{entityName}}({{entityName}} entity) {
        {{entityName}} old = this.getById(entity.getId());
        if (old == null) {
            throw new JeecgBootException("未找到对应实体");
        }
        String old_pid = old.getPid();
        String new_pid = entity.getPid();
        if (!old_pid.equals(new_pid)) {
            // 父节点变更：更新旧父节点状态
            updateOldParentNode(old_pid);
            if (oConvertUtils.isEmpty(new_pid)) {
                entity.setPid(I{{entityName}}Service.ROOT_PID_VALUE);
            }
            // 更新新父节点为有子节点
            if (!I{{entityName}}Service.ROOT_PID_VALUE.equals(entity.getPid())) {
                baseMapper.updateTreeNodeStatus(entity.getPid(), I{{entityName}}Service.HASCHILD);
            }
        }
        baseMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete{{entityName}}(String id) throws JeecgBootException {
        // 查询选中节点下所有子节点一并删除
        id = this.queryTreeChildIds(id);
        if (id.indexOf(",") > 0) {
            StringBuffer sb = new StringBuffer();
            String[] idArr = id.split(",");
            for (String idVal : idArr) {
                if (idVal != null) {
                    {{entityName}} item = this.getById(idVal);
                    String pidVal = item.getPid();
                    // 查询此节点上一级是否还有其他子节点
                    List<{{entityName}}> dataList = baseMapper.selectList(new QueryWrapper<{{entityName}}>().eq("pid", pidVal).notIn("id", Arrays.asList(idArr)));
                    boolean flag = (dataList == null || dataList.size() == 0) && !Arrays.asList(idArr).contains(pidVal) && !sb.toString().contains(pidVal);
                    if (flag) {
                        sb.append(pidVal).append(",");
                    }
                }
            }
            // 批量删除节点
            baseMapper.deleteBatchIds(Arrays.asList(idArr));
            // 修改已无子节点的标识
            String[] pidArr = sb.toString().split(",");
            for (String pid : pidArr) {
                this.updateOldParentNode(pid);
            }
        } else {
            {{entityName}} item = this.getById(id);
            if (item == null) {
                throw new JeecgBootException("未找到对应实体");
            }
            updateOldParentNode(item.getPid());
            baseMapper.deleteById(id);
        }
    }

    @Override
    public List<{{entityName}}> queryTreeListNoPage(QueryWrapper<{{entityName}}> queryWrapper) {
        List<{{entityName}}> dataList = baseMapper.selectList(queryWrapper);
        List<{{entityName}}> mapList = new ArrayList<>();
        for ({{entityName}} data : dataList) {
            String pidVal = data.getPid();
            // 递归查询子节点的根节点
            if (pidVal != null && !I{{entityName}}Service.ROOT_PID_VALUE.equals(pidVal)) {
                {{entityName}} rootVal = this.getTreeRoot(pidVal);
                if (rootVal != null && !mapList.contains(rootVal)) {
                    mapList.add(rootVal);
                }
            } else {
                if (!mapList.contains(data)) {
                    mapList.add(data);
                }
            }
        }
        return mapList;
    }

    @Override
    public List<SelectTreeModel> queryListByCode(String parentCode) {
        String pid = ROOT_PID_VALUE;
        if (oConvertUtils.isNotEmpty(parentCode)) {
            LambdaQueryWrapper<{{entityName}}> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq({{entityName}}::getPid, parentCode);
            List<{{entityName}}> list = baseMapper.selectList(queryWrapper);
            if (list == null || list.size() == 0) {
                throw new JeecgBootException("该编码【" + parentCode + "】不存在，请核实!");
            }
            if (list.size() > 1) {
                throw new JeecgBootException("该编码【" + parentCode + "】存在多个，请核实!");
            }
            pid = list.get(0).getId();
        }
        return baseMapper.queryListByPid(pid, null);
    }

    @Override
    public List<SelectTreeModel> queryListByPid(String pid) {
        if (oConvertUtils.isEmpty(pid)) {
            pid = ROOT_PID_VALUE;
        }
        return baseMapper.queryListByPid(pid, null);
    }

    /**
     * 根据所传pid查询旧的父级节点的子节点并修改相应状态值
     */
    private void updateOldParentNode(String pid) {
        if (!I{{entityName}}Service.ROOT_PID_VALUE.equals(pid)) {
            Long count = baseMapper.selectCount(new QueryWrapper<{{entityName}}>().eq("pid", pid));
            if (count == null || count <= 1) {
                baseMapper.updateTreeNodeStatus(pid, I{{entityName}}Service.NOCHILD);
            }
        }
    }

    /**
     * 递归查询节点的根节点
     */
    private {{entityName}} getTreeRoot(String pidVal) {
        {{entityName}} data = baseMapper.selectById(pidVal);
        if (data != null && !I{{entityName}}Service.ROOT_PID_VALUE.equals(data.getPid()) && !data.getPid().equals(data.getId())) {
            return this.getTreeRoot(data.getPid());
        } else {
            return data;
        }
    }

    /**
     * 根据id查询所有子节点id
     */
    private String queryTreeChildIds(String ids) {
        String[] idArr = ids.split(",");
        StringBuffer sb = new StringBuffer();
        for (String pidVal : idArr) {
            if (pidVal != null) {
                if (!sb.toString().contains(pidVal)) {
                    if (sb.toString().length() > 0) {
                        sb.append(",");
                    }
                    sb.append(pidVal);
                    this.getTreeChildIds(pidVal, sb);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 递归查询所有子节点
     */
    private StringBuffer getTreeChildIds(String pidVal, StringBuffer sb) {
        List<{{entityName}}> dataList = baseMapper.selectList(new QueryWrapper<{{entityName}}>().eq("pid", pidVal));
        if (dataList != null && dataList.size() > 0) {
            for ({{entityName}} tree : dataList) {
                if (!sb.toString().contains(tree.getId())) {
                    sb.append(",").append(tree.getId());
                }
                this.getTreeChildIds(tree.getId(), sb);
            }
        }
        return sb;
    }
}
```

### B5. Controller 额外端点

树表 Controller 不使用单表的 `list` 端点，替换为以下端点：

```java
    /**
     * 分页列表查询（树根节点）
     * - 有搜索条件时(hasQuery=true)：调用 queryTreeListNoPage 搜索并递归找到根节点
     * - 无搜索条件时：只查 pid=0 的根节点
     */
    @Operation(summary = "{{description}}-分页列表查询")
    @GetMapping(value = "/rootList")
    public Result<IPage<{{entityName}}>> queryPageList({{entityName}} entity,
                                                        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                        HttpServletRequest req) {
        String hasQuery = req.getParameter("hasQuery");
        if (hasQuery != null && "true".equals(hasQuery)) {
            QueryWrapper<{{entityName}}> queryWrapper = QueryGenerator.initQueryWrapper(entity, req.getParameterMap());
            List<{{entityName}}> list = {{entityName_uncap}}Service.queryTreeListNoPage(queryWrapper);
            IPage<{{entityName}}> pageList = new Page<>(1, 10, list.size());
            pageList.setRecords(list);
            return Result.OK(pageList);
        } else {
            String parentId = entity.getPid();
            if (oConvertUtils.isEmpty(parentId)) {
                parentId = "0";
            }
            entity.setPid(null);
            QueryWrapper<{{entityName}}> queryWrapper = QueryGenerator.initQueryWrapper(entity, req.getParameterMap());
            // 使用 eq 防止模糊查询
            queryWrapper.eq("pid", parentId);
            Page<{{entityName}}> page = new Page<>(pageNo, pageSize);
            IPage<{{entityName}}> pageList = {{entityName_uncap}}Service.page(page, queryWrapper);
            return Result.OK(pageList);
        }
    }

    /**
     * 获取子数据（展开树节点时调用）
     */
    @Operation(summary = "{{description}}-获取子数据")
    @GetMapping(value = "/childList")
    public Result<IPage<{{entityName}}>> queryChildList({{entityName}} entity, HttpServletRequest req) {
        QueryWrapper<{{entityName}}> queryWrapper = QueryGenerator.initQueryWrapper(entity, req.getParameterMap());
        List<{{entityName}}> list = {{entityName_uncap}}Service.list(queryWrapper);
        IPage<{{entityName}}> pageList = new Page<>(1, 10, list.size());
        pageList.setRecords(list);
        return Result.OK(pageList);
    }

    /**
     * 批量查询子节点（已展开节点刷新时批量加载子数据）
     */
    @Operation(summary = "{{description}}-批量获取子数据")
    @GetMapping("/getChildListBatch")
    public Result getChildListBatch(@RequestParam("parentIds") String parentIds) {
        try {
            QueryWrapper<{{entityName}}> queryWrapper = new QueryWrapper<>();
            List<String> parentIdList = Arrays.asList(parentIds.split(","));
            queryWrapper.in("pid", parentIdList);
            List<{{entityName}}> list = {{entityName_uncap}}Service.list(queryWrapper);
            IPage<{{entityName}}> pageList = new Page<>(1, 10, list.size());
            pageList.setRecords(list);
            return Result.OK(pageList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("批量查询子节点失败：" + e.getMessage());
        }
    }

    /**
     * 【vue3专用】加载节点的子数据（用于JTreeSelect父节点选择组件）
     */
    @RequestMapping(value = "/loadTreeChildren", method = RequestMethod.GET)
    public Result<List<SelectTreeModel>> loadTreeChildren(@RequestParam(name = "pid") String pid) {
        Result<List<SelectTreeModel>> result = new Result<>();
        try {
            List<SelectTreeModel> ls = {{entityName_uncap}}Service.queryListByPid(pid);
            result.setResult(ls);
            result.setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
            result.setMessage(e.getMessage());
            result.setSuccess(false);
        }
        return result;
    }

    /**
     * 【vue3专用】加载一级节点/如果是同步则所有数据（用于JTreeSelect父节点选择组件）
     */
    @RequestMapping(value = "/loadTreeRoot", method = RequestMethod.GET)
    public Result<List<SelectTreeModel>> loadTreeRoot(@RequestParam(name = "async") Boolean async, @RequestParam(name = "pcode") String pcode) {
        Result<List<SelectTreeModel>> result = new Result<>();
        try {
            List<SelectTreeModel> ls = {{entityName_uncap}}Service.queryListByCode(pcode);
            if (!async) {
                loadAllChildren(ls);
            }
            result.setResult(ls);
            result.setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
            result.setMessage(e.getMessage());
            result.setSuccess(false);
        }
        return result;
    }

    /**
     * 递归求子节点 同步加载用到
     */
    private void loadAllChildren(List<SelectTreeModel> ls) {
        for (SelectTreeModel tsm : ls) {
            List<SelectTreeModel> temp = {{entityName_uncap}}Service.queryListByPid(tsm.getKey());
            if (temp != null && temp.size() > 0) {
                tsm.setChildren(temp);
                loadAllChildren(temp);
            }
        }
    }

    // 注意：树表批量删除应逐条调用 delete{{entityName}} 以正确处理树结构
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        for (String id : ids.split(",")) {
            {{entityName_uncap}}Service.delete{{entityName}}(id);
        }
        return Result.OK("批量删除成功!");
    }
```

**注意：** 如果实体中有多选/checkbox类字段，需在 `rootList` 方法中添加自定义查询规则：
```java
    Map<String, QueryRuleEnum> customeRuleMap = new HashMap<>();
    customeRuleMap.put("多选字段名", QueryRuleEnum.LIKE_WITH_OR);
    // 然后传给 QueryGenerator.initQueryWrapper(entity, req.getParameterMap(), customeRuleMap);
```

### B6. 树表前端 API ({{entityName}}.api.ts)

树表 API 完整模板（注意区别于单表的几个关键点）：

```typescript
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  rootList = '/{{entityPackagePath}}/{{entityName_uncap}}/rootList',
  childList = '/{{entityPackagePath}}/{{entityName_uncap}}/childList',
  getChildListBatch = '/{{entityPackagePath}}/{{entityName_uncap}}/getChildListBatch',
  save = '/{{entityPackagePath}}/{{entityName_uncap}}/add',
  edit = '/{{entityPackagePath}}/{{entityName_uncap}}/edit',
  deleteOne = '/{{entityPackagePath}}/{{entityName_uncap}}/delete',
  deleteBatch = '/{{entityPackagePath}}/{{entityName_uncap}}/deleteBatch',
  importExcel = '/{{entityPackagePath}}/{{entityName_uncap}}/importExcel',
  exportXls = '/{{entityPackagePath}}/{{entityName_uncap}}/exportXls',
  loadTreeData = '/{{entityPackagePath}}/{{entityName_uncap}}/loadTreeRoot',
}

export const getExportUrl = Api.exportXls;
export const getImportUrl = Api.importExcel;
export const list = (params) => defHttp.get({ url: Api.rootList, params });
export const getChildList = (params) => defHttp.get({ url: Api.childList, params });
// 关键：isTransformResponse: false 返回完整 Result 对象，用于 loadDataByExpandedRows
export const getChildListBatch = (params) => defHttp.get({ url: Api.getChildListBatch, params }, { isTransformResponse: false });
export const deleteOne = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteOne, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
export const batchDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};
export const loadTreeData = (params) => defHttp.get({ url: Api.loadTreeData, params });
```

### B7. 树表前端 data.ts 差异

`formSchema` 中 `pid` 字段必须使用 `JTreeSelect` 组件（而非隐藏的 Input），配置如下：

```typescript
// formSchema 中的 pid 字段
{
  label: '父级节点',
  field: 'pid',
  component: 'JTreeSelect',
  componentProps: {
    dict: '{{tableName}},{{displayField}},id',  // 表名,显示字段,值字段
    pidField: 'pid',
    pidValue: '0',
    hasChildField: 'has_child',
    placeholder: '请选择父级节点',
  },
},
```

columns 中 `title` 字段（树表主显示列）建议 `align: 'left'`，便于展示层级缩进。

### B8. 树表前端 List 页面 ({{entityName}}List.vue) — 完整模板

树表 List 页面与单表有**根本性差异**，需要完整的树节点展开/收缩/懒加载/刷新逻辑：

```vue
<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection" :expandedRowKeys="expandedRowKeys" @expand="handleExpand" @fetch-success="onFetchSuccess">
      <template #tableTitle>
        <a-button type="primary" v-auth="'{{entityPackage}}:{{tableName}}:add'" @click="handleCreate" preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button type="primary" v-auth="'{{entityPackage}}:{{tableName}}:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button type="primary" v-auth="'{{entityPackage}}:{{tableName}}:importExcel'" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined" />
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'{{entityPackage}}:{{tableName}}:deleteBatch'">批量操作
            <Icon icon="mdi:chevron-down" />
          </a-button>
        </a-dropdown>
        <!-- 高级查询 -->
        <super-query :config="superQueryConfig" @search="handleSuperQuery" />
      </template>
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
      <template v-slot:bodyCell="{ column, record, index, text }">
        {{!-- 根据业务需要添加自定义列渲染 --}}
      </template>
    </BasicTable>
    <{{entityName}}Modal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="{{entityPackage}}-{{entityName_uncap}}" setup>
  import { ref, reactive, unref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  import {{entityName}}Modal from './components/{{entityName}}Modal.vue';
  import { columns, searchFormSchema, superQuerySchema } from './{{entityName}}.data';
  import { list, getChildList, getChildListBatch, deleteOne, batchDelete, getExportUrl, getImportUrl } from './{{entityName}}.api';
  import { getDateByPicker } from '/@/utils';

  const queryParam = reactive<any>({});
  const expandedRowKeys = ref<Array<string | number>>([]);
  const [registerModal, { openModal }] = useModal();
  // 日期个性化选择（如有季度/年/月/周字段）
  const fieldPickers = reactive({
    // quarterVal: 'quarter', yearVal: 'year', monthVal: 'month', weekVal: 'week',
  });

  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: '{{description}}',
      api: list,
      columns,
      canResize: true,
      isTreeTable: true,
      formConfig: {
        schemas: searchFormSchema,
        autoSubmitOnEnter: true,
        showAdvancedButton: true,
        fieldMapToNumber: [],
        fieldMapToTime: [],
      },
      actionColumn: {
        width: 240,
        fixed: 'right',
      },
      beforeFetch: (params) => {
        if (params && fieldPickers) {
          for (let key in fieldPickers) {
            if (params[key]) {
              params[key] = getDateByPicker(params[key], fieldPickers[key]);
            }
          }
        }
        params['hasQuery'] = 'true';
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: '{{description}}',
      url: getExportUrl,
      params: queryParam,
    },
    importConfig: {
      url: getImportUrl,
      success: importSuccess,
    },
  });

  const [registerTable, { reload, updateTableDataRecord, findTableDataRecord, getDataSource }, { rowSelection, selectedRowKeys }] = tableContext;
  const superQueryConfig = reactive(superQuerySchema);

  /**
   * 接口请求成功后回调：为有子节点的数据添加loading占位
   */
  function onFetchSuccess(result) {
    getDataByResult(result.items) && loadDataByExpandedRows();
  }

  /**
   * 根据已展开的行查询数据（用于保存后刷新时异步加载子级的数据）
   */
  async function loadDataByExpandedRows() {
    if (unref(expandedRowKeys).length > 0) {
      const res = await getChildListBatch({ parentIds: unref(expandedRowKeys).join(',') });
      if (res.success && res.result.records.length > 0) {
        let records = res.result.records;
        const listMap = new Map();
        for (let item of records) {
          let pid = item['pid'];
          if (unref(expandedRowKeys).includes(pid)) {
            let mapList = listMap.get(pid);
            if (mapList == null) {
              mapList = [];
            }
            mapList.push(item);
            listMap.set(pid, mapList);
          }
        }
        let childrenMap = listMap;
        let fn = (list) => {
          if (list) {
            list.forEach((data) => {
              if (unref(expandedRowKeys).includes(data.id)) {
                data.children = getDataByResult(childrenMap.get(data.id));
                fn(data.children);
              }
            });
          }
        };
        fn(getDataSource());
      }
    }
  }

  /**
   * 处理数据集：为 hasChild='1' 的节点添加 loading 占位子节点
   */
  function getDataByResult(result) {
    if (result && result.length > 0) {
      return result.map((item) => {
        if (item['hasChild'] == '1') {
          let loadChild = { id: item.id + '_loadChild', name: 'loading...', isLoading: true };
          item.children = [loadChild];
        }
        return item;
      });
    }
  }

  /**
   * 树节点展开/合并：展开时懒加载子节点数据
   */
  async function handleExpand(expanded, record) {
    if (expanded) {
      expandedRowKeys.value.push(record.id);
      if (record.children.length > 0 && !!record.children[0].isLoading) {
        let result = await getChildList({ pid: record.id });
        result = result.records ? result.records : result;
        if (result && result.length > 0) {
          record.children = getDataByResult(result);
        } else {
          record.children = null;
          record.hasChild = '0';
        }
      }
    } else {
      let keyIndex = expandedRowKeys.value.indexOf(record.id);
      if (keyIndex >= 0) {
        expandedRowKeys.value.splice(keyIndex, 1);
      }
    }
  }

  /**
   * 操作表格后处理树节点展开
   */
  async function expandTreeNode(key) {
    let record = findTableDataRecord(key);
    expandedRowKeys.value.push(key);
    let result = await getChildList({ pid: key });
    if (result && result.length > 0) {
      record.children = getDataByResult(result);
    } else {
      record.children = null;
      record.hasChild = '0';
    }
    updateTableDataRecord(key, record);
  }

  function handleSuperQuery(params) {
    Object.keys(params).map((k) => {
      queryParam[k] = params[k];
    });
    reload();
  }

  function handleCreate() {
    openModal(true, { isUpdate: false });
  }

  function handleAddChild(record) {
    openModal(true, { record: { pid: record.id }, isUpdate: false });
  }

  function handleEdit(record) {
    openModal(true, { record, isUpdate: true });
  }

  function handleDetail(record) {
    openModal(true, { record, isUpdate: true, hideFooter: true });
  }

  async function handleDelete(record) {
    await deleteOne({ id: record.id }, importSuccess);
  }

  async function batchHandleDelete() {
    const ids = selectedRowKeys.value.filter((item) => !item.includes('loadChild'));
    await batchDelete({ ids: ids }, importSuccess);
  }

  function importSuccess() {
    (selectedRowKeys.value = []) && reload();
  }

  /**
   * 成功回调（编辑/新增后精确刷新，避免全量reload）
   */
  async function handleSuccess({ isUpdate, values, expandedArr, changeParent }) {
    if (isUpdate) {
      if (changeParent) {
        // 父节点变更，需要全量刷新
        reload();
      } else {
        // 父节点未变，只更新单条记录
        let data = await list({ id: values.id, pageSize: 1, pageNo: 1, pid: values['pid'] });
        if (data && data.records && data.records.length > 0) {
          updateTableDataRecord(values.id, data.records[0]);
        } else {
          updateTableDataRecord(values.id, values);
        }
      }
    } else {
      if (!values['id'] || !values['pid']) {
        // 新增根节点
        reload();
      } else {
        // 新增子节点：按展开路径逐级加载
        expandedRowKeys.value = [];
        for (let key of unref(expandedArr)) {
          await expandTreeNode(key);
        }
      }
    }
  }

  function getTableAction(record) {
    return [
      { label: '编辑', onClick: handleEdit.bind(null, record), auth: '{{entityPackage}}:{{tableName}}:edit' },
      { label: '添加子节点', onClick: handleAddChild.bind(null, record), auth: '{{entityPackage}}:{{tableName}}:add' },
    ];
  }

  function getDropDownAction(record) {
    return [
      { label: '详情', onClick: handleDetail.bind(null, record) },
      {
        label: '删除',
        popConfirm: { title: '确定删除吗?', confirm: handleDelete.bind(null, record), placement: 'topLeft' },
        auth: '{{entityPackage}}:{{tableName}}:delete',
      },
    ];
  }
</script>

<style lang="less" scoped>
  :deep(.ant-picker),:deep(.ant-input-number) {
    width: 100%;
  }
</style>
```

### B9. 树表前端 Modal ({{entityName}}Modal.vue) — 完整模板

树表 Modal 与单表的关键差异：
1. 打开时加载父级树数据（`loadTreeData`），用于 JTreeSelect 组件
2. 编辑时通过 `updateSchema` 设置 `hiddenNodeKey`，防止选择自己作为父节点
3. 提交成功后回传 `expandedArr`（展开路径）和 `changeParent`（是否变更父级），以便 List 页面精确刷新

```vue
<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :maxHeight="500" :width="800" :title="title" @ok="handleSubmit">
    <BasicForm @register="registerForm" name="{{entityName}}Form" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { formSchema } from '../{{entityName}}.data';
  import { saveOrUpdate, loadTreeData } from '../{{entityName}}.api';
  import { getDateByPicker } from '/@/utils';

  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  const isDetail = ref(false);
  const expandedRowKeys = ref([]);
  const treeData = ref([]);
  // 当前编辑的数据（用于判断父节点是否变更）
  let model: Nullable<Recordable> = null;
  // 日期个性化选择（如有季度/年/月/周字段）
  const fieldPickers = reactive({
    // quarterVal: 'quarter', yearVal: 'year', monthVal: 'month', weekVal: 'week',
  });

  const [registerForm, { setProps, resetFields, setFieldsValue, validate, updateSchema, scrollToField }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 24 },
    labelCol: { xs: { span: 24 }, sm: { span: 4 } },
    wrapperCol: { xs: { span: 24 }, sm: { span: 18 } },
    baseRowStyle: { padding: '0 20px' },
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    await resetFields();
    expandedRowKeys.value = [];
    setModalProps({ confirmLoading: false, minHeight: 80, showOkBtn: !data?.hideFooter });
    isUpdate.value = !!data?.isUpdate;
    isDetail.value = !data?.hideFooter;
    if (data?.record) {
      model = data.record;
      await setFieldsValue({ ...data.record });
      // 编辑时隐藏自身节点（防止选自己做父级）
      updateSchema([{ field: 'pid', componentProps: { hiddenNodeKey: data.record.id } }]);
    } else {
      model = null;
      updateSchema([{ field: 'pid', componentProps: { hiddenNodeKey: '' } }]);
    }
    // 加载父级节点树信息（仅用于 getExpandKeysByPid，不传给 JTreeSelect）
    // 注意：JTreeSelect 通过 dict 配置自行加载数据，updateSchema 中禁止传递 treeData！
    treeData.value = await loadTreeData({ async: false, pcode: '' });
    // 详情模式下禁用表单
    setProps({ disabled: !!data?.hideFooter });
  });

  const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(isDetail) ? '详情' : '编辑'));

  /**
   * 根据pid获取展开的节点路径（从子到根）
   */
  function getExpandKeysByPid(pid, arr) {
    if (pid && arr && arr.length > 0) {
      for (let i = 0; i < arr.length; i++) {
        if (arr[i].key == pid && unref(expandedRowKeys).indexOf(pid) < 0) {
          expandedRowKeys.value.push(arr[i].key);
          getExpandKeysByPid(arr[i]['parentId'], unref(treeData));
        } else {
          getExpandKeysByPid(pid, arr[i].children);
        }
      }
    }
  }

  const changeDateValue = (formData) => {
    if (formData && fieldPickers) {
      for (let key in fieldPickers) {
        if (formData[key]) {
          formData[key] = getDateByPicker(formData[key], fieldPickers[key]);
        }
      }
    }
  };

  async function handleSubmit() {
    try {
      let values = await validate();
      changeDateValue(values);
      setModalProps({ confirmLoading: true });
      await saveOrUpdate(values, isUpdate.value);
      closeModal();
      // 收集展开的节点路径信息
      await getExpandKeysByPid(values['pid'], unref(treeData));
      // 回传给 List 页面进行精确刷新
      emit('success', {
        isUpdate: unref(isUpdate),
        values: { ...values },
        expandedArr: unref(expandedRowKeys).reverse(),
        changeParent: model != null && model['pid'] != values['pid'],
      });
    } catch ({ errorFields }) {
      if (errorFields) {
        const firstField = errorFields[0];
        if (firstField) {
          scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
        }
      }
      return Promise.reject(errorFields);
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped>
  :deep(.ant-input-number) {
    width: 100%;
  }
  :deep(.ant-calendar-picker) {
    width: 100%;
  }
</style>
```

### B10. 树表技术要点总结

| 技术点 | 说明 |
|--------|------|
| **懒加载占位** | `onFetchSuccess` + `getDataByResult` 为 `hasChild='1'` 的节点添加 `isLoading` 占位子节点 |
| **占位节点显示字段** | loading 占位节点的显示字段名必须与 columns 第一列 dataIndex 一致（如 `categoryName: 'loading...'`，不要写 `name`） |
| **展开懒加载** | `handleExpand` 判断 `isLoading` 占位，首次展开时请求 `childList` 接口 |
| **handleExpand 结果处理** | `result = result.records ? result.records : result` 兼容返回 Page 和 List 两种格式 |
| **批量子节点恢复** | `loadDataByExpandedRows` 页面刷新后根据 `expandedRowKeys` 批量请求 `getChildListBatch` 恢复子数据 |
| **精确刷新** | `handleSuccess` 区分：编辑不变父→重新查询单条（含dictText），编辑变父→全刷新，新增根→全刷新，新增子→按路径展开 |
| **编辑后重新查询** | 编辑后必须 `await list({id, pageSize:1, pageNo:1})` 重新查询，不能直接用 form values（缺少 `_dictText` 翻译） |
| **新增子节点前清空** | 新增子节点展开前必须 `expandedRowKeys.value = []` 先清空，否则展开逻辑出错 |
| **展开路径回传** | Modal 的 `getExpandKeysByPid` 递归收集从当前节点到根的展开路径，提交后回传给 List |
| **父节点变更检测** | Modal 保存 `model` 原始数据，提交时对比 `model.pid != values.pid` 判断是否变更父级 |
| **隐藏自身节点** | 编辑时 `updateSchema` **只传** `hiddenNodeKey`，**禁止传 treeData**（会导致下拉框 label 变 value） |
| **treeData 用途** | `treeData` 变量仅用于 `getExpandKeysByPid` 计算展开路径，不传给 JTreeSelect（组件通过 dict 自行加载） |
| **API isTransformResponse** | `getChildListBatch` 必须设 `{ isTransformResponse: false }` 返回完整 Result 对象 |
| **批量删除过滤** | `batchHandleDelete` 需过滤掉 `_loadChild` 占位节点的 ID |
| **批量删除递归** | Controller 批量删除应逐条调用 `deleteXxx` 以正确处理树结构（递归子节点+父节点状态） |
| **搜索模式** | `rootList` 的 `hasQuery=true` 参数触发 `queryTreeListNoPage`，搜索结果递归找到根节点展示 |
| **pid 组件** | formSchema 中 `pid` 使用 `JTreeSelect` 组件（非隐藏 Input），配置 `dict/pidField/pidValue/hasChildField` |
| **id 隐藏字段位置** | formSchema 中 `id` 隐藏字段放在**最后**（参考 JeecgBoot 代码生成器标准输出） |
| **Mapper XML 参数名** | `updateTreeNodeStatus` 用 `#{id}` `#{status}`，`queryListByPid` 用 `#{pid}`，需包含 `query` 的 foreach 支持 |
| **详情模式** | Modal 用 `setProps({ disabled: !!data?.hideFooter })` 禁用表单，`showOkBtn: !!!data?.hideFooter` 隐藏确认按钮 |
| **权限和布局** | List 所有按钮需 `v-auth`，操作拆分为主操作（编辑+添加下级）和下拉操作（详情+删除），`actionColumn.fixed:'right'` |

---
