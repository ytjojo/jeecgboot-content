package org.jeecg.modules.content.circle.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.modules.content.circle.biz.CircleJoinReviewBizService;
import org.jeecg.modules.content.circle.entity.CircleJoinRequest;
import org.jeecg.modules.content.circle.req.CircleJoinReviewReq;
import org.jeecg.modules.content.circle.service.ICircleJoinReviewService;
import org.jeecg.modules.content.circle.vo.CircleJoinRequestVO;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 圈子加入申请审核控制器。
 */
@Tag(name = "圈子加入审核", description = "圈子加入申请审核与查询")
@RestController
@RequestMapping("/circle-join-review")
public class CircleJoinReviewController {

    @Resource
    private CircleJoinReviewBizService circleJoinReviewBizService;

    @Resource
    private ICircleJoinReviewService circleJoinReviewService;

    @Operation(summary = "查询加入申请列表")
    @GetMapping("/list")
    public Result<Page<CircleJoinRequest>> listRequests(
            @Parameter(description = "圈子ID", required = true) @RequestParam String circleId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        LambdaQueryWrapper<CircleJoinRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CircleJoinRequest::getCircleId, circleId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(CircleJoinRequest::getStatus, status);
        }
        wrapper.orderByDesc(CircleJoinRequest::getCreateTime);
        return Result.OK(circleJoinReviewService.page(new Page<>(current, size), wrapper));
    }

    @Operation(summary = "获取待审核申请列表")
    @GetMapping("/pending/{circleId}")
    public Result<List<CircleJoinRequestVO>> getPending(
            @PathVariable @Parameter(description = "圈子ID") String circleId) {
        List<CircleJoinRequest> requests = circleJoinReviewService.getPendingRequests(circleId);
        List<CircleJoinRequestVO> voList = requests.stream().map(req -> {
            CircleJoinRequestVO vo = new CircleJoinRequestVO();
            BeanUtils.copyProperties(req, vo);
            return vo;
        }).collect(Collectors.toList());
        return Result.OK(voList);
    }

    @Operation(summary = "批准加入申请")
    @PostMapping("/approve")
    public Result<String> approve(@RequestBody @Valid CircleJoinReviewReq req,
                                  @RequestParam @Parameter(description = "圈子ID") String circleId,
                                  HttpServletRequest request) {
        String operatorId = JwtUtil.getUserNameByToken(request);
        circleJoinReviewBizService.approve(req.getRequestId(), operatorId, circleId);
        return Result.OK("已批准");
    }

    @Operation(summary = "拒绝加入申请")
    @PostMapping("/reject")
    public Result<String> reject(@RequestBody @Valid CircleJoinReviewReq req,
                                 @RequestParam @Parameter(description = "圈子ID") String circleId,
                                 HttpServletRequest request) {
        String operatorId = JwtUtil.getUserNameByToken(request);
        circleJoinReviewBizService.reject(req.getRequestId(), operatorId, circleId, req.getRejectReason());
        return Result.OK("已拒绝");
    }
}
