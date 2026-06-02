package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ContentChannelCategory;
import org.jeecg.modules.content.channel.mapper.ContentChannelCategoryMapper;
import org.jeecg.modules.content.channel.req.create.ChannelCategoryCreateReq;
import org.jeecg.modules.content.channel.req.update.ChannelCategoryUpdateReq;
import org.jeecg.modules.content.channel.service.impl.ContentChannelCategoryServiceImpl;
import org.jeecg.modules.content.channel.vo.ChannelCategoryTreeVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentChannelCategoryServiceTest {

    @Mock
    private ContentChannelCategoryMapper categoryMapper;

    @InjectMocks
    private ContentChannelCategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(categoryService, "baseMapper", categoryMapper);
    }

    @Test
    void createCategory_shouldRejectEmptyName() {
        ChannelCategoryCreateReq req = new ChannelCategoryCreateReq();
        req.setName("");

        assertThatThrownBy(() -> categoryService.createCategory(req))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("分类名称不能为空");
    }

    @Test
    void createCategory_shouldRejectNameExceeding50Chars() {
        ChannelCategoryCreateReq req = new ChannelCategoryCreateReq();
        req.setName("a".repeat(51));

        assertThatThrownBy(() -> categoryService.createCategory(req))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("分类名称不能超过50个字符");
    }

    @Test
    void createCategory_shouldRejectLevelExceeding4() {
        // 模拟父级分类存在且level=4
        ContentChannelCategory parent = new ContentChannelCategory();
        parent.setId("parent1");
        parent.setLevel(4);
        parent.setPath("/root/");

        when(categoryMapper.selectById("parent1")).thenReturn(parent);

        ChannelCategoryCreateReq req = new ChannelCategoryCreateReq();
        req.setName("L5");
        req.setParentId("parent1");

        assertThatThrownBy(() -> categoryService.createCategory(req))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("分类层级不能超过4级");
    }

    @Test
    void createCategory_shouldRejectDuplicateNameAtSameLevel() {
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        ChannelCategoryCreateReq req = new ChannelCategoryCreateReq();
        req.setName("科技");
        req.setParentId(null);

        assertThatThrownBy(() -> categoryService.createCategory(req))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("同级分类名称已存在");
    }

    @Test
    void createCategory_shouldCreateRootCategory() {
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(categoryMapper.insert(any(ContentChannelCategory.class))).thenReturn(1);

        ChannelCategoryCreateReq req = new ChannelCategoryCreateReq();
        req.setName("科技");

        ContentChannelCategory result = categoryService.createCategory(req);

        assertThat(result.getName()).isEqualTo("科技");
        assertThat(result.getLevel()).isEqualTo(1);
        verify(categoryMapper).insert(any(ContentChannelCategory.class));
    }

    @Test
    void createCategory_shouldCreateChildCategory() {
        ContentChannelCategory parent = new ContentChannelCategory();
        parent.setId("parent1");
        parent.setLevel(1);
        parent.setPath("/parent1/");

        when(categoryMapper.selectById("parent1")).thenReturn(parent);
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(categoryMapper.insert(any(ContentChannelCategory.class))).thenReturn(1);

        ChannelCategoryCreateReq req = new ChannelCategoryCreateReq();
        req.setName("人工智能");
        req.setParentId("parent1");

        ContentChannelCategory result = categoryService.createCategory(req);

        assertThat(result.getLevel()).isEqualTo(2);
        assertThat(result.getParentId()).isEqualTo("parent1");
    }

    @Test
    void updateCategory_shouldRejectNonExistentCategory() {
        when(categoryMapper.selectById("nonexistent")).thenReturn(null);

        ChannelCategoryUpdateReq req = new ChannelCategoryUpdateReq();
        req.setId("nonexistent");
        req.setName("新名称");

        assertThatThrownBy(() -> categoryService.updateCategory(req))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("分类不存在");
    }

    @Test
    void updateCategory_shouldUpdateName() {
        ContentChannelCategory existing = new ContentChannelCategory();
        existing.setId("cat1");
        existing.setName("旧名称");
        existing.setParentId(null);

        when(categoryMapper.selectById("cat1")).thenReturn(existing);
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(categoryMapper.updateById(any(ContentChannelCategory.class))).thenReturn(1);

        ChannelCategoryUpdateReq req = new ChannelCategoryUpdateReq();
        req.setId("cat1");
        req.setName("新名称");

        categoryService.updateCategory(req);

        verify(categoryMapper).updateById(argThat((ContentChannelCategory c) -> c.getName().equals("新名称")));
    }

    @Test
    void updateCategory_shouldRejectDuplicateNameAtSameLevel() {
        ContentChannelCategory existing = new ContentChannelCategory();
        existing.setId("cat1");
        existing.setName("旧名称");
        existing.setParentId(null);

        when(categoryMapper.selectById("cat1")).thenReturn(existing);
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        ChannelCategoryUpdateReq req = new ChannelCategoryUpdateReq();
        req.setId("cat1");
        req.setName("已存在的名称");

        assertThatThrownBy(() -> categoryService.updateCategory(req))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("同级分类名称已存在");
    }

    @Test
    void disableCategory_shouldRejectNonExistentCategory() {
        when(categoryMapper.selectById("nonexistent")).thenReturn(null);

        assertThatThrownBy(() -> categoryService.disableCategory("nonexistent"))
                .isInstanceOf(JeecgBootException.class)
                .hasMessageContaining("分类不存在");
    }

    @Test
    void disableCategory_shouldSetStatusToZero() {
        ContentChannelCategory existing = new ContentChannelCategory();
        existing.setId("cat1");
        existing.setStatus(1);

        when(categoryMapper.selectById("cat1")).thenReturn(existing);
        when(categoryMapper.updateById(any(ContentChannelCategory.class))).thenReturn(1);

        categoryService.disableCategory("cat1");

        verify(categoryMapper).updateById(argThat((ContentChannelCategory c) -> c.getStatus() == 0));
    }
}
