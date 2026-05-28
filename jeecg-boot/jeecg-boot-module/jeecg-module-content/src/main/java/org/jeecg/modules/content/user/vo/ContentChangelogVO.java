package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 更新日志视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "更新日志")
public class ContentChangelogVO {

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "发布日期")
    private Date releaseDate;

    @Schema(description = "新增功能")
    private List<String> additions;

    @Schema(description = "优化改进")
    private List<String> improvements;

    @Schema(description = "修复问题")
    private List<String> fixes;
}
