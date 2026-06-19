# 评估维度定义

本文档定义 `openspec-code-drift-sync` 技能的全部评估维度，每个维度包含：检查目标、检查项清单、判定标准、严重级别。

---

## 第一部分：漂移检测维度

### 维度 B-1：API 端点对比（后端）

**检查目标**：验证 spec/design 文档中声明的 API 端点与实际 Controller 实现完全一致。

**检查项**：

| 编号 | 检查项 | 判定标准 | 严重级别 |
|------|--------|---------|---------|
| B-1-1 | 端点存在性 | spec/design 声明的端点必须在 Controller 中有对应方法 | CRITICAL |
| B-1-2 | HTTP 方法一致 | Controller 的 @GetMapping/@PostMapping 等与文档一致 | CRITICAL |
| B-1-3 | 路径一致 | 实际路径与文档路径匹配（含路径参数） | WARNING |
| B-1-4 | 入参名一致 | @RequestParam/@PathVariable 名与文档一致 | WARNING |
| B-1-5 | 入参类型一致 | 参数类型与文档描述一致（String/Integer/Long） | CRITICAL |
| B-1-6 | 入参必填一致 | @RequestParam(required) 与文档必填标注一致 | WARNING |
| B-1-7 | 出参类型一致 | 返回 VO 类名与文档一致 | WARNING |
| B-1-8 | 出参字段一致 | VO 字段名与文档返回字段一致 | WARNING |
| B-1-9 | 多余端点 | Controller 中存在但文档未声明的端点 | SUGGESTION |
| B-1-10 | 文档多余端点 | 文档声明但 Controller 不存在的端点 | CRITICAL |

**检查方式**：
1. 从 spec.md 和 design.md 提取所有 API 端点（HTTP 方法 + 路径 + 参数 + 返回类型）
2. 搜索 change 涉及的所有 Controller，读取每个端点方法签名
3. 逐条对比，输出差异表

---

### 维度 B-2：VO/DTO 字段完整性（后端）

**检查目标**：验证 spec 要求返回的字段在 VO 中声明且被 Service 实际赋值。

**检查项**：

| 编号 | 检查项 | 判定标准 | 严重级别 |
|------|--------|---------|---------|
| B-2-1 | 字段声明 | spec "系统 SHALL 展示/返回 XXX" 字段 → VO 是否有对应字段 | WARNING |
| B-2-2 | 字段赋值 | VO 声明的字段 → Service 中是否实际赋值 | WARNING |
| B-2-3 | 空字段 | 字段已声明但从未被赋值 | WARNING |
| B-2-4 | 字段类型匹配 | 字段类型与 spec 描述一致 | WARNING |
| B-2-5 | 多余字段 | VO 有但 spec 未提及的字段 | SUGGESTION |
| B-2-6 | 命名差异 | 字段名大小写/命名风格差异（如 badgeId vs badgeCode） | WARNING |

**检查方式**：
1. 从 spec Requirement 提取所有 "SHALL 展示/返回" 的字段名
2. 读取对应 VO 类的完整字段列表（`private Xxx fieldName;`）
3. 搜索 Service 中对 VO 字段的 set 调用或 Builder 赋值
4. 三向对比（spec ↔ VO ↔ Service.setter）

---

### 维度 B-3：业务规则覆盖（后端）

**检查目标**：验证 spec 中的每个 Scenario 在 Service 中有对应的代码分支。

**检查项**：

| 编号 | 检查项 | 判定标准 | 严重级别 |
|------|--------|---------|---------|
| B-3-1 | 主路径覆盖 | Scenario 的主流程在代码中有对应实现 | CRITICAL |
| B-3-2 | 条件分支覆盖 | Scenario 的 WHEN 条件在代码中有对应 if/switch | WARNING |
| B-3-3 | 返回结果覆盖 | Scenario 的 THEN/AND 结果在代码中有对应 return | WARNING |
| B-3-4 | 边界条件覆盖 | 空值、超出范围、权限边界的 Scenario 有对应处理 | WARNING |
| B-3-5 | 完全缺失 | Scenario 在代码中无任何对应逻辑 | CRITICAL |
| B-3-6 | 部分覆盖 | 主路径有但边界处理缺失 | WARNING |

