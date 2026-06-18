# API 接口清单

## 圈子排行榜
### LeaderboardController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/controller/LeaderboardController.java`)
**Base Path**: `/api/v1/content/user/growth/leaderboard`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|

## 圈子等级信息
### CircleLevelController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/controller/CircleLevelController.java`)
**Base Path**: `/api/v1/content/user/growth/level`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/user/growth/level/info` | 获取圈子等级信息 | circleId: String (query) | `CircleLevelVO` | 22 |

## 成员成长信息
### MemberGrowthController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/controller/MemberGrowthController.java`)
**Base Path**: `/api/v1/content/user/growth`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/user/growth/info` | 获取成员在圈子的成长信息 | circleId: String (query), userId: String (query) | `MemberGrowthVO` | 24 |
| GET | `/api/v1/content/user/growth/participation` | 获取连续参与进度 | circleId: String (query), userId: String (query) | `Integer` | 32 |

## 成就徽章
### AchievementController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/controller/AchievementController.java`)
**Base Path**: `/api/v1/content/user/growth/achievement`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/user/growth/achievement/list` | 获取成员在圈子的徽章列表 | circleId: String (query), userId: String (query) | `List<AchievementVO>` | 25 |
