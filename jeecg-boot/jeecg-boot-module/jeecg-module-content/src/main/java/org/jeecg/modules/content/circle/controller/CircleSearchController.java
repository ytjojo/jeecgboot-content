package org.jeecg.modules.content.circle.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.circle.biz.ICircleBiz;
import org.jeecg.modules.content.circle.req.query.CircleSearchReq;
import org.jeecg.modules.content.circle.util.CircleSecurityUtil;
import org.jeecg.modules.content.circle.vo.CircleSearchResultVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "圈子搜索", description = "圈子搜索接口")
@Validated
@RestController
@RequestMapping("/api/v1/content/circle")
public class CircleSearchController {

    @Resource
    private ICircleBiz circleBiz;

    @Operation(summary = "搜索圈子")
    @GetMapping("/search")
    public Result<Page<CircleSearchResultVO>> search(@Valid CircleSearchReq req) {
        try {
            String userId = CircleSecurityUtil.getCurrentUserIdOrNull();
            return Result.OK(circleBiz.search(req, userId));
        } catch (Exception e) {
            log.error("Circle search failed, keyword: {}", req.getKeyword(), e);
            return Result.error("搜索暂时不可用");
        }
    }
}
