package org.jeecg.modules.content.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentUserNotificationSetting;
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
import java.time.LocalTime;
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
        return toVO(setting);
    }

    /**
     * 判断指定通知类型和渠道是否允许发送。
     */
    @Override
    public boolean canSendNotice(String userId, String noticeType, String channel, LocalTime currentTime) {
        if (NOTICE_TYPE_SECURITY.equals(noticeType)) {
            return true;
        }
        ContentUserNotificationSetting setting = getOrCreateSetting(userId);
        if (!isNoticeTypeEnabled(setting, noticeType)) {
            return false;
        }
        ContentNotificationChannelConfigVO channelConfig = readChannelConfig(setting.getChannelConfigJson());
        if (!getChannels(channelConfig, noticeType).contains(channel)) {
            return false;
        }
        return !isInDnd(readDndRule(setting.getDndRuleJson()), currentTime);
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

    private boolean isInDnd(ContentNotificationDndRuleVO dndRule, LocalTime currentTime) {
        if (!Boolean.TRUE.equals(dndRule.getEnabled())) {
            return false;
        }
        LocalTime startTime = parseTime(dndRule.getStartTime());
        LocalTime endTime = parseTime(dndRule.getEndTime());
        if (startTime.equals(endTime)) {
            return true;
        }
        if (startTime.isBefore(endTime)) {
            return !currentTime.isBefore(startTime) && currentTime.isBefore(endTime);
        }
        return !currentTime.isBefore(startTime) || currentTime.isBefore(endTime);
    }

    private LocalTime parseTime(String time) {
        if (time == null || time.isBlank()) {
            throw new JeecgBootException("免打扰时间不能为空");
        }
        return LocalTime.parse(time);
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

    private ContentNotificationDndRuleVO readDndRule(String json) {
        if (json == null || json.isBlank() || "{}".equals(json)) {
            return defaultDndRule();
        }
        try {
            ContentNotificationDndRuleVO rule = OBJECT_MAPPER.readValue(json, ContentNotificationDndRuleVO.class);
            if (rule.getEnabled() == null) {
                rule.setEnabled(Boolean.FALSE);
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

    private ContentNotificationDndRuleVO toDndRuleVO(ContentNotificationDndRuleReq req) {
        ContentNotificationDndRuleVO rule = new ContentNotificationDndRuleVO()
            .setEnabled(Boolean.TRUE.equals(req.getEnabled()))
            .setStartTime(req.getStartTime())
            .setEndTime(req.getEndTime());
        if (Boolean.TRUE.equals(rule.getEnabled())
            && (rule.getStartTime() == null || rule.getStartTime().isBlank()
            || rule.getEndTime() == null || rule.getEndTime().isBlank())) {
            throw new JeecgBootException("启用免打扰时必须配置开始和结束时间");
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
            .setEndTime(null);
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
