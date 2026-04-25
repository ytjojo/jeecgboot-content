## JeecgBoot 权限控制架构详解

### 1. 权限控制核心架构

JeecgBoot采用**基于RBAC（Role-Based Access Control）的权限控制模型**，结合**Apache Shiro**安全框架实现：

```
用户(SysUser) ←→ 用户角色(SysUserRole) ←→ 角色(SysRole)
                                              ↓
                                    角色权限(SysRolePermission)
                                              ↓
                                        权限(SysPermission)
                                              ↓
                                    数据权限规则(SysPermissionDataRule)
```

### 2. 权限层次结构

1. **菜单权限** (menuType=0,1) - 控制页面访问
2. **按钮权限** (menuType=2) - 控制操作按钮
3. **数据权限** - 控制数据访问范围
4. **角色权限** - 基于角色的权限分配

### 3. 针对你的需求场景的完整解决方案

#### 3.1 未登录用户有限访问实现

```java:content/controller/ContentController.java
@RestController
@RequestMapping("/content")
public class ContentController {
    
    /**
     * 公开内容列表 - 未登录用户可访问
     * 只返回已发布的公开内容
     */
    @AutoLog(value = "内容-公开列表查询")
    @Operation(summary = "公开内容列表", description = "获取公开可访问的内容列表")
    @GetMapping("/public/list")
    public Result<IPage<ContentListRespVO>> getPublicContentList(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "categoryId", required = false) String categoryId) {
        
        // 构建公开内容查询条件
        Page<ContentEntity> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<ContentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContentEntity::getStatus, ContentStatusEnum.PUBLISHED.getValue())
                   .eq(ContentEntity::getIsPublic, true)  // 只查询公开内容
                   .eq(ContentEntity::getDelFlag, CommonConstant.DEL_FLAG_0);
        
        if (StringUtils.hasText(categoryId)) {
            queryWrapper.eq(ContentEntity::getCategoryId, categoryId);
        }
        
        IPage<ContentEntity> pageList = contentService.page(page, queryWrapper);
        
        // 转换为响应VO，隐藏敏感信息
        List<ContentListRespVO> respList = pageList.getRecords().stream()
                .map(this::convertToPublicListVO)
                .collect(Collectors.toList());
        
        IPage<ContentListRespVO> result = new Page<>();
        BeanUtils.copyProperties(pageList, result);
        result.setRecords(respList);
        
        return Result.OK(result);
    }
    
    /**
     * 公开内容详情 - 未登录用户可访问
     */
    @AutoLog(value = "内容-公开详情查询")
    @Operation(summary = "公开内容详情", description = "获取公开内容详情")
    @GetMapping("/public/detail/{id}")
    public Result<ContentDetailRespVO> getPublicContentDetail(@PathVariable String id) {
        
        ContentEntity content = contentService.getById(id);
        if (content == null || !content.getIsPublic() || 
            !ContentStatusEnum.PUBLISHED.getValue().equals(content.getStatus())) {
            return Result.error("内容不存在或不可访问");
        }
        
        // 转换为公开详情VO，过滤敏感信息
        ContentDetailRespVO respVO = convertToPublicDetailVO(content);
        return Result.OK(respVO);
    }
    
    /**
     * 转换为公开列表VO，过滤敏感信息
     */
    private ContentListRespVO convertToPublicListVO(ContentEntity entity) {
        ContentListRespVO vo = new ContentListRespVO();
        BeanUtils.copyProperties(entity, vo);
        // 隐藏敏感信息
        vo.setCreateBy(null);  // 不显示创建者
        vo.setUpdateBy(null);  // 不显示更新者
        return vo;
    }
}
```

#### 3.2 登录用户权限控制实现

