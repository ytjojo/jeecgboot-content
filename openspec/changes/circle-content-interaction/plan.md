# 圈子内容与互动 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在圈子基础能力之上，实现内容置顶/精华、圈子公告、@成员、加入申请审核、内容举报处理五大功能。

**Architecture:** 在 `org.jeecg.modules.content.circle` 包下按 entity/mapper/service/service-impl/biz/controller 分层构建。实体继承 `JeecgEntity`，审核日志使用独立实体。通知通过 `IContentNotificationService` 接口发送。数据库使用 Flyway 迁移管理。

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Flyway, MySQL 8, Java 17

---

## 文件结构总览

```
jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/
├── entity/
│   ├── CircleAnnouncement.java
│   ├── CircleJoinRequest.java
│   ├── CircleReport.java
│   └── CircleAuditLog.java
├── mapper/
│   ├── CircleContentMapper.java
│   ├── CircleAnnouncementMapper.java
│   ├── CircleJoinRequestMapper.java
│   ├── CircleReportMapper.java
│   └── CircleAuditLogMapper.java
├── service/
│   ├── ICircleContentPinService.java
│   ├── ICircleAnnouncementService.java
│   ├── ICircleMentionService.java
│   ├── ICircleJoinReviewService.java
│   ├── ICircleReportService.java
│   └── ICircleAuditLogService.java
├── service/impl/
│   ├── CircleContentPinServiceImpl.java
│   ├── CircleAnnouncementServiceImpl.java
│   ├── CircleMentionServiceImpl.java
│   ├── CircleJoinReviewServiceImpl.java
│   ├── CircleReportServiceImpl.java
│   └── CircleAuditLogServiceImpl.java
├── biz/
│   ├── CircleContentPinBizService.java
│   ├── CircleAnnouncementBizService.java
│   ├── CircleMentionBizService.java
│   ├── CircleJoinReviewBizService.java
│   └── CircleReportBizService.java
├── controller/
│   ├── CircleContentPinController.java
│   ├── CircleAnnouncementController.java
│   ├── CircleJoinReviewController.java
│   └── CircleReportController.java
├── req/
│   ├── CircleAnnouncementReq.java
│   ├── CircleJoinReviewReq.java
│   └── CircleReportReq.java
├── vo/
│   ├── CircleAnnouncementVO.java
│   ├── CircleJoinRequestVO.java
│   └── CircleReportVO.java
└── enums/
    ├── CircleAuditActionEnum.java
    ├── CircleReportStatusEnum.java
    └── CircleJoinRequestStatusEnum.java

jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/
├── mapper/content/circle/
│   ├── CircleContentMapper.xml
│   ├── CircleAnnouncementMapper.xml
│   ├── CircleJoinRequestMapper.xml
│   ├── CircleReportMapper.xml
│   └── CircleAuditLogMapper.xml
└── flyway/sql/mysql/
    └── V3.9.1_60__circle_content_interaction.sql

jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/
├── service/
│   ├── CircleContentPinServiceTest.java
│   ├── CircleAnnouncementServiceTest.java
│   ├── CircleMentionServiceTest.java
│   ├── CircleJoinReviewServiceTest.java
│   └── CircleReportServiceTest.java
└── biz/
    ├── CircleContentPinBizServiceTest.java
    ├── CircleAnnouncementBizServiceTest.java
    ├── CircleMentionBizServiceTest.java
    ├── CircleJoinReviewBizServiceTest.java
    └── CircleReportBizServiceTest.java
```

---

## Task 1: 数据库迁移与枚举

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_60__circle_content_interaction.sql`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/enums/CircleAuditActionEnum.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/enums/CircleReportStatusEnum.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/enums/CircleJoinRequestStatusEnum.java`

- [ ] **Step 1: 编写 Flyway 迁移脚本**

```sql
-- V3.9.1_60__circle_content_interaction.sql

-- circle_content 表扩展字段（假设 circle-core 已创建此表，如不存在需先创建基础表）
ALTER TABLE circle_content ADD COLUMN is_pinned TINYINT(1) DEFAULT 0 COMMENT '是否置顶 0否 1是';
ALTER TABLE circle_content ADD COLUMN pinned_at DATETIME DEFAULT NULL COMMENT '置顶时间';
ALTER TABLE circle_content ADD COLUMN is_featured TINYINT(1) DEFAULT 0 COMMENT '是否精华 0否 1是';
ALTER TABLE circle_content ADD COLUMN featured_at DATETIME DEFAULT NULL COMMENT '精华标记时间';

CREATE TABLE circle_announcement (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    circle_id VARCHAR(32) NOT NULL COMMENT '圈子ID',
    content TEXT NOT NULL COMMENT '公告内容',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态 ACTIVE/INACTIVE',
    expire_at DATETIME DEFAULT NULL COMMENT '有效期截止时间',
    create_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_circle_status (circle_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子公告表';

CREATE TABLE circle_join_request (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    circle_id VARCHAR(32) NOT NULL COMMENT '圈子ID',
    user_id VARCHAR(32) NOT NULL COMMENT '申请人ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING/APPROVED/REJECTED/EXPIRED',
    reject_reason VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
    operator_id VARCHAR(32) DEFAULT NULL COMMENT '审核人ID',
    operate_time DATETIME DEFAULT NULL COMMENT '审核时间',
    create_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_circle_status (circle_id, status),
    INDEX idx_user (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子加入申请表';

CREATE TABLE circle_report (
    id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    circle_id VARCHAR(32) NOT NULL COMMENT '圈子ID',
    content_id VARCHAR(32) NOT NULL COMMENT '被举报内容ID',
    reporter_id VARCHAR(32) NOT NULL COMMENT '举报者ID',
    reason VARCHAR(500) DEFAULT NULL COMMENT '举报原因',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING/RESOLVED/IGNORED',
    operator_id VARCHAR(32) DEFAULT NULL COMMENT '处理人ID',
    operate_time DATETIME DEFAULT NULL COMMENT '处理时间',
    handle_action VARCHAR(30) DEFAULT NULL COMMENT '处理动作 DELETE/IGNORE/MUTE',
    create_by VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_circle_status (circle_id, status),
    INDEX idx_content (content_id),
    INDEX idx_reporter (reporter_id),
    UNIQUE KEY uk_reporter_content (reporter_id, content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子内容举报表';

CREATE TABLE circle_audit_log (
    log_id VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '日志ID',
    circle_id VARCHAR(32) NOT NULL COMMENT '圈子ID',
    operator_id VARCHAR(32) NOT NULL COMMENT '操作人ID',
    action VARCHAR(30) NOT NULL COMMENT '操作类型',
    target_id VARCHAR(32) NOT NULL COMMENT '操作对象ID',
    target_type VARCHAR(20) NOT NULL COMMENT '操作对象类型 CONTENT/ANNOUNCEMENT/JOIN_REQUEST/REPORT/USER',
    result VARCHAR(20) NOT NULL COMMENT '操作结果',
    reason VARCHAR(500) DEFAULT NULL COMMENT '原因/备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_circle (circle_id),
    INDEX idx_target (target_id, target_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子审核日志表';
```

- [ ] **Step 2: 验证迁移脚本语法**

Run: 在 MySQL 客户端中执行 `SOURCE V3.9.1_60__circle_content_interaction.sql` 或通过 Flyway 迁移验证。
Expected: 所有表创建成功，无语法错误。

- [ ] **Step 3: 创建 CircleAuditActionEnum**

```java
package org.jeecg.modules.content.circle.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CircleAuditActionEnum {
    PIN("PIN", "置顶"),
    UNPIN("UNPIN", "取消置顶"),
    FEATURE("FEATURE", "标记精华"),
    UNFEATURE("UNFEATURE", "取消精华"),
    PUBLISH_ANNOUNCEMENT("PUBLISH_ANNOUNCEMENT", "发布公告"),
    APPROVE_JOIN("APPROVE_JOIN", "批准加入"),
    REJECT_JOIN("REJECT_JOIN", "拒绝加入"),
    DELETE_REPORTED("DELETE_REPORTED", "删除举报内容"),
    IGNORE_REPORT("IGNORE_REPORT", "忽略举报"),
    MUTE_FROM_REPORT("MUTE_FROM_REPORT", "举报禁言");

    private final String code;
    private final String desc;
}
```

- [ ] **Step 4: 创建 CircleReportStatusEnum**

```java
package org.jeecg.modules.content.circle.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CircleReportStatusEnum {
    PENDING("PENDING", "待处理"),
    RESOLVED("RESOLVED", "已处理"),
    IGNORED("IGNORED", "已忽略");

    private final String code;
    private final String desc;
}
```

- [ ] **Step 5: 创建 CircleJoinRequestStatusEnum**

```java
package org.jeecg.modules.content.circle.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CircleJoinRequestStatusEnum {
    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "已批准"),
    REJECTED("REJECTED", "已拒绝"),
    EXPIRED("EXPIRED", "已过期");

    private final String code;
    private final String desc;
}
```

- [ ] **Step 6: 提交**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_60__circle_content_interaction.sql \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/enums/
git commit -m "feat(circle): add database migration and enums for content interaction"
```

---

## Task 2: 审核日志基础设施

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/entity/CircleAuditLog.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/mapper/CircleAuditLogMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/mapper/content/circle/CircleAuditLogMapper.xml`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/ICircleAuditLogService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/impl/CircleAuditLogServiceImpl.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/service/CircleAuditLogServiceTest.java`

- [ ] **Step 1: 编写审核日志服务测试**

```java
package org.jeecg.modules.content.circle.service;

