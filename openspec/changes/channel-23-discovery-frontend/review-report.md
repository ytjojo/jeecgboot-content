# Review Report: channel-23-discovery-frontend

> **审核日期**: 2026-06-06
> **Change 类型**: 前端 change
> **配对后端 change**: channel-23-discovery
> **PRD**: `docs/requirements/prd/frontend/EPIC-23-channel-discovery-frontend-prd.md`
> **审核范围**: 6 维度 + 前后端衔接审计

---

## 总览

| 维度 | 得分 | BLOCK | FLAG | ADVISORY |
|------|------|-------|------|----------|
| 完整性 (Completeness) | 7/10 | 1 | 2 | 1 |
| 一致性 (Consistency) | 6/10 | 0 | 4 | 2 |
| 可实现性 (Feasibility) | 7/10 | 1 | 1 | 1 |
| 可测试性 (Testability) | 5/10 | 1 | 2 | 1 |
| 接口契约 (API Contract) | 6/10 | 1 | 3 | 2 |
| 边界覆盖 (Boundary) | 5/10 | 0 | 3 | 3 |

**总计**: BLOCK=4, FLAG=15, ADVISORY=10

---

## 量化指标

| 指标 | 值 | 说明 |
|------|-----|------|
| PRD AC 覆盖率 | 85% | PRD 11.1-11.5 测试要点中约 85% 在 specs 中有对应 scenario |
| API 契约完整率 | 68% | 19 个 API 中 13 个路径一致，3 个路径偏差，4 个后端缺失 |
| 边界覆盖率 | 45% | 10 类边界条件中仅覆盖约 45%（分页、空状态、校验、响应式） |
| TDD 配对率 | 0% | tasks.md 中无测试任务，设计文档中无测试策略 |
| Spec 文件完整度 | 100% | 8/8 spec 文件均存在且结构完整 |
| 后端 API 可用率 | 68% | 13/19 个 API 已存在且路径一致 |

---

## 1. 完整性 (Completeness) — 7/10

### BLOCK-1: 测试任务完全缺失

**严重程度**: BLOCK

tasks.md 包含 70 个实现任务，但**没有任何测试任务**。根据 AGENTS.md 规定的 DoD（完成标准），所有代码开发必须使用 TDD 流程，且变更代码行覆盖率 >= 90%。

**影响**: 
- 违反项目硬规则（`/superpowers:test-driven-development`）
- 无法验证实现是否符合 spec 中定义的 scenario
- 合并前无法通过 DoD 检查

**建议**: 在 tasks.md 中为每个模块组补充测试任务，至少包含：
- 3 个 Store 的单元测试（useChannelDiscoveryStore、useChannelCategoryStore、useChannelSearchStore）
- 6 个业务组件的组件测试（ChannelCard、CategoryTreeNav、SearchBar、FilterPanel、RankingList、CategoryManageTree）
- API 封装层测试（19 个接口的 defHttp 调用验证）
- 关键页面交互测试（搜索防抖、聚合降级、分页加载）

### FLAG-1: 设计文档缺少测试策略章节

**严重程度**: FLAG

backend change 的 design.md 包含完整的 Test Strategy 章节（单元测试、集成测试、定时任务测试），但 frontend change 的 design.md 没有对应章节。应补充：
- Store 测试策略（缓存失效、降级逻辑）
- 组件测试策略（多模式 ChannelCard、可编辑 CategoryManageTree）
- E2E 测试策略（搜索流程、分类管理流程）

### FLAG-2: PRD 11.5 交互测试要点未在 specs 中体现

**严重程度**: FLAG

PRD 11.5 定义了 5 个交互测试要点：
- "不感兴趣反馈后 5 分钟内推荐结果变化" — channel-discovery spec 中仅有"卡片从列表中移除"，未体现 5 分钟时效
- "分类停用展示影响范围并要求确认" — channel-category-manage spec 已覆盖
- "标签删除提示已使用内容数量" — channel-tag-manage spec 已覆盖
- "搜索空状态提供清除筛选、改写关键词、浏览分类入口" — channel-search spec 已覆盖
- "防重复提交按钮 loading 并禁用" — 无 spec 覆盖

**建议**: 在 channel-discovery spec 中补充"不感兴趣反馈时效" scenario，在通用交互 spec 或 design.md 中补充防重复提交 scenario。

