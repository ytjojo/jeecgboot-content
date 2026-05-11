---
name: springboot-testing-standards
description: Spring Boot 项目的测试规范。当用户编写单元测试、集成测试、Controller 测试、MyBatis 测试、Mock 外部依赖、设计测试分层或补充回归用例时触发。适用于 JUnit 5、Mockito、AssertJ、MockMvc 等测试场景。
---

# Spring Boot 测试规范

本技能定义 Spring Boot 项目的基础测试写法。目标是让测试快速、稳定、易读，避免“测试很多但没有价值”。

## 测试分层

| 类型 | 目标 | 推荐工具 |
|------|------|----------|
| **单元测试** | 验证单个类的业务逻辑 | JUnit 5 + Mockito + AssertJ |
| **Controller 测试** | 验证接口入参、校验、返回结构 | `@WebMvcTest` + MockMvc |
| **Mapper / Repository 测试** | 验证 SQL、映射、分页 | `@MybatisTest` 或集成测试 |
| **集成测试** | 验证多层协作 | `@SpringBootTest` |

规则：
- **优先写单元测试**，只在确有必要时启动 Spring 上下文
- **优先使用测试切片**，不要动不动就全量 `@SpringBootTest`
- **一个测试只验证一个核心行为**

## 命名规范

```java
class UserBizManageServiceTest {

    @Test
    void createUser_usernameExists_throwsDuplicateResourceException() {
        // given / when / then
    }
}
```

规则：
- **测试类名**：`被测类名 + Test`
- **测试方法名**：`method_scenario_expected`
- **用例结构**：推荐 `given / when / then`

## 单元测试规范

```java
@ExtendWith(MockitoExtension.class)
class UserBizManageServiceTest {

    @InjectMocks
    private UserBizManageService userBizManageService;

    @Mock
    private UserService userService;

    @Test
    void getUserById_userNotFound_throwsResourceNotFoundException() {
        when(userService.getById(1L)).thenReturn(null);

        assertThatThrownBy(() -> userBizManageService.getUserById(1L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
```

规则：
- **单元测试不启动 Spring 容器**
- **依赖统一 Mock**，只验证当前类行为
- **断言优先使用 AssertJ**

## Controller 测试

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserBizManageService userBizManageService;

    @Test
    void saveUser_invalidUsername_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/users/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"","password":"12345678"}
                    """))
            .andExpect(status().isOk());
    }
}
```

规则：
- **Controller 测试只测接口层职责**：参数校验、状态码、返回结构
- **不要在 Controller 测试里验证 Service 内部逻辑**
- **返回结构要校验 `Result` 包装是否正确**

## 集成测试规范

```java
@SpringBootTest
@Transactional(rollbackFor = Exception.class)
class UserIntegrationTest {

    @Resource
    private UserService userService;

    @Test
    void createUser_validInput_success() {
        // 集成验证
    }
}
```

规则：
- **集成测试用于验证真实 Bean 协作**
- **涉及数据库写操作的测试默认回滚**
- **集成测试数量保持精简，重点覆盖关键链路**

## 常见红线

- **不要使用 `Thread.sleep()` 等待异步结果**
- **不要把大量业务准备逻辑塞进测试方法**
- **不要为了凑覆盖率写没有断言的测试**
- **不要让测试依赖执行顺序**
- **不要在单元测试里连接真实数据库或 Redis**

## 检查清单

- [ ] 是否选择了合适的测试层级？
- [ ] 测试名称是否清楚表达场景和预期？
- [ ] 是否只验证了一个核心行为？
- [ ] 是否避免了 `Thread.sleep()` 和不稳定等待？
- [ ] 是否避免把全量 `@SpringBootTest` 当默认方案？
