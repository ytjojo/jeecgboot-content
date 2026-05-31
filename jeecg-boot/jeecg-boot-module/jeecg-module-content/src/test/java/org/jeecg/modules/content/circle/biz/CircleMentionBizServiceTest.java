package org.jeecg.modules.content.circle.biz;

import org.jeecg.modules.content.circle.service.ICircleMentionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 圈子@提及业务编排服务测试。
 */
@ExtendWith(MockitoExtension.class)
class CircleMentionBizServiceTest {

    @Mock
    private ICircleMentionService circleMentionService;

    @InjectMocks
    private CircleMentionBizService circleMentionBizService;

    private static final String TEST_CIRCLE_ID = "circle001";
    private static final String TEST_CONTENT_ID = "content001";
    private static final String TEST_PUBLISHER_ID = "publisher001";

    // ==================== processMentions ====================

    @Nested
    @DisplayName("processMentions - 处理@提及")
    class ProcessMentions {

        @Test
        @DisplayName("有提及 - 解析并异步通知")
        void processMentions_shouldParseAndNotifyAsync() {
            String content = "你好 @user-001";
            when(circleMentionService.parseMentions(content))
                    .thenReturn(Collections.singletonList("user-001"));

            circleMentionBizService.processMentions(
                    TEST_CIRCLE_ID, TEST_CONTENT_ID, content, TEST_PUBLISHER_ID);

            verify(circleMentionService).sendMentionNotifications(
                    TEST_CIRCLE_ID, TEST_CONTENT_ID,
                    Collections.singletonList("user-001"), TEST_PUBLISHER_ID);
        }

        @Test
        @DisplayName("无提及 - 不调用通知服务")
        void processMentions_noMentions_shouldDoNothing() {
            String content = "没有提及";
            when(circleMentionService.parseMentions(content))
                    .thenReturn(Collections.emptyList());

            circleMentionBizService.processMentions(
                    TEST_CIRCLE_ID, TEST_CONTENT_ID, content, TEST_PUBLISHER_ID);

            verify(circleMentionService, never()).sendMentionNotifications(
                    org.mockito.ArgumentMatchers.anyString(),
                    org.mockito.ArgumentMatchers.anyString(),
                    org.mockito.ArgumentMatchers.anyList(),
                    org.mockito.ArgumentMatchers.anyString());
        }
    }
}