**覆盖级别**：完全覆盖 / 部分覆盖（描述缺什么） / 完全缺失

**检查方式**：
1. 从 spec 提取所有 `#### Scenario:`（WHEN/THEN/AND）
2. 在 Service 实现中搜索对应分支（if/else/switch/return）
3. 标注每个 Scenario 的覆盖级别

---

### 维度 B-4：设计决策遵循（后端）

**检查目标**：验证 design.md 中的每个决策在代码中被正确执行。

**检查项**：

| 编号 | 检查项 | 判定标准 | 严重级别 |
|------|--------|---------|---------|
| B-4-1 | 决策实现 | design.md Decision 在代码中有对应实现 | WARNING |
| B-4-2 | 实现正确 | 实现方式与决策描述一致 | WARNING |
| B-4-3 | 偏离但更优 | 代码偏离决策但实现更完善 | SUGGESTION |
| B-4-4 | 偏离需对齐 | 代码偏离决策且不如决策方案 | WARNING |
| B-4-5 | 决策过时 | 决策描述的功能已不再需要 | SUGGESTION |

**决策类型覆盖**：目录结构、命名约定、缓存策略、降级方案、事务策略、权限方案

**判定结果**：遵循 / 偏离但更优（需更新文档） / 偏离需对齐（需修复代码） / 未实现

---

### 维度 B-5：数据一致性（后端）

**检查目标**：验证数据库表结构、Entity 映射、spec 常量值三者一致。

**检查项**：

| 编号 | 检查项 | 判定标准 | 严重级别 |
|------|--------|---------|---------|
| B-5-1 | 表存在 | Flyway SQL 中表存在 vs Entity @TableName 一致 | CRITICAL |
| B-5-2 | 字段映射 | 表字段名 vs Entity @TableField 映射一致 | WARNING |
| B-5-3 | 字段类型 | 表字段类型（varchar/int） vs Entity 字段 Java 类型一致 | WARNING |
| B-5-4 | 索引一致 | 文档声明的索引 vs 实际 DDL 索引 | WARNING |
| B-5-5 | 常量值 | spec/design 描述的阈值/上限/枚举值 vs 代码常量 | WARNING |
| B-5-6 | 枚举值 | spec 枚举值 vs 数据库数据 vs Java 枚举 | WARNING |

**检查方式**：
1. 读取 Flyway SQL 的表结构（字段名、类型、索引）
2. 读取 Java Entity 的字段映射
3. 对比 spec/design 描述的常量值
4. 标注不一致处

---

### 维度 B-6：上下游引用（后端）

**检查目标**：验证 change 涉及的新增/修改 API 在上下游文档和代码中有正确引用。

**检查项**：

| 编号 | 检查项 | 判定标准 | 严重级别 |
|------|--------|---------|---------|
| B-6-1 | 调用方识别 | 识别所有调用变更 API 的模块/类 | SUGGESTION |
| B-6-2 | 文档引用 | 变更 API 在 `docs/backend-api/` 其他文档中有交叉引用 | SUGGESTION |
| B-6-3 | 引用遗漏 | 调用方存在于代码但未在文档中记录 | WARNING |
| B-6-4 | 废弃引用 | 文档引用了已不存在或已修改的 API | WARNING |

---

### 维度 F-1：完整性检查（前端）

**检查目标**：验证 tasks.md 任务完成度和 spec Requirement 实现覆盖。

**检查项**：

| 编号 | 检查项 | 判定标准 | 严重级别 |
|------|--------|---------|---------|
| F-1-1 | 任务完成 | tasks.md 所有 `- [ ]` 已勾选为 `- [x]` | CRITICAL |
| F-1-2 | Requirement 覆盖 | 每个 spec Requirement 有对应前端实现 | CRITICAL |
| F-1-3 | Scenario 覆盖 | 每个 spec Scenario 有对应前端交互代码 | WARNING |

---

### 维度 F-2：前后端接口一致性（前端）

**检查目标**：验证前端 API 调用与后端 Controller 定义完全匹配。

**检查项**：

