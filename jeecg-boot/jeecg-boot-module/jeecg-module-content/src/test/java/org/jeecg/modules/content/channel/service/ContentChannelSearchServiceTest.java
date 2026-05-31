package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.content.channel.req.query.ChannelSearchQueryReq;
import org.jeecg.modules.content.channel.service.impl.ContentChannelSearchServiceImpl;
import org.jeecg.modules.content.channel.vo.ChannelSearchResultVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ContentChannelSearchServiceTest {

    @Mock
    private IContentChannelVisibilityService visibilityService;

    @InjectMocks
    private ContentChannelSearchServiceImpl searchService;

    @Test
    void search_shouldReturnEmptyPageWhenNoResults() {
        ChannelSearchQueryReq req = new ChannelSearchQueryReq();
        req.setKeyword("不存在的频道名称");
        req.setPageNo(1);
        req.setPageSize(20);

        IPage<ChannelSearchResultVO> result = searchService.search("test-user", req);

        assertThat(result).isNotNull();
        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    void search_shouldReturnPageWithDefaultPagination() {
        ChannelSearchQueryReq req = new ChannelSearchQueryReq();
        req.setKeyword("测试");

        IPage<ChannelSearchResultVO> result = searchService.search("test-user", req);

        assertThat(result).isNotNull();
        assertThat(result.getCurrent()).isEqualTo(1);
    }
}
