package org.jeecg.modules.content.circle.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.circle.entity.CircleJoinRequest;
import org.jeecg.modules.content.circle.service.ICircleJoinReviewService;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CircleJoinRequestTimeoutTask {

    private final ICircleJoinReviewService joinReviewService;
    private final IContentNotificationService notificationService;

    /**
     * 每小时扫描超过 3 天未处理的加入申请，提醒管理员
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void remindTimedOutRequests() {
        List<CircleJoinRequest> timedOut = joinReviewService.getTimedOutRequests();
        if (timedOut.isEmpty()) {
            return;
        }

        log.info("发现 {} 条超时加入申请，开始提醒管理员", timedOut.size());

        for (CircleJoinRequest request : timedOut) {
            try {
                // TODO: 获取圈子管理员列表（需调用 circle-core）
                // 暂时通知创建者
                notificationService.sendNotification(
                    request.getCreateBy(),
                    "JOIN_REQUEST_TIMEOUT",
                    "加入申请超时提醒",
                    "圈子有加入申请超过 3 天未处理，请及时审核"
                );
            } catch (Exception e) {
                log.error("超时提醒发送失败: requestId={}", request.getId(), e);
            }
        }
    }
}
