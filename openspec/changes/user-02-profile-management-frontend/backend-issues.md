# 后端遗留问题清单

> 基于前端 change `user-02-profile-management-frontend` 与后端 `ContentUserProfileController` 的对比分析
> 生成时间: 2026-06-04

## 一、缺失的后端 API 端点清单

经验证，后端 `ContentUserProfileController` 已提供前端所需的全部 10 个可用端点（不含后台审核端点），**无缺失端点**。

## 二、API 路径偏差对照表

| 前端文档路径 | 实际后端路径 | HTTP 方法 | 偏差类型 |
|-------------|-------------|-----------|---------|
| `/api/v1/content/user/profile/detail` | `/api/v1/content/user/profile/detail` | GET | 无偏差 |
| `/api/v1/content/user/profile/update` | `/api/v1/content/user/profile/update` | POST | 无偏差 |
| `/api/v1/content/user/profile/review/handle` | `/api/v1/content/user/profile/review/handle` | POST | 无偏差（前端不对接） |
| `/api/v1/content/user/profile/privacy/update` | `/api/v1/content/user/profile/privacy/update` | POST | 无偏差 |
| `/api/v1/content/user/profile/homepage/update` | `/api/v1/content/user/profile/homepage/update` | POST | 无偏差 |
| `/api/v1/content/user/profile/homepage/defaults/restore` | `/api/v1/content/user/profile/homepage/defaults/restore` | POST | 无偏差 |
| `/api/v1/content/user/profile/homepage/modules` | `/api/v1/content/user/profile/homepage/modules` | GET | 无偏差 |
| `/api/v1/content/user/profile/badge/list` | `/api/v1/content/user/profile/badge/list` | GET | 无偏差 |
| `/api/v1/content/user/profile/badge/detail` | `/api/v1/content/user/profile/badge/detail` | GET | 无偏差 |
| `/api/v1/content/user/profile/history/list` | `/api/v1/content/user/profile/history/list` | GET | 无偏差 |
| `/api/v1/content/user/profile/history/restore` | `/api/v1/content/user/profile/history/restore` | POST | 无偏差 |

> **结论**：所有 API 路径完全一致，无偏差。

## 三、数据结构问题

### 3.1 返回类型不一致（已在 design.md 中修正）

| 端点 | 文档原声称返回类型 | 实际返回类型 | 影响 |
|------|------------------|-------------|------|
| `POST /profile/update` | `ContentUserProfileVO` | `Result<String>` | 前端保存后需重新 GET 获取最新数据 |
| `POST /privacy/update` | `ContentUserPrivacySettingVO` | `Result<String>` | 同上，且 `ContentUserPrivacySettingVO` 类型不存在 |
| `POST /homepage/update` | `Result<ContentUserProfileVO>` | `Result<String>` | 同上 |
| `POST /homepage/defaults/restore` | `Result<ContentUserProfileVO>` | `Result<String>` | 同上 |
| `POST /history/restore` | `Result<ContentUserProfileVO>` | `Result<String>` | 同上 |

### 3.2 不存在的类型

| 类型名 | 引用位置 | 状态 |
|--------|---------|------|
| `ContentUserPrivacySettingVO` | design.md API 对接矩阵 | 代码库中不存在，已从文档中删除引用 |

### 3.3 端点数量差异

| 描述 | 数量 | 说明 |
|------|------|------|
| 文档原声称 | 12 个 | 含 `review/handle` |
| 实际 Controller | 11 个 | 含 `review/handle` |
| 前端实际可用 | 10 个 | 排除 `review/handle`（后台审核） |

> 已在 design.md 和 proposal.md 中修正为"11 个端点"。

## 四、优先级排列

### P0 - 阻塞前端开发（已修复）

| # | 问题 | 状态 | 修复方式 |
|---|------|------|---------|
| 1 | 5 个 POST 端点返回类型文档错误 | **已修复** | 更新 design.md API 对接矩阵 |
| 2 | `ContentUserPrivacySettingVO` 类型不存在 | **已修复** | 从文档中删除引用 |
| 3 | 前端保存后数据刷新策略未明确 | **已修复** | 在 specs 中明确保存后重新调用 GET /detail |

### P1 - 影响开发准确性（已修复）

| # | 问题 | 状态 | 修复方式 |
|---|------|------|---------|
| 4 | 端点数量描述不准确 | **已修复** | 统一为"11 个端点" |
| 5 | GET 端点省略 Result 包装层说明 | **已修复** | 在 API 对接矩阵增加全局说明 |

### P2 - 文档质量改进（建议）

| # | 问题 | 状态 | 建议 |
|---|------|------|------|
| 6 | specs 中缺少网络异常等边界场景 | 待补充 | 各 spec 增加网络超时、权限不足等 Scenario |
| 7 | design.md 缺少错误码对照说明 | 待补充 | 增加后端业务错误码与前端提示的映射表 |

## 五、实施建议

1. **前端 API 封装层**：所有 POST 端点返回类型应声明为 `Promise<string>`（非 VO 类型），避免前端解析错误
2. **数据刷新策略**：所有写入操作成功后，统一调用 `GET /api/v1/content/user/profile/detail` 刷新本地缓存
3. **错误处理**：后端返回的 `Result<String>` 中 `success=false` 时，前端应展示 `message` 字段内容
4. **隐私设置特殊处理**：`onlineStatusVisibility` 字段使用 `PUBLIC|HIDDEN|MUTUAL_ONLY` 三值枚举，与其他 14 个 visibility 字段的四值枚举不同，前端 Select 组件需动态切换选项
