
# Java 后端 VO 类技术规范 (v1.0)

## 1. 核心原则

1.  **单一职责 (Single Responsibility):** VO 是**表现层（Controller）与客户端**交互的专用对象，其结构应完全由前端需求决定。
2.  **解耦与隔离 (Decoupling & Isolation):** 严格隔离表现层（VO）、服务层（DTO）、持久层（Entity），禁止跨层引用。
3.  **明确无歧义 (Explicitness):** 包结构、类名、字段名必须清晰表达其意图。
4. **职责明确**：VO只负责视图展示和请求参数，字段扁平化
5. **复杂业务区分场景** 区分不同场景，如列表、详情、编辑、统计等
## 2. 包结构规范

*   **必须** 为 VO 创建独立的包，与 `dto`, `entity` 包平级。
*   **建议** 按请求/响应子包划分，结构清晰。


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

## 3. 命名规范

* **类名:**
    - 使用 `XXXReqVO` 或 `XXXRequestVO` 表示**请求参数**（如 `UserCreateReqVO`）。
    - 使用 `XXXRespVO` 或 `XXXResponseVO` 表示**响应结果**（如 `UserInfoRespVO`）。
    - 名称需能直接表达业务含义。
* **字段名:** 使用小驼峰，语义明确（如 `userId`, `userName`, `orderList`）。
* 内部类命名
    - 信息类：`XxxInfo`
    - 数据类：`XxxData`
    - 统计类：`StatsInfo`
    - 配置类：`ConfigInfo`
* 复杂场景:根据业务不同，命名规范不同
    - 基础VO：`XxxVO`
    - 详情VO：`XxxDetailVO`
    - 列表项VO：`XxxListItemVO`
    - 统计VO：`XxxStatsVO`
    - 编辑VO：`XxxEditVO`


## 4. 字段属性规范 (强制)

| 对象类型               | 是否允许作为 VO 字段 | 说明                                                                 |
| :--------------------- | :------------------: | :------------------------------------------------------------------- |
| 基本类型/包装类型/String |         **允许**         | 主体字段                                                               |
| 其他 VO 类型             |         **允许**         | **推荐**。用于组合多个视图对象（如 `OrderRespVO` 中包含 `UserSimpleRespVO`）。 |
| **DTO 类型**             |      **禁止**       | 破坏分层架构，会导致代码耦合和维护困难。                                     |
| **Entity 类型**          |      **禁止**       | 严重风险，可能导致敏感数据泄露和序列化问题。                                 |
| Enum 类型              |         **允许**         |                                                                      |

## 5. 错误示例
```java
// ❌ 错误：VO直接依赖DTO，破坏分层
public class ContentVO {
    private String title;
    private AuthorDTO author;  // 跨层依赖
    private List<TagDTO> tags; // 跨层依赖
}
```
## 5. 转换规范

*   **禁止** 在 Controller 中手动编写 `getter/setter` 进行 DTO 与 VO 的转换。
*   **建议** 使用VO类内部静态方法进行转换，如 `fromDTO`、`toDTO` 等。   
*   **建议** 在 `converter` 包中定义转换器接口。

## 6. 数据流转标准流程

**请求方向:**
`Client Request` → `Controller`(接收 `ReqVO`) → `Converter.toDTO()` → `Service`(处理 `DTO`) → `Mapper`(数据库操作)

**响应方向:**
`Mapper`(数据库操作) → `Service`(返回 `DTO`) → `Controller` → `Converter.toVO()` → `RespVO` → `Client Response`

---


## 开发实践

### 性能考虑
- VO字段应该扁平化，避免过深嵌套
- 只包含前端真正需要的字段
- 考虑JSON序列化性能
- 大列表场景使用专门的ListItemVO

### 维护性考虑
- 每个VO都有明确的使用场景
- 提供专门的转换器类
- 添加充分的文档注释
- 保持VO的简洁性
### 开发实践中注意事项
1. **分层独立**：VO、DTO、Entity各自独立，避免跨层依赖
2. **职责单一**：VO只负责视图展示与请求参数，字段扁平化
3. **转换边界**：在Controller层进行DTO到VO的转换
4. **命名规范**：使用统一的命名规范和包结构
5. **专用转换器**：使用专门的转换器类处理对象转换
6. **区分复杂业务场景** 区分不同场景，如列表、详情、编辑、统计等
7. **避免过度设计** 只设计必要的VO类，避免过度复杂化


## 执行检查清单 (Checklist)

- [ ] VO 类是否放在独立的 `vo` 包下？
- [ ] VO 类名是否使用了 `ReqVO`/`RespVO` 后缀？
- [ ] VO 的字段是否全是基本类型、String、其他 VO 或 Enum？
- [ ] 是否**没有**将 DTO 或 Entity 作为 VO 的字段类型？
- [ ] Controller 中是否通过转换器进行 DTO/VO 转换，而非手动赋值？

**负责人:** 所有后端开发人员
**监督机制:** Code Review 时严格检查，违反规范不予合并。