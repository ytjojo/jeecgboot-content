package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 内容社区用户积分台账实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_point_ledger")
@Schema(description = "内容社区用户积分台账")
public class ContentUserPointLedger extends JeecgEntity {

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

    @Schema(description = "积分变动量")
    private Integer pointDelta;

    @Schema(description = "变动后积分余额")
    private Integer balanceAfter;

    @Schema(description = "每日统计桶")
    private String dailyBucket;

    @Schema(description = "备注")
    private String remark;

    /**
     * 根据基础积分变动参数创建台账对象。
     */
    public static ContentUserPointLedger of(String userId, String sourceType, int pointDelta) {
        return new ContentUserPointLedger()
            .setUserId(userId)
            .setSourceType(sourceType)
            .setPointDelta(pointDelta);
    }

    /**
     * 根据业务来源和备注创建台账对象。
     */
    public static ContentUserPointLedger of(String userId, String sourceType, String bizId, int pointDelta, String remark) {
        return of(userId, sourceType, pointDelta)
            .setBizId(bizId)
            .setRemark(remark);
    }
}
