package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区积分消耗结果。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区积分消耗结果")
public class ContentUserPointSpendResultVO {

    @Schema(description = "订单ID")
    private String orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "商品ID")
    private String goodsId;

    @Schema(description = "商品编码")
    private String goodsCode;

    @Schema(description = "兑换数量")
    private Integer quantity;

    @Schema(description = "消耗积分")
    private Integer pointCost;

    @Schema(description = "变动后积分余额")
    private Integer balanceAfter;

    @Schema(description = "权益发放状态")
    private String benefitStatus;

    @Schema(description = "是否重复使用已有解锁")
    private Boolean reusedUnlock;

    @Schema(description = "是否触发等级变更")
    private Boolean levelChanged;

    @Schema(description = "变更后的等级，仅 levelChanged=true 时有值")
    private Integer newLevel;
}
