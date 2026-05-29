package org.jeecg.modules.content.auth.biz;

import org.jeecg.modules.content.auth.entity.ContentRiskEvent;

import java.util.List;

/**
 * 风控与异常登录业务服务接口。
 */
public interface IContentRiskControlBizService {

    /**
     * 记录登录失败，返回是否需要验证码挑战。
     *
     * @param userId    用户ID
     * @param ipAddress 登录IP
     * @return true=需要验证码挑战
     */
    boolean recordLoginFail(String userId, String ipAddress);

    /**
     * 检查账号是否被锁定。
     *
     * @param userId 用户ID
     * @return true=已锁定
     */
    boolean isAccountLocked(String userId);

    /**
     * 检查IP注册限流，true=被限流。
     *
     * @param ipAddress IP地址
     * @return true=被限流
     */
    boolean isIpRegisterRateLimited(String ipAddress);

    /**
     * 记录IP注册次数。
     *
     * @param ipAddress IP地址
     */
    void recordIpRegister(String ipAddress);

    /**
     * 记录风险事件。
     *
     * @param userId           用户ID
     * @param eventType        事件类型
     * @param riskLevel        风险等级
     * @param reason           风险原因
     * @param ipAddress        IP地址
     * @param deviceFingerprint 设备指纹
     * @param userAgent        User-Agent
     */
    void recordRiskEvent(String userId, String eventType, String riskLevel,
                         String reason, String ipAddress, String deviceFingerprint, String userAgent);

    /**
     * 申诉解除风险。
     *
     * @param eventId     风险事件ID
     * @param resolvedBy  处理人用户ID
     * @param note        处理备注
     */
    void appealRiskEvent(String eventId, String resolvedBy, String note);

    /**
     * 检测新设备登录。
     *
     * @param userId           用户ID
     * @param deviceFingerprint 设备指纹
     * @return true=新设备
     */
    boolean isNewDevice(String userId, String deviceFingerprint);

    /**
     * 检测异地登录。
     *
     * @param userId          用户ID
     * @param currentLocation 当前登录地点
     * @return true=异地登录
     */
    boolean isAbnormalLocation(String userId, String currentLocation);

    /**
     * 获取需要通知的异常登录事件。
     *
     * @param userId 用户ID
     * @return 未处理的风险事件列表
     */
    List<ContentRiskEvent> getPendingNotifications(String userId);

    /**
     * 确认异常登录并下线非本人设备。
     *
     * @param userId  用户ID
     * @param eventId 风险事件ID
     * @param isSelf  是否本人操作
     */
    void confirmAbnormalLogin(String userId, String eventId, boolean isSelf);
}
