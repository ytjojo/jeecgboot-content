package org.jeecg.modules.content.channel.biz.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.biz.ChannelGovernanceBiz;
import org.jeecg.modules.content.channel.entity.ChannelContentPublish;
import org.jeecg.modules.content.channel.entity.ChannelRecycleBin;
import org.jeecg.modules.content.channel.enums.ContentGovernanceAction;
import org.jeecg.modules.content.channel.enums.PublishStatusEnum;
import org.jeecg.modules.content.channel.mapper.ChannelContentPublishMapper;
import org.jeecg.modules.content.channel.req.governance.ChannelGovernanceReq;
import org.jeecg.modules.content.channel.service.ChannelContentGovernanceLogService;
import org.jeecg.modules.content.channel.service.ChannelEditAssistService;
import org.jeecg.modules.content.channel.service.ChannelRecycleBinService;
import org.jeecg.common.exception.JeecgBootException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Service
public class ChannelGovernanceBizImpl implements ChannelGovernanceBiz {

    @Resource
    private ChannelContentPublishMapper publishMapper;

    @Resource
    private ChannelRecycleBinService recycleBinService;

    @Resource
    private ChannelContentGovernanceLogService governanceLogService;

    @Resource
    private ChannelEditAssistService editAssistService;

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
                case "MOVE":
                    handleMove(req, operatorId);
                    break;
                case "EDIT_ASSIST":
                    handleEditAssist(req, operatorId);
                    break;
                default:
                    throw new JeecgBootException("不支持的操作类型: " + action);
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
        publish.setPublishStatus(PublishStatusEnum.RECYCLED.getCode());
        publishMapper.updateById(publish);
    }

    private void handleRestore(ChannelGovernanceReq req, String operatorId) {
        ChannelContentPublish publish = getPublishRecord(req);
        publish.setPublishStatus(PublishStatusEnum.PUBLISHED.getCode());
        publishMapper.updateById(publish);
    }

    private void handleMove(ChannelGovernanceReq req, String operatorId) {
        if (req.getTargetChannelId() == null || req.getTargetChannelId().isEmpty()) {
            throw new JeecgBootException("移出频道时目标频道ID不能为空");
        }
        ChannelContentPublish publish = getPublishRecord(req);
        // 检查目标频道是否已存在该内容
        Long existCount = publishMapper.selectCount(new LambdaQueryWrapper<ChannelContentPublish>()
                .eq(ChannelContentPublish::getChannelId, req.getTargetChannelId())
                .eq(ChannelContentPublish::getContentId, publish.getContentId()));
        if (existCount > 0) {
            throw new JeecgBootException("目标频道已存在该内容");
        }
        // 在目标频道创建新发布记录
        ChannelContentPublish newPublish = new ChannelContentPublish();
        newPublish.setChannelId(req.getTargetChannelId());
        newPublish.setContentId(publish.getContentId());
        newPublish.setContentType(publish.getContentType());
        newPublish.setPublisherId(publish.getPublisherId());
        newPublish.setPublishStatus(PublishStatusEnum.PUBLISHED.getCode());
        newPublish.setSourceType(ContentGovernanceAction.MOVE.getCode());
        publishMapper.insert(newPublish);
        // 标记原频道记录为已回收
        publish.setPublishStatus(PublishStatusEnum.RECYCLED.getCode());
        publishMapper.updateById(publish);
    }

    private void handleEditAssist(ChannelGovernanceReq req, String operatorId) {
        Map<String, String> editFields = req.getEditFields();
        if (editFields == null || editFields.isEmpty()) {
            throw new JeecgBootException("编辑字段不能为空");
        }
        for (Map.Entry<String, String> entry : editFields.entrySet()) {
            editAssistService.recordEdit(req.getChannelId(), req.getContentId(), operatorId,
                    entry.getKey(), null, entry.getValue());
        }
    }

    private ChannelContentPublish getPublishRecord(ChannelGovernanceReq req) {
        LambdaQueryWrapper<ChannelContentPublish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChannelContentPublish::getChannelId, req.getChannelId())
               .eq(ChannelContentPublish::getContentId, req.getContentId());
        ChannelContentPublish publish = publishMapper.selectOne(wrapper);
        if (publish == null) {
            throw new JeecgBootException("发布记录不存在: channelId=" + req.getChannelId() + ", contentId=" + req.getContentId());
        }
        return publish;
    }
}
