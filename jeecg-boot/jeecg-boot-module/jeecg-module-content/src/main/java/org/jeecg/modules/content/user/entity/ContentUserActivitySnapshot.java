package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 内容社区用户动态快照。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_activity_snapshot")
@Schema(description = "内容社区用户动态快照")
public class ContentUserActivitySnapshot extends JeecgEntity {

    @Schema(description = "动态用户ID")
    private String actorUserId;

    @Schema(description = "动态类型")
    private String activityType;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务ID")
    private String bizId;

    @Schema(description = "动态摘要")
    private String summary;

    @Schema(description = "动态发生时间")
    private Date activityTime;

    @Schema(description = "可见范围")
    private String visibleScope;

    @Schema(description = "快照状态")
    private String snapshotStatus;
}
