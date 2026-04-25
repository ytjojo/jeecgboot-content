

# DTO类技术规范 V3.0

## 1. 核心原则

- **分层解耦**: 严格区分Entity(数据层)、DTO(服务层)、VO(控制层)
- **单一职责**: 每个数据对象必须有明确单一用途
- **语义明确**: 类名清晰表达用途和场景
- **数据安全**: 绝不暴露敏感信息
- **避免上帝对象**: 拒绝万能DTO，使用多个精确对象

## 2. 分层定义

| 类型 | 命名规范 | 所属层 | 核心职责 |
|------|----------|--------|----------|
| **Entity** | `XxxEntity` | 数据持久层 | 映射数据库表结构，承载核心业务数据 |
| **DTO** | `XxxDTO` | **服务层** | 服务内部数据传输，业务逻辑处理载体 |
| **VO** | `Xxx[操作][类型]VO` | **表现层** | 定义API输入输出契约，客户端交互协议 |

## 3. 场景化VO命名规范

### 请求VO (Input)
- `XxxCreateReqVO` - 创建请求
- `XxxUpdateReqVO` - 更新请求  
- `XxxQueryReqVO` - 查询参数请求

### 响应VO (Output)
- `XxxItemRespVO` - 列表项响应
- `XxxDetailRespVO` - 详情响应
- `XxxSimpleRespVO` - 简单关联对象响应

## 4. 多场景字段差异处理方案

### 方案一：组合模式（首选推荐）

**适用场景**: 所有业务场景，特别是大型复杂项目

**实现方式**: 通过对象组合来构建不同复杂度的响应结构

```java
// 1. 基础信息DTO - 包含最核心字段
@Data
public class UserBaseDTO {
    protected Long id;
    protected String name;
    protected String avatarUrl;
}

// 2. 列表项DTO - 极简字段
@Data
@EqualsAndHashCode(callSuper = true)
public class UserItemDTO extends UserBaseDTO {
    // 仅包含列表展示所需的最少字段
    private String title;
    private String departmentName;
}

// 3. 详情DTO - 扩展详细信息
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDetailDTO extends UserBaseDTO {
    // 扩展的详情字段
    private String email;
    private String phone;
    private String bio;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;
}

// 4. 复合详情DTO - 通过组合关联信息
@Data
public class UserCompositeDetailDTO {
    // 核心用户信息
    private UserDetailDTO userInfo;
    
    // 关联部门信息
    private DepartmentItemDTO department;
    
    // 关联角色列表
    private List<RoleItemDTO> roles;
    
    // 统计信息等
    private UserStatsDTO stats;
}
```

**优点**: 
- ✅ 极度灵活，可应对各种复杂场景
- ✅ 职责清晰，每个DTO用途明确
- ✅ 易于维护和扩展
- ✅ 天然支持微服务间数据聚合

**使用建议**: 作为默认方案在所有项目中推行

### 方案二：继承体系（限制使用）

**适用场景**: 简单的、稳定的线性字段扩展需求

**实现方式**: 通过类继承来扩展字段

```java
// 基类DTO
@Data
public class UserBaseDTO {
    private Long id;
    private String name;
}

// 列表DTO - 继承并扩展
@Data
@EqualsAndHashCode(callSuper = true)
public class UserItemDTO extends UserBaseDTO {
    private String avatarUrl;
    private Boolean onlineStatus;
}

// 详情DTO - 继续扩展
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDetailDTO extends UserItemDTO {
    private String email;
    private String phone;
    private String bio;
}
```

**限制条件**:
- ❗ 继承层级不得超过2层
- ❗ 仅适用于字段的简单追加，不适用于结构变化
- ❗ 扩展关系必须稳定，不会频繁变更

**使用建议**: 仅在小型项目或极其稳定的模块中谨慎使用

## 5. 转换规范

### 转换位置约定
- **Controller层**: 负责 `VO ⇄ DTO` 转换
- **Service层**: 负责 `DTO ⇄ Entity` 转换

### 转换方法规范
```java
// 在ReqVO中定义转换方法
public class UserCreateReqVO {
    @NotBlank private String name;
    @Email private String email;
    
    // VO → DTO
    public UserCreateDTO toDTO() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setName(this.name);
        dto.setEmail(this.email);
        return dto;
    }
}

// 在RespVO中定义转换方法  
public class UserItemRespVO {
    private Long id;
    private String name;
    
    // DTO → VO
    public static UserItemRespVO fromDTO(UserItemDTO dto) {
        UserItemRespVO vo = new UserItemRespVO();
        vo.setId(dto.getId());
        vo.setName(dto.getName());
        return vo;
    }
}

// 在DTO中定义转换方法
public class UserDTO {
    private Long id;
    private String name;
    
    // Entity → DTO
    public static UserDTO fromEntity(UserEntity entity) {
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }
    
    // DTO → Entity
    public UserEntity toEntity() {
        UserEntity entity = new UserEntity();
        entity.setId(this.id);
        entity.setName(this.name);
        return entity;
    }
}
```

