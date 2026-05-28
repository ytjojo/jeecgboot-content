package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 客服会话实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_customer_service_session")
@Schema(description = "客服会话")
public class ContentCustomerServiceSession extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "会话类型")
    private String sessionType;

    @Schema(description = "会话状态")
    private String status;

    @Schema(description = "评分")
    private Integer rating;

    @Schema(description = "评分内容")
    private String ratingComment;

    @Schema(description = "开始时间")
    private Date startTime;

    @Schema(description = "结束时间")
    private Date endTime;
}
