package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ContentChannelCategory;
import org.jeecg.modules.content.channel.mapper.ContentChannelCategoryMapper;
import org.jeecg.modules.content.channel.req.create.ChannelCategoryCreateReq;
import org.jeecg.modules.content.channel.req.update.ChannelCategoryUpdateReq;
import org.jeecg.modules.content.channel.service.IContentChannelCategoryService;
import org.jeecg.modules.content.channel.vo.ChannelCategoryTreeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContentChannelCategoryServiceImpl
        extends ServiceImpl<ContentChannelCategoryMapper, ContentChannelCategory>
        implements IContentChannelCategoryService {

    private static final int MAX_LEVEL = 4;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentChannelCategory createCategory(ChannelCategoryCreateReq req) {
        validateName(req.getName(), null);

        int level = 1;
        String path = "/";
        if (req.getParentId() != null) {
            ContentChannelCategory parent = getById(req.getParentId());
            if (parent == null) {
                throw new JeecgBootException("父级分类不存在");
            }
            level = parent.getLevel() + 1;
            if (level > MAX_LEVEL) {
                throw new JeecgBootException("分类层级不能超过" + MAX_LEVEL + "级");
            }
            path = parent.getPath() + parent.getId() + "/";
        }

        long count = count(Wrappers.<ContentChannelCategory>lambdaQuery()
                .eq(ContentChannelCategory::getParentId, req.getParentId())
                .eq(ContentChannelCategory::getName, req.getName())
                .eq(ContentChannelCategory::getStatus, 1));
        if (count > 0) {
            throw new JeecgBootException("同级分类名称已存在");
        }

        ContentChannelCategory category = new ContentChannelCategory();
        category.setName(req.getName());
        category.setParentId(req.getParentId());
        category.setLevel(level);
        category.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        category.setStatus(1);
        category.setIsSystem(req.getIsSystem() != null ? req.getIsSystem() : 0);
        save(category);

        category.setPath(path + category.getId() + "/");
        updateById(category);

        return category;
    }

    @Override
    public void updateCategory(ChannelCategoryUpdateReq req) {
        ContentChannelCategory category = getById(req.getId());
        if (category == null) {
            throw new JeecgBootException("分类不存在");
        }
        if (req.getName() != null) {
            validateName(req.getName(), req.getId());
            // 检查同级分类名称是否重复（排除自身）
            long count = count(Wrappers.<ContentChannelCategory>lambdaQuery()
                    .eq(ContentChannelCategory::getParentId, category.getParentId())
                    .eq(ContentChannelCategory::getName, req.getName())
                    .eq(ContentChannelCategory::getStatus, 1)
                    .ne(ContentChannelCategory::getId, req.getId()));
            if (count > 0) {
                throw new JeecgBootException("同级分类名称已存在");
            }
            category.setName(req.getName());
        }
        if (req.getSortOrder() != null) {
            category.setSortOrder(req.getSortOrder());
        }
        updateById(category);
    }

    @Override
    public List<ChannelCategoryTreeVO> getCategoryTree() {
        List<ContentChannelCategory> all = list(Wrappers.<ContentChannelCategory>lambdaQuery()
                .eq(ContentChannelCategory::getStatus, 1)
                .orderByAsc(ContentChannelCategory::getSortOrder));

        Map<String, List<ContentChannelCategory>> grouped = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(ContentChannelCategory::getParentId));

        return all.stream()
                .filter(c -> c.getParentId() == null)
                .map(c -> toTreeVO(c, grouped))
                .collect(Collectors.toList());
    }

    @Override
    public void disableCategory(String categoryId) {
        ContentChannelCategory category = getById(categoryId);
        if (category == null) {
            throw new JeecgBootException("分类不存在");
        }
        category.setStatus(0);
        updateById(category);
    }

    private void validateName(String name, String excludeId) {
        if (name == null || name.isBlank()) {
            throw new JeecgBootException("分类名称不能为空");
        }
        if (name.length() > 50) {
            throw new JeecgBootException("分类名称不能超过50个字符");
        }
    }

    private ChannelCategoryTreeVO toTreeVO(ContentChannelCategory category,
                                            Map<String, List<ContentChannelCategory>> grouped) {
        ChannelCategoryTreeVO vo = new ChannelCategoryTreeVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setLevel(category.getLevel());
        vo.setSortOrder(category.getSortOrder());
        vo.setIsSystem(category.getIsSystem());

        List<ContentChannelCategory> children = grouped.getOrDefault(category.getId(), new ArrayList<>());
        vo.setChildren(children.stream()
                .map(c -> toTreeVO(c, grouped))
                .collect(Collectors.toList()));
        return vo;
    }
}
