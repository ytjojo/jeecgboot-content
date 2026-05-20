package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentSubscriptionNotificationPreference;
import org.jeecg.modules.content.user.entity.ContentUserNotificationSetting;
import org.jeecg.modules.content.user.entity.ContentUserSubscription;
import org.jeecg.modules.content.user.mapper.ContentSubscriptionNotificationPreferenceMapper;
import org.jeecg.modules.content.user.mapper.ContentUserNotificationSettingMapper;
import org.jeecg.modules.content.user.mapper.ContentUserSubscriptionMapper;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionNotificationPreferenceReq;
import org.jeecg.modules.content.user.service.IContentSubscriptionNotificationPreferenceService;
import org.jeecg.modules.content.user.vo.ContentSubscriptionNotificationDecisionVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionNotificationPreferenceVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
/**
 * 内容社区订阅通知偏好服务实现。
 */
@Service
public class ContentSubscriptionNotificationPreferenceServiceImpl
    extends ServiceImpl<ContentSubscriptionNotificationPreferenceMapper, ContentSubscriptionNotificationPreference>
    implements IContentSubscriptionNotificationPreferenceService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final Set<String> SUPPORTED_CHANNELS = Set.of("IN_APP", "PUSH", "EMAIL");
    private static final Set<String> SUPPORTED_FREQUENCIES = Set.of("REALTIME", "DAILY");

    @Resource
    private ContentSubscriptionNotificationPreferenceMapper preferenceMapper;

    @Resource
    private ContentUserSubscriptionMapper subscriptionMapper;

    @Resource
    private ContentUserNotificationSettingMapper notificationSettingMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentSubscriptionNotificationPreferenceVO savePreference(String userId, ContentSubscriptionNotificationPreferenceReq req) {
        ContentUserSubscription subscription = requireOwnedSubscription(userId, req == null ? null : req.getSubscriptionId());
        String channels = normalizeChannels(req.getNotificationChannels());
        String frequency = normalizeFrequency(req.getNotificationFrequency());
        validateDnd(req.getDndStartTime(), req.getDndEndTime());
        ContentSubscriptionNotificationPreference preference = preferenceMapper.selectBySubscriptionId(subscription.getId());
        if (preference == null) {
            preference = new ContentSubscriptionNotificationPreference()
                .setSubscriptionId(subscription.getId())
                .setUserId(userId)
                .setPreferenceStatus(ACTIVE_STATUS);
            preference.setId(UUIDGenerator.generate());
            preferenceMapper.insert(preference);
        }
        preference.setNotificationChannels(channels)
            .setNotificationFrequency(frequency)
            .setDndStartTime(req.getDndStartTime())
            .setDndEndTime(req.getDndEndTime())
            .setPreferenceStatus(ACTIVE_STATUS);
        preferenceMapper.updateById(preference);
        return ContentSubscriptionNotificationPreferenceVO.from(preference, false);
    }

    @Override
    public ContentSubscriptionNotificationPreferenceVO getEffectivePreference(String userId, String subscriptionId) {
        ContentUserSubscription subscription = requireOwnedSubscription(userId, subscriptionId);
        ContentSubscriptionNotificationPreference preference = preferenceMapper.selectBySubscriptionId(subscription.getId());
        if (preference != null && ACTIVE_STATUS.equals(preference.getPreferenceStatus())) {
            return ContentSubscriptionNotificationPreferenceVO.from(preference, false);
        }
        ContentUserNotificationSetting global = notificationSettingMapper.selectByUserId(userId);
        if (global == null) {
            global = ContentUserNotificationSetting.defaults(userId);
        }
        ContentSubscriptionNotificationPreference inherited = new ContentSubscriptionNotificationPreference()
            .setSubscriptionId(subscription.getId())
            .setUserId(userId)
            .setNotificationChannels(Boolean.FALSE.equals(global.getSubscriptionNoticeEnabled()) ? "" : global.getSubscriptionDefaultChannels())
            .setNotificationFrequency(global.getSubscriptionDefaultFrequency())
            .setPreferenceStatus(ACTIVE_STATUS);
        return ContentSubscriptionNotificationPreferenceVO.from(inherited, true);
    }

    @Override
    public ContentSubscriptionNotificationDecisionVO decideUpdateNotification(String userId, String subscriptionId, String updateBizId) {
        ContentSubscriptionNotificationPreferenceVO preference = getEffectivePreference(userId, subscriptionId);
        boolean daily = "DAILY".equals(preference.getNotificationFrequency());
        boolean delayed = !daily && isNowInDnd(preference.getDndStartTime(), preference.getDndEndTime());
        return new ContentSubscriptionNotificationDecisionVO()
            .setRealtimeDelivery(!daily && !delayed && !preference.getNotificationChannels().isEmpty())
            .setDailySummary(daily || delayed)
            .setDelayedByDnd(delayed)
            .setChannels(preference.getNotificationChannels())
            .setUpdateBizId(updateBizId);
    }

    private ContentUserSubscription requireOwnedSubscription(String userId, String subscriptionId) {
        if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
            throw new JeecgBootException("订阅ID不能为空");
        }
        ContentUserSubscription subscription = subscriptionMapper.selectById(subscriptionId);
        if (subscription == null || !userId.equals(subscription.getUserId())) {
            throw new JeecgBootException("订阅不存在或无权操作");
        }
        return subscription;
    }

    private String normalizeChannels(List<String> channels) {
        if (channels == null || channels.isEmpty()) {
            throw new JeecgBootException("通知渠道不能为空");
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String channel : channels) {
            if (channel == null || channel.trim().isEmpty()) {
                throw new JeecgBootException("通知渠道不能为空");
            }
            String value = channel.trim().toUpperCase();
            if (!SUPPORTED_CHANNELS.contains(value)) {
                throw new JeecgBootException("通知渠道不支持");
            }
            if (!normalized.add(value)) {
                throw new JeecgBootException("通知渠道不能重复");
            }
        }
        return String.join(",", normalized);
    }

    private String normalizeFrequency(String frequency) {
        if (frequency == null || frequency.trim().isEmpty()) {
            throw new JeecgBootException("通知频率不能为空");
        }
        String value = frequency.trim().toUpperCase();
        if (!SUPPORTED_FREQUENCIES.contains(value)) {
            throw new JeecgBootException("通知频率不支持");
        }
        return value;
    }

    private void validateDnd(String start, String end) {
        if ((start == null || start.isBlank()) && (end == null || end.isBlank())) {
            return;
        }
        LocalTime startTime = parseTime(start, "免打扰开始时间格式错误");
        LocalTime endTime = parseTime(end, "免打扰结束时间格式错误");
        if (startTime.equals(endTime)) {
            throw new JeecgBootException("免打扰开始和结束时间不能相同");
        }
    }

    private LocalTime parseTime(String time, String message) {
        try {
            return LocalTime.parse(time);
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new JeecgBootException(message);
        }
    }

    private boolean isNowInDnd(String start, String end) {
        if (start == null || start.isBlank() || end == null || end.isBlank()) {
            return false;
        }
        LocalTime now = LocalTime.now();
        LocalTime startTime = LocalTime.parse(start);
        LocalTime endTime = LocalTime.parse(end);
        if (startTime.isBefore(endTime)) {
            return !now.isBefore(startTime) && now.isBefore(endTime);
        }
        return !now.isBefore(startTime) || now.isBefore(endTime);
    }
}
