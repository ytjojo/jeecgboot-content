package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.entity.ChannelSubscription;
import org.jeecg.modules.content.channel.service.ChannelSubscriptionGroupService;
import org.jeecg.modules.content.channel.service.ChannelSubscriptionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 频道订阅控制器测试
 * 验证订阅接口正确委托给 service 层
 */
@ExtendWith(MockitoExtension.class)
class ChannelSubscriptionControllerTest {

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_USERNAME = "testuser";

    @Mock
    private ChannelSubscriptionService subscriptionService;
    @Mock
    private ChannelSubscriptionGroupService groupService;

    @InjectMocks
    private ChannelSubscriptionController controller;

    @BeforeEach
    void setUp() {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(TEST_USER_ID);
        loginUser.setUsername(TEST_USERNAME);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                JSON.toJSONString(loginUser), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_subscribe_channel() {
        Result<String> result = controller.subscribe("ch1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("订阅成功");
        verify(subscriptionService).subscribe("ch1", TEST_USER_ID);
    }

    @Test
    void should_unsubscribe_channel() {
        Result<String> result = controller.unsubscribe("ch1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("已取消订阅");
        verify(subscriptionService).unsubscribe("ch1", TEST_USER_ID);
    }

    @Test
    void should_list_subscriptions() {
        ChannelSubscription sub1 = new ChannelSubscription();
        sub1.setId("sub1");
        ChannelSubscription sub2 = new ChannelSubscription();
        sub2.setId("sub2");
        when(subscriptionService.listByUser(TEST_USER_ID)).thenReturn(Arrays.asList(sub1, sub2));

        Result<List<ChannelSubscription>> result = controller.listSubscriptions();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getResult()).hasSize(2);
    }

    @Test
    void should_get_subscription_status() {
        when(subscriptionService.isSubscribed("ch1", TEST_USER_ID)).thenReturn(true);

        Result<Boolean> result = controller.getStatus("ch1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getResult()).isTrue();
    }

    @Test
    void should_get_subscription_status_false_when_not_subscribed() {
        when(subscriptionService.isSubscribed("ch1", TEST_USER_ID)).thenReturn(false);

        Result<Boolean> result = controller.getStatus("ch1");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getResult()).isFalse();
    }
}
