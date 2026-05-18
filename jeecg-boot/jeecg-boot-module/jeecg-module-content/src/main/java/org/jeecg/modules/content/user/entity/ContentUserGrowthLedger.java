package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 内容社区用户成长值台账实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_growth_ledger")
@Schema(description = "内容社区用户成长值台账")
public class ContentUserGrowthLedger extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "来源说明")
    private String sourceDescription;

    @Schema(description = "业务ID")
    private String bizId;

    @Schema(description = "奖励事件ID")
    private String eventId;

    @Schema(description = "规则快照JSON")
    private String ruleSnapshotJson;

    @Schema(description = "成长值变动量")
    private Integer growthDelta;

    @Schema(description = "变动后成长值")
    private Integer growthAfter;

    @Schema(description = "备注")
    private String remark;

    /**
     * 根据基础成长值变动参数创建台账对象。
     */
    public static ContentUserGrowthLedger of(String userId, String sourceType, int growthDelta) {
        return new ContentUserGrowthLedger()
            .setUserId(userId)
            .setSourceType(sourceType)
            .setGrowthDelta(growthDelta);
    }

    /**
     * 根据业务来源和备注创建台账对象。
     */
    public static ContentUserGrowthLedger of(String userId, String sourceType, String bizId, int growthDelta, String remark) {
        return of(userId, sourceType, growthDelta)
            .setBizId(bizId)
            .setRemark(remark);
    }
}