| 编号 | 检查项 | 判定标准 | 严重级别 |
|------|--------|---------|---------|
| F-2-1 | API 存在 | 前端调用的每个 API 在后端有对应 Controller 方法 | CRITICAL |
| F-2-2 | 路径一致 | 前端请求路径与后端 @RequestMapping 一致 | CRITICAL |
| F-2-3 | 方法一致 | 前端 HTTP 方法与后端一致（get/post/put/delete） | CRITICAL |
| F-2-4 | 参数名一致 | 前端传参属性名 vs 后端 @RequestParam 名 | WARNING |
| F-2-5 | 参数类型一致 | 前端传参类型 vs 后端参数类型 | WARNING |
| F-2-6 | 多余调用 | 前端调用了后端不存在的 API | CRITICAL |
| F-2-7 | 未对接 | 后端有但前端未调用的 API | SUGGESTION |

---

### 维度 F-3：VO/DTO 字段级对齐（前端）

**检查目标**：验证前端类型定义与后端 VO 字段一致。

**检查项**：

| 编号 | 检查项 | 判定标准 | 严重级别 |
|------|--------|---------|---------|
| F-3-1 | 字段存在 | 后端 VO 字段在前端 interface/type 中有对应 | WARNING |
| F-3-2 | 类型一致 | 前端字段 TypeScript 类型与后端 Java 类型对应 | WARNING |
| F-3-3 | 多余字段 | 前端类型定义有但后端 VO 无的字段 | SUGGESTION |

---

### 维度 F-4：设计决策有效性（前端）

**检查目标**：验证 design.md 决策在前端代码中的执行情况。

**检查项**：

| 编号 | 检查项 | 判定标准 | 严重级别 |
|------|--------|---------|---------|
| F-4-1 | 路由方案 | design.md 路由路径 vs 实际 router 注册 | WARNING |
| F-4-2 | 状态管理 | design.md Pinia store 定义 vs 实际 store 文件 | WARNING |
| F-4-3 | 组件拆分 | design.md 组件树 vs 实际组件目录 | SUGGESTION |
| F-4-4 | API 封装 | design.md API 依赖清单 vs 实际 defHttp 调用 | WARNING |

---

### 维度 F-5：降级策略验证（前端）

**检查目标**：验证 design.md 降级方案在前端实际可执行。

**检查项**：

| 编号 | 检查项 | 判定标准 | 严重级别 |
|------|--------|---------|---------|
| F-5-1 | 字段缺失降级 | design.md "缺失字段降级策略" 表中每个字段的降级方案已实现 | WARNING |
| F-5-2 | 降级可行性 | 降级方案中的常量值/默认值实际存在 | WARNING |
| F-5-3 | 后端字段确认 | 后端确认该字段是否确实缺失（读 VO 源码） | SUGGESTION |

---

## 第二部分：架构审核维度

### 维度 A：分层架构合规性

| 编号 | 检查项 | 判定标准 | 严重级别 |
|------|--------|---------|---------|
| A-1 | Controller 直接调用 Mapper | Controller 不应绕过 Biz/Service 直接操作数据层 | CRITICAL |
| A-2 | Controller 包含业务逻辑 | Controller 中不应出现复杂 if/else 分支、事务注解 | WARNING |
| A-3 | Biz 层直接调用 Mapper | Biz 应通过 Service 层访问数据 | WARNING |
| A-4 | Service 调用 Controller | 绝对禁止反向依赖 | CRITICAL |
| A-5 | Mapper 包含业务逻辑 | Mapper 应仅做数据访问 | WARNING |
| A-6 | 跨模块直接调用 Mapper | 模块间应通过 Biz/Service 接口通信 | CRITICAL |

### 维度 B：模块边界隔离

| 编号 | 检查项 | 判定标准 | 严重级别 |
|------|--------|---------|---------|
| B-1 | 直接 import 其他模块内部类 | 模块间只能通过 Feign/API/公共接口通信 | CRITICAL |
| B-2 | new 实例化其他模块类 | 应通过依赖注入 | WARNING |
| B-3 | 直接访问其他模块数据库表 | 每个模块只操作自己的表 | CRITICAL |
| B-4 | 跨模块 Service 注入 | 检查是否应抽象为 API 调用 | WARNING |
| B-5 | 包结构混乱 | 代码放在正确的模块/包下 | FLAG |

