package org.jeecg.modules.content.user.req.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 关注流动态类型设置请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "关注流动态类型设置请求")
public class ContentFeedSettingUpdateReq {

    @Schema(description = "启用的动态类型，支持 PUBLISH、LIKE、FAVORITE")
    private List<String> activityTypes;
}
