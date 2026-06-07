package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.channel.biz.ContentChannelDiscoveryBiz;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "频道发现", description = "频道发现首页数据接口")
@RestController
@RequestMapping("/api/v1/content/channel/discovery")
public class ContentChannelDiscoveryController {

    @Resource
    private ContentChannelDiscoveryBiz discoveryBiz;

    @Operation(summary = "获取发现首页数据")
    @GetMapping("/home")
    public Result<Map<String, Object>> getDiscoveryHome() {
        String userId = SecureUtil.currentUser().getId();
        return Result.OK(discoveryBiz.getDiscoveryData(userId));
    }
}
