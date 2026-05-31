package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.biz.ChannelGovernanceBizService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 频道治理控制器测试
 * 验证治理接口（移除/禁言/黑名单）正确委托给 bizService
 */
@ExtendWith(MockitoExtension.class)
class ChannelGovernanceControllerTest {

    @Mock
    private ChannelGovernanceBizService governanceBizService;

    @InjectMocks
    private ChannelGovernanceController controller;

    @BeforeEach
    void setUp() {
        LoginUser user = new LoginUser();
        user.setId("admin1");
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
    void should_remove_member() {
        // 移除成员接口应调用 bizService 的 removeMember
        Result<String> result = controller.removeMember("m1", "违规");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("已移除");
        verify(governanceBizService).removeMember("m1", "admin1", "违规");
    }

    @Test
    void should_mute_member() {
        // 禁言接口应调用 bizService 的 muteMember
        Result<String> result = controller.muteMember("ch1", "user1", 7, "违规发言");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("已禁言");
        verify(governanceBizService).muteMember("ch1", "user1", "admin1", "违规发言", 7);
    }

    @Test
    void should_unmute_member() {
        // 解除禁言接口应调用 bizService 的 unmuteMember
        Result<String> result = controller.unmuteMember("ch1", "user1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("已解除禁言");
        verify(governanceBizService).unmuteMember("ch1", "user1", "admin1");
    }

    @Test
    void should_add_to_blacklist() {
        // 加入黑名单接口应调用 bizService 的 addToBlacklist
        Result<String> result = controller.addToBlacklist("ch1", "user1", "骚扰");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("已加入黑名单");
        verify(governanceBizService).addToBlacklist("ch1", "user1", "admin1", "骚扰");
    }

    @Test
    void should_remove_from_blacklist() {
        // 移出黑名单接口应调用 bizService 的 removeFromBlacklist
        Result<String> result = controller.removeFromBlacklist("ch1", "user1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("已移出黑名单");
        verify(governanceBizService).removeFromBlacklist("ch1", "user1", "admin1");
    }
}
