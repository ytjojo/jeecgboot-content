package org.jeecg.modules.content.channel.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.req.query.ChannelBrowseQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelVisibilityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ContentChannelBrowseControllerTest {

    @Mock
    private IContentChannelVisibilityService visibilityService;

    @InjectMocks
    private ContentChannelBrowseController browseController;

    @SuppressWarnings("unchecked")
    @Test
    void browseByCategory_shouldReturnOkResult() {
        ChannelBrowseQueryReq req = new ChannelBrowseQueryReq();
        req.setCategoryId("cat1");
        req.setPageNo(1);
        req.setPageSize(20);

        Result<?> result = (Result<?>) browseController.browseByCategory(req);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    void browseByCategory_shouldAcceptNullCategoryId() {
        ChannelBrowseQueryReq req = new ChannelBrowseQueryReq();

        Result<?> result = (Result<?>) browseController.browseByCategory(req);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }
}
