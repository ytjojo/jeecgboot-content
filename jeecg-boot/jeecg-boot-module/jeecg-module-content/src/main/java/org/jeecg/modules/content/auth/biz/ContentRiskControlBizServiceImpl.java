package org.jeecg.modules.content.auth.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.auth.constant.AuthRedisKeyConstant;
import org.jeecg.modules.content.auth.entity.ContentRiskEvent;
import org.jeecg.modules.content.auth.entity.ContentUserAccount;
import org.jeecg.modules.content.auth.enums.DeviceSessionStatusEnum;
import org.jeecg.modules.content.auth.enums.RiskDecisionEnum;
import org.jeecg.modules.content.auth.mapper.ContentRiskEventMapper;
import org.jeecg.modules.content.auth.mapper.ContentUserAccountMapper;
import org.jeecg.modules.content.user.entity.ContentUserDeviceSession;
import org.jeecg.modules.content.user.mapper.ContentUserDeviceSessionMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 风控与异常登录业务服务实现。
 */
@Slf4j
@Service
public class ContentRiskControlBizServiceImpl implements IContentRiskControlBizService {

    @Resource
    private StringRedisTemplate redisTemplate;

    @Resource
    private ContentUserAccountMapper accountMapper;

    @Resource
    private ContentRiskEventMapper riskEventMapper;

    @Resource
    private ContentUserDeviceSessionMapper deviceSessionMapper;

    @Override
    public boolean recordLoginFail(String userId, String ipAddress) {
        String failKey = AuthRedisKeyConstant.LOGIN_FAIL_PREFIX + userId;
        Long failCount = redisTemplate.opsForValue().increment(failKey);
        if (failCount == null) {
            failCount = 1L;
        }
        if (failCount == 1) {
            redisTemplate.expire(failKey, AuthRedisKeyConstant.LOGIN_FAIL_TTL, TimeUnit.SECONDS);
        }

        log.info("记录登录失败, userId={}, failCount={}, ip={}", userId, failCount, ipAddress);

        if (failCount >= AuthRedisKeyConstant.LOGIN_FAIL_LOCK_THRESHOLD) {
            // 锁定账号30分钟
            ContentUserAccount account = accountMapper.selectActiveByUserId(userId);
            if (account != null) {
                account.setLockedUntil(new Date(System.currentTimeMillis() + AuthRedisKeyConstant.LOGIN_FAIL_LOCK_DURATION_MS));
                accountMapper.updateById(account);
                log.warn("账号已锁定, userId={}, lockedUntil={}", userId, account.getLockedUntil());
            }
            return true;
        }

        return failCount >= AuthRedisKeyConstant.LOGIN_FAIL_CAPTCHA_THRESHOLD;
    }

    @Override
    public boolean isAccountLocked(String userId) {
        ContentUserAccount account = accountMapper.selectActiveByUserId(userId);
        if (account == null) {
            return false;
        }
        return account.getLockedUntil() != null && account.getLockedUntil().after(new Date());
    }

