# 修复计划 — channel-23-discovery-frontend

**生成时间**: 2026-06-30
**审核文档数**: 5 (drift-report/review-report*2/verify-report/backend-issues)
**总问题数**: 16
**整体评估**: ✅ 整体质量高，主要是架构一致性问题，修复P0后可联调

---

## 修复项

### FE-001 - API文件位置错误，需移动到api/content/channel/目录并按领域拆分

**来源**: drift-report CRITICAL D-01/ARCH-API-01, verify-report C-01/C-04
**位置**: api/content/channelDiscovery.ts
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-前端（重构）

**修复步骤**:
1. 将api/content/channelDiscovery.ts移动到api/content/channel/目录下
2. 按领域拆分为多个文件（参考其他channel模块如publish.ts/review.ts/governance.ts的组织方式）：
   - discovery.ts：发现页聚合接口
   - category.ts：分类CRUD、分类树
   - search.ts：搜索、搜索建议、热词、搜索反馈
   - ranking.ts：各类榜单
   - editorialPick.ts：精选CRUD
   - tag.ts：标签CRUD
   - recommendation.ts：推荐、不感兴趣
3. 每个文件独立定义Api enum和导出API方法
4. 更新所有import路径（Store、组件中的引用）
5. 创建index.ts统一导出（可选，按需）
6. 参考EPIC-22的api/content/channel/目录结构保持一致

**验证方式**:
- 所有discovery相关API在api/content/channel/目录下
- 单文件不超过300行，职责清晰
- 所有import路径更新正确
- 编译无错误
- 功能与拆分前一致
- 单元测试更新后通过

**状态**: pending

---

### FE-002 - 分页参数page/pageSize与JeecgBoot默认current/size不兼容

**来源**: drift-report CRITICAL D-02/ARCH-API-02, verify-report C-03, review-report 跨端一致性检查
**位置**: API封装、Store、组件
**优先级**: BLOCK
**依赖**: FE-001（API拆分时一并修改）
**类型**: 代码修复-前端

**修复步骤**:
1. 统一所有分页参数名为current和size（JeecgBoot项目标准）：
   - page → current
   - pageSize → size
2. 修改位置：
   - API函数参数定义
   - Store中的分页状态变量
   - 组件中传递分页参数的地方
   - 表格组件绑定的分页参数（Ant Design Table pagination配置）
3. 后端响应分页字段也确认使用current/size/records/total等标准字段
4. 全局搜索page和pageSize确保无遗漏
5. 参考governance.ts等已有API文件的分页参数写法保持一致

**验证方式**:
- 所有分页请求参数名是current和size
- 分页查询返回正确页码数据
- 翻页、切换页大小功能正常
- 与后端参数一致无字段名错误
- 单元测试覆盖分页参数场景

**状态**: pending

---

### FE-003 - 后端BI-1/BI-2/BI-3三个P0 API缺失，阻塞前端核心功能

**来源**: review-report BLOCK B-01/B-02/B-03, backend-issues BI-1/BI-2/BI-3
**位置**: API/Store/组件
**优先级**: BLOCK
**依赖**: BE-006（后端实现后）
**类型**: 阻塞（等待后端）

**修复步骤**:
1. 列出三个P0阻塞API：
   - BI-1: GET /api/v1/content/channels/discovery/home 发现页聚合接口
   - BI-2: POST /api/v1/content/channels/categories/{id}/enable 分类启用接口
   - BI-3: POST /api/v1/content/channels/{channelId}/tags/update 标签编辑接口
2. 待后端实现BE-006后：
   - 验证API路径与前端调用路径一致
   - 联调发现页聚合数据正常返回
   - 分类启用/停用功能正常
   - 标签编辑重命名功能正常
3. 前端已有降级逻辑：
   - 发现页聚合失败时fallback调用独立接口（hotRanking、editorialPicks等）
   - 降级期间功能可用但性能稍差
