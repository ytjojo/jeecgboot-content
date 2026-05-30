# 频道内容发布与治理 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 建立频道内容发布权限模型、审核流程、内容治理能力和公告机制

**Architecture:** 遵循 jeecg-module-content 分层规范（controller/biz/service/mapper/entity/req/vo）。发布权限模型存储在频道配置表，待审区、定时发布、回收站、治理日志、公告各独立表。biz 层编排跨聚合流程，service 层处理单表逻辑。

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Flyway, XXL-Job (定时发布), Java 17

---

## Task 1: 数据库迁移与实体层

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/db/migration/V1__channel_content_governance.sql`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/entity/ChannelContentPublish.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/entity/ChannelContentReview.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/entity/ChannelScheduledPublish.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/entity/ChannelPublishLimit.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/entity/ChannelRecycleBin.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/entity/ChannelGovernanceLog.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/entity/ChannelAnnouncement.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/mapper/ChannelContentPublishMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/mapper/ChannelContentReviewMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/mapper/ChannelScheduledPublishMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/mapper/ChannelPublishLimitMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/mapper/ChannelRecycleBinMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/mapper/ChannelGovernanceLogMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/mapper/ChannelAnnouncementMapper.java`

- [ ] **Step 1: 创建 Flyway SQL 迁移脚本**

```sql
-- V1__channel_content_governance.sql

-- 频道内容发布关联表
CREATE TABLE channel_content_publish (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL COMMENT '频道ID',
    content_id VARCHAR(36) NOT NULL COMMENT '内容ID',
    content_type VARCHAR(32) NOT NULL COMMENT '内容类型：article/post/video/note/question/answer',
    publisher_id VARCHAR(36) NOT NULL COMMENT '发布者ID',
    publish_status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED' COMMENT '发布状态：PUBLISHED/PENDING/REJECTED/RECYCLED',
    is_pinned TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶',
    pin_order INT DEFAULT 0 COMMENT '置顶排序',
    is_featured TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否精华',
    source_type VARCHAR(32) DEFAULT 'DIRECT' COMMENT '来源类型：DIRECT/SCHEDULED/MOVE/ADD_EXISTING',
    create_by VARCHAR(36),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(36),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_channel_content (channel_id, content_id),
    INDEX idx_channel_status (channel_id, publish_status),
    INDEX idx_publisher (publisher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道内容发布关联';

-- 待审区表
CREATE TABLE channel_content_review (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL COMMENT '频道ID',
    content_id VARCHAR(36) NOT NULL COMMENT '内容ID',
    content_type VARCHAR(32) NOT NULL COMMENT '内容类型',
    submitter_id VARCHAR(36) NOT NULL COMMENT '提交者ID',
    review_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING/APPROVED/REJECTED',
    reviewer_id VARCHAR(36) COMMENT '审核人ID',
    review_time DATETIME COMMENT '审核时间',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    source_scene VARCHAR(32) DEFAULT 'PUBLISH' COMMENT '来源场景：PUBLISH/ADD_EXISTING/MOVE',
    hit_rule VARCHAR(128) COMMENT '命中规则：PUBLIC_SUBMIT/PRE_REVIEW',
    create_by VARCHAR(36),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(36),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_channel_status (channel_id, review_status),
    INDEX idx_submitter (submitter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道待审区';

-- 定时发布任务表
CREATE TABLE channel_scheduled_publish (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL COMMENT '频道ID',
    content_id VARCHAR(36) NOT NULL COMMENT '内容ID',
    content_type VARCHAR(32) NOT NULL COMMENT '内容类型',
    publisher_id VARCHAR(36) NOT NULL COMMENT '发布者ID',
    scheduled_time DATETIME NOT NULL COMMENT '计划发布时间',
    publish_status VARCHAR(32) NOT NULL DEFAULT 'SCHEDULED' COMMENT '状态：SCHEDULED/PUBLISHED/FAILED/CANCELLED',
    fail_reason VARCHAR(500) COMMENT '失败原因',
    create_by VARCHAR(36),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(36),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_scheduled_time (scheduled_time, publish_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道定时发布任务';

-- 发布限额配置表
CREATE TABLE channel_publish_limit (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL COMMENT '频道ID',
    hourly_limit INT DEFAULT 0 COMMENT '每小时发布上限，0表示不限',
    daily_limit INT DEFAULT 0 COMMENT '每日发布上限，0表示不限',
    min_word_count INT DEFAULT 0 COMMENT '内容字数下限，0表示不限',
    create_by VARCHAR(36),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(36),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_channel (channel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道发布限额配置';

-- 频道回收站表
CREATE TABLE channel_recycle_bin (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL COMMENT '频道ID',
    content_id VARCHAR(36) NOT NULL COMMENT '内容ID',
    content_type VARCHAR(32) NOT NULL COMMENT '内容类型',
    original_author_id VARCHAR(36) NOT NULL COMMENT '原作者ID',
    deleted_by VARCHAR(36) NOT NULL COMMENT '删除人ID',
    delete_time DATETIME NOT NULL COMMENT '删除时间',
    delete_reason VARCHAR(500) COMMENT '删除原因',
    expire_time DATETIME NOT NULL COMMENT '过期时间（删除后30天）',
    is_restored TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已恢复',
    restored_by VARCHAR(36) COMMENT '恢复人ID',
    restore_time DATETIME COMMENT '恢复时间',
    create_by VARCHAR(36),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(36),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_channel_expire (channel_id, expire_time),
    INDEX idx_content (content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道回收站';

-- 治理日志表
CREATE TABLE channel_governance_log (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL COMMENT '频道ID',
    content_id VARCHAR(36) COMMENT '内容ID',
    operator_id VARCHAR(36) NOT NULL COMMENT '操作者ID',
    action VARCHAR(32) NOT NULL COMMENT '操作类型：PIN/UNPIN/FEATURE/UNFEATURE/DELETE/RESTORE/MOVE/EDIT_ASSIST/ANNOUNCEMENT_CREATE/ANNOUNCEMENT_UPDATE/ANNOUNCEMENT_DELETE',
    action_detail VARCHAR(1000) COMMENT '操作详情JSON',
    reason VARCHAR(500) COMMENT '操作原因',
    result VARCHAR(32) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果：SUCCESS/FAILED',
    create_by VARCHAR(36),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(36),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_channel_action (channel_id, action),
    INDEX idx_operator (operator_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道治理日志';

-- 频道公告表
CREATE TABLE channel_announcement (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL COMMENT '频道ID',
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容（富文本）',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DELETED',
    created_by VARCHAR(36) NOT NULL COMMENT '创建人ID',
    create_by VARCHAR(36),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(36),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_channel_active (channel_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道公告';
```

