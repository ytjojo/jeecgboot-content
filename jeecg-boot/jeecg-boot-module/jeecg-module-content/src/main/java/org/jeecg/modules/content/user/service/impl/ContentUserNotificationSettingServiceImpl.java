package org.jeecg.modules.content.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentNotificationAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserNotificationSetting;
import org.jeecg.modules.content.user.mapper.ContentNotificationAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserNotificationSettingMapper;
import org.jeecg.modules.content.user.req.settings.ContentNotificationChannelConfigReq;
import org.jeecg.modules.content.user.req.settings.ContentNotificationDndRuleReq;
import org.jeecg.modules.content.user.req.settings.ContentUserNotificationUpdateReq;
import org.jeecg.modules.content.user.service.IContentUserNotificationSettingService;
import org.jeecg.modules.content.user.vo.ContentNotificationChannelConfigVO;
import org.jeecg.modules.content.user.vo.ContentNotificationDndRuleVO;
import org.jeecg.modules.content.user.vo.ContentUserNotificationSettingVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import org.jeecg.modules.content.user.service.ContentUserSettingsCacheService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 内容社区用户通知设置服务实现。
 */
@Service
public class ContentUserNotificationSettingServiceImpl implements IContentUserNotificationSettingService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String NOTICE_TYPE_LIKE = "LIKE";
    private static final String NOTICE_TYPE_COMMENT = "COMMENT";
    private static final String NOTICE_TYPE_FOLLOW = "FOLLOW";
    private static final String NOTICE_TYPE_FAVORITE = "FAVORITE";
    private static final String NOTICE_TYPE_MENTION = "MENTION";
    private static final String NOTICE_TYPE_PRIVATE_MESSAGE = "PRIVATE_MESSAGE";
    private static final String NOTICE_TYPE_SECURITY = "SECURITY";
    private static final List<String> DEFAULT_CHANNELS = List.of("IN_APP", "PUSH");

    @Resource
    private ContentUserNotificationSettingMapper notificationSettingMapper;

    @Resource
    private ContentNotificationAuditLogMapper auditLogMapper;

    @Resource
    private ContentUserSettingsCacheService settingsCacheService;

    /**
     * 查询用户通知设置，缺省记录会按注册初始化口径补齐。
     */
    @Override
    public ContentUserNotificationSettingVO getSetting(String userId) {
        ContentUserNotificationSetting setting = getOrCreateSetting(userId);
        return toVO(setting);
    }

    /**
     * 更新通知开关、通知渠道和免打扰规则。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentUserNotificationSettingVO updateSetting(String userId, ContentUserNotificationUpdateReq req) {
        ContentUserNotificationSetting setting = getOrCreateSetting(userId);
        applySwitches(setting, req);
        if (req.getChannelConfig() != null) {
            setting.setChannelConfigJson(writeJson(toChannelConfigVO(req.getChannelConfig())));
        }
        if (req.getDndRule() != null) {
            setting.setDndRuleJson(writeJson(toDndRuleVO(req.getDndRule())));
        }
        notificationSettingMapper.updateById(setting);
        settingsCacheService.evictNotification(userId);
        return toVO(setting);
    }

    /**
     * 单独更新免打扰规则。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentNotificationDndRuleVO updateDndRule(String userId, ContentNotificationDndRuleReq req) {
        ContentUserNotificationSetting setting = getOrCreateSetting(userId);
        ContentNotificationDndRuleVO ruleVO = toDndRuleVO(req);
        setting.setDndRuleJson(writeJson(ruleVO));
        notificationSettingMapper.updateById(setting);
        settingsCacheService.evictNotification(userId);
        return ruleVO;
    }

    /**
     * 判断指定通知类型和渠道是否允许发送，并写入审计日志。
     */
    @Override
    public boolean canSendNotice(String userId, String noticeType, String channel, LocalTime currentTime) {
        boolean result;
        if (NOTICE_TYPE_SECURITY.equals(noticeType)) {
            result = true;
        } else {
            ContentUserNotificationSetting setting = getOrCreateSetting(userId);
            if (!isNoticeTypeEnabled(setting, noticeType)) {
                result = false;
            } else {
                ContentNotificationChannelConfigVO channelConfig = readChannelConfig(setting.getChannelConfigJson());
                if (!getChannels(channelConfig, noticeType).contains(channel)) {
                    result = false;
                } else {
                    ContentNotificationDndRuleVO dndRule = readDndRule(setting.getDndRuleJson());
                    result = !isInDnd(dndRule, currentTime, LocalDate.now());
                }
            }
        }
        // 审计日志写入失败不影响主流程
        try {
            ContentNotificationAuditLog log = new ContentNotificationAuditLog()
                .setUserId(userId)
                .setNoticeType(noticeType)
                .setChannel(channel)
                .setDecision(result ? "SEND" : "SKIP")
                .setReason(result ? "allowed" : "blocked_by_preference");
            log.setId(UUIDGenerator.generate());
            auditLogMapper.insert(log);
        } catch (Exception e) {
            // 审计日志写入失败不影响主流程
        }
        return result;
    }

    private ContentUserNotificationSetting getOrCreateSetting(String userId) {
        ContentUserNotificationSetting setting = notificationSettingMapper.selectByUserId(userId);
        if (setting != null) {
            fillDefaultSwitches(setting);
            return setting;
        }
        setting = ContentUserNotificationSetting.defaults(userId);
        setting.setId(UUIDGenerator.generate());
        notificationSettingMapper.insert(setting);
        return setting;
    }

    private void applySwitches(ContentUserNotificationSetting setting, ContentUserNotificationUpdateReq req) {
        if (req.getLikeNoticeEnabled() != null) {
            setting.setLikeNoticeEnabled(req.getLikeNoticeEnabled());
        }
        if (req.getCommentNoticeEnabled() != null) {
            setting.setCommentNoticeEnabled(req.getCommentNoticeEnabled());
        }
        if (req.getFollowNoticeEnabled() != null) {
            setting.setFollowNoticeEnabled(req.getFollowNoticeEnabled());
        }
        if (req.getFavoriteNoticeEnabled() != null) {
            setting.setFavoriteNoticeEnabled(req.getFavoriteNoticeEnabled());
        }
        if (req.getMentionNoticeEnabled() != null) {
            setting.setMentionNoticeEnabled(req.getMentionNoticeEnabled());
        }
        if (req.getPrivateMessageNoticeEnabled() != null) {
            setting.setPrivateMessageNoticeEnabled(req.getPrivateMessageNoticeEnabled());
        }
    }

    private boolean isNoticeTypeEnabled(ContentUserNotificationSetting setting, String noticeType) {
        return switch (noticeType) {
            case NOTICE_TYPE_LIKE -> Boolean.TRUE.equals(setting.getLikeNoticeEnabled());
            case NOTICE_TYPE_COMMENT -> Boolean.TRUE.equals(setting.getCommentNoticeEnabled());
            case NOTICE_TYPE_FOLLOW -> Boolean.TRUE.equals(setting.getFollowNoticeEnabled());
            case NOTICE_TYPE_FAVORITE -> Boolean.TRUE.equals(setting.getFavoriteNoticeEnabled());
            case NOTICE_TYPE_MENTION -> Boolean.TRUE.equals(setting.getMentionNoticeEnabled());
            case NOTICE_TYPE_PRIVATE_MESSAGE -> Boolean.TRUE.equals(setting.getPrivateMessageNoticeEnabled());
            default -> throw new JeecgBootException("通知类型不合法");
        };
    }

    private List<String> getChannels(ContentNotificationChannelConfigVO channelConfig, String noticeType) {
        return switch (noticeType) {
            case NOTICE_TYPE_LIKE -> defaultIfEmpty(channelConfig.getLikeChannels());
            case NOTICE_TYPE_COMMENT -> defaultIfEmpty(channelConfig.getCommentChannels());
            case NOTICE_TYPE_FOLLOW -> defaultIfEmpty(channelConfig.getFollowChannels());
            case NOTICE_TYPE_FAVORITE -> defaultIfEmpty(channelConfig.getFavoriteChannels());
            case NOTICE_TYPE_MENTION -> defaultIfEmpty(channelConfig.getMentionChannels());
            case NOTICE_TYPE_PRIVATE_MESSAGE -> defaultIfEmpty(channelConfig.getPrivateMessageChannels());
            default -> throw new JeecgBootException("通知类型不合法");
        };
    }

    /**
     * 多时段免打扰判定：遍历所有规则，任一匹配即视为免打扰中。
     */
    private boolean isInDnd(ContentNotificationDndRuleVO dndRule, LocalTime currentTime, LocalDate currentDate) {
        // 临时关闭免打扰
        if (dndRule.getTemporaryDisableUntil() != null && dndRule.getTemporaryDisableUntil() > System.currentTimeMillis()) {
            return false;
        }
        // 多时段规则优先
        if (dndRule.getDndRules() != null && !dndRule.getDndRules().isEmpty()) {
            for (ContentNotificationDndRuleVO.DndRuleItem item : dndRule.getDndRules()) {
                if (isDndRuleActive(item, currentTime, currentDate)) {
                    return true;
                }
            }
            return false;
        }
        // 旧单时段兼容
        if (!Boolean.TRUE.equals(dndRule.getEnabled())) {
            return false;
        }
        return isInSingleDnd(dndRule.getStartTime(), dndRule.getEndTime(), currentTime);
    }

    /**
     * 判断单条规则是否在免打扰时段内。
     */
    private boolean isDndRuleActive(ContentNotificationDndRuleVO.DndRuleItem item, LocalTime currentTime, LocalDate currentDate) {
        if (!Boolean.TRUE.equals(item.getEnabled())) {
            return false;
        }
        if (!matchesDayType(item.getDayType(), currentDate)) {
            return false;
        }
        return isInSingleDnd(item.getStartTime(), item.getEndTime(), currentTime);
    }

    /**
     * 判断当前日期是否匹配日期类型。
     */
    private boolean matchesDayType(String dayType, LocalDate currentDate) {
        if (dayType == null || "DAILY".equals(dayType)) {
            return true;
        }
        DayOfWeek dow = currentDate.getDayOfWeek();
        if ("WORKDAY".equals(dayType)) {
            return dow.getValue() >= 1 && dow.getValue() <= 5;
        }
        if ("WEEKEND".equals(dayType)) {
            return dow.getValue() >= 6;
        }
        // CUSTOM 类型暂不做日期匹配，视为每天生效
        return true;
    }

    /**
     * 单时段判定逻辑（兼容旧格式）。
     */
    private boolean isInSingleDnd(String startTimeStr, String endTimeStr, LocalTime currentTime) {
        if (startTimeStr == null || startTimeStr.isBlank() || endTimeStr == null || endTimeStr.isBlank()) {
            return false;
        }
        LocalTime startTime = LocalTime.parse(startTimeStr);
        LocalTime endTime = LocalTime.parse(endTimeStr);
        if (startTime.equals(endTime)) {
            return true;
        }
        if (startTime.isBefore(endTime)) {
            return !currentTime.isBefore(startTime) && currentTime.isBefore(endTime);
        }
        // 跨午夜
        return !currentTime.isBefore(startTime) || currentTime.isBefore(endTime);
    }

    private ContentUserNotificationSettingVO toVO(ContentUserNotificationSetting setting) {
        return ContentUserNotificationSettingVO.from(
            setting,
            readChannelConfig(setting.getChannelConfigJson()),
            readDndRule(setting.getDndRuleJson())
        );
    }

    private ContentNotificationChannelConfigVO readChannelConfig(String json) {
        if (json == null || json.isBlank() || "{}".equals(json)) {
            return defaultChannelConfig();
        }
        try {
            ContentNotificationChannelConfigVO config = OBJECT_MAPPER.readValue(json, ContentNotificationChannelConfigVO.class);
            return fillDefaultChannels(config);
        } catch (JsonProcessingException e) {
            throw new JeecgBootException("通知渠道配置格式不合法");
        }
    }

    /**
     * 读取免打扰规则，支持旧格式自动升级为多时段列表。
     */
    private ContentNotificationDndRuleVO readDndRule(String json) {
        if (json == null || json.isBlank() || "{}".equals(json)) {
            return defaultDndRule();
        }
        try {
            // 先尝试按多时段格式解析
            ContentNotificationDndRuleVO rule = OBJECT_MAPPER.readValue(json, ContentNotificationDndRuleVO.class);
            if (rule.getEnabled() == null) {
                rule.setEnabled(Boolean.FALSE);
            }
            // 旧格式兼容：如果有 startTime/endTime 但没有 dndRules，自动升级为单元素列表
            if ((rule.getDndRules() == null || rule.getDndRules().isEmpty())
                && rule.getStartTime() != null && !rule.getStartTime().isBlank()
                && rule.getEndTime() != null && !rule.getEndTime().isBlank()) {
                ContentNotificationDndRuleVO.DndRuleItem item = new ContentNotificationDndRuleVO.DndRuleItem()
                    .setEnabled(Boolean.TRUE.equals(rule.getEnabled()))
                    .setStartTime(rule.getStartTime())
                    .setEndTime(rule.getEndTime())
                    .setDayType("DAILY")
                    .setSummaryMode(false);
                rule.setDndRules(List.of(item));
            }
            return rule;
        } catch (JsonProcessingException e) {
            throw new JeecgBootException("免打扰配置格式不合法");
        }
    }

    private ContentNotificationChannelConfigVO toChannelConfigVO(ContentNotificationChannelConfigReq req) {
        return fillDefaultChannels(new ContentNotificationChannelConfigVO()
            .setLikeChannels(req.getLikeChannels())
            .setCommentChannels(req.getCommentChannels())
            .setFollowChannels(req.getFollowChannels())
            .setFavoriteChannels(req.getFavoriteChannels())
            .setMentionChannels(req.getMentionChannels())
            .setPrivateMessageChannels(req.getPrivateMessageChannels()));
    }

    /**
     * 将请求转换为免打扰规则VO，支持多时段和旧单时段格式。
     */
    private ContentNotificationDndRuleVO toDndRuleVO(ContentNotificationDndRuleReq req) {
        ContentNotificationDndRuleVO rule = new ContentNotificationDndRuleVO();
        // 多时段规则
        if (req.getDndRules() != null && !req.getDndRules().isEmpty()) {
            List<ContentNotificationDndRuleVO.DndRuleItem> items = new ArrayList<>();
            for (ContentNotificationDndRuleReq.DndRuleItemReq itemReq : req.getDndRules()) {
                ContentNotificationDndRuleVO.DndRuleItem item = new ContentNotificationDndRuleVO.DndRuleItem()
                    .setEnabled(Boolean.TRUE.equals(itemReq.getEnabled()))
                    .setStartTime(itemReq.getStartTime())
                    .setEndTime(itemReq.getEndTime())
                    .setDayType(itemReq.getDayType() != null ? itemReq.getDayType() : "DAILY")
                    .setSummaryMode(Boolean.TRUE.equals(itemReq.getSummaryMode()));
                if (Boolean.TRUE.equals(item.getEnabled())
                    && (item.getStartTime() == null || item.getStartTime().isBlank()
                    || item.getEndTime() == null || item.getEndTime().isBlank())) {
                    throw new JeecgBootException("启用免打扰规则时必须配置开始和结束时间");
                }
                items.add(item);
            }
            rule.setEnabled(items.stream().anyMatch(i -> Boolean.TRUE.equals(i.getEnabled())));
            rule.setDndRules(items);
        } else {
            // 旧单时段兼容
            rule.setEnabled(Boolean.TRUE.equals(req.getEnabled()))
                .setStartTime(req.getStartTime())
                .setEndTime(req.getEndTime());
            if (Boolean.TRUE.equals(rule.getEnabled())
                && (rule.getStartTime() == null || rule.getStartTime().isBlank()
                || rule.getEndTime() == null || rule.getEndTime().isBlank())) {
                throw new JeecgBootException("启用免打扰时必须配置开始和结束时间");
            }
        }
        // 临时关闭免打扰（1小时）
        if (Boolean.TRUE.equals(req.getTemporaryDisable())) {
            rule.setTemporaryDisableUntil(System.currentTimeMillis() + 3_600_000L);
        }
        return rule;
    }

    private ContentNotificationChannelConfigVO fillDefaultChannels(ContentNotificationChannelConfigVO config) {
        if (config == null) {
            return defaultChannelConfig();
        }
        return config
            .setLikeChannels(defaultIfEmpty(config.getLikeChannels()))
            .setCommentChannels(defaultIfEmpty(config.getCommentChannels()))
            .setFollowChannels(defaultIfEmpty(config.getFollowChannels()))
            .setFavoriteChannels(defaultIfEmpty(config.getFavoriteChannels()))
            .setMentionChannels(defaultIfEmpty(config.getMentionChannels()))
            .setPrivateMessageChannels(defaultIfEmpty(config.getPrivateMessageChannels()));
    }

    private ContentNotificationChannelConfigVO defaultChannelConfig() {
        return new ContentNotificationChannelConfigVO()
            .setLikeChannels(DEFAULT_CHANNELS)
            .setCommentChannels(DEFAULT_CHANNELS)
            .setFollowChannels(DEFAULT_CHANNELS)
            .setFavoriteChannels(DEFAULT_CHANNELS)
            .setMentionChannels(DEFAULT_CHANNELS)
            .setPrivateMessageChannels(DEFAULT_CHANNELS);
    }

    private ContentNotificationDndRuleVO defaultDndRule() {
        return new ContentNotificationDndRuleVO()
            .setEnabled(Boolean.FALSE)
            .setStartTime(null)
            .setEndTime(null)
            .setDndRules(null)
            .setTemporaryDisableUntil(null);
    }

    private List<String> defaultIfEmpty(List<String> channels) {
        if (channels == null || channels.isEmpty()) {
            return DEFAULT_CHANNELS;
        }
        return channels.stream().distinct().toList();
    }

    private String writeJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new JeecgBootException("通知设置序列化失败");
        }
    }

    private void fillDefaultSwitches(ContentUserNotificationSetting setting) {
        if (setting.getLikeNoticeEnabled() == null) {
            setting.setLikeNoticeEnabled(Boolean.TRUE);
        }
        if (setting.getCommentNoticeEnabled() == null) {
            setting.setCommentNoticeEnabled(Boolean.TRUE);
        }
        if (setting.getFollowNoticeEnabled() == null) {
            setting.setFollowNoticeEnabled(Boolean.TRUE);
        }
        if (setting.getFavoriteNoticeEnabled() == null) {
            setting.setFavoriteNoticeEnabled(Boolean.TRUE);
        }
        if (setting.getMentionNoticeEnabled() == null) {
            setting.setMentionNoticeEnabled(Boolean.TRUE);
        }
        if (setting.getPrivateMessageNoticeEnabled() == null) {
            setting.setPrivateMessageNoticeEnabled(Boolean.TRUE);
        }
    }
}
