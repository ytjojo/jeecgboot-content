package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 内容社区用户主页模块配置。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_homepage_module")
@Schema(description = "内容社区用户主页模块配置")
public class ContentUserHomepageModule extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "模块编码")
    private String moduleKey;

    @Schema(description = "模块名称")
    private String moduleName;

    @Schema(description = "是否展示")
    private Boolean visible;

    @Schema(description = "排序号")
    private Integer sortOrder;
}
