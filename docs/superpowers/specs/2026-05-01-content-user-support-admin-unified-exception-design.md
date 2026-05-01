# 内容社区用户域 - 支持域 Admin 全部接口统一异常返回设计

## 1. 背景

当前支持域 admin 控制器已经覆盖以下接口：

- `POST /content/user/support/admin/appeal/handle`
- `POST /content/user/support/admin/report/handle`
- `GET /content/user/support/admin/report/list`
- `GET /content/user/support/admin/report/detail`

现有 `ContentUserSupportAdminControllerWebMvcTest` 采用 standalone MockMvc，自行挂载 controller 与 validator。该方式可以覆盖主路径与基础参数校验，但有一个明显缺口：

- 当 service 抛出 `JeecgBootException` 时，异常会直接向上冒泡
- 测试无法验证项目标准的统一错误体 `Result.error(...)`
- 支持域 admin 接口的异常行为，与项目已有 controller WebMvc 测试样板不一致

项目内已经存在成熟样板：

- `ContentAccountControllerWebMvcTest`
- `JeecgBootExceptionHandler`

因此本次最小方案应优先复用现有异常处理体系，而不是新增支持域专属异常处理器。

## 2. 目标

将支持域 admin 全部接口的 WebMvc 测试切换到项目统一异常处理模式，补齐业务异常返回覆盖，确保以下行为被测试锁定：

- `JeecgBootException` 经过 `JeecgBootExceptionHandler` 处理
- HTTP 状态保持 `200`
- 返回体 `success=false`
- 返回体 `message` 为业务异常文案

## 3. 范围

### 3.1 包含

- 改造 `ContentUserSupportAdminControllerWebMvcTest`
- 接入 `JeecgBootExceptionHandler`
- 补齐 support admin 全部 4 个接口的业务异常返回测试
- 保留并迁移现有成功路径与参数校验测试

### 3.2 不包含

- 不修改 `ContentUserSupportAdminController` 生产代码
- 不新增新的全局异常处理器
- 不修改 `JeecgBootExceptionHandler` 的生产逻辑
- 不扩展到用户侧支持域接口
- 不在本轮引入新的 service 逻辑

## 4. 方案选择

### 4.1 方案 A：继续使用 standalone MockMvc

优点：

- 改动小
- 已有测试基本可复用

缺点：

- 无法自然复用项目全局异常处理器装配方式
- 业务异常测试只能断言异常传播，不能验证标准错误体
- 与项目已有 controller WebMvc 测试模式不一致

### 4.2 方案 B：切换为 `@WebMvcTest` 并导入全局异常处理器

能力：

- 复用项目既有异常处理方式
- 直接校验 `Result` 统一错误返回结构
- 与 `ContentAccountControllerWebMvcTest` 保持一致

代价：

- 需要改造测试类初始化方式
- 需要补齐 `@MockitoBean` 依赖

### 4.3 结论

本次采用方案 B。

## 5. 设计

### 5.1 测试基础设施调整

将 `ContentUserSupportAdminControllerWebMvcTest` 从：

- `@ExtendWith(MockitoExtension.class)`
- `MockMvcBuilders.standaloneSetup(...)`
- `@Mock + @InjectMocks`

切换为：

- `@AutoConfigureMockMvc(addFilters = false)`
- `@Import(JeecgBootExceptionHandler.class)`
- `@WebMvcTest(controllers = ContentUserSupportAdminController.class, excludeAutoConfiguration = { SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class })`
- `@Autowired MockMvc`
- `@MockitoBean IContentUserSupportService`
- `@MockitoBean BaseCommonService`

说明：

- `BaseCommonService` 是 `JeecgBootExceptionHandler` 记录系统日志所需依赖
- 复用 `ContentAccountControllerWebMvcTest` 的既有接入模式，避免引入支持域特例

### 5.2 业务异常覆盖范围

新增以下测试：

- `shouldReturnBusinessErrorWhenHandleAppealThrowsException`
- `shouldReturnBusinessErrorWhenHandleReportThrowsException`
- `shouldReturnBusinessErrorWhenListReportsThrowsException`
- `shouldReturnBusinessErrorWhenGetReportDetailThrowsException`

覆盖目标：

- service 抛出 `JeecgBootException("...")`
- controller 返回 `200`
- JSON 返回：
  - `success=false`
  - `message=...`

### 5.3 现有测试迁移原则

现有主路径与参数校验测试继续保留，但要适配 `@WebMvcTest` 结构：

- 申诉处理成功
- 申诉处理非法请求
- 举报处理成功
- 举报处理非法请求
- 举报列表成功
- 举报列表分页参数非法
- 举报列表时间格式绑定成功
- 举报列表非法时间格式
- 举报详情成功

说明：

- 本次核心新增是“统一异常返回”覆盖
- 已有成功路径与校验路径不应在改造中回退

## 6. 风险与边界

- `JeecgBootExceptionHandler` 已显式处理 `JeecgBootException`，该部分预期稳定
- 查询参数绑定失败与 `@RequestBody` 校验失败是否统一为错误体，受 Spring 异常类型影响，可能并不完全一致
- 因此本轮“统一异常返回”验收重点只锁定业务异常路径，不额外扩大到所有校验异常类型的统一化改造

## 7. 验收标准

- `ContentUserSupportAdminControllerWebMvcTest` 成功切换为 `@WebMvcTest` 方案
- support admin 四个接口均有业务异常统一返回测试
- 业务异常测试均断言：
  - `status().isOk()`
  - `jsonPath("$.success").value(false)`
  - `jsonPath("$.message").value(...)`
- 现有支持域 admin controller 相关成功路径测试继续通过
- 不引入生产代码改动，除非测试暴露真实缺陷
