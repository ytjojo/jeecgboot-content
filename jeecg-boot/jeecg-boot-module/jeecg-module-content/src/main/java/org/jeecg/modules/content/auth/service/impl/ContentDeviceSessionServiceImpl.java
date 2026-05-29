package org.jeecg.modules.content.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.auth.constant.AuthRedisKeyConstant;
import org.jeecg.modules.content.auth.dto.DeviceInfo;
import org.jeecg.modules.content.auth.enums.DeviceSessionStatusEnum;
import org.jeecg.modules.content.auth.service.IContentDeviceSessionService;
import org.jeecg.modules.content.auth.vo.DeviceSessionVO;
import org.jeecg.modules.content.user.entity.ContentUserDeviceSession;
import org.jeecg.modules.content.user.mapper.ContentUserDeviceSessionMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 设备会话服务实现。
 */
@Slf4j
@Service
public class ContentDeviceSessionServiceImpl implements IContentDeviceSessionService {

    @Resource
    private ContentUserDeviceSessionMapper deviceSessionMapper;

    @Resource
    private StringRedisTemplate redisTemplate;

    @Override
    public ContentUserDeviceSession createSession(String userId, String tokenJti, DeviceInfo deviceInfo) {
        ContentUserDeviceSession session = new ContentUserDeviceSession();
        session.setUserId(userId);
        session.setTokenJti(tokenJti);
        session.setDeviceId(deviceInfo.getDeviceId());
        session.setDeviceName(deviceInfo.getDeviceName());
        session.setDeviceType(deviceInfo.getDeviceType());
        session.setOsType(deviceInfo.getOsType());
        session.setOsVersion(deviceInfo.getOsVersion());
        session.setBrowserType(deviceInfo.getBrowserType());
        session.setDeviceFingerprint(deviceInfo.getDeviceFingerprint());
        session.setLoginIp(deviceInfo.getLoginIp());
        session.setLoginLocation(deviceInfo.getLoginLocation());
        session.setSessionStatus(DeviceSessionStatusEnum.ACTIVE.getCode());
        session.setLastActiveTime(new Date());
        deviceSessionMapper.insert(session);
        return session;
    }

    @Override
    public List<ContentUserDeviceSession> listActiveSessions(String userId) {
        LambdaQueryWrapper<ContentUserDeviceSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContentUserDeviceSession::getUserId, userId)
                .eq(ContentUserDeviceSession::getSessionStatus, DeviceSessionStatusEnum.ACTIVE.getCode())
                .orderByDesc(ContentUserDeviceSession::getLastActiveTime);
        return deviceSessionMapper.selectList(wrapper);
    }

    @Override
    public void revokeSession(String sessionId, String operatorUserId) {
        ContentUserDeviceSession session = deviceSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }
        session.setSessionStatus(DeviceSessionStatusEnum.OFFLINE.getCode());
        session.setOfflineTime(new Date());
        session.setOfflineReason("USER_REVOKED");
        deviceSessionMapper.updateById(session);
    }

    @Override
    public boolean isTokenValid(String jti) {
        LambdaQueryWrapper<ContentUserDeviceSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContentUserDeviceSession::getTokenJti, jti)
                .eq(ContentUserDeviceSession::getSessionStatus, DeviceSessionStatusEnum.ACTIVE.getCode());
        ContentUserDeviceSession session = deviceSessionMapper.selectOne(wrapper);
        return session != null;
    }

    private static final int MAX_ACTIVE_DEVICES = 5;

    @Override
    public ContentUserDeviceSession createSessionWithLimit(String userId, String tokenJti, DeviceInfo deviceInfo) {
        int activeCount = countActiveSessions(userId);
        if (activeCount >= MAX_ACTIVE_DEVICES) {
            LambdaQueryWrapper<ContentUserDeviceSession> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ContentUserDeviceSession::getUserId, userId)
                    .eq(ContentUserDeviceSession::getSessionStatus, DeviceSessionStatusEnum.ACTIVE.getCode())
                    .orderByAsc(ContentUserDeviceSession::getLastActiveTime)
                    .last("LIMIT 1");
            ContentUserDeviceSession oldest = deviceSessionMapper.selectOne(wrapper);
            if (oldest != null) {
                oldest.setSessionStatus(DeviceSessionStatusEnum.OFFLINE.getCode());
                oldest.setOfflineTime(new Date());
                oldest.setOfflineReason("DEVICE_LIMIT_EVICTION");
                deviceSessionMapper.updateById(oldest);
            }
        }
        return createSession(userId, tokenJti, deviceInfo);
    }

    @Override
    public int countActiveSessions(String userId) {
        LambdaQueryWrapper<ContentUserDeviceSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContentUserDeviceSession::getUserId, userId)
                .eq(ContentUserDeviceSession::getSessionStatus, DeviceSessionStatusEnum.ACTIVE.getCode());
        return Math.toIntExact(deviceSessionMapper.selectCount(wrapper));
    }

    @Override
    public List<DeviceSessionVO> listDevices(String userId, String currentTokenJti) {
        List<ContentUserDeviceSession> sessions = listActiveSessions(userId);
        List<DeviceSessionVO> result = new ArrayList<>();
        for (ContentUserDeviceSession session : sessions) {
            DeviceSessionVO vo = new DeviceSessionVO();
            vo.setSessionId(session.getId());
            vo.setDeviceName(session.getDeviceName());
            vo.setDeviceType(session.getDeviceType());
            vo.setOsType(session.getOsType());
            vo.setBrowserType(session.getBrowserType());
            vo.setLoginIp(session.getLoginIp());
            vo.setLoginLocation(session.getLoginLocation());
            vo.setLastActiveTime(session.getLastActiveTime());
            vo.setCurrent(session.getTokenJti() != null && session.getTokenJti().equals(currentTokenJti));
            vo.setTrusted(Boolean.TRUE.equals(session.getTrusted()));
            result.add(vo);
        }
        return result;
    }

    @Override
    public void revokeOtherDevice(String userId, String sessionId, String currentTokenJti) {
        ContentUserDeviceSession session = deviceSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }
        if (!userId.equals(session.getUserId())) {
            throw new IllegalArgumentException("会话不属于该用户");
        }
        if (session.getTokenJti() != null && session.getTokenJti().equals(currentTokenJti)) {
            throw new IllegalArgumentException("不能下线当前设备");
        }
        session.setSessionStatus(DeviceSessionStatusEnum.OFFLINE.getCode());
        session.setOfflineTime(new Date());
        session.setOfflineReason("USER_REVOKED");
        deviceSessionMapper.updateById(session);
        // 将token jti加入Redis黑名单
        if (session.getTokenJti() != null) {
            redisTemplate.opsForValue().set(
                    AuthRedisKeyConstant.TOKEN_BLACKLIST_PREFIX + session.getTokenJti(),
                    "REVOKED",
                    Duration.ofSeconds(AuthRedisKeyConstant.TOKEN_BLACKLIST_TTL)
            );
        }
        log.info("已下线设备会话 sessionId={}, userId={}", sessionId, userId);
    }
}
