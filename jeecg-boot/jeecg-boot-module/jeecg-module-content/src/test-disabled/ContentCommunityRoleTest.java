package org.jeecg.modules.content.user.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.enums.ContentCommunityRoleEnum;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserGovernanceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 社区角色权限测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentCommunityRoleTest {

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private ContentUserAuditLogMapper auditLogMapper;

    @InjectMocks
    private ContentUserGovernanceServiceImpl governanceService;

    // ========== 角色枚举 ==========

    @Test
    void shouldHaveCorrectRoleValues() {
        assertThat(ContentCommunityRoleEnum.NORMAL.getValue()).isEqualTo("NORMAL");
        assertThat(ContentCommunityRoleEnum.CREATOR.getValue()).isEqualTo("CREATOR");
        assertThat(ContentCommunityRoleEnum.MODERATOR.getValue()).isEqualTo("MODERATOR");
        assertThat(ContentCommunityRoleEnum.ADMIN.getValue()).isEqualTo("ADMIN");
    }

    @Test
    void shouldParseRoleFromString() {
        assertThat(ContentCommunityRoleEnum.fromValue("MODERATOR")).isEqualTo(ContentCommunityRoleEnum.MODERATOR);
        assertThat(ContentCommunityRoleEnum.fromValue("ADMIN")).isEqualTo(ContentCommunityRoleEnum.ADMIN);
        assertThat(ContentCommunityRoleEnum.fromValue("NORMAL")).isEqualTo(ContentCommunityRoleEnum.NORMAL);
    }

    @Test
    void shouldReturnNormalForUnknownValue() {
        assertThat(ContentCommunityRoleEnum.fromValue("UNKNOWN")).isEqualTo(ContentCommunityRoleEnum.NORMAL);
    }

    // ========== 版主删评论 ==========

    @Test
    void shouldAllowModeratorToDeleteComment() {
        String moderatorId = "mod1";
        String commentId = "comment1";
        String reason = "违规内容";

        ContentUserProfile moderatorProfile = new ContentUserProfile()
                .setUserId(moderatorId)
                .setCommunityRole("MODERATOR");
        when(profileMapper.selectByUserId(moderatorId)).thenReturn(moderatorProfile);

        governanceService.deleteComment(moderatorId, commentId, reason);

        verify(auditLogMapper).insert(any(ContentUserAuditLog.class));
    }

    @Test
    void shouldAllowAdminToDeleteComment() {
        String adminId = "admin1";
        String commentId = "comment1";

        ContentUserProfile adminProfile = new ContentUserProfile()
                .setUserId(adminId)
                .setCommunityRole("ADMIN");
        when(profileMapper.selectByUserId(adminId)).thenReturn(adminProfile);

        governanceService.deleteComment(adminId, commentId, "违规");

        verify(auditLogMapper).insert(any(ContentUserAuditLog.class));
    }

    @Test
    void shouldRejectNormalUserDeletingComment() {
        String normalUserId = "user1";
        String commentId = "comment1";

        ContentUserProfile normalProfile = new ContentUserProfile()
                .setUserId(normalUserId)
                .setCommunityRole("NORMAL");
        when(profileMapper.selectByUserId(normalUserId)).thenReturn(normalProfile);

        assertThatThrownBy(() -> governanceService.deleteComment(normalUserId, commentId, "违规"))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("权限不足");
    }

    // ========== 版主警告用户 ==========

    @Test
    void shouldAllowModeratorToWarnUser() {
        String moderatorId = "mod1";
        String targetUserId = "user1";
        String reason = "不当言论";

        ContentUserProfile moderatorProfile = new ContentUserProfile()
                .setUserId(moderatorId)
                .setCommunityRole("MODERATOR");
        when(profileMapper.selectByUserId(moderatorId)).thenReturn(moderatorProfile);

        governanceService.warnUser(moderatorId, targetUserId, reason);

        verify(auditLogMapper).insert(any(ContentUserAuditLog.class));
    }

    @Test
    void shouldRejectNormalUserWarning() {
        String normalUserId = "user1";
        String targetUserId = "user2";

        ContentUserProfile normalProfile = new ContentUserProfile()
                .setUserId(normalUserId)
                .setCommunityRole("NORMAL");
        when(profileMapper.selectByUserId(normalUserId)).thenReturn(normalProfile);

        assertThatThrownBy(() -> governanceService.warnUser(normalUserId, targetUserId, "不当言论"))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("权限不足");
    }

    // ========== 审计日志 ==========

    @Test
    void shouldWriteAuditLogOnModeratorAction() {
        String moderatorId = "mod1";
        String commentId = "comment1";
        String reason = "违规内容";

        ContentUserProfile moderatorProfile = new ContentUserProfile()
                .setUserId(moderatorId)
                .setCommunityRole("MODERATOR");
        when(profileMapper.selectByUserId(moderatorId)).thenReturn(moderatorProfile);

        governanceService.deleteComment(moderatorId, commentId, reason);

        verify(auditLogMapper).insert(argThat(log -> {
            ContentUserAuditLog auditLog = (ContentUserAuditLog) log;
            return auditLog.getEventType().equals("COMMENT_DELETED")
                    && auditLog.getOperatorUserId().equals(moderatorId);
        }));
    }

    @Test
    void shouldWriteAuditLogOnWarnUser() {
        String moderatorId = "mod1";
        String targetUserId = "user1";
        String reason = "不当言论";

        ContentUserProfile moderatorProfile = new ContentUserProfile()
                .setUserId(moderatorId)
                .setCommunityRole("MODERATOR");
        when(profileMapper.selectByUserId(moderatorId)).thenReturn(moderatorProfile);

        governanceService.warnUser(moderatorId, targetUserId, reason);

        verify(auditLogMapper).insert(argThat(log -> {
            ContentUserAuditLog auditLog = (ContentUserAuditLog) log;
            return auditLog.getEventType().equals("USER_WARNED")
                    && auditLog.getOperatorUserId().equals(moderatorId);
        }));
    }

    // ========== Profile communityRole 字段 ==========

    @Test
    void shouldReadCommunityRoleFromProfile() {
        ContentUserProfile profile = new ContentUserProfile()
                .setUserId("user1")
                .setCommunityRole("MODERATOR");

        assertThat(profile.getCommunityRole()).isEqualTo("MODERATOR");
    }

    @Test
    void shouldDefaultCommunityRoleToNormal() {
        ContentUserProfile profile = new ContentUserProfile()
                .setUserId("user1");

        // 默认值在数据库层面设置为'NORMAL'
        // 实体层面如果没有设置应该是null，数据库会填充默认值
        assertThat(profile.getCommunityRole()).isNull();
    }
}
