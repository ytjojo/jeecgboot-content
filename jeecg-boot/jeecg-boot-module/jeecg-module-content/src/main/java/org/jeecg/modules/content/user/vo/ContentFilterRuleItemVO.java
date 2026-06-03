package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 内容社区用户屏蔽规则项。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户屏蔽规则项")
public class ContentFilterRuleItemVO {

    @Schema(description = "规则ID")
    private String id;

    @Schema(description = "规则类型")
    private String ruleType;

    @Schema(description = "规则值")
    private String ruleValue;

    @Schema(description = "匹配范围")
    private String matchScope;

    @Schema(description = "过期时间")
    private Date expiresAt;

    @Schema(description = "规则状态")
    private String status;

    @Schema(description = "创建时间")
    private Date createTime;
}
