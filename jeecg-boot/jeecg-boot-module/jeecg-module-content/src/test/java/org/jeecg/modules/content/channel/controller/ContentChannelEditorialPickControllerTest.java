package org.jeecg.modules.content.channel.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.entity.ContentChannelEditorialPick;
import org.jeecg.modules.content.channel.req.create.ChannelEditorialPickCreateReq;
import org.jeecg.modules.content.channel.req.update.ChannelEditorialPickUpdateReq;
import org.jeecg.modules.content.channel.service.IContentChannelEditorialPickService;
import org.jeecg.modules.content.channel.vo.ChannelEditorialPickVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 频道编辑精选控制器测试
 */
@ExtendWith(MockitoExtension.class)
class ContentChannelEditorialPickControllerTest {

    @Mock
    private IContentChannelEditorialPickService editorialPickService;

    @InjectMocks
    private ContentChannelEditorialPickController controller;

    @Test
    void should_list_active_picks() {
        when(editorialPickService.listActivePicks()).thenReturn(List.of());

        Result<List<ChannelEditorialPickVO>> result = controller.listActivePicks();

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void should_create_pick() {
        ChannelEditorialPickCreateReq req = new ChannelEditorialPickCreateReq();
        ContentChannelEditorialPick created = new ContentChannelEditorialPick();
        when(editorialPickService.createPick(req)).thenReturn(created);

        Result<ContentChannelEditorialPick> result = controller.createPick(req);

        assertThat(result.isSuccess()).isTrue();
        verify(editorialPickService).createPick(req);
    }

    @Test
    void should_update_pick() {
        ChannelEditorialPickUpdateReq req = new ChannelEditorialPickUpdateReq();

        Result<Void> result = controller.updatePick(req);

        assertThat(result.isSuccess()).isTrue();
        verify(editorialPickService).updatePick(req);
    }

    @Test
    void should_remove_pick() {
        Result<Void> result = controller.removePick("p1");

        assertThat(result.isSuccess()).isTrue();
        verify(editorialPickService).removePick("p1");
    }
}
