package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.entity.ContentUserHomepageModule;

/**
 * 内容社区主页模块视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区主页模块视图")
public class ContentUserHomepageModuleVO {

    @Schema(description = "模块编码")
    private String moduleKey;

    @Schema(description = "模块名称")
    private String moduleName;

    @Schema(description = "是否展示")
    private Boolean visible;

    @Schema(description = "排序号")
    private Integer sortOrder;

    public static ContentUserHomepageModuleVO from(ContentUserHomepageModule module) {
        return new ContentUserHomepageModuleVO()
            .setModuleKey(module.getModuleKey())
            .setModuleName(module.getModuleName())
            .setVisible(module.getVisible())
            .setSortOrder(module.getSortOrder());
    }
}
