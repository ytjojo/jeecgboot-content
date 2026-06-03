# 频道隐私、订阅与成员管理 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为频道建立访问控制、订阅关系、成员角色和频道级治理能力

**Architecture:** 在 channel-infrastructure 已设计的 content_channel 表基础上，扩展隐私和加入方式字段，新增 9 张表（订阅、成员、角色、申请、禁言、黑名单、治理日志、邀请、订阅分组）。遵循 Controller → BizManageService → Service → Mapper 分层架构，所有治理操作统一记录审计日志。

**Tech Stack:** Spring Boot, MyBatis Plus, MySQL, Flyway, JUnit 5, Mockito

---

## Task 1: 数据库迁移与实体定义

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/db/migration/V_channel_privacy_membership.sql`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/enums/*.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/entity/*.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/mapper/*.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/mapper/content/channel/*.xml`

### Step 1: 编写迁移脚本

```sql
-- V_channel_privacy_membership.sql

-- 扩展 content_channel 表
ALTER TABLE content_channel
ADD COLUMN privacy_type TINYINT NOT NULL DEFAULT 1 COMMENT '隐私类型: 1=公开 2=私有',
ADD COLUMN join_method TINYINT NOT NULL DEFAULT 1 COMMENT '加入方式: 1=自由加入 2=审核加入 3=邀请加入';

-- 订阅表
CREATE TABLE content_channel_subscription (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    source TINYINT NOT NULL DEFAULT 1 COMMENT '来源: 1=主动订阅 2=默认关注',
    remind_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '提醒开关: 0=关闭 1=开启',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_channel_id (channel_id),
    INDEX idx_user_id (user_id),
    UNIQUE INDEX uk_channel_user (channel_id, user_id)
) COMMENT '频道订阅表';

-- 订阅分组表
CREATE TABLE content_channel_subscription_group (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    group_name VARCHAR(50) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) COMMENT '订阅分组表';

-- 订阅分组关联表
CREATE TABLE content_channel_subscription_group_rel (
    id VARCHAR(36) PRIMARY KEY,
    subscription_id VARCHAR(36) NOT NULL,
    group_id VARCHAR(36) NOT NULL,
    UNIQUE INDEX uk_sub_group (subscription_id, group_id)
) COMMENT '订阅分组关联表';

-- 成员表
CREATE TABLE content_channel_member (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    role TINYINT NOT NULL DEFAULT 4 COMMENT '角色: 1=频道主 2=管理员 3=内容编辑 4=普通成员',
    join_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cooling_end_time DATETIME COMMENT '冷却期结束时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_channel_id (channel_id),
    INDEX idx_user_id (user_id),
    UNIQUE INDEX uk_channel_user (channel_id, user_id)
) COMMENT '频道成员表';

-- 加入申请表
CREATE TABLE content_channel_join_application (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    reason VARCHAR(500),
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1=待审核 2=已批准 3=已拒绝',
    reviewer_id VARCHAR(36),
    review_time DATETIME,
    review_reason VARCHAR(500),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_channel_id (channel_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) COMMENT '加入申请表';

-- 禁言表
CREATE TABLE content_channel_mute (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    operator_id VARCHAR(36) NOT NULL,
    reason VARCHAR(500),
    start_time DATETIME NOT NULL,
    end_time DATETIME COMMENT 'NULL表示永久禁言',
    unmute_type TINYINT COMMENT '解除方式: 1=自动到期 2=手动解除',
    unmute_time DATETIME,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_channel_user (channel_id, user_id)
) COMMENT '禁言记录表';

-- 黑名单表
CREATE TABLE content_channel_blacklist (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    operator_id VARCHAR(36) NOT NULL,
    reason VARCHAR(500),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_channel_id (channel_id),
    INDEX idx_user_id (user_id),
    UNIQUE INDEX uk_channel_user (channel_id, user_id)
) COMMENT '频道黑名单表';

-- 治理日志表
CREATE TABLE content_channel_governance_log (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL,
    action TINYINT NOT NULL COMMENT '操作类型: 1=移除 2=禁言 3=解除禁言 4=加入黑名单 5=移出黑名单',
    operator_id VARCHAR(36) NOT NULL,
    target_user_id VARCHAR(36) NOT NULL,
    reason VARCHAR(500),
    extra_data JSON COMMENT '扩展数据（冷却期结束时间等）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_channel_id (channel_id),
    INDEX idx_target_user (target_user_id)
) COMMENT '治理操作日志表';

-- 邀请表
CREATE TABLE content_channel_invite (
    id VARCHAR(36) PRIMARY KEY,
    channel_id VARCHAR(36) NOT NULL,
    code VARCHAR(32) NOT NULL,
    type TINYINT NOT NULL COMMENT '类型: 1=邀请码 2=邀请链接',
    max_uses INT COMMENT '最大使用次数，NULL表示不限',
    used_count INT NOT NULL DEFAULT 0,
    expire_time DATETIME COMMENT '过期时间，NULL表示不过期',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1=有效 2=已用完 3=已撤销 4=已过期',
    creator_id VARCHAR(36) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_channel_id (channel_id),
    INDEX idx_code (code),
    UNIQUE INDEX uk_code (code)
) COMMENT '频道邀请表';
```

