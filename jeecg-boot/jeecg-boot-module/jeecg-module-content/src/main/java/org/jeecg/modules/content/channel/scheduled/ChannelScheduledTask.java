package org.jeecg.modules.content.channel.scheduled;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ChannelTransfer;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.enums.TransferStatus;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.jeecg.modules.content.channel.service.ChannelTransferService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Date;

@Component
@Slf4j
public class ChannelScheduledTask {

    @Resource
    private ChannelService channelService;

    @Resource
    private ChannelTransferService channelTransferService;

    /**
     * 每小时扫描冷静期到期的频道，批量处理为 Deleted
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void processDeleteCoolingExpired() {
        boolean updated = channelService.update(new LambdaUpdateWrapper<Channel>()
            .eq(Channel::getStatus, ChannelStatus.DELETE_COOLING)
            .le(Channel::getDeleteCoolingEndTime, new Date())
            .set(Channel::getStatus, ChannelStatus.DELETED));
        if (updated) {
            log.info("冷静期到期处理完成");
        }
    }

    /**
     * 每小时扫描超时的转让请求（错开5分钟执行）
     */
    @Scheduled(cron = "0 5 * * * ?")
    public void processTransferExpired() {
        boolean updated = channelTransferService.update(new LambdaUpdateWrapper<ChannelTransfer>()
            .eq(ChannelTransfer::getStatus, TransferStatus.PENDING)
            .le(ChannelTransfer::getExpireTime, new Date())
            .set(ChannelTransfer::getStatus, TransferStatus.EXPIRED));
        if (updated) {
            log.info("转让请求超时处理完成");
        }
    }
}
