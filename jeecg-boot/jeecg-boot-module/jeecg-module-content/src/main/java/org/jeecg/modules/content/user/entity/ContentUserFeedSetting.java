package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 内容社区关注流设置。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_feed_setting")
@Schema(description = "内容社区关注流设置")
public class ContentUserFeedSetting extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "是否展示发布动态")
    private Boolean publishEnabled;

    @Schema(description = "是否展示点赞动态")
    private Boolean likeEnabled;

    @Schema(description = "是否展示收藏动态")
    private Boolean favoriteEnabled;

    @Schema(description = "启用动态类型列表")
    private String activityTypes;
}
