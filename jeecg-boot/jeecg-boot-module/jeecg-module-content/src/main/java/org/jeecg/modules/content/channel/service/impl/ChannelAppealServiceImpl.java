package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ChannelAppeal;
import org.jeecg.modules.content.channel.mapper.ChannelAppealMapper;
import org.jeecg.modules.content.channel.service.IChannelAppealService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ChannelAppealServiceImpl extends ServiceImpl<ChannelAppealMapper, ChannelAppeal>
    implements IChannelAppealService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChannelAppeal submitAppeal(String channelId, String lifecycleLogId, String applicantId,
                                      String appealReason, String attachmentUrls) {
        ChannelAppeal appeal = new ChannelAppeal()
                .setAppealId(UUID.randomUUID().toString().replace("-", ""))
                .setChannelId(channelId)
                .setLifecycleLogId(lifecycleLogId)
                .setApplicantId(applicantId)
                .setAppealReason(appealReason)
                .setAttachmentUrls(attachmentUrls)
                .setStatus("pending")
                .setCreatedTime(LocalDateTime.now())
                .setUpdatedTime(LocalDateTime.now());
        save(appeal);
        return appeal;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChannelAppeal handleAppeal(String appealId, String handlerId, String action, String handleResult) {
        ChannelAppeal appeal = lambdaQuery().eq(ChannelAppeal::getAppealId, appealId).one();
        if (appeal == null) {
            throw new IllegalArgumentException("申诉记录不存在: " + appealId);
        }
        if (!"pending".equals(appeal.getStatus()) && !"processing".equals(appeal.getStatus())) {
            throw new IllegalStateException("申诉已处理，不可重复操作");
        }

        LocalDateTime now = LocalDateTime.now();
        appeal.setStatus(action);
        appeal.setHandlerId(handlerId);
        appeal.setHandleResult(handleResult);
        appeal.setHandleTime(now);
        appeal.setUpdatedTime(now);
        if (appeal.getFirstResponseTime() == null) {
            appeal.setFirstResponseTime(now);
        }
        updateById(appeal);
        return appeal;
    }
}
