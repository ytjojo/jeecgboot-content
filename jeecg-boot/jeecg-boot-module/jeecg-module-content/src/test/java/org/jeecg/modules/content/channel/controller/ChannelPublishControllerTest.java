package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.biz.ChannelPublishBiz;
import org.jeecg.modules.content.channel.req.publish.ChannelAddExistingContentReq;
import org.jeecg.modules.content.channel.req.publish.ChannelPublishReq;
import org.jeecg.modules.content.channel.vo.publish.ChannelPublishResultVO;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 频道内容发布控制器测试
 */
@ExtendWith(MockitoExtension.class)
class ChannelPublishControllerTest {

    @Mock
    private ChannelPublishBiz channelPublishBiz;

    @InjectMocks
    private ChannelPublishController controller;

    @BeforeEach
    void setUp() {
        LoginUser user = new LoginUser();
        user.setId("user1");
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
    void should_publish_content() {
        ChannelPublishReq req = new ChannelPublishReq();
        List<ChannelPublishResultVO> results = Collections.emptyList();
        when(channelPublishBiz.publish(req, "user1")).thenReturn(results);

        Result<List<ChannelPublishResultVO>> result = controller.publish(req);

        assertThat(result.isSuccess()).isTrue();
        verify(channelPublishBiz).publish(req, "user1");
    }

    @Test
    void should_add_existing_content() {
        ChannelAddExistingContentReq req = new ChannelAddExistingContentReq();
        List<ChannelPublishResultVO> results = Collections.emptyList();
        when(channelPublishBiz.addExistingContent(req, "user1")).thenReturn(results);

        Result<List<ChannelPublishResultVO>> result = controller.addExistingContent(req);

        assertThat(result.isSuccess()).isTrue();
        verify(channelPublishBiz).addExistingContent(req, "user1");
    }
}
