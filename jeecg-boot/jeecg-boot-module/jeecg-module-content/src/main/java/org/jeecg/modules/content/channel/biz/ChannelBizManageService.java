package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.constant.ChannelConstants;
import org.jeecg.modules.content.channel.dto.CreateChannelDTO;
import org.jeecg.modules.content.channel.dto.UpdateChannelDTO;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ChannelTransfer;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.enums.ReviewResult;
import org.jeecg.modules.content.channel.service.ChannelReviewService;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.jeecg.modules.content.channel.service.ChannelTransferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@Slf4j
public class ChannelBizManageService {

    @Resource
    private ChannelService channelService;

    @Resource
    private ChannelReviewService channelReviewService;

    @Resource
    private ChannelTransferService channelTransferService;

    @Transactional(rollbackFor = Exception.class)
    public Channel createSystemChannel(CreateChannelDTO dto, String operatorId) {
        Channel channel = buildChannelFromDTO(dto, ChannelType.SYSTEM, ChannelStatus.ACTIVE, ChannelConstants.PRIVACY_PUBLIC);
        channel.setOwnerId(operatorId);
        channel.setPinWeight(dto.getPinWeight() != null ? dto.getPinWeight() : 0);
        channelService.save(channel);
        log.info("系统频道创建成功: channelId={}, name={}", channel.getId(), channel.getName());
        return channel;
    }

    @Transactional(rollbackFor = Exception.class)
    public Channel createPersonalChannel(CreateChannelDTO dto, String userId) {
        if (!channelService.checkNameUnique(dto.getName(), null)) {
            throw new JeecgBootException("该频道名称已被使用，请更换");
        }

        long count = channelService.count(new LambdaQueryWrapper<Channel>()
            .eq(Channel::getOwnerId, userId)
            .eq(Channel::getChannelType, ChannelType.PERSONAL)
            .ne(Channel::getStatus, ChannelStatus.DELETED));
        if (count >= ChannelConstants.MAX_PERSONAL_CHANNELS) {
            throw new JeecgBootException("个人频道数量已达上限（" + ChannelConstants.MAX_PERSONAL_CHANNELS + "个）");
        }

        Channel channel = buildChannelFromDTO(dto, ChannelType.PERSONAL, ChannelStatus.PENDING_REVIEW, ChannelConstants.PRIVACY_PUBLIC);
        channel.setOwnerId(userId);
        channelService.save(channel);
        log.info("个人频道创建成功: channelId={}, userId={}", channel.getId(), userId);
        return channel;
    }

