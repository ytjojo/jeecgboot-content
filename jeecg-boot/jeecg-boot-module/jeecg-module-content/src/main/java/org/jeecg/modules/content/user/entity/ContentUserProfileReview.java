package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 内容社区用户资料审核记录。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_profile_review")
@Schema(description = "内容社区用户资料审核")
public class ContentUserProfileReview extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "审核状态")
    private String reviewStatus;

    @Schema(description = "审核类型")
    private String reviewType;

    @Schema(description = "风险原因")
    private String riskReason;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "原始资料快照JSON")
    private String originalSnapshotJson;

    @Schema(description = "目标资料快照JSON")
    private String targetSnapshotJson;

    @Schema(description = "审核人")
    private String reviewedBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "审核时间")
    private Date reviewedAt;
}
