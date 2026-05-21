package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 内容社区用户不感兴趣反馈。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_not_interested")
@Schema(description = "内容社区用户不感兴趣反馈")
public class ContentUserNotInterested extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "内容ID")
    private String contentId;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "关联话题或Tag")
    private String topic;

    @Schema(description = "不感兴趣原因")
    private String reason;

    @Schema(description = "反馈时间")
    private Date feedbackTime;

    @Schema(description = "反馈状态")
    private String status;
}
