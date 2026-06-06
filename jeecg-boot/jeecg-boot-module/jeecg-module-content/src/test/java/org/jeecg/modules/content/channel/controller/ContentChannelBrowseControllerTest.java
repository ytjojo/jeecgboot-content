package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.req.query.ChannelBrowseQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelBrowseService;
import org.jeecg.modules.content.channel.vo.ChannelBrowseItemVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentChannelBrowseControllerTest {

    @Mock
    private IContentChannelBrowseService browseService;

    @InjectMocks
    private ContentChannelBrowseController browseController;

    @Test
    void browseByCategory_shouldReturnOkResult() {
        // mock 服务返回
        Page<ChannelBrowseItemVO> page = new Page<>(1, 20, 0);
        page.setRecords(Collections.emptyList());
        when(browseService.browseByCategory(any(ChannelBrowseQueryReq.class))).thenReturn(page);

        ChannelBrowseQueryReq req = new ChannelBrowseQueryReq();
        req.setCategoryId("cat1");
        req.setPageNo(1);
        req.setPageSize(20);

        Result<IPage<ChannelBrowseItemVO>> result = browseController.browseByCategory(req);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void browseByCategory_shouldAcceptNullCategoryId() {
        // mock 服务返回
        Page<ChannelBrowseItemVO> page = new Page<>(1, 20, 0);
        page.setRecords(Collections.emptyList());
        when(browseService.browseByCategory(any(ChannelBrowseQueryReq.class))).thenReturn(page);

        ChannelBrowseQueryReq req = new ChannelBrowseQueryReq();

        Result<IPage<ChannelBrowseItemVO>> result = browseController.browseByCategory(req);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }
}