    @Transactional(rollbackFor = Exception.class)
    public Channel createOrganizationChannel(CreateChannelDTO dto, String userId, boolean isOrgCertified) {
        if (!isOrgCertified) {
            throw new JeecgBootException("请先完成组织认证");
        }

        if (!channelService.checkNameUnique(dto.getName(), null)) {
            throw new JeecgBootException("该频道名称已被使用，请更换");
        }

        long count = channelService.count(new LambdaQueryWrapper<Channel>()
            .eq(Channel::getOrganizationId, dto.getOrganizationId())
            .eq(Channel::getChannelType, ChannelType.ORGANIZATION)
            .ne(Channel::getStatus, ChannelStatus.DELETED));
        if (count >= ChannelConstants.MAX_ORG_CHANNELS) {
            throw new JeecgBootException("组织频道数量已达上限（" + ChannelConstants.MAX_ORG_CHANNELS + "个）");
        }

        Channel channel = buildChannelFromDTO(dto, ChannelType.ORGANIZATION, ChannelStatus.PENDING_REVIEW, ChannelConstants.PRIVACY_PUBLIC);
        channel.setOwnerId(userId);
        channel.setOrganizationId(dto.getOrganizationId());
        channelService.save(channel);
        log.info("组织频道创建成功: channelId={}, orgId={}", channel.getId(), dto.getOrganizationId());
        return channel;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateChannel(String channelId, UpdateChannelDTO dto, String userId) {
        Channel channel = channelService.getById(channelId);
        if (channel == null) {
            throw new JeecgBootException("频道不存在");
        }

        boolean hasCriticalChange = false;

        if (dto.getName() != null && !dto.getName().equals(channel.getName())) {
            if (!channelService.checkNameUnique(dto.getName(), channelId)) {
                throw new JeecgBootException("该频道名称已被使用，请更换");
            }
            channel.setName(dto.getName());
            hasCriticalChange = true;
        }

        if (dto.getDescription() != null) {
            channel.setDescription(dto.getDescription());
            hasCriticalChange = true;
        }
        if (dto.getIconUrl() != null) {
            channel.setIconUrl(dto.getIconUrl());
            hasCriticalChange = true;
        }
        if (dto.getCoverUrl() != null) {
            channel.setCoverUrl(dto.getCoverUrl());
            hasCriticalChange = true;
        }
        if (dto.getCategoryId() != null) {
            channel.setCategoryId(dto.getCategoryId());
            hasCriticalChange = true;
        }

        // 关键字段修改触发审核（系统频道除外）
        if (hasCriticalChange && channel.getChannelType() != ChannelType.SYSTEM) {
            channel.setStatus(ChannelStatus.PENDING_REVIEW);
        }

        channelService.updateById(channel);
        log.info("频道信息更新: channelId={}, hasCriticalChange={}", channelId, hasCriticalChange);
    }

    @Transactional(rollbackFor = Exception.class)
    public void transferChannel(String channelId, String fromUserId, String toUserId) {
        Channel channel = channelService.getById(channelId);
        if (channel == null) {
            throw new JeecgBootException("频道不存在");
        }
        if (channel.getChannelType() == ChannelType.SYSTEM) {
            throw new JeecgBootException("系统频道不可转让");
        }
        if (!channel.getOwnerId().equals(fromUserId)) {
            throw new JeecgBootException("仅频道主可发起转让");
        }
        // 组织频道仅可在组织管理员间转移
        if (channel.getChannelType() == ChannelType.ORGANIZATION) {
            if (channel.getOrganizationId() == null) {
                throw new JeecgBootException("组织频道未绑定组织，无法转让");
            }
            // TODO: 当组织模块实现后，校验 toUserId 是否为该组织管理员
        }
        channelTransferService.createTransfer(channelId, fromUserId, toUserId);
        log.info("频道转让请求已创建: channelId={}, from={}, to={}", channelId, fromUserId, toUserId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmTransfer(String transferId, String userId) {
        ChannelTransfer transfer = channelTransferService.confirmTransfer(transferId, userId);
        if (transfer == null) {
            throw new JeecgBootException("转让确认失败，请求可能已过期或无效");
        }
        Channel channel = channelService.getById(transfer.getChannelId());
        channel.setOwnerId(transfer.getToUserId());
        channelService.updateById(channel);
        log.info("频道转让完成: channelId={}, newOwner={}", channel.getId(), transfer.getToUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void rejectTransfer(String transferId, String userId) {
        boolean success = channelTransferService.rejectTransfer(transferId, userId);
        if (!success) {
            throw new JeecgBootException("转让拒绝失败，请求可能已过期或无效");
        }
        log.info("频道转让已拒绝: transferId={}, userId={}", transferId, userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteChannel(String channelId, String userId) {
        Channel channel = channelService.getById(channelId);
        if (channel == null) {
            throw new JeecgBootException("频道不存在");
        }
        if (channel.getChannelType() == ChannelType.SYSTEM) {
            throw new JeecgBootException("系统频道仅平台可管理");
        }
        if (!channel.getOwnerId().equals(userId)) {
            throw new JeecgBootException("仅频道主可删除频道");
        }

        channel.setStatus(ChannelStatus.DELETE_COOLING);
        channel.setDeleteCoolingEndTime(Date.from(LocalDateTime.now().plusDays(ChannelConstants.COOLING_DAYS)
                .atZone(ZoneId.systemDefault()).toInstant()));
        channelService.updateById(channel);
        log.info("频道进入删除冷静期: channelId={}, endTime={}", channelId, channel.getDeleteCoolingEndTime());
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelDelete(String channelId, String userId) {
        Channel channel = channelService.getById(channelId);
        if (channel == null || channel.getStatus() != ChannelStatus.DELETE_COOLING) {
            throw new JeecgBootException("频道不在冷静期内");
        }
        if (!channel.getOwnerId().equals(userId)) {
            throw new JeecgBootException("仅频道主可撤销删除");
        }
        if (new Date().after(channel.getDeleteCoolingEndTime())) {
            throw new JeecgBootException("冷静期已过，无法撤销");
        }

        channel.setStatus(ChannelStatus.ACTIVE);
        channel.setDeleteCoolingEndTime(null);
        channelService.updateById(channel);
        log.info("频道删除已撤销: channelId={}", channelId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void reviewChannel(String channelId, String reviewerId, ReviewResult result, String reason) {
        Channel channel = channelService.getById(channelId);
        if (channel == null) {
            throw new JeecgBootException("频道不存在");
        }
        if (channel.getStatus() != ChannelStatus.PENDING_REVIEW) {
            throw new JeecgBootException("频道当前状态不可审核");
        }

        channelReviewService.createReview(channelId, reviewerId, result, reason);

        channel.setStatus(switch (result) {
            case PASS -> ChannelStatus.ACTIVE;
            case REJECT -> ChannelStatus.REJECTED;
            case RETURN_FOR_EDIT -> ChannelStatus.DRAFT;
        });

        channelService.updateById(channel);
        log.info("频道审核完成: channelId={}, result={}", channelId, result);
    }

    private Channel buildChannelFromDTO(CreateChannelDTO dto, ChannelType channelType, ChannelStatus status, int privacyDefault) {
        Channel channel = new Channel();
        channel.setName(dto.getName());
        channel.setDescription(dto.getDescription());
        channel.setIconUrl(dto.getIconUrl());
        channel.setCoverUrl(dto.getCoverUrl());
        channel.setChannelType(channelType);
        channel.setStatus(status);
        channel.setPrivacy(dto.getPrivacy() != null ? dto.getPrivacy() : privacyDefault);
        channel.setCategoryId(dto.getCategoryId());
        return channel;
    }
}
