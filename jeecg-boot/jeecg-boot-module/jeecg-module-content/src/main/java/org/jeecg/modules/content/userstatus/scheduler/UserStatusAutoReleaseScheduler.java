package org.jeecg.modules.content.userstatus.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.userstatus.biz.UserStatusBizManageService;
import org.jeecg.modules.content.userstatus.entity.UserStatusEnum;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 自动解禁定时任务。
 * 每 5 分钟扫描到期处罚状态，自动解禁。
 */
@Slf4j
@Component
public class UserStatusAutoReleaseScheduler {

    @Resource
    private UserStatusBizManageService bizManageService;

    /**
     * 自动解禁到期状态
     * 每 5 分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void autoReleaseExpiredStatus() {
        log.info("开始执行自动解禁定时任务");

        try {
            // 处理禁言到期
            releaseExpiredStatus(UserStatusEnum.MUTED, "自动解禁：禁言期限到期");

            // 处理限制推荐到期
            releaseExpiredStatus(UserStatusEnum.RESTRICTED_RECOMMEND, "自动解禁：限制推荐期限到期");

            // 处理冻结到期
            releaseExpiredStatus(UserStatusEnum.FROZEN, "自动解禁：冻结期限到期");

            // 处理临时封禁到期
            releaseExpiredStatus(UserStatusEnum.BANNED, "自动解禁：封禁期限到期");

            log.info("自动解禁定时任务执行完成");
        } catch (Exception e) {
            log.error("自动解禁定时任务执行异常", e);
        }
    }

    /**
     * 解禁指定状态的到期用户
     *
     * @param status   用户状态
     * @param reason   解禁原因
     */
    private void releaseExpiredStatus(UserStatusEnum status, String reason) {
        List<String> expiredUserIds = bizManageService.findExpiredStatusUsers(status);

        if (expiredUserIds.isEmpty()) {
            log.debug("没有到期的{}状态用户", status.getDisplayName());
            return;
        }

        log.info("发现{}个到期的{}状态用户，开始解禁", expiredUserIds.size(), status.getDisplayName());

        try {
            bizManageService.batchChangeStatus(
                expiredUserIds,
                status,
                UserStatusEnum.NORMAL,
                reason,
                "SYSTEM",
                "SYSTEM"
            );
            log.info("成功解禁{}个{}状态用户", expiredUserIds.size(), status.getDisplayName());
        } catch (Exception e) {
            log.error("批量解禁{}状态用户失败", status.getDisplayName(), e);
        }
    }
}
