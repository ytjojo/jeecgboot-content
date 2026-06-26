API 路径合规性修复计划

 Context

 AGENTS.md 规定：内容社区 API 路径统一使用 /api/v1/content/ 前缀。user-10 已修复 auth 模块（26 个后端端点 + 13 个前端路径）。但 channel、circle、user、userstatus 四个模块仍不合规。

 目标路径格式: /api/v1/content/{module}/{resource}

 ---
 现状汇总

 ┌────────────┬────────────────────┬─────────────────────────────────────────┬─────────────────┬──────────────────────────┐
 │    模块    │ 后端 Controller 数 │                 已合规                  │   前端文件数    │          已合规          │
 ├────────────┼────────────────────┼─────────────────────────────────────────┼─────────────────┼──────────────────────────┤
 │ channel    │ 24                 │ 5 (export/lifecycle/merge/review/stats) │ 1 (index.ts)    │ 0                        │
 ├────────────┼────────────────────┼─────────────────────────────────────────┼─────────────────┼──────────────────────────┤
 │ circle     │ 13                 │ 0                                       │ 0 (无前端API层) │ N/A                      │
 ├────────────┼────────────────────┼─────────────────────────────────────────┼─────────────────┼──────────────────────────┤
 │ user       │ 19                 │ 0                                       │ 14              │ 0 (除 account/ 下已合规) │
 ├────────────┼────────────────────┼─────────────────────────────────────────┼─────────────────┼──────────────────────────┤
 │ userstatus │ 1                  │ 0                                       │ 1               │ 0                        │
 └────────────┴────────────────────┴─────────────────────────────────────────┴─────────────────┴──────────────────────────┘

 总计: 57 个后端 Controller + 16 个前端文件需修复

 ---
 修复方案

 规则

 后端: 只修改 class-level @RequestMapping 注解值，不改动方法签名和业务逻辑
 前端: 只修改 API 路径字符串，不改动函数签名

 Phase 1: Channel 模块后端 (19 个 Controller)

 1a. 路径映射表

 ┌────────────────────────────────────────┬─────────────────────────────────┬────────────────────────────────────────┐
 │               Controller               │        当前 class-level         │            目标 class-level            │
 ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
 │ ChannelController                      │ /api/v1/channels                │ /api/v1/content/channels               │
 ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
 │ ChannelAdminController                 │ /api/v1/admin/channels          │ /api/v1/content/admin/channels         │
 ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
 │ ChannelAnnouncementController          │ /content/channel/announcement   │ /api/v1/content/channel/announcement   │
 ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
 │ ChannelContentGovernanceController     │ /content/channel/governance     │ /api/v1/content/channel/governance     │
 ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
 │ ChannelContentReviewController         │ /content/channel/review         │ /api/v1/content/channel/review         │
 ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
 │ ChannelGovernanceController            │ /channel/governance             │ /api/v1/content/channel/governance     │
 ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
 │ ChannelInviteController                │ /channel/invite                 │ /api/v1/content/channel/invite         │
 ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
 │ ChannelMemberController                │ /channel/member                 │ /api/v1/content/channel/member         │
 ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
 │ ChannelPublishController               │ /content/channel/publish        │ /api/v1/content/channel/publish        │
 ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
 │ ChannelSubscriptionController          │ /channel/subscription           │ /api/v1/content/channel/subscription   │
 ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
 │ ContentChannelBrowseController         │ /content/channel/browse         │ /api/v1/content/channel/browse         │
 ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
 │ ContentChannelCategoryController       │ /content/channel/category       │ /api/v1/content/channel/category       │
 ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
 │ ContentChannelDiscoveryController      │ /content/channel/discovery      │ /api/v1/content/channel/discovery      │
 ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
 │ ContentChannelEditorialPickController  │ /content/channel/editorial-pick │ /api/v1/content/channel/editorial-pick │
 ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
 │ ContentChannelRankingController        │ /content/channel/ranking        │ /api/v1/content/channel/ranking        │
 ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
 │ ContentChannelRecommendationController │ /content/channel/recommendation │ /api/v1/content/channel/recommendation │
 ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
 │ ContentChannelSearchController         │ /content/channel/search         │ /api/v1/content/channel/search         │
 ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
 │ ContentChannelTagController            │ /content/channel/tag            │ /api/v1/content/channel/tag            │
 └────────────────────────────────────────┴─────────────────────────────────┴────────────────────────────────────────┘

 无冲突: ChannelGovernanceController (remove/mute/unmute/blacklist/log) 和 ChannelContentGovernanceController (POST /, content/list, recycle-bin/list) 方法路径无重叠，可共用 class-level 路径。

 1b. 已合规 (不修改)

 - ChannelExportController (/api/v1/content/channel/export)
 - ChannelLifecycleController (/api/v1/content/channel/lifecycle)
 - ChannelMergeController (/api/v1/content/channel/merge)
 - ChannelReviewController (/api/v1/content/channel/review)
 - ChannelStatsController (/api/v1/content/channel/stats)

 Phase 2: Channel 模块前端

 修改 jeecgboot-vue3/src/api/content/channel/index.ts:

 ┌──────────────────────────────────────────────────┬───────────────────────────────────────────────────┐
 │                     当前路径                     │                     目标路径                      │
 ├──────────────────────────────────────────────────┼───────────────────────────────────────────────────┤
 │ /api/v1/channels/create                          │ /api/v1/content/channels/create                   │
 ├──────────────────────────────────────────────────┼───────────────────────────────────────────────────┤
 │ /api/v1/channels/{id}                            │ /api/v1/content/channels/{id}                     │
 ├──────────────────────────────────────────────────┼───────────────────────────────────────────────────┤
 │ /api/v1/channels/list                            │ /api/v1/content/channels/list                     │
 ├──────────────────────────────────────────────────┼───────────────────────────────────────────────────┤
 │ /api/v1/channels/check-name                      │ /api/v1/content/channels/check-name               │
 ├──────────────────────────────────────────────────┼───────────────────────────────────────────────────┤
 │ /api/v1/admin/channels/create-system             │ /api/v1/content/admin/channels/create-system      │
 ├──────────────────────────────────────────────────┼───────────────────────────────────────────────────┤
 │ /api/v1/admin/channels/{channelId}/review        │ /api/v1/content/admin/channels/{channelId}/review │
 ├──────────────────────────────────────────────────┼───────────────────────────────────────────────────┤
 │ /jeecg-boot/api/v1/content/channel/review/list   │ /api/v1/content/channel/review/list               │
 ├──────────────────────────────────────────────────┼───────────────────────────────────────────────────┤
 │ /jeecg-boot/api/v1/content/channel/review/action │ /api/v1/content/channel/review/action             │
 └──────────────────────────────────────────────────┴───────────────────────────────────────────────────┘

 以及动态拼接的 transfer/delete-check 等路径（需从 /api/v1/channels/ 改为 /api/v1/content/channels/）。

 Phase 3: Circle 模块后端 (13 个 Controller)

 ┌───────────────────────────────┬────────────────────────────────┬───────────────────────────────────────┐
 │          Controller           │        当前 class-level        │           目标 class-level            │
 ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
 │ CircleController              │ /content/circle                │ /api/v1/content/circle                │
 ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
 │ CircleContentController       │ /content/circle                │ /api/v1/content/circle                │
 ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
 │ CircleSearchController        │ /content/circle                │ /api/v1/content/circle                │
 ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
 │ CircleMemberController        │ /content/circle/member         │ /api/v1/content/circle/member         │
 ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
 │ CircleDataController          │ /api/circle                    │ /api/v1/content/circle                │
 ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
 │ CircleGovernanceLogController │ /content/circle/governance-log │ /api/v1/content/circle/governance-log │
 ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
 │ CircleRankingController       │ /api/circle/ranking            │ /api/v1/content/circle/ranking        │
 ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
 │ CircleRecommendController     │ /api/circle                    │ /api/v1/content/circle                │
 ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
 │ CircleAnnouncementController  │ /circle-announcement           │ /api/v1/content/circle/announcement   │
 ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
 │ CircleContentPinController    │ /circle-content                │ /api/v1/content/circle/content        │
 ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
 │ CircleJoinReviewController    │ /circle-join-review            │ /api/v1/content/circle/join-review    │
 ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
 │ CircleReportController        │ /circle-report                 │ /api/v1/content/circle/report         │
 ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
 │ CircleLevelController         │ /content/user/growth/level     │ /api/v1/content/user/growth/level     │
 └───────────────────────────────┴────────────────────────────────┴───────────────────────────────────────┘

 无冲突: CircleDataController (/{circleId}/data/statistics, /{circleId}/data/export) 和 CircleRecommendController (/recommend, /recommend/click|join|exposure) 方法路径无重叠。CircleController 和 CircleContentController (/{circleId}/posts) 也无重叠。均可共用 /api/v1/content/circle。

 Phase 4: User 模块后端 (19 个 Controller)

 所有 /content/user/* 控制器添加 /api/v1 前缀：

 ┌─────────────────────────────────────┬──────────────────────────────────┬─────────────────────────────────────────┐
 │             Controller              │         当前 class-level         │            目标 class-level             │
 ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
 │ ContentAccountController            │ /content/user/account            │ /api/v1/content/user/account            │
 ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
 │ ContentFanAnalyticsController       │ /content/user/fan                │ /api/v1/content/user/fan                │
 ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
 │ ContentInviteController             │ /content/user/invite             │ /api/v1/content/user/invite             │
 ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
 │ ContentUserFilterRuleController     │ /content/user/filter-rule        │ /api/v1/content/user/filter-rule        │
 ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
 │ ContentUserGovernanceController     │ /content/user/governance         │ /api/v1/content/user/governance         │
 ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
 │ ContentUserGrowthController         │ /content/user/growth             │ /api/v1/content/user/growth             │
 ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
 │ ContentUserNotInterestedController  │ /content/user                    │ /api/v1/content/user                    │
 ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
 │ ContentUserProfileController        │ /content/user/profile            │ /api/v1/content/user/profile            │
 ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
 │ ContentUserRelationController       │ /content/user/relation           │ /api/v1/content/user/relation           │
 ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
 │ ContentUserSettingsController       │ /content/user/settings           │ /api/v1/content/user/settings           │
 ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
 │ ContentUserSubscriptionController   │ /content/user/subscription       │ /api/v1/content/user/subscription       │
 ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
 │ ContentUserSupportAdminController   │ /content/user/support/admin      │ /api/v1/content/user/support/admin      │
 ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
 │ ContentUserSupportController        │ /content/user/support            │ /api/v1/content/user/support            │
 ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
 │ ContentUserThirdPartyAuthController │ /content/user/auth/third-party   │ /api/v1/content/user/auth/third-party   │
 ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
 │ AchievementController               │ /content/user/growth/achievement │ /api/v1/content/circle/growth/achievement │
 ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
 │ CircleLevelController               │ /content/user/growth/level       │ /api/v1/content/user/growth/level       │
 ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
 │ LeaderboardController               │ /content/user/growth/leaderboard │ /api/v1/content/circle/growth/leaderboard │
 ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
 │ MemberGrowthController              │ /content/user/growth             │ /api/v1/content/user/growth             │
 └─────────────────────────────────────┴──────────────────────────────────┴─────────────────────────────────────────┘

 Phase 5: UserStatus 模块后端 (1 个 Controller)

 ┌──────────────────────┬──────────────────────────┬─────────────────────────────┐
 │      Controller      │     当前 class-level     │      目标 class-level       │
 ├──────────────────────┼──────────────────────────┼─────────────────────────────┤
 │ UserStatusController │ /api/content/user-status │ /api/v1/content/user-status │
 └──────────────────────┴──────────────────────────┴─────────────────────────────┘

 Phase 6: 前端路径同步

 6a. User 模块前端 (12 个文件)

 所有 /content/user/* 路径添加 /api/v1 前缀：

 ┌─────────────────────┬───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
 │        文件         │                                                               变更模式                                                                │
 ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
 │ relation.ts         │ /content/user/relation/* → /api/v1/content/user/relation/*                                                                            │
 ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
 │ subscribe.ts        │ /content/user/subscription/* → /api/v1/content/user/subscription/*                                                                    │
 ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
 │ settings.ts         │ /content/user/settings/* → /api/v1/content/user/settings/* + /content/user/auth/third-party → /api/v1/content/user/auth/third-party   │
 ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
 │ governance.ts       │ /content/user/governance/* → /api/v1/content/user/governance/*                                                                        │
 ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
 │ block.ts            │ /content/user/relation/* → /api/v1/content/user/relation/*                                                                            │
 ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
 │ invite.ts           │ /content/user/invite/* → /api/v1/content/user/invite/*                                                                                │
 ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
 │ mute.ts             │ /content/user/relation/mute* → /api/v1/content/user/relation/mute*                                                                    │
 ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
 │ fan-analytics.ts    │ /content/user/fan/* → /api/v1/content/user/fan/*                                                                                      │
 ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
 │ filterRule.ts       │ /content/user/filter-rule/* → /api/v1/content/user/filter-rule/* + /content/user/not-interested → /api/v1/content/user/not-interested │
 ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
 │ growth/index.ts     │ /content/user/growth/* → /api/v1/content/user/growth/*                                                                                │
 ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
 │ growth/badge.ts     │ /content/user/growth/badge/* → /api/v1/content/user/growth/badge/*                                                                    │
 ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
 │ growth/point.ts     │ /content/user/growth/point/* → /api/v1/content/user/growth/point/*                                                                    │
 ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
 │ profile/index.ts    │ /content/user/profile/* → /api/v1/content/user/profile/*                                                                              │
 ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
 │ user/preferences.ts │ /api/v1/user/preferences/* → /api/v1/content/user/preferences/*                                                                       │
 └─────────────────────┴───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘

 6b. UserStatus 前端 (1 个文件)

 ┌───────────────┬────────────────────────────────────────────────────────────┐
 │     文件      │                          变更模式                          │
 ├───────────────┼────────────────────────────────────────────────────────────┤
 │ userStatus.ts │ /api/content/user-status/* → /api/v1/content/user-status/* │
 └───────────────┴────────────────────────────────────────────────────────────┘

 ---
 已验证

 - Governance 路径冲突: 无 — ChannelGovernanceController (remove/mute/unmute/blacklist/log) 和 ChannelContentGovernanceController (POST /, content/list, recycle-bin/list) 方法路径无重叠
 - Growth 路径冲突: 无 — ContentUserGrowthController (/record, /summary, /badge/, /point/, /level/, /decay/) 和 MemberGrowthController (/info, /participation) 方法路径无重叠
   └ Explore user + userstatus controllers · 46 tool uses · 0 tokens
     ⎿  Done


⏺ User approved Claude's plan
  ⎿  Plan saved to: ~/.claude/plans/gentle-dazzling-mango.md · /plan to edit
     API 路径合规性修复计划

     Context

     AGENTS.md 规定：内容社区 API 路径统一使用 /api/v1/content/ 前缀。user-10 已修复 auth 模块（26 个后端端点 + 13 个前端路径）。但 channel、circle、user、userstatus 四个模块仍不合规。

     目标路径格式: /api/v1/content/{module}/{resource}

     ---
     现状汇总

     ┌────────────┬────────────────────┬─────────────────────────────────────────┬─────────────────┬──────────────────────────┐
     │    模块    │ 后端 Controller 数 │                 已合规                  │   前端文件数    │          已合规          │
     ├────────────┼────────────────────┼─────────────────────────────────────────┼─────────────────┼──────────────────────────┤
     │ channel    │ 24                 │ 5 (export/lifecycle/merge/review/stats) │ 1 (index.ts)    │ 0                        │
     ├────────────┼────────────────────┼─────────────────────────────────────────┼─────────────────┼──────────────────────────┤
     │ circle     │ 13                 │ 0                                       │ 0 (无前端API层) │ N/A                      │
     ├────────────┼────────────────────┼─────────────────────────────────────────┼─────────────────┼──────────────────────────┤
     │ user       │ 19                 │ 0                                       │ 14              │ 0 (除 account/ 下已合规) │
     ├────────────┼────────────────────┼─────────────────────────────────────────┼─────────────────┼──────────────────────────┤
     │ userstatus │ 1                  │ 0                                       │ 1               │ 0                        │
     └────────────┴────────────────────┴─────────────────────────────────────────┴─────────────────┴──────────────────────────┘

     总计: 57 个后端 Controller + 16 个前端文件需修复

     ---
     修复方案

     规则

     后端: 只修改 class-level @RequestMapping 注解值，不改动方法签名和业务逻辑
     前端: 只修改 API 路径字符串，不改动函数签名

     Phase 1: Channel 模块后端 (19 个 Controller)

     1a. 路径映射表

     ┌────────────────────────────────────────┬─────────────────────────────────┬────────────────────────────────────────┐
     │               Controller               │        当前 class-level         │            目标 class-level            │
     ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
     │ ChannelController                      │ /api/v1/channels                │ /api/v1/content/channels               │
     ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
     │ ChannelAdminController                 │ /api/v1/admin/channels          │ /api/v1/content/admin/channels         │
     ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
     │ ChannelAnnouncementController          │ /content/channel/announcement   │ /api/v1/content/channel/announcement   │
     ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
     │ ChannelContentGovernanceController     │ /content/channel/governance     │ /api/v1/content/channel/governance     │
     ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
     │ ChannelContentReviewController         │ /content/channel/review         │ /api/v1/content/channel/review         │
     ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
     │ ChannelGovernanceController            │ /channel/governance             │ /api/v1/content/channel/governance     │
     ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
     │ ChannelInviteController                │ /channel/invite                 │ /api/v1/content/channel/invite         │
     ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
     │ ChannelMemberController                │ /channel/member                 │ /api/v1/content/channel/member         │
     ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
     │ ChannelPublishController               │ /content/channel/publish        │ /api/v1/content/channel/publish        │
     ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
     │ ChannelSubscriptionController          │ /channel/subscription           │ /api/v1/content/channel/subscription   │
     ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
     │ ContentChannelBrowseController         │ /content/channel/browse         │ /api/v1/content/channel/browse         │
     ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
     │ ContentChannelCategoryController       │ /content/channel/category       │ /api/v1/content/channel/category       │
     ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
     │ ContentChannelDiscoveryController      │ /content/channel/discovery      │ /api/v1/content/channel/discovery      │
     ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
     │ ContentChannelEditorialPickController  │ /content/channel/editorial-pick │ /api/v1/content/channel/editorial-pick │
     ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
     │ ContentChannelRankingController        │ /content/channel/ranking        │ /api/v1/content/channel/ranking        │
     ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
     │ ContentChannelRecommendationController │ /content/channel/recommendation │ /api/v1/content/channel/recommendation │
     ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
     │ ContentChannelSearchController         │ /content/channel/search         │ /api/v1/content/channel/search         │
     ├────────────────────────────────────────┼─────────────────────────────────┼────────────────────────────────────────┤
     │ ContentChannelTagController            │ /content/channel/tag            │ /api/v1/content/channel/tag            │
     └────────────────────────────────────────┴─────────────────────────────────┴────────────────────────────────────────┘

     无冲突: ChannelGovernanceController (remove/mute/unmute/blacklist/log) 和 ChannelContentGovernanceController (POST /, content/list, recycle-bin/list) 方法路径无重叠，可共用 class-level 路径。

     1b. 已合规 (不修改)

     - ChannelExportController (/api/v1/content/channel/export)
     - ChannelLifecycleController (/api/v1/content/channel/lifecycle)
     - ChannelMergeController (/api/v1/content/channel/merge)
     - ChannelReviewController (/api/v1/content/channel/review)
     - ChannelStatsController (/api/v1/content/channel/stats)

     Phase 2: Channel 模块前端

     修改 jeecgboot-vue3/src/api/content/channel/index.ts:

     ┌──────────────────────────────────────────────────┬───────────────────────────────────────────────────┐
     │                     当前路径                     │                     目标路径                      │
     ├──────────────────────────────────────────────────┼───────────────────────────────────────────────────┤
     │ /api/v1/channels/create                          │ /api/v1/content/channels/create                   │
     ├──────────────────────────────────────────────────┼───────────────────────────────────────────────────┤
     │ /api/v1/channels/{id}                            │ /api/v1/content/channels/{id}                     │
     ├──────────────────────────────────────────────────┼───────────────────────────────────────────────────┤
     │ /api/v1/channels/list                            │ /api/v1/content/channels/list                     │
     ├──────────────────────────────────────────────────┼───────────────────────────────────────────────────┤
     │ /api/v1/channels/check-name                      │ /api/v1/content/channels/check-name               │
     ├──────────────────────────────────────────────────┼───────────────────────────────────────────────────┤
     │ /api/v1/admin/channels/create-system             │ /api/v1/content/admin/channels/create-system      │
     ├──────────────────────────────────────────────────┼───────────────────────────────────────────────────┤
     │ /api/v1/admin/channels/{channelId}/review        │ /api/v1/content/admin/channels/{channelId}/review │
     ├──────────────────────────────────────────────────┼───────────────────────────────────────────────────┤
     │ /jeecg-boot/api/v1/content/channel/review/list   │ /api/v1/content/channel/review/list               │
     ├──────────────────────────────────────────────────┼───────────────────────────────────────────────────┤
     │ /jeecg-boot/api/v1/content/channel/review/action │ /api/v1/content/channel/review/action             │
     └──────────────────────────────────────────────────┴───────────────────────────────────────────────────┘

     以及动态拼接的 transfer/delete-check 等路径（需从 /api/v1/channels/ 改为 /api/v1/content/channels/）。

     Phase 3: Circle 模块后端 (13 个 Controller)

     ┌───────────────────────────────┬────────────────────────────────┬───────────────────────────────────────┐
     │          Controller           │        当前 class-level        │           目标 class-level            │
     ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
     │ CircleController              │ /content/circle                │ /api/v1/content/circle                │
     ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
     │ CircleContentController       │ /content/circle                │ /api/v1/content/circle                │
     ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
     │ CircleSearchController        │ /content/circle                │ /api/v1/content/circle                │
     ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
     │ CircleMemberController        │ /content/circle/member         │ /api/v1/content/circle/member         │
     ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
     │ CircleDataController          │ /api/circle                    │ /api/v1/content/circle                │
     ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
     │ CircleGovernanceLogController │ /content/circle/governance-log │ /api/v1/content/circle/governance-log │
     ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
     │ CircleRankingController       │ /api/circle/ranking            │ /api/v1/content/circle/ranking        │
     ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
     │ CircleRecommendController     │ /api/circle                    │ /api/v1/content/circle                │
     ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
     │ CircleAnnouncementController  │ /circle-announcement           │ /api/v1/content/circle/announcement   │
     ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
     │ CircleContentPinController    │ /circle-content                │ /api/v1/content/circle/content        │
     ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
     │ CircleJoinReviewController    │ /circle-join-review            │ /api/v1/content/circle/join-review    │
     ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
     │ CircleReportController        │ /circle-report                 │ /api/v1/content/circle/report         │
     ├───────────────────────────────┼────────────────────────────────┼───────────────────────────────────────┤
     │ CircleLevelController         │ /content/user/growth/level     │ /api/v1/content/user/growth/level     │
     └───────────────────────────────┴────────────────────────────────┴───────────────────────────────────────┘

     无冲突: CircleDataController (/{circleId}/data/statistics, /{circleId}/data/export) 和 CircleRecommendController (/recommend, /recommend/click|join|exposure) 方法路径无重叠。CircleController 和 CircleContentController (/{circleId}/posts) 也无重叠。均可共用 /api/v1/content/circle。

     Phase 4: User 模块后端 (19 个 Controller)

     所有 /content/user/* 控制器添加 /api/v1 前缀：

     ┌─────────────────────────────────────┬──────────────────────────────────┬─────────────────────────────────────────┐
     │             Controller              │         当前 class-level         │            目标 class-level             │
     ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
     │ ContentAccountController            │ /content/user/account            │ /api/v1/content/user/account            │
     ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
     │ ContentFanAnalyticsController       │ /content/user/fan                │ /api/v1/content/user/fan                │
     ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
     │ ContentInviteController             │ /content/user/invite             │ /api/v1/content/user/invite             │
     ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
     │ ContentUserFilterRuleController     │ /content/user/filter-rule        │ /api/v1/content/user/filter-rule        │
     ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
     │ ContentUserGovernanceController     │ /content/user/governance         │ /api/v1/content/user/governance         │
     ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
     │ ContentUserGrowthController         │ /content/user/growth             │ /api/v1/content/user/growth             │
     ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
     │ ContentUserNotInterestedController  │ /content/user                    │ /api/v1/content/user                    │
     ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
     │ ContentUserProfileController        │ /content/user/profile            │ /api/v1/content/user/profile            │
     ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
     │ ContentUserRelationController       │ /content/user/relation           │ /api/v1/content/user/relation           │
     ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
     │ ContentUserSettingsController       │ /content/user/settings           │ /api/v1/content/user/settings           │
     ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
     │ ContentUserSubscriptionController   │ /content/user/subscription       │ /api/v1/content/user/subscription       │
     ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
     │ ContentUserSupportAdminController   │ /content/user/support/admin      │ /api/v1/content/user/support/admin      │
     ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
     │ ContentUserSupportController        │ /content/user/support            │ /api/v1/content/user/support            │
     ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
     │ ContentUserThirdPartyAuthController │ /content/user/auth/third-party   │ /api/v1/content/user/auth/third-party   │
     ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
     │ AchievementController               │ /content/user/growth/achievement │ /api/v1/content/circle/growth/achievement │
     ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
     │ CircleLevelController               │ /content/user/growth/level       │ /api/v1/content/user/growth/level       │
     ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
     │ LeaderboardController               │ /content/user/growth/leaderboard │ /api/v1/content/circle/growth/leaderboard │
     ├─────────────────────────────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
     │ MemberGrowthController              │ /content/user/growth             │ /api/v1/content/user/growth             │
     └─────────────────────────────────────┴──────────────────────────────────┴─────────────────────────────────────────┘

     Phase 5: UserStatus 模块后端 (1 个 Controller)

     ┌──────────────────────┬──────────────────────────┬─────────────────────────────┐
     │      Controller      │     当前 class-level     │      目标 class-level       │
     ├──────────────────────┼──────────────────────────┼─────────────────────────────┤
     │ UserStatusController │ /api/content/user-status │ /api/v1/content/user-status │
     └──────────────────────┴──────────────────────────┴─────────────────────────────┘

     Phase 6: 前端路径同步

     6a. User 模块前端 (12 个文件)

     所有 /content/user/* 路径添加 /api/v1 前缀：

     ┌─────────────────────┬───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
     │        文件         │                                                               变更模式                                                                │
     ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
     │ relation.ts         │ /content/user/relation/* → /api/v1/content/user/relation/*                                                                            │
     ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
     │ subscribe.ts        │ /content/user/subscription/* → /api/v1/content/user/subscription/*                                                                    │
     ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
     │ settings.ts         │ /content/user/settings/* → /api/v1/content/user/settings/* + /content/user/auth/third-party → /api/v1/content/user/auth/third-party   │
     ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
     │ governance.ts       │ /content/user/governance/* → /api/v1/content/user/governance/*                                                                        │
     ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
     │ block.ts            │ /content/user/relation/* → /api/v1/content/user/relation/*                                                                            │
     ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
     │ invite.ts           │ /content/user/invite/* → /api/v1/content/user/invite/*                                                                                │
     ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
     │ mute.ts             │ /content/user/relation/mute* → /api/v1/content/user/relation/mute*                                                                    │
     ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
     │ fan-analytics.ts    │ /content/user/fan/* → /api/v1/content/user/fan/*                                                                                      │
     ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
     │ filterRule.ts       │ /content/user/filter-rule/* → /api/v1/content/user/filter-rule/* + /content/user/not-interested → /api/v1/content/user/not-interested │
     ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
     │ growth/index.ts     │ /content/user/growth/* → /api/v1/content/user/growth/*                                                                                │
     ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
     │ growth/badge.ts     │ /content/user/growth/badge/* → /api/v1/content/user/growth/badge/*                                                                    │
     ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
     │ growth/point.ts     │ /content/user/growth/point/* → /api/v1/content/user/growth/point/*                                                                    │
     ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
     │ profile/index.ts    │ /content/user/profile/* → /api/v1/content/user/profile/*                                                                              │
     ├─────────────────────┼───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
     │ user/preferences.ts │ /api/v1/user/preferences/* → /api/v1/content/user/preferences/*                                                                       │
     └─────────────────────┴───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘

     6b. UserStatus 前端 (1 个文件)

     ┌───────────────┬────────────────────────────────────────────────────────────┐
     │     文件      │                          变更模式                          │
     ├───────────────┼────────────────────────────────────────────────────────────┤
     │ userStatus.ts │ /api/content/user-status/* → /api/v1/content/user-status/* │
     └───────────────┴────────────────────────────────────────────────────────────┘

     ---
     已验证

     - Governance 路径冲突: 无 — ChannelGovernanceController (remove/mute/unmute/blacklist/log) 和 ChannelContentGovernanceController (POST /, content/list, recycle-bin/list) 方法路径无重叠