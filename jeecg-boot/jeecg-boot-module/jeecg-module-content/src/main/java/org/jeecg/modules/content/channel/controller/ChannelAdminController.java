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
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.enums.ReviewResult;
import org.jeecg.modules.content.channel.req.ChannelListQuery;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.jeecg.modules.content.channel.util.ChannelConvertUtil;
import org.jeecg.modules.content.channel.vo.ChannelVO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/content/admin/channels")
@Slf4j
@Tag(name = "频道后台管理", description = "后台频道管理API")
public class ChannelAdminController {

    @Resource
    private ChannelBizManageService channelBizManageService;

    @Resource
    private ChannelService channelService;

    @PostMapping("/create-system")
    @Operation(summary = "创建系统频道")
    public Result<ChannelVO> createSystemChannel(@Valid @RequestBody CreateChannelDTO dto) {
        String operatorId = SecureUtil.currentUser().getId();
        dto.setChannelType(ChannelType.SYSTEM);
        Channel channel = channelBizManageService.createSystemChannel(dto, operatorId);
        return Result.OK(ChannelConvertUtil.toVO(channel));
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "审核频道")
    public Result<Void> reviewChannel(@PathVariable String id,
                                       @RequestParam ReviewResult result,
                                       @RequestParam(required = false) String reason) {
        String reviewerId = SecureUtil.currentUser().getId();
        channelBizManageService.reviewChannel(id, reviewerId, result, reason);
        return Result.OK();
    }

    @GetMapping("/list")
    @Operation(summary = "查询频道列表（后台）")
    public Result<IPage<ChannelVO>> listAllChannels(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            ChannelListQuery query) {
        Page<Channel> page = new Page<>(current, size);
        IPage<Channel> result = channelService.listAllChannels(page, query);
        return Result.OK(result.convert(ChannelConvertUtil::toVO));
    }
}
