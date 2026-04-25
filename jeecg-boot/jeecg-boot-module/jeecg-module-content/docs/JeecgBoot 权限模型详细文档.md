          
# JeecgBoot 权限模型详细文档

## 1. 权限模型概述

JeecgBoot 采用基于 **RBAC（Role-Based Access Control）** 的权限控制模型，结合 **Apache Shiro** 安全框架实现统一的身份认证和权限管理。系统支持多层级权限控制，包括菜单权限、按钮权限、数据权限和部门权限。

## 2. 核心技术栈

### 2.1 权限控制框架
- **Apache Shiro 2.0.4**: 核心安全框架，负责身份认证和权限授权
- **JWT (JSON Web Token) 4.5.0**: 无状态身份验证
- **Redis**: 权限信息缓存和会话存储
- **MyBatis-Plus**: 权限数据持久化

### 2.2 权限注解支持
- `@RequiresPermissions`: 方法级权限控制
- `@RequiresRoles`: 角色级权限控制
- `@PermissionData`: 数据权限控制

## 3. 权限模型架构

### 3.1 核心实体关系

```
用户(SysUser) ←→ 用户角色(SysUserRole) ←→ 角色(SysRole)
                                              ↓
                                    角色权限(SysRolePermission)
                                              ↓
                                        权限(SysPermission)
                                              ↓
                                    数据权限规则(SysPermissionDataRule)
```

### 3.2 权限层次结构

1. **菜单权限** (menuType=0,1)
   - 一级菜单 (menuType=0)
   - 子菜单 (menuType=1)
   - 支持树形结构，通过 parentId 建立层级关系

2. **按钮权限** (menuType=2)
   - 页面内的操作按钮权限
   - 如：新增、编辑、删除、查看等

3. **数据权限**
   - 基于数据规则的行级权限控制
   - 支持动态SQL条件注入

## 4. 核心实体类详解

### 4.1 权限实体 (SysPermission)

```java:jeecg-system-biz/src/main/java/org/jeecg/modules/system/entity/SysPermission.java
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysPermission implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;                    // 权限ID
    private String parentId;              // 父权限ID
    private String name;                  // 权限名称
    private String perms;                 // 权限编码，如"sys:user:add"
    private String permsType;             // 权限策略：1显示2禁用
    private String icon;                  // 菜单图标
    private String component;             // 前端组件路径
    private String url;                   // 访问路径
    private Integer menuType;             // 类型：0一级菜单，1子菜单，2按钮权限
    private boolean leaf;                 // 是否叶子节点
    private boolean route;                // 是否路由菜单
    private boolean keepAlive;            // 是否缓存页面
    private boolean hidden;               // 是否隐藏
    private Integer status;               // 状态：1启用，0禁用
    // ... 其他字段
}
```

### 4.2 角色实体 (SysRole)

```java:jeecg-system-biz/src/main/java/org/jeecg/modules/system/entity/SysRole.java
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysRole implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;                    // 角色ID
    private String roleName;              // 角色名称
    private String roleCode;              // 角色编码
    private String description;           // 角色描述
    private Integer tenantId;             // 租户ID（多租户支持）
    // ... 审计字段
}
```

### 4.3 数据权限规则 (SysPermissionDataRule)

```java:jeecg-system-biz/src/main/java/org/jeecg/modules/system/entity/SysPermissionDataRule.java
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysPermissionDataRule implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;                    // 规则ID
    private String permissionId;          // 对应的菜单权限ID
    private String ruleName;              // 规则名称
    private String ruleColumn;            // 字段名
    private String ruleConditions;        // 条件：如 = != > < in like
    private String ruleValue;             // 规则值
    private String status;                // 状态：1有效，0无效
    // ... 审计字段
}
```

## 5. 权限控制实现机制

### 5.1 Shiro 配置 (ShiroConfig)

**核心配置要点：**

1. **过滤器链配置**
   ```java
   // 配置不拦截的URL
   filterChainDefinitionMap.put("/sys/login", "anon");     // 登录接口
   filterChainDefinitionMap.put("/sys/logout", "anon");    // 登出接口
   filterChainDefinitionMap.put("/sys/randomImage/**", "anon"); // 验证码
   // 其他接口需要JWT认证
   filterChainDefinitionMap.put("/**", "jwt");
   ```

2. **Redis 缓存管理**
   - 权限信息缓存到Redis，提高查询性能
   - 支持集群模式和单机模式

3. **JWT Token 管理**
   - 无状态身份验证
   - Token 自动刷新机制

### 5.2 权限认证实现 (ShiroRealm)

**核心认证流程：**

```java:jeecg-boot-base-core/src/main/java/org/jeecg/config/shiro/ShiroRealm.java
@Override
protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    String userId = ((LoginUser) principals.getPrimaryPrincipal()).getId();
    SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
    
    // 1. 查询用户角色集合
    Set<String> roleSet = commonApi.queryUserRolesById(userId);
    info.setRoles(roleSet);
    
    // 2. 查询用户权限集合
    Set<String> permissionSet = commonApi.queryUserAuths(userId);
    info.addStringPermissions(permissionSet);
    
    return info;
}
```