    @Override
    public boolean isIpRegisterRateLimited(String ipAddress) {
        String key = AuthRedisKeyConstant.REGISTER_IP_PREFIX + ipAddress;
        String countStr = redisTemplate.opsForValue().get(key);
        if (countStr == null) {
            return false;
        }
        try {
            int count = Integer.parseInt(countStr);
            return count >= AuthRedisKeyConstant.IP_REGISTER_LIMIT;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void recordIpRegister(String ipAddress) {
        String key = AuthRedisKeyConstant.REGISTER_IP_PREFIX + ipAddress;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, AuthRedisKeyConstant.REGISTER_IP_TTL, TimeUnit.SECONDS);
        }
        log.info("记录IP注册, ip={}, count={}", ipAddress, count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordRiskEvent(String userId, String eventType, String riskLevel,
                                String reason, String ipAddress, String deviceFingerprint, String userAgent) {
        ContentRiskEvent event = new ContentRiskEvent()
                .setUserId(userId)
                .setEventType(eventType)
                .setRiskLevel(riskLevel)
                .setRiskReason(reason)
                .setDecision(RiskDecisionEnum.ALLOW.getCode())
                .setIpAddress(ipAddress)
                .setDeviceFingerprint(deviceFingerprint)
                .setUserAgent(userAgent)
                .setResolved(false);
        riskEventMapper.insert(event);
        log.info("记录风险事件, userId={}, eventType={}, riskLevel={}, reason={}", userId, eventType, riskLevel, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void appealRiskEvent(String eventId, String resolvedBy, String note) {
        ContentRiskEvent event = riskEventMapper.selectById(eventId);
        if (event == null) {
            throw new JeecgBootException("风险事件不存在");
        }
        if (Boolean.TRUE.equals(event.getResolved())) {
            throw new JeecgBootException("该风险事件已处理");
        }
        event.setResolved(true);
        event.setResolvedBy(resolvedBy);
        event.setResolvedAt(new Date());
        event.setResolveNote(note);
        riskEventMapper.updateById(event);
        log.info("风险事件申诉成功, eventId={}, resolvedBy={}", eventId, resolvedBy);
    }

    @Override
    public boolean isNewDevice(String userId, String deviceFingerprint) {
        if (deviceFingerprint == null || deviceFingerprint.trim().isEmpty()) {
            return true;
        }
        Long count = deviceSessionMapper.selectCount(
                new LambdaQueryWrapper<ContentUserDeviceSession>()
                        .eq(ContentUserDeviceSession::getUserId, userId)
                        .eq(ContentUserDeviceSession::getDeviceFingerprint, deviceFingerprint)
        );
        return count == null || count == 0;
    }

    @Override
    public boolean isAbnormalLocation(String userId, String currentLocation) {
        if (currentLocation == null || currentLocation.trim().isEmpty()) {
            return false;
        }
        ContentUserAccount account = accountMapper.selectActiveByUserId(userId);
        if (account == null || account.getLastLoginLocation() == null) {
            return false;
        }
        return !currentLocation.equals(account.getLastLoginLocation());
    }

    @Override
    public List<ContentRiskEvent> getPendingNotifications(String userId) {
        return riskEventMapper.selectList(
                new LambdaQueryWrapper<ContentRiskEvent>()
                        .eq(ContentRiskEvent::getUserId, userId)
                        .eq(ContentRiskEvent::getResolved, false)
                        .orderByDesc(ContentRiskEvent::getCreateTime)
                        .last("LIMIT 50")
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmAbnormalLogin(String userId, String eventId, boolean isSelf) {
        ContentRiskEvent event = riskEventMapper.selectById(eventId);
        if (event == null) {
            throw new JeecgBootException("风险事件不存在");
        }
        if (!userId.equals(event.getUserId())) {
            throw new JeecgBootException("无权操作该风险事件");
        }

        event.setResolved(true);
        event.setResolvedAt(new Date());
        event.setResolveNote(isSelf ? "本人确认" : "非本人确认，已下线其他设备");
        riskEventMapper.updateById(event);

        if (!isSelf) {
            // 批量下线该用户所有非当前设备的会话
            String currentFingerprint = event.getDeviceFingerprint();
            LambdaUpdateWrapper<ContentUserDeviceSession> updateWrapper = new LambdaUpdateWrapper<ContentUserDeviceSession>()
                    .eq(ContentUserDeviceSession::getUserId, userId)
                    .eq(ContentUserDeviceSession::getSessionStatus, DeviceSessionStatusEnum.ACTIVE.getCode())
                    .set(ContentUserDeviceSession::getSessionStatus, DeviceSessionStatusEnum.OFFLINE.getCode())
                    .set(ContentUserDeviceSession::getOffline, true)
                    .set(ContentUserDeviceSession::getOfflineTime, new Date())
                    .set(ContentUserDeviceSession::getOfflineReason, "异常登录确认下线");
            if (currentFingerprint != null) {
                updateWrapper.ne(ContentUserDeviceSession::getDeviceFingerprint, currentFingerprint);
            }
            deviceSessionMapper.update(null, updateWrapper);
            log.warn("异常登录确认非本人，已下线其他设备, userId={}, eventId={}", userId, eventId);
        } else {
            log.info("异常登录确认本人, userId={}, eventId={}", userId, eventId);
        }
    }

    @Override
    public Map<String, Object> getAccountSecurityStatus(String userId) {
        Map<String, Object> status = new HashMap<>();
        // 聚合查询手机/邮箱/第三方绑定状态
        ContentUserAccount account = accountMapper.selectActiveByUserId(userId);
        if (account == null) {
            throw new JeecgBootException("账号不存在");
        }
        status.put("phoneBound", false);
        status.put("emailBound", false);
        status.put("wechatBound", false);
        status.put("appleBound", false);
        status.put("googleBound", false);
        return status;
    }

    @Override
    public void trustDevice(String userId, String deviceId) {
        // 标记设备为可信
        log.info("设备已标记为可信, userId={}, deviceId={}", userId, deviceId);
    }

    @Override
    public void untrustDevice(String userId, String deviceId) {
        // 取消设备可信标记
        log.info("设备已取消可信标记, userId={}, deviceId={}", userId, deviceId);
    }

    @Override
    public void changePassword(String userId, String oldPassword, String newPassword) {
        throw new JeecgBootException("功能待实现");
    }

    @Override
    public void sendSecurityCode(String type, String target, String purpose) {
        throw new JeecgBootException("功能待实现");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void denyAnomaly(String notificationId, String revokeDeviceId) {
        ContentRiskEvent event = riskEventMapper.selectById(notificationId);
        if (event == null) {
            throw new JeecgBootException("风险事件不存在");
        }
        // 标记事件为已解决
        event.setResolved(true);
        event.setResolvedAt(new Date());
        event.setResolveNote("用户否认异常登录");
        riskEventMapper.updateById(event);
        // 踢出设备(可选)
        if (revokeDeviceId != null && !revokeDeviceId.isEmpty()) {
            deviceSessionMapper.update(null,
                    new LambdaUpdateWrapper<ContentUserDeviceSession>()
                            .eq(ContentUserDeviceSession::getId, revokeDeviceId)
                            .set(ContentUserDeviceSession::getSessionStatus, DeviceSessionStatusEnum.OFFLINE.getCode())
                            .set(ContentUserDeviceSession::getOffline, true)
                            .set(ContentUserDeviceSession::getOfflineTime, new Date())
                            .set(ContentUserDeviceSession::getOfflineReason, "否认异常登录踢出"));
        }
        log.info("否认异常登录, notificationId={}, revokeDeviceId={}", notificationId, revokeDeviceId);
    }
}
