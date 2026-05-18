package org.jeecg.modules.content.user.req.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 主页个性化更新请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区主页个性化更新请求")
public class ContentUserHomepageUpdateReq {

    @Size(max = 500, message = "主页背景图长度不能超过500位")
    @Schema(description = "主页背景图")
    private String homepageBackground;

    @Size(max = 32, message = "主题色长度不能超过32位")
    @Schema(description = "主题色")
    private String themeColor;

    @Valid
    @Schema(description = "模块配置")
    private List<ContentUserHomepageModuleReq> modules;
}
