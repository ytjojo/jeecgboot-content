package org.jeecg.modules.content.channel.biz.impl;

import org.jeecg.modules.content.channel.biz.ChannelPublishBiz;
import org.jeecg.modules.content.channel.biz.ScheduledPublishDispatchBiz;
import org.jeecg.modules.content.channel.entity.ChannelScheduledPublish;
import org.jeecg.modules.content.channel.req.publish.ChannelPublishReq;
import org.jeecg.modules.content.channel.service.ChannelScheduledPublishService;
import org.jeecg.modules.content.channel.service.ChannelContentGovernanceLogService;
import org.jeecg.modules.content.channel.vo.publish.ChannelPublishResultVO;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Service
public class ScheduledPublishDispatchBizImpl implements ScheduledPublishDispatchBiz {

    private static final Logger log = LoggerFactory.getLogger(ScheduledPublishDispatchBizImpl.class);

    @Resource
    private ChannelScheduledPublishService scheduledPublishService;

    @Resource
    private ChannelPublishBiz publishBiz;

    @Resource
    private ChannelContentGovernanceLogService governanceLogService;

    @Override
    public void dispatch() {
        List<ChannelScheduledPublish> dueTasks = scheduledPublishService.findDueTasks();
        if (dueTasks.isEmpty()) {
            return;
        }
        for (ChannelScheduledPublish task : dueTasks) {
            processTask(task);
        }
    }

    private void processTask(ChannelScheduledPublish task) {
        try {
            if (task.getPublisherId() == null || task.getPublisherId().isEmpty()) {
                scheduledPublishService.markFailed(task.getId(), "发布者信息缺失");
                return;
            }

            ChannelPublishReq req = new ChannelPublishReq();
            req.setContentId(task.getContentId());
            req.setContentType(task.getContentType());
            req.setChannelIds(Collections.singletonList(task.getChannelId()));

            List<ChannelPublishResultVO> results = publishBiz.publish(req, task.getPublisherId());
            if (!results.isEmpty()) {
                ChannelPublishResultVO result = results.get(0);
                if ("PUBLISHED".equals(result.getStatus()) || "PENDING".equals(result.getStatus())) {
                    scheduledPublishService.markPublished(task.getId());
                    governanceLogService.log(task.getChannelId(), task.getContentId(), task.getPublisherId(),
                            "SCHEDULED_PUBLISH", "status=" + result.getStatus(), null, "SUCCESS");
                } else {
                    scheduledPublishService.markFailed(task.getId(), result.getFailReason());
                    governanceLogService.log(task.getChannelId(), task.getContentId(), task.getPublisherId(),
                            "SCHEDULED_PUBLISH", null, result.getFailReason(), "FAILED");
                }
            }
        } catch (Exception e) {
            log.error("定时发布任务执行失败, taskId={}", task.getId(), e);
            scheduledPublishService.markFailed(task.getId(), "系统异常");
        }
    }
}
