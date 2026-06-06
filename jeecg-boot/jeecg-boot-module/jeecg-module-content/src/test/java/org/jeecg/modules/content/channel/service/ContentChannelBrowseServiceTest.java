package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ContentChannelCategory;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.mapper.ChannelMapper;
import org.jeecg.modules.content.channel.mapper.ContentChannelCategoryMapper;
import org.jeecg.modules.content.channel.req.query.ChannelBrowseQueryReq;
import org.jeecg.modules.content.channel.service.impl.ContentChannelBrowseServiceImpl;
import org.jeecg.modules.content.channel.vo.ChannelBrowseItemVO;
import org.jeecg.modules.content.user.entity.ContentSubscriptionSource;
import org.jeecg.modules.content.user.mapper.ContentSubscriptionSourceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentChannelBrowseServiceTest {

    @Mock
    private ChannelMapper channelMapper;

    @Mock
    private ContentSubscriptionSourceMapper subscriptionSourceMapper;

    @Mock
    private ContentChannelCategoryMapper categoryMapper;

    @InjectMocks
    private ContentChannelBrowseServiceImpl browseService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(browseService, "baseMapper", channelMapper);
    }

    @Test
    void browseByCategory_shouldReturnChannelsWithSubscriberCount() {
        // 准备数据
        Channel channel1 = createChannel("ch1", "频道1", ChannelType.PERSONAL, "cat1");
        Channel channel2 = createChannel("ch2", "频道2", ChannelType.ORGANIZATION, "cat1");

        ContentSubscriptionSource source1 = createSubscriptionSource("ch1", 100);
        ContentSubscriptionSource source2 = createSubscriptionSource("ch2", 200);

        ContentChannelCategory category = createCategory("cat1", "科技");

        // mock 分页查询
        Page<Channel> channelPage = new Page<>(1, 20, 2);
        channelPage.setRecords(Arrays.asList(channel1, channel2));
        when(channelMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(channelPage);

        // mock 订阅源查询
        when(subscriptionSourceMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(source1, source2));

        // mock 分类查询
        when(categoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(category));

        // 执行查询
        ChannelBrowseQueryReq req = new ChannelBrowseQueryReq();
        req.setCategoryId("cat1");
        req.setPageNo(1);
        req.setPageSize(20);
        IPage<ChannelBrowseItemVO> result = browseService.browseByCategory(req);

        // 验证结果（默认按订阅数降序排列）
        assertThat(result.getRecords()).hasSize(2);
        assertThat(result.getRecords().get(0).getChannelName()).isEqualTo("频道2");
        assertThat(result.getRecords().get(0).getSubscriberCount()).isEqualTo(200L);
        assertThat(result.getRecords().get(1).getChannelName()).isEqualTo("频道1");
        assertThat(result.getRecords().get(1).getSubscriberCount()).isEqualTo(100L);
        assertThat(result.getRecords().get(1).getCategoryName()).isEqualTo("科技");
    }

    @Test
    void browseByCategory_shouldSortBySubscriberCountDesc() {
        // 准备数据
        Channel channel1 = createChannel("ch1", "频道1", ChannelType.PERSONAL, "cat1");
        Channel channel2 = createChannel("ch2", "频道2", ChannelType.PERSONAL, "cat1");

        ContentSubscriptionSource source1 = createSubscriptionSource("ch1", 100);
        ContentSubscriptionSource source2 = createSubscriptionSource("ch2", 200);

        ContentChannelCategory category = createCategory("cat1", "科技");

        // mock 分页查询
        Page<Channel> channelPage = new Page<>(1, 20, 2);
        channelPage.setRecords(Arrays.asList(channel1, channel2));
        when(channelMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(channelPage);

        // mock 订阅源查询
        when(subscriptionSourceMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(source1, source2));

        // mock 分类查询
        when(categoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(category));

        // 执行查询（默认按订阅数排序）
        ChannelBrowseQueryReq req = new ChannelBrowseQueryReq();
        req.setCategoryId("cat1");
        req.setSortBy("SUBSCRIBER_COUNT");
        req.setPageNo(1);
        req.setPageSize(20);
        IPage<ChannelBrowseItemVO> result = browseService.browseByCategory(req);

        // 验证结果：按订阅数降序排列
        assertThat(result.getRecords()).hasSize(2);
        assertThat(result.getRecords().get(0).getSubscriberCount()).isEqualTo(200L);
        assertThat(result.getRecords().get(1).getSubscriberCount()).isEqualTo(100L);
    }

    @Test
    void browseByCategory_shouldFilterByChannelType() {
        // 准备数据
        Channel channel1 = createChannel("ch1", "频道1", ChannelType.PERSONAL, "cat1");
        ContentSubscriptionSource source1 = createSubscriptionSource("ch1", 100);
        ContentChannelCategory category = createCategory("cat1", "科技");

        // mock 分页查询
        Page<Channel> channelPage = new Page<>(1, 20, 1);
        channelPage.setRecords(Collections.singletonList(channel1));
        when(channelMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(channelPage);

        // mock 订阅源查询
        when(subscriptionSourceMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(source1));

        // mock 分类查询
        when(categoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(category));

        // 执行查询（筛选 PERSONAL 类型）
        ChannelBrowseQueryReq req = new ChannelBrowseQueryReq();
        req.setCategoryId("cat1");
        req.setChannelType("PERSONAL");
        req.setPageNo(1);
        req.setPageSize(20);
        IPage<ChannelBrowseItemVO> result = browseService.browseByCategory(req);

        // 验证结果
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getChannelType()).isEqualTo("PERSONAL");
    }

    @Test
    void browseByCategory_shouldHandleEmptyResult() {
        // mock 分页查询返回空结果
        Page<Channel> channelPage = new Page<>(1, 20, 0);
        channelPage.setRecords(Collections.emptyList());
        when(channelMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(channelPage);

        // 执行查询
        ChannelBrowseQueryReq req = new ChannelBrowseQueryReq();
        req.setCategoryId("cat1");
        req.setPageNo(1);
        req.setPageSize(20);
        IPage<ChannelBrowseItemVO> result = browseService.browseByCategory(req);

        // 验证结果
        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    void browseByCategory_shouldHandleMissingSubscriptionSource() {
        // 准备数据
        Channel channel1 = createChannel("ch1", "频道1", ChannelType.PERSONAL, "cat1");
        ContentChannelCategory category = createCategory("cat1", "科技");

        // mock 分页查询
        Page<Channel> channelPage = new Page<>(1, 20, 1);
        channelPage.setRecords(Collections.singletonList(channel1));
        when(channelMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(channelPage);

        // mock 订阅源查询返回空
        when(subscriptionSourceMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        // mock 分类查询
        when(categoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(category));

        // 执行查询
        ChannelBrowseQueryReq req = new ChannelBrowseQueryReq();
        req.setCategoryId("cat1");
        req.setPageNo(1);
        req.setPageSize(20);
        IPage<ChannelBrowseItemVO> result = browseService.browseByCategory(req);

        // 验证结果：订阅数默认为 0
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getSubscriberCount()).isEqualTo(0L);
    }

    private Channel createChannel(String id, String name, ChannelType type, String categoryId) {
        Channel channel = new Channel();
        channel.setId(id);
        channel.setName(name);
        channel.setChannelType(type);
        channel.setCategoryId(categoryId);
        channel.setStatus(ChannelStatus.ACTIVE);
        channel.setPrivacy(1);
        return channel;
    }

    private ContentSubscriptionSource createSubscriptionSource(String sourceId, int subscriberCount) {
        ContentSubscriptionSource source = new ContentSubscriptionSource();
        source.setSourceType("CHANNEL");
        source.setSourceId(sourceId);
        source.setSubscriberCount(subscriberCount);
        source.setHeatScore(BigDecimal.valueOf(subscriberCount));
        return source;
    }

    private ContentChannelCategory createCategory(String id, String name) {
        ContentChannelCategory category = new ContentChannelCategory();
        category.setId(id);
        category.setName(name);
        return category;
    }
}
