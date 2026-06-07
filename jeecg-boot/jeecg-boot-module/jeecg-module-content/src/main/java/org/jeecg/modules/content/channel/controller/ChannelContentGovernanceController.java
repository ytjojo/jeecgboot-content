package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.channel.biz.ChannelGovernanceBiz;
import org.jeecg.modules.content.channel.req.governance.ChannelGovernanceReq;
import org.jeecg.modules.content.channel.req.governance.GovernanceContentListReq;
import org.jeecg.modules.content.channel.req.governance.RecycleBinListReq;
import org.jeecg.modules.content.channel.vo.governance.GovernanceContentItemVO;
import org.jeecg.modules.content.channel.vo.governance.RecycleBinItemVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "频道内容治理", description = "频道内容置顶、精华、删除、恢复等治理操作接口")
@Validated
@RestController
@RequestMapping("/api/v1/content/channel/governance")
public class ChannelContentGovernanceController {

    @Resource
    private ChannelGovernanceBiz channelGovernanceBiz;

    @Operation(summary = "执行治理操作")
    @PostMapping
    public Result<Void> governance(@Valid @RequestBody ChannelGovernanceReq req) {
        String userId = SecureUtil.currentUser().getId();
        channelGovernanceBiz.executeGovernance(req, userId);
        return Result.OK();
    }

    @Operation(summary = "获取频道内容列表")
    @GetMapping("/content/list")
    public Result<Page<GovernanceContentItemVO>> getContentList(@Valid GovernanceContentListReq req) {
        return Result.OK(channelGovernanceBiz.getContentList(req));
    }

    @Operation(summary = "获取回收站列表")
    @GetMapping("/recycle-bin/list")
    public Result<Page<RecycleBinItemVO>> getRecycleBinList(@Valid RecycleBinListReq req) {
        return Result.OK(channelGovernanceBiz.getRecycleBinList(req));
    }
}
