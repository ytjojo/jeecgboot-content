package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.biz.impl.ChannelGovernanceBizImpl;
import org.jeecg.modules.content.channel.entity.ChannelContentPublish;
import org.jeecg.modules.content.channel.entity.ChannelRecycleBin;
import org.jeecg.modules.content.channel.mapper.ChannelContentPublishMapper;
import org.jeecg.modules.content.channel.mapper.ChannelRecycleBinMapper;
import org.jeecg.modules.content.channel.req.governance.ChannelGovernanceReq;
import org.jeecg.modules.content.channel.req.governance.GovernanceContentListReq;
import org.jeecg.modules.content.channel.req.governance.RecycleBinListReq;
import org.jeecg.modules.content.channel.service.ChannelContentGovernanceLogService;
import org.jeecg.modules.content.channel.service.ChannelEditAssistService;
import org.jeecg.modules.content.channel.service.ChannelRecycleBinService;
import org.jeecg.modules.content.channel.vo.governance.GovernanceContentItemVO;
import org.jeecg.modules.content.channel.vo.governance.RecycleBinItemVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.Date;
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

    @Mock
    private ChannelRecycleBinMapper recycleBinMapper;

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
    void restore_shouldFailWhenNotRecycled() {
        ChannelContentPublish publish = new ChannelContentPublish();
        publish.setPublishStatus("PUBLISHED");
        when(publishMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(publish);

        ChannelGovernanceReq req = new ChannelGovernanceReq();
        req.setChannelId("ch-1");
        req.setContentId("content-1");
        req.setAction("RESTORE");

        assertThrows(JeecgBootException.class, () -> biz.executeGovernance(req, "admin-1"));
        verify(publishMapper, never()).updateById(any(ChannelContentPublish.class));
    }

    @Test
    void restore_shouldSucceedWhenRecycled() {
        ChannelContentPublish publish = new ChannelContentPublish();
        publish.setPublishStatus("RECYCLED");
        when(publishMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(publish);
        when(publishMapper.updateById(any(ChannelContentPublish.class))).thenReturn(1);

        ChannelGovernanceReq req = new ChannelGovernanceReq();
        req.setChannelId("ch-1");
        req.setContentId("content-1");
        req.setAction("RESTORE");

        biz.executeGovernance(req, "admin-1");

        assertEquals("PUBLISHED", publish.getPublishStatus());
        verify(publishMapper).updateById(publish);
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

    @Test
    void getContentList_shouldReturnPaginatedResults() {
        ChannelContentPublish publish = new ChannelContentPublish();
        publish.setId("pub-1");
        publish.setChannelId("ch-1");
        publish.setContentId("content-1");
        publish.setContentType("article");
        publish.setPublishStatus("PUBLISHED");
        publish.setIsPinned(false);
        publish.setPublisherId("user-1");

        Page<ChannelContentPublish> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(publish));
        page.setTotal(1);
        when(publishMapper.selectPage(any(IPage.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        GovernanceContentListReq req = new GovernanceContentListReq();
        req.setChannelId("ch-1");

        Page<GovernanceContentItemVO> result = biz.getContentList(req);

        assertEquals(1, result.getRecords().size());
        assertEquals("pub-1", result.getRecords().get(0).getId());
        assertEquals("PUBLISHED", result.getRecords().get(0).getPublishStatus());
    }

    @Test
    void getRecycleBinList_shouldReturnPaginatedResults() {
        ChannelRecycleBin recycleBin = new ChannelRecycleBin();
        recycleBin.setId("rb-1");
        recycleBin.setChannelId("ch-1");
        recycleBin.setContentId("content-1");
        recycleBin.setContentType("article");
        recycleBin.setDeletedBy("admin-1");
        recycleBin.setExpireTime(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L));

        Page<ChannelRecycleBin> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(recycleBin));
        page.setTotal(1);
        when(recycleBinMapper.selectPage(any(IPage.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        RecycleBinListReq req = new RecycleBinListReq();
        req.setChannelId("ch-1");

        Page<RecycleBinItemVO> result = biz.getRecycleBinList(req);

        assertEquals(1, result.getRecords().size());
        assertEquals("rb-1", result.getRecords().get(0).getId());
        assertNotNull(result.getRecords().get(0).getRemainingDays());
    }
}