```java:content/controller/ContentController.java
/**
 * 登录用户内容列表 - 需要登录权限
 */
@RequiresPermissions("content:list")
@PermissionData(pageComponent = "content/ContentList")
@AutoLog(value = "内容-列表查询")
@Operation(summary = "内容列表查询", description = "分页查询内容列表")
@GetMapping("/list")
public Result<IPage<ContentListRespVO>> queryPageList(
        ContentQueryReqVO queryVO,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
        HttpServletRequest req) {
    
    // 获取当前登录用户
    LoginUser loginUser = LoginUserUtils.getLoginUser();
    
    Page<ContentEntity> page = new Page<>(pageNo, pageSize);
    LambdaQueryWrapper<ContentEntity> queryWrapper = buildQueryWrapper(queryVO, loginUser);
    
    // 数据权限会通过@PermissionData注解自动注入
    IPage<ContentEntity> pageList = contentService.page(page, queryWrapper);
    
    // 转换为响应VO
    List<ContentListRespVO> respList = pageList.getRecords().stream()
            .map(this::convertToListVO)
            .collect(Collectors.toList());
    
    IPage<ContentListRespVO> result = new Page<>();
    BeanUtils.copyProperties(pageList, result);
    result.setRecords(respList);
    
    return Result.OK(result);
}

/**
 * VIP内容详情 - 需要VIP权限
 */
@RequiresPermissions("content:vip:view")
@AutoLog(value = "内容-VIP详情查询")
@Operation(summary = "VIP内容详情", description = "查看VIP专属内容详情")
@GetMapping("/vip/detail/{id}")
public Result<ContentDetailRespVO> getVipContentDetail(@PathVariable String id) {
    
    LoginUser loginUser = LoginUserUtils.getLoginUser();
    
    // 检查用户是否有VIP权限
    if (!hasVipPermission(loginUser)) {
        return Result.error(403, "需要VIP权限才能查看此内容");
    }
    
    ContentEntity content = contentService.getById(id);
    if (content == null || !ContentTypeEnum.VIP.getValue().equals(content.getContentType())) {
        return Result.error("VIP内容不存在");
    }
    
    ContentDetailRespVO respVO = convertToDetailVO(content);
    return Result.OK(respVO);
}

/**
 * 检查用户是否有VIP权限
 */
private boolean hasVipPermission(LoginUser loginUser) {
    // 方式1：通过角色检查
    Set<String> userRoles = commonApi.queryUserRolesById(loginUser.getId());
    if (userRoles.contains("vip") || userRoles.contains("admin")) {
        return true;
    }
    
    // 方式2：通过权限检查
    Set<String> userPermissions = commonApi.queryUserAuths(loginUser.getId());
    return userPermissions.contains("content:vip:view");
}
```

#### 3.3 危险操作权限控制实现

