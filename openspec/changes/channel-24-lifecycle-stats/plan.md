# 频道数据统计与生命周期管理 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 建立频道数据统计看板、数据导出、生命周期状态流转和合规治理能力

**Architecture:** 分层架构 controller/biz/service/mapper/entity，biz 层编排跨聚合操作，service 层处理单表逻辑。统计采用预聚合汇总表 + 定时任务刷新，生命周期采用状态枚举 + 状态转换规则。

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Flyway, Lombok, Java 17

---

## 文件结构概览

```
jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/
├── entity/
│   ├── ChannelStats.java
│   ├── ChannelExportTask.java
│   ├── ChannelReview.java
│   ├── ChannelLifecycleLog.java
│   └── ChannelAppeal.java
├── enums/
│   ├── ChannelLifecycleStatus.java
│   ├── ChannelReviewStatus.java
│   ├── ChannelExportStatus.java
│   ├── ChannelAppealStatus.java
│   └── ChannelViolationType.java
├── mapper/
│   ├── ChannelStatsMapper.java
│   ├── ChannelStatsMapper.xml
│   ├── ChannelExportTaskMapper.java
│   ├── ChannelReviewMapper.java
│   ├── ChannelLifecycleLogMapper.java
│   └── ChannelAppealMapper.java
├── service/
│   ├── IChannelStatsService.java
│   ├── impl/ChannelStatsServiceImpl.java
│   ├── IChannelExportTaskService.java
│   ├── impl/ChannelExportTaskServiceImpl.java
│   ├── IChannelReviewService.java
│   ├── impl/ChannelReviewServiceImpl.java
│   ├── IChannelLifecycleLogService.java
│   ├── impl/ChannelLifecycleLogServiceImpl.java
│   ├── IChannelAppealService.java
│   └── impl/ChannelAppealServiceImpl.java
├── biz/
│   ├── ChannelStatsBiz.java
│   ├── ChannelExportBiz.java
│   ├── ChannelLifecycleBiz.java
│   └── ChannelMergeBiz.java
├── controller/
│   ├── ChannelStatsController.java
│   ├── ChannelExportController.java
│   ├── ChannelReviewController.java
│   └── ChannelLifecycleController.java
├── req/
│   ├── ChannelStatsQueryReq.java
│   ├── ChannelExportReq.java
│   ├── ChannelReviewActionReq.java
│   ├── ChannelLifecycleActionReq.java
│   └── ChannelAppealReq.java
├── vo/
│   ├── ChannelStatsVO.java
│   ├── ChannelTrendVO.java
│   ├── ChannelHotContentVO.java
│   ├── ChannelUserAnalysisVO.java
│   ├── ChannelExportTaskVO.java
│   ├── ChannelReviewVO.java
│   └── ChannelLifecycleLogVO.java
└── constant/
    └── ChannelStatsConstant.java

src/main/resources/flyway/sql/mysql/
└── V{version}__channel_lifecycle_stats.sql

src/test/java/org/jeecg/modules/content/channel/
├── biz/
│   ├── ChannelStatsBizTest.java
│   ├── ChannelExportBizTest.java
│   ├── ChannelLifecycleBizTest.java
│   └── ChannelMergeBizTest.java
└── service/
    ├── ChannelStatsServiceTest.java
    ├── ChannelLifecycleLogServiceTest.java
    └── ChannelAppealServiceTest.java
```

---

## Task 1: 数据库迁移脚本

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V2025_001__channel_lifecycle_stats.sql`

- [ ] **Step 1: 编写 Flyway 迁移脚本**

```sql
-- V2025_001__channel_lifecycle_stats.sql
-- 频道统计汇总表
CREATE TABLE IF NOT EXISTS `channel_stats` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `channel_id` VARCHAR(64) NOT NULL COMMENT '频道ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `stat_type` VARCHAR(20) NOT NULL COMMENT '统计类型：daily/weekly/monthly',
    `subscriber_count` INT UNSIGNED DEFAULT 0 COMMENT '订阅数',
    `content_count` INT UNSIGNED DEFAULT 0 COMMENT '内容数',
    `pv` BIGINT UNSIGNED DEFAULT 0 COMMENT '浏览量',
    `uv` BIGINT UNSIGNED DEFAULT 0 COMMENT '访客数',
    `like_count` BIGINT UNSIGNED DEFAULT 0 COMMENT '点赞数',
    `comment_count` BIGINT UNSIGNED DEFAULT 0 COMMENT '评论数',
    `favorite_count` BIGINT UNSIGNED DEFAULT 0 COMMENT '收藏数',
    `share_count` BIGINT UNSIGNED DEFAULT 0 COMMENT '分享数',
    `effective_visit_count` BIGINT UNSIGNED DEFAULT 0 COMMENT '有效访问数',
    `new_subscriber_count` INT UNSIGNED DEFAULT 0 COMMENT '新增订阅数',
    `lost_subscriber_count` INT UNSIGNED DEFAULT 0 COMMENT '流失订阅数',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_channel_id` (`channel_id`),
    INDEX `idx_stat_date` (`stat_date`),
    UNIQUE KEY `uk_channel_date_type` (`channel_id`, `stat_date`, `stat_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道统计汇总表';

-- 频道导出任务表
CREATE TABLE IF NOT EXISTS `channel_export_task` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id` VARCHAR(64) NOT NULL COMMENT '任务ID',
    `channel_id` VARCHAR(64) NOT NULL COMMENT '频道ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '导出用户ID',
    `export_type` VARCHAR(30) NOT NULL COMMENT '导出类型：core_stats/interaction/user_analysis',
    `file_format` VARCHAR(10) NOT NULL COMMENT '文件格式：xlsx/csv',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/processing/completed/failed',
    `file_path` VARCHAR(500) DEFAULT NULL COMMENT '文件路径',
    `file_size` BIGINT UNSIGNED DEFAULT NULL COMMENT '文件大小(字节)',
    `row_count` INT UNSIGNED DEFAULT NULL COMMENT '数据行数',
    `start_date` DATE DEFAULT NULL COMMENT '开始日期',
    `end_date` DATE DEFAULT NULL COMMENT '结束日期',
    `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `expire_time` DATETIME DEFAULT NULL COMMENT '文件过期时间',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_id` (`task_id`),
    INDEX `idx_channel_id` (`channel_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道导出任务表';

