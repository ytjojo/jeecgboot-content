package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.channel.biz.ChannelBizManageService;
import org.jeecg.modules.content.channel.dto.CreateChannelDTO;
import org.jeecg.modules.content.channel.dto.UpdateChannelDTO;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ChannelTransfer;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.enums.JoinMethod;
import org.jeecg.modules.content.channel.enums.PrivacyType;
import org.jeecg.modules.content.channel.req.ChannelListQuery;
import org.jeecg.modules.content.channel.req.UpdateJoinMethodReq;
import org.jeecg.modules.content.channel.req.UpdatePrivacyReq;
import org.jeecg.modules.content.channel.service.ChannelJoinMethodService;
import org.jeecg.modules.content.channel.service.ChannelPrivacyService;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.jeecg.modules.content.channel.util.ChannelConvertUtil;
import org.jeecg.modules.content.channel.service.ChannelTransferService;
import org.jeecg.modules.content.channel.vo.ChannelTransferVO;
import org.jeecg.modules.content.channel.vo.ChannelVO;
import org.jeecg.modules.content.channel.vo.DeleteCheckResultVO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/channels")
@Slf4j
@Tag(name = "频道管理", description = "用户端频道API")
public class ChannelController {

    @Resource
    private ChannelBizManageService channelBizManageService;

    @Resource
    private ChannelService channelService;

    @Resource
    private ChannelTransferService channelTransferService;

    @Resource
    private ChannelPrivacyService channelPrivacyService;

    @Resource
    private ChannelJoinMethodService channelJoinMethodService;

    @PostMapping("/create")
    @Operation(summary = "创建频道")
    public Result<ChannelVO> createChannel(@Valid @RequestBody CreateChannelDTO dto) {
        String userId = SecureUtil.currentUser().getId();
        Channel channel;
        if (dto.getChannelType() == ChannelType.PERSONAL) {
            channel = channelBizManageService.createPersonalChannel(dto, userId);
        } else if (dto.getChannelType() == ChannelType.ORGANIZATION) {
            channel = channelBizManageService.createOrganizationChannel(dto, userId, true);
        } else {
            return Result.error("用户端不可创建系统频道");
        }
        return Result.OK(ChannelConvertUtil.toVO(channel));
    }

    @GetMapping("/list")
    @Operation(summary = "查询我的频道列表")
    public Result<IPage<ChannelVO>> listMyChannels(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            ChannelListQuery query) {
        String userId = SecureUtil.currentUser().getId();
        Page<Channel> page = new Page<>(current, size);
        IPage<Channel> result = channelService.listMyChannels(page, userId, query);
        return Result.OK(result.convert(ChannelConvertUtil::toVO));
    }

    @GetMapping("/{id}/delete-check")
    @Operation(summary = "删除前置条件校验")
    public Result<DeleteCheckResultVO> checkDeletePrecondition(@PathVariable String id) {
        String userId = SecureUtil.currentUser().getId();
        DeleteCheckResultVO result = channelBizManageService.checkDeletePrecondition(id, userId);
        return Result.OK(result);
    }

    @GetMapping("/{id}/transfers")
    @Operation(summary = "查询转让历史")
    public Result<List<ChannelTransferVO>> getTransferHistory(@PathVariable String id) {
        String userId = SecureUtil.currentUser().getId();
        Channel channel = channelService.getById(id);
        if (channel == null || !channel.getOwnerId().equals(userId)) {
            return Result.error("无权访问");
        }
        List<ChannelTransfer> transfers = channelTransferService.getTransferHistory(id);
        List<ChannelTransferVO> voList = transfers.stream()
            .map(this::convertToTransferVO)
            .collect(Collectors.toList());
        return Result.OK(voList);
    }

    @GetMapping("/check-name")
    @Operation(summary = "校验频道名称唯一性")
    public Result<Boolean> checkNameUnique(
            @RequestParam String name,
            @RequestParam(required = false) String excludeId) {
        boolean available = channelService.checkNameUnique(name, excludeId);
        return Result.OK(available);
    }

