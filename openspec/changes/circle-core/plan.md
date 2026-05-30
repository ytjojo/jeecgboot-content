# 圈子核心基础设施 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 jeecg-module-content 中实现圈子创建、成员管理和搜索三大能力，构建圈子 MVP。

**Architecture:** 遵循现有 controller/biz/service/mapper/entity 分层架构。圈子作为 content 模块的新子包 `circle` 引入。三张核心表：circle（圈子）、circle_member（成员）、circle_governance_log（治理日志）。搜索使用 MySQL LIKE 作为 MVP 实现。

**Tech Stack:** Spring Boot 3 + MyBatis-Plus + MySQL + BCrypt + Flyway

**关键路径:** `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/`

---

## Task 1: 数据库迁移脚本

**Files:**
- Create: `main/resources/flyway/sql/mysql/V3.9.1_63__content_circle_tables.sql`

- [ ] **Step 1: 编写 Flyway 迁移脚本**

```sql
-- ============================================================
-- 1. 圈子主表
-- ============================================================
CREATE TABLE IF NOT EXISTS `content_circle` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '圈子名称',
  `description` varchar(500) DEFAULT NULL COMMENT '圈子简介',
  `icon_url` varchar(500) DEFAULT NULL COMMENT '图标URL',
  `cover_url` varchar(500) DEFAULT NULL COMMENT '封面图URL',
  `category` varchar(64) DEFAULT NULL COMMENT '分类标签',
  `privacy_type` varchar(32) NOT NULL DEFAULT 'PUBLIC' COMMENT '隐私类型: PUBLIC/PRIVATE/PASSWORD',
  `join_type` varchar(32) NOT NULL DEFAULT 'DIRECT' COMMENT '加入方式: DIRECT/APPROVAL/INVITE/PASSWORD',
  `password_hash` varchar(255) DEFAULT NULL COMMENT '密码保护密码哈希(BCrypt)',
  `creator_id` varchar(32) NOT NULL COMMENT '创建者用户ID',
  `member_count` int NOT NULL DEFAULT 1 COMMENT '成员数',
  `max_member_count` int NOT NULL DEFAULT 10000 COMMENT '最大成员数',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/DISABLED',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_circle_name` (`name`),
  KEY `idx_content_circle_creator` (`creator_id`),
  KEY `idx_content_circle_status` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子';

