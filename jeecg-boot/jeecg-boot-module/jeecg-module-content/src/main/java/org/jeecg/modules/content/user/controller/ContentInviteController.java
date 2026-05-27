package org.jeecg.modules.content.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.user.service.IContentInviteService;
import org.jeecg.modules.content.user.vo.ContentInviteCodeVO;
import org.jeecg.modules.content.user.vo.ContentInviteRecordPageVO;
import org.jeecg.modules.content.user.vo.ContentInviteStatsVO;
import org.springframework.web.bind.annotation.*;

/**
 * 内容社区邀请相关接口。
 */
@Tag(name = "内容社区邀请")
@RestController
@RequestMapping("/content/user/invite")
public class ContentInviteController {

    @Resource
    private IContentInviteService inviteService;

    /**
     * 生成或获取邀请码。
     */
    @Operation(summary = "生成或获取邀请码")
    @PostMapping("/generate")
    public Result<ContentInviteCodeVO> generateInviteCode(@RequestParam("userId") String userId) {
        return Result.OK(inviteService.generateOrGetInviteCode(userId));
    }

    /**
     * 绑定邀请关系（注册时调用）。
     */
    @Operation(summary = "绑定邀请关系")
    @PostMapping("/bind")
    public Result<String> bindInviteRelation(@RequestParam("inviteCode") String inviteCode,
                                             @RequestParam("inviteeUserId") String inviteeUserId) {
        inviteService.bindInviteRelation(inviteCode, inviteeUserId);
        return Result.OK("绑定成功");
    }

    /**
     * 查询邀请记录。
     */
    @Operation(summary = "查询邀请记录")
    @GetMapping("/records")
    public Result<ContentInviteRecordPageVO> listInviteRecords(
            @RequestParam("userId") String userId,
            @RequestParam(value = "pageNo", defaultValue = "1") Long pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.OK(inviteService.listInviteRecords(userId, pageNo, pageSize));
    }

    /**
     * 查询邀请统计。
     */
    @Operation(summary = "查询邀请统计")
    @GetMapping("/stats")
    public Result<ContentInviteStatsVO> getInviteStats(@RequestParam("userId") String userId) {
        return Result.OK(inviteService.getInviteStats(userId));
    }
}
