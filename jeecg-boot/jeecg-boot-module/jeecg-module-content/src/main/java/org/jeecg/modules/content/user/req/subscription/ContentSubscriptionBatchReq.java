package org.jeecg.modules.content.user.req.subscription;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 批量订阅操作请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "批量订阅操作请求")
public class ContentSubscriptionBatchReq {

    @Schema(description = "订阅ID列表")
    private List<String> subscriptionIds;
}