### 维度 C：依赖方向正确性

| 编号 | 检查项 | 判定标准 | 严重级别 |
|------|--------|---------|---------|
| C-1 | Entity 依赖 Service | Entity 是纯数据模型，不可依赖业务层 | CRITICAL |
| C-2 | VO/DTO 包含业务逻辑 | VO/DTO 仅作数据传输 | WARNING |
| C-3 | Util 类依赖业务接口 | 工具类应无状态、无业务依赖 | WARNING |
| C-4 | 循环依赖 (A→B→A) | Spring Bean 之间形成循环依赖链 | CRITICAL |
| C-5 | Config 类依赖 Service | 配置类不应依赖运行时 Bean | WARNING |

### 维度 D：命名与组织规范

| 编号 | 检查项 | 判定标准 | 严重级别 |
|------|--------|---------|---------|
| D-1 | Controller 命名 | XxxController 结尾 | FLAG |
| D-2 | Service 命名 | I 前缀接口 + Impl 后缀实现 | FLAG |
| D-3 | Entity 表名映射 | @TableName 值与类名对应 | WARNING |
| D-4 | 目录位置 | 代码放在正确的模块目录下 | CRITICAL |
| D-5 | 魔法值 | 散落的魔法数字/字符串 | WARNING |
| D-6 | 文件膨胀 | 单一目录文件数 > 20 | ADVISORY |

### 维度 E：过度工程化检测

| 编号 | 检查项 | 判定标准 | 严重级别 |
|------|--------|---------|---------|
| E-1 | 单一实现接口 | 只有一个 Impl 的 Service 接口 | ADVISORY |
| E-2 | 未使用泛型/抽象类 | 为"未来扩展"设计的抽象但只有一个子类 | ADVISORY |
| E-3 | 过度拆分 | 简单 CRUD 过度分层 | ADVISORY |
| E-4 | 重复转换层 | 同一数据多层反复转换 | WARNING |
| E-5 | 无默认值配置 | Config 中无 defaultValue 的必填配置 | WARNING |
| E-6 | 提前优化 | 无性能需求就加入缓存/异步/队列 | ADVISORY |

### 维度 F：安全架构基线

| 编号 | 检查项 | 判定标准 | 严重级别 |
|------|--------|---------|---------|
| F-1 | 缺少权限注解 | 公开 API 无 @RequiresPermissions 或 @Anonymous | CRITICAL |
| F-2 | 敏感数据透传 | VO 中包含密码/Token 等敏感字段 | CRITICAL |
| F-3 | SQL 拼接 | 字符串拼接 SQL | CRITICAL |
| F-4 | 文件上传无限制 | 无大小/类型限制 | WARNING |
| F-5 | 日志泄露敏感信息 | log 中打印密码/身份证号等 | WARNING |

### 维度 G：可观测性架构

| 编号 | 检查项 | 判定标准 | 严重级别 |
|------|--------|---------|---------|
| G-1 | 关键操作无日志 | 创建/更新/删除核心实体无 log | WARNING |
| G-2 | 异常吞没 | catch 块为空或仅 printStackTrace() | WARNING |
| G-3 | 外部调用无超时 | 调外部 API 无超时设置 | WARNING |
| G-4 | 事务边界不当 | @Transactional 放在 Controller 或范围不当 | WARNING |

---

## 得分计算规则

每个维度满分 10 分，扣分规则：

| 情况 | 扣分 |
|------|------|
| 检查项 PASS | 0 |
| 检查项 SUGGESTION/ADVISORY | -0.5 |
| 检查项 WARNING/FLAG | -1 |
| 检查项 CRITICAL | -2 |
| 缺失关键 artifact | 该维度直接 0 分 |

**综合满分**：漂移检测（后端 60 + 前端 50）+ 架构审核 70 = 180 分（全量时）
**实际满分**：根据 change 类型动态计算（仅后端、仅前端、前后端配对）