### 5.3 数据权限控制 (PermissionDataAspect)

**数据权限AOP实现：**

```java:jeecg-boot-base-core/src/main/java/org/jeecg/common/aspect/PermissionDataAspect.java
@Around("@annotation(permissionData)")
public Object around(ProceedingJoinPoint point, PermissionData permissionData) throws Throwable {
    // 1. 获取当前用户
    String username = JwtUtil.getUserNameByToken(request);
    
    // 2. 查询数据权限规则
    List<SysPermissionDataRuleModel> dataRules = commonApi.queryPermissionDataRule(
        component, requestPath, username);
    
    // 3. 注入数据权限条件
    if(dataRules != null && dataRules.size() > 0) {
        JeecgDataAutorUtils.installDataSearchConditon(request, dataRules);
    }
    
    return point.proceed();
}
```

## 6. 权限使用方式

### 6.1 Controller 层权限控制

```java
@RestController
@RequestMapping("/sys/user")
public class SysUserController {
    
    /**
     * 用户列表查询 - 需要用户查询权限
     */
    @RequiresPermissions("user:list")
    @GetMapping("/list")
    public Result<IPage<SysUser>> queryPageList(SysUser sysUser, 
                                               @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                               @RequestParam(name="pageSize", defaultValue="10") Integer pageSize) {
        // 业务逻辑
    }
    
    /**
     * 用户新增 - 需要用户新增权限
     */
    @RequiresPermissions("user:add")
    @PostMapping("/add")
    public Result<SysUser> add(@RequestBody SysUser sysUser) {
        // 业务逻辑
    }
    
    /**
     * 用户编辑 - 需要用户编辑权限
     */
    @RequiresPermissions("user:edit")
    @PutMapping("/edit")
    public Result<SysUser> edit(@RequestBody SysUser sysUser) {
        // 业务逻辑
    }
    
    /**
     * 用户删除 - 需要用户删除权限
     */
    @RequiresPermissions("user:delete")
    @DeleteMapping("/delete")
    public Result<?> delete(@RequestParam(name="id") String id) {
        // 业务逻辑
    }
}
```

### 6.2 数据权限使用

```java
@RestController
public class SysUserController {
    
    /**
     * 带数据权限的查询
     */
    @PermissionData(pageComponent = "system/UserList")
    @GetMapping("/list")
    public Result<IPage<SysUser>> queryPageList(SysUser sysUser, 
                                               @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                               @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                               HttpServletRequest req) {
        // 数据权限会自动注入到查询条件中
        QueryWrapper<SysUser> queryWrapper = QueryGenerator.initQueryWrapper(sysUser, req.getParameterMap());
        Page<SysUser> page = new Page<SysUser>(pageNo, pageSize);
        IPage<SysUser> pageList = sysUserService.page(page, queryWrapper);
        return Result.OK(pageList);
    }
}
```

### 6.3 前端权限控制

**Vue3 权限指令使用：**

```typescript
// 权限检查Hook
function hasPermission(value?: RoleEnum | RoleEnum[] | string | string[], def = true): boolean {
  if (!value) {
    return def;
  }
  
  const permMode = projectSetting.permissionMode;
  
  if (PermissionModeEnum.BACK === permMode) {
    const allCodeList = permissionStore.getPermCodeList as string[];
    if (!isArray(value) && allCodeList && allCodeList.length > 0) {
      return allCodeList.includes(value);
    }
    return (intersection(value, allCodeList) as string[]).length > 0;
  }
  return true;
}

// 模板中使用
<template>
  <div>
    <!-- 按钮权限控制 -->
    <a-button v-if="hasPermission('user:add')" @click="handleAdd">
      新增用户
    </a-button>
    
    <a-button v-if="hasPermission('user:edit')" @click="handleEdit">
      编辑用户
    </a-button>
    
    <a-button v-if="hasPermission('user:delete')" @click="handleDelete">
      删除用户
    </a-button>
  </div>
</template>
```

## 7. 部门权限扩展

### 7.1 部门权限模型

JeecgBoot 还支持部门级权限控制，核心实体包括：

- **SysDepartPermission**: 部门权限关联表
- **SysDepartRole**: 部门角色表
- **SysDepartRolePermission**: 部门角色权限关联表
- **SysDepartRoleUser**: 部门角色用户关联表

### 7.2 部门权限查询

```java
/**
 * 查询部门权限数据
 */
@GetMapping("/queryDepartPermissionList")
public Result<List<SysPermission>> queryDepartPermissionList(@RequestParam String departId) {
    List<SysPermission> list = sysPermissionService.queryDepartPermissionList(departId);
    return Result.OK(list);
}
```

## 8. 权限缓存策略

### 8.1 Redis 缓存设计

1. **用户权限缓存**
   - Key: `sys:cache:user:permission:{userId}`
   - 存储用户的所有权限编码集合
   - 过期时间：30分钟

