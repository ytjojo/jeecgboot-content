# 后端遗留代码问题

本文档记录前端开发所依赖但后端尚未实现的 API 端点和数据结构问题。前端开发前需确认后端已完成以下补充。

---

## 问题 1: 隐私设置 GET 端点缺失（高优先级）

**现状**: `ContentUserSettingsController` 中仅有 `POST /content/user/settings/privacy/update`，无独立的 GET 查询端点。

**前端需求**: 隐私设置页面加载时需调用 `GET /content/user/settings/privacy` 获取当前用户的隐私配置。

**建议实现**:

```java
// ContentUserSettingsController.java 中补充
@Operation(summary = "查询隐私设置")
@GetMapping("/privacy")
public Result<ContentUserPrivacySetting> getPrivacy(@RequestParam("userId") String userId) {
    return Result.OK(profileService.getPrivacySetting(userId));
}
```

**涉及文件**:
- `ContentUserSettingsController.java` - 添加 GET 端点
- `IContentUserProfileService` / `ContentUserProfileServiceImpl` - 可能需要补充 `getPrivacySetting(userId)` 方法（当前 `defaultPrivacy()` 方法为 private）

**返回数据结构**: `ContentUserPrivacySetting` 实体，包含以下字段:
- `browseHistoryVisibility` - 浏览记录可见范围
- `likeActivityVisibility` - 点赞动态可见范围
- `favoriteVisibility` - 收藏夹可见范围
- `onlineStatusVisibility` - 在线状态可见性
- `allowSearchEngineIndex` - 是否允许搜索引擎索引
- 及其他字段（详见实体类）

---

## 问题 2: 安全设置更新端点缺失（高优先级）

**现状**: `ContentUserSettingsController` 中仅有 `GET /content/user/settings/security`，无 POST 更新端点。

**前端需求**: 账户安全页面的登录提醒 Switch 开关需要调用接口保存状态。

**建议实现**:

```java
// ContentUserSettingsController.java 中补充
@Operation(summary = "更新安全设置")
@PostMapping("/security/update")
public Result<String> updateSecuritySetting(@RequestParam("userId") String userId,
                                             @RequestBody ContentUserSecurityUpdateReq req) {
    securitySettingService.updateSetting(userId, req);
    return Result.OK("更新成功");
}
```

**需新增**:
- `ContentUserSecurityUpdateReq` 请求类（包含 `loginAlertEnabled` 字段）
- `IContentUserSecuritySettingService.updateSetting()` 方法实现

**涉及文件**:
- `ContentUserSettingsController.java` - 添加 POST 端点
- `ContentUserSecurityUpdateReq.java` - 新建请求类
- `IContentUserSecuritySettingService` / `ContentUserSecuritySettingServiceImpl` - 补充 update 方法

---

## 问题 3: 订阅更新渠道字段缺失（中优先级）

**现状**: `ContentNotificationChannelConfigVO` 包含 6 个渠道字段:
- `likeChannels`
- `commentChannels`
- `followChannels`
- `favoriteChannels`
- `mentionChannels`
- `privateMessageChannels`

**缺失**: `subscriptionChannels` 字段（对应第 7 类通知"订阅更新"）。

**前端需求**: PRD 定义 7 类通知，订阅更新也需要渠道配置。

**建议修改**: 在 `ContentNotificationChannelConfigVO` 中补充:

```java
@Schema(description = "订阅更新通知渠道")
private List<String> subscriptionChannels;
```

同时在 `ContentUserNotificationSettingServiceImpl` 的序列化/反序列化逻辑中补充该字段的处理。

**涉及文件**:
- `ContentNotificationChannelConfigVO.java` - 添加字段
- `ContentUserNotificationSettingServiceImpl.java` - 更新 JSON 序列化逻辑

---

## 问题 4: 安全设置 null 默认值说明（低优先级）

**现状**: `ContentUserSecuritySettingVO` 的 getter 方法已处理 null 默认值:
- `deviceManagementEnabled` → null 时返回 `true`
- `passwordChangeEnabled` → null 时返回 `true`
- `twoFactorEnabled` → null 时返回 `false`
- `loginAlertEnabled` → null 时返回 `true`

**说明**: 此项无需修改，仅作前端开发参考。前端收到的 Boolean 值不会为 null。

---

## 优先级排序

| 优先级 | 问题 | 阻塞程度 | 预计工作量 |
|--------|------|----------|-----------|
| P0 | 隐私设置 GET 端点 | 阻塞隐私设置页面数据加载 | 小（复用已有 service 方法） |
| P0 | 安全设置更新端点 | 阻塞登录提醒开关保存 | 小（新增 req + service 方法） |
| P1 | 订阅更新渠道字段 | 影响第 7 类通知渠道配置 | 小（VO 加字段 + JSON 序列化） |