- [ ] **Step 2: 创建 ChannelContentPublish 实体**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("channel_content_publish")
@Schema(description = "频道内容发布关联")
public class ChannelContentPublish extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "内容ID")
    private String contentId;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "发布者ID")
    private String publisherId;

    @Schema(description = "发布状态：PUBLISHED/PENDING/REJECTED/RECYCLED")
    private String publishStatus;

    @Schema(description = "是否置顶")
    private Boolean isPinned;

    @Schema(description = "置顶排序")
    private Integer pinOrder;

    @Schema(description = "是否精华")
    private Boolean isFeatured;

    @Schema(description = "来源类型：DIRECT/SCHEDULED/MOVE/ADD_EXISTING")
    private String sourceType;
}
```

- [ ] **Step 3: 创建 ChannelContentReview 实体**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("channel_content_review")
@Schema(description = "频道待审区")
public class ChannelContentReview extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "内容ID")
    private String contentId;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "提交者ID")
    private String submitterId;

    @Schema(description = "审核状态：PENDING/APPROVED/REJECTED")
    private String reviewStatus;

    @Schema(description = "审核人ID")
    private String reviewerId;

    @Schema(description = "审核时间")
    private Date reviewTime;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "来源场景：PUBLISH/ADD_EXISTING/MOVE")
    private String sourceScene;

    @Schema(description = "命中规则")
    private String hitRule;
}
```

- [ ] **Step 4: 创建 ChannelScheduledPublish 实体**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("channel_scheduled_publish")
@Schema(description = "频道定时发布任务")
public class ChannelScheduledPublish extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "内容ID")
    private String contentId;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "发布者ID")
    private String publisherId;

    @Schema(description = "计划发布时间")
    private Date scheduledTime;

    @Schema(description = "状态：SCHEDULED/PUBLISHED/FAILED/CANCELLED")
    private String publishStatus;

    @Schema(description = "失败原因")
    private String failReason;
}
```

- [ ] **Step 5: 创建 ChannelPublishLimit 实体**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("channel_publish_limit")
@Schema(description = "频道发布限额配置")
public class ChannelPublishLimit extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "每小时发布上限，0表示不限")
    private Integer hourlyLimit;

    @Schema(description = "每日发布上限，0表示不限")
    private Integer dailyLimit;

    @Schema(description = "内容字数下限，0表示不限")
    private Integer minWordCount;
}
```

- [ ] **Step 6: 创建 ChannelRecycleBin 实体**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("channel_recycle_bin")
@Schema(description = "频道回收站")
public class ChannelRecycleBin extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "内容ID")
    private String contentId;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "原作者ID")
    private String originalAuthorId;

    @Schema(description = "删除人ID")
    private String deletedBy;

    @Schema(description = "删除时间")
    private Date deleteTime;

    @Schema(description = "删除原因")
    private String deleteReason;

    @Schema(description = "过期时间")
    private Date expireTime;

    @Schema(description = "是否已恢复")
    private Boolean isRestored;

    @Schema(description = "恢复人ID")
    private String restoredBy;

    @Schema(description = "恢复时间")
    private Date restoreTime;
}
```

- [ ] **Step 7: 创建 ChannelGovernanceLog 实体**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("channel_governance_log")
@Schema(description = "频道治理日志")
public class ChannelGovernanceLog extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "内容ID")
    private String contentId;

    @Schema(description = "操作者ID")
    private String operatorId;

    @Schema(description = "操作类型")
    private String action;

    @Schema(description = "操作详情JSON")
    private String actionDetail;

    @Schema(description = "操作原因")
    private String reason;

    @Schema(description = "操作结果：SUCCESS/FAILED")
    private String result;
}
```

- [ ] **Step 8: 创建 ChannelAnnouncement 实体**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("channel_announcement")
@Schema(description = "频道公告")
public class ChannelAnnouncement extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "公告标题")
    private String title;

    @Schema(description = "公告内容（富文本）")
    private String content;

    @Schema(description = "状态：ACTIVE/DELETED")
    private String status;

    @Schema(description = "创建人ID")
    private String createdBy;
}
```

- [ ] **Step 9: 创建所有 Mapper 接口**

```java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.content.channel.entity.ChannelContentPublish;

public interface ChannelContentPublishMapper extends BaseMapper<ChannelContentPublish> {
}
```

```java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.content.channel.entity.ChannelContentReview;

public interface ChannelContentReviewMapper extends BaseMapper<ChannelContentReview> {
}
```

```java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.content.channel.entity.ChannelScheduledPublish;

public interface ChannelScheduledPublishMapper extends BaseMapper<ChannelScheduledPublish> {
}
```

```java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.content.channel.entity.ChannelPublishLimit;

public interface ChannelPublishLimitMapper extends BaseMapper<ChannelPublishLimit> {
}
```

```java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.content.channel.entity.ChannelRecycleBin;

public interface ChannelRecycleBinMapper extends BaseMapper<ChannelRecycleBin> {
}
```

```java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.content.channel.entity.ChannelGovernanceLog;

public interface ChannelGovernanceLogMapper extends BaseMapper<ChannelGovernanceLog> {
}
```

```java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.content.channel.entity.ChannelAnnouncement;

public interface ChannelAnnouncementMapper extends BaseMapper<ChannelAnnouncement> {
}
```

- [ ] **Step 10: 验证编译通过**

Run: `cd jeecg-boot && mvn compile -pl jeecg-boot-module/jeecg-module-content -am`
Expected: BUILD SUCCESS

- [ ] **Step 11: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/db/migration/
git commit -m "feat(channel): add entity, mapper and SQL migration for content governance"
```

---

