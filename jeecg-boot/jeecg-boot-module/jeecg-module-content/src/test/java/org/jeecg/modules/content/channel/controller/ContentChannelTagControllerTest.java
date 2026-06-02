package org.jeecg.modules.content.channel.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.entity.ContentChannelTag;
import org.jeecg.modules.content.channel.req.create.ChannelTagCreateReq;
import org.jeecg.modules.content.channel.service.IContentChannelTagService;
import org.jeecg.modules.content.channel.vo.ChannelTagVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 频道标签控制器测试
 */
@ExtendWith(MockitoExtension.class)
class ContentChannelTagControllerTest {

    @Mock
    private IContentChannelTagService tagService;

    @InjectMocks
    private ContentChannelTagController controller;

    @Test
    void should_list_tags_by_channel() {
        when(tagService.listByChannel("ch1")).thenReturn(List.of());

        Result<List<ChannelTagVO>> result = controller.listByChannel("ch1");

        assertThat(result.isSuccess()).isTrue();
        verify(tagService).listByChannel("ch1");
    }

    @Test
    void should_create_tag() {
        ChannelTagCreateReq req = new ChannelTagCreateReq();
        ContentChannelTag created = new ContentChannelTag();
        when(tagService.createTag(req)).thenReturn(created);

        Result<ContentChannelTag> result = controller.createTag(req);

        assertThat(result.isSuccess()).isTrue();
        verify(tagService).createTag(req);
    }

    @Test
    void should_delete_tag() {
        Result<Void> result = controller.deleteTag("t1");

        assertThat(result.isSuccess()).isTrue();
        verify(tagService).deleteTag("t1");
    }
}
