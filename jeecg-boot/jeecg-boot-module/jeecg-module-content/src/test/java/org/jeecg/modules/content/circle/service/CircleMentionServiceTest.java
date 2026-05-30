package org.jeecg.modules.content.circle.service;

import org.jeecg.modules.content.circle.mapper.CircleMemberMapper;
import org.jeecg.modules.content.circle.service.impl.CircleMentionServiceImpl;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 圈子@提及服务测试。
 */
@ExtendWith(MockitoExtension.class)
class CircleMentionServiceTest {

    @Mock
    private CircleMemberMapper circleMemberMapper;

    @Mock
    private IContentNotificationService contentNotificationService;

    @InjectMocks
    private CircleMentionServiceImpl circleMentionService;

    private static final String TEST_CIRCLE_ID = "circle001";
    private static final String TEST_CONTENT_ID = "content001";
    private static final String TEST_PUBLISHER_ID = "publisher001";

    // ==================== parseMentions ====================

    @Nested
    @DisplayName("parseMentions - 解析@提及")
    class ParseMentions {

        @Test
        @DisplayName("解析 - 提取用户ID")
        void parseMentions_shouldExtractUserIds() {
            List<String> result = circleMentionService.parseMentions("你好 @user-001 和 @user-002");
            assertThat(result).containsExactly("user-001", "user-002");
        }

        @Test
        @DisplayName("无提及 - 返回空列表")
        void parseMentions_noMentions_shouldReturnEmpty() {
            List<String> result = circleMentionService.parseMentions("没有提及");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null输入 - 返回空列表")
        void parseMentions_nullInput_shouldReturnEmpty() {
            List<String> result = circleMentionService.parseMentions(null);
            assertThat(result).isEmpty();
        }
    }

    // ==================== getMentionCandidates ====================

    @Nested
    @DisplayName("getMentionCandidates - 获取可提及成员")
    class GetMentionCandidates {

        @Test
        @DisplayName("无关键词 - 返回全部成员")
        void getMentionCandidates_noKeyword_shouldReturnAllMembers() {
            List<String> members = Arrays.asList("user-001", "user-002", "user-003");
            when(circleMemberMapper.selectMemberUserIds(TEST_CIRCLE_ID)).thenReturn(members);

            List<String> result = circleMentionService.getMentionCandidates(TEST_CIRCLE_ID, null);

            assertThat(result).containsExactly("user-001", "user-002", "user-003");
        }

        @Test
        @DisplayName("有关键词 - 返回匹配成员")
        void getMentionCandidates_withKeyword_shouldFilterMembers() {
            List<String> members = Arrays.asList("user-001", "user-002", "admin-001");
            when(circleMemberMapper.selectMemberUserIds(TEST_CIRCLE_ID)).thenReturn(members);

            List<String> result = circleMentionService.getMentionCandidates(TEST_CIRCLE_ID, "admin");

            assertThat(result).containsExactly("admin-001");
        }
    }

    // ==================== sendMentionNotifications ====================

    @Nested
    @DisplayName("sendMentionNotifications - 发送@提及通知")
    class SendMentionNotifications {

        @Test
        @DisplayName("跳过已退出成员")
        void sendMentionNotifications_shouldSkipExitedMembers() {
            List<String> activeMembers = Collections.singletonList("user-001");
            when(circleMemberMapper.selectMemberUserIds(TEST_CIRCLE_ID)).thenReturn(activeMembers);

            List<String> mentionedUserIds = Arrays.asList("user-001", "user-002");
            circleMentionService.sendMentionNotifications(
                    TEST_CIRCLE_ID, TEST_CONTENT_ID, mentionedUserIds, TEST_PUBLISHER_ID);

            verify(contentNotificationService).sendNotification(
                    eq("user-001"), eq("MENTION"), eq("你被@提及了"),
                    eq("圈子内容 content001 中提到了你"));
            verify(contentNotificationService, never()).sendNotification(
                    eq("user-002"), eq("MENTION"), eq("你被@提及了"),
                    eq("圈子内容 content001 中提到了你"));
        }
    }
}