4. BI-4/BI-5/BI-6非P0阻塞：
   - BI-4搜索反馈：可暂时不实现或不调用
   - BI-5精选分页：先用listActivePicks全量加载+前端分页
   - BI-6分类浏览：后端方法体为空时返回空数据，等后端实现

**验证方式**:
- 后端完成三个P0 API后联调通过
- 发现页展示推荐、热门、精选、分类
- 分类停用后可重新启用
- 标签可编辑保存
- 降级逻辑正常工作（后端异常时不白屏）

**状态**: pending（等待后端）

---

### FE-004 - userId自动注入方案不明确，设计与实现不一致

**来源**: drift-report CRITICAL D-03, review-report BLOCK B-04, verify-report W-06
**位置**: API封装层、Store
**优先级**: BLOCK
**依赖**: 无
**类型**: 代码修复-前端+文档

**修复步骤**:
1. 明确userId注入方案并统一：
   - 方案A：API层自动注入（从useUserStore获取userId自动添加到params）
   - 方案B：调用方（Store）显式传入userId
2. 推荐方案B（更清晰可控）：
   - 理由：个性化接口（推荐、不感兴趣）需要userId，公开接口（分类树、热榜、公开搜索）不需要
   - Store在调用需要个性化的API时，从useUserStore获取userId传入
   - API层不做隐式注入，避免未登录时问题
3. 更新design.md文档说明userId传递方式：
   - 需要用户身份的API（推荐、不感兴趣、搜索反馈）显式传userId
   - 公开浏览API不需要userId
   - userId从用户Store获取，未登录时相关功能降级（返回非个性化结果）
4. 检查所有需要userId的API调用确保正确传入：
   - getRecommendationList
   - markNotInterested
   - searchFeedback
5. 未登录场景处理：
   - 不显示"不感兴趣"按钮
   - 推荐返回热门/精选（非个性化）
   - 不记录搜索反馈

**验证方式**:
- 文档明确userId传递规则
- 登录用户推荐个性化
- 未登录用户推荐展示热门+精选
- 所有需要userId的调用正确传入
- 无undefined的userId参数请求
- 单元测试覆盖登录/未登录场景

**状态**: pending

---

### FE-005 - model类型文件路径错误，需统一到channel/model/目录

**来源**: drift-report D-05/ARCH-API-03, verify-report C-02
**位置**: store/modules/channelDiscovery.ts:20
**优先级**: FLAG
**依赖**: FE-001
**类型**: 代码修复-前端

**修复步骤**:
1. 检查model文件实际位置：api/content/model/channelDiscoveryModel.ts
2. 统一类型文件路径到api/content/channel/model/目录（与其他channel模块一致）
3. 创建channel/model目录
4. 移动或拆分类型文件：
   - channel/model/discoveryModel.ts
   - channel/model/categoryModel.ts
   - channel/model/searchModel.ts
   - 等
5. 更新所有import路径
6. 检查类型定义与后端VO字段对齐
7. 类型文件与API文件一一对应，单文件类型不重复定义

**验证方式**:
- 类型文件在api/content/channel/model/目录下
- 所有import路径正确
- 编译无类型错误
- 类型定义与后端VO字段一致
- 与其他channel模块类型组织一致

**状态**: pending

---

### FE-006 - Store缺少useXxxStoreWithOut函数，组件外使用不便

**来源**: drift-report ARCH-STORE-01
**位置**: 三个Store（channelDiscovery/channelCategory/channelSearch）
**优先级**: FLAG
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 为三个Store添加WithOut后缀函数（参考项目其他Store写法）：
   ```ts
   export function useChannelDiscoveryStoreWithOut() {
     return useChannelDiscoveryStore(pinia)
   }
   ```
2. 在组件外（如路由守卫、API拦截器）可通过WithOut函数获取Store实例
3. 确保pinia实例正确传递
4. 参考channelPublishStore等已有Store的写法保持一致
5. 统一导出方式

