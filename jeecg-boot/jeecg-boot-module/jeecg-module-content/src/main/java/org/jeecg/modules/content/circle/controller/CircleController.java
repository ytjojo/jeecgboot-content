package org.jeecg.modules.content.circle.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.req.create.CircleCreateReq;
import org.jeecg.modules.content.circle.req.create.CircleJoinReq;
import org.jeecg.modules.content.circle.req.create.CircleLeaveReq;
import org.jeecg.modules.content.circle.req.update.CircleUpdateReq;
import org.jeecg.modules.content.circle.service.ICircleMemberService;
import org.jeecg.modules.content.circle.service.ICircleService;
import org.jeecg.modules.content.circle.vo.CircleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public Result<String> leaveCircle(@Valid @RequestBody CircleLeaveReq req) {
        String userId = SecureUtil.currentUser().getId();
        circleMemberBiz.leaveCircle(req.getCircleId(), userId);
        return Result.OK("退出成功");
    }

    @Operation(summary = "获取我的圈子列表")
    @GetMapping("/my-list")
    public Result<Page<CircleVO>> myList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        String userId = SecureUtil.currentUser().getId();
        LambdaQueryWrapper<CircleMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(CircleMember::getUserId, userId)
                .in(CircleMember::getStatus, CircleMember.Status.ACTIVE, CircleMember.Status.MUTED)
                .orderByDesc(CircleMember::getCreateTime);
        Page<CircleMember> memberPage = circleMemberService.page(new Page<>(pageNum, pageSize), memberWrapper);

        List<String> circleIds = memberPage.getRecords().stream()
                .map(CircleMember::getCircleId).collect(Collectors.toList());

        List<CircleVO> voList = new ArrayList<>();
        if (!circleIds.isEmpty()) {
            List<Circle> circles = circleService.listByIds(circleIds);
            Map<String, Circle> circleMap = circles.stream()
                    .collect(Collectors.toMap(Circle::getId, c -> c));
            for (CircleMember member : memberPage.getRecords()) {
                Circle circle = circleMap.get(member.getCircleId());
                if (circle != null) {
                    CircleVO vo = new CircleVO();
                    BeanUtils.copyProperties(circle, vo);
                    vo.setJoined(true);
                    vo.setMyRole(member.getRole().name());
                    voList.add(vo);
                }
            }
        }

        Page<CircleVO> result = new Page<>(pageNum, pageSize, memberPage.getTotal());
        result.setRecords(voList);
        return Result.OK(result);
    }

    @Operation(summary = "获取公开圈子列表")
    @GetMapping("/public-list")
    public Result<Page<CircleVO>> publicList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        LambdaQueryWrapper<Circle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Circle::getPrivacyType, Circle.PrivacyType.PUBLIC)
                .eq(Circle::getStatus, Circle.Status.ACTIVE)
                .orderByDesc(Circle::getMemberCount);

        Page<Circle> page = circleService.page(new Page<>(pageNum, pageSize), wrapper);

        String userId = SecureUtil.currentUser().getId();
        List<CircleVO> voList = page.getRecords().stream().map(c -> {
            CircleVO vo = new CircleVO();
            BeanUtils.copyProperties(c, vo);
            CircleMember member = circleMemberService.findByCircleAndUser(c.getId(), userId);
            vo.setJoined(member != null && member.getStatus() == CircleMember.Status.ACTIVE);
            vo.setMyRole(member != null ? member.getRole().name() : null);
            return vo;
        }).collect(Collectors.toList());

        Page<CircleVO> result = new Page<>(pageNum, pageSize, page.getTotal());
        result.setRecords(voList);
        return Result.OK(result);
    }

    @Operation(summary = "获取圈子详情(PATH参数)")
    @GetMapping("/{id}")
    public Result<CircleVO> getDetailByPath(
            @Parameter(description = "圈子ID", required = true) @PathVariable String id) {
        Circle circle = circleService.getById(id);
        if (circle == null) {
            return Result.error("圈子不存在");
        }
        CircleVO vo = new CircleVO();
        BeanUtils.copyProperties(circle, vo);
        String userId = SecureUtil.currentUser().getId();
        CircleMember member = circleMemberService.findByCircleAndUser(id, userId);
        vo.setJoined(member != null && member.getStatus() == CircleMember.Status.ACTIVE);
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
