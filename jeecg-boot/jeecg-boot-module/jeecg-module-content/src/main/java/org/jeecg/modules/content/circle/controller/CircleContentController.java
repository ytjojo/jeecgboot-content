package org.jeecg.modules.content.circle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.circle.biz.CircleContentBizService;
import org.jeecg.modules.content.circle.vo.CircleContentVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 圈子内容控制器。
 * 提供帖子列表查询，携带作者佩戴的勋章信息。
 */
@Tag(name = "圈子内容", description = "帖子列表与详情查询")
@RestController
@RequestMapping("/content/circle")
public class CircleContentController {

    @Resource
    private CircleContentBizService circleContentBizService;

    @Operation(summary = "查询圈子帖子列表（含作者勋章）")
    @GetMapping("/{circleId}/posts")
    public Result<List<CircleContentVO>> listPosts(
            @PathVariable @Parameter(description = "圈子ID") String circleId) {
        List<CircleContentVO> posts = circleContentBizService.listPostsWithAuthorBadges(circleId);
        return Result.OK(posts);
    }
}
