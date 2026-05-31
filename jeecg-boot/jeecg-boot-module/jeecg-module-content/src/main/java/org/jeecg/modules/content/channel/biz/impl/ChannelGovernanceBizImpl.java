package org.jeecg.modules.content.channel.biz.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.biz.ChannelGovernanceBiz;
import org.jeecg.modules.content.channel.entity.ChannelContentPublish;
import org.jeecg.modules.content.channel.entity.ChannelRecycleBin;
import org.jeecg.modules.content.channel.mapper.ChannelContentPublishMapper;
import org.jeecg.modules.content.channel.req.governance.ChannelGovernanceReq;
import org.jeecg.modules.content.channel.service.ChannelContentGovernanceLogService;
import org.jeecg.modules.content.channel.service.ChannelRecycleBinService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChannelGovernanceBizImpl implements ChannelGovernanceBiz {

    @Resource
    private ChannelContentPublishMapper publishMapper;

    @Resource
    private ChannelRecycleBinService recycleBinService;

    @Resource
    private ChannelContentGovernanceLogService governanceLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeGovernance(ChannelGovernanceReq req, String operatorId) {
        String action = req.getAction();
        try {
            switch (action) {
                case "PIN":
                    handlePin(req, true);
                    break;
                case "UNPIN":
                    handlePin(req, false);
                    break;
                case "FEATURE":
                    handleFeature(req, true);
                    break;
                case "UNFEATURE":
                    handleFeature(req, false);
                    break;
                case "DELETE":
                    handleDelete(req, operatorId);
                    break;
                case "RESTORE":
                    handleRestore(req, operatorId);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的操作类型: " + action);
            }
            governanceLogService.log(req.getChannelId(), req.getContentId(), operatorId, action, null, req.getReason(), "SUCCESS");
        } catch (Exception e) {
            governanceLogService.log(req.getChannelId(), req.getContentId(), operatorId, action, null, req.getReason(), "FAILED");
            throw e;
        }
    }

    private void handlePin(ChannelGovernanceReq req, boolean pinned) {
        ChannelContentPublish publish = getPublishRecord(req);
        publish.setIsPinned(pinned);
        publishMapper.updateById(publish);
    }

    private void handleFeature(ChannelGovernanceReq req, boolean featured) {
        ChannelContentPublish publish = getPublishRecord(req);
        publish.setIsFeatured(featured);
        publishMapper.updateById(publish);
    }

    private void handleDelete(ChannelGovernanceReq req, String operatorId) {
        ChannelContentPublish publish = getPublishRecord(req);
        recycleBinService.addToRecycleBin(req.getChannelId(), req.getContentId(), publish.getContentType(), publish.getPublisherId(), operatorId, req.getReason());
        publish.setPublishStatus("RECYCLED");
        publishMapper.updateById(publish);
    }

    private void handleRestore(ChannelGovernanceReq req, String operatorId) {
        LambdaQueryWrapper<ChannelRecycleBin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChannelRecycleBin::getChannelId, req.getChannelId())
               .eq(ChannelRecycleBin::getContentId, req.getContentId())
               .eq(ChannelRecycleBin::getIsRestored, false);
        // TODO: 注入 ChannelRecycleBinMapper 或在 ChannelRecycleBinService 增加 findByChannelAndContent 方法
        // 暂时通过 publish 记录恢复状态
        ChannelContentPublish publish = getPublishRecord(req);
        publish.setPublishStatus("PUBLISHED");
        publishMapper.updateById(publish);
    }

    private ChannelContentPublish getPublishRecord(ChannelGovernanceReq req) {
        LambdaQueryWrapper<ChannelContentPublish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChannelContentPublish::getChannelId, req.getChannelId())
               .eq(ChannelContentPublish::getContentId, req.getContentId());
        ChannelContentPublish publish = publishMapper.selectOne(wrapper);
        if (publish == null) {
            throw new IllegalArgumentException("发布记录不存在: channelId=" + req.getChannelId() + ", contentId=" + req.getContentId());
        }
        return publish;
    }
}
