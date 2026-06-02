package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.biz.ChannelMemberBizService;
import org.jeecg.modules.content.channel.entity.ChannelInvite;
import org.jeecg.modules.content.channel.service.ChannelInviteService;
import org.jeecg.modules.content.channel.service.ChannelMemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 频道邀请控制器测试
 */
@ExtendWith(MockitoExtension.class)
class ChannelInviteControllerTest {

    @Mock
    private ChannelInviteService inviteService;
    @Mock
    private ChannelMemberBizService memberBizService;
    @Mock
    private ChannelMemberService memberService;

    @InjectMocks
    private ChannelInviteController controller;

    @BeforeEach
    void setUp() {
        LoginUser user = new LoginUser();
        user.setId("operator1");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
            JSON.toJSONString(user), null));
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_create_invite() {
        ChannelInvite invite = new ChannelInvite();
        when(inviteService.createInvite("ch1", 1, 10, 7, "operator1")).thenReturn(invite);

        Result<?> result = controller.createInvite("ch1", 1, 10, 7);

        assertThat(result.isSuccess()).isTrue();
        verify(inviteService).createInvite("ch1", 1, 10, 7, "operator1");
    }

    @Test
    void should_list_invites() {
        when(inviteService.listByChannel("ch1")).thenReturn(Collections.emptyList());

        Result<?> result = controller.listInvites("ch1");

        assertThat(result.isSuccess()).isTrue();
        verify(inviteService).listByChannel("ch1");
    }

    @Test
    void should_revoke_invite() {
        Result<String> result = controller.revokeInvite("inv1");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).isEqualTo("已撤销");
        verify(inviteService).revokeInvite("inv1", "operator1");
    }

    @Test
    void should_use_invite() {
        Result<String> result = controller.useInvite("ch1", "ABCD");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).isEqualTo("加入成功");
        verify(memberBizService).joinByInvite("ch1", "operator1", "ABCD");
    }
}
