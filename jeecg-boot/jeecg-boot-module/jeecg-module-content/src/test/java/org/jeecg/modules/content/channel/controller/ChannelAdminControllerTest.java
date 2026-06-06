package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.biz.ChannelBizManageService;
import org.jeecg.modules.content.channel.dto.CreateChannelDTO;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.enums.ReviewResult;
import org.jeecg.modules.content.channel.req.ChannelListQuery;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.jeecg.modules.content.channel.vo.ChannelVO;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 频道后台管理控制器测试
 * 验证系统频道创建与审核操作的委托行为
 */
@ExtendWith(MockitoExtension.class)
class ChannelAdminControllerTest {

    @Mock
    private ChannelBizManageService channelBizManageService;

    @Mock
    private ChannelService channelService;

    @InjectMocks
    private ChannelAdminController controller;

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
    void should_create_system_channel() {
        CreateChannelDTO dto = new CreateChannelDTO();
        dto.setName("SystemChannel");
        Channel ch = new Channel();
        ch.setId("sys1");
        ch.setChannelType(ChannelType.SYSTEM);
        when(channelBizManageService.createSystemChannel(any(CreateChannelDTO.class), eq("admin1")))
            .thenReturn(ch);

        Result<?> result = controller.createSystemChannel(dto);

        assertThat(result.isSuccess()).isTrue();
        // DTO 强制为 SYSTEM 类型
        assertThat(dto.getChannelType()).isEqualTo(ChannelType.SYSTEM);
        verify(channelBizManageService).createSystemChannel(dto, "admin1");
    }

    @Test
    void should_review_channel() {
        Result<Void> result = controller.reviewChannel("ch1", ReviewResult.PASS, "ok");

        assertThat(result.isSuccess()).isTrue();
        verify(channelBizManageService).reviewChannel("ch1", "admin1", ReviewResult.PASS, "ok");
    }

    @Test
    void should_list_all_channels_for_admin() {
        Page<Channel> page = new Page<>(1, 20);
        IPage<Channel> serviceResult = new Page<>();
        when(channelService.listAllChannels(any(Page.class), any(ChannelListQuery.class)))
            .thenReturn(serviceResult);

        Result<IPage<ChannelVO>> result = controller.listAllChannels(1, 20, new ChannelListQuery());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isNotNull();
        verify(channelService).listAllChannels(any(Page.class), any(ChannelListQuery.class));
    }
}
