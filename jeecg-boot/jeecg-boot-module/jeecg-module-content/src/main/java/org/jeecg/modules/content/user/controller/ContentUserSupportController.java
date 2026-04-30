package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.req.support.ContentAppealCreateReq;
import org.jeecg.modules.content.user.service.IContentUserSupportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ReST endpoints for content user support.
 */
@Tag(name = "内容社区用户支持")
@RestController
@RequestMapping("/content/user/support")
public class ContentUserSupportController {

    @Resource
    private IContentUserSupportService supportService;

    /**
     * Creates a user appeal record and returns its identifier.
     */
    @Operation(summary = "创建处罚申诉")
    @PostMapping("/appeal/create")
    public Result<String> createAppeal(@Valid @RequestBody ContentAppealCreateReq req) {
        return Result.OK(supportService.createAppeal(req));
    }
}
