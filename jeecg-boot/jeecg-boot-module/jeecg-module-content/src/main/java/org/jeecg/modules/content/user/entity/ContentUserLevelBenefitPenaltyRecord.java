package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * Persistence model for level benefit recovery.
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_level_benefit_penalty_record")
@Schema(description = "内容社区等级权益处罚恢复记录")
public class ContentUserLevelBenefitPenaltyRecord extends JeecgEntity {

    @Schema(description = "成长处罚记录ID")
    private String penaltyRecordId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "等级权益编码")
    private String benefitCode;

    @Schema(description = "处罚前是否启用")
    private Boolean previousEnabled;

    @Schema(description = "处罚后是否启用")
    private Boolean currentEnabled;

    @Schema(description = "恢复状态")
    private String recoverStatus;

    @Schema(description = "恢复原因")
    private String recoverReason;

    @Schema(description = "恢复操作人")
    private String recoveredBy;

    @Schema(description = "恢复时间")
    private Date recoveredAt;
}
