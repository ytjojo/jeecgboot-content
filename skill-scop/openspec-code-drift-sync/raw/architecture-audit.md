# 架构审核维度 — 代码架构问题发现

## 概述

在 OpenSpec change 完成后，除了检查文档与代码的同步漂移外，还需要从**架构层面**审核代码是否存在结构性问题。架构审核关注的是代码的组织方式、模块边界、分层合规性和依赖方向，而非功能正确性。

## 与漂移检测的关系

| 检测类型 | 关注点 | 对比目标 |
|---------|--------|---------|
| 漂移检测 | 文档 vs 代码的一致性 | spec/design/tasks vs 实现代码 |
| **架构审核** | **代码内部的结构质量** | 项目架构规范 vs 实际代码组织 |

架构审核是漂移检测的**补充维度**——漂移检测回答"代码是否按文档说的做了"，架构审核回答"代码做得是否合理、是否破坏架构约束"。

---

## 审核维度

### 维度 A：分层架构合规性

**检查目标**：验证代码是否遵循项目定义的分层架构，是否存在跨层调用。

#### A.1 分层定义（本项目）

```
Controller (接口层) → Biz (业务层) → Service (服务层) → Mapper (持久层)
     ↓                    ↓                ↓               ↓
  @RestController     业务编排         数据访问          数据库操作
  参数校验/转换       事务边界         缓存/外部调用     MyBatis-Plus
```

#### A.2 检查项

| 检查项 | 判定标准 | 严重级别 |
|--------|---------|---------|
| Controller 直接调用 Mapper | Controller 不应绕过 Biz/Service 直接操作数据层 | CRITICAL |
| Controller 包含业务逻辑 | Controller 中不应出现复杂 if/else 分支、事务注解 | WARNING |
| Biz 层直接调用 Mapper | Biz 应通过 Service 层访问数据，直接调 Mapper 绕过缓存/切面 | WARNING |
| Service 调用 Controller | 绝对禁止反向依赖 | CRITICAL |
| Mapper 包含业务逻辑 | Mapper 应仅做数据访问，不应有业务判断 | WARNING |
| 跨模块直接调用 Mapper | 模块间应通过 Biz/Service 接口通信，禁止直接访问对方 Mapper | CRITICAL |

#### A.3 检查方式

```
1. 扫描 change 涉及的所有 Controller，检查其 @Autowired/构造注入的依赖
2. 逐层追溯调用链：Controller.inject → Biz.method → Service.method → Mapper.method
3. 标记跳层调用（如 Controller 注入了 Mapper）
```

---

### 维度 B：模块边界隔离

**检查目标**：验证跨模块通信是否通过正规接口，是否存在绕过边界的"后门"调用。

#### B.1 本项目模块结构

```
jeecg-boot-module/
├── jeecg-module-content/     ← 内容社区模块（本项目主模块）
├── jeecg-module-system/      ← 系统管理模块（基础库）
└── ...
```

#### B.2 检查项

| 检查项 | 判定标准 | 严重级别 |
|--------|---------|---------|
| 直接 import 其他模块的内部类 | 模块间只能通过 @FeignClient/@Api 或公共接口通信 | CRITICAL |
| new 关键字实例化其他模块的类 | 应通过依赖注入获取，不直接 new | WARNING |
| 直接访问其他模块的数据库表 | 每个模块只操作自己负责的表，跨模块数据通过 API 获取 | CRITICAL |
| 模块 A 的 Service 直接注入模块 B 的 Service | 检查是否应该抽象为 API 调用 | WARNING |
| 包结构混乱 | 新增代码放在正确的模块/包下（如 content 相关放在 jeecg-module-content） | FLAG |

#### B.3 检查方式

```
1. 扫描新增/修改文件的 import 语句
2. 识别跨模块 import：import 路径包含其他模块名（如 jeecg-module-system.xxx）
3. 对每个跨模块引用，判断是否为合法公共接口
```