-- ============================================================
-- 2. 圈子成员表
-- ============================================================
CREATE TABLE IF NOT EXISTS `content_circle_member` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `circle_id` varchar(32) NOT NULL COMMENT '圈子ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `role` varchar(32) NOT NULL DEFAULT 'MEMBER' COMMENT '角色: CREATOR/MODERATOR/MEMBER',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/MUTED/REMOVED',
  `mute_end_time` datetime DEFAULT NULL COMMENT '禁言结束时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_circle_member` (`circle_id`, `user_id`),
  KEY `idx_content_circle_member_user` (`user_id`),
  KEY `idx_content_circle_member_status` (`circle_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子成员';

-- ============================================================
-- 3. 圈子治理日志表
-- ============================================================
CREATE TABLE IF NOT EXISTS `content_circle_governance_log` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `circle_id` varchar(32) NOT NULL COMMENT '圈子ID',
  `operator_id` varchar(32) NOT NULL COMMENT '操作者用户ID',
  `target_user_id` varchar(32) NOT NULL COMMENT '目标用户ID',
  `action` varchar(32) NOT NULL COMMENT '动作: MUTE/UNMUTE/REMOVE/ROLE_CHANGE',
  `reason` varchar(500) DEFAULT NULL COMMENT '操作原因',
  `duration` varchar(32) DEFAULT NULL COMMENT '禁言时长(如: 1h/24h/7d/PERMANENT)',
  `extra_data_json` text DEFAULT NULL COMMENT '额外数据JSON(如角色变更前后)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_circle_governance_circle` (`circle_id`, `create_time`),
  KEY `idx_content_circle_governance_target` (`target_user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子治理操作日志';
```

- [ ] **Step 2: 验证 SQL 语法**

手动检查 SQL 语法正确性，确认表名、字段名、索引命名符合 `content_` 前缀规范。

- [ ] **Step 3: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_63__content_circle_tables.sql
git commit -m "feat(circle): add Flyway migration for circle tables"
```

---

## Task 2: Entity 层 — Circle

**Files:**
- Create: `main/java/org/jeecg/modules/content/circle/entity/Circle.java`
- Create: `test/java/org/jeecg/modules/content/circle/entity/CircleTest.java`

- [ ] **Step 1: 编写 Circle Entity 枚举测试**

```java
package org.jeecg.modules.content.circle.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Circle Entity")
class CircleTest {

    @Test
    @DisplayName("PrivacyType enum values")
    void privacyType_enumValues() {
        Circle.PrivacyType[] values = Circle.PrivacyType.values();
        assertEquals(3, values.length);
        assertNotNull(Circle.PrivacyType.valueOf("PUBLIC"));
        assertNotNull(Circle.PrivacyType.valueOf("PRIVATE"));
        assertNotNull(Circle.PrivacyType.valueOf("PASSWORD"));
    }

    @Test
    @DisplayName("JoinType enum values")
    void joinType_enumValues() {
        Circle.JoinType[] values = Circle.JoinType.values();
        assertEquals(4, values.length);
        assertNotNull(Circle.JoinType.valueOf("DIRECT"));
        assertNotNull(Circle.JoinType.valueOf("APPROVAL"));
        assertNotNull(Circle.JoinType.valueOf("INVITE"));
        assertNotNull(Circle.JoinType.valueOf("PASSWORD"));
    }

    @Test
    @DisplayName("Status enum values")
    void status_enumValues() {
        Circle.Status[] values = Circle.Status.values();
        assertEquals(2, values.length);
        assertNotNull(Circle.Status.valueOf("ACTIVE"));
        assertNotNull(Circle.Status.valueOf("DISABLED"));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.entity.CircleTest" -DfailIfNoTests=false
```

Expected: COMPILATION ERROR — Circle class does not exist

- [ ] **Step 3: 创建 Circle Entity**

```java
package org.jeecg.modules.content.circle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_circle")
public class Circle extends JeecgEntity {

    @Schema(description = "圈子名称")
    private String name;

    @Schema(description = "圈子简介")
    private String description;

    @Schema(description = "图标URL")
    private String iconUrl;

    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "分类标签")
    private String category;

    @Schema(description = "隐私类型: PUBLIC/PRIVATE/PASSWORD")
    private PrivacyType privacyType;

    @Schema(description = "加入方式: DIRECT/APPROVAL/INVITE/PASSWORD")
    private JoinType joinType;

    @Schema(description = "密码保护密码哈希(BCrypt)")
    private String passwordHash;

    @Schema(description = "创建者用户ID")
    private String creatorId;

    @Schema(description = "成员数")
    private Integer memberCount;

    @Schema(description = "最大成员数")
    private Integer maxMemberCount;

    @Schema(description = "状态: ACTIVE/DISABLED")
    private Status status;

    public enum PrivacyType {
        PUBLIC, PRIVATE, PASSWORD
    }

    public enum JoinType {
        DIRECT, APPROVAL, INVITE, PASSWORD
    }

    public enum Status {
        ACTIVE, DISABLED
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

```bash
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.entity.CircleTest" -DfailIfNoTests=false
```

Expected: Tests run: 3, Failures: 0

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/entity/Circle.java
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/entity/CircleTest.java
git commit -m "feat(circle): add Circle entity with enums"
```

---

## Task 3: Entity 层 — CircleMember

**Files:**
- Create: `main/java/org/jeecg/modules/content/circle/entity/CircleMember.java`
- Create: `test/java/org/jeecg/modules/content/circle/entity/CircleMemberTest.java`

- [ ] **Step 1: 编写 CircleMember 枚举测试**

```java
package org.jeecg.modules.content.circle.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CircleMember Entity")
class CircleMemberTest {

    @Test
    @DisplayName("Role enum values")
    void role_enumValues() {
        CircleMember.Role[] values = CircleMember.Role.values();
        assertEquals(3, values.length);
        assertNotNull(CircleMember.Role.valueOf("CREATOR"));
        assertNotNull(CircleMember.Role.valueOf("MODERATOR"));
        assertNotNull(CircleMember.Role.valueOf("MEMBER"));
    }

    @Test
    @DisplayName("Status enum values")
    void status_enumValues() {
        CircleMember.Status[] values = CircleMember.Status.values();
        assertEquals(3, values.length);
        assertNotNull(CircleMember.Status.valueOf("ACTIVE"));
        assertNotNull(CircleMember.Status.valueOf("MUTED"));
        assertNotNull(CircleMember.Status.valueOf("REMOVED"));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.entity.CircleMemberTest" -DfailIfNoTests=false
```

Expected: COMPILATION ERROR — CircleMember class does not exist

- [ ] **Step 3: 创建 CircleMember Entity**

```java
package org.jeecg.modules.content.circle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_circle_member")
public class CircleMember extends JeecgEntity {

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "角色: CREATOR/MODERATOR/MEMBER")
    private Role role;

    @Schema(description = "状态: ACTIVE/MUTED/REMOVED")
    private Status status;

    @Schema(description = "禁言结束时间")
    private LocalDateTime muteEndTime;

    public enum Role {
        CREATOR, MODERATOR, MEMBER
    }

    public enum Status {
        ACTIVE, MUTED, REMOVED
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

```bash
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.entity.CircleMemberTest" -DfailIfNoTests=false
```

Expected: Tests run: 2, Failures: 0

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/entity/CircleMember.java
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/entity/CircleMemberTest.java
git commit -m "feat(circle): add CircleMember entity with enums"
```

---

## Task 4: Entity 层 — CircleGovernanceLog

**Files:**
- Create: `main/java/org/jeecg/modules/content/circle/entity/CircleGovernanceLog.java`

- [ ] **Step 1: 创建 CircleGovernanceLog Entity**

```java
package org.jeecg.modules.content.circle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("content_circle_governance_log")
public class CircleGovernanceLog {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "操作者用户ID")
    private String operatorId;

    @Schema(description = "目标用户ID")
    private String targetUserId;

    @Schema(description = "动作: MUTE/UNMUTE/REMOVE/ROLE_CHANGE")
    private Action action;

    @Schema(description = "操作原因")
    private String reason;

    @Schema(description = "禁言时长(如: 1h/24h/7d/PERMANENT)")
    private String duration;

    @Schema(description = "额外数据JSON")
    private String extraDataJson;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    public enum Action {
        MUTE, UNMUTE, REMOVE, ROLE_CHANGE
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/entity/CircleGovernanceLog.java
git commit -m "feat(circle): add CircleGovernanceLog entity"
```

---

## Task 5: Mapper 层

**Files:**
- Create: `main/java/org/jeecg/modules/content/circle/mapper/CircleMapper.java`
- Create: `main/java/org/jeecg/modules/content/circle/mapper/CircleMemberMapper.java`
- Create: `main/java/org/jeecg/modules/content/circle/mapper/CircleGovernanceLogMapper.java`
- Create: `main/resources/mapper/content/circle/CircleMapper.xml`
- Create: `main/resources/mapper/content/circle/CircleMemberMapper.xml`
- Create: `main/resources/mapper/content/circle/CircleGovernanceLogMapper.xml`

- [ ] **Step 1: 创建 CircleMapper**

```java
package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.circle.entity.Circle;

@Mapper
public interface CircleMapper extends BaseMapper<Circle> {
}
```

- [ ] **Step 2: 创建 CircleMemberMapper**

```java
package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.circle.entity.CircleMember;

@Mapper
public interface CircleMemberMapper extends BaseMapper<CircleMember> {
}
```

- [ ] **Step 3: 创建 CircleGovernanceLogMapper**

```java
package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.circle.entity.CircleGovernanceLog;

@Mapper
public interface CircleGovernanceLogMapper extends BaseMapper<CircleGovernanceLog> {
}
```

- [ ] **Step 4: 创建 Mapper XML 文件**

CircleMapper.xml:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.jeecg.modules.content.circle.mapper.CircleMapper">
</mapper>
```

CircleMemberMapper.xml:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.jeecg.modules.content.circle.mapper.CircleMemberMapper">
</mapper>
```

CircleGovernanceLogMapper.xml:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.jeecg.modules.content.circle.mapper.CircleGovernanceLogMapper">
</mapper>
```

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/mapper/
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/mapper/content/circle/
git commit -m "feat(circle): add Mapper interfaces and XML files"
```

---

## Task 6: Req/VO/DTO 层

**Files:**
- Create: `main/java/org/jeecg/modules/content/circle/req/create/CircleCreateReq.java`
- Create: `main/java/org/jeecg/modules/content/circle/req/create/CircleJoinReq.java`
- Create: `main/java/org/jeecg/modules/content/circle/req/update/CircleUpdateReq.java`
- Create: `main/java/org/jeecg/modules/content/circle/req/update/CircleMemberUpdateReq.java`
- Create: `main/java/org/jeecg/modules/content/circle/req/query/CircleSearchReq.java`
- Create: `main/java/org/jeecg/modules/content/circle/vo/CircleVO.java`
- Create: `main/java/org/jeecg/modules/content/circle/vo/CircleMemberVO.java`
- Create: `main/java/org/jeecg/modules/content/circle/vo/CircleSearchResultVO.java`

- [ ] **Step 1: 创建 CircleCreateReq**

```java
package org.jeecg.modules.content.circle.req.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建圈子请求")
public class CircleCreateReq {

    @NotBlank(message = "圈子名称不能为空")
    @Size(max = 100, message = "圈子名称最多100个字符")
    @Schema(description = "圈子名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "圈子简介不能为空")
    @Size(max = 500, message = "圈子简介最多500个字符")
    @Schema(description = "圈子简介", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(description = "图标URL")
    private String iconUrl;

    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "分类标签")
    private String category;

    @NotNull(message = "隐私类型不能为空")
    @Schema(description = "隐私类型: PUBLIC/PRIVATE/PASSWORD", requiredMode = Schema.RequiredMode.REQUIRED)
    private String privacyType;

    @NotNull(message = "加入方式不能为空")
    @Schema(description = "加入方式: DIRECT/APPROVAL/INVITE/PASSWORD", requiredMode = Schema.RequiredMode.REQUIRED)
    private String joinType;

    @Schema(description = "密码保护密码(当privacyType=PASSWORD时必填)")
    private String password;
}
```

- [ ] **Step 2: 创建 CircleJoinReq**

```java
package org.jeecg.modules.content.circle.req.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "加入圈子请求")
public class CircleJoinReq {

    @NotBlank(message = "圈子ID不能为空")
    @Schema(description = "圈子ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String circleId;

    @Schema(description = "密码(当圈子为密码保护时必填)")
    private String password;
}
```

- [ ] **Step 3: 创建 CircleUpdateReq**

```java
package org.jeecg.modules.content.circle.req.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新圈子请求")
public class CircleUpdateReq {

    @NotBlank(message = "圈子ID不能为空")
    @Schema(description = "圈子ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String circleId;

    @Size(max = 500, message = "圈子简介最多500个字符")
    @Schema(description = "圈子简介")
    private String description;

    @Schema(description = "图标URL")
    private String iconUrl;

    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "分类标签")
    private String category;
}
```

- [ ] **Step 4: 创建 CircleMemberUpdateReq**

```java
package org.jeecg.modules.content.circle.req.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "圈子成员操作请求")
public class CircleMemberUpdateReq {

    @NotBlank(message = "圈子ID不能为空")
    @Schema(description = "圈子ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String circleId;

    @NotBlank(message = "目标用户ID不能为空")
    @Schema(description = "目标用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetUserId;

    @Schema(description = "目标角色(角色变更时必填)")
    private String targetRole;

    @Schema(description = "禁言时长: 1h/24h/7d/PERMANENT")
    private String muteDuration;

    @Schema(description = "操作原因")
    private String reason;
}
```

- [ ] **Step 5: 创建 CircleSearchReq**

```java
package org.jeecg.modules.content.circle.req.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "圈子搜索请求")
public class CircleSearchReq {

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "页码(从1开始)")
    private Integer pageNum = 1;

    @Schema(description = "每页条数")
    private Integer pageSize = 20;
}
```

- [ ] **Step 6: 创建 CircleVO**

```java
package org.jeecg.modules.content.circle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "圈子详情响应")
public class CircleVO {

    @Schema(description = "圈子ID")
    private String id;

    @Schema(description = "圈子名称")
    private String name;

    @Schema(description = "圈子简介")
    private String description;

    @Schema(description = "图标URL")
    private String iconUrl;

    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "分类标签")
    private String category;

    @Schema(description = "隐私类型")
    private String privacyType;

    @Schema(description = "加入方式")
    private String joinType;

    @Schema(description = "创建者用户ID")
    private String creatorId;

    @Schema(description = "成员数")
    private Integer memberCount;

    @Schema(description = "最大成员数")
    private Integer maxMemberCount;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "当前用户是否已加入")
    private Boolean joined;

    @Schema(description = "当前用户在圈子中的角色")
    private String myRole;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
```

- [ ] **Step 7: 创建 CircleMemberVO**

```java
package org.jeecg.modules.content.circle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "圈子成员响应")
public class CircleMemberVO {

    @Schema(description = "成员记录ID")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "禁言结束时间")
    private LocalDateTime muteEndTime;

    @Schema(description = "加入时间")
    private LocalDateTime createTime;
}
```

- [ ] **Step 8: 创建 CircleSearchResultVO**

```java
package org.jeecg.modules.content.circle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "圈子搜索结果项")
public class CircleSearchResultVO {

    @Schema(description = "圈子ID")
    private String id;

    @Schema(description = "圈子名称")
    private String name;

    @Schema(description = "图标URL")
    private String iconUrl;

    @Schema(description = "圈子简介")
    private String description;

    @Schema(description = "成员数")
    private Integer memberCount;

    @Schema(description = "当前用户是否已加入")
    private Boolean joined;
}
```

- [ ] **Step 9: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/req/
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/vo/
git commit -m "feat(circle): add Req/VO request and response objects"
```

---

## Task 7: Service 层 — CircleService

**Files:**
- Create: `main/java/org/jeecg/modules/content/circle/service/ICircleService.java`
- Create: `main/java/org/jeecg/modules/content/circle/service/impl/CircleServiceImpl.java`
- Create: `test/java/org/jeecg/modules/content/circle/service/CircleServiceTest.java`

- [ ] **Step 1: 编写 CircleService 测试**

```java
package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.mapper.CircleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleService")
class CircleServiceTest {

    @Mock
    private CircleMapper circleMapper;

    @InjectMocks
    private CircleServiceImpl circleService;

    @Nested
    @DisplayName("createCircle")
    class CreateCircle {

        @Test
        @DisplayName("name exists - throws exception")
        void nameExists_throwsException() {
            when(circleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> circleService.checkNameUnique("已存在的圈子"));
            assertEquals("该圈子名称已存在，请修改", ex.getMessage());
        }

        @Test
        @DisplayName("name unique - passes")
        void nameUnique_passes() {
            when(circleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            assertDoesNotThrow(() -> circleService.checkNameUnique("新圈子"));
        }
    }

    @Nested
    @DisplayName("incrementMemberCount")
    class IncrementMemberCount {

        @Test
        @DisplayName("reaches max - throws exception")
        void reachesMax_throwsException() {
            Circle circle = new Circle();
            circle.setId("c_001");
            circle.setMemberCount(10000);
            circle.setMaxMemberCount(10000);

            when(circleMapper.selectById("c_001")).thenReturn(circle);

            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> circleService.incrementMemberCount("c_001"));
            assertEquals("圈子已满员，无法加入", ex.getMessage());
        }

        @Test
        @DisplayName("under max - increments")
        void underMax_increments() {
            Circle circle = new Circle();
            circle.setId("c_001");
            circle.setMemberCount(9999);
            circle.setMaxMemberCount(10000);

            when(circleMapper.selectById("c_001")).thenReturn(circle);
            when(circleMapper.updateById(any())).thenReturn(1);

            assertDoesNotThrow(() -> circleService.incrementMemberCount("c_001"));
            verify(circleMapper).updateById(any());
        }
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.service.CircleServiceTest" -DfailIfNoTests=false
```

Expected: COMPILATION ERROR — CircleService/CircleServiceImpl does not exist

- [ ] **Step 3: 创建 ICircleService 接口**

```java
package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.circle.entity.Circle;

public interface ICircleService extends IService<Circle> {

    void checkNameUnique(String name);

    void incrementMemberCount(String circleId);

    void decrementMemberCount(String circleId);
}
```

- [ ] **Step 4: 创建 CircleServiceImpl**

```java
package org.jeecg.modules.content.circle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.mapper.CircleMapper;
import org.jeecg.modules.content.circle.service.ICircleService;
import org.springframework.stereotype.Service;

@Service
public class CircleServiceImpl extends ServiceImpl<CircleMapper, Circle> implements ICircleService {

    @Override
    public void checkNameUnique(String name) {
        long count = count(new LambdaQueryWrapper<Circle>().eq(Circle::getName, name));
        if (count > 0) {
            throw new JeecgBootException("该圈子名称已存在，请修改");
        }
    }

    @Override
    public void incrementMemberCount(String circleId) {
        Circle circle = getById(circleId);
        if (circle.getMemberCount() >= circle.getMaxMemberCount()) {
            throw new JeecgBootException("圈子已满员，无法加入");
        }
        circle.setMemberCount(circle.getMemberCount() + 1);
        updateById(circle);
    }

    @Override
    public void decrementMemberCount(String circleId) {
        Circle circle = getById(circleId);
        if (circle.getMemberCount() > 0) {
            circle.setMemberCount(circle.getMemberCount() - 1);
            updateById(circle);
        }
    }
}
```

- [ ] **Step 5: 运行测试确认通过**

```bash
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.service.CircleServiceTest" -DfailIfNoTests=false
```

Expected: Tests run: 3, Failures: 0

- [ ] **Step 6: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/ICircleService.java
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/impl/CircleServiceImpl.java
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/service/CircleServiceTest.java
git commit -m "feat(circle): add CircleService with name check and member count"
```

---

## Task 8: Service 层 — CircleMemberService

**Files:**
- Create: `main/java/org/jeecg/modules/content/circle/service/ICircleMemberService.java`
- Create: `main/java/org/jeecg/modules/content/circle/service/impl/CircleMemberServiceImpl.java`
- Create: `test/java/org/jeecg/modules/content/circle/service/CircleMemberServiceTest.java`

- [ ] **Step 1: 编写 CircleMemberService 测试**

```java
package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.mapper.CircleMemberMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleMemberService")
class CircleMemberServiceTest {

    @Mock
    private CircleMemberMapper circleMemberMapper;

    @InjectMocks
    private CircleMemberServiceImpl circleMemberService;

    @Nested
    @DisplayName("checkAlreadyMember")
    class CheckAlreadyMember {

        @Test
        @DisplayName("already member - throws exception")
        void alreadyMember_throwsException() {
            when(circleMemberMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> circleMemberService.checkAlreadyMember("c_001", "u_001"));
            assertEquals("您已是圈子成员", ex.getMessage());
        }

        @Test
        @DisplayName("not member - passes")
        void notMember_passes() {
            when(circleMemberMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            assertDoesNotThrow(() -> circleMemberService.checkAlreadyMember("c_001", "u_001"));
        }
    }

    @Nested
    @DisplayName("checkNotMuted")
    class CheckNotMuted {

        @Test
        @DisplayName("muted and not expired - throws exception")
        void mutedAndNotExpired_throwsException() {
            CircleMember member = new CircleMember();
            member.setStatus(CircleMember.Status.MUTED);
            member.setMuteEndTime(LocalDateTime.now().plusHours(1));

            when(circleMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);

            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> circleMemberService.checkNotMuted("c_001", "u_001"));
            assertTrue(ex.getMessage().contains("您已被禁言"));
        }

        @Test
        @DisplayName("muted but expired - auto unmute and pass")
        void mutedButExpired_autoUnmuteAndPass() {
            CircleMember member = new CircleMember();
            member.setId("m_001");
            member.setStatus(CircleMember.Status.MUTED);
            member.setMuteEndTime(LocalDateTime.now().minusHours(1));

            when(circleMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);
            when(circleMemberMapper.updateById(any())).thenReturn(1);

            assertDoesNotThrow(() -> circleMemberService.checkNotMuted("c_001", "u_001"));
            verify(circleMemberMapper).updateById(any());
        }

        @Test
        @DisplayName("not muted - passes")
        void notMuted_passes() {
            CircleMember member = new CircleMember();
            member.setStatus(CircleMember.Status.ACTIVE);

            when(circleMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);
            assertDoesNotThrow(() -> circleMemberService.checkNotMuted("c_001", "u_001"));
        }
    }

    @Nested
    @DisplayName("checkPermission")
    class CheckPermission {

        @Test
        @DisplayName("moderator tries to change role - throws exception")
        void moderatorTriesToChangeRole_throwsException() {
            CircleMember operator = new CircleMember();
            operator.setRole(CircleMember.Role.MODERATOR);

            when(circleMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(operator);

            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> circleMemberService.checkCreatorPermission("c_001", "u_001"));
            assertEquals("权限不足，仅创建者可管理角色", ex.getMessage());
        }

        @Test
        @DisplayName("creator - passes")
        void creator_passes() {
            CircleMember operator = new CircleMember();
            operator.setRole(CircleMember.Role.CREATOR);

            when(circleMemberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(operator);
            assertDoesNotThrow(() -> circleMemberService.checkCreatorPermission("c_001", "u_001"));
        }
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.service.CircleMemberServiceTest" -DfailIfNoTests=false
```

Expected: COMPILATION ERROR

- [ ] **Step 3: 创建 ICircleMemberService 接口**

```java
package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.circle.entity.CircleMember;

public interface ICircleMemberService extends IService<CircleMember> {

    void checkAlreadyMember(String circleId, String userId);

    void checkNotMuted(String circleId, String userId);

    void checkCreatorPermission(String circleId, String operatorId);

    void checkModeratorManageable(String circleId, String targetUserId);
}
```

- [ ] **Step 4: 创建 CircleMemberServiceImpl**

```java
package org.jeecg.modules.content.circle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.mapper.CircleMemberMapper;
import org.jeecg.modules.content.circle.service.ICircleMemberService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CircleMemberServiceImpl extends ServiceImpl<CircleMemberMapper, CircleMember> implements ICircleMemberService {

    @Override
    public void checkAlreadyMember(String circleId, String userId) {
        long count = count(new LambdaQueryWrapper<CircleMember>()
                .eq(CircleMember::getCircleId, circleId)
                .eq(CircleMember::getUserId, userId)
                .ne(CircleMember::getStatus, CircleMember.Status.REMOVED));
        if (count > 0) {
            throw new JeecgBootException("您已是圈子成员");
        }
    }

    @Override
    public void checkNotMuted(String circleId, String userId) {
        CircleMember member = getOne(new LambdaQueryWrapper<CircleMember>()
                .eq(CircleMember::getCircleId, circleId)
                .eq(CircleMember::getUserId, userId));
        if (member == null) {
            return;
        }
        if (member.getStatus() == CircleMember.Status.MUTED) {
            if (member.getMuteEndTime() != null && member.getMuteEndTime().isBefore(LocalDateTime.now())) {
                member.setStatus(CircleMember.Status.ACTIVE);
                member.setMuteEndTime(null);
                updateById(member);
            } else {
                String endTime = member.getMuteEndTime() != null ? member.getMuteEndTime().toString() : "永久";
                throw new JeecgBootException("您已被禁言至 " + endTime);
            }
        }
    }

    @Override
    public void checkCreatorPermission(String circleId, String operatorId) {
        CircleMember operator = getOne(new LambdaQueryWrapper<CircleMember>()
                .eq(CircleMember::getCircleId, circleId)
                .eq(CircleMember::getUserId, operatorId));
        if (operator == null || operator.getRole() != CircleMember.Role.CREATOR) {
            throw new JeecgBootException("权限不足，仅创建者可管理角色");
        }
    }

    @Override
    public void checkModeratorManageable(String circleId, String targetUserId) {
        CircleMember target = getOne(new LambdaQueryWrapper<CircleMember>()
                .eq(CircleMember::getCircleId, circleId)
                .eq(CircleMember::getUserId, targetUserId));
        if (target != null && target.getRole() == CircleMember.Role.MODERATOR) {
            throw new JeecgBootException("权限不足，仅创建者可管理版主");
        }
    }
}
```

- [ ] **Step 5: 运行测试确认通过**

```bash
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.service.CircleMemberServiceTest" -DfailIfNoTests=false
```

Expected: Tests run: 6, Failures: 0

- [ ] **Step 6: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/ICircleMemberService.java
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/impl/CircleMemberServiceImpl.java
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/service/CircleMemberServiceTest.java
git commit -m "feat(circle): add CircleMemberService with permission checks"
```

---

## Task 9: Service 层 — CircleGovernanceLogService

**Files:**
- Create: `main/java/org/jeecg/modules/content/circle/service/ICircleGovernanceLogService.java`
- Create: `main/java/org/jeecg/modules/content/circle/service/impl/CircleGovernanceLogServiceImpl.java`
- Create: `test/java/org/jeecg/modules/content/circle/service/CircleGovernanceLogServiceTest.java`

- [ ] **Step 1: 编写 CircleGovernanceLogService 测试**

```java
package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.circle.entity.CircleGovernanceLog;
import org.jeecg.modules.content.circle.mapper.CircleGovernanceLogMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleGovernanceLogService")
class CircleGovernanceLogServiceTest {

    @Mock
    private CircleGovernanceLogMapper circleGovernanceLogMapper;

    @InjectMocks
    private CircleGovernanceLogServiceImpl governanceLogService;

    @Test
    @DisplayName("logMute - saves log")
    void logMute_savesLog() {
        when(circleGovernanceLogMapper.insert(any())).thenReturn(1);

        governanceLogService.logMute("c_001", "op_001", "target_001", "违规发言", "24h");

        verify(circleGovernanceLogMapper).insert(any());
    }

    @Test
    @DisplayName("logRoleChange - saves log")
    void logRoleChange_savesLog() {
        when(circleGovernanceLogMapper.insert(any())).thenReturn(1);

        governanceLogService.logRoleChange("c_001", "op_001", "target_001", "MEMBER", "MODERATOR");

        verify(circleGovernanceLogMapper).insert(any());
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.service.CircleGovernanceLogServiceTest" -DfailIfNoTests=false
```

Expected: COMPILATION ERROR

- [ ] **Step 3: 创建 ICircleGovernanceLogService 接口**

```java
package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.circle.entity.CircleGovernanceLog;

public interface ICircleGovernanceLogService extends IService<CircleGovernanceLog> {

    void logMute(String circleId, String operatorId, String targetUserId, String reason, String duration);

    void logUnmute(String circleId, String operatorId, String targetUserId);

    void logRemove(String circleId, String operatorId, String targetUserId, String reason);

    void logRoleChange(String circleId, String operatorId, String targetUserId, String fromRole, String toRole);
}
```

- [ ] **Step 4: 创建 CircleGovernanceLogServiceImpl**

```java
package org.jeecg.modules.content.circle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.circle.entity.CircleGovernanceLog;
import org.jeecg.modules.content.circle.mapper.CircleGovernanceLogMapper;
import org.jeecg.modules.content.circle.service.ICircleGovernanceLogService;
import org.springframework.stereotype.Service;

@Service
public class CircleGovernanceLogServiceImpl extends ServiceImpl<CircleGovernanceLogMapper, CircleGovernanceLog>
        implements ICircleGovernanceLogService {

    @Override
    public void logMute(String circleId, String operatorId, String targetUserId, String reason, String duration) {
        CircleGovernanceLog log = new CircleGovernanceLog();
        log.setCircleId(circleId);
        log.setOperatorId(operatorId);
        log.setTargetUserId(targetUserId);
        log.setAction(CircleGovernanceLog.Action.MUTE);
        log.setReason(reason);
        log.setDuration(duration);
        save(log);
    }

    @Override
    public void logUnmute(String circleId, String operatorId, String targetUserId) {
        CircleGovernanceLog log = new CircleGovernanceLog();
        log.setCircleId(circleId);
        log.setOperatorId(operatorId);
        log.setTargetUserId(targetUserId);
        log.setAction(CircleGovernanceLog.Action.UNMUTE);
        save(log);
    }

    @Override
    public void logRemove(String circleId, String operatorId, String targetUserId, String reason) {
        CircleGovernanceLog log = new CircleGovernanceLog();
        log.setCircleId(circleId);
        log.setOperatorId(operatorId);
        log.setTargetUserId(targetUserId);
        log.setAction(CircleGovernanceLog.Action.REMOVE);
        log.setReason(reason);
        save(log);
    }

    @Override
    public void logRoleChange(String circleId, String operatorId, String targetUserId, String fromRole, String toRole) {
        CircleGovernanceLog log = new CircleGovernanceLog();
        log.setCircleId(circleId);
        log.setOperatorId(operatorId);
        log.setTargetUserId(targetUserId);
        log.setAction(CircleGovernanceLog.Action.ROLE_CHANGE);
        log.setExtraDataJson("{\"from\":\"" + fromRole + "\",\"to\":\"" + toRole + "\"}");
        save(log);
    }
}
```

- [ ] **Step 5: 运行测试确认通过**

```bash
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.service.CircleGovernanceLogServiceTest" -DfailIfNoTests=false
```

Expected: Tests run: 2, Failures: 0

- [ ] **Step 6: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/ICircleGovernanceLogService.java
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/impl/CircleGovernanceLogServiceImpl.java
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/service/CircleGovernanceLogServiceTest.java
git commit -m "feat(circle): add CircleGovernanceLogService for audit trail"
```

---

## Task 10: Biz 层 — CircleBiz（圈子创建编排）

**Files:**
- Create: `main/java/org/jeecg/modules/content/circle/biz/ICircleBiz.java`
- Create: `main/java/org/jeecg/modules/content/circle/biz/CircleBizImpl.java`
- Create: `test/java/org/jeecg/modules/content/circle/biz/CircleBizTest.java`

- [ ] **Step 1: 编写 CircleBiz 测试**

```java
package org.jeecg.modules.content.circle.biz;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.req.create.CircleCreateReq;
import org.jeecg.modules.content.circle.service.ICircleMemberService;
import org.jeecg.modules.content.circle.service.ICircleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleBiz")
class CircleBizTest {

    @Mock
    private ICircleService circleService;

    @Mock
    private ICircleMemberService circleMemberService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private CircleBizImpl circleBiz;

    @Nested
    @DisplayName("createCircle")
    class CreateCircle {

        @Test
        @DisplayName("name exists - throws exception")
        void nameExists_throwsException() {
            CircleCreateReq req = new CircleCreateReq();
            req.setName("已存在的圈子");
            req.setDescription("简介");
            req.setPrivacyType("PUBLIC");
            req.setJoinType("DIRECT");

            doThrow(new JeecgBootException("该圈子名称已存在，请修改"))
                    .when(circleService).checkNameUnique("已存在的圈子");

            assertThrows(JeecgBootException.class, () -> circleBiz.createCircle(req, "u_001"));
        }

        @Test
        @DisplayName("valid request - creates circle and member")
        void validRequest_createsCircleAndMember() {
            CircleCreateReq req = new CircleCreateReq();
            req.setName("新圈子");
            req.setDescription("简介");
            req.setPrivacyType("PUBLIC");
            req.setJoinType("DIRECT");

            when(circleService.save(any())).thenReturn(true);
            when(circleMemberService.save(any())).thenReturn(true);

            circleBiz.createCircle(req, "u_001");

            verify(circleService).save(any());
            verify(circleMemberService).save(any());
        }

        @Test
        @DisplayName("password type - encodes password")
        void passwordType_encodesPassword() {
            CircleCreateReq req = new CircleCreateReq();
            req.setName("密码圈子");
            req.setDescription("简介");
            req.setPrivacyType("PASSWORD");
            req.setJoinType("PASSWORD");
            req.setPassword("secret123");

            when(passwordEncoder.encode("secret123")).thenReturn("$2a$encoded");
            when(circleService.save(any())).thenReturn(true);
            when(circleMemberService.save(any())).thenReturn(true);

            circleBiz.createCircle(req, "u_001");

            verify(passwordEncoder).encode("secret123");
        }
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.biz.CircleBizTest" -DfailIfNoTests=false
```

Expected: COMPILATION ERROR

- [ ] **Step 3: 创建 ICircleBiz 接口**

```java
package org.jeecg.modules.content.circle.biz;

import org.jeecg.modules.content.circle.req.create.CircleCreateReq;
import org.jeecg.modules.content.circle.req.update.CircleUpdateReq;
import org.jeecg.modules.content.circle.vo.CircleVO;

public interface ICircleBiz {

    CircleVO createCircle(CircleCreateReq req, String userId);

    void updateCircle(CircleUpdateReq req, String userId);
}
```

- [ ] **Step 4: 创建 CircleBizImpl**

```java
package org.jeecg.modules.content.circle.biz;

import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.req.create.CircleCreateReq;
import org.jeecg.modules.content.circle.req.update.CircleUpdateReq;
import org.jeecg.modules.content.circle.service.ICircleMemberService;
import org.jeecg.modules.content.circle.service.ICircleService;
import org.jeecg.modules.content.circle.vo.CircleVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CircleBizImpl implements ICircleBiz {

    @Resource
    private ICircleService circleService;

    @Resource
    private ICircleMemberService circleMemberService;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CircleVO createCircle(CircleCreateReq req, String userId) {
        // 1. 名称唯一性校验
        circleService.checkNameUnique(req.getName());

        // 2. 构建圈子实体
        Circle circle = new Circle();
        circle.setName(req.getName());
        circle.setDescription(req.getDescription());
        circle.setIconUrl(req.getIconUrl());
        circle.setCoverUrl(req.getCoverUrl());
        circle.setCategory(req.getCategory());
        circle.setPrivacyType(Circle.PrivacyType.valueOf(req.getPrivacyType()));
        circle.setJoinType(Circle.JoinType.valueOf(req.getJoinType()));
        circle.setCreatorId(userId);
        circle.setMemberCount(1);
        circle.setMaxMemberCount(10000);
        circle.setStatus(Circle.Status.ACTIVE);

        // 3. 密码保护处理
        if (circle.getPrivacyType() == Circle.PrivacyType.PASSWORD) {
            if (req.getPassword() == null || req.getPassword().isBlank()) {
                throw new JeecgBootException("密码保护圈子必须设置密码");
            }
            circle.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }

        // 4. 保存圈子
        circleService.save(circle);

        // 5. 创建者自动成为成员
        CircleMember member = new CircleMember();
        member.setCircleId(circle.getId());
        member.setUserId(userId);
        member.setRole(CircleMember.Role.CREATOR);
        member.setStatus(CircleMember.Status.ACTIVE);
        circleMemberService.save(member);

        // 6. 构建返回值
        CircleVO vo = new CircleVO();
        vo.setId(circle.getId());
        vo.setName(circle.getName());
        vo.setDescription(circle.getDescription());
        vo.setIconUrl(circle.getIconUrl());
        vo.setCoverUrl(circle.getCoverUrl());
        vo.setCategory(circle.getCategory());
        vo.setPrivacyType(circle.getPrivacyType().name());
        vo.setJoinType(circle.getJoinType().name());
        vo.setCreatorId(circle.getCreatorId());
        vo.setMemberCount(circle.getMemberCount());
        vo.setMaxMemberCount(circle.getMaxMemberCount());
        vo.setStatus(circle.getStatus().name());
        vo.setJoined(true);
        vo.setMyRole("CREATOR");
        vo.setCreateTime(circle.getCreateTime());
        return vo;
    }

    @Override
    @Transactional
    public void updateCircle(CircleUpdateReq req, String userId) {
        Circle circle = circleService.getById(req.getCircleId());
        if (circle == null) {
            throw new JeecgBootException("圈子不存在");
        }
        if (!circle.getCreatorId().equals(userId)) {
            throw new JeecgBootException("仅创建者可修改圈子信息");
        }

        if (req.getDescription() != null) {
            circle.setDescription(req.getDescription());
        }
        if (req.getIconUrl() != null) {
            circle.setIconUrl(req.getIconUrl());
        }
        if (req.getCoverUrl() != null) {
            circle.setCoverUrl(req.getCoverUrl());
        }
        if (req.getCategory() != null) {
            circle.setCategory(req.getCategory());
        }
        circleService.updateById(circle);
    }
}
```

- [ ] **Step 5: 运行测试确认通过**

```bash
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.biz.CircleBizTest" -DfailIfNoTests=false
```

Expected: Tests run: 3, Failures: 0

- [ ] **Step 6: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/biz/ICircleBiz.java
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/biz/CircleBizImpl.java
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/biz/CircleBizTest.java
git commit -m "feat(circle): add CircleBiz for circle creation orchestration"
```

---

## Task 11: Biz 层 — CircleMemberBiz（成员管理编排）

**Files:**
- Create: `main/java/org/jeecg/modules/content/circle/biz/ICircleMemberBiz.java`
- Create: `main/java/org/jeecg/modules/content/circle/biz/CircleMemberBizImpl.java`
- Create: `test/java/org/jeecg/modules/content/circle/biz/CircleMemberBizTest.java`

- [ ] **Step 1: 编写 CircleMemberBiz 测试**

```java
package org.jeecg.modules.content.circle.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.req.create.CircleJoinReq;
import org.jeecg.modules.content.circle.req.update.CircleMemberUpdateReq;
import org.jeecg.modules.content.circle.service.ICircleGovernanceLogService;
import org.jeecg.modules.content.circle.service.ICircleMemberService;
import org.jeecg.modules.content.circle.service.ICircleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleMemberBiz")
class CircleMemberBizTest {

    @Mock
    private ICircleService circleService;

    @Mock
    private ICircleMemberService circleMemberService;

    @Mock
    private ICircleGovernanceLogService governanceLogService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private CircleMemberBizImpl circleMemberBiz;

    @Nested
    @DisplayName("joinCircle")
    class JoinCircle {

        @Test
        @DisplayName("direct join - succeeds")
        void directJoin_succeeds() {
            Circle circle = new Circle();
            circle.setId("c_001");
            circle.setPrivacyType(Circle.PrivacyType.PUBLIC);
            circle.setJoinType(Circle.JoinType.DIRECT);

            when(circleService.getById("c_001")).thenReturn(circle);
            when(circleMemberService.save(any())).thenReturn(true);

            circleMemberBiz.joinCircle(new CircleJoinReq() {{ setCircleId("c_001"); }}, "u_001");

            verify(circleService).incrementMemberCount("c_001");
            verify(circleMemberService).save(any());
        }

        @Test
        @DisplayName("invite only - throws exception")
        void inviteOnly_throwsException() {
            Circle circle = new Circle();
            circle.setId("c_001");
            circle.setPrivacyType(Circle.PrivacyType.PRIVATE);
            circle.setJoinType(Circle.JoinType.INVITE);

            when(circleService.getById("c_001")).thenReturn(circle);

            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> circleMemberBiz.joinCircle(new CircleJoinReq() {{ setCircleId("c_001"); }}, "u_001"));
            assertEquals("该圈子仅限邀请加入", ex.getMessage());
        }

        @Test
        @DisplayName("password join wrong password - throws exception")
        void passwordJoinWrongPassword_throwsException() {
            Circle circle = new Circle();
            circle.setId("c_001");
            circle.setPrivacyType(Circle.PrivacyType.PASSWORD);
            circle.setJoinType(Circle.JoinType.PASSWORD);
            circle.setPasswordHash("$2a$encoded");

            CircleJoinReq req = new CircleJoinReq();
            req.setCircleId("c_001");
            req.setPassword("wrong");

            when(circleService.getById("c_001")).thenReturn(circle);
            when(passwordEncoder.matches("wrong", "$2a$encoded")).thenReturn(false);

            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> circleMemberBiz.joinCircle(req, "u_001"));
            assertEquals("密码错误", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("muteMember")
    class MuteMember {

        @Test
        @DisplayName("moderator tries to mute moderator - throws exception")
        void moderatorTriesToMuteModerator_throwsException() {
            CircleMemberUpdateReq req = new CircleMemberUpdateReq();
            req.setCircleId("c_001");
            req.setTargetUserId("target_001");
            req.setMuteDuration("24h");
            req.setReason("违规");

            CircleMember operator = new CircleMember();
            operator.setRole(CircleMember.Role.MODERATOR);

            CircleMember target = new CircleMember();
            target.setRole(CircleMember.Role.MODERATOR);

            when(circleMemberService.getOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(operator)
                    .thenReturn(target);

            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> circleMemberBiz.muteMember(req, "op_001"));
            assertEquals("权限不足，仅创建者可管理版主", ex.getMessage());
        }
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.biz.CircleMemberBizTest" -DfailIfNoTests=false
```

Expected: COMPILATION ERROR

- [ ] **Step 3: 创建 ICircleMemberBiz 接口**

```java
package org.jeecg.modules.content.circle.biz;

import org.jeecg.modules.content.circle.req.create.CircleJoinReq;
import org.jeecg.modules.content.circle.req.update.CircleMemberUpdateReq;

public interface ICircleMemberBiz {

    void joinCircle(CircleJoinReq req, String userId);

    void leaveCircle(String circleId, String userId);

    void changeRole(CircleMemberUpdateReq req, String operatorId);

    void muteMember(CircleMemberUpdateReq req, String operatorId);

    void unmuteMember(String circleId, String targetUserId, String operatorId);

    void removeMember(CircleMemberUpdateReq req, String operatorId);
}
```

- [ ] **Step 4: 创建 CircleMemberBizImpl**

```java
package org.jeecg.modules.content.circle.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.req.create.CircleJoinReq;
import org.jeecg.modules.content.circle.req.update.CircleMemberUpdateReq;
import org.jeecg.modules.content.circle.service.ICircleGovernanceLogService;
import org.jeecg.modules.content.circle.service.ICircleMemberService;
import org.jeecg.modules.content.circle.service.ICircleService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CircleMemberBizImpl implements ICircleMemberBiz {

    @Resource
    private ICircleService circleService;

    @Resource
    private ICircleMemberService circleMemberService;

    @Resource
    private ICircleGovernanceLogService governanceLogService;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void joinCircle(CircleJoinReq req, String userId) {
        Circle circle = circleService.getById(req.getCircleId());
        if (circle == null) {
            throw new JeecgBootException("圈子不存在");
        }

        // 检查是否已是成员
        circleMemberService.checkAlreadyMember(req.getCircleId(), userId);

        // 检查禁言状态（已移除的成员可重新加入）
        CircleMember existing = circleMemberService.getOne(new LambdaQueryWrapper<CircleMember>()
                .eq(CircleMember::getCircleId, req.getCircleId())
                .eq(CircleMember::getUserId, userId));
        if (existing != null && existing.getStatus() == CircleMember.Status.MUTED) {
            throw new JeecgBootException("您已被禁言，无法加入");
        }

        // 根据加入方式处理
        switch (circle.getJoinType()) {
            case DIRECT:
                break; // 直接加入
            case APPROVAL:
                throw new JeecgBootException("申请已提交，请等待审核");
            case INVITE:
                throw new JeecgBootException("该圈子仅限邀请加入");
            case PASSWORD:
                if (req.getPassword() == null || !passwordEncoder.matches(req.getPassword(), circle.getPasswordHash())) {
                    throw new JeecgBootException("密码错误");
                }
                break;
        }

        // 增加成员数
        circleService.incrementMemberCount(req.getCircleId());

        // 创建成员记录
        CircleMember member = new CircleMember();
        member.setCircleId(req.getCircleId());
        member.setUserId(userId);
        member.setRole(CircleMember.Role.MEMBER);
        member.setStatus(CircleMember.Status.ACTIVE);
        if (existing != null) {
            existing.setStatus(CircleMember.Status.ACTIVE);
            existing.setRole(CircleMember.Role.MEMBER);
            circleMemberService.updateById(existing);
        } else {
            circleMemberService.save(member);
        }
    }

    @Override
    @Transactional
    public void leaveCircle(String circleId, String userId) {
        Circle circle = circleService.getById(circleId);
        if (circle == null) {
            throw new JeecgBootException("圈子不存在");
        }
        if (circle.getCreatorId().equals(userId)) {
            throw new JeecgBootException("创建者不可退出圈子，请先转让或解散圈子");
        }

        CircleMember member = circleMemberService.getOne(new LambdaQueryWrapper<CircleMember>()
                .eq(CircleMember::getCircleId, circleId)
                .eq(CircleMember::getUserId, userId));
        if (member == null || member.getStatus() == CircleMember.Status.REMOVED) {
            throw new JeecgBootException("您不是该圈子成员");
        }

        member.setStatus(CircleMember.Status.REMOVED);
        circleMemberService.updateById(member);
        circleService.decrementMemberCount(circleId);
    }

    @Override
    @Transactional
    public void changeRole(CircleMemberUpdateReq req, String operatorId) {
        circleMemberService.checkCreatorPermission(req.getCircleId(), operatorId);

        CircleMember target = circleMemberService.getOne(new LambdaQueryWrapper<CircleMember>()
                .eq(CircleMember::getCircleId, req.getCircleId())
                .eq(CircleMember::getUserId, req.getTargetUserId()));
        if (target == null) {
            throw new JeecgBootException("目标用户不是圈子成员");
        }
        if (target.getRole() == CircleMember.Role.CREATOR) {
            throw new JeecgBootException("创建者角色不可变更");
        }

        String fromRole = target.getRole().name();
        target.setRole(CircleMember.Role.valueOf(req.getTargetRole()));
        circleMemberService.updateById(target);

        governanceLogService.logRoleChange(req.getCircleId(), operatorId, req.getTargetUserId(), fromRole, req.getTargetRole());
    }

    @Override
    @Transactional
    public void muteMember(CircleMemberUpdateReq req, String operatorId) {
        CircleMember operator = circleMemberService.getOne(new LambdaQueryWrapper<CircleMember>()
                .eq(CircleMember::getCircleId, req.getCircleId())
                .eq(CircleMember::getUserId, operatorId));
        if (operator == null) {
            throw new JeecgBootException("您不是该圈子成员");
        }

        CircleMember target = circleMemberService.getOne(new LambdaQueryWrapper<CircleMember>()
                .eq(CircleMember::getCircleId, req.getCircleId())
                .eq(CircleMember::getUserId, req.getTargetUserId()));
        if (target == null || target.getStatus() == CircleMember.Status.REMOVED) {
            throw new JeecgBootException("目标用户不是圈子成员");
        }

        // 版主不可禁言其他版主
        if (operator.getRole() == CircleMember.Role.MODERATOR && target.getRole() == CircleMember.Role.MODERATOR) {
            throw new JeecgBootException("权限不足，仅创建者可管理版主");
        }

        target.setStatus(CircleMember.Status.MUTED);
        target.setMuteEndTime(calculateMuteEndTime(req.getMuteDuration()));
        circleMemberService.updateById(target);

        governanceLogService.logMute(req.getCircleId(), operatorId, req.getTargetUserId(), req.getReason(), req.getMuteDuration());
    }

    @Override
    @Transactional
    public void unmuteMember(String circleId, String targetUserId, String operatorId) {
        CircleMember target = circleMemberService.getOne(new LambdaQueryWrapper<CircleMember>()
                .eq(CircleMember::getCircleId, circleId)
                .eq(CircleMember::getUserId, targetUserId));
        if (target == null || target.getStatus() != CircleMember.Status.MUTED) {
            throw new JeecgBootException("该成员未被禁言");
        }

        target.setStatus(CircleMember.Status.ACTIVE);
        target.setMuteEndTime(null);
        circleMemberService.updateById(target);

        governanceLogService.logUnmute(circleId, operatorId, targetUserId);
    }

    @Override
    @Transactional
    public void removeMember(CircleMemberUpdateReq req, String operatorId) {
        CircleMember operator = circleMemberService.getOne(new LambdaQueryWrapper<CircleMember>()
                .eq(CircleMember::getCircleId, req.getCircleId())
                .eq(CircleMember::getUserId, operatorId));
        if (operator == null) {
            throw new JeecgBootException("您不是该圈子成员");
        }

        CircleMember target = circleMemberService.getOne(new LambdaQueryWrapper<CircleMember>()
                .eq(CircleMember::getCircleId, req.getCircleId())
                .eq(CircleMember::getUserId, req.getTargetUserId()));
        if (target == null || target.getStatus() == CircleMember.Status.REMOVED) {
            throw new JeecgBootException("目标用户不是圈子成员");
        }

        if (operator.getRole() == CircleMember.Role.MODERATOR && target.getRole() == CircleMember.Role.MODERATOR) {
            throw new JeecgBootException("权限不足，仅创建者可管理版主");
        }

        target.setStatus(CircleMember.Status.REMOVED);
        circleMemberService.updateById(target);
        circleService.decrementMemberCount(req.getCircleId());

        governanceLogService.logRemove(req.getCircleId(), operatorId, req.getTargetUserId(), req.getReason());
    }

    private LocalDateTime calculateMuteEndTime(String duration) {
        if (duration == null || "PERMANENT".equals(duration)) {
            return null; // 永久禁言
        }
        return switch (duration) {
            case "1h" -> LocalDateTime.now().plusHours(1);
            case "24h" -> LocalDateTime.now().plusHours(24);
            case "7d" -> LocalDateTime.now().plusDays(7);
            default -> throw new JeecgBootException("无效的禁言时长: " + duration);
        };
    }
}
```

- [ ] **Step 5: 运行测试确认通过**

```bash
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.biz.CircleMemberBizTest" -DfailIfNoTests=false
```

Expected: Tests run: 3, Failures: 0

- [ ] **Step 6: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/biz/ICircleMemberBiz.java
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/biz/CircleMemberBizImpl.java
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/biz/CircleMemberBizTest.java
git commit -m "feat(circle): add CircleMemberBiz for member management orchestration"
```

---

## Task 12: Controller 层 — CircleController

**Files:**
- Create: `main/java/org/jeecg/modules/content/circle/controller/CircleController.java`
- Create: `test/java/org/jeecg/modules/content/circle/controller/CircleControllerWebMvcTest.java`

- [ ] **Step 1: 编写 CircleController WebMvcTest**

```java
package org.jeecg.modules.content.circle.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.biz.ICircleBiz;
import org.jeecg.modules.content.circle.biz.ICircleMemberBiz;
import org.jeecg.modules.content.circle.req.create.CircleCreateReq;
import org.jeecg.modules.content.circle.req.update.CircleUpdateReq;
import org.jeecg.modules.content.circle.vo.CircleVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleController WebMvc")
class CircleControllerWebMvcTest {

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(JeecgBootException.class)
        Result<?> handleJeecgBootException(JeecgBootException e) {
            return Result.error(e.getErrCode(), e.getMessage());
        }
    }

    private MockMvc mockMvc;

    @Mock
    private ICircleBiz circleBiz;

    @Mock
    private ICircleMemberBiz circleMemberBiz;

    @InjectMocks
    private CircleController controller;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .setControllerAdvice(new TestExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("createCircle")
    class CreateCircle {

        @Test
        @DisplayName("valid request - returns success")
        void validRequest_returnsSuccess() throws Exception {
            CircleVO vo = new CircleVO();
            vo.setId("c_001");
            vo.setName("测试圈子");

            when(circleBiz.createCircle(any(), eq("u_001"))).thenReturn(vo);

            mockMvc.perform(post("/content/circle/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"测试圈子","description":"简介","privacyType":"PUBLIC","joinType":"DIRECT"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result.id").value("c_001"));
        }

        @Test
        @DisplayName("blank name - returns 400")
        void blankName_returns400() throws Exception {
            mockMvc.perform(post("/content/circle/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"","description":"简介","privacyType":"PUBLIC","joinType":"DIRECT"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("name exists - returns business error")
        void nameExists_returnsBusinessError() throws Exception {
            when(circleBiz.createCircle(any(), eq("u_001")))
                    .thenThrow(new JeecgBootException("该圈子名称已存在，请修改"));

            mockMvc.perform(post("/content/circle/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"已存在","description":"简介","privacyType":"PUBLIC","joinType":"DIRECT"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("该圈子名称已存在，请修改"));
        }
    }

    @Nested
    @DisplayName("updateCircle")
    class UpdateCircle {

        @Test
        @DisplayName("valid request - returns success")
        void validRequest_returnsSuccess() throws Exception {
            mockMvc.perform(put("/content/circle/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"circleId":"c_001","description":"新简介"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.controller.CircleControllerWebMvcTest" -DfailIfNoTests=false
```

Expected: COMPILATION ERROR

- [ ] **Step 3: 创建 CircleController**

```java
package org.jeecg.modules.content.circle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.circle.biz.ICircleBiz;
import org.jeecg.modules.content.circle.biz.ICircleMemberBiz;
import org.jeecg.modules.content.circle.req.create.CircleCreateReq;
import org.jeecg.modules.content.circle.req.create.CircleJoinReq;
import org.jeecg.modules.content.circle.req.update.CircleUpdateReq;
import org.jeecg.modules.content.circle.vo.CircleVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "圈子管理", description = "圈子创建、更新、查询等接口")
@Validated
@RestController
@RequestMapping("/content/circle")
public class CircleController {

    @Resource
    private ICircleBiz circleBiz;

    @Resource
    private ICircleMemberBiz circleMemberBiz;

    @Operation(summary = "创建圈子")
    @PostMapping("/create")
    public Result<CircleVO> createCircle(@Valid @RequestBody CircleCreateReq req) {
        String userId = SecureUtil.currentUser().getId();
        return Result.OK(circleBiz.createCircle(req, userId));
    }

    @Operation(summary = "更新圈子信息")
    @PutMapping("/update")
    public Result<String> updateCircle(@Valid @RequestBody CircleUpdateReq req) {
        String userId = SecureUtil.currentUser().getId();
        circleBiz.updateCircle(req, userId);
        return Result.OK("更新成功");
    }

    @Operation(summary = "加入圈子")
    @PostMapping("/join")
    public Result<String> joinCircle(@Valid @RequestBody CircleJoinReq req) {
        String userId = SecureUtil.currentUser().getId();
        circleMemberBiz.joinCircle(req, userId);
        return Result.OK("加入成功");
    }

    @Operation(summary = "退出圈子")
    @PostMapping("/leave")
    public Result<String> leaveCircle(@RequestParam String circleId) {
        String userId = SecureUtil.currentUser().getId();
        circleMemberBiz.leaveCircle(circleId, userId);
        return Result.OK("退出成功");
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

```bash
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.controller.CircleControllerWebMvcTest" -DfailIfNoTests=false
```

Expected: Tests run: 4, Failures: 0

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleController.java
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/controller/CircleControllerWebMvcTest.java
git commit -m "feat(circle): add CircleController with create/update/join/leave APIs"
```

---

## Task 13: Controller 层 — CircleMemberController

**Files:**
- Create: `main/java/org/jeecg/modules/content/circle/controller/CircleMemberController.java`
- Create: `test/java/org/jeecg/modules/content/circle/controller/CircleMemberControllerWebMvcTest.java`

- [ ] **Step 1: 编写 CircleMemberController WebMvcTest**

```java
package org.jeecg.modules.content.circle.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.biz.ICircleMemberBiz;
import org.jeecg.modules.content.circle.req.update.CircleMemberUpdateReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleMemberController WebMvc")
class CircleMemberControllerWebMvcTest {

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(JeecgBootException.class)
        Result<?> handleJeecgBootException(JeecgBootException e) {
            return Result.error(e.getErrCode(), e.getMessage());
        }
    }

    private MockMvc mockMvc;

    @Mock
    private ICircleMemberBiz circleMemberBiz;

    @InjectMocks
    private CircleMemberController controller;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .setControllerAdvice(new TestExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("changeRole")
    class ChangeRole {

        @Test
        @DisplayName("valid request - returns success")
        void validRequest_returnsSuccess() throws Exception {
            mockMvc.perform(post("/content/circle/member/change-role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"circleId":"c_001","targetUserId":"u_002","targetRole":"MODERATOR"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(circleMemberBiz).changeRole(any(), eq("u_001"));
        }

        @Test
        @DisplayName("blank targetUserId - returns 400")
        void blankTargetUserId_returns400() throws Exception {
            mockMvc.perform(post("/content/circle/member/change-role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"circleId":"c_001","targetUserId":"","targetRole":"MODERATOR"}
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("muteMember")
    class MuteMember {

        @Test
        @DisplayName("valid request - returns success")
        void validRequest_returnsSuccess() throws Exception {
            mockMvc.perform(post("/content/circle/member/mute")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"circleId":"c_001","targetUserId":"u_002","muteDuration":"24h","reason":"违规发言"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("removeMember")
    class RemoveMember {

        @Test
        @DisplayName("valid request - returns success")
        void validRequest_returnsSuccess() throws Exception {
            mockMvc.perform(post("/content/circle/member/remove")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"circleId":"c_001","targetUserId":"u_002","reason":"严重违规"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("not member - returns business error")
        void notMember_returnsBusinessError() throws Exception {
            doThrow(new JeecgBootException("目标用户不是圈子成员"))
                    .when(circleMemberBiz).removeMember(any(), eq("u_001"));

            mockMvc.perform(post("/content/circle/member/remove")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"circleId":"c_001","targetUserId":"u_999"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("目标用户不是圈子成员"));
        }
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.controller.CircleMemberControllerWebMvcTest" -DfailIfNoTests=false
```

Expected: COMPILATION ERROR

- [ ] **Step 3: 创建 CircleMemberController**

```java
package org.jeecg.modules.content.circle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.circle.biz.ICircleMemberBiz;
import org.jeecg.modules.content.circle.req.update.CircleMemberUpdateReq;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "圈子成员管理", description = "成员角色变更、禁言、移除等接口")
@Validated
@RestController
@RequestMapping("/content/circle/member")
public class CircleMemberController {

    @Resource
    private ICircleMemberBiz circleMemberBiz;

    @Operation(summary = "变更成员角色")
    @PostMapping("/change-role")
    public Result<String> changeRole(@Valid @RequestBody CircleMemberUpdateReq req) {
        String operatorId = SecureUtil.currentUser().getId();
        circleMemberBiz.changeRole(req, operatorId);
        return Result.OK("角色变更成功");
    }

    @Operation(summary = "禁言成员")
    @PostMapping("/mute")
    public Result<String> muteMember(@Valid @RequestBody CircleMemberUpdateReq req) {
        String operatorId = SecureUtil.currentUser().getId();
        circleMemberBiz.muteMember(req, operatorId);
        return Result.OK("禁言成功");
    }

    @Operation(summary = "解除禁言")
    @PostMapping("/unmute")
    public Result<String> unmuteMember(@RequestParam String circleId, @RequestParam String targetUserId) {
        String operatorId = SecureUtil.currentUser().getId();
        circleMemberBiz.unmuteMember(circleId, targetUserId, operatorId);
        return Result.OK("解除禁言成功");
    }

    @Operation(summary = "移除成员")
    @PostMapping("/remove")
    public Result<String> removeMember(@Valid @RequestBody CircleMemberUpdateReq req) {
        String operatorId = SecureUtil.currentUser().getId();
        circleMemberBiz.removeMember(req, operatorId);
        return Result.OK("移除成功");
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

```bash
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.controller.CircleMemberControllerWebMvcTest" -DfailIfNoTests=false
```

Expected: Tests run: 5, Failures: 0

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleMemberController.java
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/controller/CircleMemberControllerWebMvcTest.java
git commit -m "feat(circle): add CircleMemberController for role/mute/remove APIs"
```

---

## Task 14: Controller 层 — CircleSearchController

**Files:**
- Create: `main/java/org/jeecg/modules/content/circle/controller/CircleSearchController.java`
- Create: `test/java/org/jeecg/modules/content/circle/controller/CircleSearchControllerWebMvcTest.java`

- [ ] **Step 1: 编写 CircleSearchController WebMvcTest**

```java
package org.jeecg.modules.content.circle.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.req.query.CircleSearchReq;
import org.jeecg.modules.content.circle.service.ICircleService;
import org.jeecg.modules.content.circle.vo.CircleSearchResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleSearchController WebMvc")
class CircleSearchControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private ICircleService circleService;

    @InjectMocks
    private CircleSearchController controller;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .build();
    }

    @Nested
    @DisplayName("search")
    class Search {

        @Test
        @DisplayName("keyword matches - returns results")
        void keywordMatches_returnsResults() throws Exception {
            Circle circle = new Circle();
            circle.setId("c_001");
            circle.setName("Java技术圈");
            circle.setDescription("Java技术交流");
            circle.setIconUrl("http://icon.png");
            circle.setMemberCount(100);

            when(circleService.page(any(), any())).thenReturn(new Page<Circle>().setRecords(List.of(circle)));

            mockMvc.perform(get("/content/circle/search")
                            .param("keyword", "Java"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result[0].id").value("c_001"))
                    .andExpect(jsonPath("$.result[0].name").value("Java技术圈"));
        }

        @Test
        @DisplayName("no results - returns empty list")
        void noResults_returnsEmptyList() throws Exception {
            when(circleService.page(any(), any())).thenReturn(new Page<Circle>().setRecords(List.of()));

            mockMvc.perform(get("/content/circle/search")
                            .param("keyword", "不存在的关键词"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result").isEmpty());
        }
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.controller.CircleSearchControllerWebMvcTest" -DfailIfNoTests=false
```

Expected: COMPILATION ERROR

- [ ] **Step 3: 创建 CircleSearchController**

```java
package org.jeecg.modules.content.circle.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.req.query.CircleSearchReq;
import org.jeecg.modules.content.circle.service.ICircleService;
import org.jeecg.modules.content.circle.vo.CircleSearchResultVO;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "圈子搜索", description = "圈子搜索接口")
@Validated
@RestController
@RequestMapping("/content/circle")
public class CircleSearchController {

    @Resource
    private ICircleService circleService;

    @Operation(summary = "搜索圈子")
    @GetMapping("/search")
    public Result<List<CircleSearchResultVO>> search(CircleSearchReq req) {
        LambdaQueryWrapper<Circle> wrapper = new LambdaQueryWrapper<Circle>()
                .eq(Circle::getStatus, Circle.Status.ACTIVE)
                .eq(Circle::getPrivacyType, Circle.PrivacyType.PUBLIC);

        if (StringUtils.hasText(req.getKeyword())) {
            wrapper.and(w -> w
                    .like(Circle::getName, req.getKeyword())
                    .or()
                    .like(Circle::getDescription, req.getKeyword()));
        }

        wrapper.orderByDesc(Circle::getMemberCount);

        Page<Circle> page = new Page<>(req.getPageNum(), req.getPageSize());
        circleService.page(page, wrapper);

        List<CircleSearchResultVO> results = page.getRecords().stream().map(c -> {
            CircleSearchResultVO vo = new CircleSearchResultVO();
            vo.setId(c.getId());
            vo.setName(c.getName());
            vo.setIconUrl(c.getIconUrl());
            vo.setDescription(c.getDescription());
            vo.setMemberCount(c.getMemberCount());
            vo.setJoined(false); // 需要结合当前用户判断，MVP 阶段默认 false
            return vo;
        }).toList();

        return Result.OK(results);
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

```bash
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.controller.CircleSearchControllerWebMvcTest" -DfailIfNoTests=false
```

Expected: Tests run: 2, Failures: 0

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleSearchController.java
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/controller/CircleSearchControllerWebMvcTest.java
git commit -m "feat(circle): add CircleSearchController with keyword search API"
```

---

## Task 15: 全量测试验证

- [ ] **Step 1: 运行所有 circle 模块测试**

```bash
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass
mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.**" -DfailIfNoTests=false
```

Expected: All tests pass

- [ ] **Step 2: 编译验证**

```bash
mvn compile -pl jeecg-boot/jeecg-boot-module/jeecg-module-content
```

Expected: BUILD SUCCESS

- [ ] **Step 3: Final Commit**

```bash
git add -A
git commit -m "feat(circle): complete circle core MVP - creation, member management, search"
```