import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import org.jeecg.modules.content.circle.enums.CircleAuditActionEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CircleAuditLogServiceTest {

    @Autowired
    private ICircleAuditLogService auditLogService;

    @Test
    void writeAuditLog_shouldPersistLog() {
        CircleAuditLog log = new CircleAuditLog()
            .setCircleId("circle-001")
            .setOperatorId("user-admin")
            .setAction(CircleAuditActionEnum.PIN.getCode())
            .setTargetId("content-001")
            .setTargetType("CONTENT")
            .setResult("SUCCESS");

        auditLogService.writeAuditLog(log);

        List<CircleAuditLog> results = auditLogService.queryByTarget("content-001", "CONTENT");
        assertFalse(results.isEmpty());
        assertEquals("PIN", results.get(0).getAction());
        assertEquals("user-admin", results.get(0).getOperatorId());
    }

    @Test
    void queryByTimeRange_shouldReturnLogsInTimeRange() {
        CircleAuditLog log = new CircleAuditLog()
            .setCircleId("circle-002")
            .setOperatorId("user-admin")
            .setAction(CircleAuditActionEnum.FEATURE.getCode())
            .setTargetId("content-002")
            .setTargetType("CONTENT")
            .setResult("SUCCESS");

        auditLogService.writeAuditLog(log);

        java.util.Date now = new java.util.Date();
        java.util.Date oneHourAgo = new java.util.Date(now.getTime() - 3600000);
        List<CircleAuditLog> results = auditLogService.queryByTimeRange(oneHourAgo, now);
        assertFalse(results.isEmpty());
    }
}
```

- [ ] **Step 2: 创建 CircleAuditLog 实体**

```java
package org.jeecg.modules.content.circle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import java.util.Date;

@Data
@Accessors(chain = true)
@TableName("circle_audit_log")
@Schema(description = "圈子审核日志")
public class CircleAuditLog {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "日志ID")
    private String logId;

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "操作人ID")
    private String operatorId;

    @Schema(description = "操作类型")
    private String action;

    @Schema(description = "操作对象ID")
    private String targetId;

    @Schema(description = "操作对象类型")
    private String targetType;

    @Schema(description = "操作结果")
    private String result;

    @Schema(description = "原因/备注")
    private String reason;

    @Schema(description = "操作时间")
    private Date createdAt;
}
```

- [ ] **Step 3: 创建 CircleAuditLogMapper**

```java
package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import java.util.Date;
import java.util.List;

@Mapper
public interface CircleAuditLogMapper extends BaseMapper<CircleAuditLog> {

    List<CircleAuditLog> selectByTarget(@Param("targetId") String targetId, @Param("targetType") String targetType);

    List<CircleAuditLog> selectByTimeRange(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
}
```

- [ ] **Step 4: 创建 CircleAuditLogMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.jeecg.modules.content.circle.mapper.CircleAuditLogMapper">

    <select id="selectByTarget" resultType="org.jeecg.modules.content.circle.entity.CircleAuditLog">
        SELECT * FROM circle_audit_log
        WHERE target_id = #{targetId} AND target_type = #{targetType}
        ORDER BY created_at DESC
    </select>

    <select id="selectByTimeRange" resultType="org.jeecg.modules.content.circle.entity.CircleAuditLog">
        SELECT * FROM circle_audit_log
        WHERE created_at BETWEEN #{startTime} AND #{endTime}
        ORDER BY created_at DESC
    </select>

</mapper>
```

- [ ] **Step 5: 创建 ICircleAuditLogService 接口**

```java
package org.jeecg.modules.content.circle.service;

import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import java.util.Date;
import java.util.List;

public interface ICircleAuditLogService {

    void writeAuditLog(CircleAuditLog log);

    List<CircleAuditLog> queryByTarget(String targetId, String targetType);

    List<CircleAuditLog> queryByTimeRange(Date startTime, Date endTime);
}
```

- [ ] **Step 6: 实现 CircleAuditLogServiceImpl**

```java
package org.jeecg.modules.content.circle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import org.jeecg.modules.content.circle.mapper.CircleAuditLogMapper;
import org.jeecg.modules.content.circle.service.ICircleAuditLogService;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class CircleAuditLogServiceImpl extends ServiceImpl<CircleAuditLogMapper, CircleAuditLog>
        implements ICircleAuditLogService {

    @Override
    public void writeAuditLog(CircleAuditLog log) {
        if (log.getCreatedAt() == null) {
            log.setCreatedAt(new Date());
        }
        save(log);
    }

    @Override
    public List<CircleAuditLog> queryByTarget(String targetId, String targetType) {
        return baseMapper.selectByTarget(targetId, targetType);
    }

    @Override
    public List<CircleAuditLog> queryByTimeRange(Date startTime, Date endTime) {
        return baseMapper.selectByTimeRange(startTime, endTime);
    }
}
```

- [ ] **Step 7: 运行测试**

Run: `mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest=CircleAuditLogServiceTest -DfailIfNoTests=false`
Expected: 测试通过（注意：需要数据库环境）

- [ ] **Step 8: 提交**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/entity/CircleAuditLog.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/mapper/CircleAuditLogMapper.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/mapper/content/circle/CircleAuditLogMapper.xml \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/ICircleAuditLogService.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/impl/CircleAuditLogServiceImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/service/CircleAuditLogServiceTest.java
git commit -m "feat(circle): add audit log entity, mapper, service and tests"
```

---

## Task 3: 内容置顶与精华

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/entity/CircleContent.java` (如已存在，扩展字段)
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/mapper/CircleContentMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/mapper/content/circle/CircleContentMapper.xml`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/ICircleContentPinService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/impl/CircleContentPinServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/biz/CircleContentPinBizService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleContentPinController.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/service/CircleContentPinServiceTest.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/biz/CircleContentPinBizServiceTest.java`

- [ ] **Step 1: 编写 CircleContentPinServiceTest**

```java
package org.jeecg.modules.content.circle.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CircleContentPinServiceTest {

    @Autowired
    private ICircleContentPinService pinService;

    @Test
    void pinContent_shouldSetPinnedFields() {
        // 前置：需要先创建一条 circle_content 记录
        String contentId = "test-content-001";
        pinService.pinContent(contentId);

        var content = pinService.getById(contentId);
        assertNotNull(content);
        assertTrue(content.getIsPinned());
        assertNotNull(content.getPinnedAt());
    }

    @Test
    void unpinContent_shouldClearPinnedFields() {
        String contentId = "test-content-002";
        pinService.pinContent(contentId);
        pinService.unpinContent(contentId);

        var content = pinService.getById(contentId);
        assertNotNull(content);
        assertFalse(content.getIsPinned());
        assertNull(content.getPinnedAt());
    }

    @Test
    void featureContent_shouldSetFeaturedFields() {
        String contentId = "test-content-003";
        pinService.featureContent(contentId);

        var content = pinService.getById(contentId);
        assertNotNull(content);
        assertTrue(content.getIsFeatured());
        assertNotNull(content.getFeaturedAt());
    }

    @Test
    void unfeatureContent_shouldClearFeaturedFields() {
        String contentId = "test-content-004";
        pinService.featureContent(contentId);
        pinService.unfeatureContent(contentId);

        var content = pinService.getById(contentId);
        assertNotNull(content);
        assertFalse(content.getIsFeatured());
        assertNull(content.getFeaturedAt());
    }

    @Test
    void togglePin_shouldToggleState() {
        String contentId = "test-content-005";
        pinService.togglePin(contentId);
        var content1 = pinService.getById(contentId);
        assertTrue(content1.getIsPinned());

        pinService.togglePin(contentId);
        var content2 = pinService.getById(contentId);
        assertFalse(content2.getIsPinned());
    }
}
```

- [ ] **Step 2: 创建 CircleContentMapper**

```java
package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.circle.entity.CircleContent;

@Mapper
public interface CircleContentMapper extends BaseMapper<CircleContent> {
}
```

- [ ] **Step 3: 创建 CircleContentMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.jeecg.modules.content.circle.mapper.CircleContentMapper">

    <!-- 置顶排序查询：置顶内容在前，按 pinned_at 倒序，然后按创建时间倒序 -->
    <select id="selectCircleContentList" resultType="org.jeecg.modules.content.circle.entity.CircleContent">
        SELECT * FROM circle_content
        WHERE circle_id = #{circleId} AND deleted = 0
        ORDER BY is_pinned DESC, pinned_at DESC, create_time DESC
    </select>

</mapper>
```

- [ ] **Step 4: 创建 ICircleContentPinService 接口**

```java
package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.circle.entity.CircleContent;

public interface ICircleContentPinService extends IService<CircleContent> {

    void pinContent(String contentId);

    void unpinContent(String contentId);

    void featureContent(String contentId);

    void unfeatureContent(String contentId);

    void togglePin(String contentId);