**验证方式**:
- 三个Store都有对应的WithOut函数
- 组件外调用不报错
- 与项目其他Store写法一致
- 单元测试中可正常使用

**状态**: pending

---

### FE-007 - 组件目录路径不一致（views/channel/ vs views/content/channel/）

**来源**: drift-report ARCH-COMP-01, verify-report 组件验证
**位置**: views/目录
**优先级**: FLAG
**依赖**: 无（先确认正确路径）
**类型**: 代码修复-前端（需确认）

**修复步骤**:
1. 确认项目正确的channel组件根目录：
   - 检查EPIC-20/21/22组件实际放在哪个目录
   - 是views/content/channel/还是views/channel/
2. 统一所有channel相关组件和页面到正确目录
3. 如果正确目录是views/content/channel/：
   - channel-22等模块如有在views/channel/的也移动过来
4. 如果正确目录是views/channel/：
   - 将channel-23的组件从views/content/channel/移动到views/channel/
5. 更新所有import路径和路由配置
6. 更新design.md中的目录结构说明
7. 注意：不要随意移动用户已有未提交的文件，如果channel-22的文件是用户已存在的，以用户实际目录为准

**建议**: 以用户现有文件位置为准，检查EPIC-20/21已有组件实际在哪，统一到该路径。

**验证方式**:
- 所有channel组件在同一根目录下
- import路径正确
- 路由指向正确组件路径
- 页面可正常访问无404
- 无重复组件在两个目录下

**状态**: pending

---

### FE-008 - 列表操作按钮缺少重复提交防护

**来源**: drift-report 边界场景审核, review-report FLAG-06
**位置**: 所有操作按钮（不感兴趣、订阅、收藏、关注等）
**优先级**: FLAG
**依赖**: FE-003（Store loading状态完善）
**类型**: 代码修复-前端

**修复步骤**:
1. 除了表单提交按钮，列表项操作按钮也需添加防重复点击：
   - 不感兴趣按钮
   - 订阅/取消订阅频道
   - 收藏/取消收藏
   - 关注/取消关注
   - 分类折叠/展开（可选，非数据操作）
2. 实现方式：
   - 为每个操作维护独立的loading状态（如actioningChannelId）
   - 操作中禁用该按钮，显示小loading
   - 或使用useLockFn钩子包装操作函数
   - 操作完成（成功/失败）后解除锁定
3. 参考项目中useLockFn的实现
4. 批量操作时整个列表禁用或每个项独立loading
5. 防止快速多次点击触发重复API请求

**验证方式**:
- 快速多次点击列表操作按钮只发送一次请求
- 操作中按钮有loading反馈
- 操作完成后按钮可再次点击
- 无重复数据产生（如重复添加不感兴趣记录）
- 用户体验流畅

**状态**: pending

---

### FE-009 - 缓存有效性判断逻辑不完善，部分数据为空时不刷新

**来源**: verify-report C-06
**位置**: store/modules/channelDiscovery.ts:51
**优先级**: FLAG
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 改进缓存有效判断条件：
   - 当前逻辑：recommendations或hotRanking有数据即认为缓存有效
   - 问题：editorialPicks、categories可能为空但不刷新
2. 修改hasData判断逻辑，检查所有关键数据是否存在：
   ```ts
   const hasAllData = computed(() => {
     return recommendations.value.length > 0
       && hotRanking.value.length > 0
       && editorialPicks.value.length > 0
       && categories.value.length > 0
   })
   ```
3. 缓存判断改为：
   ```ts
   !forceRefresh && isCacheValid.value && hasAllData.value
   ```
4. 或者更精细：哪个数据缺失拉哪个，不必全量刷新（更优）
5. 添加缓存时间戳判断，即使数据存在但超5分钟也刷新
6. forceRefresh=true时强制从服务器拉取最新数据

