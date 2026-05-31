package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.biz.impl.ChannelGovernanceBizImpl;
import org.jeecg.modules.content.channel.entity.ChannelContentPublish;
import org.jeecg.modules.content.channel.mapper.ChannelContentPublishMapper;
import org.jeecg.modules.content.channel.req.governance.ChannelGovernanceReq;
import org.jeecg.modules.content.channel.service.ChannelContentGovernanceLogService;
import org.jeecg.modules.content.channel.service.ChannelEditAssistService;
import org.jeecg.modules.content.channel.service.ChannelRecycleBinService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelGovernanceBizTest {

    @InjectMocks
    private ChannelGovernanceBizImpl biz;

    @Mock
    private ChannelContentPublishMapper publishMapper;

    @Mock
    private ChannelRecycleBinService recycleBinService;

    @Mock
    private ChannelContentGovernanceLogService governanceLogService;

    @Mock
    private ChannelEditAssistService editAssistService;

    @Test
    void pin_shouldSetPinnedTrue() {
        ChannelContentPublish publish = new ChannelContentPublish();
        publish.setIsPinned(false);
        when(publishMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(publish);
        when(publishMapper.updateById(any(ChannelContentPublish.class))).thenReturn(1);

        ChannelGovernanceReq req = new ChannelGovernanceReq();
        req.setChannelId("ch-1");
        req.setContentId("content-1");
        req.setAction("PIN");

        biz.executeGovernance(req, "admin-1");

        assertTrue(publish.getIsPinned());
        verify(publishMapper).updateById(publish);
        verify(governanceLogService).log(eq("ch-1"), eq("content-1"), eq("admin-1"), eq("PIN"), isNull(), isNull(), eq("SUCCESS"));
    }

    @Test
    void feature_shouldSetFeaturedTrue() {
        ChannelContentPublish publish = new ChannelContentPublish();
        publish.setIsFeatured(false);
        when(publishMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(publish);
        when(publishMapper.updateById(any(ChannelContentPublish.class))).thenReturn(1);

        ChannelGovernanceReq req = new ChannelGovernanceReq();
        req.setChannelId("ch-1");
        req.setContentId("content-1");
        req.setAction("FEATURE");

        biz.executeGovernance(req, "admin-1");

        assertTrue(publish.getIsFeatured());
    }

    @Test
    void move_shouldCreateNewPublishAndRecycleOld() {
        ChannelContentPublish publish = new ChannelContentPublish();
        publish.setContentType("article");
        publish.setPublisherId("user-1");
        when(publishMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(publish);
        when(publishMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(publishMapper.insert(any(ChannelContentPublish.class))).thenReturn(1);
        when(publishMapper.updateById(any(ChannelContentPublish.class))).thenReturn(1);

        ChannelGovernanceReq req = new ChannelGovernanceReq();
        req.setChannelId("ch-1");
        req.setContentId("content-1");
        req.setAction("MOVE");
        req.setTargetChannelId("ch-2");

        biz.executeGovernance(req, "admin-1");

        verify(publishMapper).insert(argThat((ChannelContentPublish p) -> "ch-2".equals(p.getChannelId()) && "MOVE".equals(p.getSourceType())));
        assertEquals("RECYCLED", publish.getPublishStatus());
    }

    @Test
    void move_shouldFailWhenNoTargetChannel() {
        ChannelGovernanceReq req = new ChannelGovernanceReq();
        req.setChannelId("ch-1");
        req.setContentId("content-1");
        req.setAction("MOVE");

        assertThrows(JeecgBootException.class, () -> biz.executeGovernance(req, "admin-1"));
    }

    @Test
    void editAssist_shouldRecordEditHistory() {
        Map<String, String> editFields = new HashMap<>();
        editFields.put("title", "新标题");

        ChannelGovernanceReq req = new ChannelGovernanceReq();
        req.setChannelId("ch-1");
        req.setContentId("content-1");
        req.setAction("EDIT_ASSIST");
        req.setEditFields(editFields);

        biz.executeGovernance(req, "admin-1");

        verify(editAssistService).recordEdit("ch-1", "content-1", "admin-1", "title", null, "新标题");
    }

    @Test
    void delete_shouldMoveToRecycleBin() {
        ChannelContentPublish publish = new ChannelContentPublish();
        publish.setContentType("article");
        publish.setPublisherId("user-1");
        when(publishMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(publish);
        when(publishMapper.updateById(any(ChannelContentPublish.class))).thenReturn(1);

        ChannelGovernanceReq req = new ChannelGovernanceReq();
        req.setChannelId("ch-1");
        req.setContentId("content-1");
        req.setAction("DELETE");
        req.setReason("违规内容");

        biz.executeGovernance(req, "admin-1");

        verify(recycleBinService).addToRecycleBin("ch-1", "content-1", "article", "user-1", "admin-1", "违规内容");
        assertEquals("RECYCLED", publish.getPublishStatus());
    }

    @Test
    void unsupportedAction_shouldThrow() {
        ChannelGovernanceReq req = new ChannelGovernanceReq();
        req.setChannelId("ch-1");
        req.setContentId("content-1");
        req.setAction("UNKNOWN");

        assertThrows(JeecgBootException.class, () -> biz.executeGovernance(req, "admin-1"));
        verify(governanceLogService).log(eq("ch-1"), eq("content-1"), eq("admin-1"), eq("UNKNOWN"), isNull(), isNull(), eq("FAILED"));
    }
}
