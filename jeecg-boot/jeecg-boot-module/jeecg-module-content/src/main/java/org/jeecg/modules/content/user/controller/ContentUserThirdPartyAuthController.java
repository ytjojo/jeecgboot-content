package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.service.IContentUserThirdPartyAuthService;
import org.jeecg.modules.content.user.vo.ContentThirdPartyAuthVO;
import org.jeecg.modules.content.user.vo.ContentThirdPartyAuthorizationDetailVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 内容社区用户第三方授权管理 ReST 端点。
 */
@Tag(name = "内容社区用户第三方授权")
@RestController
@RequestMapping("/api/v1/content/user/auth/third-party")
public class ContentUserThirdPartyAuthController {

    @Resource
    private IContentUserThirdPartyAuthService thirdPartyAuthService;

    /**
     * 查询当前用户的所有活跃第三方授权。
     */
    @Operation(summary = "查询活跃第三方授权列表")
    @GetMapping("/")
    public Result<List<ContentThirdPartyAuthVO>> listActiveAuths(@RequestParam("userId") String userId) {
        return Result.OK(thirdPartyAuthService.listActiveAuths(userId));
    }

    /**
     * 查询指定授权的详情。
     */
    @Operation(summary = "查询第三方授权详情")
    @GetMapping("/{authId}")
    public Result<ContentThirdPartyAuthorizationDetailVO> getAuthDetail(@RequestParam("userId") String userId,
                                                                         @PathVariable("authId") String authId) {
        return Result.OK(thirdPartyAuthService.getAuthDetail(userId, authId));
    }

    /**
     * 撤销指定的第三方授权。
     */
    @Operation(summary = "撤销第三方授权")
    @DeleteMapping("/{authId}")
    public Result<String> revokeAuth(@RequestParam("userId") String userId,
                                     @PathVariable("authId") String authId) {
        thirdPartyAuthService.revokeAuth(userId, authId);
        return Result.OK("授权已撤销");
    }
}