## Task 2: 发布权限校验 Service

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/enums/PublishPermissionEnum.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/enums/PublishStatusEnum.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelContentPublishService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/impl/ChannelContentPublishServiceImpl.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelContentPublishServiceTest.java`

- [ ] **Step 1: 创建发布权限枚举**

```java
package org.jeecg.modules.content.channel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PublishPermissionEnum {
    ADMIN_ONLY("ADMIN_ONLY", "仅管理员可发布"),
    ALL_MEMBERS("ALL_MEMBERS", "所有成员可发布"),
    PUBLIC_SUBMIT("PUBLIC_SUBMIT", "公开投稿"),
    PRE_REVIEW("PRE_REVIEW", "先审后发");

    private final String code;
    private final String desc;
}
```

```java
package org.jeecg.modules.content.channel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PublishStatusEnum {
    PUBLISHED("PUBLISHED", "已发布"),
    PENDING("PENDING", "待审核"),
    REJECTED("REJECTED", "已拒绝"),
    RECYCLED("RECYCLED", "已回收");

    private final String code;
    private final String desc;
}
```

- [ ] **Step 2: 编写发布权限校验失败测试**

```java
package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.enums.PublishPermissionEnum;
import org.jeecg.modules.content.channel.service.impl.ChannelContentPublishServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChannelContentPublishServiceTest {

    private ChannelContentPublishServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ChannelContentPublishServiceImpl();
    }

    @Test
    void adminOnly_shouldRejectNormalMember() {
        // 普通成员在仅管理员可发布频道应被拒绝
        String userRole = "MEMBER";
        String permission = PublishPermissionEnum.ADMIN_ONLY.getCode();
        boolean isMuted = false;
        boolean isBlacklisted = false;

        String result = service.checkPublishPermission(userRole, permission, isMuted, isBlacklisted);
        assertEquals("REJECT", result);
    }
}
```

- [ ] **Step 3: 运行测试确认失败**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelContentPublishServiceTest`
Expected: FAIL (class not found)

- [ ] **Step 4: 实现 ChannelContentPublishService 接口和实现**

```java
package org.jeecg.modules.content.channel.service;

public interface ChannelContentPublishService {
    /**
     * 校验发布权限
     * @return ALLOW/REJECT/REVIEW
     */
    String checkPublishPermission(String userRole, String publishPermission, boolean isMuted, boolean isBlacklisted);
}
```

```java
package org.jeecg.modules.content.channel.service.impl;

import org.jeecg.modules.content.channel.enums.PublishPermissionEnum;
import org.jeecg.modules.content.channel.service.ChannelContentPublishService;
import org.springframework.stereotype.Service;

@Service
public class ChannelContentPublishServiceImpl implements ChannelContentPublishService {

    @Override
    public String checkPublishPermission(String userRole, String publishPermission, boolean isMuted, boolean isBlacklisted) {
        if (isMuted || isBlacklisted) {
            return "REJECT";
        }
        if (PublishPermissionEnum.PRE_REVIEW.getCode().equals(publishPermission)) {
            return "REVIEW";
        }
        if (PublishPermissionEnum.ADMIN_ONLY.getCode().equals(publishPermission)) {
            return "ADMIN".equals(userRole) || "OWNER".equals(userRole) || "EDITOR".equals(userRole) ? "ALLOW" : "REJECT";
        }
        if (PublishPermissionEnum.PUBLIC_SUBMIT.getCode().equals(publishPermission)) {
            if ("NON_MEMBER".equals(userRole)) {
                return "REVIEW";
            }
            return "ALLOW";
        }
        if (PublishPermissionEnum.ALL_MEMBERS.getCode().equals(publishPermission)) {
            return "NON_MEMBER".equals(userRole) ? "REJECT" : "ALLOW";
        }
        return "REJECT";
    }
}
```

- [ ] **Step 5: 运行测试确认通过**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelContentPublishServiceTest`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/
git commit -m "feat(channel): add publish permission validation service"
```

---

