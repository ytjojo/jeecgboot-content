package org.jeecg.modules.content.circle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.config.security.utils.SecureUtil;
import org.jeecg.modules.content.circle.biz.ICircleBiz;
import org.jeecg.modules.content.circle.biz.ICircleMemberBiz;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.req.create.CircleCreateReq;
import org.jeecg.modules.content.circle.req.create.CircleJoinReq;
import org.jeecg.modules.content.circle.req.update.CircleUpdateReq;
import org.jeecg.modules.content.circle.service.ICircleMemberService;
import org.jeecg.modules.content.circle.service.ICircleService;
import org.jeecg.modules.content.circle.vo.CircleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "圈子管理", description = "圈子创建、更新、查询等接口")
@Validated
@RestController
@RequestMapping("/api/v1/content/circle")
public class CircleController {

    @Resource
    private ICircleBiz circleBiz;

    @Resource
    private ICircleMemberBiz circleMemberBiz;

    @Resource
    private ICircleService circleService;

    @Resource
    private ICircleMemberService circleMemberService;

    @Operation(summary = "创建圈子")
    @PostMapping("/create")
    public Result<CircleVO> createCircle(@Valid @RequestBody CircleCreateReq req) {
        String userId = SecureUtil.currentUser().getId();
        return Result.OK(circleBiz.createCircle(req, userId));
    }

    @Operation(summary = "更新圈子信息")
    @PutMapping("/update")
    public Result<String> updateCircle(@Valid @RequestBody CircleUpdateReq req) {
        String userId = SecureUtil.currentUser().getId();
        circleBiz.updateCircle(req, userId);
        return Result.OK("更新成功");
    }

    @Operation(summary = "加入圈子")
    @PostMapping("/join")
    public Result<String> joinCircle(@Valid @RequestBody CircleJoinReq req) {
        String userId = SecureUtil.currentUser().getId();
        circleMemberBiz.joinCircle(req, userId);
        return Result.OK("加入成功");
    }

    @Operation(summary = "退出圈子")
    @PostMapping("/leave")
    public Result<String> leaveCircle(@RequestParam String circleId) {
        String userId = SecureUtil.currentUser().getId();
        circleMemberBiz.leaveCircle(circleId, userId);
        return Result.OK("退出成功");
    }

    @Operation(summary = "获取圈子详情")
    @GetMapping("/detail")
    public Result<CircleVO> getDetail(
            @Parameter(description = "圈子ID", required = true) @RequestParam String id) {
        Circle circle = circleService.getById(id);
        if (circle == null) {
            return Result.error("圈子不存在");
        }
        CircleVO vo = new CircleVO();
        BeanUtils.copyProperties(circle, vo);
        String userId = SecureUtil.currentUser().getId();
        org.jeecg.modules.content.circle.entity.CircleMember member =
                circleMemberService.findByCircleAndUser(id, userId);
        vo.setJoined(member != null && member.getStatus() == org.jeecg.modules.content.circle.entity.CircleMember.Status.ACTIVE);
        vo.setMyRole(member != null ? member.getRole().name() : null);
        return Result.OK(vo);
    }

    @Operation(summary = "检查圈子名称是否可用")
    @GetMapping("/check-name")
    public Result<Boolean> checkName(@Parameter(description = "圈子名称", required = true) @RequestParam String name) {
        try {
            circleService.checkNameUnique(name);
            return Result.OK(true);
        } catch (JeecgBootException e) {
            return Result.OK(false);
        }
    }
}
