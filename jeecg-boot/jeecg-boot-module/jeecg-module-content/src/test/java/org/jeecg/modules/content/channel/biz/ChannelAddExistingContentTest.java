package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.biz.impl.ChannelPublishBizImpl;
import org.jeecg.modules.content.channel.entity.ChannelContentPublish;
import org.jeecg.modules.content.channel.entity.ChannelContentReview;
import org.jeecg.modules.content.channel.mapper.ChannelContentPublishMapper;
import org.jeecg.modules.content.channel.mapper.ChannelContentReviewMapper;
import org.jeecg.modules.content.channel.req.publish.ChannelAddExistingContentReq;
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
class ChannelAddExistingContentTest {

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
    void shouldAddToChannelWithSourceTypeAddExisting() {
        ChannelAddExistingContentReq req = new ChannelAddExistingContentReq();
        req.setContentId("content-1");
        req.setContentType("article");
        req.setChannelIds(Arrays.asList("ch-1"));

        when(publishMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(java.util.Collections.emptyList());
        when(publishService.checkPublishPermission(anyString(), anyString(), anyBoolean(), anyBoolean()))
            .thenReturn("ALLOW");
        when(publishMapper.insert(any(ChannelContentPublish.class))).thenReturn(1);

        List<ChannelPublishResultVO> results = biz.addExistingContent(req, "user-1");

        assertEquals(1, results.size());
        assertEquals("PUBLISHED", results.get(0).getStatus());
        verify(publishMapper).insert(argThat((ChannelContentPublish p) ->
            "ADD_EXISTING".equals(p.getSourceType()) && "ch-1".equals(p.getChannelId())));
    }

    @Test
    void shouldRejectWhenAlreadyExists() {
        ChannelAddExistingContentReq req = new ChannelAddExistingContentReq();
        req.setContentId("content-1");
        req.setContentType("article");
        req.setChannelIds(Arrays.asList("ch-1"));

        ChannelContentPublish existing = new ChannelContentPublish();
        existing.setChannelId("ch-1");
        when(publishMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(existing));

        List<ChannelPublishResultVO> results = biz.addExistingContent(req, "user-1");

        assertEquals(1, results.size());
        assertEquals("FAILED", results.get(0).getStatus());
        assertEquals("该内容已存在于此频道", results.get(0).getFailReason());
        verify(publishMapper, never()).insert(any(ChannelContentPublish.class));
    }

    @Test
    void shouldSendToReviewWhenPermissionRequiresReview() {
        ChannelAddExistingContentReq req = new ChannelAddExistingContentReq();
        req.setContentId("content-1");
        req.setContentType("article");
        req.setChannelIds(Arrays.asList("ch-1"));

        when(publishMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(java.util.Collections.emptyList());
        when(publishService.checkPublishPermission(anyString(), anyString(), anyBoolean(), anyBoolean()))
            .thenReturn("REVIEW");
        when(reviewMapper.insert(any(ChannelContentReview.class))).thenReturn(1);

        List<ChannelPublishResultVO> results = biz.addExistingContent(req, "user-1");

        assertEquals(1, results.size());
        assertEquals("PENDING", results.get(0).getStatus());
        verify(reviewMapper).insert(argThat((ChannelContentReview r) ->
            "ADD_EXISTING".equals(r.getSourceScene())));
    }

    @Test
    void shouldRejectWhenPermissionDenied() {
        ChannelAddExistingContentReq req = new ChannelAddExistingContentReq();
        req.setContentId("content-1");
        req.setContentType("article");
        req.setChannelIds(Arrays.asList("ch-1"));

        when(publishMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(java.util.Collections.emptyList());
        when(publishService.checkPublishPermission(anyString(), anyString(), anyBoolean(), anyBoolean()))
            .thenReturn("REJECT");

        List<ChannelPublishResultVO> results = biz.addExistingContent(req, "user-1");

        assertEquals(1, results.size());
        assertEquals("FAILED", results.get(0).getStatus());
        assertEquals("权限不足", results.get(0).getFailReason());
    }
}
