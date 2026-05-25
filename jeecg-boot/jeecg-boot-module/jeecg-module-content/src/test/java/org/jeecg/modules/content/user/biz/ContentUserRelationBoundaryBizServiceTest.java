package org.jeecg.modules.content.user.biz;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserBlock;
import org.jeecg.modules.content.user.mapper.ContentUserBlockMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 内容社区拉黑边界编排测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentUserRelationBoundaryBizServiceTest {

    @Mock
    private ContentUserBlockMapper blockMapper;

    @InjectMocks
    private ContentUserRelationBoundaryBizService relationBoundaryBizService;

    @Test
    void shouldDetectForwardBlockAsBidirectionalBoundary() {
        when(blockMapper.selectByPair("u1", "u2")).thenReturn(activeBlock("u1", "u2"));

        assertThat(relationBoundaryBizService.isBlockedEitherWay("u1", "u2")).isTrue();
        assertThat(relationBoundaryBizService.isBlockedBy("u1", "u2")).isTrue();
        assertThatThrownBy(() -> relationBoundaryBizService.assertCanInteract("u1", "u2"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("操作失败");
    }

    @Test
    void shouldDetectReverseBlockAsBidirectionalBoundary() {
        when(blockMapper.selectByPair("u1", "u2")).thenReturn(null);
        when(blockMapper.selectByPair("u2", "u1")).thenReturn(activeBlock("u2", "u1"));

        assertThat(relationBoundaryBizService.isBlockedEitherWay("u1", "u2")).isTrue();
        assertThatThrownBy(() -> relationBoundaryBizService.assertCanInteract("u1", "u2"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("操作失败");
    }

    @Test
    void shouldAllowInteractionWhenNoActiveBlockExists() {
        when(blockMapper.selectByPair("u1", "u2")).thenReturn(cancelledBlock("u1", "u2"));
        when(blockMapper.selectByPair("u2", "u1")).thenReturn(null);

        assertThat(relationBoundaryBizService.isBlockedEitherWay("u1", "u2")).isFalse();
        assertThat(relationBoundaryBizService.isBlockedBy("u1", "u2")).isFalse();
        relationBoundaryBizService.assertCanInteract("u1", "u2");
    }

    @Test
    void shouldHandleBlankUserIdsWithoutQueryingBlockTable() {
        assertThat(relationBoundaryBizService.isBlockedEitherWay(null, "u2")).isFalse();
        assertThat(relationBoundaryBizService.isBlockedEitherWay("u1", " ")).isFalse();
        assertThatThrownBy(() -> relationBoundaryBizService.assertCanInteract(null, "u2"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("当前用户ID不能为空");
        assertThatThrownBy(() -> relationBoundaryBizService.assertCanInteract("u1", " "))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("目标用户ID不能为空");
        verify(blockMapper, never()).selectByPair(null, "u2");
    }

    private ContentUserBlock activeBlock(String userId, String blockedUserId) {
        return new ContentUserBlock()
            .setUserId(userId)
            .setBlockedUserId(blockedUserId)
            .setStatus("ACTIVE");
    }

    @Test
    void shouldRejectOversizedUserIdInAssertCanInteract() {
        String longUserId = "a".repeat(65);
        assertThatThrownBy(() -> relationBoundaryBizService.assertCanInteract(longUserId, "u2"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("当前用户ID长度不能超过64位");
        assertThatThrownBy(() -> relationBoundaryBizService.assertCanInteract("u1", longUserId))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("目标用户ID长度不能超过64位");
        verify(blockMapper, never()).selectByPair(longUserId, "u2");
    }

    private ContentUserBlock cancelledBlock(String userId, String blockedUserId) {
        return new ContentUserBlock()
            .setUserId(userId)
            .setBlockedUserId(blockedUserId)
            .setStatus("CANCELLED");
    }
}
