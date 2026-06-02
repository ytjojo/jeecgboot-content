package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.biz.ChannelReviewBiz;
import org.jeecg.modules.content.channel.req.review.ChannelReviewReq;
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
 * 频道内容审核控制器测试
 */
@ExtendWith(MockitoExtension.class)
class ChannelContentReviewControllerTest {

    @Mock
    private ChannelReviewBiz channelReviewBiz;

    @InjectMocks
    private ChannelContentReviewController controller;

    @BeforeEach
    void setUp() {
        LoginUser user = new LoginUser();
        user.setId("reviewer1");
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
    void should_review_content() {
        ChannelReviewReq req = new ChannelReviewReq();
        req.setReviewId("r1");
        req.setAction("approved");

        Result<Void> result = controller.review(req);

        assertThat(result.isSuccess()).isTrue();
        verify(channelReviewBiz).review(req, "reviewer1");
    }

    @Test
    void should_pass_reject_reason_through() {
        ChannelReviewReq req = new ChannelReviewReq();
        req.setReviewId("r2");
        req.setAction("rejected");
        req.setRejectReason("violation");

        Result<Void> result = controller.review(req);

        assertThat(result.isSuccess()).isTrue();
        verify(channelReviewBiz).review(req, "reviewer1");
    }
}
