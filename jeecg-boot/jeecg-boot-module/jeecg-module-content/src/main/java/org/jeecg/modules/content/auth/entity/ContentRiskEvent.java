package org.jeecg.modules.content.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 风控事件实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_risk_event")
public class ContentRiskEvent extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "事件类型")
    private String eventType;

    @Schema(description = "风险等级 0-100")
    private Integer riskLevel;

    @Schema(description = "风险评分")
    private Integer riskScore;

    @Schema(description = "风险原因")
    private String riskReason;

    @Schema(description = "处置决策")
    private String decision;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "设备指纹")
    private String deviceFingerprint;

    @Schema(description = "User-Agent")
    private String userAgent;

    @Schema(description = "扩展数据JSON")
    private String extraDataJson;

    @Schema(description = "是否已处理")
    private Boolean resolved;

    @Schema(description = "处理人用户ID")
    private String resolvedBy;

    @Schema(description = "处理时间")
    private Date resolvedAt;

    @Schema(description = "处理备注")
    private String resolveNote;
}