```java:content/controller/ContentController.java
/**
 * 创建内容 - 需要创建权限
 */
@RequiresPermissions("content:add")
@AutoLog(value = "内容-添加")
@Operation(summary = "添加内容", description = "创建新内容")
@PostMapping("/add")
public Result<ContentRespVO> add(@Valid @RequestBody ContentCreateReqVO createVO) {
    
    LoginUser loginUser = LoginUserUtils.getLoginUser();
    
    // 检查内容类型创建权限
    if (!canCreateContentType(loginUser, createVO.getContentType())) {
        throw PermissionException.permissionDenied("创建此类型内容");
    }
    
    // 转换DTO并设置创建者信息
    ContentBaseDTO dto = createVO.toDTO();
    dto.setCreateBy(loginUser.getUsername());
    dto.setCreateTime(LocalDateTime.now());
    
    ContentEntity entity = contentService.createContent(dto);
    ContentRespVO respVO = convertToRespVO(entity);
    
    return Result.OK(respVO);
}

/**
 * 更新内容 - 需要编辑权限且只能编辑自己的内容
 */
@RequiresPermissions("content:edit")
@AutoLog(value = "内容-编辑")
@Operation(summary = "编辑内容", description = "更新内容信息")
@PutMapping("/edit")
public Result<ContentRespVO> edit(@Valid @RequestBody ContentUpdateReqVO updateVO) {
    
    LoginUser loginUser = LoginUserUtils.getLoginUser();
    
    // 检查内容是否存在
    ContentEntity existingContent = contentService.getById(updateVO.getId());
    if (existingContent == null) {
        return Result.error("内容不存在");
    }
    
    // 权限检查：只能编辑自己的内容，除非是管理员
    if (!canEditContent(loginUser, existingContent)) {
        throw PermissionException.cannotEditContent(updateVO.getId());
    }
    
    // 转换DTO并更新
    ContentBaseDTO dto = updateVO.toDTO();
    dto.setUpdateBy(loginUser.getUsername());
    dto.setUpdateTime(LocalDateTime.now());
    
    ContentEntity entity = contentService.updateContent(dto);
    ContentRespVO respVO = convertToRespVO(entity);
    
    return Result.OK(respVO);
}

/**
 * 删除内容 - 需要删除权限且只能删除自己的内容
 */
@RequiresPermissions("content:delete")
@AutoLog(value = "内容-删除")
@Operation(summary = "删除内容", description = "删除指定内容")
@DeleteMapping("/delete")
public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
    
    LoginUser loginUser = LoginUserUtils.getLoginUser();
    
    ContentEntity content = contentService.getById(id);
    if (content == null) {
        return Result.error("内容不存在");
    }
    
    // 权限检查：只能删除自己的内容，除非是管理员
    if (!canDeleteContent(loginUser, content)) {
        throw PermissionException.cannotDeleteContent(id);
    }
    
    contentService.removeById(id);
    return Result.OK("删除成功!");
}

/**
 * 批量删除 - 需要管理员权限
 */
@RequiresRoles("admin")
@RequiresPermissions("content:batch:delete")
@AutoLog(value = "内容-批量删除")
@Operation(summary = "批量删除内容", description = "批量删除指定内容")
@DeleteMapping("/deleteBatch")
public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
    
    LoginUser loginUser = LoginUserUtils.getLoginUser();
    log.info("管理员{}执行批量删除操作，内容IDs: {}", loginUser.getUsername(), ids);
    
    String[] idArray = ids.split(",");
    for (String id : idArray) {
        contentService.removeById(id);
    }
    
    return Result.OK("批量删除成功!");
}

/**
 * 检查是否可以创建指定类型的内容
 */
private boolean canCreateContentType(LoginUser loginUser, String contentType) {
    // VIP内容需要特殊权限
    if (ContentTypeEnum.VIP.getValue().equals(contentType)) {
        Set<String> userPermissions = commonApi.queryUserAuths(loginUser.getId());
        return userPermissions.contains("content:vip:create");
    }
    
    // 公告类内容需要管理员权限
    if (ContentTypeEnum.ANNOUNCEMENT.getValue().equals(contentType)) {
        Set<String> userRoles = commonApi.queryUserRolesById(loginUser.getId());
        return userRoles.contains("admin") || userRoles.contains("moderator");
    }
    
    return true; // 普通内容可以创建
}

/**
 * 检查是否可以编辑内容
 */
private boolean canEditContent(LoginUser loginUser, ContentEntity content) {
    // 管理员可以编辑所有内容
    Set<String> userRoles = commonApi.queryUserRolesById(loginUser.getId());
    if (userRoles.contains("admin")) {
        return true;
    }
    
    // 内容创建者可以编辑自己的内容
    return loginUser.getUsername().equals(content.getCreateBy());
}

/**
 * 检查是否可以删除内容
 */
private boolean canDeleteContent(LoginUser loginUser, ContentEntity content) {
    // 管理员可以删除所有内容
    Set<String> userRoles = commonApi.queryUserRolesById(loginUser.getId());
    if (userRoles.contains("admin")) {
        return true;
    }
    
    // 内容创建者可以删除自己的内容
    return loginUser.getUsername().equals(content.getCreateBy());
}
```

#### 3.4 管理员权限分配实现

