package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.biz.impl.ChannelPublishBizImpl;
import org.jeecg.modules.content.channel.entity.*;
import org.jeecg.modules.content.channel.mapper.*;
import org.jeecg.modules.content.channel.service.IChannelLifecycleLogService;
import org.jeecg.modules.content.channel.req.publish.ChannelPublishReq;
import org.jeecg.modules.content.channel.service.ChannelContentPublishService;
import org.jeecg.modules.content.channel.service.ChannelPublishLimitService;
import org.jeecg.modules.content.channel.vo.publish.AvailableChannelVO;
import org.jeecg.modules.content.channel.vo.publish.ChannelPublishResultVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelPublishBizTest {

    @InjectMocks
    private ChannelPublishBizImpl biz;

    @Mock
    private ChannelContentPublishService publishService;

    @Mock
    private ChannelPublishLimitService limitService;

    @Mock
    private ChannelContentPublishMapper publishMapper;

    @Mock
    private ChannelContentReviewMapper reviewMapper;

    @Mock
    private IChannelLifecycleLogService lifecycleLogService;

    @Mock
    private ChannelMapper channelMapper;

    @Mock
    private ChannelMemberMapper channelMemberMapper;

    @Mock
    private ChannelBlacklistMapper channelBlacklistMapper;

    @Mock
    private ChannelMuteMapper channelMuteMapper;

    @Mock
    private ChannelPublishLimitMapper channelPublishLimitMapper;

    @Mock
    private ChannelLifecycleLogMapper channelLifecycleLogMapper;

    @Test
    void shouldPublishToMultipleChannels() {
        when(publishService.checkPublishPermission(anyString(), anyString(), anyBoolean(), anyBoolean())).thenReturn("ALLOW");
        when(limitService.checkLimit(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt())).thenReturn("PASS");
        when(publishMapper.insert(any(ChannelContentPublish.class))).thenReturn(1);

        ChannelPublishReq req = new ChannelPublishReq();
        req.setContentId("content-1");
        req.setContentType("article");
        req.setChannelIds(Arrays.asList("ch-1", "ch-2"));

        List<ChannelPublishResultVO> results = biz.publish(req, "user-1");
        assertEquals(2, results.size());
        assertEquals("PUBLISHED", results.get(0).getStatus());
        assertEquals("PUBLISHED", results.get(1).getStatus());
    }

    @Test
    void shouldRejectWhenPermissionDenied() {
        when(publishService.checkPublishPermission(anyString(), anyString(), anyBoolean(), anyBoolean())).thenReturn("REJECT");

        ChannelPublishReq req = new ChannelPublishReq();
        req.setContentId("content-1");
        req.setContentType("article");
        req.setChannelIds(Arrays.asList("ch-1"));

        List<ChannelPublishResultVO> results = biz.publish(req, "user-1");
        assertEquals(1, results.size());
        assertEquals("FAILED", results.get(0).getStatus());
        assertEquals("权限不足", results.get(0).getFailReason());
    }

    @Test
    void shouldSendToReviewWhenPreReviewMode() {
        when(publishService.checkPublishPermission(anyString(), anyString(), anyBoolean(), anyBoolean())).thenReturn("REVIEW");
        when(reviewMapper.insert(any(ChannelContentReview.class))).thenReturn(1);

        ChannelPublishReq req = new ChannelPublishReq();
        req.setContentId("content-1");
        req.setContentType("article");
        req.setChannelIds(Arrays.asList("ch-1"));

        List<ChannelPublishResultVO> results = biz.publish(req, "user-1");
        assertEquals(1, results.size());
        assertEquals("PENDING", results.get(0).getStatus());
    }

    @Test
    void getAvailableChannels_shouldReturnActiveChannels() {
        ChannelMember member = new ChannelMember();
        member.setChannelId("ch-1");
        member.setUserId("user-1");
        member.setRole(4); // MEMBER
        when(channelMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(member));

        Channel channel = new Channel();
        channel.setId("ch-1");
        channel.setName("测试频道");
        channel.setIconUrl("http://icon.png");
        when(channelMapper.selectBatchIds(any())).thenReturn(Arrays.asList(channel));

        // No lifecycle logs => channel is ACTIVE
        when(channelLifecycleLogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList());

        when(channelBlacklistMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList());
        when(channelMuteMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList());

        List<AvailableChannelVO> result = biz.getAvailableChannels("user-1");

        assertEquals(1, result.size());
        assertEquals("ch-1", result.get(0).getChannelId());
        assertEquals("测试频道", result.get(0).getChannelName());
        assertTrue(result.get(0).getCanPublish());
    }

    @Test
    void getAvailableChannels_shouldBlockWhenFrozen() {
        ChannelMember member = new ChannelMember();
        member.setChannelId("ch-1");
        member.setUserId("user-1");
        member.setRole(4);
        when(channelMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(member));

        Channel channel = new Channel();
        channel.setId("ch-1");
        channel.setName("冻结频道");
        when(channelMapper.selectBatchIds(any())).thenReturn(Arrays.asList(channel));

        ChannelLifecycleLog log = new ChannelLifecycleLog();
        log.setChannelId("ch-1");
        log.setToStatus("ReadonlyFrozen");
        when(channelLifecycleLogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(log));

        when(channelBlacklistMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList());
        when(channelMuteMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList());

        List<AvailableChannelVO> result = biz.getAvailableChannels("user-1");

        assertEquals(1, result.size());
        assertFalse(result.get(0).getCanPublish());
        assertNotNull(result.get(0).getBlockedReason());
    }

    @Test
    void getAvailableChannels_shouldReturnEmptyWhenNoMembership() {
        when(channelMemberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList());

        List<AvailableChannelVO> result = biz.getAvailableChannels("user-1");

        assertTrue(result.isEmpty());
    }
}
