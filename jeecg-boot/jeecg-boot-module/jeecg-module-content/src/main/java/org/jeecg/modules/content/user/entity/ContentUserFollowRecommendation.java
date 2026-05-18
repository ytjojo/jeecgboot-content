package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 内容社区关注推荐。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_follow_recommendation")
@Schema(description = "内容社区关注推荐")
public class ContentUserFollowRecommendation extends JeecgEntity {

    @Schema(description = "被推荐用户ID")
    private String userId;

    @Schema(description = "推荐关注用户ID")
    private String targetUserId;

    @Schema(description = "推荐规则")
    private String recommendationRule;

    @Schema(description = "推荐理由")
    private String recommendationReason;

    @Schema(description = "排序分数")
    private BigDecimal rankingScore;

    @Schema(description = "推荐状态")
    private String recommendationStatus;

    @Schema(description = "过期时间")
    private Date expiresAt;
}
