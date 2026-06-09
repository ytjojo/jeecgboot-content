package org.jeecg.modules.content.circle.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.circle.biz.ICircleMemberBiz;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.req.update.CircleMemberUpdateReq;
import org.jeecg.modules.content.circle.service.ICircleMemberService;
import org.jeecg.modules.content.circle.vo.CircleMemberVO;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "圈子成员管理", description = "成员角色变更、禁言、移除等接口")
@Validated
@RestController
@RequestMapping("/api/v1/content/circle/member")
public class CircleMemberController {

    @Resource
    private ICircleMemberBiz circleMemberBiz;

    @Resource
    private ICircleMemberService circleMemberService;

    @Operation(summary = "变更成员角色")
    @PostMapping("/change-role")
    public Result<String> changeRole(@Valid @RequestBody CircleMemberUpdateReq req) {
        String operatorId = SecureUtil.currentUser().getId();
        circleMemberBiz.changeRole(req, operatorId);
        return Result.OK("角色变更成功");
    }

    @Operation(summary = "禁言成员")
    @PostMapping("/mute")
    public Result<String> muteMember(@Valid @RequestBody CircleMemberUpdateReq req) {
        String operatorId = SecureUtil.currentUser().getId();
        circleMemberBiz.muteMember(req, operatorId);
        return Result.OK("禁言成功");
    }

    @Operation(summary = "解除禁言")
    @PostMapping("/unmute")
    public Result<String> unmuteMember(@Valid @RequestBody CircleMemberUpdateReq req) {
        String operatorId = SecureUtil.currentUser().getId();
        circleMemberBiz.unmuteMember(req.getCircleId(), req.getTargetUserId(), operatorId);
        return Result.OK("解除禁言成功");
    }

    @Operation(summary = "移除成员")
    @PostMapping("/remove")
    public Result<String> removeMember(@Valid @RequestBody CircleMemberUpdateReq req) {
        String operatorId = SecureUtil.currentUser().getId();
        circleMemberBiz.removeMember(req, operatorId);
        return Result.OK("移除成功");
    }

    @Operation(summary = "获取圈子成员列表")
    @GetMapping("/list")
    public Result<Page<CircleMemberVO>> listMembers(
            @Parameter(description = "圈子ID", required = true) @RequestParam String circleId,
            @Parameter(description = "角色筛选: CREATOR/MODERATOR/MEMBER") @RequestParam(required = false) String role,
            @Parameter(description = "状态筛选: ACTIVE/MUTED/REMOVED") @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "50") Integer pageSize) {
        LambdaQueryWrapper<CircleMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CircleMember::getCircleId, circleId)
                .ne(CircleMember::getStatus, CircleMember.Status.REMOVED);
        if (StringUtils.hasText(role)) {
            wrapper.eq(CircleMember::getRole, role);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(CircleMember::getStatus, status);
        }
        wrapper.orderByAsc(CircleMember::getCreateTime);

        Page<CircleMember> memberPage = circleMemberService.page(new Page<>(pageNum, pageSize), wrapper);

        List<CircleMemberVO> voList = memberPage.getRecords().stream().map(m -> {
            CircleMemberVO vo = new CircleMemberVO();
            BeanUtils.copyProperties(m, vo);
            return vo;
        }).collect(Collectors.toList());

        Page<CircleMemberVO> result = new Page<>(pageNum, pageSize, memberPage.getTotal());
        result.setRecords(voList);
        return Result.OK(result);
    }
}
