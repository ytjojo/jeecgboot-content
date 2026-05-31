package org.jeecg.modules.content.channel.biz;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ChannelJoinApplication;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.enums.JoinMethod;
import org.jeecg.modules.content.channel.enums.MemberRole;
import org.jeecg.modules.content.channel.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

/**
 * 成员业务逻辑测试
 * 验证加入频道、冷却期检查、角色分配的业务规则
 */
@ExtendWith(MockitoExtension.class)
class ChannelMemberBizServiceTest {

    @Mock
    private ChannelService channelService;
    @Mock
    private ChannelMemberService memberService;
    @Mock
    private ChannelBlacklistService blacklistService;
    @Mock
    private ChannelInviteService inviteService;
    @Mock
    private ChannelJoinApplicationService applicationService;

    @InjectMocks
    private ChannelMemberBizService bizService;

    @Test
    void should_add_member_with_free_join() {
        // 自由加入模式下，非黑名单用户应直接成为成员
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setJoinMethod(JoinMethod.FREE.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);
        when(blacklistService.isBlacklisted("ch1", "user1")).thenReturn(false);

        bizService.joinByFree("ch1", "user1");

        verify(memberService).addMember("ch1", "user1", MemberRole.MEMBER);
    }

    @Test
    void should_reject_free_join_for_non_free_channel() {
        // 非自由加入模式的频道不能通过 free 方式加入
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setJoinMethod(JoinMethod.REVIEW.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);

        assertThatThrownBy(() -> bizService.joinByFree("ch1", "user1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("不是自由加入模式");
    }

    @Test
    void should_check_cooling_period_blocks_rejoining() {
        // 冷却期内的用户不能重新加入，防止恶意退出再加入
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setJoinMethod(JoinMethod.FREE.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);
        when(blacklistService.isBlacklisted("ch1", "user1")).thenReturn(false);
        doThrow(new JeecgBootException("冷却期内无法加入"))
            .when(memberService).addMember("ch1", "user1", MemberRole.MEMBER);

        assertThatThrownBy(() -> bizService.joinByFree("ch1", "user1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("冷却期");
    }

    @Test
    void should_reject_blacklisted_user_joining() {
        // 黑名单用户不能通过任何方式加入频道
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setJoinMethod(JoinMethod.FREE.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);
        when(blacklistService.isBlacklisted("ch1", "user1")).thenReturn(true);

        assertThatThrownBy(() -> bizService.joinByFree("ch1", "user1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("无法加入");
    }

    @Test
    void should_submit_application_via_review_join() {
        // 审核加入模式下应提交申请而非直接加入
        Channel channel = new Channel();
        channel.setId("ch1");
        channel.setJoinMethod(JoinMethod.REVIEW.getCode());
        when(channelService.getById("ch1")).thenReturn(channel);
        when(blacklistService.isBlacklisted("ch1", "user1")).thenReturn(false);

        bizService.joinByReview("ch1", "user1", "希望加入");

        verify(applicationService).apply("ch1", "user1", "希望加入");
    }

    @Test
    void should_approve_and_add_member() {
        // 审批通过后应自动将用户添加为成员
        ChannelJoinApplication app = new ChannelJoinApplication();
        app.setId("app1");
        app.setChannelId("ch1");
        app.setUserId("user1");
        when(applicationService.getById("app1")).thenReturn(app);

        bizService.approveAndAddMember("app1", "admin1", "欢迎");

        verify(applicationService).approve("app1", "admin1", "欢迎");
        verify(memberService).addMember("ch1", "user1", MemberRole.MEMBER);
    }

    @Test
    void should_reject_join_when_channel_not_found() {
        // 加入不存在的频道应明确报错
        when(channelService.getById("nonexistent")).thenReturn(null);

        assertThatThrownBy(() -> bizService.joinByFree("nonexistent", "user1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("频道不存在");
    }
}