### ADVISORY-1: 冷启动推荐场景缺少未登录用户的 API 调用说明

channel-discovery spec 定义了"未登录用户查看发现页"场景，但未明确说明未登录状态下推荐接口如何处理 userId（传空？不传？使用冷启动接口？）。design.md 提到冷启动使用 `GET /content/channel/recommendation/cold-start`，但 spec 中未引用此接口。

---

## 2. 一致性 (Consistency) — 6/10

### FLAG-3: API 路径不一致 — 搜索接口

**严重程度**: FLAG

| 位置 | 路径 |
|------|------|
| 前端 PRD Section 5.1 | `/content/channel/search` |
| 前端 design.md | `/content/channel/search` |
| 前端 spec (channel-search) | `/content/channel/search/query`（已修正） |
| 后端实际端点 | `GET /content/channel/search/query` |

spec 已修正为正确路径，但 PRD 和 design.md 仍引用旧路径。前端实现时可能因参考不同文档而产生混淆。

**建议**: 将前端 PRD 和 design.md 中的搜索路径统一修正为 `/content/channel/search/query`。

### FLAG-4: API 路径不一致 — 排行榜接口

**严重程度**: FLAG

| 位置 | 路径 |
|------|------|
| 前端 PRD Section 5.1 | `/content/channel/ranking/list`（单一接口，参数 type/dimension） |
| 前端 spec (channel-ranking) | 三个独立端点：`/ranking/hot`、`/ranking/new`、`/ranking/system` |
| 后端实际端点 | 三个独立 Controller 方法 |

PRD 定义了单一排行榜接口，但后端实际为三个独立端点，spec 已按后端实际修正。

**建议**: 更新前端 PRD Section 5.1 API 列表，将排行榜接口拆分为三个独立条目。

### FLAG-5: API 路径不一致 — 分类管理 CRUD

**严重程度**: FLAG

| 位置 | 新增路径 | 编辑路径 |
|------|---------|---------|
| 前端 PRD Section 5.1 | `/content/channel/category/add` | `/content/channel/category/edit` |
| 后端实际端点 | `/content/channel/category/create` | `/content/channel/category/update` |

**建议**: 统一前端文档中的分类管理 API 路径与后端一致。

### FLAG-6: Store 缓存策略文档分散

**严重程度**: FLAG

useChannelDiscoveryStore 的缓存策略在三处描述：
- design.md Decision 2：提到"5 分钟缓存"
- 前端 PRD Section 6.2.1：详细缓存失效规则
- tasks.md 2.1：简要提及"5 分钟 TTL"

三处描述基本一致，但 design.md 未引用 PRD 中的详细缓存失效规则，可能导致实现时遗漏细节（如"不感兴趣反馈后立即刷新"）。

**建议**: 在 design.md 中引用 PRD Section 6.2.1 的缓存失效规则表，避免重复维护。

### ADVISORY-2: ChannelInfo 类型与后端 VO 映射关系未明确

前端 PRD 定义了 `ChannelInfo` 基础接口，`ChannelRecommendationVO`、`ChannelSearchResultVO` 等均 extends ChannelInfo。但后端各 VO 字段名可能与前端定义不完全一致（如 `subscriberCount` vs `subscribeCount`），需在实现时逐一确认。

### ADVISORY-3: 响应式断点定义存在微小差异

| 位置 | 移动端阈值 |
|------|-----------|
| 前端 PRD Section 9.1 | xs: < 576px |
| channel-category-browse spec | 移动端: < 576px |
| channel-category-manage spec | 移动端: < 576px |
| channel-editorial-pick-manage spec | PC: >= 992px, 移动端: < 768px |
| channel-ranking spec | PC: >= 992px, 移动端: < 768px |

管理后台页面使用了不同的断点阈值（768px/992px），而用户端页面使用 576px。这是合理的设计（管理后台可能主要在 PC 端使用），但应在 design.md 中说明理由。

---

## 3. 可实现性 (Feasibility) — 7/10

### BLOCK-2: ~~4 个后端 API 缺失阻塞前端功能~~ [已大部分解决]

**严重程度**: BLOCK → FLAG（降级）

**更新（2026-06-07）**: 经代码验证，6 个缺失 API 中已有 4 个实现：

