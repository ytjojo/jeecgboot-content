package org.jeecg.modules.content.user.constant;

import java.util.Set;

/**
 * 内容社区奖励来源类型常量。
 */
public interface ContentUserRewardSourceTypeConstant {

    /** 首次登录。 */
    String FIRST_LOGIN = "FIRST_LOGIN";

    /** 浏览满十分钟。 */
    String BROWSE_10_MIN = "BROWSE_10_MIN";

    /** 点赞行为。 */
    String LIKE = "LIKE";

    /** 分享行为。 */
    String SHARE = "SHARE";

    /** 评论行为。 */
    String COMMENT = "COMMENT";

    /** 发布内容。 */
    String CONTENT_PUBLISH = "CONTENT_PUBLISH";

    /** 获得编辑推荐。 */
    String EDITOR_RECOMMEND = "EDITOR_RECOMMEND";

    /** 内容加精。 */
    String FEATURED = "FEATURED";

    /** 被转发。 */
    String REPOST = "REPOST";

    /** 被关注。 */
    String FOLLOWED = "FOLLOWED";

    /** 邀请注册。 */
    String INVITE_REGISTER = "INVITE_REGISTER";

    /** 完成新手任务。 */
    String NEWCOMER_TASK = "NEWCOMER_TASK";

    /** 完成每日任务。 */
    String DAILY_TASK = "DAILY_TASK";

    /** 完成活动任务。 */
    String ACTIVITY_TASK = "ACTIVITY_TASK";

    /** 当前奖励体系支持的来源类型集合。 */
    Set<String> SUPPORTED_TYPES = Set.of(
        FIRST_LOGIN,
        BROWSE_10_MIN,
        LIKE,
        SHARE,
        COMMENT,
        CONTENT_PUBLISH,
        EDITOR_RECOMMEND,
        FEATURED,
        REPOST,
        FOLLOWED,
        INVITE_REGISTER,
        NEWCOMER_TASK,
        DAILY_TASK,
        ACTIVITY_TASK
    );
}
