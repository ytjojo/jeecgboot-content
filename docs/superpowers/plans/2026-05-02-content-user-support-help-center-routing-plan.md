# Content User Support Help Center Routing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让 `GET /content/user/support/help-center` 按用户画像返回结构化帮助中心分类，并输出与 `customer-service` 一致的分类级客服推荐摘要。

**Architecture:** 复用现有 `ContentUserSupportServiceImpl` 中的用户分层语义，不引入数据库与 admin 改造；通过新增帮助中心条目 VO、调整 controller/service 签名、补齐 service/controller 测试完成最小闭环。

**Tech Stack:** Spring Boot 3, JeecgBoot Result, MyBatis-Plus, JUnit 5, Mockito, MockMvc

---

### Task 1: 先写帮助中心服务层失败用例

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`

- [ ] 新增默认用户、高等级用户、治理异常用户、release notes 空推荐字段四组断言。
- [ ] 运行 `ContentUserSupportServiceTest`，确认新断言先失败，失败点集中在 `getHelpCenter()` 旧签名和旧返回结构。

### Task 2: 实现结构化帮助中心返回

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserSupportService.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportController.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentHelpCenterVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentHelpCenterEntryVO.java`

- [ ] 将 `getHelpCenter()` 改为 `getHelpCenter(String userId)`，controller 增加必填 `userId`。
- [ ] 将 `ContentHelpCenterVO` 的三个列表改为 `List<ContentHelpCenterEntryVO>`。
- [ ] 在 service 中复用现有用户路由优先级，按分类映射输出 `recommendedRouteType`、`recommendedRouteTitle`、`manualSupported`。
- [ ] 保持 `releaseNotes` 只承载内容展示，三项推荐字段显式为 `null`。

### Task 3: 写控制器失败用例并补齐协议校验

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportControllerWebMvcTest.java`

- [ ] 增加 `help-center` 缺少 `userId` 返回 400 的用例。
- [ ] 增加结构化 JSON 返回断言，覆盖 `recommendedRouteType`、`recommendedRouteTitle`、`manualSupported`。
- [ ] 运行 `ContentUserSupportControllerWebMvcTest`，确认协议断言与字段断言全部通过。

### Task 4: 聚焦回归与规范检查

**Files:**
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportControllerWebMvcTest.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java`

- [ ] 运行三组聚焦测试，确认支持域用户侧和 admin 侧回归通过。
- [ ] 运行 `python3 .trae/skills/ai-coding-java-springboot/scripts/standards-check.py --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user --warn-only`。
- [ ] 检查改动文件诊断，修复新增问题后再交付。