**验证方式**:
- 首次加载正确拉取所有数据
- 部分数据缺失时自动补拉
- 5分钟TTL过期自动刷新
- forceRefresh可强制刷新
- 单元测试覆盖缓存判断逻辑

**状态**: pending

---

### FE-010 - 验证TagManage和CategorySelect组件是否存在并集成

**来源**: verify-report W-03/W-04
**位置**: 组件目录、频道设置页
**优先级**: FLAG
**依赖**: 无
**类型**: 代码修复-前端（验证+补充）

**修复步骤**:
1. 检查是否存在TagManage组件（标签管理页）：
   - 路径：views/content/channel/tag-manage/index.vue
   - 如果不存在，创建标签管理页面
   - 功能：标签列表、新增、编辑、删除、按频道筛选
2. 检查是否存在CategorySelect组件（分类选择器）：
   - 路径：views/content/channel/components/CategorySelect.vue
   - 用于频道创建/编辑时选择分类
   - 功能：级联选择、搜索、树形展示
3. 将标签管理页集成到频道设置/管理后台路由
4. 将分类选择器集成到频道创建/编辑表单
5. 检查TagManage和CategorySelect的specs内容完整性，补充交互说明
6. 参考成员管理、公告管理页面结构实现

**验证方式**:
- 标签管理页可访问，功能正常
- 分类选择器可正常选择分类
- 标签CRUD功能完整
- 组件集成到对应页面
- specs文档完整

**状态**: pending

---

### FE-011 - Store代码风格Composition API vs Options API不统一

**来源**: drift-report D-04/ARCH-STORE-02, verify-report S-01/W-09
**位置**: store/modules/
**优先级**: ADVISORY
**依赖**: 无
**类型**: 代码风格（可选统一）

**修复步骤**:
1. 决策：保持现状还是统一风格
   - 方案A：保持channelDiscovery用Composition API，其他用Options API（允许混合）
   - 方案B：统一所有Channel Store为Options API（与EPIC-20/21/22一致）
   - 方案C：统一所有新Store用Composition API，旧Store逐步迁移
2. 推荐方案B（本期统一）：
   - 将channelDiscovery/channelCategory/channelSearch改为Options API风格
   - 与其他channel module Store保持一致
   - 降低维护成本和团队理解成本
3. 如果不统一，在文档中说明两种风格都可接受，新代码推荐Composition API
4. 无论选哪种，确保代码可读性和功能正确

**验证方式**:
- 所有Channel Store代码风格一致（如果选择统一）
- 功能与重构前一致
- 所有测试通过
- 团队无风格理解成本

**状态**: pending（团队决策）

---

### FE-012 - 路由文件命名不一致（contentDiscovery.ts vs channel-*.ts）

**来源**: drift-report D-06, review-report A-04, verify-report S-02
**位置**: router/routes/modules/contentDiscovery.ts
**优先级**: ADVISORY
**依赖**: 无
**类型**: 代码修复-前端（可选）

**修复步骤**:
1. 考虑重命名路由文件：
   - 从contentDiscovery.ts改为channel-discovery.ts
   - 或合并到channel.ts统一路由模块
2. 参考其他路由文件命名风格
3. 如果其他模块都按功能命名（如channel.ts、content.ts），合并或保持一致
4. 重命名时更新路由注册引用
5. 此问题不影响功能，只是命名一致性问题

**验证方式**:
- 路由文件命名与项目其他模块一致
- 路由正确注册
- 页面访问正常
- 无导入错误

**状态**: pending（可选）

---

### FE-013 - CACHE_TTL硬编码，建议提取为常量并说明可配置性

**来源**: review-report A-03
**位置**: Store/API中的缓存时间配置
**优先级**: ADVISORY
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 提取缓存TTL常量：
   ```ts
   const CACHE_TTL = 5 * 60 * 1000; // 5分钟，可根据环境调整
   ```
