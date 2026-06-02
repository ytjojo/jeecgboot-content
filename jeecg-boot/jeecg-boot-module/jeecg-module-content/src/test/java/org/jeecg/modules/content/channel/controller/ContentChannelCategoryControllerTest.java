package org.jeecg.modules.content.channel.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.entity.ContentChannelCategory;
import org.jeecg.modules.content.channel.req.create.ChannelCategoryCreateReq;
import org.jeecg.modules.content.channel.req.update.ChannelCategoryUpdateReq;
import org.jeecg.modules.content.channel.service.IContentChannelCategoryService;
import org.jeecg.modules.content.channel.vo.ChannelCategoryTreeVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 频道分类控制器测试
 */
@ExtendWith(MockitoExtension.class)
class ContentChannelCategoryControllerTest {

    @Mock
    private IContentChannelCategoryService categoryService;

    @InjectMocks
    private ContentChannelCategoryController controller;

    @Test
    void should_get_category_tree() {
        when(categoryService.getCategoryTree()).thenReturn(Collections.emptyList());

        Result<List<ChannelCategoryTreeVO>> result = controller.getCategoryTree();

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isEmpty();
    }

    @Test
    void should_create_category() {
        ChannelCategoryCreateReq req = new ChannelCategoryCreateReq();
        ContentChannelCategory created = new ContentChannelCategory();
        when(categoryService.createCategory(req)).thenReturn(created);

        Result<ContentChannelCategory> result = controller.createCategory(req);

        assertThat(result.isSuccess()).isTrue();
        verify(categoryService).createCategory(req);
    }

    @Test
    void should_update_category() {
        ChannelCategoryUpdateReq req = new ChannelCategoryUpdateReq();

        Result<Void> result = controller.updateCategory(req);

        assertThat(result.isSuccess()).isTrue();
        verify(categoryService).updateCategory(req);
    }

    @Test
    void should_disable_category() {
        Result<Void> result = controller.disableCategory("c1");

        assertThat(result.isSuccess()).isTrue();
        verify(categoryService).disableCategory("c1");
    }
}
