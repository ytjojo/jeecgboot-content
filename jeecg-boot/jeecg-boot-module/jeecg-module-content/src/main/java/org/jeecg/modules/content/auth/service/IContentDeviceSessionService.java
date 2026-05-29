package org.jeecg.modules.content.auth.service;

import org.jeecg.modules.content.auth.dto.DeviceInfo;
import org.jeecg.modules.content.auth.vo.DeviceSessionVO;
import org.jeecg.modules.content.user.entity.ContentUserDeviceSession;

import java.util.List;

/**
 * 设备会话服务接口。
 */
public interface IContentDeviceSessionService {

    /**
     * 创建设备会话记录。
     *
     * @param userId     用户ID
     * @param tokenJti   Token JTI
     * @param deviceInfo 设备信息
     * @return 会话记录
     */
    ContentUserDeviceSession createSession(String userId, String tokenJti, DeviceInfo deviceInfo);

    /**
     * 查询用户活跃设备列表。
     *
     * @param userId 用户ID
     * @return 活跃会话列表
     */
    List<ContentUserDeviceSession> listActiveSessions(String userId);

    /**
     * 下线指定设备会话。
     *
     * @param sessionId     会话ID
     * @param operatorUserId 操作人ID
     */
    void revokeSession(String sessionId, String operatorUserId);

    /**
     * 校验token jti是否有效(未被下线)。
     *
     * @param jti Token JTI
     * @return 是否有效
     */
    boolean isTokenValid(String jti);

    /**
     * 创建设备会话，如果超过5个活跃设备则自动挤出最早的。
     *
     * @param userId     用户ID
     * @param tokenJti   Token JTI
     * @param deviceInfo 设备信息
     * @return 创建的会话
     */
    ContentUserDeviceSession createSessionWithLimit(String userId, String tokenJti, DeviceInfo deviceInfo);

    /**
     * 统计用户活跃会话数。
     */
    int countActiveSessions(String userId);

    /**
     * 获取用户设备列表(含当前设备标记)。
     */
    List<DeviceSessionVO> listDevices(String userId, String currentTokenJti);

    /**
     * 下线指定设备(不允许下线当前设备)。
     * @param userId 用户ID
     * @param sessionId 要下线的会话ID
     * @param currentTokenJti 当前设备的token jti
     */
    void revokeOtherDevice(String userId, String sessionId, String currentTokenJti);
}
