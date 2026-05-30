package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.channel.biz.ChannelBizManageService;
import org.jeecg.modules.content.channel.dto.CreateChannelDTO;
import org.jeecg.modules.content.channel.dto.UpdateChannelDTO;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.jeecg.modules.content.channel.util.ChannelConvertUtil;
import org.jeecg.modules.content.channel.vo.ChannelVO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/channels")
@Slf4j
@Tag(name = "频道管理", description = "用户端频道API")
public class ChannelController {

    @Resource
    private ChannelBizManageService channelBizManageService;

    @Resource
    private ChannelService channelService;

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
}
