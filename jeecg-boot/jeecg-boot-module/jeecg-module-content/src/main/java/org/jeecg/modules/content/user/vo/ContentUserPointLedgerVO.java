package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 内容社区积分明细视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区积分明细视图")
public class ContentUserPointLedgerVO {

    @Schema(description = "台账ID")
    private String id;

    @Schema(description = "积分变动量")
    private Integer pointDelta;

    @Schema(description = "变动后积分余额")
    private Integer balanceAfter;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "来源说明")
    private String sourceDescription;

    @Schema(description = "业务ID")
    private String bizId;

    @Schema(description = "创建时间")
    private Date createTime;
}
