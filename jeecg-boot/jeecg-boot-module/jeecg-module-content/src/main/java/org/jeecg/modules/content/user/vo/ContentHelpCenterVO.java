package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * View object for help center metadata.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区帮助中心视图")
public class ContentHelpCenterVO {

    @Schema(description = "常见问题分类", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private List<String> faqCategories;

    @Schema(description = "使用指南入口", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private List<String> guideEntries;

    @Schema(description = "功能更新日志入口", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private List<String> releaseNotes;
}