```java:content/controller/ContentAdminController.java
/**
 * 内容管理员控制器
 * 提供管理员专用的内容管理功能
 */
@Tag(name = "内容管理员", description = "管理员专用内容管理接口")
@RestController
@RequestMapping("/content/admin")
@RequiresRoles("admin")  // 整个控制器需要管理员角色
@Slf4j
public class ContentAdminController {
    
    @Autowired
    private IContentService contentService;
    
    /**
     * 管理员查看所有内容 - 包括未发布和私有内容
     */
    @RequiresPermissions("content:admin:list")
    @AutoLog(value = "内容管理-列表查询")
    @Operation(summary = "管理员内容列表", description = "管理员查看所有内容列表")
    @GetMapping("/list")
    public Result<IPage<ContentListRespVO>> adminQueryPageList(
            ContentQueryReqVO queryVO,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        
        Page<ContentEntity> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<ContentEntity> queryWrapper = new LambdaQueryWrapper<>();
        
        // 管理员可以查看所有状态的内容
        if (StringUtils.hasText(queryVO.getStatus())) {
            queryWrapper.eq(ContentEntity::getStatus, queryVO.getStatus());
        }
        
        queryWrapper.eq(ContentEntity::getDelFlag, CommonConstant.DEL_FLAG_0);
        queryWrapper.orderByDesc(ContentEntity::getCreateTime);
        
        IPage<ContentEntity> pageList = contentService.page(page, queryWrapper);
        
        // 转换为管理员视图VO（包含更多信息）
        List<ContentListRespVO> respList = pageList.getRecords().stream()
                .map(this::convertToAdminListVO)
                .collect(Collectors.toList());
        
        IPage<ContentListRespVO> result = new Page<>();
        BeanUtils.copyProperties(pageList, result);
        result.setRecords(respList);
        
        return Result.OK(result);
    }
    
    /**
     * 内容审核 - 管理员专用
     */
    @RequiresPermissions("content:admin:audit")
    @AutoLog(value = "内容管理-审核")
    @Operation(summary = "内容审核", description = "管理员审核内容")
    @PostMapping("/audit")
    public Result<?> auditContent(@RequestBody ContentAuditReqVO auditVO) {
        
        LoginUser loginUser = LoginUserUtils.getLoginUser();
        log.info("管理员{}审核内容，ID: {}, 结果: {}", 
                loginUser.getUsername(), auditVO.getContentId(), auditVO.getAuditResult());
        
        contentService.auditContent(auditVO.getContentId(), auditVO.getAuditResult(), 
                                  auditVO.getAuditRemark(), loginUser.getUsername());
        
        return Result.OK("审核完成");
    }
    
    /**
     * 强制删除内容 - 超级管理员专用
     */
    @RequiresRoles("super_admin")
    @RequiresPermissions("content:admin:force_delete")
    @AutoLog(value = "内容管理-强制删除")
    @Operation(summary = "强制删除内容", description = "超级管理员强制删除内容")
    @DeleteMapping("/forceDelete")
    public Result<?> forceDelete(@RequestParam(name = "id", required = true) String id) {
        
        LoginUser loginUser = LoginUserUtils.getLoginUser();
        log.warn("超级管理员{}强制删除内容，ID: {}", loginUser.getUsername(), id);
        
        contentService.forceDelete(id);
        return Result.OK("强制删除成功");
    }
}
```

#### 3.5 数据权限配置实现

```java:content/service/impl/ContentServiceImpl.java
/**
 * 内容服务实现类
 * 包含数据权限控制逻辑
 */
@Service
public class ContentServiceImpl extends ServiceImpl<ContentMapper, ContentEntity> implements IContentService {
    
    /**
     * 分页查询内容（带数据权限）
     */
    @Override
    @PermissionData(pageComponent = "content/ContentList")
    public IPage<ContentEntity> selectPageWithPermission(Page<ContentEntity> page, 
                                                        ContentQueryReqVO queryVO, 
                                                        String userId) {
        
        // 构建基础查询条件
        LambdaQueryWrapper<ContentEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContentEntity::getDelFlag, CommonConstant.DEL_FLAG_0);
        
        // 数据权限会通过@PermissionData注解自动注入以下条件：
        // 1. 普通用户只能看到自己创建的内容
        // 2. 部门管理员可以看到本部门的内容  
        // 3. 管理员可以看到所有内容
        
        return this.page(page, queryWrapper);
    }
    
    /**
     * 检查用户对内容的操作权限
     */
    @Override
    public boolean checkContentPermission(String contentId, String userId, String operation) {
        
        ContentEntity content = this.getById(contentId);
        if (content == null) {
            return false;
        }
        
        // 获取用户角色
        Set<String> userRoles = commonApi.queryUserRolesById(userId);
        
        // 管理员拥有所有权限
        if (userRoles.contains("admin")) {
            return true;
        }
        
        // 内容创建者权限检查
        if (userId.equals(content.getCreateBy())) {
            return Arrays.asList("view", "edit", "delete").contains(operation);
        }
        
        // VIP内容权限检查
        if (ContentTypeEnum.VIP.getValue().equals(content.getContentType())) {
            if ("view".equals(operation)) {
                return userRoles.contains("vip") || userRoles.contains("admin");
            }
            return false; // VIP内容只有管理员和创建者可以编辑/删除
        }
        
        // 公开内容的查看权限
        if ("view".equals(operation) && content.getIsPublic()) {
            return true;
        }
        
        return false;
    }
}
```

