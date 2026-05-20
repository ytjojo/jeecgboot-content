package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量订阅操作结果。
 */
@Data
@Accessors(chain = true)
@Schema(description = "批量订阅操作结果")
public class ContentSubscriptionBatchResultVO {

    @Schema(description = "成功数量")
    private int successCount;

    @Schema(description = "失败数量")
    private int failureCount;

    @Schema(description = "失败明细")
    private List<Failure> failures = new ArrayList<>();

    public void addSuccess() {
        successCount++;
    }

    public void addFailure(String subscriptionId, String reason) {
        failureCount++;
        failures.add(new Failure().setSubscriptionId(subscriptionId).setReason(reason));
    }

    @Data
    @Accessors(chain = true)
    @Schema(description = "批量订阅失败明细")
    public static class Failure {

        @Schema(description = "订阅ID")
        private String subscriptionId;

        @Schema(description = "失败原因")
        private String reason;
    }
}
