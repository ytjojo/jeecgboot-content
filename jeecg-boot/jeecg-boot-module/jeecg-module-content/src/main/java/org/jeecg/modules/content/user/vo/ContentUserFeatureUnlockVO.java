package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 内容社区功能解锁视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区功能解锁视图")
public class ContentUserFeatureUnlockVO {

    @Schema(description = "功能编码")
    private String featureCode;

    @Schema(description = "生效时间")
    private Date validFrom;

    @Schema(description = "失效时间，空表示永久有效")
    private Date validUntil;

    @Schema(description = "是否有效")
    private Boolean enabled;
}
