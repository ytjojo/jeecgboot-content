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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for community role enum, moderator permission validation, and audit logging.
 */
@ExtendWith(MockitoExtension.class)
class ContentCommunityRoleTest {

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private ContentUserAuditLogMapper auditLogMapper;

    @InjectMocks
    private ContentUserGovernanceServiceImpl governanceService;

    // --- Enum tests ---

    @Test
    void enumShouldHaveFourRoles() {
        assertThat(ContentCommunityRoleEnum.values()).hasSize(4);
    }

    @Test
    void enumShouldContainExpectedCodes() {
        assertThat(ContentCommunityRoleEnum.codes()).containsExactlyInAnyOrder("NORMAL", "CREATOR", "MODERATOR", "ADMIN");
    }

    @Test
    void enumValuesShouldHaveNameAndDescription() {
        for (ContentCommunityRoleEnum role : ContentCommunityRoleEnum.values()) {
            assertThat(role.getName()).isNotBlank();
            assertThat(role.getDescription()).isNotBlank();
        }
    }

    // --- Moderator delete comment tests ---

    @Test
    void moderatorCanDeleteComment() {
        when(profileMapper.selectByUserId("mod-1")).thenReturn(
            new ContentUserProfile().setUserId("mod-1").setCommunityRole("MODERATOR"));

        governanceService.deleteComment("mod-1", "comment-1", "违规内容");

        verify(auditLogMapper).insert(argThat((ContentUserAuditLog log) ->
            "MODERATOR_ACTION".equals(log.getEventType())
                && "mod-1".equals(log.getOperatorUserId())
                && "DELETE_COMMENT".equals(log.getEventContent())
                && log.getExtraDataJson().contains("comment-1")
                && log.getExtraDataJson().contains("违规内容")));
    }

    @Test
    void adminCanDeleteComment() {
        when(profileMapper.selectByUserId("admin-1")).thenReturn(
            new ContentUserProfile().setUserId("admin-1").setCommunityRole("ADMIN"));

        governanceService.deleteComment("admin-1", "comment-2", "广告");

        verify(auditLogMapper).insert(any(ContentUserAuditLog.class));
    }

    // --- Moderator warn user tests ---

    @Test
    void moderatorCanWarnUser() {
        when(profileMapper.selectByUserId("mod-1")).thenReturn(
            new ContentUserProfile().setUserId("mod-1").setCommunityRole("MODERATOR"));

        governanceService.warnUser("mod-1", "user-1", "多次发布不当内容");

        verify(auditLogMapper).insert(argThat((ContentUserAuditLog log) ->
            "MODERATOR_ACTION".equals(log.getEventType())
                && "mod-1".equals(log.getOperatorUserId())
                && "WARN_USER".equals(log.getEventContent())
                && log.getUserId().equals("user-1")
                && log.getExtraDataJson().contains("多次发布不当内容")));
    }

    @Test
    void adminCanWarnUser() {
        when(profileMapper.selectByUserId("admin-1")).thenReturn(
            new ContentUserProfile().setUserId("admin-1").setCommunityRole("ADMIN"));

        governanceService.warnUser("admin-1", "user-2", "警告原因");

        verify(auditLogMapper).insert(any(ContentUserAuditLog.class));
    }

    // --- Permission denial tests ---

    @Test
    void normalUserCannotDeleteComment() {
        when(profileMapper.selectByUserId("user-1")).thenReturn(
            new ContentUserProfile().setUserId("user-1").setCommunityRole("NORMAL"));

        assertThatThrownBy(() -> governanceService.deleteComment("user-1", "comment-1", "reason"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("版主或管理员权限");

        verify(auditLogMapper, never()).insert(any(ContentUserAuditLog.class));
    }

    @Test
    void normalUserCannotWarnUser() {
        when(profileMapper.selectByUserId("user-1")).thenReturn(
            new ContentUserProfile().setUserId("user-1").setCommunityRole("NORMAL"));

        assertThatThrownBy(() -> governanceService.warnUser("user-1", "user-2", "reason"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("版主或管理员权限");

        verify(auditLogMapper, never()).insert(any(ContentUserAuditLog.class));
    }

    @Test
    void userWithNullRoleCannotDeleteComment() {
        when(profileMapper.selectByUserId("user-1")).thenReturn(
            new ContentUserProfile().setUserId("user-1").setCommunityRole(null));

        assertThatThrownBy(() -> governanceService.deleteComment("user-1", "comment-1", "reason"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("版主或管理员权限");

        verify(auditLogMapper, never()).insert(any(ContentUserAuditLog.class));
    }

    @Test
    void creatorCannotDeleteComment() {
        when(profileMapper.selectByUserId("creator-1")).thenReturn(
            new ContentUserProfile().setUserId("creator-1").setCommunityRole("CREATOR"));

        assertThatThrownBy(() -> governanceService.deleteComment("creator-1", "comment-1", "reason"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("版主或管理员权限");

        verify(auditLogMapper, never()).insert(any(ContentUserAuditLog.class));
    }

    @Test
    void userWithNoProfileCannotDeleteComment() {
        when(profileMapper.selectByUserId("ghost-1")).thenReturn(null);

        assertThatThrownBy(() -> governanceService.deleteComment("ghost-1", "comment-1", "reason"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("版主或管理员权限");

        verify(auditLogMapper, never()).insert(any(ContentUserAuditLog.class));
    }

    // --- Audit log tests ---

    @Test
    void deleteCommentAuditLogShouldContainCorrectFields() {
        when(profileMapper.selectByUserId("mod-1")).thenReturn(
            new ContentUserProfile().setUserId("mod-1").setCommunityRole("MODERATOR"));

        governanceService.deleteComment("mod-1", "comment-99", "色情内容");

        verify(auditLogMapper).insert(argThat((ContentUserAuditLog log) ->
            log.getEventType().equals("MODERATOR_ACTION")
                && log.getEventContent().equals("DELETE_COMMENT")
                && log.getOperatorUserId().equals("mod-1")
                && log.getEventTime() != null
                && log.getExtraDataJson().contains("comment-99")));
    }

    @Test
    void warnUserAuditLogShouldContainCorrectFields() {
        when(profileMapper.selectByUserId("admin-1")).thenReturn(
            new ContentUserProfile().setUserId("admin-1").setCommunityRole("ADMIN"));

        governanceService.warnUser("admin-1", "user-5", "骚扰其他用户");

        verify(auditLogMapper).insert(argThat((ContentUserAuditLog log) ->
            log.getEventType().equals("MODERATOR_ACTION")
                && log.getEventContent().equals("WARN_USER")
                && log.getOperatorUserId().equals("admin-1")
                && log.getUserId().equals("user-5")
                && log.getEventTime() != null));
    }
}
