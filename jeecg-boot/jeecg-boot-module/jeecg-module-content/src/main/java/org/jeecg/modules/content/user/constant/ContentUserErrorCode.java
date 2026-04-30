package org.jeecg.modules.content.user.constant;

/**
 * Constants for content user error code.
 */
public interface ContentUserErrorCode {

    /** 用户不存在。 */
    int USER_NOT_FOUND = 5404;

    /** 用户关系不允许执行当前操作。 */
    int RELATION_FORBIDDEN = 5409;

    /** 用户状态流转不合法。 */
    int STATUS_TRANSITION_INVALID = 5410;

    /** 可见性校验不通过。 */
    int VISIBILITY_DENIED = 5411;
}