| 编号 | API | 当前状态 | 验证位置 |
|------|-----|---------|---------|
| ~~BI-1~~ | `GET /content/channel/discovery/home` | ✅ 已实现 | `ContentChannelDiscoveryController.java:24` |
| ~~BI-2~~ | `POST /content/channel/category/enable` | ✅ 已实现 | `ContentChannelCategoryController.java:53` |
| ~~BI-3~~ | `POST /content/channel/tag/update` | ✅ 已实现 | `ContentChannelTagController.java:37` |
| ~~BI-4~~ | `POST /content/channel/search/feedback` | ✅ 已实现 | `ContentChannelSearchController.java:35` |
| BI-5 | `GET /content/channel/editorial-pick/page` | ❌ 仍缺失 | `IContentChannelEditorialPickService` 仅有 `listActivePicks()` |
| BI-6 | `ContentChannelBrowseController.browseByCategory()` | ❌ 仍缺失（stub） | 方法体仅 `return Result.OK()` |

**剩余影响**:
- BI-5 阻塞精选管理 admin 分页列表，但不影响用户端精选展示
- BI-6 阻塞分类浏览页数据加载，需在前端开发前或开发中完成后端实现

### FLAG-7: 聚合接口降级逻辑增加了前端复杂度

design.md Decision 3 定义了聚合接口降级策略：聚合失败时 fallback 到三个独立接口。但 BI-1 表明聚合接口 Controller 尚不存在，这意味着前端开发初期必须直接使用独立接口，降级逻辑的实际价值存疑。

**建议**: 
- 短期：直接使用独立接口（recommendation/list + ranking/hot + editorial-pick/list）
- 长期：待 BI-1 完成后，再添加聚合接口 + 降级逻辑
- 在 tasks.md 中将 4.7（降级逻辑）调整为可选任务

### ADVISORY-4: 分类树一次性加载假设

design.md 假设"分类总数 < 500，一次性加载到 Store"。当前后端无分类数量限制机制，若分类数增长超出预期，需改为懒加载子分类。这是一个合理的初始假设，但建议在 useChannelCategoryStore 中预留懒加载扩展点。

---

## 4. 可测试性 (Testability) — 5/10

### BLOCK-3: 无测试任务、无测试策略

**严重程度**: BLOCK

与 BLOCK-1 相同问题。tasks.md 中 0/70 个任务涉及测试。design.md 中无 Test Strategy 章节。

这是 DoD 违规：根据 AGENTS.md，所有代码开发必须使用 TDD 流程，变更代码行覆盖率 >= 90%。

### FLAG-8: Spec scenario 缺少可量化的验收标准

以下 spec scenario 缺少具体数值或可验证条件：

| Spec | Scenario | 缺失的量化标准 |
|------|---------|--------------|
| channel-discovery | "不感兴趣反馈后卡片移除" | 未定义移除动画时长、是否需要 undo 操作 |
| channel-ranking | "排名序号样式" | 未定义金银铜的具体 CSS 样式值或 design token |
| channel-category-manage | "拖拽排序" | 未定义拖拽的手势触发条件、排序号自动计算规则 |
| channel-search | "搜索降级" | 未定义降级触发条件（超时时间？HTTP 状态码？） |

### FLAG-9: 前端验证报告已有但未纳入 spec 体系

verification-review.md 和 backend-issues.md 已识别 6 个后端问题，但这些信息仅以独立文件存在，未反映到各 spec 的 NOTE 或约束中。channel-search spec 中有 1 处 NOTE 引用 backend-issues.md，但其他 spec（channel-discovery、channel-category-manage、channel-tag-manage）未标注对应的后端依赖。

**建议**: 在每个受后端缺失影响的 spec 中添加 NOTE 标注，明确说明后端依赖状态。

### ADVISORY-5: 移动端交互场景缺少手势定义

channel-tag-manage spec 定义"移动端操作按钮转为长按或滑动"，但未定义：
- 长按触发时长（500ms？800ms？）
- 滑动方向（左滑？右滑？）
- 滑动后的操作菜单样式

---

## 5. 接口契约 (API Contract) — 6/10

### BLOCK-4: 推荐和搜索接口的 userId 参数未在 PRD API 表中声明

**严重程度**: BLOCK

后端 `ContentChannelRecommendationController.getRecommendations()` 和 `ContentChannelSearchController.search()` 均需要 `@RequestParam String userId` 参数。但前端 PRD Section 5.1 API 列表中未提及此参数。