### Step 2: 创建枚举类

```java
// PrivacyType.java
@Getter
@AllArgsConstructor
public enum PrivacyType {
    PUBLIC(1, "公开"),
    PRIVATE(2, "私有");

    private final int code;
    private final String desc;
}

// JoinMethod.java
@Getter
@AllArgsConstructor
public enum JoinMethod {
    FREE(1, "自由加入"),
    REVIEW(2, "审核加入"),
    INVITE(3, "邀请加入");

    private final int code;
    private final String desc;
}

// MemberRole.java
@Getter
@AllArgsConstructor
public enum MemberRole {
    OWNER(1, "频道主"),
    ADMIN(2, "管理员"),
    EDITOR(3, "内容编辑"),
    MEMBER(4, "普通成员");

    private final int code;
    private final String desc;
}

// ApplicationStatus.java
@Getter
@AllArgsConstructor
public enum ApplicationStatus {
    PENDING(1, "待审核"),
    APPROVED(2, "已批准"),
    REJECTED(3, "已拒绝");

    private final int code;
    private final String desc;
}

// GovernanceAction.java
@Getter
@AllArgsConstructor
public enum GovernanceAction {
    REMOVE(1, "移除"),
    MUTE(2, "禁言"),
    UNMUTE(3, "解除禁言"),
    BLACKLIST_ADD(4, "加入黑名单"),
    BLACKLIST_REMOVE(5, "移出黑名单");

    private final int code;
    private final String desc;
}

// InviteStatus.java
@Getter
@AllArgsConstructor
public enum InviteStatus {
    ACTIVE(1, "有效"),
    USED_UP(2, "已用完"),
    REVOKED(3, "已撤销"),
    EXPIRED(4, "已过期");

    private final int code;
    private final String desc;
}
```

### Step 3: 创建实体类

```java
// ChannelSubscription.java
@Data
@TableName("content_channel_subscription")
public class ChannelSubscription {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String channelId;
    private String userId;
    private Integer source;
    private Integer remindEnabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

// ChannelMember.java
@Data
@TableName("content_channel_member")
public class ChannelMember {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String channelId;
    private String userId;
    private Integer role;
    private LocalDateTime joinTime;
    private LocalDateTime coolingEndTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

// ChannelJoinApplication.java
@Data
@TableName("content_channel_join_application")
public class ChannelJoinApplication {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String channelId;
    private String userId;
    private String reason;
    private Integer status;
    private String reviewerId;
    private LocalDateTime reviewTime;
    private String reviewReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

// ChannelMute.java
@Data
@TableName("content_channel_mute")
public class ChannelMute {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String channelId;
    private String userId;
    private String operatorId;
    private String reason;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer unmuteType;
    private LocalDateTime unmuteTime;
    private LocalDateTime createTime;
}

// ChannelBlacklist.java
@Data
@TableName("content_channel_blacklist")
public class ChannelBlacklist {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String channelId;
    private String userId;
    private String operatorId;
    private String reason;
    private LocalDateTime createTime;
}

// ChannelGovernanceLog.java
@Data
@TableName("content_channel_governance_log")
public class ChannelGovernanceLog {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String channelId;
    private Integer action;
    private String operatorId;
    private String targetUserId;
    private String reason;
    private String extraData;
    private LocalDateTime createTime;
}

// ChannelInvite.java
@Data
@TableName("content_channel_invite")
public class ChannelInvite {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String channelId;
    private String code;
    private Integer type;
    private Integer maxUses;
    private Integer usedCount;
    private LocalDateTime expireTime;
    private Integer status;
    private String creatorId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

// ChannelSubscriptionGroup.java
@Data
@TableName("content_channel_subscription_group")
public class ChannelSubscriptionGroup {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String userId;
    private String groupName;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

### Step 4: 创建 Mapper 接口

```java
// ChannelSubscriptionMapper.java
public interface ChannelSubscriptionMapper extends BaseMapper<ChannelSubscription> {
}

