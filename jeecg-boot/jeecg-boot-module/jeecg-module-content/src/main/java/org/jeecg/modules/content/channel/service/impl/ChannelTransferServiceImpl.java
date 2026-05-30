package org.jeecg.modules.content.channel.service.impl;

import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.content.channel.constant.ChannelConstants;
import org.jeecg.modules.content.channel.entity.ChannelTransfer;
import org.jeecg.modules.content.channel.enums.TransferStatus;
import org.jeecg.modules.content.channel.mapper.ChannelTransferMapper;
import org.jeecg.modules.content.channel.service.ChannelTransferService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class ChannelTransferServiceImpl extends JeecgServiceImpl<ChannelTransferMapper, ChannelTransfer>
    implements ChannelTransferService {

    @Override
    public ChannelTransfer createTransfer(String channelId, String fromUserId, String toUserId) {
        ChannelTransfer transfer = new ChannelTransfer();
        transfer.setChannelId(channelId);
        transfer.setFromUserId(fromUserId);
        transfer.setToUserId(toUserId);
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setExpireTime(Date.from(LocalDateTime.now().plusDays(ChannelConstants.TRANSFER_EXPIRE_DAYS)
                .atZone(ZoneId.systemDefault()).toInstant()));
        baseMapper.insert(transfer);
        return transfer;
    }

    @Override
    public ChannelTransfer confirmTransfer(String transferId, String userId) {
        ChannelTransfer transfer = baseMapper.selectById(transferId);
        if (transfer == null || transfer.getStatus() != TransferStatus.PENDING) {
            return null;
        }
        if (!transfer.getToUserId().equals(userId)) {
            return null;
        }
        if (new Date().after(transfer.getExpireTime())) {
            transfer.setStatus(TransferStatus.EXPIRED);
            baseMapper.updateById(transfer);
            return null;
        }
        transfer.setStatus(TransferStatus.ACCEPTED);
        baseMapper.updateById(transfer);
        return transfer;
    }

    @Override
    public boolean rejectTransfer(String transferId, String userId) {
        ChannelTransfer transfer = baseMapper.selectById(transferId);
        if (transfer == null || transfer.getStatus() != TransferStatus.PENDING) {
            return false;
        }
        if (!transfer.getToUserId().equals(userId)) {
            return false;
        }
        transfer.setStatus(TransferStatus.REJECTED);
        baseMapper.updateById(transfer);
        return true;
    }
}
