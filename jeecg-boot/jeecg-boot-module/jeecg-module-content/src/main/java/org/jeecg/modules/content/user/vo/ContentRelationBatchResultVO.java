package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 内容社区关系批量操作结果。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区关系批量操作结果")
public class ContentRelationBatchResultVO {

    @Schema(description = "成功数量")
    private Integer successCount = 0;

    @Schema(description = "失败数量")
    private Integer failureCount = 0;

    @Schema(description = "失败明细")
    private List<FailureItem> failures = new ArrayList<>();

    /**
     * 记录一个成功项。
     */
    public void addSuccess() {
        this.successCount = this.successCount + 1;
    }

    /**
     * 记录一个失败项。
     */
    public void addFailure(String targetUserId, String reason) {
        this.failureCount = this.failureCount + 1;
        this.failures.add(new FailureItem().setTargetUserId(targetUserId).setReason(reason));
    }

    /**
     * 内容社区关系批量操作失败项。
     */
    @Data
    @Accessors(chain = true)
    @Schema(description = "内容社区关系批量操作失败项")
    public static class FailureItem {

        @Schema(description = "目标用户ID")
        private String targetUserId;

        @Schema(description = "失败原因")
        private String reason;
    }
}
