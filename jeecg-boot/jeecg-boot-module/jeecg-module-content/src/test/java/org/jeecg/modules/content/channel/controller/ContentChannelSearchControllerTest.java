package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.req.query.ChannelSearchQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelSearchService;
import org.jeecg.modules.content.channel.vo.ChannelSearchResultVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 频道搜索控制器测试
 */
@ExtendWith(MockitoExtension.class)
class ContentChannelSearchControllerTest {

    @Mock
    private IContentChannelSearchService searchService;

    @InjectMocks
    private ContentChannelSearchController controller;

    @Test
    void should_search_channels() {
        ChannelSearchQueryReq req = new ChannelSearchQueryReq();
        when(searchService.search("user1", req)).thenReturn(null);

        Result<IPage<ChannelSearchResultVO>> result = controller.search("user1", req);

        assertThat(result.isSuccess()).isTrue();
        verify(searchService).search("user1", req);
    }
}
