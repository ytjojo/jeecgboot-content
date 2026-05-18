package org.jeecg.modules.content.user.constant;

/**
 * Constants for content user cache.
 */
public interface ContentUserCacheConstant {

    /** 用户资料缓存前缀。 */
    String PROFILE_CACHE_PREFIX = "content:user:profile:";

    /** 用户隐私设置缓存前缀。 */
    String PRIVACY_CACHE_PREFIX = "content:user:privacy:";

    /** 用户关系缓存前缀。 */
    String RELATION_CACHE_PREFIX = "content:user:relation:";

    /** 用户可见性判定缓存前缀。 */
    String VISIBILITY_CACHE_PREFIX = "content:user:visibility:";

    /** 用户资料修改次数缓存前缀。 */
    String PROFILE_UPDATE_COUNT_PREFIX = "content:user:profile:update_count:";

    /** 用户隐私修改次数缓存前缀。 */
    String PRIVACY_UPDATE_COUNT_PREFIX = "content:user:privacy:update_count:";

    /** 用户公共资料缓存前缀。 */
    String PROFILE_PUBLIC_CACHE_PREFIX = "content:user:profile:public:";

    /** 用户奖励每日上限计数缓存前缀。 */
    String GROWTH_DAILY_CAP_PREFIX = "content:growth:daily_cap:";

    /** 用户奖励事件短期处理锁缓存前缀。 */
    String GROWTH_EVENT_LOCK_PREFIX = "content:growth:event_lock:";

    /** 用户勋章进度缓存前缀。 */
    String BADGE_PROGRESS_PREFIX = "content:growth:badge_progress:";
}
