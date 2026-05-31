package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.biz.impl.ChannelPublishBizImpl;
import org.jeecg.modules.content.channel.entity.ChannelContentPublish;
import org.jeecg.modules.content.channel.entity.ChannelContentReview;
import org.jeecg.modules.content.channel.mapper.ChannelContentPublishMapper;
import org.jeecg.modules.content.channel.mapper.ChannelContentReviewMapper;
import org.jeecg.modules.content.channel.req.publish.ChannelPublishReq;
import org.jeecg.modules.content.channel.service.ChannelContentPublishService;
import org.jeecg.modules.content.channel.service.ChannelPublishLimitService;
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
}