2. 添加注释说明为什么是5分钟：
   - 发现页数据不需要强一致性
   - 5分钟平衡性能和实时性
   - 运营修改分类/精选后最长5分钟用户可见
3. 如果需要可配置，从环境变量读取：
   ```ts
   const CACHE_TTL = import.meta.env.VITE_DISCOVERY_CACHE_TTL || 5 * 60 * 1000;
   ```
4. 所有缓存时间使用同一常量，不要散落魔法数字
5. 不同数据可设置不同TTL：
   - 分类树：1小时（变更少）
   - 榜单：1小时（每日更新）
   - 推荐：5分钟
   - 精选：5分钟

**验证方式**:
- 无魔法数字5*60*1000散落各处
- 常量命名有意义
- 注释说明TTL选择理由
- 修改一处即可调整所有缓存时间（或按数据类型分别配置）

**状态**: pending

---

### FE-014 - 搜索历史持久化需明确条数上限和清除策略

**来源**: review-report A-02
**位置**: 搜索相关Store/composable
**优先级**: ADVISORY
**依赖**: 无
**类型**: 文档修复+实现补充

**修复步骤**:
1. 明确搜索历史规则并实现：
   - 最多保存最近10条搜索记录
   - 新搜索置顶，重复搜索不重复记录（去重）
   - 持久化到localStorage
   - 提供"清除历史"功能
   - 超过10条时删除最旧的
2. localStorage key命名规范：
   - `channel-search-history` 或带用户id区分
3. 未登录用户和登录用户是否共享历史？
   - 建议：按浏览器存储，不区分用户（简化）
4. 在specs或design.md中明确说明
5. 实现去重、截断、过期逻辑（可选过期，如30天前的自动清除）

**验证方式**:
- 搜索历史最多10条
- 重复搜索不产生重复记录
- 新搜索置顶
- 一键清除功能正常
- 页面刷新历史不丢失
- 文档有明确说明

**状态**: pending

---

### FE-015 - 验证并修复1个超时测试

**来源**: verify-report W-05
**位置**: 测试文件
**优先级**: FLAG
**依赖**: 无
**类型**: 代码修复-前端（测试）

**修复步骤**:
1. 运行测试套件定位哪个测试超时：
   ```bash
   npm run test -- --reporter verbose
   ```
2. 分析超时原因：
   - 是否是异步测试未正确等待
   - 是否是Mock定时器问题
   - 是否是网络请求未Mock导致真实请求超时
3. 修复超时测试：
   - 添加正确的async/await
   - 使用vi.useFakeTimers()处理定时器
   - 确保所有API调用都被Mock
   - 或增加超时阈值（如果是合理的慢测试）
4. 目标：109/109测试全部通过
5. 验证测试覆盖率：
   - 运行覆盖率命令确认≥90%
   - 确认是分支覆盖率还是行覆盖率，补充缺口

**验证方式**:
- npm run test全部通过，无超时
- 无跳过的测试（skip/only）
- 覆盖率达标（Store≥90%，组件≥70%）
- 测试包含真实断言不是空测试

**状态**: pending

---

### FE-016 - 确认前端配对change关系并补充依赖说明

**来源**: review-report ADV-02
**位置**: openspec/changes/
**优先级**: ADVISORY
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 确认channel-23-discovery-frontend确实是channel-23-discovery的前端配对change
2. 如果存在circle-12/13等其他change，明确对应关系
3. 在两个change的proposal.md或design.md中互相引用：
   - 后端change说明前端配对是channel-23-discovery-frontend
   - 前端change说明后端配对是channel-23-discovery
4. backend-issues.md已记录后端缺失API，确认是双向同步的
5. 在跨端依赖文档中记录前后端API依赖关系

**验证方式**:
- 前后端配对关系明确
- 双向引用
- 联调时无歧义哪个后端change对应哪个前端change

**状态**: pending