// ChannelMemberMapper.java
public interface ChannelMemberMapper extends BaseMapper<ChannelMember> {
}

// ChannelJoinApplicationMapper.java
public interface ChannelJoinApplicationMapper extends BaseMapper<ChannelJoinApplication> {
}

// ChannelMuteMapper.java
public interface ChannelMuteMapper extends BaseMapper<ChannelMute> {
}

// ChannelBlacklistMapper.java
public interface ChannelBlacklistMapper extends BaseMapper<ChannelBlacklist> {
}

// ChannelGovernanceLogMapper.java
public interface ChannelGovernanceLogMapper extends BaseMapper<ChannelGovernanceLog> {
}

// ChannelInviteMapper.java
public interface ChannelInviteMapper extends BaseMapper<ChannelInvite> {
}

// ChannelSubscriptionGroupMapper.java
public interface ChannelSubscriptionGroupMapper extends BaseMapper<ChannelSubscriptionGroup> {
}
```

### Step 5: 验证迁移脚本

Run: 本地 MySQL 执行迁移脚本，确认表结构正确创建

### Step 6: Commit

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/
git commit -m "feat(channel): add privacy and membership database schema, entities, and mappers"
```

---

## Task 2: 频道隐私设置

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelPrivacyService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/impl/ChannelPrivacyServiceImpl.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelPrivacyServiceTest.java`

### Step 1: 编写隐私设置失败测试

```java
@ExtendWith(MockitoExtension.class)
class ChannelPrivacyServiceTest {

    @InjectMocks
    private ChannelPrivacyServiceImpl channelPrivacyService;

    @Mock
    private ChannelService channelService;

    @Test
    void should_set_channel_to_private() {
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setChannelType(ChannelType.PERSONAL.getCode());
        channel.setPrivacyType(PrivacyType.PUBLIC.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);

        channelPrivacyService.updatePrivacy("ch1", PrivacyType.PRIVATE, "user1");

        assertEquals(PrivacyType.PRIVATE.getCode(), channel.getPrivacyType());
        verify(channelService).updateById(channel);
    }

    @Test
    void should_reject_setting_system_channel_to_private() {
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setChannelType(ChannelType.SYSTEM.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);

        assertThrows(BusinessException.class,
            () -> channelPrivacyService.updatePrivacy("ch1", PrivacyType.PRIVATE, "user1"));
    }
}
```

### Step 2: 运行测试确认失败

Run: `mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest=ChannelPrivacyServiceTest -DfailIfNoTests=false`
Expected: FAIL - ChannelPrivacyServiceImpl not found

### Step 3: 实现 ChannelPrivacyService

```java
public interface ChannelPrivacyService {
    void updatePrivacy(String channelId, PrivacyType privacyType, String operatorId);
}

@Service
public class ChannelPrivacyServiceImpl implements ChannelPrivacyService {
    @Resource
    private ChannelService channelService;

