package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 审计日志分页列表单项。
 */
@Data
@Accessors(chain = true)
@Schema(description = "审计日志记录")
public class ContentUserAuditLogItemVO {

    @Schema(description = "日志ID")
    private String id;

    @Schema(description = "关联用户ID")
    private String userId;

    @Schema(description = "事件类型")
    private String eventType;

    @Schema(description = "操作人ID")
    private String operatorUserId;

    @Schema(description = "事件内容")
    private String eventContent;

    @Schema(description = "额外数据JSON")
    private String extraDataJson;

    @Schema(description = "事件时间")
    private Date eventTime;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "设备信息")
    private String deviceInfo;

    @Schema(description = "创建时间")
    private Date createTime;
}
