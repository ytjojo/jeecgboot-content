package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 帮助文章搜索结果视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "帮助文章搜索结果")
public class ContentHelpSearchResultVO {

    @Schema(description = "文章标识")
    private String code;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章说明")
    private String description;

    @Schema(description = "匹配摘要")
    private String snippet;
}
