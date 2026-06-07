package org.jeecg.modules.content.circle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.modules.content.circle.biz.CircleAnnouncementBizService;
import org.jeecg.modules.content.circle.entity.CircleAnnouncement;
import org.jeecg.modules.content.circle.req.CircleAnnouncementReq;
import org.jeecg.modules.content.circle.service.ICircleAnnouncementService;
import org.jeecg.modules.content.circle.vo.CircleAnnouncementVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 圈子公告控制器。
 */
@Tag(name = "圈子公告", description = "圈子公告发布与查询")
@RestController
@RequestMapping("/api/v1/content/circle/announcement")
public class CircleAnnouncementController {

    @Resource
    private CircleAnnouncementBizService circleAnnouncementBizService;

    @Resource
    private ICircleAnnouncementService circleAnnouncementService;

    @Operation(summary = "发布公告")
    @PostMapping("/")
    public Result<String> publish(@RequestBody @Valid CircleAnnouncementReq req,
                                  HttpServletRequest request) {
        String operatorId = JwtUtil.getUserNameByToken(request);
        CircleAnnouncement announcement = new CircleAnnouncement();
        BeanUtils.copyProperties(req, announcement);
        circleAnnouncementBizService.publish(announcement, operatorId);
        return Result.OK("发布成功");
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable @Parameter(description = "公告ID") String id) {
        circleAnnouncementService.removeById(id);
        return Result.OK("删除成功");
    }

    @Operation(summary = "获取圈子当前有效公告")
    @GetMapping("/active/{circleId}")
    public Result<CircleAnnouncementVO> getActive(
            @PathVariable @Parameter(description = "圈子ID") String circleId) {
        CircleAnnouncement announcement = circleAnnouncementService.getActiveByCircleId(circleId);
        if (announcement == null) {
            return Result.OK(null);
        }
        CircleAnnouncementVO vo = new CircleAnnouncementVO();
        BeanUtils.copyProperties(announcement, vo);
        return Result.OK(vo);
    }
}
