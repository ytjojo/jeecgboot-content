package org.jeecg.modules.content.userstatus.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.userstatus.annotation.CheckUserStatus;
import org.jeecg.modules.content.userstatus.entity.UserStatusEnum;
import org.jeecg.modules.content.userstatus.model.UserRestriction;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户状态检查 AOP 切面。
 * 统一检查用户状态，执行功能限制策略。
 */
@Aspect
@Component
public class UserStatusCheckAspect {

    /**
     * 检查用户状态
     *
     * @param joinPoint  切入点
     * @param annotation 注解
     * @param userStatus 用户当前状态
     * @return 方法执行结果
     * @throws Throwable 如果状态不允许访问
     */
    @Around("@annotation(annotation) && args(userStatus, ..)")
    public Object checkUserStatus(ProceedingJoinPoint joinPoint, CheckUserStatus annotation, UserStatusEnum userStatus) throws Throwable {
        if (userStatus == null) {
            throw new JeecgBootException("用户状态不能为空");
        }

        // 检查 allow 列表
        UserStatusEnum[] allowStatuses = annotation.allow();
        if (allowStatuses != null && allowStatuses.length > 0) {
            boolean isAllowed = Arrays.asList(allowStatuses).contains(userStatus);
            if (!isAllowed) {
                throw new JeecgBootException(String.format(
                    "当前状态不允许访问，当前状态：%s，允许状态：%s",
                    userStatus.getDisplayName(),
                    Arrays.toString(allowStatuses)
                ));
            }
        }

        // 检查 forbid 列表
        // forbid 声明了端点不执行的操作；如果用户被限制的操作不在 forbid 列表中，
        // 说明用户无法执行该端点实际提供的功能，应拒绝访问。
        String[] forbidFunctions = annotation.forbid();
        if (forbidFunctions != null && forbidFunctions.length > 0) {
            Set<String> forbidSet = new HashSet<>(Arrays.asList(forbidFunctions));
            Set<String> userRestrictions = UserRestriction.getRestrictions(userStatus);
            for (String restriction : userRestrictions) {
                if (!forbidSet.contains(restriction)) {
                    throw new JeecgBootException(getRestrictionMessage(userStatus, restriction));
                }
            }
        }

        return joinPoint.proceed();
    }

    /**
     * 获取限制提示信息
     *
     * @param status   用户状态
     * @param function 被限制的功能
     * @return 提示信息
     */
    private String getRestrictionMessage(UserStatusEnum status, String function) {
        switch (status) {
            case MUTED:
                return "您已被禁言，暂时无法使用" + getFunctionName(function) + "功能";
            case FROZEN:
                return "账号已被冻结，请进行安全核验后重试";
            case BANNED:
                return "账号已被封禁，无法使用" + getFunctionName(function) + "功能";
            case DEACTIVATING:
                return "账号正在注销中，无法使用" + getFunctionName(function) + "功能";
            case DEACTIVATED:
                return "账号已注销，无法使用" + getFunctionName(function) + "功能";
            default:
                return "当前状态不允许使用" + getFunctionName(function) + "功能";
        }
    }

    /**
     * 获取功能名称
     *
     * @param function 功能标识
     * @return 功能名称
     */
    private String getFunctionName(String function) {
        switch (function) {
            case "publish":
                return "发布";
            case "comment":
                return "评论";
            case "message":
                return "私信";
            case "like":
                return "点赞";
            case "favorite":
                return "收藏";
            case "follow":
                return "关注";
            case "login":
                return "登录";
            default:
                return function;
        }
    }
}
