package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ContentChannelCategory;
import org.jeecg.modules.content.channel.req.create.ChannelCategoryCreateReq;
import org.jeecg.modules.content.channel.req.update.ChannelCategoryUpdateReq;
import org.jeecg.modules.content.channel.vo.ChannelCategoryTreeVO;

import java.util.List;

public interface IContentChannelCategoryService extends IService<ContentChannelCategory> {

    /**
     * 创建分类
     */
    ContentChannelCategory createCategory(ChannelCategoryCreateReq req);

    /**
     * 更新分类
     */
    void updateCategory(ChannelCategoryUpdateReq req);

    /**
     * 获取分类树
     */
    List<ChannelCategoryTreeVO> getCategoryTree();

    /**
     * 停用分类（需检查关联频道）
     */
    void disableCategory(String categoryId);

    /**
     * 启用分类
     */
    void enableCategory(String categoryId);
}
