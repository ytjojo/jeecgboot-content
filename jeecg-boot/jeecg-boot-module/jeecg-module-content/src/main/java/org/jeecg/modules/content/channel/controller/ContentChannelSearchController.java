package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.channel.req.query.ChannelSearchQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelSearchFeedbackService;
import org.jeecg.modules.content.channel.service.IContentChannelSearchService;
import org.jeecg.modules.content.channel.vo.ChannelSearchResultVO;
import org.springframework.web.bind.annotation.*;

@Tag(name = "频道搜索")
@RestController
@RequestMapping("/api/v1/content/channel/search")
public class ContentChannelSearchController {

    @Resource
    private IContentChannelSearchService searchService;

    @Resource
    private IContentChannelSearchFeedbackService searchFeedbackService;

    @Operation(summary = "搜索频道")
    @GetMapping("/query")
    public Result<IPage<ChannelSearchResultVO>> search(
            @RequestParam String userId,
            ChannelSearchQueryReq req) {
        return Result.OK(searchService.search(userId, req));
    }

    @Operation(summary = "提交搜索反馈")
    @PostMapping("/feedback")
    public Result<Void> submitFeedback(
            @RequestParam String keyword,
            @RequestParam String channelId,
            @RequestParam String action) {
        String userId = SecureUtil.currentUser().getId();
        searchFeedbackService.recordFeedback(userId, keyword, channelId, action);
        return Result.OK();
    }
}