    @Override
    public void updatePrivacy(String channelId, PrivacyType privacyType, String operatorId) {
        Channel channel = channelService.getById(channelId);
        if (channel == null) {
            throw new BusinessException("频道不存在");
        }
        if (channel.getChannelType() == ChannelType.SYSTEM.getCode()
            && privacyType == PrivacyType.PRIVATE) {
            throw new BusinessException("系统频道必须公开");
        }
        channel.setPrivacyType(privacyType.getCode());
        channelService.updateById(channel);
    }
}
```

### Step 4: 运行测试确认通过

Run: `mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content -Dtest=ChannelPrivacyServiceTest -DfailIfNoTests=false`
Expected: PASS

### Step 5: Commit

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelPrivacyServiceTest.java
git commit -m "feat(channel): implement channel privacy setting service"
```

---

## Task 3: 加入方式配置

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelJoinMethodService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/impl/ChannelJoinMethodServiceImpl.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelJoinMethodServiceTest.java`

### Step 1: 编写加入方式配置失败测试

```java
@ExtendWith(MockitoExtension.class)
class ChannelJoinMethodServiceTest {

    @InjectMocks
    private ChannelJoinMethodServiceImpl joinMethodService;

    @Mock
    private ChannelService channelService;

    @Test
    void should_set_join_method_to_review() {
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setJoinMethod(JoinMethod.FREE.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);

        joinMethodService.updateJoinMethod("ch1", JoinMethod.REVIEW, "user1");

        assertEquals(JoinMethod.REVIEW.getCode(), channel.getJoinMethod());
    }
}
```

### Step 2: 运行测试确认失败

Run: `mvn test -Dtest=ChannelJoinMethodServiceTest -DfailIfNoTests=false`
Expected: FAIL

### Step 3: 实现 ChannelJoinMethodService

```java
public interface ChannelJoinMethodService {
    void updateJoinMethod(String channelId, JoinMethod joinMethod, String operatorId);
}

@Service
public class ChannelJoinMethodServiceImpl implements ChannelJoinMethodService {
    @Resource
    private ChannelService channelService;

    @Override
    public void updateJoinMethod(String channelId, JoinMethod joinMethod, String operatorId) {
        Channel channel = channelService.getById(channelId);
        if (channel == null) {
            throw new BusinessException("频道不存在");
        }
        channel.setJoinMethod(joinMethod.getCode());
        channelService.updateById(channel);
    }
}
```

### Step 4: 运行测试确认通过

Run: `mvn test -Dtest=ChannelJoinMethodServiceTest -DfailIfNoTests=false`
Expected: PASS

### Step 5: Commit

```bash
git commit -m "feat(channel): implement join method configuration service"
```

---

## Task 4: 邀请机制

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelInviteService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/impl/ChannelInviteServiceImpl.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelInviteServiceTest.java`

### Step 1: 编写邀请有效性校验失败测试

```java
@ExtendWith(MockitoExtension.class)
class ChannelInviteServiceTest {

    @InjectMocks
    private ChannelInviteServiceImpl inviteService;

    @Mock
    private ChannelInviteMapper inviteMapper;

    @Test
    void should_validate_active_invite() {
        ChannelInvite invite = new ChannelInvite();
        invite.setStatus(InviteStatus.ACTIVE.getCode());
        invite.setExpireTime(LocalDateTime.now().plusDays(7));
        invite.setMaxUses(10);
        invite.setUsedCount(5);
        when(inviteMapper.selectOne(any())).thenReturn(invite);

        boolean valid = inviteService.validateInvite("INVITE123");
        assertTrue(valid);
    }

    @Test
    void should_reject_expired_invite() {
        ChannelInvite invite = new ChannelInvite();
        invite.setStatus(InviteStatus.ACTIVE.getCode());
        invite.setExpireTime(LocalDateTime.now().minusDays(1));
        when(inviteMapper.selectOne(any())).thenReturn(invite);

        assertThrows(BusinessException.class, () -> inviteService.validateInvite("INVITE123"));
    }
}
```

### Step 2-5: (实现 → 测试 → Commit)

---

## Task 5: 订阅与取消订阅

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelSubscriptionService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/impl/ChannelSubscriptionServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/ChannelSubscriptionBizService.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelSubscriptionServiceTest.java`

### Step 1: 编写订阅失败测试

```java
@ExtendWith(MockitoExtension.class)
class ChannelSubscriptionServiceTest {

