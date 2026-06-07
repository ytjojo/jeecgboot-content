package org.jeecg.modules.content.circle.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.req.query.CircleSearchReq;
import org.jeecg.modules.content.circle.service.ICircleService;
import org.jeecg.modules.content.circle.vo.CircleSearchResultVO;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "圈子搜索", description = "圈子搜索接口")
@Validated
@RestController
@RequestMapping("/api/v1/content/circle")
public class CircleSearchController {

    @Resource
    private ICircleService circleService;

    @Operation(summary = "搜索圈子")
    @GetMapping("/search")
    public Result<List<CircleSearchResultVO>> search(CircleSearchReq req) {
        LambdaQueryWrapper<Circle> wrapper = new LambdaQueryWrapper<Circle>()
                .eq(Circle::getStatus, Circle.Status.ACTIVE)
                .eq(Circle::getPrivacyType, Circle.PrivacyType.PUBLIC);

        if (StringUtils.hasText(req.getKeyword())) {
            String keyword = req.getKeyword()
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
            wrapper.and(w -> w
                    .like(Circle::getName, keyword)
                    .or()
                    .like(Circle::getDescription, keyword));
        }

        wrapper.orderByDesc(Circle::getMemberCount);

        Page<Circle> page = new Page<>(req.getPageNum(), req.getPageSize());
        circleService.page(page, wrapper);

        List<CircleSearchResultVO> results = page.getRecords().stream().map(c -> {
            CircleSearchResultVO vo = new CircleSearchResultVO();
            vo.setId(c.getId());
            vo.setName(c.getName());
            vo.setIconUrl(c.getIconUrl());
            vo.setDescription(c.getDescription());
            vo.setMemberCount(c.getMemberCount());
            vo.setJoined(false); // 需要结合当前用户判断，MVP 阶段默认 false
            return vo;
        }).toList();

        return Result.OK(results);
    }
}