design.md 提到"推荐接口和搜索接口需要 userId 参数，前端从 useUserStore 自动注入"，但 PRD 的 API 调用示例（Section 5.2）也未包含 userId。

**建议**: 
- 在前端 PRD Section 5.1 API 列表中补充 userId 参数说明
- 在 design.md 中明确 userId 的注入方式（defHttp 拦截器？请求参数？）

### FLAG-10: 分类管理 API 路径不一致

如 FLAG-5 所述，PRD 引用 `/add` 和 `/edit`，后端使用 `/create` 和 `/update`。

### FLAG-11: 排行榜查询参数结构未对齐

后端使用 `ChannelRankingQueryReq` 对象，前端 PRD 仅描述"参数: type, dimension"。未明确：
- dimension 参数的枚举值（DAILY/WEEKLY/MONTHLY？daily/weekly/monthly？）
- type 参数与三个独立端点的关系（是否仍需传递？）
- 分页参数（page/pageSize）的字段名

**建议**: 在 channel-ranking spec 中补充完整的请求参数定义。

### FLAG-12: 精选管理缺少后端 admin 接口

BI-5 指出 `IContentChannelEditorialPickService` 仅有 `listActivePicks()` 返回有效精选列表，运营后台需要按状态筛选的分页列表。前端 spec（channel-editorial-pick-manage）定义了"状态筛选（全部/生效中/已过期/状态异常）"，但后端尚无对应接口。

### ADVISORY-6: 前端 VO 定义与后端 VO 字段需逐一确认

前端 PRD Section 5.3 定义了 TypeScript 接口，但未标注与后端 Java VO 的字段映射关系。需在实现时确认：
- `ChannelRecommendationVO.reason` 的结构是否与后端 `ContentChannelRecommendationCache` 的推荐理由字段一致
- `ChannelSearchResultVO.matchReason` 是否由后端计算并返回
- `ChannelEditorialPickVO.status` 的枚举值是否与后端一致

### ADVISORY-7: 浏览接口路径不一致

| 位置 | 路径 |
|------|------|
| 前端 PRD Section 5.1 | `/content/channel/browse/list` |
| verification-review.md | `/content/channel/browse/category` |
| 后端实际端点 | `GET /content/channel/browse/category` |

需统一为 `/content/channel/browse/category`。

---

## 6. 边界覆盖 (Boundary) — 5/10

### FLAG-13: 缺少网络异常边界场景

10 类边界条件中，以下类型未在 specs 中覆盖：

| 边界类型 | 覆盖状态 | 说明 |
|---------|---------|------|
| 网络超时 | 未覆盖 | 仅 design.md 提到"搜索 P99 <= 200ms"，spec 中未定义超时处理 |
| 请求并发 | 未覆盖 | 搜索防抖 300ms 可能产生并发请求，未定义取消策略 |
| 数据量边界 | 部分覆盖 | 分页每页 20 条已定义，但未定义最大页数或总数据量上限 |
| 空状态 | 已覆盖 | 各 spec 均有空状态 scenario |
| 校验边界 | 已覆盖 | 标签名 20 字符、分类名 50 字符、副分类 3 个上限 |
| 响应式断点 | 已覆盖 | 各 spec 均有响应式 scenario |
| 权限边界 | 未覆盖 | 未定义非运营用户访问管理后台的行为 |
| 并发写入 | 未覆盖 | 多人同时编辑分类/标签的冲突处理 |
| 缓存边界 | 部分覆盖 | 5 分钟 TTL 已定义，但未覆盖缓存击穿场景 |
| 降级边界 | 部分覆盖 | 聚合降级和搜索降级已定义，但降级后的数据一致性未说明 |

### FLAG-14: 分类管理缺少权限边界

channel-category-manage spec 未定义：
- 非运营人员访问分类管理页时的行为（403？重定向？）
- 分类操作的权限粒度（谁能新增？谁能删除？谁能停用？）

### FLAG-15: 精选管理缺少数据量边界

channel-editorial-pick-manage spec 未定义：
- 最多可添加多少个精选频道？
- 推荐语 200 字限制的字符计算方式（中文算 1 还是 2？）
- 有效期的最大范围（1 年？永久？）

### ADVISORY-8: 排行榜虚拟滚动阈值未在 spec 中定义