---

### 维度 C：依赖方向正确性

**检查目标**：验证依赖关系是否符合"高层依赖低层"原则，是否存在循环依赖。

#### C.1 检查项

| 检查项 | 判定标准 | 严重级别 |
|--------|---------|---------|
| Entity 依赖 Service | Entity 是纯数据模型，不可依赖业务层 | CRITICAL |
| VO/DTO 包含业务逻辑 | VO/DTO 仅作数据传输，不应有方法/逻辑 | WARNING |
| Util 类依赖业务接口 | 工具类应无状态、无业务依赖 | WARNING |
| 循环依赖 (A → B → A) | Spring Bean 之间形成循环依赖链 | CRITICAL |
| Config 类依赖 Service | 配置类在容器启动早期加载，不应依赖运行时 Bean | WARNING |

#### C.2 循环依赖检测方式

```
1. 绘制 change 涉及的所有 Spring Bean 的 @Autowired 依赖图
2. 检测是否存在 A → B → ... → A 的回环路径
3. 常见问题模式：
   - ServiceA @Autowired ServiceB，同时 ServiceB @Autowired ServiceA
   - 通过 @Lazy 注解"掩盖"的循环依赖（应视为 WARNING）
```

---

### 维度 D：命名与组织规范

**检查目标**：验证新增代码的命名、目录位置是否符合项目约定。

#### D.1 检查项

| 检查项 | 判定标准 | 严重级别 |
|--------|---------|---------|
| Controller 类名不以 `Controller` 结尾 | 后端规范：XxxController | FLAG |
| Service 接口/实现命名不规范 | I 前缀接口 + Impl 后缀实现，或直接类名（按项目约定） | FLAG |
| Entity 类名与表名映射不一致 | @TableName 值应与类名对应 | WARNING |
| 目录位置不符合模块约定 | 如 content 模块代码放在 system 目录下 | CRITICAL |
| 常量/枚举集中管理 | 散落的魔法数字/字符串（排除已有常量类） | WARNING |
| 文件数量膨胀 | 单一目录文件数 > 20，考虑拆分子目录 | ADVISORY |

#### D.2 本项目关键约定

```
后端 Controller 路径前缀：/api/v1/content/
后端包结构基准：org.jeecg.modules.content.{domain}.{controller|biz|service|entity|mapper}
前端 API 封装目录：src/api/content/
前端组件目录：src/views/content/{domain}/
```

---

### 维度 E：过度工程化检测

**检查目标**：识别不必要的抽象、提前优化的模式、为"未来可能"写的代码。

#### E.1 检查项

| 检查项 | 判定标准 | 严重级别 |
|--------|---------|---------|
| 单一实现的接口 | 只有一个 Impl 的 Service 接口，若无扩展需求为过度抽象 | ADVISORY |
| 未使用的泛型/抽象类 | 为"未来扩展"设计的抽象但当前只有一个子类 | ADVISORY |
| 过度拆分 | 一个简单 CRUD 拆成 Controller → Biz → Service → Mapper + 4 个 VO/DTO | ADVISORY |
| 重复的转换层 | 同一数据在多个层之间反复转换（Entity → VO → DTO → VO' → Response） | WARNING |
| 配置项无默认值 | Config 类中无 @Value(defaultValue) 的必填配置 | WARNING |
| 提前的性能优化 | 无明显性能需求就加入缓存、异步、队列等 | ADVISORY |

---

### 维度 F：安全架构基线

**检查目标**：从架构层面发现安全隐患，不涉及具体漏洞扫描。

#### F.1 检查项

| 检查项 | 判定标准 | 严重级别 |
|--------|---------|---------|
| 公开 API 缺少权限注解 | Controller 方法无 @RequiresPermissions 或 @Anonymous | CRITICAL |
| 敏感数据透传 | VO 中包含密码/Token 等敏感字段直接返回前端 | CRITICAL |
| SQL 拼接 | Mapper 或 Service 中使用字符串拼接 SQL（应使用 MyBatis-Plus LambdaWrapper） | CRITICAL |
| 文件上传无限制 | 文件上传接口无大小/类型限制 | WARNING |
| 日志打印敏感信息 | log.info 中打印用户密码、完整身份证号等 | WARNING |

