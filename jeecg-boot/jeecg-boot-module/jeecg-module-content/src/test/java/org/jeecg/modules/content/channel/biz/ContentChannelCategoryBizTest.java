package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.mapper.ChannelMapper;
import org.jeecg.modules.content.channel.service.IContentChannelCategoryService;
import org.jeecg.modules.content.channel.vo.ChannelCategoryTreeVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentChannelCategoryBizTest {

    @Mock
    private IContentChannelCategoryService categoryService;

    @Mock
    private ChannelMapper channelMapper;

    @InjectMocks
    private ContentChannelCategoryBiz categoryBiz;

    @Test
    void getCategoryTree_shouldDelegateToService() {
        ChannelCategoryTreeVO root = new ChannelCategoryTreeVO();
        root.setId("cat1");
        root.setName("科技");
        root.setLevel(1);
        root.setChildren(Arrays.asList());

        when(categoryService.getCategoryTree()).thenReturn(Arrays.asList(root));

        List<ChannelCategoryTreeVO> result = categoryBiz.getCategoryTree();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("科技");
    }

    @Test
    void assessDisableImpact_shouldReturnChannelCount() {
        when(categoryService.getById("cat1")).thenReturn(new org.jeecg.modules.content.channel.entity.ContentChannelCategory());
        when(channelMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        long count = categoryBiz.assessDisableImpact("cat1");

        assertThat(count).isEqualTo(5);
    }
}
