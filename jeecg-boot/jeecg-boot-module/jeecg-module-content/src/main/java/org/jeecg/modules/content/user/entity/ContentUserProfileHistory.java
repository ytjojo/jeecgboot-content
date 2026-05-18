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
 * 内容社区用户昵称和头像历史。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_profile_history")
@Schema(description = "内容社区用户资料历史")
public class ContentUserProfileHistory extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "历史类型")
    private String historyType;

    @Schema(description = "历史值")
    private String historyValue;

    @Schema(description = "来源审核ID")
    private String sourceReviewId;

    @Schema(description = "来源更新ID")
    private String sourceUpdateId;

    @Schema(description = "是否过期")
    private Boolean expired;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "过期时间")
    private Date expiresAt;
}