    @InjectMocks
    private ChannelSubscriptionServiceImpl subscriptionService;

    @Mock
    private ChannelSubscriptionMapper subscriptionMapper;

    @Mock
    private ChannelService channelService;

    @Test
    void should_subscribe_to_public_channel() {
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setPrivacyType(PrivacyType.PUBLIC.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);
        when(subscriptionMapper.selectCount(any())).thenReturn(0L);

        subscriptionService.subscribe("ch1", "user1");

        verify(subscriptionMapper).insert(any(ChannelSubscription.class));
    }

    @Test
    void should_reject_subscribe_to_private_channel_for_non_member() {
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setPrivacyType(PrivacyType.PRIVATE.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);

        assertThrows(BusinessException.class,
            () -> subscriptionService.subscribe("ch1", "user1"));
    }

    @Test
    void should_unsubscribe() {
        ChannelSubscription sub = new ChannelSubscription();
        sub.setId("sub1");
        when(subscriptionMapper.selectOne(any())).thenReturn(sub);

        subscriptionService.unsubscribe("ch1", "user1");

        verify(subscriptionMapper).deleteById("sub1");
    }
}
```

### Step 2-5: (实现 → 测试 → Commit)

---

## Task 6: 订阅列表管理

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelSubscriptionGroupService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelSubscriptionController.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelSubscriptionGroupServiceTest.java`

### Step 1: 编写分组管理失败测试

```java
@ExtendWith(MockitoExtension.class)
class ChannelSubscriptionGroupServiceTest {

    @InjectMocks
    private ChannelSubscriptionGroupServiceImpl groupService;

    @Mock
    private ChannelSubscriptionGroupMapper groupMapper;

    @Test
    void should_create_group() {
        ChannelSubscriptionGroup group = new ChannelSubscriptionGroup();
        group.setUserId("user1");
        group.setGroupName("技术频道");

        groupService.createGroup(group);

        verify(groupMapper).insert(group);
    }

    @Test
    void should_move_subscription_to_group() {
        groupService.moveToGroup("sub1", "group1");
        // verify group relation update
    }
}
```

### Step 2-5: (实现 → 测试 → Commit)

---

## Task 7: 加入申请审核

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelJoinApplicationService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/ChannelMemberBizService.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelJoinApplicationServiceTest.java`

### Step 1: 编写申请审核失败测试

```java
@ExtendWith(MockitoExtension.class)
class ChannelJoinApplicationServiceTest {

    @InjectMocks
    private ChannelJoinApplicationServiceImpl applicationService;

    @Mock
    private ChannelJoinApplicationMapper applicationMapper;

    @Test
    void should_submit_application() {
        when(applicationMapper.selectCount(any())).thenReturn(0L);

        applicationService.apply("ch1", "user1", "希望加入");

        verify(applicationMapper).insert(any(ChannelJoinApplication.class));
    }

    @Test
    void should_reject_duplicate_application() {
        when(applicationMapper.selectCount(any())).thenReturn(1L);

        assertThrows(BusinessException.class,
            () -> applicationService.apply("ch1", "user1", "希望加入"));
    }

    @Test
    void should_approve_application() {
        ChannelJoinApplication app = new ChannelJoinApplication();
        app.setId("app1");
        app.setStatus(ApplicationStatus.PENDING.getCode());
        when(applicationMapper.selectById("app1")).thenReturn(app);

        applicationService.approve("app1", "admin1", "欢迎加入");

        assertEquals(ApplicationStatus.APPROVED.getCode(), app.getStatus());
    }
}
```

### Step 2-5: (实现 → 测试 → Commit)

---

## Task 8: 成员角色与权限

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelMemberService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelMemberRoleService.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelMemberServiceTest.java`

### Step 1: 编写成员角色失败测试