### 4. Shiro配置中的白名单设置

```java:config/shiro/ShiroConfig.java
@Bean("shiroFilterFactoryBean")
public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
    CustomShiroFilterFactoryBean shiroFilterFactoryBean = new CustomShiroFilterFactoryBean();
    shiroFilterFactoryBean.setSecurityManager(securityManager);
    
    Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
    
    // 公开访问的接口（无需登录）
    filterChainDefinitionMap.put("/content/public/**", "anon");  // 公开内容接口
    filterChainDefinitionMap.put("/sys/login", "anon");          // 登录接口
    filterChainDefinitionMap.put("/sys/logout", "anon");         // 登出接口
    filterChainDefinitionMap.put("/sys/randomImage/**", "anon"); // 验证码
    filterChainDefinitionMap.put("/doc.html", "anon");           // API文档
    filterChainDefinitionMap.put("/swagger-ui/**", "anon");      // Swagger UI
    
    // 其他接口需要JWT认证
    filterChainDefinitionMap.put("/**", "jwt");
    
    // 添加JWT过滤器
    Map<String, Filter> filterMap = new HashMap<>();
    filterMap.put("jwt", new JwtFilter(cloudServer == null));
    shiroFilterFactoryBean.setFilters(filterMap);
    
    shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
    return shiroFilterFactoryBean;
}
```

### 5. 前端权限控制配合

```typescript:hooks/usePermission.ts
// 前端权限检查Hook
export function usePermission() {
  const permissionStore = usePermissionStore();
  
  /**
   * 检查是否有指定权限
   */
  const hasPermission = (permission: string | string[]): boolean => {
    const allCodeList = permissionStore.getPermCodeList;
    if (!allCodeList || allCodeList.length === 0) {
      return false;
    }
    
    if (Array.isArray(permission)) {
      return permission.some(p => allCodeList.includes(p));
    }
    
    return allCodeList.includes(permission);
  };
  
  /**
   * 检查是否有指定角色
   */
  const hasRole = (role: string | string[]): boolean => {
    const userRoles = permissionStore.getRoleList;
    if (!userRoles || userRoles.length === 0) {
      return false;
    }
    
    if (Array.isArray(role)) {
      return role.some(r => userRoles.includes(r));
    }
    
    return userRoles.includes(role);
  };
  
  /**
   * 检查是否是VIP用户
   */
  const isVip = (): boolean => {
    return hasRole(['vip', 'admin']);
  };
  
  return {
    hasPermission,
    hasRole,
    isVip
  };
}
```

### 6. 权限编码规范

```
模块:功能:操作
例如：
- content:list         (内容列表查询)
- content:add          (内容新增)
- content:edit         (内容编辑)
- content:delete       (内容删除)
- content:vip:view     (VIP内容查看)
- content:vip:create   (VIP内容创建)
- content:admin:audit  (内容审核)
- content:batch:delete (批量删除)
```

这套权限控制方案具有以下特点：

1. **分层权限控制**：从接口级到数据级的全方位权限控制
2. **灵活的角色管理**：支持普通用户、VIP用户、管理员等多种角色
3. **细粒度权限**：支持按操作类型、内容类型的精细权限控制
4. **数据安全**：通过数据权限规则确保用户只能访问授权数据
5. **易于扩展**：基于注解的权限控制，便于新功能的权限集成
        