-- 频道审核记录表
CREATE TABLE IF NOT EXISTS `channel_review` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `review_id` VARCHAR(64) NOT NULL COMMENT '审核ID',
    `channel_id` VARCHAR(64) NOT NULL COMMENT '频道ID',
    `review_type` VARCHAR(30) NOT NULL COMMENT '审核类型：create/update_field/archive/merge',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/approved/rejected/returned',
    `applicant_id` VARCHAR(64) NOT NULL COMMENT '申请人ID',
    `reviewer_id` VARCHAR(64) DEFAULT NULL COMMENT '审核人ID',
    `review_reason` VARCHAR(500) DEFAULT NULL COMMENT '审核原因',
    `submit_time` DATETIME NOT NULL COMMENT '提交时间',
    `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `timeout_flag` TINYINT(1) DEFAULT 0 COMMENT '是否超时：0-否 1-是',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_review_id` (`review_id`),
    INDEX `idx_channel_id` (`channel_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_applicant_id` (`applicant_id`),
    INDEX `idx_submit_time` (`submit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道审核记录表';

-- 频道生命周期变更日志表
CREATE TABLE IF NOT EXISTS `channel_lifecycle_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `log_id` VARCHAR(64) NOT NULL COMMENT '日志ID',
    `channel_id` VARCHAR(64) NOT NULL COMMENT '频道ID',
    `action_type` VARCHAR(30) NOT NULL COMMENT '操作类型：freeze/unfreeze/hide/restrict_recommend/close/archive/merge/delete',
    `from_status` VARCHAR(20) NOT NULL COMMENT '变更前状态',
    `to_status` VARCHAR(20) NOT NULL COMMENT '变更后状态',
    `operator_id` VARCHAR(64) NOT NULL COMMENT '操作人ID',
    `reason` VARCHAR(500) DEFAULT NULL COMMENT '操作原因',
    `impact_scope` VARCHAR(200) DEFAULT NULL COMMENT '影响范围',
    `target_channel_id` VARCHAR(64) DEFAULT NULL COMMENT '目标频道ID(合并时)',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_log_id` (`log_id`),
    INDEX `idx_channel_id` (`channel_id`),
    INDEX `idx_action_type` (`action_type`),
    INDEX `idx_operator_id` (`operator_id`),
    INDEX `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道生命周期变更日志表';

-- 频道申诉记录表
CREATE TABLE IF NOT EXISTS `channel_appeal` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `appeal_id` VARCHAR(64) NOT NULL COMMENT '申诉ID',
    `channel_id` VARCHAR(64) NOT NULL COMMENT '频道ID',
    `lifecycle_log_id` VARCHAR(64) NOT NULL COMMENT '关联的生命周期日志ID',
    `applicant_id` VARCHAR(64) NOT NULL COMMENT '申诉人ID',
    `appeal_reason` VARCHAR(1000) NOT NULL COMMENT '申诉理由',
    `attachment_urls` VARCHAR(2000) DEFAULT NULL COMMENT '附件URL(JSON数组)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/processing/approved/rejected',
    `handler_id` VARCHAR(64) DEFAULT NULL COMMENT '处理人ID',
    `handle_result` VARCHAR(500) DEFAULT NULL COMMENT '处理结果',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `first_response_time` DATETIME DEFAULT NULL COMMENT '首次响应时间',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_appeal_id` (`appeal_id`),
    INDEX `idx_channel_id` (`channel_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_applicant_id` (`applicant_id`),
    INDEX `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道申诉记录表';
```

- [ ] **Step 2: 验证迁移脚本语法**

```bash
# 在本地 MySQL 中执行语法检查
mysql -u root -p -e "SOURCE jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V2025_001__channel_lifecycle_stats.sql" --dry-run
```

- [ ] **Step 3: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V2025_001__channel_lifecycle_stats.sql
git commit -m "feat(channel): add lifecycle stats migration script"
```

---

## Task 2: 枚举类

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/enums/ChannelLifecycleStatus.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/enums/ChannelReviewStatus.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/enums/ChannelExportStatus.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/enums/ChannelAppealStatus.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/enums/ChannelViolationType.java`

- [ ] **Step 1: 创建 ChannelLifecycleStatus 枚举**

```java
package org.jeecg.modules.content.channel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChannelLifecycleStatus {
    PENDING_REVIEW("PendingReview", "待审核"),
    ACTIVE("Active", "正常"),
    READONLY_FROZEN("ReadonlyFrozen", "只读冻结"),
    HIDDEN("Hidden", "强制隐藏"),
    ARCHIVED("Archived", "已归档"),
    MERGED("Merged", "已合并"),
    CLOSED("Closed", "永久关闭"),
    DELETED("Deleted", "已删除");

    private final String code;
    private final String desc;

    public static ChannelLifecycleStatus fromCode(String code) {
        for (ChannelLifecycleStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown lifecycle status code: " + code);
    }
}
```

- [ ] **Step 2: 创建 ChannelReviewStatus 枚举**

```java
package org.jeecg.modules.content.channel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChannelReviewStatus {
    PENDING("pending", "待审核"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已拒绝"),
    RETURNED("returned", "已退回");

    private final String code;
    private final String desc;
}
```

- [ ] **Step 3: 创建 ChannelExportStatus 枚举**

```java
package org.jeecg.modules.content.channel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChannelExportStatus {
    PENDING("pending", "待处理"),
    PROCESSING("processing", "处理中"),
    COMPLETED("completed", "已完成"),
    FAILED("failed", "失败");

    private final String code;
    private final String desc;
}
```

- [ ] **Step 4: 创建 ChannelAppealStatus 枚举**

```java
package org.jeecg.modules.content.channel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChannelAppealStatus {
    PENDING("pending", "待处理"),
    PROCESSING("processing", "处理中"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已驳回");

    private final String code;
    private final String desc;
}
```

- [ ] **Step 5: 创建 ChannelViolationType 枚举**

```java
package org.jeecg.modules.content.channel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChannelViolationType {
    RESTRICT_RECOMMEND("restrict_recommend", "限制推荐"),
    FREEZE("freeze", "只读冻结"),
    HIDE("hide", "强制隐藏"),
    CLOSE("close", "永久关闭");

    private final String code;
    private final String desc;
}
```

- [ ] **Step 6: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/enums/
git commit -m "feat(channel): add lifecycle enums"
```

---

## Task 3: 实体类

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/entity/ChannelStats.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/entity/ChannelExportTask.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/entity/ChannelReview.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/entity/ChannelLifecycleLog.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/entity/ChannelAppeal.java`

- [ ] **Step 1: 创建 ChannelStats 实体**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("channel_stats")
@Schema(description = "频道统计汇总")
public class ChannelStats {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "统计日期")
    private LocalDate statDate;

    @Schema(description = "统计类型：daily/weekly/monthly")
    private String statType;

    @Schema(description = "订阅数")
    private Integer subscriberCount;

    @Schema(description = "内容数")
    private Integer contentCount;

    @Schema(description = "浏览量")
    private Long pv;

    @Schema(description = "访客数")
    private Long uv;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "评论数")
    private Long commentCount;

    @Schema(description = "收藏数")
    private Long favoriteCount;

    @Schema(description = "分享数")
    private Long shareCount;

    @Schema(description = "有效访问数")
    private Long effectiveVisitCount;

    @Schema(description = "新增订阅数")
    private Integer newSubscriberCount;

    @Schema(description = "流失订阅数")
    private Integer lostSubscriberCount;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}
```

- [ ] **Step 2: 创建 ChannelExportTask 实体**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("channel_export_task")
@Schema(description = "频道导出任务")
public class ChannelExportTask {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "导出用户ID")
    private String userId;

    @Schema(description = "导出类型：core_stats/interaction/user_analysis")
    private String exportType;

    @Schema(description = "文件格式：xlsx/csv")
    private String fileFormat;

    @Schema(description = "状态：pending/processing/completed/failed")
    private String status;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "数据行数")
    private Integer rowCount;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "文件过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}
```

- [ ] **Step 3: 创建 ChannelReview 实体**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("channel_review")
@Schema(description = "频道审核记录")
public class ChannelReview {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "审核ID")
    private String reviewId;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "审核类型：create/update_field/archive/merge")
    private String reviewType;

    @Schema(description = "状态：pending/approved/rejected/returned")
    private String status;

    @Schema(description = "申请人ID")
    private String applicantId;

    @Schema(description = "审核人ID")
    private String reviewerId;

    @Schema(description = "审核原因")
    private String reviewReason;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;

    @Schema(description = "是否超时：0-否 1-是")
    private Integer timeoutFlag;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}
```

- [ ] **Step 4: 创建 ChannelLifecycleLog 实体**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("channel_lifecycle_log")
@Schema(description = "频道生命周期变更日志")
public class ChannelLifecycleLog {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "日志ID")
    private String logId;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "操作类型：freeze/unfreeze/hide/restrict_recommend/close/archive/merge/delete")
    private String actionType;

    @Schema(description = "变更前状态")
    private String fromStatus;

    @Schema(description = "变更后状态")
    private String toStatus;

    @Schema(description = "操作人ID")
    private String operatorId;

    @Schema(description = "操作原因")
    private String reason;

    @Schema(description = "影响范围")
    private String impactScope;

    @Schema(description = "目标频道ID(合并时)")
    private String targetChannelId;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
}
```

- [ ] **Step 5: 创建 ChannelAppeal 实体**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("channel_appeal")
@Schema(description = "频道申诉记录")
public class ChannelAppeal {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "申诉ID")
    private String appealId;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "关联的生命周期日志ID")
    private String lifecycleLogId;

    @Schema(description = "申诉人ID")
    private String applicantId;

    @Schema(description = "申诉理由")
    private String appealReason;

    @Schema(description = "附件URL(JSON数组)")
    private String attachmentUrls;

    @Schema(description = "状态：pending/processing/approved/rejected")
    private String status;

    @Schema(description = "处理人ID")
    private String handlerId;

    @Schema(description = "处理结果")
    private String handleResult;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "首次响应时间")
    private LocalDateTime firstResponseTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}
```

- [ ] **Step 6: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/entity/
git commit -m "feat(channel): add lifecycle entity classes"
```

---

## Task 4: Mapper 接口

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/mapper/ChannelStatsMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/mapper/ChannelStatsMapper.xml`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/mapper/ChannelExportTaskMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/mapper/ChannelReviewMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/mapper/ChannelLifecycleLogMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/mapper/ChannelAppealMapper.java`

- [ ] **Step 1: 创建 ChannelStatsMapper**

```java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.content.channel.entity.ChannelStats;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ChannelStatsMapper extends BaseMapper<ChannelStats> {

    List<ChannelStats> selectTrendData(@Param("channelId") String channelId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("statType") String statType);
}
```

- [ ] **Step 2: 创建 ChannelStatsMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.jeecg.modules.content.channel.mapper.ChannelStatsMapper">

    <select id="selectTrendData" resultType="org.jeecg.modules.content.channel.entity.ChannelStats">
        SELECT *
        FROM channel_stats
        WHERE channel_id = #{channelId}
          AND stat_date BETWEEN #{startDate} AND #{endDate}
          AND stat_type = #{statType}
        ORDER BY stat_date ASC
    </select>

</mapper>
```

- [ ] **Step 3: 创建其他 Mapper 接口**

```java
// ChannelExportTaskMapper.java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.channel.entity.ChannelExportTask;

@Mapper
public interface ChannelExportTaskMapper extends BaseMapper<ChannelExportTask> {
}
```

```java
// ChannelReviewMapper.java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.channel.entity.ChannelReview;

@Mapper
public interface ChannelReviewMapper extends BaseMapper<ChannelReview> {
}
```

```java
// ChannelLifecycleLogMapper.java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.channel.entity.ChannelLifecycleLog;

@Mapper
public interface ChannelLifecycleLogMapper extends BaseMapper<ChannelLifecycleLog> {
}
```

```java
// ChannelAppealMapper.java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.channel.entity.ChannelAppeal;

@Mapper
public interface ChannelAppealMapper extends BaseMapper<ChannelAppeal> {
}
```

- [ ] **Step 4: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/mapper/
git commit -m "feat(channel): add lifecycle mapper interfaces"
```

---

## Task 5: Service 接口与实现

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/IChannelStatsService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/impl/ChannelStatsServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/IChannelExportTaskService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/impl/ChannelExportTaskServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/IChannelReviewService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/impl/ChannelReviewServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/IChannelLifecycleLogService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/impl/ChannelLifecycleLogServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/IChannelAppealService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/impl/ChannelAppealServiceImpl.java`

- [ ] **Step 1: 创建 IChannelStatsService**

```java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ChannelStats;

import java.time.LocalDate;
import java.util.List;

public interface IChannelStatsService extends IService<ChannelStats> {

    ChannelStats getLatestStats(String channelId);

    List<ChannelStats> getTrendData(String channelId, LocalDate startDate, LocalDate endDate, String statType);
}
```

- [ ] **Step 2: 创建 ChannelStatsServiceImpl**

```java
package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ChannelStats;
import org.jeecg.modules.content.channel.mapper.ChannelStatsMapper;
import org.jeecg.modules.content.channel.service.IChannelStatsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ChannelStatsServiceImpl extends ServiceImpl<ChannelStatsMapper, ChannelStats>
        implements IChannelStatsService {

    @Override
    public ChannelStats getLatestStats(String channelId) {
        return lambdaQuery()
                .eq(ChannelStats::getChannelId, channelId)
                .eq(ChannelStats::getStatType, "daily")
                .orderByDesc(ChannelStats::getStatDate)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public List<ChannelStats> getTrendData(String channelId, LocalDate startDate, LocalDate endDate, String statType) {
        return baseMapper.selectTrendData(channelId, startDate, endDate, statType);
    }
}
```

- [ ] **Step 3: 创建其他 Service 接口与实现**

```java
// IChannelExportTaskService.java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ChannelExportTask;

public interface IChannelExportTaskService extends IService<ChannelExportTask> {
}
```

```java
// ChannelExportTaskServiceImpl.java
package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ChannelExportTask;
import org.jeecg.modules.content.channel.mapper.ChannelExportTaskMapper;
import org.jeecg.modules.content.channel.service.IChannelExportTaskService;
import org.springframework.stereotype.Service;

@Service
public class ChannelExportTaskServiceImpl extends ServiceImpl<ChannelExportTaskMapper, ChannelExportTask>
        implements IChannelExportTaskService {
}
```

```java
// IChannelReviewService.java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ChannelReview;

public interface IChannelReviewService extends IService<ChannelReview> {
}
```

```java
// ChannelReviewServiceImpl.java
package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ChannelReview;
import org.jeecg.modules.content.channel.mapper.ChannelReviewMapper;
import org.jeecg.modules.content.channel.service.IChannelReviewService;
import org.springframework.stereotype.Service;

@Service
public class ChannelReviewServiceImpl extends ServiceImpl<ChannelReviewMapper, ChannelReview>
        implements IChannelReviewService {
}
```

```java
// IChannelLifecycleLogService.java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ChannelLifecycleLog;

public interface IChannelLifecycleLogService extends IService<ChannelLifecycleLog> {
}
```

```java
// ChannelLifecycleLogServiceImpl.java
package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ChannelLifecycleLog;
import org.jeecg.modules.content.channel.mapper.ChannelLifecycleLogMapper;
import org.jeecg.modules.content.channel.service.IChannelLifecycleLogService;
import org.springframework.stereotype.Service;

@Service
public class ChannelLifecycleLogServiceImpl extends ServiceImpl<ChannelLifecycleLogMapper, ChannelLifecycleLog>
        implements IChannelLifecycleLogService {
}
```

```java
// IChannelAppealService.java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ChannelAppeal;

public interface IChannelAppealService extends IService<ChannelAppeal> {
}
```

```java
// ChannelAppealServiceImpl.java
package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ChannelAppeal;
import org.jeecg.modules.content.channel.mapper.ChannelAppealMapper;
import org.jeecg.modules.content.channel.service.IChannelAppealService;
import org.springframework.stereotype.Service;

@Service
public class ChannelAppealServiceImpl extends ServiceImpl<ChannelAppealMapper, ChannelAppeal>
        implements IChannelAppealService {
}
```

- [ ] **Step 4: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/
git commit -m "feat(channel): add lifecycle service interfaces and implementations"
```

---

## Task 6: VO/DTO/Req 类

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/vo/ChannelStatsVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/vo/ChannelTrendVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/vo/ChannelHotContentVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/vo/ChannelUserAnalysisVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/vo/ChannelExportTaskVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/vo/ChannelReviewVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/vo/ChannelLifecycleLogVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/req/ChannelStatsQueryReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/req/ChannelExportReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/req/ChannelReviewActionReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/req/ChannelLifecycleActionReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/req/ChannelAppealReq.java`

- [ ] **Step 1: 创建 VO 类**

```java
// ChannelStatsVO.java
package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "频道统计数据响应")
public class ChannelStatsVO {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "订阅数")
    private Integer subscriberCount;

    @Schema(description = "内容数")
    private Integer contentCount;

    @Schema(description = "浏览量")
    private Long pv;

    @Schema(description = "访客数")
    private Long uv;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "评论数")
    private Long commentCount;

    @Schema(description = "收藏数")
    private Long favoriteCount;

    @Schema(description = "分享数")
    private Long shareCount;

    @Schema(description = "有效访问数")
    private Long effectiveVisitCount;

    @Schema(description = "数据更新时间")
    private LocalDateTime dataUpdateTime;
}
```

```java
// ChannelTrendVO.java
package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Schema(description = "趋势数据响应")
public class ChannelTrendVO {

    @Schema(description = "日期列表")
    private List<LocalDate> dates;

    @Schema(description = "订阅数趋势")
    private List<Integer> subscriberCounts;

    @Schema(description = "内容数趋势")
    private List<Integer> contentCounts;

    @Schema(description = "PV趋势")
    private List<Long> pvs;

    @Schema(description = "UV趋势")
    private List<Long> uvs;
}
```

```java
// ChannelHotContentVO.java
package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "热门内容响应")
public class ChannelHotContentVO {

    @Schema(description = "内容ID")
    private String contentId;

    @Schema(description = "内容标题")
    private String title;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "有效互动量")
    private Long effectiveInteractionCount;

    @Schema(description = "排名")
    private Integer rank;
}
```

```java
// ChannelUserAnalysisVO.java
package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@Schema(description = "用户分析响应")
public class ChannelUserAnalysisVO {

    @Schema(description = "新增订阅数")
    private Integer newSubscriberCount;

    @Schema(description = "流失订阅数")
    private Integer lostSubscriberCount;

    @Schema(description = "成员活跃度占比")
    private Map<String, Integer> activityDistribution;

    @Schema(description = "贡献排行")
    private List<Map<String, Object>> contributionRanking;
}
```

```java
// ChannelExportTaskVO.java
package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "导出任务响应")
public class ChannelExportTaskVO {

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "文件下载地址")
    private String downloadUrl;

    @Schema(description = "数据行数")
    private Integer rowCount;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "错误信息")
    private String errorMessage;
}
```

```java
// ChannelReviewVO.java
package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "审核记录响应")
public class ChannelReviewVO {

    @Schema(description = "审核ID")
    private String reviewId;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "频道名称")
    private String channelName;

    @Schema(description = "审核类型")
    private String reviewType;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "申请人")
    private String applicantName;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "是否超时")
    private Boolean timeout;
}
```

```java
// ChannelLifecycleLogVO.java
package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "生命周期日志响应")
public class ChannelLifecycleLogVO {

    @Schema(description = "日志ID")
    private String logId;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "操作类型")
    private String actionType;

    @Schema(description = "变更前状态")
    private String fromStatus;

    @Schema(description = "变更后状态")
    private String toStatus;

    @Schema(description = "操作人")
    private String operatorName;

    @Schema(description = "操作原因")
    private String reason;

    @Schema(description = "操作时间")
    private LocalDateTime createdTime;
}
```

- [ ] **Step 2: 创建 Req 类**

```java
// ChannelStatsQueryReq.java
package org.jeecg.modules.content.channel.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@Schema(description = "统计查询请求")
public class ChannelStatsQueryReq {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID", required = true)
    private String channelId;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "统计类型：daily/weekly/monthly")
    private String statType;
}
```

```java
// ChannelExportReq.java
package org.jeecg.modules.content.channel.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "导出请求")
public class ChannelExportReq {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID", required = true)
    private String channelId;

    @NotBlank(message = "导出类型不能为空")
    @Schema(description = "导出类型：core_stats/interaction/user_analysis", required = true)
    private String exportType;

    @NotBlank(message = "文件格式不能为空")
    @Pattern(regexp = "^(xlsx|csv)$", message = "文件格式必须是xlsx或csv")
    @Schema(description = "文件格式：xlsx/csv", required = true)
    private String fileFormat;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "导出字段列表")
    private List<String> fields;
}
```

```java
// ChannelReviewActionReq.java
package org.jeecg.modules.content.channel.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "审核操作请求")
public class ChannelReviewActionReq {

    @NotBlank(message = "审核ID不能为空")
    @Schema(description = "审核ID", required = true)
    private String reviewId;

    @NotBlank(message = "审核结果不能为空")
    @Schema(description = "审核结果：approved/rejected/returned", required = true)
    private String action;

    @Schema(description = "审核原因")
    private String reason;
}
```

```java
// ChannelLifecycleActionReq.java
package org.jeecg.modules.content.channel.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "生命周期操作请求")
public class ChannelLifecycleActionReq {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID", required = true)
    private String channelId;

    @NotBlank(message = "操作类型不能为空")
    @Schema(description = "操作类型：freeze/unfreeze/hide/restrict_recommend/close/archive/merge", required = true)
    private String actionType;

    @Schema(description = "操作原因")
    private String reason;

    @Schema(description = "目标频道ID(合并时)")
    private String targetChannelId;
}
```

```java
// ChannelAppealReq.java
package org.jeecg.modules.content.channel.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Schema(description = "申诉请求")
public class ChannelAppealReq {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID", required = true)
    private String channelId;

    @NotBlank(message = "生命周期日志ID不能为空")
    @Schema(description = "关联的生命周期日志ID", required = true)
    private String lifecycleLogId;

    @NotBlank(message = "申诉理由不能为空")
    @Size(min = 10, max = 1000, message = "申诉理由长度10-1000字符")
    @Schema(description = "申诉理由", required = true)
    private String appealReason;

    @Schema(description = "附件URL列表")
    private List<String> attachmentUrls;
}
```

- [ ] **Step 3: 创建常量类**

```java
// ChannelStatsConstant.java
package org.jeecg.modules.content.channel.constant;

public class ChannelStatsConstant {

    public static final String STAT_TYPE_DAILY = "daily";
    public static final String STAT_TYPE_WEEKLY = "weekly";
    public static final String STAT_TYPE_MONTHLY = "monthly";

    public static final String EXPORT_TYPE_CORE_STATS = "core_stats";
    public static final String EXPORT_TYPE_INTERACTION = "interaction";
    public static final String EXPORT_TYPE_USER_ANALYSIS = "user_analysis";

    public static final String FILE_FORMAT_XLSX = "xlsx";
    public static final String FILE_FORMAT_CSV = "csv";

    public static final int EXPORT_THRESHOLD_ASYNC = 10000;
    public static final int EXPORT_FILE_EXPIRE_DAYS = 7;

    public static final int REVIEW_TIMEOUT_HOURS = 24;
    public static final int APPEAL_SLA_WORK_DAYS = 3;

    public static final int INACTIVITY_MONTHS = 6;
    public static final int INACTIVITY_REMINDER_MONTHS = 1;
}
```

- [ ] **Step 4: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/vo/
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/req/
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/constant/
git commit -m "feat(channel): add VO, Req and constant classes"
```

---

## Task 7: 统计看板 Biz 层 (TDD)

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/biz/ChannelStatsBizTest.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/ChannelStatsBiz.java`

- [ ] **Step 1: 编写 ChannelStatsBizTest 测试**

```java
package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.entity.ChannelStats;
import org.jeecg.modules.content.channel.service.IChannelStatsService;
import org.jeecg.modules.content.channel.vo.ChannelStatsVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelStatsBizTest {

    @Mock
    private IChannelStatsService statsService;

    @InjectMocks
    private ChannelStatsBiz channelStatsBiz;

    @Test
    void shouldGetCoreStats() {
        // Given
        String channelId = "CH001";
        ChannelStats stats = new ChannelStats()
                .setChannelId(channelId)
                .setSubscriberCount(1000)
                .setContentCount(50)
                .setPv(50000L)
                .setUv(10000L)
                .setUpdatedTime(LocalDateTime.now());

        when(statsService.getLatestStats(channelId)).thenReturn(stats);

        // When
        ChannelStatsVO result = channelStatsBiz.getCoreStats(channelId);

        // Then
        assertNotNull(result);
        assertEquals(1000, result.getSubscriberCount());
        assertEquals(50, result.getContentCount());
        assertEquals(50000L, result.getPv());
        assertEquals(10000L, result.getUv());
        verify(statsService).getLatestStats(channelId);
    }

    @Test
    void shouldReturnEmptyStatsWhenNoData() {
        // Given
        String channelId = "CH001";
        when(statsService.getLatestStats(channelId)).thenReturn(null);

        // When
        ChannelStatsVO result = channelStatsBiz.getCoreStats(channelId);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getSubscriberCount());
        assertEquals(0, result.getContentCount());
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

```bash
cd jeecg-boot
./mvnw test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelStatsBizTest -DfailIfNoTests=false
```
Expected: FAIL - ChannelStatsBiz class not found

- [ ] **Step 3: 实现 ChannelStatsBiz**

```java
package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.entity.ChannelStats;
import org.jeecg.modules.content.channel.service.IChannelStatsService;
import org.jeecg.modules.content.channel.vo.ChannelStatsVO;
import org.jeecg.modules.content.channel.vo.ChannelTrendVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChannelStatsBiz {

    @Resource
    private IChannelStatsService statsService;

    public ChannelStatsVO getCoreStats(String channelId) {
        ChannelStats stats = statsService.getLatestStats(channelId);
        if (stats == null) {
            return ChannelStatsVO.builder()
                    .channelId(channelId)
                    .subscriberCount(0)
                    .contentCount(0)
                    .pv(0L)
                    .uv(0L)
                    .likeCount(0L)
                    .commentCount(0L)
                    .favoriteCount(0L)
                    .shareCount(0L)
                    .effectiveVisitCount(0L)
                    .build();
        }
        return ChannelStatsVO.builder()
                .channelId(stats.getChannelId())
                .subscriberCount(stats.getSubscriberCount())
                .contentCount(stats.getContentCount())
                .pv(stats.getPv())
                .uv(stats.getUv())
                .likeCount(stats.getLikeCount())
                .commentCount(stats.getCommentCount())
                .favoriteCount(stats.getFavoriteCount())
                .shareCount(stats.getShareCount())
                .effectiveVisitCount(stats.getEffectiveVisitCount())
                .dataUpdateTime(stats.getUpdatedTime())
                .build();
    }

    public ChannelTrendVO getTrendData(String channelId, LocalDate startDate, LocalDate endDate, String statType) {
        List<ChannelStats> trendList = statsService.getTrendData(channelId, startDate, endDate, statType);
        return ChannelTrendVO.builder()
                .dates(trendList.stream().map(ChannelStats::getStatDate).collect(Collectors.toList()))
                .subscriberCounts(trendList.stream().map(ChannelStats::getSubscriberCount).collect(Collectors.toList()))
                .contentCounts(trendList.stream().map(ChannelStats::getContentCount).collect(Collectors.toList()))
                .pvs(trendList.stream().map(ChannelStats::getPv).collect(Collectors.toList()))
                .uvs(trendList.stream().map(ChannelStats::getUv).collect(Collectors.toList()))
                .build();
    }
}
```

- [ ] **Step 4: 运行测试验证通过**

```bash
cd jeecg-boot
./mvnw test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelStatsBizTest -DfailIfNoTests=false
```
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/biz/ChannelStatsBizTest.java
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/ChannelStatsBiz.java
git commit -m "feat(channel): add ChannelStatsBiz with TDD"
```

---

## Task 8: 统计看板 Controller

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelStatsController.java`

- [ ] **Step 1: 创建 ChannelStatsController**

```java
package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelStatsBiz;
import org.jeecg.modules.content.channel.constant.ChannelStatsConstant;
import org.jeecg.modules.content.channel.req.ChannelStatsQueryReq;
import org.jeecg.modules.content.channel.vo.ChannelStatsVO;
import org.jeecg.modules.content.channel.vo.ChannelTrendVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/content/channel/stats")
@Tag(name = "频道统计", description = "频道数据统计看板接口")
public class ChannelStatsController {

    @Resource
    private ChannelStatsBiz channelStatsBiz;

    @GetMapping("/core")
    @Operation(summary = "获取核心指标")
    public Result<ChannelStatsVO> getCoreStats(
            @Parameter(description = "频道ID", required = true)
            @RequestParam String channelId) {
        return Result.OK(channelStatsBiz.getCoreStats(channelId));
    }

    @GetMapping("/trend")
    @Operation(summary = "获取趋势数据")
    public Result<ChannelTrendVO> getTrendData(
            @Parameter(description = "频道ID", required = true)
            @RequestParam String channelId,
            @Parameter(description = "开始日期")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "统计类型：daily/weekly/monthly")
            @RequestParam(defaultValue = ChannelStatsConstant.STAT_TYPE_DAILY) String statType) {
        return Result.OK(channelStatsBiz.getTrendData(channelId, startDate, endDate, statType));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelStatsController.java
git commit -m "feat(channel): add ChannelStatsController"
```

---

## Task 9: 生命周期状态机 Biz (TDD)

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/biz/ChannelLifecycleBizTest.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/ChannelLifecycleBiz.java`

- [ ] **Step 1: 编写 ChannelLifecycleBizTest 测试**

```java
package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.entity.ChannelLifecycleLog;
import org.jeecg.modules.content.channel.enums.ChannelLifecycleStatus;
import org.jeecg.modules.content.channel.service.IChannelLifecycleLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelLifecycleBizTest {

    @Mock
    private IChannelLifecycleLogService lifecycleLogService;

    @InjectMocks
    private ChannelLifecycleBiz lifecycleBiz;

    @Test
    void shouldAllowFreezeFromActive() {
        // Given
        String channelId = "CH001";
        String operatorId = "USER001";
        String reason = "违规内容";

        when(lifecycleLogService.save(any(ChannelLifecycleLog.class))).thenReturn(true);

        // When
        assertDoesNotThrow(() -> lifecycleBiz.freeze(channelId, operatorId, reason));

        // Then
        verify(lifecycleLogService).save(any(ChannelLifecycleLog.class));
    }

    @Test
    void shouldThrowWhenFreezeFromInvalidStatus() {
        // Given
        String channelId = "CH001";
        String operatorId = "USER001";
        String reason = "违规内容";

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> lifecycleBiz.freeze(channelId, operatorId, reason, ChannelLifecycleStatus.ARCHIVED));
    }

    @Test
    void shouldAllowUnfreezeFromFrozen() {
        // Given
        String channelId = "CH001";
        String operatorId = "USER001";
        String reason = "整改完成";

        when(lifecycleLogService.save(any(ChannelLifecycleLog.class))).thenReturn(true);

        // When
        assertDoesNotThrow(() -> lifecycleBiz.unfreeze(channelId, operatorId, reason));

        // Then
        verify(lifecycleLogService).save(any(ChannelLifecycleLog.class));
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

```bash
cd jeecg-boot
./mvnw test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelLifecycleBizTest -DfailIfNoTests=false
```
Expected: FAIL - ChannelLifecycleBiz class not found

- [ ] **Step 3: 实现 ChannelLifecycleBiz**

```java
package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.entity.ChannelLifecycleLog;
import org.jeecg.modules.content.channel.enums.ChannelLifecycleStatus;
import org.jeecg.modules.content.channel.service.IChannelLifecycleLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class ChannelLifecycleBiz {

    private static final Set<ChannelLifecycleStatus> FREEZE_ALLOWED_FROM = new HashSet<>(
            Arrays.asList(ChannelLifecycleStatus.ACTIVE, ChannelLifecycleStatus.READONLY_FROZEN)
    );

    private static final Set<ChannelLifecycleStatus> UNFREEZE_ALLOWED_FROM = new HashSet<>(
            Arrays.asList(ChannelLifecycleStatus.READONLY_FROZEN)
    );

    private static final Set<ChannelLifecycleStatus> HIDE_ALLOWED_FROM = new HashSet<>(
            Arrays.asList(ChannelLifecycleStatus.ACTIVE, ChannelLifecycleStatus.READONLY_FROZEN)
    );

    private static final Set<ChannelLifecycleStatus> CLOSE_ALLOWED_FROM = new HashSet<>(
            Arrays.asList(ChannelLifecycleStatus.ACTIVE, ChannelLifecycleStatus.READONLY_FROZEN, ChannelLifecycleStatus.HIDDEN)
    );

    @Resource
    private IChannelLifecycleLogService lifecycleLogService;

    @Transactional(rollbackFor = Exception.class)
    public void freeze(String channelId, String operatorId, String reason) {
        freeze(channelId, operatorId, reason, ChannelLifecycleStatus.ACTIVE);
    }

    @Transactional(rollbackFor = Exception.class)
    public void freeze(String channelId, String operatorId, String reason, ChannelLifecycleStatus currentStatus) {
        validateTransition(currentStatus, ChannelLifecycleStatus.READONLY_FROZEN, FREEZE_ALLOWED_FROM);
        saveLog(channelId, "freeze", currentStatus.getCode(), ChannelLifecycleStatus.READONLY_FROZEN.getCode(), operatorId, reason, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unfreeze(String channelId, String operatorId, String reason) {
        validateTransition(ChannelLifecycleStatus.READONLY_FROZEN, ChannelLifecycleStatus.ACTIVE, UNFREEZE_ALLOWED_FROM);
        saveLog(channelId, "unfreeze", ChannelLifecycleStatus.READONLY_FROZEN.getCode(), ChannelLifecycleStatus.ACTIVE.getCode(), operatorId, reason, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void hide(String channelId, String operatorId, String reason) {
        validateTransition(ChannelLifecycleStatus.ACTIVE, ChannelLifecycleStatus.HIDDEN, HIDE_ALLOWED_FROM);
        saveLog(channelId, "hide", ChannelLifecycleStatus.ACTIVE.getCode(), ChannelLifecycleStatus.HIDDEN.getCode(), operatorId, reason, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void close(String channelId, String operatorId, String reason) {
        validateTransition(ChannelLifecycleStatus.ACTIVE, ChannelLifecycleStatus.CLOSED, CLOSE_ALLOWED_FROM);
        saveLog(channelId, "close", ChannelLifecycleStatus.ACTIVE.getCode(), ChannelLifecycleStatus.CLOSED.getCode(), operatorId, reason, null);
    }

    private void validateTransition(ChannelLifecycleStatus from, ChannelLifecycleStatus to, Set<ChannelLifecycleStatus> allowedFrom) {
        if (!allowedFrom.contains(from)) {
            throw new IllegalStateException(
                    String.format("不允许从 %s 状态转换到 %s 状态", from.getCode(), to.getCode()));
        }
    }

    private void saveLog(String channelId, String actionType, String fromStatus, String toStatus,
                         String operatorId, String reason, String targetChannelId) {
        ChannelLifecycleLog log = new ChannelLifecycleLog()
                .setLogId(UUID.randomUUID().toString())
                .setChannelId(channelId)
                .setActionType(actionType)
                .setFromStatus(fromStatus)
                .setToStatus(toStatus)
                .setOperatorId(operatorId)
                .setReason(reason)
                .setTargetChannelId(targetChannelId);
        lifecycleLogService.save(log);
    }
}
```

- [ ] **Step 4: 运行测试验证通过**

```bash
cd jeecg-boot
./mvnw test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelLifecycleBizTest -DfailIfNoTests=false
```
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/biz/ChannelLifecycleBizTest.java
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/ChannelLifecycleBiz.java
git commit -m "feat(channel): add ChannelLifecycleBiz with TDD"
```

---

## Task 10: 生命周期 Controller

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelLifecycleController.java`

- [ ] **Step 1: 创建 ChannelLifecycleController**

```java
package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelLifecycleBiz;
import org.jeecg.modules.content.channel.req.ChannelLifecycleActionReq;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/content/channel/lifecycle")
@Tag(name = "频道生命周期", description = "频道生命周期管理接口")
public class ChannelLifecycleController {

    @Resource
    private ChannelLifecycleBiz lifecycleBiz;

    @PostMapping("/freeze")
    @Operation(summary = "冻结频道")
    public Result<Void> freeze(@Valid @RequestBody ChannelLifecycleActionReq req) {
        lifecycleBiz.freeze(req.getChannelId(), getCurrentUserId(), req.getReason());
        return Result.OK();
    }

    @PostMapping("/unfreeze")
    @Operation(summary = "解冻频道")
    public Result<Void> unfreeze(@Valid @RequestBody ChannelLifecycleActionReq req) {
        lifecycleBiz.unfreeze(req.getChannelId(), getCurrentUserId(), req.getReason());
        return Result.OK();
    }

    @PostMapping("/hide")
    @Operation(summary = "强制隐藏频道")
    public Result<Void> hide(@Valid @RequestBody ChannelLifecycleActionReq req) {
        lifecycleBiz.hide(req.getChannelId(), getCurrentUserId(), req.getReason());
        return Result.OK();
    }

    @PostMapping("/close")
    @Operation(summary = "永久关闭频道")
    public Result<Void> close(@Valid @RequestBody ChannelLifecycleActionReq req) {
        lifecycleBiz.close(req.getChannelId(), getCurrentUserId(), req.getReason());
        return Result.OK();
    }

    private String getCurrentUserId() {
        // TODO: 从安全上下文获取当前用户ID
        return "current-user-id";
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelLifecycleController.java
git commit -m "feat(channel): add ChannelLifecycleController"
```

---

## Task 11: 审核流程 Controller

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelReviewController.java`

- [ ] **Step 1: 创建 ChannelReviewController**

```java
package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.entity.ChannelReview;
import org.jeecg.modules.content.channel.req.ChannelReviewActionReq;
import org.jeecg.modules.content.channel.service.IChannelReviewService;
import org.jeecg.modules.content.channel.vo.ChannelReviewVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/content/channel/review")
@Tag(name = "频道审核", description = "频道审核管理接口")
public class ChannelReviewController {

    @Resource
    private IChannelReviewService reviewService;

    @GetMapping("/list")
    @Operation(summary = "审核队列列表")
    public Result<Page<ChannelReviewVO>> listReviews(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String reviewType) {

        LambdaQueryWrapper<ChannelReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, ChannelReview::getStatus, status);
        wrapper.eq(reviewType != null, ChannelReview::getReviewType, reviewType);
        wrapper.orderByDesc(ChannelReview::getSubmitTime);

        Page<ChannelReview> page = reviewService.page(new Page<>(current, size), wrapper);
        Page<ChannelReviewVO> voPage = page.convert(this::convertToVO);
        return Result.OK(voPage);
    }

    @PostMapping("/action")
    @Operation(summary = "审核操作")
    public Result<Void> reviewAction(@Valid @RequestBody ChannelReviewActionReq req) {
        ChannelReview review = reviewService.getById(req.getReviewId());
        if (review == null) {
            return Result.error("审核记录不存在");
        }

        review.setStatus(req.getAction());
        review.setReviewerId(getCurrentUserId());
        review.setReviewReason(req.getReason());
        review.setReviewTime(LocalDateTime.now());
        reviewService.updateById(review);

        return Result.OK();
    }

    private ChannelReviewVO convertToVO(ChannelReview review) {
        return ChannelReviewVO.builder()
                .reviewId(review.getReviewId())
                .channelId(review.getChannelId())
                .reviewType(review.getReviewType())
                .status(review.getStatus())
                .submitTime(review.getSubmitTime())
                .timeout(review.getTimeoutFlag() == 1)
                .build();
    }

    private String getCurrentUserId() {
        // TODO: 从安全上下文获取当前用户ID
        return "current-user-id";
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelReviewController.java
git commit -m "feat(channel): add ChannelReviewController"
```

---

## Task 12: 导出 Controller

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelExportController.java`

- [ ] **Step 1: 创建 ChannelExportController**

```java
package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelExportBiz;
import org.jeecg.modules.content.channel.req.ChannelExportReq;
import org.jeecg.modules.content.channel.vo.ChannelExportTaskVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/content/channel/export")
@Tag(name = "频道数据导出", description = "频道数据导出接口")
public class ChannelExportController {

    @Resource
    private ChannelExportBiz exportBiz;

    @PostMapping("/create")
    @Operation(summary = "创建导出任务")
    public Result<ChannelExportTaskVO> createExport(@Valid @RequestBody ChannelExportReq req) {
        return Result.OK(exportBiz.createExport(req, getCurrentUserId()));
    }

    @GetMapping("/status")
    @Operation(summary = "查询导出状态")
    public Result<ChannelExportTaskVO> getExportStatus(@RequestParam String taskId) {
        return Result.OK(exportBiz.getExportStatus(taskId));
    }

    private String getCurrentUserId() {
        // TODO: 从安全上下文获取当前用户ID
        return "current-user-id";
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelExportController.java
git commit -m "feat(channel): add ChannelExportController"
```

---

## Task 13: 验证与集成测试

- [ ] **Step 1: 运行所有单元测试**

```bash
cd jeecg-boot
./mvnw test -pl jeecg-boot-module/jeecg-module-content -DfailIfNoTests=false
```
Expected: All tests PASS

- [ ] **Step 2: 验证编译通过**

```bash
cd jeecg-boot
./mvnw compile -pl jeecg-boot-module/jeecg-module-content
```
Expected: BUILD SUCCESS

- [ ] **Step 3: 验证数据库迁移脚本**

```bash
# 检查 Flyway 迁移脚本语法
cd jeecg-boot
./mvnw flyway:validate -pl jeecg-boot-module/jeecg-module-content
```

- [ ] **Step 4: Final Commit**

```bash
git add -A
git commit -m "feat(channel): complete lifecycle stats implementation"
```

---

## Summary

| Task | Description | Files Created |
|------|-------------|---------------|
| 1 | 数据库迁移脚本 | 1 SQL file |
| 2 | 枚举类 | 5 enum classes |
| 3 | 实体类 | 5 entity classes |
| 4 | Mapper 接口 | 5 mapper + 1 XML |
| 5 | Service 层 | 5 interface + 5 impl |
| 6 | VO/Req/Constant | 7 VO + 5 Req + 1 Constant |
| 7 | 统计看板 Biz | 1 Biz + 1 Test |
| 8 | 统计看板 Controller | 1 Controller |
| 9 | 生命周期 Biz | 1 Biz + 1 Test |
| 10 | 生命周期 Controller | 1 Controller |
| 11 | 审核 Controller | 1 Controller |
| 12 | 导出 Controller | 1 Controller |
| 13 | 验证 | Integration tests |
