好的，基于之前的讨论，我们梳理并生成一份简洁、易执行的技术规范文档。

---

# Java 后端 VO 类技术规范 (v1.0)

## 1. 核心原则

1.  **单一职责 (Single Responsibility):** VO 是**表现层（Controller）与客户端**交互的专用对象，其结构应完全由前端需求决定。
2.  **解耦与隔离 (Decoupling & Isolation):** 严格隔离表现层（VO）、服务层（DTO）、持久层（Entity），禁止跨层引用。
3.  **明确无歧义 (Explicitness):** 包结构、类名、字段名必须清晰表达其意图。

## 2. 包结构规范

*   **必须** 为 VO 创建独立的包，与 `dto`, `entity` 包平级。
*   **建议** 按请求/响应子包划分，结构清晰。

```
src/main/java/com/[项目名]/
├── controller/        # 【必须】控制器包  
├── service/           # 【必须】服务层包  
├── vo/                # 【必须】视图对象包
│   ├── req/           # 【建议】请求VO包 (入参)
│   └── resp/          # 【建议】响应VO包 (出参)
├── entity/            # 【必须】持久层实体包   
├── dto/               # 【必须】服务层DTO包
├── converter/         # 【建议】转换器包 (存放MapStruct Mapper)
└── mapper/            # 【建议】数据库映射包 (MyBatis Mapper)
```

## 3. 命名规范

*   **类名:**
    *   使用 `XXXReqVO` 或 `XXXRequestVO` 表示**请求参数**（如 `UserCreateReqVO`）。
    *   使用 `XXXRespVO` 或 `XXXResponseVO` 表示**响应结果**（如 `UserInfoRespVO`）。
    *   名称需能直接表达业务含义。
*   **字段名:** 使用小驼峰，语义明确（如 `userId`, `userName`, `orderList`）。

## 4. 字段属性规范 (强制)

| 对象类型               | 是否允许作为 VO 字段 | 说明                                                                 |
| :--------------------- | :------------------: | :------------------------------------------------------------------- |
| 基本类型/包装类型/String |         **允许**         | 主体字段                                                               |
| 其他 VO 类型             |         **允许**         | **推荐**。用于组合多个视图对象（如 `OrderRespVO` 中包含 `UserSimpleRespVO`）。 |
| **DTO 类型**             |      **禁止**       | 破坏分层架构，会导致代码耦合和维护困难。                                     |
| **Entity 类型**          |      **禁止**       | 严重风险，可能导致敏感数据泄露和序列化问题。                                 |
| Enum 类型              |         **允许**         |                                                                      |

## 5. 转换规范

*   **禁止** 在 Controller 中手动编写 `getter/setter` 进行 DTO 与 VO 的转换。
*   **必须** 使用 **MapStruct** 注解处理器进行对象转换。
*   **建议** 在 `converter` 包中定义转换器接口。

**示例：**
1.  **引入依赖** (`pom.xml`):
    ```xml
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>
    ```
2.  **定义转换器**:
    ```java
    // UserConverter.java
    @Mapper(componentModel = "spring")
    public interface UserConverter {
        // 单个对象转换
        UserInfoRespVO dtoToRespVO(UserDTO userDTO);
        // 集合转换
        List<UserInfoRespVO> dtoListToRespVOList(List<UserDTO> userDTOList);
    }
    ```
3.  **在Controller中使用**:
    ```java
    @RestController
    public class UserController {
        @Autowired
        private UserService userService;
        @Autowired
        private UserConverter userConverter; // 注入MapStruct生成的实现

        @GetMapping("/users")
        public ApiResult<List<UserInfoRespVO>> getUsers() {
            List<UserDTO> dtoList = userService.getAllUsers();
            // 使用转换器转换
            List<UserInfoRespVO> voList = userConverter.dtoListToRespVOList(dtoList);
            return ApiResult.success(voList);
        }
    }
    ```

## 6. 数据流转标准流程

**请求方向:**
`Client Request` → `Controller`(接收 `ReqVO`) → `Converter.toDTO()` → `Service`(处理 `DTO`)

**响应方向:**
`Service`(返回 `DTO`) → `Controller` → `Converter.toVO()` → `RespVO` → `Client Response`

---

## 执行检查清单 (Checklist)

- [ ] VO 类是否放在独立的 `vo` 包下？
- [ ] VO 类名是否使用了 `ReqVO`/`RespVO` 后缀？
- [ ] VO 的字段是否全是基本类型、String、其他 VO 或 Enum？
- [ ] 是否**没有**将 DTO 或 Entity 作为 VO 的字段类型？
- [ ] 是否引入了 MapStruct 并定义了转换器接口？
- [ ] Controller 中是否通过转换器进行 DTO/VO 转换，而非手动赋值？

**负责人:** 所有后端开发人员
**监督机制:** Code Review 时严格检查，违反规范不予合并。