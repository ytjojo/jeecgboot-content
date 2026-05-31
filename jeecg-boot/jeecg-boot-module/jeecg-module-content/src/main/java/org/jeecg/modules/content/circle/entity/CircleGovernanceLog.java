package org.jeecg.modules.content.circle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("content_circle_governance_log")
public class CircleGovernanceLog {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "操作者用户ID")
    private String operatorId;

    @Schema(description = "目标用户ID")
    private String targetUserId;

    @Schema(description = "动作: MUTE/UNMUTE/REMOVE/ROLE_CHANGE")
    private Action action;

    @Schema(description = "操作原因")
    private String reason;

    @Schema(description = "禁言时长(如: 1h/24h/7d/PERMANENT)")
    private String duration;

    @Schema(description = "额外数据JSON")
    private String extraDataJson;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    public enum Action {
        MUTE, UNMUTE, REMOVE, ROLE_CHANGE
    }
}
