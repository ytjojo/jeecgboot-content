package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ContentChannelTag;
import org.jeecg.modules.content.channel.mapper.ContentChannelTagMapper;
import org.jeecg.modules.content.channel.req.create.ChannelTagCreateReq;
import org.jeecg.modules.content.channel.service.IContentChannelTagService;
import org.jeecg.modules.content.channel.vo.ChannelTagVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentChannelTagServiceImpl
        extends ServiceImpl<ContentChannelTagMapper, ContentChannelTag>
        implements IContentChannelTagService {

    @Override
    public ContentChannelTag createTag(ChannelTagCreateReq req) {
        validateName(req.getName(), req.getChannelId());

        // 检查是否存在同名标签（包括已软删除的）
        ContentChannelTag existing = getOne(Wrappers.<ContentChannelTag>lambdaQuery()
                .eq(ContentChannelTag::getChannelId, req.getChannelId())
                .eq(ContentChannelTag::getName, req.getName()));

        if (existing != null) {
            if (existing.getStatus() == 1) {
                throw new JeecgBootException("该标签已存在");
            }
            // 软删除的标签被重新创建时，直接激活
            existing.setStatus(1);
            updateById(existing);
            return existing;
        }

        ContentChannelTag tag = new ContentChannelTag();
        tag.setChannelId(req.getChannelId());
        tag.setName(req.getName());
        tag.setStatus(1);
        save(tag);
        return tag;
    }

    @Override
    public void deleteTag(String tagId) {
        ContentChannelTag tag = getById(tagId);
        if (tag == null) {
            throw new JeecgBootException("标签不存在");
        }
        tag.setStatus(0);
        updateById(tag);
    }

    @Override
    public List<ChannelTagVO> listByChannel(String channelId) {
        return list(Wrappers.<ContentChannelTag>lambdaQuery()
                .eq(ContentChannelTag::getChannelId, channelId)
                .eq(ContentChannelTag::getStatus, 1))
                .stream()
                .map(t -> {
                    ChannelTagVO vo = new ChannelTagVO();
                    vo.setId(t.getId());
                    vo.setName(t.getName());
                    vo.setChannelId(t.getChannelId());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private void validateName(String name, String channelId) {
        if (name == null || name.isBlank()) {
            throw new JeecgBootException("标签名称不能为空");
        }
        if (name.length() > 20) {
            throw new JeecgBootException("标签名称不能超过20个字符");
        }
    }
}