    @GetMapping("/{id}/transfer/pending")
    @Operation(summary = "查询待确认的转让请求")
    public Result<ChannelTransferVO> getPendingTransfer(@PathVariable String id) {
        String userId = SecureUtil.currentUser().getId();
        Channel channel = channelService.getById(id);
        if (channel == null || !channel.getOwnerId().equals(userId)) {
            return Result.error("无权访问");
        }
        ChannelTransfer transfer = channelTransferService.getPendingTransfer(id);
        if (transfer == null) {
            return Result.OK(null);
        }
        return Result.OK(convertToTransferVO(transfer));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询频道详情")
    public Result<ChannelVO> getChannel(@PathVariable String id) {
        Channel channel = channelService.getById(id);
        if (channel == null) {
            return Result.error("频道不存在");
        }
        return Result.OK(ChannelConvertUtil.toVO(channel));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑频道")
    public Result<Void> updateChannel(@PathVariable String id, @Valid @RequestBody UpdateChannelDTO dto) {
        String userId = SecureUtil.currentUser().getId();
        channelBizManageService.updateChannel(id, dto, userId);
        return Result.OK();
    }

    @PostMapping("/{id}/transfer")
    @Operation(summary = "发起转让")
    public Result<Void> transferChannel(@PathVariable String id, @RequestParam String toUserId) {
        String userId = SecureUtil.currentUser().getId();
        channelBizManageService.transferChannel(id, userId, toUserId);
        return Result.OK();
    }

    @PostMapping("/transfer/{transferId}/confirm")
    @Operation(summary = "确认转让")
    public Result<Void> confirmTransfer(@PathVariable String transferId) {
        String userId = SecureUtil.currentUser().getId();
        channelBizManageService.confirmTransfer(transferId, userId);
        return Result.OK();
    }

    @PostMapping("/transfer/{transferId}/reject")
    @Operation(summary = "拒绝转让")
    public Result<Void> rejectTransfer(@PathVariable String transferId) {
        String userId = SecureUtil.currentUser().getId();
        channelBizManageService.rejectTransfer(transferId, userId);
        return Result.OK();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "申请删除")
    public Result<Void> deleteChannel(@PathVariable String id) {
        String userId = SecureUtil.currentUser().getId();
        channelBizManageService.deleteChannel(id, userId);
        return Result.OK();
    }

    @PostMapping("/{id}/cancel-delete")
    @Operation(summary = "撤销删除")
    public Result<Void> cancelDelete(@PathVariable String id) {
        String userId = SecureUtil.currentUser().getId();
        channelBizManageService.cancelDelete(id, userId);
        return Result.OK();
    }

    @PutMapping("/privacy")
    @Operation(summary = "更新隐私设置")
    public Result<Void> updatePrivacy(@Valid @RequestBody UpdatePrivacyReq req) {
        String userId = SecureUtil.currentUser().getId();
        PrivacyType privacyType;
        try {
            privacyType = PrivacyType.fromCode(req.getPrivacy());
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
        channelPrivacyService.updatePrivacy(req.getChannelId(), privacyType, userId);
        return Result.OK();
    }

    @PutMapping("/join-method")
    @Operation(summary = "更新加入方式")
    public Result<Void> updateJoinMethod(@Valid @RequestBody UpdateJoinMethodReq req) {
        String userId = SecureUtil.currentUser().getId();
        JoinMethod joinMethod;
        try {
            joinMethod = JoinMethod.fromCode(req.getJoinMethod());
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
        channelJoinMethodService.updateJoinMethod(req.getChannelId(), joinMethod, userId);
        return Result.OK();
    }

    private ChannelTransferVO convertToTransferVO(ChannelTransfer transfer) {
        ChannelTransferVO vo = new ChannelTransferVO();
        vo.setTransferId(transfer.getId());
        vo.setChannelId(transfer.getChannelId());
        vo.setFromUserId(transfer.getFromUserId());
        vo.setToUserId(transfer.getToUserId());
        vo.setStatus(transfer.getStatus().name());
        vo.setCreatedTime(transfer.getCreateTime());
        return vo;
    }
}