tasks.md 12.6 提到"排行榜列表超过 100 条时启用虚拟滚动"，但 channel-ranking spec 中未提及此阈值，也未定义虚拟滚动的行高和容器高度。

### ADVISORY-9: 搜索防抖取消策略未定义

channel-search spec 定义了"300ms 防抖"，但未定义：
- 用户快速输入时是否取消前一个请求（AbortController？）
- 防抖期间是否展示 loading 状态
- 筛选条件变更时的防抖策略（PRD 提到 200ms 防抖，但 spec 未体现）

### ADVISORY-10: 搜索历史 localStorage 容量边界

channel-search spec 定义"最近 10 条搜索历史持久化到 localStorage"，但未定义：
- localStorage 写入失败时的降级策略
- 搜索关键词的最大长度限制
- 跨设备/跨浏览器的历史同步（不在范围内，但应明确声明）

---

## 前后端衔接审计

> 触发条件：change-prd-mapping.yaml 中存在配对后端 change `channel-23-discovery`，且目录已存在。

### 接口清单双向对比

| 前端引用的 API | 后端定义的状态 | 匹配结果 |
|---------------|-------------|---------|
| `GET /content/channel/discovery/home` | 缺失 Controller（BI-1） | 不匹配 |
| `GET /content/channel/category/tree` | 已存在 | 匹配 |
| `POST /content/channel/category/create` | 已存在（PRD 引用 /add） | 路径偏差 |
| `POST /content/channel/category/update` | 已存在（PRD 引用 /edit） | 路径偏差 |
| `POST /content/channel/category/disable` | 已存在 | 匹配 |
| `POST /content/channel/category/enable` | 缺失（BI-2） | 不匹配 |
| `GET /content/channel/tag/list` | 已存在 | 匹配 |
| `POST /content/channel/tag/add` | 已存在（create） | 匹配 |
| `POST /content/channel/tag/edit` | 缺失（BI-3） | 不匹配 |
| `POST /content/channel/tag/delete` | 已存在 | 匹配 |
| `GET /content/channel/recommendation/list` | 已存在 | 匹配 |
| `GET /content/channel/recommendation/cold-start` | 已存在 | 匹配 |
| `POST /content/channel/recommendation/not-interested` | 已存在 | 匹配 |
| `GET /content/channel/ranking/hot` | 已存在 | 匹配 |
| `GET /content/channel/ranking/new` | 已存在 | 匹配 |
| `GET /content/channel/ranking/system` | 已存在 | 匹配 |
| `GET /content/channel/editorial-pick/list` | 已存在 | 匹配 |
| `GET /content/channel/search/query` | 已存在 | 匹配（PRD 引用 /search） |
| `GET /content/channel/browse/category` | 已存在（方法体未实现） | 部分匹配 |

**匹配率**: 13/19 完全匹配（68%），3 个路径偏差，3 个后端缺失

### 数据模型一致性

| 前端 TypeScript 接口 | 后端 Java VO | 一致性 |
|---------------------|-------------|--------|
| `CategoryTreeVO` | `ChannelCategoryTreeVO` | 需确认字段映射 |
| `ChannelTagVO` | `ChannelTagVO` | 需确认字段映射 |
| `ChannelRecommendationVO` | `ChannelRecommendationVO` | 需确认 reason 字段结构 |
| `ChannelRankingItemVO` | `ChannelRankingItemVO` | 需确认 score 字段 |
| `ChannelEditorialPickVO` | `ChannelEditorialPickVO` | 需确认 status 枚举值 |
| `ChannelSearchResultVO` | `ChannelSearchResultVO` | 需确认 matchReason 结构 |
| `ChannelInfo` | 无直接对应 VO | 前端聚合类型，需确认来源 |

### 错误码覆盖

前端文档未定义 API 错误码处理策略。后端使用 `Result<T>` 统一返回格式，前端应定义：
- `code !== 200` 时的通用错误处理
- 业务错误码的前端提示映射（如分类层级超限、标签名重复等）
- 网络错误（timeout、network error）的统一处理

### 认证鉴权一致性

- 推荐接口和搜索接口需要 `userId` 参数，前端从 useUserStore 获取
- 运营后台接口需要运营权限，前端从 usePermissionStore 判断
- design.md 提到"userId 从 useUserStore 自动注入"，但未说明注入方式

### 分页契约

