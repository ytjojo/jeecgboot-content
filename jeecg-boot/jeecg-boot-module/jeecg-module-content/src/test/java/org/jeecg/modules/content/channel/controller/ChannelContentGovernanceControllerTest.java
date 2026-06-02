package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.biz.ChannelGovernanceBiz;
import org.jeecg.modules.content.channel.req.governance.ChannelGovernanceReq;
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
 * 频道内容治理控制器测试
 */
@ExtendWith(MockitoExtension.class)
class ChannelContentGovernanceControllerTest {

    @Mock
    private ChannelGovernanceBiz channelGovernanceBiz;

    @InjectMocks
    private ChannelContentGovernanceController controller;

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
    void should_execute_governance() {
        ChannelGovernanceReq req = new ChannelGovernanceReq();
        req.setChannelId("ch1");
        req.setContentId("c1");
        req.setAction("pin");

        Result<Void> result = controller.governance(req);

        assertThat(result.isSuccess()).isTrue();
        verify(channelGovernanceBiz).executeGovernance(req, "admin1");
    }
}