---

### 维度 G：可观测性架构

**检查目标**：确保新增代码具备基本的可观测性。

#### G.1 检查项

| 检查项 | 判定标准 | 严重级别 |
|--------|---------|---------|
| 关键业务操作无日志 | 创建/更新/删除核心实体无 log 记录 | WARNING |
| 异常吞没 | catch 块为空或仅 printStackTrace() | WARNING |
| 外部调用无超时 | 调用外部 API 无超时设置 | WARNING |
| 事务边界不当 | @Transactional 放在 Controller 层或范围过大/过小 | WARNING |

---

## 审核流程

### 输入

- change 目录路径（`openspec/changes/<name>/`）
- change 涉及的所有新增/修改文件列表（`git diff --name-only base..head`）
- 项目架构规范文档（`docs/agent-context/architecture.md`）

### 步骤

```
1. 确定变更范围
   - 获取 change 的所有 commit，提取文件变更列表
   - 按模块/包/层分类

2. 逐维度扫描
   - A 分层合规：遍历 Controller → Mapper 调用链
   - B 模块边界：检查跨模块 import
   - C 依赖方向：绘制 Bean 依赖图，检测循环
   - D 命名组织：验证文件位置和类名
   - E 过度工程：识别不必要的抽象
   - F 安全基线：权限注解、SQL 安全、敏感数据
   - G 可观测性：日志、异常处理、事务

3. 生成架构报告
   - 按严重级别分组（CRITICAL/WARNING/ADVISORY）
   - 每个问题标注：文件路径:行号 + 问题描述 + 修复建议
```

### 输出格式

```markdown
## 架构审核报告

### 概览
| 维度 | 检查项数 | CRITICAL | WARNING | ADVISORY | 状态 |
|------|---------|----------|---------|----------|------|
| A 分层合规 | 3 | 0 | 0 | 0 | ✅ |
| B 模块边界 | 5 | 1 | 0 | 0 | ❌ |
| ... | | | | | |

### 问题清单

#### CRITICAL
- **[B-001] 跨模块直接访问 Mapper**
  - 位置：`jeecg-module-content/.../CircleBizImpl.java:42`
  - 问题：直接 `@Autowired SysUserMapper`（属于 jeecg-module-system）
  - 修复：通过 `ISysUserService` 接口调用，或使用 Feign 调用系统模块 API

#### WARNING
...

#### ADVISORY
...
```

---

## 严重级别定义

| 级别 | 定义 | 处理方式 |
|------|------|---------|
| **CRITICAL** | 破坏架构约束，会导致维护灾难或安全漏洞 | 立即修复，阻断归档 |
| **WARNING** | 违反最佳实践，增加技术债务 | 建议修复，记录债务 |
| **ADVISORY** | 改进建议，不影响功能 | 记录，人工判断 |

---

## 与漂移检测的整合

架构审核产出独立的 `architecture-audit-report.md`，与漂移检测报告一起作为 change 归档前置检查的一部分：

```
openspec change 完成后：
  ├── drift-report.md           ← 漂移检测（文档 vs 代码）
  ├── architecture-audit-report.md  ← 架构审核（代码 vs 架构规范）
  └── verify-report.md          ← 综合验证报告（汇总）
```

综合门禁：任一份报告存在 **CRITICAL** → 禁止归档。

---

## 参考

- 项目分层架构：`docs/agent-context/architecture.md`
- 后端编码规范：`docs/agent-context/springboot-coding-conventions.md`
- 后端数据库设计：`docs/agent-context/springboot-db-design.md`
- AGENTS.md 全局硬规则