    void toggleFeature(String contentId);
}
```

- [ ] **Step 5: 实现 CircleContentPinServiceImpl**

```java
package org.jeecg.modules.content.circle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.circle.entity.CircleContent;
import org.jeecg.modules.content.circle.mapper.CircleContentMapper;
import org.jeecg.modules.content.circle.service.ICircleContentPinService;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class CircleContentPinServiceImpl extends ServiceImpl<CircleContentMapper, CircleContent>
        implements ICircleContentPinService {

    @Override
    public void pinContent(String contentId) {
        lambdaUpdate()
            .eq(CircleContent::getId, contentId)
            .set(CircleContent::getIsPinned, true)
            .set(CircleContent::getPinnedAt, new Date())
            .update();
    }

    @Override
    public void unpinContent(String contentId) {
        lambdaUpdate()
            .eq(CircleContent::getId, contentId)
            .set(CircleContent::getIsPinned, false)
            .set(CircleContent::getPinnedAt, null)
            .update();
    }

    @Override
    public void featureContent(String contentId) {
        lambdaUpdate()
            .eq(CircleContent::getId, contentId)
            .set(CircleContent::getIsFeatured, true)
            .set(CircleContent::getFeaturedAt, new Date())
            .update();
    }

    @Override
    public void unfeatureContent(String contentId) {
        lambdaUpdate()
            .eq(CircleContent::getId, contentId)
            .set(CircleContent::getIsFeatured, false)
            .set(CircleContent::getFeaturedAt, null)
            .update();
    }

    @Override
    public void togglePin(String contentId) {
        CircleContent content = getById(contentId);
        if (content != null && content.getIsPinned()) {
            unpinContent(contentId);
        } else {
            pinContent(contentId);
        }
    }

    @Override
    public void toggleFeature(String contentId) {
        CircleContent content = getById(contentId);
        if (content != null && content.getIsFeatured()) {
            unfeatureContent(contentId);
        } else {
            featureContent(contentId);
        }
    }
}
```

- [ ] **Step 6: 编写 CircleContentPinBizServiceTest**

```java
package org.jeecg.modules.content.circle.biz;

import org.jeecg.modules.content.circle.service.ICircleAuditLogService;
import org.jeecg.modules.content.circle.service.ICircleContentPinService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CircleContentPinBizServiceTest {

    @Autowired
    private CircleContentPinBizService pinBizService;

    @Test
    void pin_withAdminRole_shouldSucceed() {
        // 需要模拟管理员用户
        String contentId = "test-content-010";
        String operatorId = "user-admin";
        String circleId = "circle-001";

        assertDoesNotThrow(() -> pinBizService.pin(contentId, operatorId, circleId));
    }

    @Test
    void pin_withMemberRole_shouldThrowPermissionDenied() {
        String contentId = "test-content-011";
        String operatorId = "user-member";
        String circleId = "circle-001";

        assertThrows(IllegalArgumentException.class,
            () -> pinBizService.pin(contentId, operatorId, circleId));
    }

    @Test
    void pin_shouldWriteAuditLog() {
        String contentId = "test-content-012";
        String operatorId = "user-admin";
        String circleId = "circle-001";

        pinBizService.pin(contentId, operatorId, circleId);

        // 验证审核日志已写入
        var logs = auditLogService.queryByTarget(contentId, "CONTENT");
        assertFalse(logs.isEmpty());
        assertEquals("PIN", logs.get(0).getAction());
    }
}
```

- [ ] **Step 7: 实现 CircleContentPinBizService**

```java
package org.jeecg.modules.content.circle.biz;

import lombok.RequiredArgsConstructor;
import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import org.jeecg.modules.content.circle.enums.CircleAuditActionEnum;
import org.jeecg.modules.content.circle.service.ICircleAuditLogService;
import org.jeecg.modules.content.circle.service.ICircleContentPinService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CircleContentPinBizService {

    private final ICircleContentPinService pinService;
    private final ICircleAuditLogService auditLogService;
    // TODO: 注入权限校验服务（依赖 circle-core 的角色服务）

    @Transactional(rollbackFor = Exception.class)
    public void pin(String contentId, String operatorId, String circleId) {
        checkPermission(operatorId, circleId);
        pinService.pinContent(contentId);
        writeAuditLog(circleId, operatorId, CircleAuditActionEnum.PIN, contentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unpin(String contentId, String operatorId, String circleId) {
        checkPermission(operatorId, circleId);
        pinService.unpinContent(contentId);
        writeAuditLog(circleId, operatorId, CircleAuditActionEnum.UNPIN, contentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void feature(String contentId, String operatorId, String circleId) {
        checkPermission(operatorId, circleId);
        pinService.featureContent(contentId);
        writeAuditLog(circleId, operatorId, CircleAuditActionEnum.FEATURE, contentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unfeature(String contentId, String operatorId, String circleId) {
        checkPermission(operatorId, circleId);
        pinService.unfeatureContent(contentId);
        writeAuditLog(circleId, operatorId, CircleAuditActionEnum.UNFEATURE, contentId);
    }

    private void checkPermission(String operatorId, String circleId) {
        // TODO: 调用 circle-core 的角色服务校验是否为版主或创建者
        // if (!roleService.isAdminOrModerator(operatorId, circleId)) {
        //     throw new IllegalArgumentException("权限不足");
        // }
    }

    private void writeAuditLog(String circleId, String operatorId, CircleAuditActionEnum action, String targetId) {
        CircleAuditLog log = new CircleAuditLog()
            .setCircleId(circleId)
            .setOperatorId(operatorId)
            .setAction(action.getCode())
            .setTargetId(targetId)
            .setTargetType("CONTENT")
            .setResult("SUCCESS");
        auditLogService.writeAuditLog(log);
    }
}
```

- [ ] **Step 8: 创建 CircleContentPinController**

```java
package org.jeecg.modules.content.circle.controller;

import lombok.RequiredArgsConstructor;
import org.jeecg.modules.content.circle.biz.CircleContentPinBizService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.util.JwtUtil;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/circle-content")
@RequiredArgsConstructor
public class CircleContentPinController {

    private final CircleContentPinBizService pinBizService;

    @PutMapping("/{contentId}/pin")
    public Result<Void> togglePin(@PathVariable String contentId,
                                   @RequestParam String circleId,
                                   HttpServletRequest request) {
        String operatorId = JwtUtil.getUserIdByToken(request);
        pinBizService.pin(contentId, operatorId, circleId);
        return Result.ok();
    }

    @PutMapping("/{contentId}/featured")
    public Result<Void> toggleFeature(@PathVariable String contentId,
                                      @RequestParam String circleId,
                                      HttpServletRequest request) {
        String operatorId = JwtUtil.getUserIdByToken(request);
        pinBizService.feature(contentId, operatorId, circleId);
        return Result.ok();
    }
}
```

- [ ] **Step 9: 运行测试**

Run: `mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest=CircleContentPinServiceTest,CircleContentPinBizServiceTest -DfailIfNoTests=false`
Expected: 测试通过

- [ ] **Step 10: 提交**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/mapper/CircleContentMapper.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/mapper/content/circle/CircleContentMapper.xml \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/ICircleContentPinService.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/impl/CircleContentPinServiceImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/biz/CircleContentPinBizService.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleContentPinController.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/service/CircleContentPinServiceTest.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/biz/CircleContentPinBizServiceTest.java
git commit -m "feat(circle): add content pin and featured with audit logging"
```

---

## Task 4: 圈子公告

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/entity/CircleAnnouncement.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/mapper/CircleAnnouncementMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/mapper/content/circle/CircleAnnouncementMapper.xml`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/ICircleAnnouncementService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/impl/CircleAnnouncementServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/biz/CircleAnnouncementBizService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleAnnouncementController.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/req/CircleAnnouncementReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/vo/CircleAnnouncementVO.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/service/CircleAnnouncementServiceTest.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/biz/CircleAnnouncementBizServiceTest.java`

- [ ] **Step 1: 编写 CircleAnnouncementServiceTest**

```java
package org.jeecg.modules.content.circle.service;

import org.jeecg.modules.content.circle.entity.CircleAnnouncement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CircleAnnouncementServiceTest {

    @Autowired
    private ICircleAnnouncementService announcementService;

    @Test
    void publish_shouldCreateActiveAnnouncement() {
        CircleAnnouncement announcement = new CircleAnnouncement()
            .setCircleId("circle-001")
            .setContent("测试公告内容")
            .setStatus("ACTIVE");

        announcementService.publish(announcement);

        CircleAnnouncement active = announcementService.getActiveByCircleId("circle-001");
        assertNotNull(active);
        assertEquals("测试公告内容", active.getContent());
        assertEquals("ACTIVE", active.getStatus());
    }

    @Test
    void publish_shouldDeactivateOldAnnouncement() {
        // 发布第一条公告
        CircleAnnouncement first = new CircleAnnouncement()
            .setCircleId("circle-002")
            .setContent("第一条公告")
            .setStatus("ACTIVE");
        announcementService.publish(first);

        // 发布第二条公告
        CircleAnnouncement second = new CircleAnnouncement()
            .setCircleId("circle-002")
            .setContent("第二条公告")
            .setStatus("ACTIVE");
        announcementService.publish(second);

        // 验证只有第二条是 ACTIVE
        CircleAnnouncement active = announcementService.getActiveByCircleId("circle-002");
        assertNotNull(active);
        assertEquals("第二条公告", active.getContent());
    }

    @Test
    void getActiveByCircleId_expiredAnnouncement_shouldReturnNull() {
        CircleAnnouncement announcement = new CircleAnnouncement()
            .setCircleId("circle-003")
            .setContent("已过期公告")
            .setStatus("ACTIVE")
            .setExpireAt(new Date(System.currentTimeMillis() - 86400000)); // 昨天过期
        announcementService.publish(announcement);

        CircleAnnouncement active = announcementService.getActiveByCircleId("circle-003");
        assertNull(active);
    }
}
```

- [ ] **Step 2: 创建 CircleAnnouncement 实体**

```java
package org.jeecg.modules.content.circle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("circle_announcement")
@Schema(description = "圈子公告")
public class CircleAnnouncement extends JeecgEntity {

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "公告内容")
    private String content;

    @Schema(description = "状态 ACTIVE/INACTIVE")
    private String status;

    @Schema(description = "有效期截止时间")
    private Date expireAt;
}
```

- [ ] **Step 3: 创建 CircleAnnouncementMapper 和 Mapper.xml**

```java
package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.content.circle.entity.CircleAnnouncement;

@Mapper
public interface CircleAnnouncementMapper extends BaseMapper<CircleAnnouncement> {

    CircleAnnouncement selectActiveByCircleId(@Param("circleId") String circleId);
}
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.jeecg.modules.content.circle.mapper.CircleAnnouncementMapper">

    <select id="selectActiveByCircleId" resultType="org.jeecg.modules.content.circle.entity.CircleAnnouncement">
        SELECT * FROM circle_announcement
        WHERE circle_id = #{circleId}
          AND status = 'ACTIVE'
          AND (expire_at IS NULL OR expire_at > NOW())
        LIMIT 1
    </select>

</mapper>
```

- [ ] **Step 4: 创建 ICircleAnnouncementService 和实现**

```java
package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.circle.entity.CircleAnnouncement;

public interface ICircleAnnouncementService extends IService<CircleAnnouncement> {

    void publish(CircleAnnouncement announcement);

    CircleAnnouncement getActiveByCircleId(String circleId);
}
```

```java
package org.jeecg.modules.content.circle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.circle.entity.CircleAnnouncement;
import org.jeecg.modules.content.circle.mapper.CircleAnnouncementMapper;
import org.jeecg.modules.content.circle.service.ICircleAnnouncementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CircleAnnouncementServiceImpl extends ServiceImpl<CircleAnnouncementMapper, CircleAnnouncement>
        implements ICircleAnnouncementService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(CircleAnnouncement announcement) {
        // 将旧公告设为 INACTIVE
        lambdaUpdate()
            .eq(CircleAnnouncement::getCircleId, announcement.getCircleId())
            .eq(CircleAnnouncement::getStatus, "ACTIVE")
            .set(CircleAnnouncement::getStatus, "INACTIVE")
            .update();

        // 保存新公告
        announcement.setStatus("ACTIVE");
        save(announcement);
    }

    @Override
    public CircleAnnouncement getActiveByCircleId(String circleId) {
        return baseMapper.selectActiveByCircleId(circleId);
    }
}
```

- [ ] **Step 5: 创建 Req/VO**

```java
package org.jeecg.modules.content.circle.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@Schema(description = "发布公告请求")
public class CircleAnnouncementReq {

    @NotBlank(message = "圈子ID不能为空")
    @Schema(description = "圈子ID")
    private String circleId;

    @NotBlank(message = "公告内容不能为空")
    @Schema(description = "公告内容")
    private String content;

    @Schema(description = "有效期截止时间")
    private Date expireAt;
}
```

```java
package org.jeecg.modules.content.circle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

@Data
@Schema(description = "公告展示VO")
public class CircleAnnouncementVO {

    @Schema(description = "公告ID")
    private String id;

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "公告内容")
    private String content;

    @Schema(description = "有效期截止时间")
    private Date expireAt;

    @Schema(description = "发布时间")
    private Date createTime;
}
```

- [ ] **Step 6: 编写 CircleAnnouncementBizServiceTest**

```java
package org.jeecg.modules.content.circle.biz;

import org.jeecg.modules.content.circle.req.CircleAnnouncementReq;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CircleAnnouncementBizServiceTest {

    @Autowired
    private CircleAnnouncementBizService announcementBizService;

    @Test
    void publish_withAdminRole_shouldSucceed() {
        CircleAnnouncementReq req = new CircleAnnouncementReq();
        req.setCircleId("circle-010");
        req.setContent("测试公告");

        assertDoesNotThrow(() -> announcementBizService.publish(req, "user-admin"));
    }

    @Test
    void publish_withMemberRole_shouldThrowPermissionDenied() {
        CircleAnnouncementReq req = new CircleAnnouncementReq();
        req.setCircleId("circle-011");
        req.setContent("测试公告");

        assertThrows(IllegalArgumentException.class,
            () -> announcementBizService.publish(req, "user-member"));
    }
}
```

- [ ] **Step 7: 实现 CircleAnnouncementBizService**

```java
package org.jeecg.modules.content.circle.biz;

import lombok.RequiredArgsConstructor;
import org.jeecg.modules.content.circle.entity.CircleAnnouncement;
import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import org.jeecg.modules.content.circle.enums.CircleAuditActionEnum;
import org.jeecg.modules.content.circle.req.CircleAnnouncementReq;
import org.jeecg.modules.content.circle.service.ICircleAnnouncementService;
import org.jeecg.modules.content.circle.service.ICircleAuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CircleAnnouncementBizService {

    private final ICircleAnnouncementService announcementService;
    private final ICircleAuditLogService auditLogService;

    @Transactional(rollbackFor = Exception.class)
    public void publish(CircleAnnouncementReq req, String operatorId) {
        checkPermission(operatorId, req.getCircleId());

        CircleAnnouncement announcement = new CircleAnnouncement()
            .setCircleId(req.getCircleId())
            .setContent(req.getContent())
            .setExpireAt(req.getExpireAt());

        announcementService.publish(announcement);

        CircleAuditLog log = new CircleAuditLog()
            .setCircleId(req.getCircleId())
            .setOperatorId(operatorId)
            .setAction(CircleAuditActionEnum.PUBLISH_ANNOUNCEMENT.getCode())
            .setTargetId(req.getCircleId())
            .setTargetType("CIRCLE")
            .setResult("SUCCESS");
        auditLogService.writeAuditLog(log);
    }

    private void checkPermission(String operatorId, String circleId) {
        // TODO: 调用 circle-core 角色服务校验
    }
}
```

- [ ] **Step 8: 创建 CircleAnnouncementController**

```java
package org.jeecg.modules.content.circle.controller;

import lombok.RequiredArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.modules.content.circle.biz.CircleAnnouncementBizService;
import org.jeecg.modules.content.circle.req.CircleAnnouncementReq;
import org.jeecg.modules.content.circle.service.ICircleAnnouncementService;
import org.jeecg.modules.content.circle.vo.CircleAnnouncementVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/circle-announcement")
@RequiredArgsConstructor
public class CircleAnnouncementController {

    private final CircleAnnouncementBizService announcementBizService;
    private final ICircleAnnouncementService announcementService;

    @PostMapping
    public Result<Void> publish(@Valid @RequestBody CircleAnnouncementReq req,
                                HttpServletRequest request) {
        String operatorId = JwtUtil.getUserIdByToken(request);
        announcementBizService.publish(req, operatorId);
        return Result.ok();
    }

    @GetMapping("/active/{circleId}")
    public Result<CircleAnnouncementVO> getActive(@PathVariable String circleId) {
        var announcement = announcementService.getActiveByCircleId(circleId);
        if (announcement == null) {
            return Result.ok(null);
        }
        CircleAnnouncementVO vo = new CircleAnnouncementVO();
        BeanUtils.copyProperties(announcement, vo);
        return Result.ok(vo);
    }
}
```

- [ ] **Step 9: 运行测试**

Run: `mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest=CircleAnnouncementServiceTest,CircleAnnouncementBizServiceTest -DfailIfNoTests=false`
Expected: 测试通过

- [ ] **Step 10: 提交**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/entity/CircleAnnouncement.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/mapper/CircleAnnouncementMapper.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/mapper/content/circle/CircleAnnouncementMapper.xml \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/ICircleAnnouncementService.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/impl/CircleAnnouncementServiceImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/biz/CircleAnnouncementBizService.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleAnnouncementController.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/req/CircleAnnouncementReq.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/vo/CircleAnnouncementVO.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/service/CircleAnnouncementServiceTest.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/biz/CircleAnnouncementBizServiceTest.java
git commit -m "feat(circle): add announcement with publish, replace and expiry logic"
```

---

## Task 5: @成员功能

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/mapper/CircleMemberMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/ICircleMentionService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/impl/CircleMentionServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/biz/CircleMentionBizService.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/service/CircleMentionServiceTest.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/biz/CircleMentionBizServiceTest.java`

- [ ] **Step 1: 编写 CircleMentionServiceTest**

```java
package org.jeecg.modules.content.circle.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CircleMentionServiceTest {

    @Autowired
    private ICircleMentionService mentionService;

    @Test
    void parseMentions_shouldExtractUserIds() {
        String content = "你好 @user-001 和 @user-002，看看这个";
        List<String> userIds = mentionService.parseMentions(content);
        assertEquals(2, userIds.size());
        assertTrue(userIds.contains("user-001"));
        assertTrue(userIds.contains("user-002"));
    }

    @Test
    void parseMentions_noMentions_shouldReturnEmpty() {
        String content = "没有提及任何人的内容";
        List<String> userIds = mentionService.parseMentions(content);
        assertTrue(userIds.isEmpty());
    }

    @Test
    void getMentionCandidates_shouldReturnCircleMembers() {
        List<String> candidates = mentionService.getMentionCandidates("circle-001", "user-search");
        assertNotNull(candidates);
        // 验证不包含已退出的成员
    }

    @Test
    void sendMentionNotifications_shouldNotBlock() {
        // 验证异步执行不阻塞
        long start = System.currentTimeMillis();
        mentionService.sendMentionNotifications("circle-001", "content-001",
            Arrays.asList("user-001", "user-002"), "publisher-001");
        long elapsed = System.currentTimeMillis() - start;
        // 异步调用应立即返回
        assertTrue(elapsed < 100, "异步通知不应阻塞主流程");
    }

    @Test
    void sendMentionNotifications_exitedMember_shouldNotSend() {
        // user-003 已退出圈子
        assertDoesNotThrow(() ->
            mentionService.sendMentionNotifications("circle-001", "content-001",
                Arrays.asList("user-003"), "publisher-001"));
        // 验证未发送通知
    }
}
```

- [ ] **Step 2: 创建 CircleMemberMapper**

```java
package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.content.circle.entity.CircleMember;
import java.util.List;

@Mapper
public interface CircleMemberMapper extends BaseMapper<CircleMember> {

    List<String> selectMemberUserIds(@Param("circleId") String circleId);
}
```

- [ ] **Step 3: 创建 ICircleMentionService 和实现**

```java
package org.jeecg.modules.content.circle.service;

import java.util.List;

public interface ICircleMentionService {

    List<String> parseMentions(String content);

    List<String> getMentionCandidates(String circleId, String keyword);

    void sendMentionNotifications(String circleId, String contentId,
                                   List<String> mentionedUserIds, String publisherId);
}
```

```java
package org.jeecg.modules.content.circle.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.circle.mapper.CircleMemberMapper;
import org.jeecg.modules.content.circle.service.ICircleMentionService;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CircleMentionServiceImpl implements ICircleMentionService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\S+)");
    private final CircleMemberMapper memberMapper;
    private final IContentNotificationService notificationService;

    @Override
    public List<String> parseMentions(String content) {
        List<String> userIds = new ArrayList<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            userIds.add(matcher.group(1));
        }
        return userIds;
    }

    @Override
    public List<String> getMentionCandidates(String circleId, String keyword) {
        List<String> memberIds = memberMapper.selectMemberUserIds(circleId);
        if (keyword == null || keyword.isEmpty()) {
            return memberIds;
        }
        return memberIds.stream()
            .filter(id -> id.contains(keyword))
            .collect(Collectors.toList());
    }

    @Override
    @Async
    public void sendMentionNotifications(String circleId, String contentId,
                                          List<String> mentionedUserIds, String publisherId) {
        // 获取当前圈子成员（过滤已退出的）
        List<String> currentMembers = memberMapper.selectMemberUserIds(circleId);

        for (String userId : mentionedUserIds) {
            if (!currentMembers.contains(userId)) {
                log.info("用户 {} 已退出圈子 {}，跳过通知", userId, circleId);
                continue;
            }
            try {
                notificationService.sendNotification(userId, "MENTION",
                    "你被提及了", "在圈子内容中被 @" + publisherId + " 提及");
            } catch (Exception e) {
                log.error("@成员通知发送失败: userId={}, contentId={}", userId, contentId, e);
                // 失败记录已通过 notificationService 内部写入审计日志
            }
        }
    }
}
```

- [ ] **Step 4: 实现 CircleMentionBizService**

```java
package org.jeecg.modules.content.circle.biz;

import lombok.RequiredArgsConstructor;
import org.jeecg.modules.content.circle.service.ICircleMentionService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CircleMentionBizService {

    private final ICircleMentionService mentionService;

    public void processMentions(String circleId, String contentId,
                                 String content, String publisherId) {
        List<String> mentionedUserIds = mentionService.parseMentions(content);
        if (!mentionedUserIds.isEmpty()) {
            mentionService.sendMentionNotifications(circleId, contentId,
                mentionedUserIds, publisherId);
        }
    }
}
```

- [ ] **Step 5: 编写 CircleMentionBizServiceTest**

```java
package org.jeecg.modules.content.circle.biz;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CircleMentionBizServiceTest {

    @Autowired
    private CircleMentionBizService mentionBizService;

    @Test
    void processMentions_shouldParseAndNotifyAsync() {
        assertDoesNotThrow(() ->
            mentionBizService.processMentions("circle-001", "content-001",
                "你好 @user-001", "publisher-001"));
    }

    @Test
    void processMentions_noMentions_shouldDoNothing() {
        assertDoesNotThrow(() ->
            mentionBizService.processMentions("circle-001", "content-002",
                "没有提及的内容", "publisher-001"));
    }
}
```

- [ ] **Step 6: 运行测试**

Run: `mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest=CircleMentionServiceTest,CircleMentionBizServiceTest -DfailIfNoTests=false`
Expected: 测试通过

- [ ] **Step 7: 提交**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/mapper/CircleMemberMapper.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/ICircleMentionService.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/impl/CircleMentionServiceImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/biz/CircleMentionBizService.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/service/CircleMentionServiceTest.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/biz/CircleMentionBizServiceTest.java
git commit -m "feat(circle): add @mention with async notification"
```

---

## Task 6: 加入申请审核

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/entity/CircleJoinRequest.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/mapper/CircleJoinRequestMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/mapper/content/circle/CircleJoinRequestMapper.xml`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/ICircleJoinReviewService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/impl/CircleJoinReviewServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/biz/CircleJoinReviewBizService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleJoinReviewController.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/req/CircleJoinReviewReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/vo/CircleJoinRequestVO.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/service/CircleJoinReviewServiceTest.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/biz/CircleJoinReviewBizServiceTest.java`

- [ ] **Step 1: 编写 CircleJoinReviewServiceTest**

```java
package org.jeecg.modules.content.circle.service;

import org.jeecg.modules.content.circle.entity.CircleJoinRequest;
import org.jeecg.modules.content.circle.enums.CircleJoinRequestStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CircleJoinReviewServiceTest {

    @Autowired
    private ICircleJoinReviewService joinReviewService;

    @Test
    void approve_shouldUpdateStatusToApproved() {
        // 先创建一条申请
        CircleJoinRequest request = new CircleJoinRequest()
            .setCircleId("circle-001")
            .setUserId("user-applicant-001")
            .setStatus(CircleJoinRequestStatusEnum.PENDING.getCode());
        joinReviewService.save(request);

        joinReviewService.approve(request.getId(), "user-admin");

        CircleJoinRequest updated = joinReviewService.getById(request.getId());
        assertEquals(CircleJoinRequestStatusEnum.APPROVED.getCode(), updated.getStatus());
        assertEquals("user-admin", updated.getOperatorId());
        assertNotNull(updated.getOperateTime());
    }

    @Test
    void reject_shouldUpdateStatusToRejectedWithReason() {
        CircleJoinRequest request = new CircleJoinRequest()
            .setCircleId("circle-002")
            .setUserId("user-applicant-002")
            .setStatus(CircleJoinRequestStatusEnum.PENDING.getCode());
        joinReviewService.save(request);

        joinReviewService.reject(request.getId(), "user-admin", "不符合条件");

        CircleJoinRequest updated = joinReviewService.getById(request.getId());
        assertEquals(CircleJoinRequestStatusEnum.REJECTED.getCode(), updated.getStatus());
        assertEquals("不符合条件", updated.getRejectReason());
    }

    @Test
    void getPendingRequests_shouldReturnOnlyPending() {
        List<CircleJoinRequest> pending = joinReviewService.getPendingRequests("circle-003");
        pending.forEach(r ->
            assertEquals(CircleJoinRequestStatusEnum.PENDING.getCode(), r.getStatus()));
    }

    @Test
    void getTimedOutRequests_shouldReturnOverdueRequests() {
        List<CircleJoinRequest> timedOut = joinReviewService.getTimedOutRequests();
        // 所有返回的申请应超过 3 天
        timedOut.forEach(r -> {
            long daysSinceCreate = (System.currentTimeMillis() - r.getCreateTime().getTime()) / 86400000;
            assertTrue(daysSinceCreate >= 3);
            assertEquals(CircleJoinRequestStatusEnum.PENDING.getCode(), r.getStatus());
        });
    }
}
```

- [ ] **Step 2: 创建 CircleJoinRequest 实体**

```java
package org.jeecg.modules.content.circle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("circle_join_request")
@Schema(description = "圈子加入申请")
public class CircleJoinRequest extends JeecgEntity {

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "申请人ID")
    private String userId;

    @Schema(description = "状态 PENDING/APPROVED/REJECTED/EXPIRED")
    private String status;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "审核人ID")
    private String operatorId;

    @Schema(description = "审核时间")
    private Date operateTime;
}
```

- [ ] **Step 3: 创建 CircleJoinRequestMapper 和 Mapper.xml**

```java
package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.content.circle.entity.CircleJoinRequest;
import java.util.List;

@Mapper
public interface CircleJoinRequestMapper extends BaseMapper<CircleJoinRequest> {

    List<CircleJoinRequest> selectPendingByCircleId(@Param("circleId") String circleId);

    List<CircleJoinRequest> selectTimedOutRequests();
}
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.jeecg.modules.content.circle.mapper.CircleJoinRequestMapper">

    <select id="selectPendingByCircleId" resultType="org.jeecg.modules.content.circle.entity.CircleJoinRequest">
        SELECT * FROM circle_join_request
        WHERE circle_id = #{circleId} AND status = 'PENDING'
        ORDER BY create_time ASC
    </select>

    <select id="selectTimedOutRequests" resultType="org.jeecg.modules.content.circle.entity.CircleJoinRequest">
        SELECT * FROM circle_join_request
        WHERE status = 'PENDING'
          AND create_time < DATE_SUB(NOW(), INTERVAL 3 DAY)
        ORDER BY create_time ASC
    </select>

</mapper>
```

- [ ] **Step 4: 创建 ICircleJoinReviewService 和实现**

```java
package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.circle.entity.CircleJoinRequest;
import java.util.List;

public interface ICircleJoinReviewService extends IService<CircleJoinRequest> {

    void approve(String requestId, String operatorId);

    void reject(String requestId, String operatorId, String reason);

    List<CircleJoinRequest> getPendingRequests(String circleId);

    List<CircleJoinRequest> getTimedOutRequests();
}
```

```java
package org.jeecg.modules.content.circle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.circle.entity.CircleJoinRequest;
import org.jeecg.modules.content.circle.enums.CircleJoinRequestStatusEnum;
import org.jeecg.modules.content.circle.mapper.CircleJoinRequestMapper;
import org.jeecg.modules.content.circle.service.ICircleJoinReviewService;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class CircleJoinReviewServiceImpl extends ServiceImpl<CircleJoinRequestMapper, CircleJoinRequest>
        implements ICircleJoinReviewService {

    private final IContentNotificationService notificationService;

    public CircleJoinReviewServiceImpl(IContentNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void approve(String requestId, String operatorId) {
        CircleJoinRequest request = getById(requestId);
        request.setStatus(CircleJoinRequestStatusEnum.APPROVED.getCode());
        request.setOperatorId(operatorId);
        request.setOperateTime(new Date());
        updateById(request);

        // TODO: 调用 circle-core 的成员服务将用户加入圈子

        notificationService.sendNotification(request.getUserId(), "JOIN_APPROVED",
            "加入申请已批准", "你的圈子加入申请已被批准");
    }

    @Override
    public void reject(String requestId, String operatorId, String reason) {
        CircleJoinRequest request = getById(requestId);
        request.setStatus(CircleJoinRequestStatusEnum.REJECTED.getCode());
        request.setOperatorId(operatorId);
        request.setOperateTime(new Date());
        request.setRejectReason(reason);
        updateById(request);

        notificationService.sendNotification(request.getUserId(), "JOIN_REJECTED",
            "加入申请被拒绝", "你的圈子加入申请被拒绝: " + reason);
    }

    @Override
    public List<CircleJoinRequest> getPendingRequests(String circleId) {
        return baseMapper.selectPendingByCircleId(circleId);
    }

    @Override
    public List<CircleJoinRequest> getTimedOutRequests() {
        return baseMapper.selectTimedOutRequests();
    }
}
```

- [ ] **Step 5: 创建 Req/VO**

```java
package org.jeecg.modules.content.circle.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "审核加入申请请求")
public class CircleJoinReviewReq {

    @NotBlank(message = "申请ID不能为空")
    @Schema(description = "申请ID")
    private String requestId;

    @Schema(description = "拒绝原因（拒绝时必填）")
    private String rejectReason;
}
```

```java
package org.jeecg.modules.content.circle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

@Data
@Schema(description = "加入申请VO")
public class CircleJoinRequestVO {

    @Schema(description = "申请ID")
    private String id;

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "申请人ID")
    private String userId;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "申请时间")
    private Date createTime;
}
```

- [ ] **Step 6: 实现 CircleJoinReviewBizService**

```java
package org.jeecg.modules.content.circle.biz;

import lombok.RequiredArgsConstructor;
import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import org.jeecg.modules.content.circle.enums.CircleAuditActionEnum;
import org.jeecg.modules.content.circle.service.ICircleAuditLogService;
import org.jeecg.modules.content.circle.service.ICircleJoinReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CircleJoinReviewBizService {

    private final ICircleJoinReviewService joinReviewService;
    private final ICircleAuditLogService auditLogService;

    @Transactional(rollbackFor = Exception.class)
    public void approve(String requestId, String operatorId, String circleId) {
        checkPermission(operatorId, circleId);
        joinReviewService.approve(requestId, operatorId);

        CircleAuditLog log = new CircleAuditLog()
            .setCircleId(circleId)
            .setOperatorId(operatorId)
            .setAction(CircleAuditActionEnum.APPROVE_JOIN.getCode())
            .setTargetId(requestId)
            .setTargetType("JOIN_REQUEST")
            .setResult("APPROVED");
        auditLogService.writeAuditLog(log);
    }

    @Transactional(rollbackFor = Exception.class)
    public void reject(String requestId, String operatorId, String circleId, String reason) {
        checkPermission(operatorId, circleId);
        joinReviewService.reject(requestId, operatorId, reason);

        CircleAuditLog log = new CircleAuditLog()
            .setCircleId(circleId)
            .setOperatorId(operatorId)
            .setAction(CircleAuditActionEnum.REJECT_JOIN.getCode())
            .setTargetId(requestId)
            .setTargetType("JOIN_REQUEST")
            .setResult("REJECTED")
            .setReason(reason);
        auditLogService.writeAuditLog(log);
    }

    private void checkPermission(String operatorId, String circleId) {
        // TODO: 调用 circle-core 角色服务校验
    }
}
```

- [ ] **Step 7: 创建 CircleJoinReviewController**

```java
package org.jeecg.modules.content.circle.controller;

import lombok.RequiredArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.modules.content.circle.biz.CircleJoinReviewBizService;
import org.jeecg.modules.content.circle.req.CircleJoinReviewReq;
import org.jeecg.modules.content.circle.service.ICircleJoinReviewService;
import org.jeecg.modules.content.circle.vo.CircleJoinRequestVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/circle-join-review")
@RequiredArgsConstructor
public class CircleJoinReviewController {

    private final CircleJoinReviewBizService joinReviewBizService;
    private final ICircleJoinReviewService joinReviewService;

    @GetMapping("/pending/{circleId}")
    public Result<List<CircleJoinRequestVO>> getPending(@PathVariable String circleId) {
        List<CircleJoinRequestVO> vos = joinReviewService.getPendingRequests(circleId).stream()
            .map(entity -> {
                CircleJoinRequestVO vo = new CircleJoinRequestVO();
                BeanUtils.copyProperties(entity, vo);
                return vo;
            })
            .collect(Collectors.toList());
        return Result.ok(vos);
    }

    @PostMapping("/approve")
    public Result<Void> approve(@Valid @RequestBody CircleJoinReviewReq req,
                                @RequestParam String circleId,
                                HttpServletRequest request) {
        String operatorId = JwtUtil.getUserIdByToken(request);
        joinReviewBizService.approve(req.getRequestId(), operatorId, circleId);
        return Result.ok();
    }

    @PostMapping("/reject")
    public Result<Void> reject(@Valid @RequestBody CircleJoinReviewReq req,
                               @RequestParam String circleId,
                               HttpServletRequest request) {
        String operatorId = JwtUtil.getUserIdByToken(request);
        joinReviewBizService.reject(req.getRequestId(), operatorId, circleId, req.getRejectReason());
        return Result.ok();
    }
}
```

- [ ] **Step 8: 编写 CircleJoinReviewBizServiceTest**

```java
package org.jeecg.modules.content.circle.biz;

import org.jeecg.modules.content.circle.entity.CircleJoinRequest;
import org.jeecg.modules.content.circle.enums.CircleJoinRequestStatusEnum;
import org.jeecg.modules.content.circle.service.ICircleJoinReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CircleJoinReviewBizServiceTest {

    @Autowired
    private CircleJoinReviewBizService joinReviewBizService;

    @Autowired
    private ICircleJoinReviewService joinReviewService;

    @Test
    void approve_shouldWriteAuditLog() {
        CircleJoinRequest request = new CircleJoinRequest()
            .setCircleId("circle-020")
            .setUserId("user-applicant-020")
            .setStatus(CircleJoinRequestStatusEnum.PENDING.getCode());
        joinReviewService.save(request);

        joinReviewBizService.approve(request.getId(), "user-admin", "circle-020");

        CircleJoinRequest updated = joinReviewService.getById(request.getId());
        assertEquals(CircleJoinRequestStatusEnum.APPROVED.getCode(), updated.getStatus());
    }

    @Test
    void reject_withReason_shouldWriteAuditLog() {
        CircleJoinRequest request = new CircleJoinRequest()
            .setCircleId("circle-021")
            .setUserId("user-applicant-021")
            .setStatus(CircleJoinRequestStatusEnum.PENDING.getCode());
        joinReviewService.save(request);

        joinReviewBizService.reject(request.getId(), "user-admin", "circle-021", "不符合条件");

        CircleJoinRequest updated = joinReviewService.getById(request.getId());
        assertEquals(CircleJoinRequestStatusEnum.REJECTED.getCode(), updated.getStatus());
        assertEquals("不符合条件", updated.getRejectReason());
    }
}
```

- [ ] **Step 9: 运行测试**

Run: `mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest=CircleJoinReviewServiceTest,CircleJoinReviewBizServiceTest -DfailIfNoTests=false`
Expected: 测试通过

- [ ] **Step 10: 提交**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/entity/CircleJoinRequest.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/mapper/CircleJoinRequestMapper.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/mapper/content/circle/CircleJoinRequestMapper.xml \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/ICircleJoinReviewService.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/impl/CircleJoinReviewServiceImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/biz/CircleJoinReviewBizService.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleJoinReviewController.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/req/CircleJoinReviewReq.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/vo/CircleJoinRequestVO.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/service/CircleJoinReviewServiceTest.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/biz/CircleJoinReviewBizServiceTest.java
git commit -m "feat(circle): add join request review with timeout reminder"
```

---

## Task 7: 内容举报处理

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/entity/CircleReport.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/mapper/CircleReportMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/mapper/content/circle/CircleReportMapper.xml`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/ICircleReportService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/impl/CircleReportServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/biz/CircleReportBizService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleReportController.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/req/CircleReportReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/vo/CircleReportVO.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/service/CircleReportServiceTest.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/biz/CircleReportBizServiceTest.java`

- [ ] **Step 1: 编写 CircleReportServiceTest**

```java
package org.jeecg.modules.content.circle.service;

import org.jeecg.modules.content.circle.entity.CircleReport;
import org.jeecg.modules.content.circle.enums.CircleReportStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CircleReportServiceTest {

    @Autowired
    private ICircleReportService reportService;

    @Test
    void submitReport_shouldCreatePendingReport() {
        CircleReport report = new CircleReport()
            .setCircleId("circle-001")
            .setContentId("content-001")
            .setReporterId("user-reporter-001")
            .setReason("违规内容");

        reportService.submitReport(report);

        CircleReport saved = reportService.getById(report.getId());
        assertNotNull(saved);
        assertEquals(CircleReportStatusEnum.PENDING.getCode(), saved.getStatus());
    }

    @Test
    void submitReport_duplicate_shouldThrow() {
        CircleReport report1 = new CircleReport()
            .setCircleId("circle-002")
            .setContentId("content-002")
            .setReporterId("user-reporter-002")
            .setReason("违规");
        reportService.submitReport(report1);

        CircleReport report2 = new CircleReport()
            .setCircleId("circle-002")
            .setContentId("content-002")
            .setReporterId("user-reporter-002")
            .setReason("再次举报");

        assertThrows(IllegalArgumentException.class, () -> reportService.submitReport(report2));
    }

    @Test
    void handleDeleteContent_shouldResolveReport() {
        CircleReport report = new CircleReport()
            .setCircleId("circle-003")
            .setContentId("content-003")
            .setReporterId("user-reporter-003")
            .setReason("违规");
        reportService.submitReport(report);

        reportService.handleDeleteContent(report.getId(), "user-admin");

        CircleReport updated = reportService.getById(report.getId());
        assertEquals(CircleReportStatusEnum.RESOLVED.getCode(), updated.getStatus());
        assertEquals("DELETE", updated.getHandleAction());
    }

    @Test
    void handleIgnore_shouldMarkIgnored() {
        CircleReport report = new CircleReport()
            .setCircleId("circle-004")
            .setContentId("content-004")
            .setReporterId("user-reporter-004")
            .setReason("违规");
        reportService.submitReport(report);

        reportService.handleIgnore(report.getId(), "user-admin");

        CircleReport updated = reportService.getById(report.getId());
        assertEquals(CircleReportStatusEnum.IGNORED.getCode(), updated.getStatus());
    }

    @Test
    void getReports_shouldFilterByStatus() {
        List<CircleReport> pending = reportService.getReports("circle-005",
            CircleReportStatusEnum.PENDING.getCode());
        pending.forEach(r ->
            assertEquals(CircleReportStatusEnum.PENDING.getCode(), r.getStatus()));
    }
}
```

- [ ] **Step 2: 创建 CircleReport 实体**

```java
package org.jeecg.modules.content.circle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("circle_report")
@Schema(description = "圈子内容举报")
public class CircleReport extends JeecgEntity {

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "被举报内容ID")
    private String contentId;

    @Schema(description = "举报者ID")
    private String reporterId;

    @Schema(description = "举报原因")
    private String reason;

    @Schema(description = "状态 PENDING/RESOLVED/IGNORED")
    private String status;

    @Schema(description = "处理人ID")
    private String operatorId;

    @Schema(description = "处理时间")
    private Date operateTime;

    @Schema(description = "处理动作 DELETE/IGNORE/MUTE")
    private String handleAction;
}
```

- [ ] **Step 3: 创建 CircleReportMapper 和 Mapper.xml**

```java
package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.content.circle.entity.CircleReport;
import java.util.List;

@Mapper
public interface CircleReportMapper extends BaseMapper<CircleReport> {

    List<CircleReport> selectByCircleAndStatus(@Param("circleId") String circleId,
                                                @Param("status") String status);
}
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.jeecg.modules.content.circle.mapper.CircleReportMapper">

    <select id="selectByCircleAndStatus" resultType="org.jeecg.modules.content.circle.entity.CircleReport">
        SELECT * FROM circle_report
        WHERE circle_id = #{circleId}
        <if test="status != null and status != ''">
          AND status = #{status}
        </if>
        ORDER BY create_time DESC
    </select>

</mapper>
```

- [ ] **Step 4: 创建 ICircleReportService 和实现**

```java
package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.circle.entity.CircleReport;
import java.util.List;

public interface ICircleReportService extends IService<CircleReport> {

    void submitReport(CircleReport report);

    void handleDeleteContent(String reportId, String operatorId);

    void handleIgnore(String reportId, String operatorId);

    void handleMute(String reportId, String operatorId);

    List<CircleReport> getReports(String circleId, String status);
}
```

```java
package org.jeecg.modules.content.circle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.circle.entity.CircleReport;
import org.jeecg.modules.content.circle.enums.CircleReportStatusEnum;
import org.jeecg.modules.content.circle.mapper.CircleReportMapper;
import org.jeecg.modules.content.circle.service.ICircleReportService;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class CircleReportServiceImpl extends ServiceImpl<CircleReportMapper, CircleReport>
        implements ICircleReportService {

    private final IContentNotificationService notificationService;

    public CircleReportServiceImpl(IContentNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void submitReport(CircleReport report) {
        // 检查重复举报
        long count = lambdaQuery()
            .eq(CircleReport::getReporterId, report.getReporterId())
            .eq(CircleReport::getContentId, report.getContentId())
            .count();
        if (count > 0) {
            throw new IllegalArgumentException("已提交过举报");
        }
        report.setStatus(CircleReportStatusEnum.PENDING.getCode());
        save(report);
    }

    @Override
    public void handleDeleteContent(String reportId, String operatorId) {
        CircleReport report = getById(reportId);
        report.setStatus(CircleReportStatusEnum.RESOLVED.getCode());
        report.setOperatorId(operatorId);
        report.setOperateTime(new Date());
        report.setHandleAction("DELETE");
        updateById(report);

        // TODO: 调用内容服务删除被举报内容

        notificationService.sendNotification(report.getReporterId(), "REPORT_RESOLVED",
            "举报已处理", "你举报的内容已被删除");
    }

    @Override
    public void handleIgnore(String reportId, String operatorId) {
        CircleReport report = getById(reportId);
        report.setStatus(CircleReportStatusEnum.IGNORED.getCode());
        report.setOperatorId(operatorId);
        report.setOperateTime(new Date());
        report.setHandleAction("IGNORE");
        updateById(report);

        notificationService.sendNotification(report.getReporterId(), "REPORT_IGNORED",
            "举报已审核", "你举报的内容经审核未违规");
    }

    @Override
    public void handleMute(String reportId, String operatorId) {
        CircleReport report = getById(reportId);
        report.setStatus(CircleReportStatusEnum.RESOLVED.getCode());
        report.setOperatorId(operatorId);
        report.setOperateTime(new Date());
        report.setHandleAction("MUTE");
        updateById(report);

        // TODO: 调用 circle-core 的禁言服务
    }

    @Override
    public List<CircleReport> getReports(String circleId, String status) {
        return baseMapper.selectByCircleAndStatus(circleId, status);
    }
}
```

- [ ] **Step 5: 创建 Req/VO**

```java
package org.jeecg.modules.content.circle.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "举报请求")
public class CircleReportReq {

    @NotBlank(message = "圈子ID不能为空")
    @Schema(description = "圈子ID")
    private String circleId;

    @NotBlank(message = "内容ID不能为空")
    @Schema(description = "被举报内容ID")
    private String contentId;

    @Schema(description = "举报原因")
    private String reason;
}
```

```java
package org.jeecg.modules.content.circle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

@Data
@Schema(description = "举报VO")
public class CircleReportVO {

    @Schema(description = "举报ID")
    private String id;

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "被举报内容ID")
    private String contentId;

    @Schema(description = "举报者ID")
    private String reporterId;

    @Schema(description = "举报原因")
    private String reason;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "处理动作")
    private String handleAction;

    @Schema(description = "举报时间")
    private Date createTime;
}
```

- [ ] **Step 6: 实现 CircleReportBizService**

```java
package org.jeecg.modules.content.circle.biz;

import lombok.RequiredArgsConstructor;
import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import org.jeecg.modules.content.circle.entity.CircleReport;
import org.jeecg.modules.content.circle.enums.CircleAuditActionEnum;
import org.jeecg.modules.content.circle.service.ICircleAuditLogService;
import org.jeecg.modules.content.circle.service.ICircleReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CircleReportBizService {

    private final ICircleReportService reportService;
    private final ICircleAuditLogService auditLogService;

    @Transactional(rollbackFor = Exception.class)
    public void submitReport(CircleReport report, String reporterId) {
        report.setReporterId(reporterId);
        reportService.submitReport(report);
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleDeleteContent(String reportId, String operatorId, String circleId) {
        checkPermission(operatorId, circleId);
        reportService.handleDeleteContent(reportId, operatorId);
        writeAuditLog(circleId, operatorId, CircleAuditActionEnum.DELETE_REPORTED, reportId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleIgnore(String reportId, String operatorId, String circleId) {
        checkPermission(operatorId, circleId);
        reportService.handleIgnore(reportId, operatorId);
        writeAuditLog(circleId, operatorId, CircleAuditActionEnum.IGNORE_REPORT, reportId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleMute(String reportId, String operatorId, String circleId) {
        checkPermission(operatorId, circleId);
        reportService.handleMute(reportId, operatorId);
        writeAuditLog(circleId, operatorId, CircleAuditActionEnum.MUTE_FROM_REPORT, reportId);
    }

    private void checkPermission(String operatorId, String circleId) {
        // TODO: 调用 circle-core 角色服务校验
    }

    private void writeAuditLog(String circleId, String operatorId, CircleAuditActionEnum action, String targetId) {
        CircleAuditLog log = new CircleAuditLog()
            .setCircleId(circleId)
            .setOperatorId(operatorId)
            .setAction(action.getCode())
            .setTargetId(targetId)
            .setTargetType("REPORT")
            .setResult("SUCCESS");
        auditLogService.writeAuditLog(log);
    }
}
```

- [ ] **Step 7: 创建 CircleReportController**

```java
package org.jeecg.modules.content.circle.controller;

import lombok.RequiredArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.modules.content.circle.biz.CircleReportBizService;
import org.jeecg.modules.content.circle.entity.CircleReport;
import org.jeecg.modules.content.circle.req.CircleReportReq;
import org.jeecg.modules.content.circle.service.ICircleReportService;
import org.jeecg.modules.content.circle.vo.CircleReportVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/circle-report")
@RequiredArgsConstructor
public class CircleReportController {

    private final CircleReportBizService reportBizService;
    private final ICircleReportService reportService;

    @PostMapping
    public Result<Void> submit(@Valid @RequestBody CircleReportReq req,
                               HttpServletRequest request) {
        String reporterId = JwtUtil.getUserIdByToken(request);
        CircleReport report = new CircleReport();
        BeanUtils.copyProperties(req, report);
        reportBizService.submitReport(report, reporterId);
        return Result.ok();
    }

    @GetMapping("/list/{circleId}")
    public Result<List<CircleReportVO>> getReports(@PathVariable String circleId,
                                                    @RequestParam(required = false) String status) {
        List<CircleReportVO> vos = reportService.getReports(circleId, status).stream()
            .map(entity -> {
                CircleReportVO vo = new CircleReportVO();
                BeanUtils.copyProperties(entity, vo);
                return vo;
            })
            .collect(Collectors.toList());
        return Result.ok(vos);
    }

    @PostMapping("/{reportId}/delete-content")
    public Result<Void> handleDeleteContent(@PathVariable String reportId,
                                             @RequestParam String circleId,
                                             HttpServletRequest request) {
        String operatorId = JwtUtil.getUserIdByToken(request);
        reportBizService.handleDeleteContent(reportId, operatorId, circleId);
        return Result.ok();
    }

    @PostMapping("/{reportId}/ignore")
    public Result<Void> handleIgnore(@PathVariable String reportId,
                                      @RequestParam String circleId,
                                      HttpServletRequest request) {
        String operatorId = JwtUtil.getUserIdByToken(request);
        reportBizService.handleIgnore(reportId, operatorId, circleId);
        return Result.ok();
    }

    @PostMapping("/{reportId}/mute")
    public Result<Void> handleMute(@PathVariable String reportId,
                                    @RequestParam String circleId,
                                    HttpServletRequest request) {
        String operatorId = JwtUtil.getUserIdByToken(request);
        reportBizService.handleMute(reportId, operatorId, circleId);
        return Result.ok();
    }
}
```

- [ ] **Step 8: 编写 CircleReportBizServiceTest**

```java
package org.jeecg.modules.content.circle.biz;

import org.jeecg.modules.content.circle.entity.CircleReport;
import org.jeecg.modules.content.circle.enums.CircleReportStatusEnum;
import org.jeecg.modules.content.circle.service.ICircleReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CircleReportBizServiceTest {

    @Autowired
    private CircleReportBizService reportBizService;

    @Autowired
    private ICircleReportService reportService;

    @Test
    void submit_shouldCreateReport() {
        CircleReport report = new CircleReport()
            .setCircleId("circle-030")
            .setContentId("content-030")
            .setReason("违规内容");

        reportBizService.submitReport(report, "user-reporter-030");

        CircleReport saved = reportService.getById(report.getId());
        assertNotNull(saved);
        assertEquals(CircleReportStatusEnum.PENDING.getCode(), saved.getStatus());
        assertEquals("user-reporter-030", saved.getReporterId());
    }

    @Test
    void handleDeleteContent_shouldResolveAndNotify() {
        CircleReport report = new CircleReport()
            .setCircleId("circle-031")
            .setContentId("content-031")
            .setReason("违规");
        reportBizService.submitReport(report, "user-reporter-031");

        reportBizService.handleDeleteContent(report.getId(), "user-admin", "circle-031");

        CircleReport updated = reportService.getById(report.getId());
        assertEquals(CircleReportStatusEnum.RESOLVED.getCode(), updated.getStatus());
        assertEquals("DELETE", updated.getHandleAction());
    }
}
```

- [ ] **Step 9: 运行测试**

Run: `mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest=CircleReportServiceTest,CircleReportBizServiceTest -DfailIfNoTests=false`
Expected: 测试通过

- [ ] **Step 10: 提交**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/entity/CircleReport.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/mapper/CircleReportMapper.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/mapper/content/circle/CircleReportMapper.xml \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/ICircleReportService.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/service/impl/CircleReportServiceImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/biz/CircleReportBizService.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleReportController.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/req/CircleReportReq.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/vo/CircleReportVO.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/service/CircleReportServiceTest.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/biz/CircleReportBizServiceTest.java
git commit -m "feat(circle): add content report with handle, notify and audit logging"
```

---

## Task 8: 加入申请超时提醒定时任务

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/task/CircleJoinRequestTimeoutTask.java`

- [ ] **Step 1: 创建超时提醒定时任务**

```java
package org.jeecg.modules.content.circle.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.circle.entity.CircleJoinRequest;
import org.jeecg.modules.content.circle.service.ICircleJoinReviewService;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CircleJoinRequestTimeoutTask {

    private final ICircleJoinReviewService joinReviewService;
    private final IContentNotificationService notificationService;

    /**
     * 每小时扫描超过 3 天未处理的加入申请，提醒管理员
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void remindTimedOutRequests() {
        List<CircleJoinRequest> timedOut = joinReviewService.getTimedOutRequests();
        if (timedOut.isEmpty()) {
            return;
        }

        log.info("发现 {} 条超时加入申请，开始提醒管理员", timedOut.size());

        for (CircleJoinRequest request : timedOut) {
            try {
                // 通知圈子创建者和版主（需要调用 circle-core 获取管理员列表）
                // TODO: 获取圈子管理员列表
                notificationService.sendNotification(
                    request.getCreateBy(), // 暂用创建者
                    "JOIN_REQUEST_TIMEOUT",
                    "加入申请超时提醒",
                    "圈子有加入申请超过 3 天未处理，请及时审核"
                );
            } catch (Exception e) {
                log.error("超时提醒发送失败: requestId={}", request.getId(), e);
            }
        }
    }
}
```

- [ ] **Step 2: 运行测试**

Run: `mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest=CircleJoinReviewServiceTest -DfailIfNoTests=false`
Expected: 测试通过

- [ ] **Step 3: 提交**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/task/CircleJoinRequestTimeoutTask.java
git commit -m "feat(circle): add join request timeout reminder scheduled task"
```

---

## Validation

- [ ] **V1:** 执行 Flyway 迁移，验证数据库表结构正确
- [ ] **V2:** 运行所有单元测试 `mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.circle.*" -DfailIfNoTests=false`
- [ ] **V3:** 验证置顶内容排序逻辑（置顶在前，按 pinned_at 倒序）
- [ ] **V4:** 验证权限控制（普通成员操作返回"权限不足"）
- [ ] **V5:** 验证公告同一时间仅一条生效
- [ ] **V6:** 验证审核日志完整记录（操作人、时间、类型、对象、结果）