```java
@ExtendWith(MockitoExtension.class)
class ChannelMemberServiceTest {

    @InjectMocks
    private ChannelMemberServiceImpl memberService;

    @Mock
    private ChannelMemberMapper memberMapper;

    @Test
    void should_add_member_with_role() {
        when(memberMapper.selectCount(any())).thenReturn(0L);

        memberService.addMember("ch1", "user1", MemberRole.MEMBER);

        verify(memberMapper).insert(any(ChannelMember.class));
    }

    @Test
    void should_check_cooling_period() {
        ChannelMember member = new ChannelMember();
        member.setCoolingEndTime(LocalDateTime.now().plusDays(3));
        when(memberMapper.selectOne(any())).thenReturn(member);

        assertThrows(BusinessException.class,
            () -> memberService.addMember("ch1", "user1", MemberRole.MEMBER));
    }

    @Test
    void should_assign_role() {
        ChannelMember member = new ChannelMember();
        member.setId("m1");
        member.setRole(MemberRole.MEMBER.getCode());
        when(memberMapper.selectById("m1")).thenReturn(member);

        memberService.assignRole("m1", MemberRole.ADMIN, "owner1");

        assertEquals(MemberRole.ADMIN.getCode(), member.getRole());
    }
}
```

### Step 2-5: (实现 → 测试 → Commit)

---

## Task 9: 移除成员与冷却期

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/biz/ChannelGovernanceBizService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelGovernanceLogService.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelGovernanceServiceTest.java`

### Step 1: 编写移除成员失败测试

```java
@ExtendWith(MockitoExtension.class)
class ChannelGovernanceServiceTest {

    @InjectMocks
    private ChannelGovernanceBizService governanceService;

    @Mock
    private ChannelMemberService memberService;

    @Mock
    private ChannelGovernanceLogService logService;

    @Test
    void should_remove_member() {
        ChannelMember member = new ChannelMember();
        member.setId("m1");
        member.setRole(MemberRole.MEMBER.getCode());
        when(memberService.getById("m1")).thenReturn(member);

        governanceService.removeMember("m1", "admin1", "违规");

        verify(memberService).removeById("m1");
        verify(logService).log(eq(GovernanceAction.REMOVE), any());
    }

    @Test
    void should_reject_removing_owner() {
        ChannelMember member = new ChannelMember();
        member.setId("m1");
        member.setRole(MemberRole.OWNER.getCode());
        when(memberService.getById("m1")).thenReturn(member);

        assertThrows(BusinessException.class,
            () -> governanceService.removeMember("m1", "admin1", "违规"));
    }
}
```

### Step 2-5: (实现 → 测试 → Commit)

---

## Task 10: 禁言与自动解封

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelMuteService.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelMuteServiceTest.java`

### Step 1: 编写禁言失败测试

```java
@ExtendWith(MockitoExtension.class)
class ChannelMuteServiceTest {

    @InjectMocks
    private ChannelMuteServiceImpl muteService;

    @Mock
    private ChannelMuteMapper muteMapper;

    @Test
    void should_mute_member() {
        muteService.mute("ch1", "user1", "admin1", "违规", 7);

        verify(muteMapper).insert(any(ChannelMute.class));
    }

    @Test
    void should_check_mute_status() {
        ChannelMute mute = new ChannelMute();
        mute.setEndTime(LocalDateTime.now().plusDays(3));
        when(muteMapper.selectOne(any())).thenReturn(mute);

        assertTrue(muteService.isMuted("ch1", "user1"));
    }

    @Test
    void should_unmute() {
        ChannelMute mute = new ChannelMute();
        mute.setId("mute1");
        when(muteMapper.selectOne(any())).thenReturn(mute);

        muteService.unmute("ch1", "user1", "admin1");

        assertEquals(2, mute.getUnmuteType());
    }
}
```

### Step 2-5: (实现 → 测试 → Commit)

---

## Task 11: 黑名单管理

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/service/ChannelBlacklistService.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/service/ChannelBlacklistServiceTest.java`

### Step 1: 编写黑名单失败测试

```java
@ExtendWith(MockitoExtension.class)
class ChannelBlacklistServiceTest {

    @InjectMocks
    private ChannelBlacklistServiceImpl blacklistService;

    @Mock
    private ChannelBlacklistMapper blacklistMapper;

