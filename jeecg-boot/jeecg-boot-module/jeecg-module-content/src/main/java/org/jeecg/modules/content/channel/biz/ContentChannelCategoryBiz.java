package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ContentChannelCategory;
import org.jeecg.modules.content.channel.mapper.ChannelMapper;
import org.jeecg.modules.content.channel.service.IContentChannelCategoryService;
import org.jeecg.modules.content.channel.vo.ChannelCategoryTreeVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentChannelCategoryBiz {

    @Resource
    private IContentChannelCategoryService categoryService;

    @Resource
    private ChannelMapper channelMapper;

    /**
     * 获取分类树
     */
    public List<ChannelCategoryTreeVO> getCategoryTree() {
        return categoryService.getCategoryTree();
    }

    /**
     * 评估停用分类的影响：返回该分类下的频道数量
     */
    public long assessDisableImpact(String categoryId) {
        ContentChannelCategory category = categoryService.getById(categoryId);
        if (category == null) {
            throw new JeecgBootException("分类不存在");
        }
        return channelMapper.selectCount(
                Wrappers.<Channel>lambdaQuery().eq(Channel::getCategoryId, categoryId));
    }

    /**
     * 将频道从一个分类迁移到另一个分类
     */
    public void migrateChannels(String fromCategoryId, String toCategoryId) {
        ContentChannelCategory from = categoryService.getById(fromCategoryId);
        if (from == null) {
            throw new JeecgBootException("源分类不存在");
        }
        if (toCategoryId != null) {
            ContentChannelCategory to = categoryService.getById(toCategoryId);
            if (to == null) {
                throw new JeecgBootException("目标分类不存在");
            }
        }
        Channel update = new Channel();
        update.setCategoryId(toCategoryId);
        channelMapper.update(update, Wrappers.<Channel>lambdaUpdate()
                .eq(Channel::getCategoryId, fromCategoryId));
    }
}