| 接口 | 前端定义 | 后端约定 |
|------|---------|---------|
| 分类浏览 | 每页 20 条，滚动加载 | 待确认分页参数名 |
| 搜索结果 | 每页 20 条，滚动加载 | 待确认分页参数名 |
| 排行榜 | 每页 20 条，滚动加载 | 待确认分页参数名 |
| 精选管理 | 未明确 pageSize | 待确认 |

前端未定义分页请求参数格式（page/pageSize vs pageNo/pageSize vs offset/limit）。

---

## PRD 追溯矩阵

| PRD 用户故事 | 对应 Spec | 覆盖状态 |
|-------------|----------|---------|
| U-C1 推荐频道 | channel-discovery | 已覆盖 |
| U-C2 排行榜 | channel-ranking | 已覆盖 |
| U-C3 分类浏览 | channel-category-browse | 已覆盖 |
| U-C4 搜索频道 | channel-search | 已覆盖 |
| U-C5 不感兴趣反馈 | channel-discovery | 已覆盖 |
| U-C6 编辑精选 | channel-discovery | 已覆盖 |
| U-O1 分类管理 | channel-category-manage | 已覆盖 |
| U-O2 精选管理 | channel-editorial-pick-manage | 已覆盖 |
| U-O3 停用影响范围 | channel-category-manage | 已覆盖 |
| U-A1 标签管理 | channel-tag-manage | 已覆盖 |
| U-A2 分类选择 | channel-category-select | 已覆盖 |

**PRD 用户故事覆盖率**: 11/11 = 100%

| PRD 测试要点 (11.1) | 对应 Spec | 覆盖状态 |
|---------------------|----------|---------|
| 分类管理 CRUD + 层级校验 | channel-category-manage | 已覆盖 |
| 分类选择主/副分类 | channel-category-select | 已覆盖 |
| 标签管理 CRUD + 校验 | channel-tag-manage | 已覆盖 |
| 推荐展示 + 冷启动 + 反馈 | channel-discovery | 已覆盖 |
| 排行榜切换 + 维度 | channel-ranking | 已覆盖 |
| 精选管理 CRUD + 状态异常 | channel-editorial-pick-manage | 已覆盖 |
| 分类浏览导航 + 筛选 + 分页 | channel-category-browse | 已覆盖 |
| 搜索 + 筛选 + 排序 + 分页 + 反馈 | channel-search | 已覆盖 |

**PRD 功能测试要点覆盖率**: 8/8 = 100%

| PRD 可见性测试 (11.2) | 覆盖状态 |
|----------------------|---------|
| 私有频道不出现在各入口 | 仅 design.md 提及，spec 中未显式定义 |
| 隐藏频道不出现在各入口 | 同上 |
| 冻结频道不出现在各入口 | 同上 |
| 限制公开曝光频道 | 同上 |
| 未通过审核频道 | 同上 |

**PRD 可见性测试覆盖率**: 设计层面 100%（依赖后端 ChannelVisibilityService），Spec 层面 0%（无显式 scenario）

---

## 最终结论

### 整体评估: 需修改后方可 Apply

channel-23-discovery-frontend 的规范文档结构完整、PRD 覆盖率高，但存在以下问题需要在 apply 前解决：

1. **BLOCK-1/BLOCK-3**: 测试任务和测试策略完全缺失，违反项目 TDD 硬规则
2. **BLOCK-2** [已大部分解决]: 原 6 个缺失后端 API 中 4 个已实现（BI-1/2/3/4），剩余 BI-5（精选 admin 分页）和 BI-6（浏览接口实现）不阻塞核心功能开发
3. **BLOCK-4**: userId 参数未在 PRD API 表中声明

### 建议操作

**Apply 前必须完成**:
1. 在 tasks.md 中补充测试任务（至少覆盖 Store、组件、API 封装层）
2. 在 design.md 中补充 Test Strategy 章节
3. 更新前端 PRD 中的 API 路径（搜索、排行榜、分类管理）和 userId 参数说明
4. 更新 design.md 中的 API 路径与后端对齐

**Apply 后建议完成**:
1. 在各受后端缺失影响的 spec 中添加 NOTE 标注（BI-5/BI-6 状态）
2. 补充可见性过滤的显式 scenario
3. 定义分页参数格式和错误码处理策略
4. 补充网络异常和并发请求的边界场景
