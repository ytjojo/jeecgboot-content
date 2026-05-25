package org.jeecg.modules.content.user.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserNotInterested;
import org.jeecg.modules.content.user.mapper.ContentUserNotInterestedMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserNotInterestedServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * 不感兴趣反馈服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentUserNotInterestedServiceTest {

    @Mock
    private ContentUserNotInterestedMapper notInterestedMapper;

    @InjectMocks
    private ContentUserNotInterestedServiceImpl notInterestedService;

    @Test
    void shouldRecordNotInterestedFeedback() {
        notInterestedService.recordFeedback("u1", "c1", "ARTICLE");

        verify(notInterestedMapper).insert(argThat((ContentUserNotInterested entity) ->
            entity != null
                && entity.getUserId().equals("u1")
                && entity.getContentId().equals("c1")
                && entity.getContentType().equals("ARTICLE")
                && entity.getFeedbackTime() != null
                && entity.getStatus().equals("ACTIVE")
        ));
    }

    @Test
    void shouldRejectNullContentId() {
        assertThatThrownBy(() -> notInterestedService.recordFeedback("u1", null, "ARTICLE"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("内容ID不能为空");
        verify(notInterestedMapper, never()).insert(any(ContentUserNotInterested.class));
    }

    @Test
    void shouldRejectEmptyContentId() {
        assertThatThrownBy(() -> notInterestedService.recordFeedback("u1", "  ", "ARTICLE"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("内容ID不能为空");
        verify(notInterestedMapper, never()).insert(any(ContentUserNotInterested.class));
    }

    @Test
    void shouldRejectOverLengthContentId() {
        assertThatThrownBy(() -> notInterestedService.recordFeedback("u1", "a".repeat(129), "ARTICLE"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("内容ID长度不能超过128位");
        verify(notInterestedMapper, never()).insert(any(ContentUserNotInterested.class));
    }

    @Test
    void shouldRejectNullContentType() {
        assertThatThrownBy(() -> notInterestedService.recordFeedback("u1", "c1", null))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("内容类型不能为空");
        verify(notInterestedMapper, never()).insert(any(ContentUserNotInterested.class));
    }

    @Test
    void shouldRejectEmptyContentType() {
        assertThatThrownBy(() -> notInterestedService.recordFeedback("u1", "c1", "  "))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("内容类型不能为空");
        verify(notInterestedMapper, never()).insert(any(ContentUserNotInterested.class));
    }

    @Test
    void shouldRejectOverLengthContentType() {
        assertThatThrownBy(() -> notInterestedService.recordFeedback("u1", "c1", "a".repeat(65)))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("内容类型长度不能超过64位");
        verify(notInterestedMapper, never()).insert(any(ContentUserNotInterested.class));
    }

    @Test
    void shouldRejectNullUserId() {
        assertThatThrownBy(() -> notInterestedService.recordFeedback(null, "c1", "ARTICLE"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("用户ID不能为空");
        verify(notInterestedMapper, never()).insert(any(ContentUserNotInterested.class));
    }
}