## 6. 目录结构规范

### JeecgBoot项目标准目录结构

```
src/main/java/org/jeecg/modules/{module_name}/
├── controller/                    # 控制层
│   └── UserController.java
├── service/                      # 服务层
│   ├── IUserService.java         # 服务接口
│   └── impl/
│       └── UserServiceImpl.java  # 服务实现
├── vo/                           # VO类目录
│   ├── req/                      # 请求VO
│   │   ├── UserCreateReqVO.java
│   │   ├── UserUpdateReqVO.java
│   │   └── UserQueryReqVO.java
│   └── resp/                     # 响应VO
│       ├── UserItemRespVO.java
│       ├── UserDetailRespVO.java
│       └── UserSimpleRespVO.java
├── dto/                          # DTO类目录
│   ├── UserBaseDTO.java
│   ├── UserCreateDTO.java
│   ├── UserUpdateDTO.java
│   ├── UserQueryDTO.java
│   ├── UserItemDTO.java
│   ├── UserDetailDTO.java
│   └── component/                # 组件DTO
│       ├── UserBasicInfo.java
│       └── UserStatsInfo.java
├── entity/                       # 实体类
│   └── UserEntity.java
├── mapper/                       # 数据访问层
│   └── UserMapper.java
├── util/                         # 工具类
│   └── UserConverter.java        # 转换工具
├── enums/                        # 枚举类
│   ├── UserStatusEnum.java       # 用户状态枚举
│   └── UserTypeEnum.java         # 用户类
├── constant/                     # 常量类
│   └── UserConstants.java
└── config/                       # 配置类
    └── UserConfig.java
```

## 7. 强制规则

1. **禁止** Controller直接操作Entity
2. **禁止** Service直接返回VO
3. **禁止** DTO包含ORM注解（如`@Entity`, `@Column`）
4. **禁止** VO用于服务层数据传输
5. **禁止** 在Entity中编写转换逻辑
6. **禁止** 创建包含所有字段的"上帝DTO"

## 8. 方案选择指南

### 决策流程
```
是否需要处理多场景字段差异？
    → 是 → 使用【方案一：组合模式】
    → 否 → 使用基础DTO/VO规范
```

### 特殊情况处理
- **简单字段扩展** → 可谨慎使用方案二，但需团队评审
- **性能极致要求** → 在持久层通过其他技术优化，不改变DTO设计

## 9. 最佳实践建议

### 9.1 分层设计原则
- **基础层**：定义通用的分页、排序、时间范围等基础DTO
- **业务层**：继承基础DTO，添加业务特定字段
- **转换层**：统一的DTO与Entity转换工具类

### 9.2 命名规范统一
- 简单场景：
  - 优先使用: XxxDTO
  - 基础DTO：`BaseXxxDTO`
  - 列表ItemDTO：`XxxItemDTO`
  - 详情DTO：`XxxDetailDTO`
  - 后台管理AdminDTO：`XxxAdminDTO`
- 复杂场景：
  - 组合DTO：`XxxCompositeDTO`
  - 嵌套DTO：`XxxNestedDTO`

### 9.3 注解复用策略
- 将常用验证注解提取到基础类
- 使用注解组合减少重复
- 统一Schema描述格式

### 9.4 使用枚举和常量
根据场景,选择枚举或常量
可供参考:
1. 状态相关 → 使用枚举（Status、MemberRole、MemberStatus等）
2. 配置参数 → 使用常量（Config类）
3. 字符串常量 → 使用常量（CacheKey、PermissionType等）
4. 事件类型 → 可考虑枚举（EventType、OperationType）

### 9.5 转换工具统一
```java
/**
 * 统一转换工具类
 * 负责DTO与Entity之间的数据转换
 */
@Component
public class TopicDTOConverter {
    
    /**
     * 创建DTO转换为Entity
     * @param dto 创建DTO对象
     * @return Entity对象
     */
    public TopicEntity createDTOToEntity(TopicCreateDTO dto) {
        // 统一转换逻辑
        TopicEntity entity = new TopicEntity();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
    
    /**
     * 更新DTO数据到Entity
     * @param entity 目标Entity对象
     * @param dto 更新DTO对象
     */
    public void updateEntityFromDTO(TopicEntity entity, TopicUpdateDTO dto) {
        // 统一更新逻辑
        BeanUtils.copyProperties(dto, entity, getNullPropertyNames(dto));
    }
}
```

### 9.6 验证规则统一
- 创建验证组：`Create.class`、`Update.class`
- 使用 `@Validated` 分组验证
- 统一错误消息格式

### 9.7 核心实践要点
1. **优先使用组合模式**，即使对于简单场景
2. **保持DTO的纯洁性**，不包含业务逻辑
3. **及时重构**，当发现DTO被用于多个场景时立即拆分
4. **团队统一评审**，确保DTO设计符合规范
5. **显著减少代码冗余**，提高代码的可维护性和一致性，遵循DRY（Don't Repeat Yourself）原则
