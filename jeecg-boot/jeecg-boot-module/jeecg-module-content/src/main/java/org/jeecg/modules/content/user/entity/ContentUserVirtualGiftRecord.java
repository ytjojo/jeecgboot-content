package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 内容社区虚拟礼物记录实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_virtual_gift_record")
@Schema(description = "内容社区虚拟礼物记录")
public class ContentUserVirtualGiftRecord extends JeecgEntity {

    @Schema(description = "赠送人用户ID")
    private String senderUserId;

    @Schema(description = "接收人用户ID")
    private String receiverUserId;

    @Schema(description = "礼物商品ID")
    private String giftGoodsId;

    @Schema(description = "礼物编码")
    private String giftCode;

    @Schema(description = "礼物数量")
    private Integer quantity;

    @Schema(description = "消耗积分")
    private Integer pointCost;

    @Schema(description = "赠言")
    private String message;

    @Schema(description = "通知状态")
    private String notificationStatus;
}