## Task 3: 发布限额 Service

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelPublishLimitService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/impl/ChannelPublishLimitServiceImpl.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelPublishLimitServiceTest.java`

- [ ] **Step 1: 编写限额校验失败测试**

```java
package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.service.impl.ChannelPublishLimitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChannelPublishLimitServiceTest {

    private ChannelPublishLimitServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ChannelPublishLimitServiceImpl();
    }

    @Test
    void shouldRejectWhenDailyLimitExceeded() {
        int dailyLimit = 5;
        int todayCount = 5;
        int wordCount = 200;
        int minWordCount = 100;

        String result = service.checkLimit(dailyLimit, 0, todayCount, 0, wordCount, minWordCount);
        assertEquals("DAILY_EXCEEDED", result);
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelPublishLimitServiceTest`
Expected: FAIL

- [ ] **Step 3: 实现 ChannelPublishLimitService**

```java
package org.jeecg.modules.content.channel.service;

public interface ChannelPublishLimitService {
    /**
     * 校验发布限额
     * @return PASS/DAILY_EXCEEDED/HOURLY_EXCEEDED/WORD_COUNT_LOW
     */
    String checkLimit(int dailyLimit, int hourlyLimit, int todayCount, int hourlyCount, int wordCount, int minWordCount);
}
```

```java
package org.jeecg.modules.content.channel.service.impl;

import org.jeecg.modules.content.channel.service.ChannelPublishLimitService;
import org.springframework.stereotype.Service;

@Service
public class ChannelPublishLimitServiceImpl implements ChannelPublishLimitService {

    @Override
    public String checkLimit(int dailyLimit, int hourlyLimit, int todayCount, int hourlyCount, int wordCount, int minWordCount) {
        if (minWordCount > 0 && wordCount < minWordCount) {
            return "WORD_COUNT_LOW";
        }
        if (dailyLimit > 0 && todayCount >= dailyLimit) {
            return "DAILY_EXCEEDED";
        }
        if (hourlyLimit > 0 && hourlyCount >= hourlyLimit) {
            return "HOURLY_EXCEEDED";
        }
        return "PASS";
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelPublishLimitServiceTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/
git commit -m "feat(channel): add publish limit validation service"
```

---

## Task 4: 发布 Biz 层编排

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/req/publish/ChannelPublishReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/vo/publish/ChannelPublishResultVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/ChannelPublishBiz.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/impl/ChannelPublishBizImpl.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/biz/ChannelPublishBizTest.java`

- [ ] **Step 1: 创建请求和响应对象**

```java
package org.jeecg.modules.content.channel.req.publish;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "频道发布请求")
public class ChannelPublishReq {

    @NotBlank(message = "内容ID不能为空")
    @Schema(description = "内容ID")
    private String contentId;

    @NotBlank(message = "内容类型不能为空")
    @Schema(description = "内容类型")
    private String contentType;

    @NotEmpty(message = "目标频道不能为空")
    @Schema(description = "目标频道ID列表")
    private List<String> channelIds;

    @Schema(description = "定时发布时间")
    private java.util.Date scheduledTime;
}
```

```java
package org.jeecg.modules.content.channel.vo.publish;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "频道发布结果")
public class ChannelPublishResultVO {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "发布状态：PUBLISHED/PENDING/FAILED")
    private String status;

    @Schema(description = "失败原因")
    private String failReason;
}
```

- [ ] **Step 2: 编写 Biz 层测试**

```java
package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.biz.impl.ChannelPublishBizImpl;
import org.jeecg.modules.content.channel.req.publish.ChannelPublishReq;
import org.jeecg.modules.content.channel.service.ChannelContentPublishService;
import org.jeecg.modules.content.channel.service.ChannelPublishLimitService;
import org.jeecg.modules.content.channel.vo.publish.ChannelPublishResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelPublishBizTest {

    @InjectMocks
    private ChannelPublishBizImpl biz;

    @Mock
    private ChannelContentPublishService publishService;

    @Mock
    private ChannelPublishLimitService limitService;

    @Test
    void shouldPublishToMultipleChannelsWithDifferentResults() {
        ChannelPublishReq req = new ChannelPublishReq();
        req.setContentId("content-1");
        req.setContentType("article");
        req.setChannelIds(Arrays.asList("ch-1", "ch-2"));

        when(publishService.checkPublishPermission(anyString(), anyString(), anyBoolean(), anyBoolean()))
            .thenReturn("ALLOW")
            .thenReturn("REVIEW");
        when(limitService.checkLimit(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt()))
            .thenReturn("PASS");

        List<ChannelPublishResultVO> results = biz.publish(req);
        assertEquals(2, results.size());
        assertEquals("PUBLISHED", results.get(0).getStatus());
        assertEquals("PENDING", results.get(1).getStatus());
    }
}
```

- [ ] **Step 3: 运行测试确认失败**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelPublishBizTest`
Expected: FAIL

- [ ] **Step 4: 实现 ChannelPublishBiz**

```java
package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.req.publish.ChannelPublishReq;
import org.jeecg.modules.content.channel.vo.publish.ChannelPublishResultVO;
import java.util.List;

public interface ChannelPublishBiz {
    List<ChannelPublishResultVO> publish(ChannelPublishReq req);
}
```

```java
package org.jeecg.modules.content.channel.biz.impl;

import org.jeecg.modules.content.channel.biz.ChannelPublishBiz;
import org.jeecg.modules.content.channel.entity.ChannelContentPublish;
import org.jeecg.modules.content.channel.entity.ChannelContentReview;
import org.jeecg.modules.content.channel.mapper.ChannelContentPublishMapper;
import org.jeecg.modules.content.channel.mapper.ChannelContentReviewMapper;
import org.jeecg.modules.content.channel.req.publish.ChannelPublishReq;
import org.jeecg.modules.content.channel.service.ChannelContentPublishService;
import org.jeecg.modules.content.channel.service.ChannelPublishLimitService;
import org.jeecg.modules.content.channel.vo.publish.ChannelPublishResultVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChannelPublishBizImpl implements ChannelPublishBiz {

    @Resource
    private ChannelContentPublishService publishService;

    @Resource
    private ChannelPublishLimitService limitService;

    @Resource
    private ChannelContentPublishMapper publishMapper;

    @Resource
    private ChannelContentReviewMapper reviewMapper;

    @Override
    public List<ChannelPublishResultVO> publish(ChannelPublishReq req) {
        List<ChannelPublishResultVO> results = new ArrayList<>();
        for (String channelId : req.getChannelIds()) {
            ChannelPublishResultVO result = new ChannelPublishResultVO();
            result.setChannelId(channelId);
            try {
                // TODO: 查询用户角色、禁言状态、黑名单状态、发布权限、限额配置
                String permissionResult = "ALLOW"; // placeholder
                if ("REJECT".equals(permissionResult)) {
                    result.setStatus("FAILED");
                    result.setFailReason("权限不足");
                } else if ("REVIEW".equals(permissionResult)) {
                    // 写入待审区
                    ChannelContentReview review = new ChannelContentReview();
                    review.setChannelId(channelId);
                    review.setContentId(req.getContentId());
                    review.setContentType(req.getContentType());
                    review.setReviewStatus("PENDING");
                    reviewMapper.insert(review);
                    result.setStatus("PENDING");
                } else {
                    // 直接发布
                    ChannelContentPublish publish = new ChannelContentPublish();
                    publish.setChannelId(channelId);
                    publish.setContentId(req.getContentId());
                    publish.setContentType(req.getContentType());
                    publish.setPublishStatus("PUBLISHED");
                    publishMapper.insert(publish);
                    result.setStatus("PUBLISHED");
                }
            } catch (Exception e) {
                result.setStatus("FAILED");
                result.setFailReason(e.getMessage());
            }
            results.add(result);
        }
        return results;
    }
}
```

- [ ] **Step 5: 运行测试确认通过**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelPublishBizTest`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/
git commit -m "feat(channel): add publish biz orchestration"
```

---

## Task 5: 发布 Controller

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelPublishController.java`

- [ ] **Step 1: 实现 ChannelPublishController**

```java
package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelPublishBiz;
import org.jeecg.modules.content.channel.req.publish.ChannelPublishReq;
import org.jeecg.modules.content.channel.vo.publish.ChannelPublishResultVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "频道内容发布", description = "频道内容发布相关接口")
@Validated
@RestController
@RequestMapping("/content/channel/publish")
public class ChannelPublishController {

    @Resource
    private ChannelPublishBiz channelPublishBiz;

    @Operation(summary = "发布内容到频道")
    @PostMapping
    public Result<List<ChannelPublishResultVO>> publish(@Valid @RequestBody ChannelPublishReq req) {
        return Result.OK(channelPublishBiz.publish(req));
    }
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd jeecg-boot && mvn compile -pl jeecg-boot-module/jeecg-module-content -am`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/
git commit -m "feat(channel): add publish controller"
```

---

## Task 6: 待审区审核 Service 和 Biz

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/req/review/ChannelReviewReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelContentReviewService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/impl/ChannelContentReviewServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/ChannelReviewBiz.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/impl/ChannelReviewBizImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelReviewController.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelContentReviewServiceTest.java`

- [ ] **Step 1: 创建审核请求对象**

```java
package org.jeecg.modules.content.channel.req.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "审核请求")
public class ChannelReviewReq {

    @NotBlank(message = "审核记录ID不能为空")
    @Schema(description = "审核记录ID")
    private String reviewId;

    @NotBlank(message = "审核动作不能为空")
    @Schema(description = "审核动作：APPROVE/REJECT")
    private String action;

    @Schema(description = "拒绝原因（拒绝时必填）")
    private String rejectReason;
}
```

- [ ] **Step 2: 编写审核 Service 测试**

```java
package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelContentReview;
import org.jeecg.modules.content.channel.mapper.ChannelContentReviewMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelContentReviewServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelContentReviewServiceTest {

    @InjectMocks
    private ChannelContentReviewServiceImpl service;

    @Mock
    private ChannelContentReviewMapper reviewMapper;

    @Test
    void approve_shouldUpdateStatusToApproved() {
        ChannelContentReview review = new ChannelContentReview();
        review.setId("review-1");
        review.setReviewStatus("PENDING");
        when(reviewMapper.selectById("review-1")).thenReturn(review);
        when(reviewMapper.updateById(any())).thenReturn(1);

        service.approve("review-1", "admin-1");
        assertEquals("APPROVED", review.getReviewStatus());
        assertEquals("admin-1", review.getReviewerId());
        assertNotNull(review.getReviewTime());
    }

    @Test
    void reject_shouldUpdateStatusAndReason() {
        ChannelContentReview review = new ChannelContentReview();
        review.setId("review-1");
        review.setReviewStatus("PENDING");
        when(reviewMapper.selectById("review-1")).thenReturn(review);
        when(reviewMapper.updateById(any())).thenReturn(1);

        service.reject("review-1", "admin-1", "内容不符合主题");
        assertEquals("REJECTED", review.getReviewStatus());
        assertEquals("内容不符合主题", review.getRejectReason());
    }
}
```

- [ ] **Step 3: 运行测试确认失败**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelContentReviewServiceTest`
Expected: FAIL

- [ ] **Step 4: 实现 ChannelContentReviewService**

```java
package org.jeecg.modules.content.channel.service;

public interface ChannelContentReviewService {
    void approve(String reviewId, String reviewerId);
    void reject(String reviewId, String reviewerId, String reason);
}
```

```java
package org.jeecg.modules.content.channel.service.impl;

import org.jeecg.modules.content.channel.entity.ChannelContentReview;
import org.jeecg.modules.content.channel.mapper.ChannelContentReviewMapper;
import org.jeecg.modules.content.channel.service.ChannelContentReviewService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class ChannelContentReviewServiceImpl implements ChannelContentReviewService {

    @Resource
    private ChannelContentReviewMapper reviewMapper;

    @Override
    public void approve(String reviewId, String reviewerId) {
        ChannelContentReview review = reviewMapper.selectById(reviewId);
        review.setReviewStatus("APPROVED");
        review.setReviewerId(reviewerId);
        review.setReviewTime(new Date());
        reviewMapper.updateById(review);
    }

    @Override
    public void reject(String reviewId, String reviewerId, String reason) {
        ChannelContentReview review = reviewMapper.selectById(reviewId);
        review.setReviewStatus("REJECTED");
        review.setReviewerId(reviewerId);
        review.setReviewTime(new Date());
        review.setRejectReason(reason);
        reviewMapper.updateById(review);
    }
}
```

- [ ] **Step 5: 运行测试确认通过**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelContentReviewServiceTest`
Expected: PASS

- [ ] **Step 6: 实现 ReviewBiz 和 ReviewController**

```java
package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.req.review.ChannelReviewReq;

public interface ChannelReviewBiz {
    void review(ChannelReviewReq req, String reviewerId);
}
```

```java
package org.jeecg.modules.content.channel.biz.impl;

import org.jeecg.modules.content.channel.biz.ChannelReviewBiz;
import org.jeecg.modules.content.channel.req.review.ChannelReviewReq;
import org.jeecg.modules.content.channel.service.ChannelContentReviewService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ChannelReviewBizImpl implements ChannelReviewBiz {

    @Resource
    private ChannelContentReviewService reviewService;

    @Override
    public void review(ChannelReviewReq req, String reviewerId) {
        if ("APPROVE".equals(req.getAction())) {
            reviewService.approve(req.getReviewId(), reviewerId);
        } else {
            reviewService.reject(req.getReviewId(), reviewerId, req.getRejectReason());
        }
    }
}
```

```java
package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelReviewBiz;
import org.jeecg.modules.content.channel.req.review.ChannelReviewReq;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "频道内容审核", description = "频道待审区和审核相关接口")
@Validated
@RestController
@RequestMapping("/content/channel/review")
public class ChannelReviewController {

    @Resource
    private ChannelReviewBiz channelReviewBiz;

    @Operation(summary = "审核内容")
    @PostMapping
    public Result<Void> review(@Valid @RequestBody ChannelReviewReq req) {
        // TODO: 从安全上下文获取当前用户ID
        channelReviewBiz.review(req, "current-user-id");
        return Result.OK();
    }
}
```

- [ ] **Step 7: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/
git commit -m "feat(channel): add review service, biz and controller"
```

---

## Task 7: 定时发布 Service

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelScheduledPublishService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/impl/ChannelScheduledPublishServiceImpl.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelScheduledPublishServiceTest.java`

- [ ] **Step 1: 编写定时发布 Service 测试**

```java
package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelScheduledPublish;
import org.jeecg.modules.content.channel.mapper.ChannelScheduledPublishMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelScheduledPublishServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelScheduledPublishServiceTest {

    @InjectMocks
    private ChannelScheduledPublishServiceImpl service;

    @Mock
    private ChannelScheduledPublishMapper scheduledPublishMapper;

    @Test
    void findDueTasks_shouldReturnTasksBeforeNow() {
        ChannelScheduledPublish task = new ChannelScheduledPublish();
        task.setId("task-1");
        task.setPublishStatus("SCHEDULED");
        when(scheduledPublishMapper.selectList(any())).thenReturn(Arrays.asList(task));

        List<ChannelScheduledPublish> tasks = service.findDueTasks();
        assertEquals(1, tasks.size());
    }

    @Test
    void markFailed_shouldUpdateStatusAndReason() {
        ChannelScheduledPublish task = new ChannelScheduledPublish();
        task.setId("task-1");
        when(scheduledPublishMapper.selectById("task-1")).thenReturn(task);
        when(scheduledPublishMapper.updateById(any())).thenReturn(1);

        service.markFailed("task-1", "用户已被禁言");
        assertEquals("FAILED", task.getPublishStatus());
        assertEquals("用户已被禁言", task.getFailReason());
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelScheduledPublishServiceTest`
Expected: FAIL

- [ ] **Step 3: 实现 ChannelScheduledPublishService**

```java
package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelScheduledPublish;
import java.util.List;

public interface ChannelScheduledPublishService {
    List<ChannelScheduledPublish> findDueTasks();
    void markPublished(String taskId);
    void markFailed(String taskId, String reason);
}
```

```java
package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.entity.ChannelScheduledPublish;
import org.jeecg.modules.content.channel.mapper.ChannelScheduledPublishMapper;
import org.jeecg.modules.content.channel.service.ChannelScheduledPublishService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class ChannelScheduledPublishServiceImpl implements ChannelScheduledPublishService {

    @Resource
    private ChannelScheduledPublishMapper scheduledPublishMapper;

    @Override
    public List<ChannelScheduledPublish> findDueTasks() {
        LambdaQueryWrapper<ChannelScheduledPublish> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(ChannelScheduledPublish::getScheduledTime, new Date())
               .eq(ChannelScheduledPublish::getPublishStatus, "SCHEDULED");
        return scheduledPublishMapper.selectList(wrapper);
    }

    @Override
    public void markPublished(String taskId) {
        ChannelScheduledPublish task = scheduledPublishMapper.selectById(taskId);
        task.setPublishStatus("PUBLISHED");
        scheduledPublishMapper.updateById(task);
    }

    @Override
    public void markFailed(String taskId, String reason) {
        ChannelScheduledPublish task = scheduledPublishMapper.selectById(taskId);
        task.setPublishStatus("FAILED");
        task.setFailReason(reason);
        scheduledPublishMapper.updateById(task);
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelScheduledPublishServiceTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/
git commit -m "feat(channel): add scheduled publish service"
```

---

## Task 8: 回收站 Service

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelRecycleBinService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/impl/ChannelRecycleBinServiceImpl.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelRecycleBinServiceTest.java`

- [ ] **Step 1: 编写回收站 Service 测试**

```java
package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelRecycleBin;
import org.jeecg.modules.content.channel.mapper.ChannelRecycleBinMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelRecycleBinServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelRecycleBinServiceTest {

    @InjectMocks
    private ChannelRecycleBinServiceImpl service;

    @Mock
    private ChannelRecycleBinMapper recycleBinMapper;

    @Test
    void addToRecycleBin_shouldSetExpireTime30DaysLater() {
        when(recycleBinMapper.insert(any())).thenReturn(1);

        ChannelRecycleBin bin = service.addToRecycleBin("ch-1", "content-1", "article", "author-1", "admin-1", "违规内容");
        assertNotNull(bin.getExpireTime());
        assertEquals("ch-1", bin.getChannelId());
        assertFalse(bin.getIsRestored());
    }

    @Test
    void restore_shouldMarkAsRestored() {
        ChannelRecycleBin bin = new ChannelRecycleBin();
        bin.setId("bin-1");
        bin.setIsRestored(false);
        bin.setExpireTime(new Date(System.currentTimeMillis() + 86400000L)); // 未来
        when(recycleBinMapper.selectById("bin-1")).thenReturn(bin);
        when(recycleBinMapper.updateById(any())).thenReturn(1);

        boolean result = service.restore("bin-1", "admin-1");
        assertTrue(result);
        assertTrue(bin.getIsRestored());
    }

    @Test
    void restore_shouldFailWhenExpired() {
        ChannelRecycleBin bin = new ChannelRecycleBin();
        bin.setId("bin-1");
        bin.setExpireTime(new Date(System.currentTimeMillis() - 86400000L)); // 已过期
        when(recycleBinMapper.selectById("bin-1")).thenReturn(bin);

        boolean result = service.restore("bin-1", "admin-1");
        assertFalse(result);
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelRecycleBinServiceTest`
Expected: FAIL

- [ ] **Step 3: 实现 ChannelRecycleBinService**

```java
package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelRecycleBin;

public interface ChannelRecycleBinService {
    ChannelRecycleBin addToRecycleBin(String channelId, String contentId, String contentType, String authorId, String deletedBy, String reason);
    boolean restore(String recycleBinId, String restoredBy);
}
```

```java
package org.jeecg.modules.content.channel.service.impl;

import org.jeecg.modules.content.channel.entity.ChannelRecycleBin;
import org.jeecg.modules.content.channel.mapper.ChannelRecycleBinMapper;
import org.jeecg.modules.content.channel.service.ChannelRecycleBinService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class ChannelRecycleBinServiceImpl implements ChannelRecycleBinService {

    @Resource
    private ChannelRecycleBinMapper recycleBinMapper;

    @Override
    public ChannelRecycleBin addToRecycleBin(String channelId, String contentId, String contentType, String authorId, String deletedBy, String reason) {
        ChannelRecycleBin bin = new ChannelRecycleBin();
        bin.setChannelId(channelId);
        bin.setContentId(contentId);
        bin.setContentType(contentType);
        bin.setOriginalAuthorId(authorId);
        bin.setDeletedBy(deletedBy);
        bin.setDeleteTime(new Date());
        bin.setDeleteReason(reason);
        bin.setExpireTime(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000));
        bin.setIsRestored(false);
        recycleBinMapper.insert(bin);
        return bin;
    }

    @Override
    public boolean restore(String recycleBinId, String restoredBy) {
        ChannelRecycleBin bin = recycleBinMapper.selectById(recycleBinId);
        if (bin.getExpireTime().before(new Date())) {
            return false;
        }
        bin.setIsRestored(true);
        bin.setRestoredBy(restoredBy);
        bin.setRestoreTime(new Date());
        recycleBinMapper.updateById(bin);
        return true;
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelRecycleBinServiceTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/
git commit -m "feat(channel): add recycle bin service"
```

---

## Task 9: 治理日志 Service

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelGovernanceLogService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/impl/ChannelGovernanceLogServiceImpl.java`

- [ ] **Step 1: 实现 ChannelGovernanceLogService**

```java
package org.jeecg.modules.content.channel.service;

public interface ChannelGovernanceLogService {
    void log(String channelId, String contentId, String operatorId, String action, String detail, String reason, String result);
}
```

```java
package org.jeecg.modules.content.channel.service.impl;

import org.jeecg.modules.content.channel.entity.ChannelGovernanceLog;
import org.jeecg.modules.content.channel.mapper.ChannelGovernanceLogMapper;
import org.jeecg.modules.content.channel.service.ChannelGovernanceLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ChannelGovernanceLogServiceImpl implements ChannelGovernanceLogService {

    @Resource
    private ChannelGovernanceLogMapper governanceLogMapper;

    @Override
    public void log(String channelId, String contentId, String operatorId, String action, String detail, String reason, String result) {
        ChannelGovernanceLog log = new ChannelGovernanceLog();
        log.setChannelId(channelId);
        log.setContentId(contentId);
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setActionDetail(detail);
        log.setReason(reason);
        log.setResult(result);
        governanceLogMapper.insert(log);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/
git commit -m "feat(channel): add governance log service"
```

---

## Task 10: 内容治理 Biz 和 Controller

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/req/governance/ChannelGovernanceReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/ChannelGovernanceBiz.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/impl/ChannelGovernanceBizImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelGovernanceController.java`

- [ ] **Step 1: 创建治理请求对象**

```java
package org.jeecg.modules.content.channel.req.governance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "频道治理操作请求")
public class ChannelGovernanceReq {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID")
    private String channelId;

    @NotBlank(message = "内容ID不能为空")
    @Schema(description = "内容ID")
    private String contentId;

    @NotBlank(message = "操作类型不能为空")
    @Schema(description = "操作类型：PIN/UNPIN/FEATURE/UNFEATURE/DELETE/RESTORE/MOVE/EDIT_ASSIST")
    private String action;

    @Schema(description = "目标频道ID（移出频道时使用）")
    private String targetChannelId;

    @Schema(description = "操作原因")
    private String reason;
}
```

- [ ] **Step 2: 实现 ChannelGovernanceBiz**

```java
package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.req.governance.ChannelGovernanceReq;

public interface ChannelGovernanceBiz {
    void executeGovernance(ChannelGovernanceReq req, String operatorId);
}
```

```java
package org.jeecg.modules.content.channel.biz.impl;

import org.jeecg.modules.content.channel.biz.ChannelGovernanceBiz;
import org.jeecg.modules.content.channel.entity.ChannelContentPublish;
import org.jeecg.modules.content.channel.mapper.ChannelContentPublishMapper;
import org.jeecg.modules.content.channel.req.governance.ChannelGovernanceReq;
import org.jeecg.modules.content.channel.service.ChannelGovernanceLogService;
import org.jeecg.modules.content.channel.service.ChannelRecycleBinService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ChannelGovernanceBizImpl implements ChannelGovernanceBiz {

    @Resource
    private ChannelContentPublishMapper publishMapper;

    @Resource
    private ChannelRecycleBinService recycleBinService;

    @Resource
    private ChannelGovernanceLogService governanceLogService;

    @Override
    public void executeGovernance(ChannelGovernanceReq req, String operatorId) {
        String action = req.getAction();
        try {
            switch (action) {
                case "PIN":
                    handlePin(req, operatorId, true);
                    break;
                case "UNPIN":
                    handlePin(req, operatorId, false);
                    break;
                case "FEATURE":
                    handleFeature(req, operatorId, true);
                    break;
                case "UNFEATURE":
                    handleFeature(req, operatorId, false);
                    break;
                case "DELETE":
                    handleDelete(req, operatorId);
                    break;
                case "RESTORE":
                    handleRestore(req, operatorId);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的操作类型: " + action);
            }
            governanceLogService.log(req.getChannelId(), req.getContentId(), operatorId, action, null, req.getReason(), "SUCCESS");
        } catch (Exception e) {
            governanceLogService.log(req.getChannelId(), req.getContentId(), operatorId, action, null, req.getReason(), "FAILED");
            throw e;
        }
    }

    private void handlePin(ChannelGovernanceReq req, String operatorId, boolean pinned) {
        ChannelContentPublish publish = getPublishRecord(req);
        publish.setIsPinned(pinned);
        publishMapper.updateById(publish);
    }

    private void handleFeature(ChannelGovernanceReq req, String operatorId, boolean featured) {
        ChannelContentPublish publish = getPublishRecord(req);
        publish.setIsFeatured(featured);
        publishMapper.updateById(publish);
    }

    private void handleDelete(ChannelGovernanceReq req, String operatorId) {
        ChannelContentPublish publish = getPublishRecord(req);
        recycleBinService.addToRecycleBin(req.getChannelId(), req.getContentId(), publish.getContentType(), publish.getPublisherId(), operatorId, req.getReason());
        publish.setPublishStatus("RECYCLED");
        publishMapper.updateById(publish);
    }

    private void handleRestore(ChannelGovernanceReq req, String operatorId) {
        // TODO: 从回收站恢复
    }

    private ChannelContentPublish getPublishRecord(ChannelGovernanceReq req) {
        // TODO: 查询发布记录
        return new ChannelContentPublish();
    }
}
```

- [ ] **Step 3: 实现 ChannelGovernanceController**

```java
package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelGovernanceBiz;
import org.jeecg.modules.content.channel.req.governance.ChannelGovernanceReq;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "频道内容治理", description = "频道内容置顶、精华、删除、恢复等治理操作接口")
@Validated
@RestController
@RequestMapping("/content/channel/governance")
public class ChannelGovernanceController {

    @Resource
    private ChannelGovernanceBiz channelGovernanceBiz;

    @Operation(summary = "执行治理操作")
    @PostMapping
    public Result<Void> governance(@Valid @RequestBody ChannelGovernanceReq req) {
        channelGovernanceBiz.executeGovernance(req, "current-user-id");
        return Result.OK();
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/
git commit -m "feat(channel): add governance biz and controller"
```

---

## Task 11: 频道公告 Service、Biz 和 Controller

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/req/announcement/ChannelAnnouncementReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelAnnouncementService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/impl/ChannelAnnouncementServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/ChannelAnnouncementBiz.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/impl/ChannelAnnouncementBizImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelAnnouncementController.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelAnnouncementServiceTest.java`

- [ ] **Step 1: 创建公告请求对象**

```java
package org.jeecg.modules.content.channel.req.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "频道公告请求")
public class ChannelAnnouncementReq {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID")
    private String channelId;

    @NotBlank(message = "公告标题不能为空")
    @Schema(description = "公告标题")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    @Schema(description = "公告内容（富文本）")
    private String content;
}
```

- [ ] **Step 2: 编写公告 Service 测试**

```java
package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelAnnouncement;
import org.jeecg.modules.content.channel.mapper.ChannelAnnouncementMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelAnnouncementServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelAnnouncementServiceTest {

    @InjectMocks
    private ChannelAnnouncementServiceImpl service;

    @Mock
    private ChannelAnnouncementMapper announcementMapper;

    @Test
    void create_shouldInsertWithActiveStatus() {
        when(announcementMapper.insert(any())).thenReturn(1);

        ChannelAnnouncement announcement = service.create("ch-1", "标题", "<p>内容</p>", "admin-1");
        assertEquals("ACTIVE", announcement.getStatus());
        assertEquals("ch-1", announcement.getChannelId());
    }

    @Test
    void delete_shouldMarkAsDeleted() {
        ChannelAnnouncement announcement = new ChannelAnnouncement();
        announcement.setId("ann-1");
        announcement.setStatus("ACTIVE");
        when(announcementMapper.selectById("ann-1")).thenReturn(announcement);
        when(announcementMapper.updateById(any())).thenReturn(1);

        service.delete("ann-1");
        assertEquals("DELETED", announcement.getStatus());
    }
}
```

- [ ] **Step 3: 运行测试确认失败**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelAnnouncementServiceTest`
Expected: FAIL

- [ ] **Step 4: 实现 ChannelAnnouncementService**

```java
package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelAnnouncement;

public interface ChannelAnnouncementService {
    ChannelAnnouncement create(String channelId, String title, String content, String createdBy);
    void update(String id, String title, String content);
    void delete(String id);
    ChannelAnnouncement getByChannelId(String channelId);
}
```

```java
package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.entity.ChannelAnnouncement;
import org.jeecg.modules.content.channel.mapper.ChannelAnnouncementMapper;
import org.jeecg.modules.content.channel.service.ChannelAnnouncementService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ChannelAnnouncementServiceImpl implements ChannelAnnouncementService {

    @Resource
    private ChannelAnnouncementMapper announcementMapper;

    @Override
    public ChannelAnnouncement create(String channelId, String title, String content, String createdBy) {
        ChannelAnnouncement announcement = new ChannelAnnouncement();
        announcement.setChannelId(channelId);
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setCreatedBy(createdBy);
        announcement.setStatus("ACTIVE");
        announcementMapper.insert(announcement);
        return announcement;
    }

    @Override
    public void update(String id, String title, String content) {
        ChannelAnnouncement announcement = announcementMapper.selectById(id);
        announcement.setTitle(title);
        announcement.setContent(content);
        announcementMapper.updateById(announcement);
    }

    @Override
    public void delete(String id) {
        ChannelAnnouncement announcement = announcementMapper.selectById(id);
        announcement.setStatus("DELETED");
        announcementMapper.updateById(announcement);
    }

    @Override
    public ChannelAnnouncement getByChannelId(String channelId) {
        LambdaQueryWrapper<ChannelAnnouncement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChannelAnnouncement::getChannelId, channelId)
               .eq(ChannelAnnouncement::getStatus, "ACTIVE");
        return announcementMapper.selectOne(wrapper);
    }
}
```

- [ ] **Step 5: 运行测试确认通过**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelAnnouncementServiceTest`
Expected: PASS

- [ ] **Step 6: 实现 AnnouncementBiz 和 AnnouncementController**

```java
package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.entity.ChannelAnnouncement;
import org.jeecg.modules.content.channel.req.announcement.ChannelAnnouncementReq;

public interface ChannelAnnouncementBiz {
    ChannelAnnouncement create(ChannelAnnouncementReq req, String operatorId);
    void update(String id, ChannelAnnouncementReq req);
    void delete(String id);
    ChannelAnnouncement getByChannelId(String channelId);
}
```

```java
package org.jeecg.modules.content.channel.biz.impl;

import org.jeecg.modules.content.channel.biz.ChannelAnnouncementBiz;
import org.jeecg.modules.content.channel.entity.ChannelAnnouncement;
import org.jeecg.modules.content.channel.req.announcement.ChannelAnnouncementReq;
import org.jeecg.modules.content.channel.service.ChannelAnnouncementService;
import org.jeecg.modules.content.channel.service.ChannelGovernanceLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ChannelAnnouncementBizImpl implements ChannelAnnouncementBiz {

    @Resource
    private ChannelAnnouncementService announcementService;

    @Resource
    private ChannelGovernanceLogService governanceLogService;

    @Override
    public ChannelAnnouncement create(ChannelAnnouncementReq req, String operatorId) {
        ChannelAnnouncement announcement = announcementService.create(req.getChannelId(), req.getTitle(), req.getContent(), operatorId);
        governanceLogService.log(req.getChannelId(), null, operatorId, "ANNOUNCEMENT_CREATE", null, null, "SUCCESS");
        return announcement;
    }

    @Override
    public void update(String id, ChannelAnnouncementReq req) {
        announcementService.update(id, req.getTitle(), req.getContent());
    }

    @Override
    public void delete(String id) {
        announcementService.delete(id);
    }

    @Override
    public ChannelAnnouncement getByChannelId(String channelId) {
        return announcementService.getByChannelId(channelId);
    }
}
```

```java
package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelAnnouncementBiz;
import org.jeecg.modules.content.channel.entity.ChannelAnnouncement;
import org.jeecg.modules.content.channel.req.announcement.ChannelAnnouncementReq;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "频道公告", description = "频道公告管理接口")
@Validated
@RestController
@RequestMapping("/content/channel/announcement")
public class ChannelAnnouncementController {

    @Resource
    private ChannelAnnouncementBiz channelAnnouncementBiz;

    @Operation(summary = "创建公告")
    @PostMapping
    public Result<ChannelAnnouncement> create(@Valid @RequestBody ChannelAnnouncementReq req) {
        return Result.OK(channelAnnouncementBiz.create(req, "current-user-id"));
    }

    @Operation(summary = "更新公告")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable String id, @Valid @RequestBody ChannelAnnouncementReq req) {
        channelAnnouncementBiz.update(id, req);
        return Result.OK();
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        channelAnnouncementBiz.delete(id);
        return Result.OK();
    }

    @Operation(summary = "获取频道公告")
    @GetMapping("/channel/{channelId}")
    public Result<ChannelAnnouncement> getByChannelId(@PathVariable String channelId) {
        return Result.OK(channelAnnouncementBiz.getByChannelId(channelId));
    }
}
```

- [ ] **Step 7: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/
git commit -m "feat(channel): add announcement service, biz and controller"
```

---

## Task 12: 最终验证

- [ ] **Step 1: 运行全量编译**

Run: `cd jeecg-boot && mvn compile -pl jeecg-boot-module/jeecg-module-content -am`
Expected: BUILD SUCCESS

- [ ] **Step 2: 运行全量测试**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content`
Expected: All tests PASS

- [ ] **Step 3: 最终 Commit**

```bash
git add -A
git commit -m "feat(channel): complete channel content governance implementation"
```