2. **用户角色缓存**
   - Key: `sys:cache:user:role:{userId}`
   - 存储用户的所有角色编码集合
   - 过期时间：30分钟

3. **数据权限规则缓存**
   - Key: `sys:cache:permission:datarule:{permissionId}`
   - 存储权限对应的数据规则
   - 过期时间：1小时

### 8.2 缓存更新策略

- **用户权限变更时**：清除对应用户的权限和角色缓存
- **角色权限变更时**：清除该角色下所有用户的权限缓存
- **数据规则变更时**：清除对应权限的数据规则缓存

## 9. 权限配置管理

### 9.1 权限树结构管理

```java
/**
 * 查询菜单权限树
 */
@GetMapping("/queryTreeList")
public Result<Map<String,Object>> queryTreeList() {
    // 1. 查询所有有效权限
    LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
    query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
    query.orderByAsc(SysPermission::getSortNo);
    List<SysPermission> list = sysPermissionService.list(query);
    
    // 2. 构建树形结构
    List<TreeModel> treeList = new ArrayList<>();
    getTreeModelList(treeList, list, null);
    
    // 3. 返回树形数据和所有ID
    Map<String,Object> resMap = new HashMap<>();
    resMap.put("treeList", treeList);
    resMap.put("ids", list.stream().map(SysPermission::getId).collect(Collectors.toList()));
    
    return Result.OK(resMap);
}
```

### 9.2 角色权限授权

```java
/**
 * 保存角色权限
 */
@PostMapping("/saveRolePermission")
public Result<String> saveRolePermission(@RequestBody JSONObject json) {
    String roleId = json.getString("roleId");
    String permissionIds = json.getString("permissionIds");
    String lastPermissionIds = json.getString("lastpermissionIds");
    
    // 调用服务层保存角色权限关系
    this.sysRolePermissionService.saveRolePermission(roleId, permissionIds, lastPermissionIds);
    return Result.OK("保存成功！");
}
```

## 10. 安全特性

### 10.1 JWT Token 安全

1. **Token 结构**
   - Header: 算法信息
   - Payload: 用户信息（用户名、过期时间等）
   - Signature: 签名验证

2. **Token 刷新机制**
   - 自动刷新：Token 即将过期时自动刷新
   - 手动刷新：提供刷新接口

3. **Token 安全策略**
   - 设置合理的过期时间
   - 支持Token黑名单机制
   - 防止Token重放攻击

### 10.2 权限验证安全

1. **防止权限绕过**
   - 所有接口默认需要认证
   - 白名单机制管理无需认证的接口

2. **数据权限安全**
   - SQL注入防护
   - 数据权限规则验证

## 11. 性能优化

### 11.1 权限查询优化

1. **数据库索引优化**
   ```sql
   -- 用户角色关联表索引
   CREATE INDEX idx_user_role_userid ON sys_user_role(user_id);
   CREATE INDEX idx_user_role_roleid ON sys_user_role(role_id);
   
   -- 角色权限关联表索引
   CREATE INDEX idx_role_permission_roleid ON sys_role_permission(role_id);
   CREATE INDEX idx_role_permission_permissionid ON sys_role_permission(permission_id);
   ```

2. **批量权限查询**
   - 一次性查询用户的所有角色和权限
   - 避免N+1查询问题

### 11.2 缓存优化

1. **多级缓存**
   - 本地缓存 + Redis缓存
   - 减少网络IO开销

2. **缓存预热**
   - 系统启动时预加载常用权限数据
   - 定时刷新缓存

## 12. 最佳实践

### 12.1 权限设计原则

1. **最小权限原则**
   - 用户只获得完成工作所需的最小权限
   - 定期审查和清理无用权限

2. **权限分离原则**
   - 敏感操作需要多重权限验证
   - 避免单一权限控制关键功能

3. **权限继承原则**
   - 合理设计角色层次结构
   - 避免权限冗余和冲突

### 12.2 开发规范

1. **权限编码规范**
   ```
   模块:功能:操作
   例如：
   - sys:user:list    (系统用户列表查询)
   - sys:user:add     (系统用户新增)
   - sys:role:edit    (系统角色编辑)
   - biz:order:delete (业务订单删除)
   ```

2. **注解使用规范**
   - Controller层必须添加权限注解
   - 敏感操作添加角色验证
   - 数据权限注解正确配置组件路径

3. **异常处理规范**
   - 权限不足时返回统一错误码
   - 记录权限验证失败日志
   - 避免敏感信息泄露

## 13. 总结

JeecgBoot 的权限模型具有以下特点：

1. **完整性**：支持菜单权限、按钮权限、数据权限的全方位控制
2. **灵活性**：基于RBAC模型，支持复杂的权限分配场景
3. **扩展性**：支持部门权限、多租户权限等扩展功能
4. **性能**：Redis缓存 + 数据库索引优化，保证高并发性能
5. **安全性**：JWT + Shiro双重保障，防止权限绕过和攻击
6. **易用性**：注解式权限控制，开发简单便捷

该权限模型能够满足大部分企业级应用的权限管理需求，为系统安全提供了坚实的保障。
        