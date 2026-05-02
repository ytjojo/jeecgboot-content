package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * Persistence model for growth penalty recovery.
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_growth_penalty_record")
@Schema(description = "内容社区成长处罚恢复记录")
public class ContentUserGrowthPenaltyRecord extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "治理状态记录ID")
    private String governanceRecordId;

    @Schema(description = "关联申诉ID")
    private String appealId;

    @Schema(description = "处罚来源类型")
    private String sourceType;

    @Schema(description = "处罚来源业务主键")
    private String sourceId;

    @Schema(description = "处罚来源状态快照")
    private String sourceStatus;

    @Schema(description = "处罚类型")
    private String penaltyType;

    @Schema(description = "处罚影响快照JSON")
    private String effectSnapshotJson;

    @Schema(description = "恢复状态")
    private String status;

    @Schema(description = "恢复触发来源")
    private String recoverTrigger;

    @Schema(description = "恢复原因")
    private String recoverReason;

    @Schema(description = "恢复操作人")
    private String recoveredBy;

    @Schema(description = "恢复时间")
    private Date recoveredAt;
}
