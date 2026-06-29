package org.jeecg.modules.content.circle.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.biz.ICircleBiz;
import org.jeecg.modules.content.circle.biz.ICircleMemberBiz;
import org.jeecg.modules.content.circle.req.create.CircleCreateReq;
import org.jeecg.modules.content.circle.req.create.CircleJoinReq;
import org.jeecg.modules.content.circle.req.create.CircleLeaveReq;
import org.jeecg.modules.content.circle.req.update.CircleUpdateReq;
import org.jeecg.modules.content.circle.util.CircleSecurityUtil;
import org.jeecg.modules.content.circle.vo.CircleVO;
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

    @Operation(summary = "创建圈子")
    @PostMapping("/create")
    public Result<CircleVO> createCircle(@Valid @RequestBody CircleCreateReq req) {
        String userId = CircleSecurityUtil.getCurrentUserIdOrThrow();
        return Result.OK(circleBiz.createCircle(req, userId));
    }

    @Operation(summary = "更新圈子信息")
    @PutMapping("/update")
    public Result<String> updateCircle(@Valid @RequestBody CircleUpdateReq req) {
        String userId = CircleSecurityUtil.getCurrentUserIdOrThrow();
        circleBiz.updateCircle(req, userId);
        return Result.OK("更新成功");
    }

    @Operation(summary = "加入圈子")
    @PostMapping("/join")
    public Result<String> joinCircle(@Valid @RequestBody CircleJoinReq req) {
        String userId = CircleSecurityUtil.getCurrentUserIdOrThrow();
        circleMemberBiz.joinCircle(req, userId);
        return Result.OK("加入成功");
    }

    @Operation(summary = "退出圈子")
    @PostMapping("/leave")
    public Result<String> leaveCircle(@Valid @RequestBody CircleLeaveReq req) {
        String userId = CircleSecurityUtil.getCurrentUserIdOrThrow();
        circleMemberBiz.leaveCircle(req.getCircleId(), userId);
        return Result.OK("退出成功");
    }

    @Operation(summary = "获取我的圈子列表")
    @GetMapping("/my-list")
    public Result<Page<CircleVO>> myList(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为1") Integer pageNum,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "每页最少1条") @Max(value = 100, message = "每页最多100条") Integer pageSize) {
        String userId = CircleSecurityUtil.getCurrentUserIdOrThrow();
        return Result.OK(circleBiz.myList(pageNum, pageSize, userId));
    }

    @Operation(summary = "获取公开圈子列表")
    @GetMapping("/public-list")
    public Result<Page<CircleVO>> publicList(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为1") Integer pageNum,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "每页最少1条") @Max(value = 100, message = "每页最多100条") Integer pageSize) {
        String userId = CircleSecurityUtil.getCurrentUserIdOrNull();
        return Result.OK(circleBiz.publicList(pageNum, pageSize, userId));
    }

    @Operation(summary = "获取圈子详情(PATH参数)")
    @GetMapping("/{id}")
    public Result<CircleVO> getDetailByPath(
            @Parameter(description = "圈子ID", required = true) @PathVariable String id) {
        String userId = CircleSecurityUtil.getCurrentUserIdOrNull();
        return Result.OK(circleBiz.getDetail(id, userId));
    }

    @Operation(summary = "检查圈子名称是否可用")
    @GetMapping("/check-name")
    public Result<Boolean> checkName(@Parameter(description = "圈子名称", required = true) @RequestParam String name) {
        try {
            boolean available = circleBiz.checkNameAvailable(name);
            return Result.OK(available);
        } catch (JeecgBootException e) {
            return Result.error(500, "检查名称时发生错误: " + e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "检查名称时发生系统错误");
        }
    }
}
