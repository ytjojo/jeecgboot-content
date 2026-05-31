package org.jeecg.modules.content.circle.biz;

import jakarta.annotation.Resource;
import org.jeecg.modules.content.circle.service.ICircleMentionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 圈子@提及业务编排服务。
 */
@Service
public class CircleMentionBizService {

    @Resource
    private ICircleMentionService circleMentionService;

    /**
     * 处理内容中的@提及：解析并异步发送通知。
     *
     * @param circleId    圈子ID
     * @param contentId   内容ID
     * @param content     内容文本
     * @param publisherId 发布者ID
     */
    public void processMentions(String circleId, String contentId,
                                String content, String publisherId) {
        List<String> mentionedUserIds = circleMentionService.parseMentions(content);
        if (mentionedUserIds.isEmpty()) {
            return;
        }
        circleMentionService.sendMentionNotifications(circleId, contentId, mentionedUserIds, publisherId);
    }
}
