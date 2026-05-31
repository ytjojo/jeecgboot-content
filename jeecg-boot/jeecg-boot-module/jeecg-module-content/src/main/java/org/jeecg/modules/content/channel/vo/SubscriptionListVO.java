package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "订阅列表响应")
public class SubscriptionListVO {

    @Schema(description = "订阅列表")
    private List<SubscriptionVO> subscriptions;

    @Schema(description = "总数")
    private Long total;
}
