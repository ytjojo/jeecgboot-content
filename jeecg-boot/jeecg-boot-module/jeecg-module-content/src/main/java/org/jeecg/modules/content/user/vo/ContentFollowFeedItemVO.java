package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.entity.ContentUserActivitySnapshot;
import org.jeecg.modules.content.user.entity.ContentUserRelation;

import java.util.Date;

/**
 * 关注流动态条目。
 */
@Data
@Accessors(chain = true)
@Schema(description = "关注流动态条目")
public class ContentFollowFeedItemVO {

    @Schema(description = "动态快照ID")
    private String snapshotId;

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

    @Schema(description = "动态时间")
    private Date activityTime;

    @Schema(description = "是否特别关注")
    private Boolean specialFollow;

    public static ContentFollowFeedItemVO from(ContentUserActivitySnapshot snapshot, ContentUserRelation relation) {
        return new ContentFollowFeedItemVO()
            .setSnapshotId(snapshot.getId())
            .setActorUserId(snapshot.getActorUserId())
            .setActivityType(snapshot.getActivityType())
            .setBizType(snapshot.getBizType())
            .setBizId(snapshot.getBizId())
            .setSummary(snapshot.getSummary())
            .setActivityTime(snapshot.getActivityTime())
            .setSpecialFollow(relation != null && Boolean.TRUE.equals(relation.getSpecialFollow()));
    }
}
