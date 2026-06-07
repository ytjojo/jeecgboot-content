package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelMemberBizService;
import org.jeecg.modules.content.channel.entity.ChannelJoinApplication;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.enums.MemberRole;
import org.jeecg.modules.content.channel.service.ChannelJoinApplicationService;
import org.jeecg.modules.content.channel.service.ChannelMemberListService;
import org.jeecg.modules.content.channel.service.ChannelMuteService;
import org.jeecg.modules.content.channel.service.ChannelSubscriptionService;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.channel.service.ChannelMemberService;
import org.jeecg.modules.content.channel.vo.UserChannelRelationVO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@Tag(name = "频道成员管理")
@RestController
@RequestMapping("/api/v1/content/channel/member")
public class ChannelMemberController {

    @Resource
    private ChannelMemberBizService memberBizService;
    @Resource
    private ChannelMemberService memberService;
    @Resource
    private ChannelMemberListService memberListService;
    @Resource
    private ChannelJoinApplicationService applicationService;
    @Resource
    private ChannelSubscriptionService subscriptionService;
    @Resource
    private ChannelMuteService muteService;

    @Operation(summary = "自由加入频道")
    @PostMapping("/join/free")
    public Result<String> joinFree(@RequestParam String channelId) {
        String userId = SecureUtil.currentUser().getId();
        memberBizService.joinByFree(channelId, userId);
        return Result.OK("加入成功");
    }

    @Operation(summary = "提交加入申请")
    @PostMapping("/join/apply")
    public Result<String> joinApply(@RequestParam String channelId,
                                    @RequestParam(required = false) String reason) {
        String userId = SecureUtil.currentUser().getId();
        memberBizService.joinByReview(channelId, userId, reason);
        return Result.OK("申请已提交");
    }

    @Operation(summary = "退出频道")
    @PostMapping("/leave")
    public Result<String> leave(@RequestParam String channelId) {
        String userId = SecureUtil.currentUser().getId();
        ChannelMember member = memberService.getByChannelAndUser(channelId, userId);
        if (member == null) {
            return Result.error("您不是该频道成员");
        }
        if (member.getRole() != null && member.getRole() == MemberRole.OWNER.getCode()) {
            return Result.error("频道主不可直接退出，请先转让频道");
        }
        memberService.removeMember(member.getId());
        return Result.OK("已退出");
    }

    @Operation(summary = "分配角色")
    @PostMapping("/assign-role")
    public Result<String> assignRole(@RequestParam String memberId, @RequestParam MemberRole role) {
        String operatorId = SecureUtil.currentUser().getId();
        memberService.assignRole(memberId, role, operatorId);
        return Result.OK("角色已更新");
    }

    @Operation(summary = "成员列表")
    @GetMapping("/list")
    public Result<IPage<ChannelMember>> listMembers(@RequestParam String channelId,
                                                     @RequestParam(required = false) Integer role,
                                                     @RequestParam(defaultValue = "1") int pageNum,
                                                     @RequestParam(defaultValue = "20") int pageSize) {
        return Result.OK(memberListService.listMembers(channelId, role, pageNum, pageSize));
    }

    @Operation(summary = "搜索成员")
    @GetMapping("/search")
    public Result<IPage<ChannelMember>> searchMembers(@RequestParam String channelId,
                                                       @RequestParam String keyword,
                                                       @RequestParam(defaultValue = "1") int pageNum,
                                                       @RequestParam(defaultValue = "20") int pageSize) {
        return Result.OK(memberListService.searchMembers(channelId, keyword, pageNum, pageSize));
    }

    @Operation(summary = "待审核列表")
    @GetMapping("/applications/pending")
    public Result<List<ChannelJoinApplication>> listPendingApplications(@RequestParam String channelId) {
        return Result.OK(applicationService.listPending(channelId));
    }

    @Operation(summary = "批准申请")
    @PostMapping("/applications/approve")
    public Result<String> approveApplication(@RequestParam String applicationId,
                                              @RequestParam(required = false) String reason) {
        String reviewerId = SecureUtil.currentUser().getId();
        memberBizService.approveAndAddMember(applicationId, reviewerId, reason);
        return Result.OK("已批准");
    }

    @Operation(summary = "拒绝申请")
    @PostMapping("/applications/reject")
    public Result<String> rejectApplication(@RequestParam String applicationId,
                                             @RequestParam(required = false) String reason) {
        String reviewerId = SecureUtil.currentUser().getId();
        applicationService.reject(applicationId, reviewerId, reason);
        return Result.OK("已拒绝");
    }

    @Operation(summary = "用户频道关系查询")
    @GetMapping("/relation")
    public Result<UserChannelRelationVO> getUserChannelRelation(@RequestParam String channelId) {
        String userId = SecureUtil.currentUser().getId();
        UserChannelRelationVO vo = new UserChannelRelationVO();
        vo.setChannelId(channelId);
        vo.setUserId(userId);

        ChannelMember member = memberService.getByChannelAndUser(channelId, userId);
        if (member != null) {
            vo.setIsMember(true);
            vo.setRole(member.getRole());
        } else {
            vo.setIsMember(false);
        }
        vo.setIsMuted(muteService.isMuted(channelId, userId));
        vo.setIsSubscribed(subscriptionService.isSubscribed(channelId, userId));

        return Result.OK(vo);
    }
}
