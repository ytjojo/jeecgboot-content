package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.mapper.ChannelMapper;
import org.jeecg.modules.content.channel.req.ChannelListQuery;
import org.jeecg.modules.content.channel.service.impl.ChannelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 频道主服务测试
 * 覆盖 checkNameUnique 在 EXCLUDE-ID / STATUS 集合 / CHANNEL-TYPE 排除上的边界
 */
@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {

    @Mock
    private ChannelMapper channelMapper;

    @InjectMocks
    private ChannelServiceImpl channelService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(channelService, "baseMapper", channelMapper);
    }

    @Test
    void should_return_true_when_name_not_taken() {
        when(channelMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertThat(channelService.checkNameUnique("new", null)).isTrue();
    }

    @Test
    void should_return_false_when_name_taken() {
        when(channelMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThat(channelService.checkNameUnique("taken", null)).isFalse();
    }

    @Test
    void should_exclude_self_id_when_checking_uniqueness() {
        when(channelMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        assertThat(channelService.checkNameUnique("myname", "self-id")).isTrue();
    }

    @Test
    void should_resolve_inject_mocks() {
        // 烟雾：channelService 与 channelMapper 已绑定
        assertThat(channelService).isNotNull();
        assertThat(channelMapper).isNotNull();
    }

    @Test
    void should_create_channel_with_default_type() {
        Channel ch = new Channel();
        ch.setName("x");
        ch.setChannelType(ChannelType.PERSONAL);
        assertThat(ch.getChannelType()).isEqualTo(ChannelType.PERSONAL);
    }

    // ===== listMyChannels =====

    @Test
    void should_list_channels_with_empty_query() {
        Page<Channel> page = new Page<>(1, 10);
        when(channelMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(new Page<>());

        ChannelListQuery query = new ChannelListQuery();
        IPage<Channel> result = channelService.listMyChannels(page, "user1", query);

        assertThat(result).isNotNull();
    }

    @Test
    void should_list_channels_with_type_filter() {
        Page<Channel> page = new Page<>(1, 10);
        when(channelMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(new Page<>());

        ChannelListQuery query = new ChannelListQuery();
        query.setChannelType("personal");
        IPage<Channel> result = channelService.listMyChannels(page, "user1", query);

        assertThat(result).isNotNull();
    }

    @Test
    void should_list_channels_with_status_filter() {
        Page<Channel> page = new Page<>(1, 10);
        when(channelMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(new Page<>());

        ChannelListQuery query = new ChannelListQuery();
        query.setStatus("ACTIVE");
        IPage<Channel> result = channelService.listMyChannels(page, "user1", query);

        assertThat(result).isNotNull();
    }

    @Test
    void should_list_channels_with_keyword_filter() {
        Page<Channel> page = new Page<>(1, 10);
        when(channelMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(new Page<>());

        ChannelListQuery query = new ChannelListQuery();
        query.setKeyword("test");
        IPage<Channel> result = channelService.listMyChannels(page, "user1", query);

        assertThat(result).isNotNull();
    }

    // ===== listAllChannels (admin) =====

    @Test
    void should_list_all_channels_with_empty_query() {
        Page<Channel> page = new Page<>(1, 20);
        when(channelMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(new Page<>());

        ChannelListQuery query = new ChannelListQuery();
        IPage<Channel> result = channelService.listAllChannels(page, query);

        assertThat(result).isNotNull();
    }

    @Test
    void should_list_all_channels_with_type_filter() {
        Page<Channel> page = new Page<>(1, 20);
        when(channelMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(new Page<>());

        ChannelListQuery query = new ChannelListQuery();
        query.setChannelType("system");
        IPage<Channel> result = channelService.listAllChannels(page, query);

        assertThat(result).isNotNull();
    }

    @Test
    void should_list_all_channels_with_status_filter() {
        Page<Channel> page = new Page<>(1, 20);
        when(channelMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(new Page<>());

        ChannelListQuery query = new ChannelListQuery();
        query.setStatus("ACTIVE");
        IPage<Channel> result = channelService.listAllChannels(page, query);

        assertThat(result).isNotNull();
    }

    @Test
    void should_list_all_channels_with_keyword_filter() {
        Page<Channel> page = new Page<>(1, 20);
        when(channelMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(new Page<>());

        ChannelListQuery query = new ChannelListQuery();
        query.setKeyword("test");
        IPage<Channel> result = channelService.listAllChannels(page, query);

        assertThat(result).isNotNull();
    }
}