    @Test
    void should_add_to_blacklist() {
        when(blacklistMapper.selectCount(any())).thenReturn(0L);

        blacklistService.addToBlacklist("ch1", "user1", "admin1", "骚扰");

        verify(blacklistMapper).insert(any(ChannelBlacklist.class));
    }

    @Test
    void should_check_blacklist() {
        when(blacklistMapper.selectCount(any())).thenReturn(1L);

        assertTrue(blacklistService.isBlacklisted("ch1", "user1"));
    }

    @Test
    void should_remove_from_blacklist() {
        ChannelBlacklist entry = new ChannelBlacklist();
        entry.setId("bl1");
        when(blacklistMapper.selectOne(any())).thenReturn(entry);

        blacklistService.removeFromBlacklist("ch1", "user1", "admin1");

        verify(blacklistMapper).deleteById("bl1");
    }
}
```

### Step 2-5: (实现 → 测试 → Commit)

---

## Task 12: 治理操作 API

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelGovernanceController.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelMemberController.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/controller/ChannelGovernanceControllerTest.java`

### Step 1: 编写治理 API 测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class ChannelGovernanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_remove_member_via_api() throws Exception {
        mockMvc.perform(post("/channel/governance/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"memberId\":\"m1\",\"reason\":\"违规\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void should_mute_member_via_api() throws Exception {
        mockMvc.perform(post("/channel/governance/mute")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"memberId\":\"m1\",\"days\":7,\"reason\":\"违规\"}"))
            .andExpect(status().isOk());
    }
}
```

### Step 2-5: (实现 → 测试 → Commit)

---

## Task 13: 成员列表 API

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelMemberController.java` (如果 Task 12 未创建)
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/controller/ChannelMemberControllerTest.java`

### Step 1: 编写成员列表 API 测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class ChannelMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_list_members_with_filter() throws Exception {
        mockMvc.perform(get("/channel/members")
                .param("channelId", "ch1")
                .param("role", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.records").isArray());
    }

    @Test
    void should_search_members() throws Exception {
        mockMvc.perform(get("/channel/members/search")
                .param("channelId", "ch1")
                .param("keyword", "张三"))
            .andExpect(status().isOk());
    }
}
```

### Step 2-5: (实现 → 测试 → Commit)

---

## Task 14: 订阅 API

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelSubscriptionController.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/controller/ChannelSubscriptionControllerTest.java`

### Step 1: 编写订阅 API 测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class ChannelSubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_subscribe_channel() throws Exception {
        mockMvc.perform(post("/channel/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"channelId\":\"ch1\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void should_unsubscribe_channel() throws Exception {
        mockMvc.perform(post("/channel/unsubscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"channelId\":\"ch1\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void should_list_subscriptions() throws Exception {
        mockMvc.perform(get("/channel/subscriptions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.records").isArray());
    }
}
```

### Step 2-5: (实现 → 测试 → Commit)

---

## Task 15: 邀请 API

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelInviteController.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/controller/ChannelInviteControllerTest.java`

### Step 1: 编写邀请 API 测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class ChannelInviteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_create_invite() throws Exception {
        mockMvc.perform(post("/channel/invite/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"channelId\":\"ch1\",\"type\":1,\"maxUses\":10}"))
            .andExpect(status().isOk());
    }

    @Test
    void should_use_invite() throws Exception {
        mockMvc.perform(post("/channel/invite/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"INVITE123\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void should_revoke_invite() throws Exception {
        mockMvc.perform(post("/channel/invite/revoke")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"inviteId\":\"inv1\"}"))
            .andExpect(status().isOk());
    }
}
```

### Step 2-5: (实现 → 测试 → Commit)

---

## Task 16: 集成验证

### Step 1: 运行全部测试

Run: `mvn test -pl jeecg-boot/jeecg-boot-module/jeecg-module-content`
Expected: All tests PASS

### Step 2: 验证私有内容不可见性

手动验证或编写集成测试：搜索、推荐、直接访问、分享入口均不暴露私有频道内容

### Step 3: 验证核心操作性能

压测订阅/取消订阅/加入申请/审核/角色变更等操作，确认 P95 <= 500ms

### Step 4: Commit

```bash
git commit -m "test(channel): add integration verification for privacy and membership"
```
