# 频道基础架构与创建 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 建立内容社区频道的基础数据模型、三类频道创建流程、审核状态机、编辑/转让/删除能力。

**Architecture:** 单表 `content_channel` + `channel_type` 枚举区分三种频道类型，审核记录独立表 `content_channel_review`，转让记录独立表 `content_channel_transfer`。遵循 Controller → BizManageService → Service → Mapper 分层架构。

**Tech Stack:** Spring Boot 3 + MyBatis Plus + MySQL + JUnit 5 + Mockito

---

## Task 1: 数据库迁移脚本与枚举类

**Files:**
- Create: `jeecg-boot/db/migration/V_channel_infrastructure.sql`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/enums/ChannelType.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/enums/ChannelStatus.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/enums/ReviewResult.java`

- [ ] **Step 1: 编写数据库迁移脚本**

```sql
-- V_channel_infrastructure.sql
-- 频道基础架构表结构

-- 频道主表
CREATE TABLE content_channel (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '频道ID',
    name VARCHAR(100) NOT NULL COMMENT '频道名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '频道简介',
    icon_url VARCHAR(500) DEFAULT NULL COMMENT '频道图标URL',
    cover_url VARCHAR(500) DEFAULT NULL COMMENT '频道封面URL',
    channel_type TINYINT NOT NULL COMMENT '频道类型: 1=system, 2=personal, 3=organization',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0=Draft, 1=PendingReview, 2=Active, 3=Rejected, 4=DeleteCooling, 5=Deleted',
    privacy TINYINT NOT NULL DEFAULT 1 COMMENT '隐私设置: 1=公开, 2=私有',
    category_id BIGINT DEFAULT NULL COMMENT '归属分类ID',
    owner_id BIGINT NOT NULL COMMENT '频道主用户ID',
    organization_id BIGINT DEFAULT NULL COMMENT '组织ID(组织频道必填)',
    pin_weight INT NOT NULL DEFAULT 0 COMMENT '置顶权重(系统频道)',
    delete_cooling_end_time DATETIME DEFAULT NULL COMMENT '冷静期结束时间',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0=正常, 1=已删除',
    PRIMARY KEY (id),
    KEY idx_channel_type (channel_type),
    KEY idx_status (status),
    KEY idx_owner_id (owner_id),
    KEY idx_organization_id (organization_id),
    KEY idx_category_id (category_id),
    KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='频道表';

-- 频道审核记录表
CREATE TABLE content_channel_review (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '审核记录ID',
    channel_id BIGINT NOT NULL COMMENT '频道ID',
    reviewer_id BIGINT NOT NULL COMMENT '审核人ID',
    result TINYINT NOT NULL COMMENT '审核结果: 1=Pass, 2=Reject, 3=ReturnForEdit',
    reason VARCHAR(500) DEFAULT NULL COMMENT '审核原因',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_channel_id (channel_id),
    KEY idx_reviewer_id (reviewer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='频道审核记录表';

-- 频道转让记录表
CREATE TABLE content_channel_transfer (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '转让记录ID',
    channel_id BIGINT NOT NULL COMMENT '频道ID',
    from_user_id BIGINT NOT NULL COMMENT '发起转让用户ID',
    to_user_id BIGINT NOT NULL COMMENT '目标用户ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0=Pending, 1=Accepted, 2=Rejected, 3=Expired',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_channel_id (channel_id),
    KEY idx_from_user_id (from_user_id),
    KEY idx_to_user_id (to_user_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='频道转让记录表';
```

- [ ] **Step 2: 创建 ChannelType 枚举**

```java
package org.jeecg.modules.content.channel.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChannelType {

    SYSTEM(1, "system"),
    PERSONAL(2, "personal"),
    ORGANIZATION(3, "organization");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
```

- [ ] **Step 3: 创建 ChannelStatus 枚举**

```java
package org.jeecg.modules.content.channel.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChannelStatus {

    DRAFT(0, "Draft"),
    PENDING_REVIEW(1, "PendingReview"),
    ACTIVE(2, "Active"),
    REJECTED(3, "Rejected"),
    DELETE_COOLING(4, "DeleteCooling"),
    DELETED(5, "Deleted");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
```

- [ ] **Step 4: 创建 ReviewResult 枚举**

```java
package org.jeecg.modules.content.channel.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReviewResult {

    PASS(1, "Pass"),
    REJECT(2, "Reject"),
    RETURN_FOR_EDIT(3, "ReturnForEdit");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
```

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot/db/migration/V_channel_infrastructure.sql \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/enums/
git commit -m "feat(channel): add database migration and enum classes"
```

---

## Task 2: 实体类与 Mapper

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/entity/Channel.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/entity/ChannelReview.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/entity/ChannelTransfer.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/mapper/ChannelMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/mapper/ChannelReviewMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/mapper/ChannelTransferMapper.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/mapper/ChannelMapperTest.java`

- [ ] **Step 1: 创建 Channel 实体类**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.enums.ChannelType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@TableName("content_channel")
public class Channel {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "频道名称不能为空")
    private String name;

    private String description;
    private String iconUrl;
    private String coverUrl;

    @NotNull(message = "频道类型不能为空")
    private ChannelType channelType;

    @NotNull(message = "频道状态不能为空")
    private ChannelStatus status;

    private Integer privacy;
    private Long categoryId;
    private Long ownerId;
    private Long organizationId;
    private Integer pinWeight;
    private LocalDateTime deleteCoolingEndTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Integer delFlag;
}
```

- [ ] **Step 2: 创建 ChannelReview 实体类**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.jeecg.modules.content.channel.enums.ReviewResult;

import java.time.LocalDateTime;

@Data
@TableName("content_channel_review")
public class ChannelReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long channelId;
    private Long reviewerId;
    private ReviewResult result;
    private String reason;
    private LocalDateTime createdTime;
}
```

- [ ] **Step 3: 创建 ChannelTransfer 实体类**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("content_channel_transfer")
public class ChannelTransfer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long channelId;
    private Long fromUserId;
    private Long toUserId;
    private Integer status; // 0=Pending, 1=Accepted, 2=Rejected, 3=Expired
    private LocalDateTime expireTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
```

- [ ] **Step 4: 创建 Mapper 接口**

```java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.channel.entity.Channel;

@Mapper
public interface ChannelMapper extends BaseMapper<Channel> {
}
```

```java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.channel.entity.ChannelReview;

@Mapper
public interface ChannelReviewMapper extends BaseMapper<ChannelReview> {
}
```

```java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.channel.entity.ChannelTransfer;

@Mapper
public interface ChannelTransferMapper extends BaseMapper<ChannelTransfer> {
}
```

- [ ] **Step 5: 编写 Mapper 测试**

```java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@MybatisPlusTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChannelMapperTest {

    @Resource
    private ChannelMapper channelMapper;

    @Test
    void insertAndSelectChannel() {
        Channel channel = new Channel();
        channel.setName("test-channel");
        channel.setChannelType(ChannelType.PERSONAL);
        channel.setStatus(ChannelStatus.PENDING_REVIEW);
        channel.setOwnerId(1L);
        channel.setPrivacy(1);
        channelMapper.insert(channel);

        assertNotNull(channel.getId());
        Channel found = channelMapper.selectById(channel.getId());
        assertEquals("test-channel", found.getName());
        assertEquals(ChannelType.PERSONAL, found.getChannelType());
    }
}
```

- [ ] **Step 6: 运行测试验证**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelMapperTest -DfailIfNoTests=false`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/entity/ \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/mapper/ \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/mapper/
git commit -m "feat(channel): add entity classes and mapper interfaces"
```

---

## Task 3: ChannelService - 名称唯一性校验

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/impl/ChannelServiceImpl.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelServiceTest.java`

- [ ] **Step 1: 编写名称唯一性校验的失败测试**

```java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.mapper.ChannelMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {

    @Mock
    private ChannelMapper channelMapper;

    @InjectMocks
    private ChannelServiceImpl channelService;

    @Test
    void checkNameUnique_returnsTrue_whenNameIsUnique() {
        when(channelMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList());

        assertTrue(channelService.checkNameUnique("new-channel", null));
    }

    @Test
    void checkNameUnique_returnsFalse_whenNameExists() {
        Channel existing = new Channel();
        existing.setName("existing-channel");
        when(channelMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Arrays.asList(existing));

        assertFalse(channelService.checkNameUnique("existing-channel", null));
    }

    @Test
    void checkNameUnique_returnsTrue_whenNameExistsButExcludedId() {
        Channel existing = new Channel();
        existing.setId(1L);
        existing.setName("existing-channel");
        when(channelMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList());

        assertTrue(channelService.checkNameUnique("existing-channel", 1L));
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelServiceTest -DfailIfNoTests=false`
Expected: FAIL (ChannelServiceImpl not found)

- [ ] **Step 3: 创建 ChannelService 接口**

```java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.Channel;

public interface ChannelService extends IService<Channel> {

    /**
     * 校验名称在用户频道范围内是否唯一
     * @param name 频道名称
     * @param excludeId 排除的频道ID（编辑时排除自身）
     * @return true=唯一, false=已存在
     */
    boolean checkNameUnique(String name, Long excludeId);
}
```

- [ ] **Step 4: 创建 ChannelServiceImpl 实现**

```java
package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.mapper.ChannelMapper;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ChannelServiceImpl extends ServiceImpl<ChannelMapper, Channel>
    implements ChannelService {

    private static final List<ChannelStatus> NAME_OCCUPIED_STATUSES = Arrays.asList(
        ChannelStatus.PENDING_REVIEW,
        ChannelStatus.ACTIVE,
        ChannelStatus.DELETE_COOLING
    );

    @Override
    public boolean checkNameUnique(String name, Long excludeId) {
        LambdaQueryWrapper<Channel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Channel::getName, name)
               .in(Channel::getStatus, NAME_OCCUPIED_STATUSES)
               .ne(Channel::getChannelType, ChannelType.SYSTEM); // 系统频道不参与用户频道唯一性

        if (excludeId != null) {
            wrapper.ne(Channel::getId, excludeId);
        }

        return baseMapper.selectList(wrapper).isEmpty();
    }
}
```

- [ ] **Step 5: 运行测试验证通过**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelServiceTest -DfailIfNoTests=false`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/
git commit -m "feat(channel): add ChannelService with name uniqueness check"
```

---

## Task 4: ChannelReviewService - 审核记录

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelReviewService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/impl/ChannelReviewServiceImpl.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelReviewServiceTest.java`

- [ ] **Step 1: 编写审核记录创建测试**

```java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ChannelReview;
import org.jeecg.modules.content.channel.enums.ReviewResult;
import org.jeecg.modules.content.channel.mapper.ChannelReviewMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelReviewServiceTest {

    @Mock
    private ChannelReviewMapper channelReviewMapper;

    @InjectMocks
    private ChannelReviewServiceImpl channelReviewService;

    @Test
    void createReview_savesRecord() {
        when(channelReviewMapper.insert(any(ChannelReview.class))).thenReturn(1);

        ChannelReview review = channelReviewService.createReview(1L, 100L, ReviewResult.PASS, "审核通过");

        assertNotNull(review);
        assertEquals(1L, review.getChannelId());
        assertEquals(100L, review.getReviewerId());
        assertEquals(ReviewResult.PASS, review.getResult());
        verify(channelReviewMapper).insert(any(ChannelReview.class));
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelReviewServiceTest -DfailIfNoTests=false`
Expected: FAIL

- [ ] **Step 3: 创建 ChannelReviewService 接口**

```java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ChannelReview;
import org.jeecg.modules.content.channel.enums.ReviewResult;

import java.util.List;

public interface ChannelReviewService extends IService<ChannelReview> {

    ChannelReview createReview(Long channelId, Long reviewerId, ReviewResult result, String reason);

    List<ChannelReview> listReviewsByChannelId(Long channelId);
}
```

- [ ] **Step 4: 创建 ChannelReviewServiceImpl 实现**

```java
package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ChannelReview;
import org.jeecg.modules.content.channel.enums.ReviewResult;
import org.jeecg.modules.content.channel.mapper.ChannelReviewMapper;
import org.jeecg.modules.content.channel.service.ChannelReviewService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChannelReviewServiceImpl extends ServiceImpl<ChannelReviewMapper, ChannelReview>
    implements ChannelReviewService {

    @Override
    public ChannelReview createReview(Long channelId, Long reviewerId, ReviewResult result, String reason) {
        ChannelReview review = new ChannelReview();
        review.setChannelId(channelId);
        review.setReviewerId(reviewerId);
        review.setResult(result);
        review.setReason(reason);
        review.setCreatedTime(LocalDateTime.now());
        baseMapper.insert(review);
        return review;
    }

    @Override
    public List<ChannelReview> listReviewsByChannelId(Long channelId) {
        return baseMapper.selectList(
            new LambdaQueryWrapper<ChannelReview>()
                .eq(ChannelReview::getChannelId, channelId)
                .orderByDesc(ChannelReview::getCreatedTime)
        );
    }
}
```

- [ ] **Step 5: 运行测试验证通过**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelReviewServiceTest -DfailIfNoTests=false`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/
git commit -m "feat(channel): add ChannelReviewService for review record management"
```

---

## Task 5: ChannelTransferService - 转让记录

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelTransferService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/impl/ChannelTransferServiceImpl.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelTransferServiceTest.java`

- [ ] **Step 1: 编写转让请求创建测试**

```java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ChannelTransfer;
import org.jeecg.modules.content.channel.mapper.ChannelTransferMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelTransferServiceTest {

    @Mock
    private ChannelTransferMapper channelTransferMapper;

    @InjectMocks
    private ChannelTransferServiceImpl channelTransferService;

    @Test
    void createTransfer_savesRecord() {
        when(channelTransferMapper.insert(any(ChannelTransfer.class))).thenReturn(1);

        ChannelTransfer transfer = channelTransferService.createTransfer(1L, 100L, 200L);

        assertNotNull(transfer);
        assertEquals(1L, transfer.getChannelId());
        assertEquals(100L, transfer.getFromUserId());
        assertEquals(200L, transfer.getToUserId());
        assertEquals(0, transfer.getStatus()); // Pending
        assertNotNull(transfer.getExpireTime());
        verify(channelTransferMapper).insert(any(ChannelTransfer.class));
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelTransferServiceTest -DfailIfNoTests=false`
Expected: FAIL

- [ ] **Step 3: 创建 ChannelTransferService 接口**

```java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ChannelTransfer;

public interface ChannelTransferService extends IService<ChannelTransfer> {

    ChannelTransfer createTransfer(Long channelId, Long fromUserId, Long toUserId);

    boolean confirmTransfer(Long transferId, Long userId);
}
```

- [ ] **Step 4: 创建 ChannelTransferServiceImpl 实现**

```java
package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ChannelTransfer;
import org.jeecg.modules.content.channel.mapper.ChannelTransferMapper;
import org.jeecg.modules.content.channel.service.ChannelTransferService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ChannelTransferServiceImpl extends ServiceImpl<ChannelTransferMapper, ChannelTransfer>
    implements ChannelTransferService {

    private static final int TRANSFER_PENDING = 0;
    private static final int TRANSFER_ACCEPTED = 1;
    private static final int TRANSFER_REJECTED = 2;
    private static final int TRANSFER_EXPIRED = 3;
    private static final int TRANSFER_EXPIRE_DAYS = 7;

    @Override
    public ChannelTransfer createTransfer(Long channelId, Long fromUserId, Long toUserId) {
        ChannelTransfer transfer = new ChannelTransfer();
        transfer.setChannelId(channelId);
        transfer.setFromUserId(fromUserId);
        transfer.setToUserId(toUserId);
        transfer.setStatus(TRANSFER_PENDING);
        transfer.setExpireTime(LocalDateTime.now().plusDays(TRANSFER_EXPIRE_DAYS));
        baseMapper.insert(transfer);
        return transfer;
    }

    @Override
    public boolean confirmTransfer(Long transferId, Long userId) {
        ChannelTransfer transfer = baseMapper.selectById(transferId);
        if (transfer == null || transfer.getStatus() != TRANSFER_PENDING) {
            return false;
        }
        if (!transfer.getToUserId().equals(userId)) {
            return false;
        }
        if (LocalDateTime.now().isAfter(transfer.getExpireTime())) {
            transfer.setStatus(TRANSFER_EXPIRED);
            baseMapper.updateById(transfer);
            return false;
        }
        transfer.setStatus(TRANSFER_ACCEPTED);
        baseMapper.updateById(transfer);
        return true;
    }
}
```

- [ ] **Step 5: 运行测试验证通过**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelTransferServiceTest -DfailIfNoTests=false`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/
git commit -m "feat(channel): add ChannelTransferService for transfer management"
```

---

## Task 6: ChannelBizManageService - 系统频道创建

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/ChannelBizManageService.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/biz/ChannelBizManageServiceTest.java`

- [ ] **Step 1: 编写系统频道创建测试**

```java
package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.dto.CreateChannelDTO;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelBizManageServiceTest {

    @Mock
    private ChannelService channelService;

    @InjectMocks
    private ChannelBizManageService channelBizManageService;

    @Test
    void createSystemChannel_createsActiveChannel() {
        when(channelService.save(any(Channel.class))).thenReturn(true);

        CreateChannelDTO dto = new CreateChannelDTO();
        dto.setName("官方公告");
        dto.setDescription("平台官方公告频道");
        dto.setChannelType(ChannelType.SYSTEM);
        dto.setPinWeight(10);

        Channel result = channelBizManageService.createSystemChannel(dto, 1L);

        assertNotNull(result);
        assertEquals(ChannelStatus.ACTIVE, result.getStatus());
        assertEquals(ChannelType.SYSTEM, result.getChannelType());
        assertEquals(1L, result.getOwnerId());
        verify(channelService).save(any(Channel.class));
    }

    @Test
    void createSystemChannel_setsDefaultPrivacy() {
        when(channelService.save(any(Channel.class))).thenReturn(true);

        CreateChannelDTO dto = new CreateChannelDTO();
        dto.setName("官方推荐");
        dto.setChannelType(ChannelType.SYSTEM);

        Channel result = channelBizManageService.createSystemChannel(dto, 1L);

        assertEquals(1, result.getPrivacy()); // 公开
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelBizManageServiceTest -DfailIfNoTests=false`
Expected: FAIL

- [ ] **Step 3: 创建 CreateChannelDTO**

```java
package org.jeecg.modules.content.channel.dto;

import lombok.Data;
import org.jeecg.modules.content.channel.enums.ChannelType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreateChannelDTO {

    @NotBlank(message = "频道名称不能为空")
    private String name;

    private String description;
    private String iconUrl;
    private String coverUrl;

    @NotNull(message = "频道类型不能为空")
    private ChannelType channelType;

    private Integer privacy;
    private Long categoryId;
    private Long organizationId;
    private Integer pinWeight;
}
```

- [ ] **Step 4: 创建 ChannelBizManageService 实现（系统频道创建部分）**

```java
package org.jeecg.modules.content.channel.biz;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.channel.dto.CreateChannelDTO;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
@Slf4j
public class ChannelBizManageService {

    @Resource
    private ChannelService channelService;

    @Transactional(rollbackFor = Exception.class)
    public Channel createSystemChannel(CreateChannelDTO dto, Long operatorId) {
        Channel channel = new Channel();
        channel.setName(dto.getName());
        channel.setDescription(dto.getDescription());
        channel.setIconUrl(dto.getIconUrl());
        channel.setCoverUrl(dto.getCoverUrl());
        channel.setChannelType(ChannelType.SYSTEM);
        channel.setStatus(ChannelStatus.ACTIVE);
        channel.setPrivacy(1); // 系统频道强制公开
        channel.setCategoryId(dto.getCategoryId());
        channel.setOwnerId(operatorId);
        channel.setPinWeight(dto.getPinWeight() != null ? dto.getPinWeight() : 0);
        channel.setCreatedTime(LocalDateTime.now());
        channel.setUpdatedTime(LocalDateTime.now());
        channel.setDelFlag(0);

        channelService.save(channel);
        log.info("系统频道创建成功: channelId={}, name={}", channel.getId(), channel.getName());
        return channel;
    }
}
```

- [ ] **Step 5: 运行测试验证通过**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelBizManageServiceTest -DfailIfNoTests=false`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/ \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/dto/ \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/biz/
git commit -m "feat(channel): add ChannelBizManageService with system channel creation"
```

---

## Task 7: ChannelBizManageService - 个人频道创建

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/ChannelBizManageService.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/biz/ChannelBizManageServicePersonalTest.java`

- [ ] **Step 1: 编写个人频道创建成功测试**

```java
package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.dto.CreateChannelDTO;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelBizManageServicePersonalTest {

    @Mock
    private ChannelService channelService;

    @InjectMocks
    private ChannelBizManageService channelBizManageService;

    @Test
    void createPersonalChannel_success() {
        when(channelService.checkNameUnique("my-channel", null)).thenReturn(true);
        when(channelService.count(any())).thenReturn(5L); // 未达上限
        when(channelService.save(any(Channel.class))).thenReturn(true);

        CreateChannelDTO dto = new CreateChannelDTO();
        dto.setName("my-channel");
        dto.setDescription("我的频道");
        dto.setChannelType(ChannelType.PERSONAL);
        dto.setPrivacy(1);

        Channel result = channelBizManageService.createPersonalChannel(dto, 100L);

        assertNotNull(result);
        assertEquals(ChannelStatus.PENDING_REVIEW, result.getStatus());
        assertEquals(ChannelType.PERSONAL, result.getChannelType());
        assertEquals(100L, result.getOwnerId());
        verify(channelService).save(any(Channel.class));
    }
}
```

- [ ] **Step 2: 编写名称冲突失败测试**

```java
@Test
void createPersonalChannel_throwsWhenNameConflict() {
    when(channelService.checkNameUnique("existing-channel", null)).thenReturn(false);

    CreateChannelDTO dto = new CreateChannelDTO();
    dto.setName("existing-channel");
    dto.setChannelType(ChannelType.PERSONAL);

    assertThrows(BusinessException.class, () ->
        channelBizManageService.createPersonalChannel(dto, 100L));
}
```

- [ ] **Step 3: 编写数量上限失败测试**

```java
@Test
void createPersonalChannel_throwsWhenLimitReached() {
    when(channelService.checkNameUnique("new-channel", null)).thenReturn(true);
    when(channelService.count(any())).thenReturn(20L); // 已达上限

    CreateChannelDTO dto = new CreateChannelDTO();
    dto.setName("new-channel");
    dto.setChannelType(ChannelType.PERSONAL);

    assertThrows(BusinessException.class, () ->
        channelBizManageService.createPersonalChannel(dto, 100L));
}
```

- [ ] **Step 4: 实现个人频道创建方法**

在 `ChannelBizManageService` 中添加：

```java
private static final int MAX_PERSONAL_CHANNELS = 20;

@Transactional(rollbackFor = Exception.class)
public Channel createPersonalChannel(CreateChannelDTO dto, Long userId) {
    // 1. 校验名称唯一性
    if (!channelService.checkNameUnique(dto.getName(), null)) {
        throw new BusinessException("该频道名称已被使用，请更换");
    }

    // 2. 校验数量上限
    long count = channelService.count(new LambdaQueryWrapper<Channel>()
        .eq(Channel::getOwnerId, userId)
        .eq(Channel::getChannelType, ChannelType.PERSONAL)
        .ne(Channel::getStatus, ChannelStatus.DELETED));
    if (count >= MAX_PERSONAL_CHANNELS) {
        throw new BusinessException("个人频道数量已达上限（" + MAX_PERSONAL_CHANNELS + "个）");
    }

    // 3. 创建频道（待审核状态）
    Channel channel = new Channel();
    channel.setName(dto.getName());
    channel.setDescription(dto.getDescription());
    channel.setIconUrl(dto.getIconUrl());
    channel.setCoverUrl(dto.getCoverUrl());
    channel.setChannelType(ChannelType.PERSONAL);
    channel.setStatus(ChannelStatus.PENDING_REVIEW);
    channel.setPrivacy(dto.getPrivacy() != null ? dto.getPrivacy() : 1);
    channel.setCategoryId(dto.getCategoryId());
    channel.setOwnerId(userId);
    channel.setCreatedTime(LocalDateTime.now());
    channel.setUpdatedTime(LocalDateTime.now());
    channel.setDelFlag(0);

    channelService.save(channel);
    log.info("个人频道创建成功: channelId={}, userId={}", channel.getId(), userId);
    return channel;
}
```

- [ ] **Step 5: 运行测试验证通过**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelBizManageServicePersonalTest -DfailIfNoTests=false`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/biz/
git commit -m "feat(channel): add personal channel creation with validation"
```

---

## Task 8: ChannelBizManageService - 组织频道创建

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/ChannelBizManageService.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/biz/ChannelBizManageServiceOrgTest.java`

- [ ] **Step 1: 编写组织频道创建成功测试**

```java
@Test
void createOrganizationChannel_success() {
    when(channelService.checkNameUnique("org-channel", null)).thenReturn(true);
    when(channelService.count(any())).thenReturn(10L);
    when(channelService.save(any(Channel.class))).thenReturn(true);

    CreateChannelDTO dto = new CreateChannelDTO();
    dto.setName("org-channel");
    dto.setChannelType(ChannelType.ORGANIZATION);
    dto.setOrganizationId(500L);

    Channel result = channelBizManageService.createOrganizationChannel(dto, 100L, true);

    assertNotNull(result);
    assertEquals(ChannelStatus.PENDING_REVIEW, result.getStatus());
    assertEquals(500L, result.getOrganizationId());
}
```

- [ ] **Step 2: 编写组织未认证失败测试**

```java
@Test
void createOrganizationChannel_throwsWhenNotCertified() {
    CreateChannelDTO dto = new CreateChannelDTO();
    dto.setName("org-channel");
    dto.setChannelType(ChannelType.ORGANIZATION);
    dto.setOrganizationId(500L);

    assertThrows(BusinessException.class, () ->
        channelBizManageService.createOrganizationChannel(dto, 100L, false));
}
```

- [ ] **Step 3: 实现组织频道创建方法**

```java
private static final int MAX_ORG_CHANNELS = 50;

@Transactional(rollbackFor = Exception.class)
public Channel createOrganizationChannel(CreateChannelDTO dto, Long userId, boolean isOrgCertified) {
    // 1. 校验组织认证
    if (!isOrgCertified) {
        throw new BusinessException("请先完成组织认证");
    }

    // 2. 校验名称唯一性
    if (!channelService.checkNameUnique(dto.getName(), null)) {
        throw new BusinessException("该频道名称已被使用，请更换");
    }

    // 3. 校验组织频道数量上限
    long count = channelService.count(new LambdaQueryWrapper<Channel>()
        .eq(Channel::getOrganizationId, dto.getOrganizationId())
        .eq(Channel::getChannelType, ChannelType.ORGANIZATION)
        .ne(Channel::getStatus, ChannelStatus.DELETED));
    if (count >= MAX_ORG_CHANNELS) {
        throw new BusinessException("组织频道数量已达上限（" + MAX_ORG_CHANNELS + "个）");
    }

    // 4. 创建频道
    Channel channel = new Channel();
    channel.setName(dto.getName());
    channel.setDescription(dto.getDescription());
    channel.setIconUrl(dto.getIconUrl());
    channel.setCoverUrl(dto.getCoverUrl());
    channel.setChannelType(ChannelType.ORGANIZATION);
    channel.setStatus(ChannelStatus.PENDING_REVIEW);
    channel.setPrivacy(dto.getPrivacy() != null ? dto.getPrivacy() : 1);
    channel.setCategoryId(dto.getCategoryId());
    channel.setOwnerId(userId);
    channel.setOrganizationId(dto.getOrganizationId());
    channel.setCreatedTime(LocalDateTime.now());
    channel.setUpdatedTime(LocalDateTime.now());
    channel.setDelFlag(0);

    channelService.save(channel);
    log.info("组织频道创建成功: channelId={}, orgId={}", channel.getId(), dto.getOrganizationId());
    return channel;
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelBizManageServiceOrgTest -DfailIfNoTests=false`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/biz/
git commit -m "feat(channel): add organization channel creation with validation"
```

---

## Task 9: ChannelBizManageService - 频道信息编辑

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/ChannelBizManageService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/dto/UpdateChannelDTO.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/biz/ChannelBizManageServiceEditTest.java`

- [ ] **Step 1: 创建 UpdateChannelDTO**

```java
package org.jeecg.modules.content.channel.dto;

import lombok.Data;

@Data
public class UpdateChannelDTO {
    private String name;
    private String description;
    private String iconUrl;
    private String coverUrl;
    private Long categoryId;
    private String tags;
}
```

- [ ] **Step 2: 编写非关键字段即时生效测试**

```java
@Test
void updateChannel_nonCriticalField_immediateEffect() {
    Channel existing = new Channel();
    existing.setId(1L);
    existing.setName("test-channel");
    existing.setStatus(ChannelStatus.ACTIVE);
    existing.setChannelType(ChannelType.PERSONAL);
    when(channelService.getById(1L)).thenReturn(existing);
    when(channelService.updateById(any())).thenReturn(true);

    UpdateChannelDTO dto = new UpdateChannelDTO();
    dto.setTags("tag1,tag2");

    channelBizManageService.updateChannel(1L, dto, 100L);

    assertEquals(ChannelStatus.ACTIVE, existing.getStatus()); // 状态不变
    verify(channelService).updateById(any());
}
```

- [ ] **Step 3: 编写关键字段触发审核测试**

```java
@Test
void updateChannel_criticalField_triggersReview() {
    Channel existing = new Channel();
    existing.setId(1L);
    existing.setName("old-name");
    existing.setStatus(ChannelStatus.ACTIVE);
    existing.setChannelType(ChannelType.PERSONAL);
    when(channelService.getById(1L)).thenReturn(existing);
    when(channelService.checkNameUnique("new-name", 1L)).thenReturn(true);
    when(channelService.updateById(any())).thenReturn(true);

    UpdateChannelDTO dto = new UpdateChannelDTO();
    dto.setName("new-name");

    channelBizManageService.updateChannel(1L, dto, 100L);

    assertEquals(ChannelStatus.PENDING_REVIEW, existing.getStatus());
    verify(channelService).updateById(any());
}
```

- [ ] **Step 4: 实现编辑方法**

```java
private static final List<String> CRITICAL_FIELDS = Arrays.asList("name", "description", "iconUrl", "coverUrl", "categoryId");

@Transactional(rollbackFor = Exception.class)
public void updateChannel(Long channelId, UpdateChannelDTO dto, Long userId) {
    Channel channel = channelService.getById(channelId);
    if (channel == null) {
        throw new BusinessException("频道不存在");
    }

    boolean hasCriticalChange = false;

    // 名称修改需校验唯一性
    if (dto.getName() != null && !dto.getName().equals(channel.getName())) {
        if (!channelService.checkNameUnique(dto.getName(), channelId)) {
            throw new BusinessException("该频道名称已被使用，请更换");
        }
        channel.setName(dto.getName());
        hasCriticalChange = true;
    }

    if (dto.getDescription() != null) {
        channel.setDescription(dto.getDescription());
        hasCriticalChange = true;
    }
    if (dto.getIconUrl() != null) {
        channel.setIconUrl(dto.getIconUrl());
        hasCriticalChange = true;
    }
    if (dto.getCoverUrl() != null) {
        channel.setCoverUrl(dto.getCoverUrl());
        hasCriticalChange = true;
    }
    if (dto.getCategoryId() != null) {
        channel.setCategoryId(dto.getCategoryId());
        hasCriticalChange = true;
    }
    // 非关键字段直接更新
    // tags 等非关键字段在此处理

    // 关键字段修改触发审核（系统频道除外）
    if (hasCriticalChange && channel.getChannelType() != ChannelType.SYSTEM) {
        channel.setStatus(ChannelStatus.PENDING_REVIEW);
    }

    channel.setUpdatedTime(LocalDateTime.now());
    channelService.updateById(channel);
    log.info("频道信息更新: channelId={}, hasCriticalChange={}", channelId, hasCriticalChange);
}
```

- [ ] **Step 5: 运行测试验证通过**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelBizManageServiceEditTest -DfailIfNoTests=false`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/dto/
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/biz/
git commit -m "feat(channel): add channel edit with critical/non-critical field handling"
```

---

## Task 10: ChannelBizManageService - 转让与删除

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/ChannelBizManageService.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/biz/ChannelBizManageServiceOwnershipTest.java`

- [ ] **Step 1: 编写个人频道转让测试**

```java
@Test
void transferChannel_personal_success() {
    Channel channel = new Channel();
    channel.setId(1L);
    channel.setChannelType(ChannelType.PERSONAL);
    channel.setOwnerId(100L);
    when(channelService.getById(1L)).thenReturn(channel);
    when(channelTransferService.createTransfer(1L, 100L, 200L)).thenReturn(new ChannelTransfer());

    channelBizManageService.transferChannel(1L, 100L, 200L);

    verify(channelTransferService).createTransfer(1L, 100L, 200L);
}
```

- [ ] **Step 2: 编写系统频道不可转让测试**

```java
@Test
void transferChannel_system_throws() {
    Channel channel = new Channel();
    channel.setId(1L);
    channel.setChannelType(ChannelType.SYSTEM);
    when(channelService.getById(1L)).thenReturn(channel);

    assertThrows(BusinessException.class, () ->
        channelBizManageService.transferChannel(1L, 100L, 200L));
}
```

- [ ] **Step 3: 编写删除冷静期测试**

```java
@Test
void deleteChannel_entersCoolingPeriod() {
    Channel channel = new Channel();
    channel.setId(1L);
    channel.setChannelType(ChannelType.PERSONAL);
    channel.setOwnerId(100L);
    channel.setStatus(ChannelStatus.ACTIVE);
    when(channelService.getById(1L)).thenReturn(channel);
    when(channelService.updateById(any())).thenReturn(true);

    channelBizManageService.deleteChannel(1L, 100L);

    assertEquals(ChannelStatus.DELETE_COOLING, channel.getStatus());
    assertNotNull(channel.getDeleteCoolingEndTime());
    verify(channelService).updateById(any());
}
```

- [ ] **Step 4: 实现转让和删除方法**

```java
public void transferChannel(Long channelId, Long fromUserId, Long toUserId) {
    Channel channel = channelService.getById(channelId);
    if (channel == null) {
        throw new BusinessException("频道不存在");
    }
    if (channel.getChannelType() == ChannelType.SYSTEM) {
        throw new BusinessException("系统频道不可转让");
    }
    if (!channel.getOwnerId().equals(fromUserId)) {
        throw new BusinessException("仅频道主可发起转让");
    }
    channelTransferService.createTransfer(channelId, fromUserId, toUserId);
    log.info("频道转让请求已创建: channelId={}, from={}, to={}", channelId, fromUserId, toUserId);
}

public void confirmTransfer(Long transferId, Long userId) {
    boolean success = channelTransferService.confirmTransfer(transferId, userId);
    if (!success) {
        throw new BusinessException("转让确认失败，请求可能已过期或无效");
    }
    // 更新频道所有权
    ChannelTransfer transfer = channelTransferService.getById(transferId);
    Channel channel = channelService.getById(transfer.getChannelId());
    channel.setOwnerId(transfer.getToUserId());
    channel.setUpdatedTime(LocalDateTime.now());
    channelService.updateById(channel);
    log.info("频道转让完成: channelId={}, newOwner={}", channel.getId(), transfer.getToUserId());
}

private static final int COOLING_DAYS = 7;

public void deleteChannel(Long channelId, Long userId) {
    Channel channel = channelService.getById(channelId);
    if (channel == null) {
        throw new BusinessException("频道不存在");
    }
    if (channel.getChannelType() == ChannelType.SYSTEM) {
        throw new BusinessException("系统频道仅平台可管理");
    }
    if (!channel.getOwnerId().equals(userId)) {
        throw new BusinessException("仅频道主可删除频道");
    }

    channel.setStatus(ChannelStatus.DELETE_COOLING);
    channel.setDeleteCoolingEndTime(LocalDateTime.now().plusDays(COOLING_DAYS));
    channel.setUpdatedTime(LocalDateTime.now());
    channelService.updateById(channel);
    log.info("频道进入删除冷静期: channelId={}, endTime={}", channelId, channel.getDeleteCoolingEndTime());
}

public void cancelDelete(Long channelId, Long userId) {
    Channel channel = channelService.getById(channelId);
    if (channel == null || channel.getStatus() != ChannelStatus.DELETE_COOLING) {
        throw new BusinessException("频道不在冷静期内");
    }
    if (!channel.getOwnerId().equals(userId)) {
        throw new BusinessException("仅频道主可撤销删除");
    }
    if (LocalDateTime.now().isAfter(channel.getDeleteCoolingEndTime())) {
        throw new BusinessException("冷静期已过，无法撤销");
    }

    channel.setStatus(ChannelStatus.ACTIVE);
    channel.setDeleteCoolingEndTime(null);
    channel.setUpdatedTime(LocalDateTime.now());
    channelService.updateById(channel);
    log.info("频道删除已撤销: channelId={}", channelId);
}
```

- [ ] **Step 5: 运行测试验证通过**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ChannelBizManageServiceOwnershipTest -DfailIfNoTests=false`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/biz/
git commit -m "feat(channel): add transfer and delete with cooling period"
```

---

## Task 11: Controller 层

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelController.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelAdminController.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/vo/ChannelVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/vo/ChannelListVO.java`

- [ ] **Step 1: 创建 ChannelVO 和 ChannelListVO**

```java
package org.jeecg.modules.content.channel.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChannelVO {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private String coverUrl;
    private Integer channelType;
    private Integer status;
    private Integer privacy;
    private Long categoryId;
    private Long ownerId;
    private Long organizationId;
    private Integer pinWeight;
    private LocalDateTime createdTime;
}
```

```java
package org.jeecg.modules.content.channel.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChannelListVO {
    private Long id;
    private String name;
    private String iconUrl;
    private Integer channelType;
    private Integer status;
    private Integer privacy;
    private LocalDateTime createdTime;
}
```

- [ ] **Step 2: 创建 ChannelController**

```java
package org.jeecg.modules.content.channel.controller;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelBizManageService;
import org.jeecg.modules.content.channel.dto.CreateChannelDTO;
import org.jeecg.modules.content.channel.dto.UpdateChannelDTO;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.jeecg.modules.content.channel.vo.ChannelVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/channels")
@Slf4j
public class ChannelController {

    @Resource
    private ChannelBizManageService channelBizManageService;

    @Resource
    private ChannelService channelService;

    @PostMapping("/create")
    public Result<ChannelVO> createChannel(@Valid @RequestBody CreateChannelDTO dto) {
        // TODO: 从 SecurityContext 获取当前用户ID
        Long userId = 1L;
        Channel channel;
        if (dto.getChannelType().name().equals("PERSONAL")) {
            channel = channelBizManageService.createPersonalChannel(dto, userId);
        } else if (dto.getChannelType().name().equals("ORGANIZATION")) {
            // TODO: 校验组织认证状态
            channel = channelBizManageService.createOrganizationChannel(dto, userId, true);
        } else {
            return Result.error("用户端不可创建系统频道");
        }
        return Result.OK(convertToVO(channel));
    }

    @GetMapping("/{id}")
    public Result<ChannelVO> getChannel(@PathVariable Long id) {
        Channel channel = channelService.getById(id);
        if (channel == null) {
            return Result.error("频道不存在");
        }
        return Result.OK(convertToVO(channel));
    }

    @PutMapping("/{id}")
    public Result<Void> updateChannel(@PathVariable Long id, @Valid @RequestBody UpdateChannelDTO dto) {
        Long userId = 1L;
        channelBizManageService.updateChannel(id, dto, userId);
        return Result.OK();
    }

    @PostMapping("/{id}/transfer")
    public Result<Void> transferChannel(@PathVariable Long id, @RequestParam Long toUserId) {
        Long userId = 1L;
        channelBizManageService.transferChannel(id, userId, toUserId);
        return Result.OK();
    }

    @PostMapping("/transfer/{transferId}/confirm")
    public Result<Void> confirmTransfer(@PathVariable Long transferId) {
        Long userId = 1L;
        channelBizManageService.confirmTransfer(transferId, userId);
        return Result.OK();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteChannel(@PathVariable Long id) {
        Long userId = 1L;
        channelBizManageService.deleteChannel(id, userId);
        return Result.OK();
    }

    @PostMapping("/{id}/cancel-delete")
    public Result<Void> cancelDelete(@PathVariable Long id) {
        Long userId = 1L;
        channelBizManageService.cancelDelete(id, userId);
        return Result.OK();
    }

    private ChannelVO convertToVO(Channel channel) {
        ChannelVO vo = new ChannelVO();
        BeanUtils.copyProperties(channel, vo);
        return vo;
    }
}
```

- [ ] **Step 3: 创建 ChannelAdminController**

```java
package org.jeecg.modules.content.channel.controller;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelBizManageService;
import org.jeecg.modules.content.channel.dto.CreateChannelDTO;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.enums.ReviewResult;
import org.jeecg.modules.content.channel.service.ChannelReviewService;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.jeecg.modules.content.channel.vo.ChannelVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/channels")
@Slf4j
public class ChannelAdminController {

    @Resource
    private ChannelBizManageService channelBizManageService;

    @Resource
    private ChannelService channelService;

    @Resource
    private ChannelReviewService channelReviewService;

    @PostMapping("/create-system")
    public Result<ChannelVO> createSystemChannel(@Valid @RequestBody CreateChannelDTO dto) {
        Long operatorId = 1L; // TODO: 从 SecurityContext 获取
        dto.setChannelType(ChannelType.SYSTEM);
        Channel channel = channelBizManageService.createSystemChannel(dto, operatorId);
        return Result.OK(convertToVO(channel));
    }

    @PostMapping("/{id}/review")
    public Result<Void> reviewChannel(@PathVariable Long id,
                                       @RequestParam ReviewResult result,
                                       @RequestParam(required = false) String reason) {
        Long reviewerId = 1L; // TODO: 从 SecurityContext 获取
        channelBizManageService.reviewChannel(id, reviewerId, result, reason);
        return Result.OK();
    }

    private ChannelVO convertToVO(Channel channel) {
        ChannelVO vo = new ChannelVO();
        BeanUtils.copyProperties(channel, vo);
        return vo;
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/vo/
git commit -m "feat(channel): add ChannelController and ChannelAdminController"
```

---

## Task 12: 定时任务

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/scheduled/ChannelScheduledTask.java`

- [ ] **Step 1: 创建定时任务类**

```java
package org.jeecg.modules.content.channel.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ChannelTransfer;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.jeecg.modules.content.channel.service.ChannelTransferService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class ChannelScheduledTask {

    @Resource
    private ChannelService channelService;

    @Resource
    private ChannelTransferService channelTransferService;

    /**
     * 每小时扫描冷静期到期的频道，批量处理为 Deleted
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void processDeleteCoolingExpired() {
        List<Channel> expiredChannels = channelService.list(
            new LambdaQueryWrapper<Channel>()
                .eq(Channel::getStatus, ChannelStatus.DELETE_COOLING)
                .le(Channel::getDeleteCoolingEndTime, LocalDateTime.now())
        );

        for (Channel channel : expiredChannels) {
            channel.setStatus(ChannelStatus.DELETED);
            channel.setUpdatedTime(LocalDateTime.now());
            channelService.updateById(channel);
            log.info("频道冷静期到期，已删除: channelId={}", channel.getId());
        }

        if (!expiredChannels.isEmpty()) {
            log.info("冷静期到期处理完成，处理数量: {}", expiredChannels.size());
        }
    }

    /**
     * 每小时扫描超时的转让请求
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void processTransferExpired() {
        List<ChannelTransfer> expiredTransfers = channelTransferService.list(
            new LambdaQueryWrapper<ChannelTransfer>()
                .eq(ChannelTransfer::getStatus, 0) // Pending
                .le(ChannelTransfer::getExpireTime, LocalDateTime.now())
        );

        for (ChannelTransfer transfer : expiredTransfers) {
            transfer.setStatus(3); // Expired
            transfer.setUpdatedTime(LocalDateTime.now());
            channelTransferService.updateById(transfer);
            log.info("转让请求超时: transferId={}", transfer.getId());
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/scheduled/
git commit -m "feat(channel): add scheduled tasks for cooling period and transfer expiry"
```

---

## Task 13: 常量类与 BusinessException

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/constant/ChannelConstants.java`

- [ ] **Step 1: 创建常量类**

```java
package org.jeecg.modules.content.channel.constant;

public class ChannelConstants {

    public static final int MAX_PERSONAL_CHANNELS = 20;
    public static final int MAX_ORG_CHANNELS = 50;
    public static final int COOLING_DAYS = 7;
    public static final int TRANSFER_EXPIRE_DAYS = 7;

    public static final int PRIVACY_PUBLIC = 1;
    public static final int PRIVACY_PRIVATE = 2;

    private ChannelConstants() {}
}
```

- [ ] **Step 2: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/constant/
git commit -m "feat(channel): add channel constants"
```

---

## Task 14: 最终验证

- [ ] **Step 1: 运行所有单元测试**

Run: `cd jeecg-boot && mvn test -pl jeecg-boot-module/jeecg-module-content -DfailIfNoTests=false`
Expected: All tests PASS

- [ ] **Step 2: 运行代码检查**

Run: `cd jeecg-boot && mvn compile -pl jeecg-boot-module/jeecg-module-content`
Expected: BUILD SUCCESS

- [ ] **Step 3: 验证数据库脚本**

手动执行 `V_channel_infrastructure.sql` 验证表结构创建成功。

- [ ] **Step 4: Commit 最终状态**

```bash
git status
# 确认所有文件已提交
```
