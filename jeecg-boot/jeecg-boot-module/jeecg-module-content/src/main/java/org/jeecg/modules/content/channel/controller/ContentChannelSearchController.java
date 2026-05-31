package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.req.query.ChannelSearchQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelSearchService;
import org.jeecg.modules.content.channel.vo.ChannelSearchResultVO;
import org.springframework.web.bind.annotation.*;

@Tag(name = "频道搜索")
@RestController
@RequestMapping("/content/channel/search")
public class ContentChannelSearchController {

    @Resource
    private IContentChannelSearchService searchService;

    @Operation(summary = "搜索频道")
    @GetMapping("/query")
    public Result<IPage<ChannelSearchResultVO>> search(
            @RequestParam String userId,
            ChannelSearchQueryReq req) {
        return Result.OK(searchService.search(userId, req));
    }
}
