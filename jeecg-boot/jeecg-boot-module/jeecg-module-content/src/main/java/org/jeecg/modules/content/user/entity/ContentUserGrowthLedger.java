package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * Entity for content user growth ledger.
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_growth_ledger")
public class ContentUserGrowthLedger extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "业务ID")
    private String bizId;

    @Schema(description = "成长值变动量")
    private Integer growthDelta;

    @Schema(description = "变动后成长值")
    private Integer growthAfter;

    @Schema(description = "备注")
    private String remark;

    /**
     * Builds the current object from the given input values.
     */
    public static ContentUserGrowthLedger of(String userId, String sourceType, int growthDelta) {
        return new ContentUserGrowthLedger()
            .setUserId(userId)
            .setSourceType(sourceType)
            .setGrowthDelta(growthDelta);
    }

    /**
     * Builds the current object from the given input values.
     */
    public static ContentUserGrowthLedger of(String userId, String sourceType, String bizId, int growthDelta, String remark) {
        return of(userId, sourceType, growthDelta)
            .setBizId(bizId)
            .setRemark(remark);
    }
}
