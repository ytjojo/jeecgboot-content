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

    static String profileCacheKey(String userId) {
        return PROFILE_CACHE_PREFIX + requireSafeKeyPart(userId, "用户ID");
    }

    static String privacyCacheKey(String userId) {
        return PRIVACY_CACHE_PREFIX + requireSafeKeyPart(userId, "用户ID");
    }

    static String publicProfileCacheKey(String userId) {
        return PROFILE_PUBLIC_CACHE_PREFIX + requireSafeKeyPart(userId, "用户ID");
    }

    static String viewerProfileCacheKey(String userId, String viewerScope) {
        return PROFILE_CACHE_PREFIX + requireSafeKeyPart(userId, "用户ID") + ":" + requireViewerScope(viewerScope);
    }

    private static String requireViewerScope(String viewerScope) {
        String scope = requireSafeKeyPart(viewerScope, "查看者范围");
        if (!"PUBLIC".equals(scope) && !"FOLLOWER".equals(scope) && !"MUTUAL".equals(scope) && !"OWNER".equals(scope)) {
            throw new IllegalArgumentException("查看者范围不合法");
        }
        return scope;
    }

    private static String requireSafeKeyPart(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        String trimmed = value.trim();
        if (trimmed.length() > 64 || trimmed.contains(":")) {
            throw new IllegalArgumentException(fieldName + "不合法");
        }
        return trimmed;
    }
}
