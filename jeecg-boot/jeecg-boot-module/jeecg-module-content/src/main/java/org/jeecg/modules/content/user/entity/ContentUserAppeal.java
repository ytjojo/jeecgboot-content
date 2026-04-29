package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecg.modules.content.user.req.support.ContentAppealCreateReq;

/**
 * Entity for content user appeal.
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_appeal")
public class ContentUserAppeal extends JeecgEntity {

    @Schema(description = "申诉用户ID")
    private String userId;

    @Schema(description = "申诉类型")
    private String appealType;

    @Schema(description = "申诉目标ID")
    private String targetId;

    @Schema(description = "申诉目标类型")
    private String targetType;

    @Schema(description = "申诉状态")
    private String status;

    @Schema(description = "申诉原因")
    private String reason;

    @Schema(description = "申诉证据JSON")
    private String evidenceJson;

    @Schema(description = "处理进度说明")
    private String progressNote;

    /**
     * Builds the current object from the given request or entity.
     */
    public static ContentUserAppeal from(ContentAppealCreateReq req) {
        return new ContentUserAppeal()
            .setUserId(req.getUserId())
            .setAppealType(req.getAppealType())
            .setTargetId(req.getTargetId())
            .setTargetType(req.getTargetType())
            .setReason(req.getReason())
            .setEvidenceJson(req.getEvidenceJson())
            .setStatus("PENDING");
    }
}
