package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.channel.biz.ChannelGovernanceBiz;
import org.jeecg.modules.content.channel.req.governance.ChannelGovernanceReq;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "频道内容治理", description = "频道内容置顶、精华、删除、恢复等治理操作接口")
@Validated
@RestController
@RequestMapping("/content/channel/governance")
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
}
