package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.req.query.ChannelBrowseQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelVisibilityService;
import org.jeecg.modules.content.channel.vo.ChannelBrowseItemVO;
import org.springframework.web.bind.annotation.*;

@Tag(name = "频道分类浏览")
@RestController
@RequestMapping("/content/channel/browse")
public class ContentChannelBrowseController {

    @Resource
    private IContentChannelVisibilityService visibilityService;

    @Operation(summary = "按分类浏览频道")
    @GetMapping("/category")
    public Result<IPage<ChannelBrowseItemVO>> browseByCategory(ChannelBrowseQueryReq req) {
        return Result.OK();
    }
}
