package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 内容社区积分兑换订单实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_exchange_order")
@Schema(description = "内容社区积分兑换订单")
public class ContentUserExchangeOrder extends JeecgEntity {

    @Schema(description = "兑换订单号")
    private String orderNo;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "商品ID")
    private String goodsId;

    @Schema(description = "商品编码")
    private String goodsCode;

    @Schema(description = "兑换数量")
    private Integer quantity;

    @Schema(description = "消耗积分")
    private Integer pointCost;

    @Schema(description = "订单状态")
    private String orderStatus;

    @Schema(description = "权益发放状态")
    private String benefitStatus;

    @Schema(description = "失败原因")
    private String failureReason;

    @Schema(description = "请求幂等ID")
    private String requestId;
}
