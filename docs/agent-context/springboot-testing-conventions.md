# 后端单元测试规范

## 测试覆盖率要求
- **行覆盖率 ≥ 90%**，低于此标准的代码不允许提交
- 重点覆盖：biz 层编排逻辑、service 层核心业务、controller 层参数校验与异常处理
- 不计入覆盖率：entity、req、vo、dto 等纯数据类、配置类、Flyway 迁移脚本

## 测试必须执行，不能只写不跑
- 写完测试后，**必须执行该测试类**确认通过，不能仅凭代码逻辑推断正确
- 代码变更涉及的模块，必须执行**模块级全量测试**（`mvn test -pl <module> -am`），不能只跑修改的测试类
- 原因：单个测试类通过不代表与其他测试无冲突，全量测试能发现 mock 泄漏、桩冲突等问题

## Service 测试（extends ServiceImpl）
- MyBatis-Plus 的 `ServiceImpl` 通过 `@Autowired` 注入 mapper，Mockito 的 `@InjectMocks` 不识别
- **必须**在 `@BeforeEach` 中添加：`ReflectionTestUtils.setField(service, "baseMapper", mockMapper);`
- 模式固定，无例外

## WebMvc 测试（涉及 SecureUtil.currentUser()）
- 控制器通过 `SecurityContextHolder.getContext().getAuthentication().getName()` 获取当前用户
- **必须**在 `@BeforeEach` 中设置 SecurityContext，序列化 `LoginUser` 为 JSON 作为 principal
- `@AfterEach` 中调用 `SecurityContextHolder.clearContext()` 清理

```java
@BeforeEach
void setUp() {
    LoginUser loginUser = new LoginUser();
    loginUser.setId("test-user-id");
    loginUser.setUsername("testuser");
    UsernamePasswordAuthenticationToken token =
        new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(token);
}

@AfterEach
void tearDown() {
    SecurityContextHolder.clearContext();
}
```

## Mock 完整性
- 被测类的所有 `@Resource` / `@Autowired` 依赖都必须 `@Mock`，遗漏任何一个都会导致 NPE
- 写完测试后自检：被测类的每个注入字段是否都有对应的 `@Mock`

## 断言消息对齐
- `hasMessage` / `hasMessageContaining` 的字符串必须与实际异常消息**完全一致**
- 修改了异常消息后，必须同步更新所有相关测试断言
- 优先用 `hasMessageContaining` 匹配关键子串，减少因文案微调导致的测试失败

## Mockito 桩签名
- `selectOne(wrapper)` 和 `selectOne(wrapper, throwEx)` 是两个不同方法
- 桩参数必须与实际调用签名匹配，否则触发 `PotentialStubbingProblem`

## LambdaQueryWrapper 在单元测试中的问题
- MyBatis-Plus 的 `LambdaQueryWrapper` 需要实体类通过 `TableInfoHelper.initTableInfo()` 注册
- 如果测试中出现 `type not in the cache` 错误，在 `@BeforeAll` 中添加：
```java
@BeforeAll
static void initTableInfo() {
    TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), EntityClass.class);
}
```
