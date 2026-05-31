package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ContentChannelTag;
import org.jeecg.modules.content.channel.mapper.ContentChannelTagMapper;
import org.jeecg.modules.content.channel.req.create.ChannelTagCreateReq;
import org.jeecg.modules.content.channel.service.impl.ContentChannelTagServiceImpl;
import org.jeecg.modules.content.channel.vo.ChannelTagVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentChannelTagServiceTest {

    @Mock
    private ContentChannelTagMapper tagMapper;

    @InjectMocks
    private ContentChannelTagServiceImpl tagService;

    @Test
    void createTag_shouldRejectEmptyName() {
        ChannelTagCreateReq req = new ChannelTagCreateReq();
        req.setChannelId("ch1");
        req.setName("");

        assertThatThrownBy(() -> tagService.createTag(req))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("标签名称不能为空");
    }

    @Test
    void createTag_shouldRejectNameExceeding20Chars() {
        ChannelTagCreateReq req = new ChannelTagCreateReq();
        req.setChannelId("ch1");
        req.setName("a".repeat(21));

        assertThatThrownBy(() -> tagService.createTag(req))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("标签名称不能超过20个字符");
    }

    @Test
    void createTag_shouldRejectDuplicateActiveName() {
        ContentChannelTag activeTag = new ContentChannelTag();
        activeTag.setId("tag1");
        activeTag.setChannelId("ch1");
        activeTag.setName("教程");
        activeTag.setStatus(1);
        when(tagMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(activeTag);

        ChannelTagCreateReq req = new ChannelTagCreateReq();
        req.setChannelId("ch1");
        req.setName("教程");

        assertThatThrownBy(() -> tagService.createTag(req))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("该标签已存在");
    }

    @Test
    void createTag_shouldReactivateSoftDeletedTag() {
        ContentChannelTag deletedTag = new ContentChannelTag();
        deletedTag.setId("tag1");
        deletedTag.setChannelId("ch1");
        deletedTag.setName("教程");
        deletedTag.setStatus(0);
        when(tagMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(deletedTag);
        when(tagMapper.updateById(any(ContentChannelTag.class))).thenReturn(1);

        ChannelTagCreateReq req = new ChannelTagCreateReq();
        req.setChannelId("ch1");
        req.setName("教程");

        ContentChannelTag result = tagService.createTag(req);

        assertThat(result.getStatus()).isEqualTo(1);
        verify(tagMapper).updateById(any(ContentChannelTag.class));
        verify(tagMapper, never()).insert(any(ContentChannelTag.class));
    }

    @Test
    void createTag_shouldCreateValidTag() {
        when(tagMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(tagMapper.insert(any(ContentChannelTag.class))).thenReturn(1);

        ChannelTagCreateReq req = new ChannelTagCreateReq();
        req.setChannelId("ch1");
        req.setName("教程");

        ContentChannelTag result = tagService.createTag(req);

        assertThat(result.getName()).isEqualTo("教程");
        assertThat(result.getChannelId()).isEqualTo("ch1");
        assertThat(result.getStatus()).isEqualTo(1);
        verify(tagMapper).insert(any(ContentChannelTag.class));
    }

    @Test
    void deleteTag_shouldRejectNonExistentTag() {
        when(tagMapper.selectById("nonexistent")).thenReturn(null);

        assertThatThrownBy(() -> tagService.deleteTag("nonexistent"))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("标签不存在");
    }

    @Test
    void deleteTag_shouldSetStatusToZero() {
        ContentChannelTag existing = new ContentChannelTag();
        existing.setId("tag1");
        existing.setStatus(1);

        when(tagMapper.selectById("tag1")).thenReturn(existing);
        when(tagMapper.updateById(any(ContentChannelTag.class))).thenReturn(1);

        tagService.deleteTag("tag1");

        verify(tagMapper).updateById(argThat((ContentChannelTag t) -> t.getStatus() == 0));
    }

    @Test
    void listByChannel_shouldReturnActiveTags() {
        ContentChannelTag tag1 = new ContentChannelTag();
        tag1.setId("tag1");
        tag1.setChannelId("ch1");
        tag1.setName("教程");
        tag1.setStatus(1);

        when(tagMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(tag1));

        List<ChannelTagVO> result = tagService.listByChannel("ch1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("教程");
    }
}
