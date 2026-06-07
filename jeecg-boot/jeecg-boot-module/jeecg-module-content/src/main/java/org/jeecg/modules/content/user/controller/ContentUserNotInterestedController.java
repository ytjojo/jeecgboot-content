package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.service.IContentUserNotInterestedService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内容社区不感兴趣反馈 Controller。
 */
@Tag(name = "内容社区不感兴趣反馈")
@RestController
@RequestMapping("/api/v1/content/user")
public class ContentUserNotInterestedController {

    @Resource
    private IContentUserNotInterestedService notInterestedService;

    /**
     * 记录不感兴趣反馈。
     */
    @Operation(summary = "记录不感兴趣")
    @PostMapping("/not-interested")
    public Result<String> recordNotInterested(@RequestParam("userId") String userId,
                                              @RequestParam("contentId") String contentId,
                                              @RequestParam("contentType") String contentType) {
        notInterestedService.recordFeedback(userId, contentId, contentType);
        return Result.OK("记录成功");
    }
}
