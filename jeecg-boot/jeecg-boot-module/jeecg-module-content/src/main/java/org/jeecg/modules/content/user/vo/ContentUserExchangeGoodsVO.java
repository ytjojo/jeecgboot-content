package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区积分兑换商品视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区积分兑换商品视图")
public class ContentUserExchangeGoodsVO {

    @Schema(description = "商品ID")
    private String goodsId;

    @Schema(description = "商品编码")
    private String goodsCode;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "商品类型")
    private String goodsType;

    @Schema(description = "积分价格")
    private Integer pointPrice;

    @Schema(description = "库存数量")
    private Integer stockQuantity;
}
