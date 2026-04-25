package org.jeecg.modules.channel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 频道禁言功能配置类
 * 提供禁言相关的配置参数和默认值
 * 
 * @author jeecg-boot
 * @version V1.0
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "jeecg.channel.mute")
public class ChannelMuteConfig {

    /**
     * 是否启用禁言功能
     * 默认启用
     */
    private boolean enabled = true;

    /**
     * 默认禁言时长（分钟）
     * 当未指定禁言时长时使用此默认值
     */
    private int defaultMuteDuration = 60;

    /**
     * 最大禁言时长（分钟）
     * 防止设置过长的禁言时间
     */
    private int maxMuteDuration = 43200; // 30天

    /**
     * 最小禁言时长（分钟）
     * 防止设置过短的禁言时间
     */
    private int minMuteDuration = 1;

    /**
     * 是否允许永久禁言
     * 默认允许
     */
    private boolean allowPermanentMute = true;

    /**
     * 批量禁言最大用户数量
     * 防止一次性禁言过多用户
     */
    private int maxBatchMuteUsers = 100;

    /**
     * 禁言历史记录保留天数
     * 超过此天数的禁言记录将被清理
     */
    private int muteHistoryRetentionDays = 90;

    /**
     * 是否启用自动清理过期禁言
     * 默认启用
     */
    private boolean autoCleanExpiredMutes = true;

    /**
     * 自动清理过期禁言的执行间隔（分钟）
     * 默认每小时执行一次
     */
    private int cleanupIntervalMinutes = 60;

    /**
     * 是否记录禁言操作日志
     * 默认记录
     */
    private boolean logMuteOperations = true;

    /**
     * 是否发送禁言通知
     * 默认发送
     */
    private boolean sendMuteNotifications = true;

    /**
     * 禁言通知模板ID
     * 用于发送禁言通知的消息模板
     */
    private String muteNotificationTemplateId = "channel_mute_notification";

    /**
     * 解除禁言通知模板ID
     * 用于发送解除禁言通知的消息模板
     */
    private String unmuteNotificationTemplateId = "channel_unmute_notification";

    /**
     * 是否启用禁言缓存
     * 默认启用，提高查询性能
     */
    private boolean enableMuteCache = true;

    /**
     * 禁言缓存过期时间（秒）
     * 默认5分钟
     */
    private int muteCacheExpireSeconds = 300;

    /**
     * 禁言检查缓存键前缀
     */
    private String muteCacheKeyPrefix = "channel:mute:";

    /**
     * 是否启用禁言状态预检查
     * 在执行敏感操作前预先检查用户禁言状态
     */
    private boolean enableMutePreCheck = true;

    /**
     * 禁言原因最大长度
     * 限制禁言原因的字符数
     */
    private int maxMuteReasonLength = 500;

    /**
     * 默认禁言原因
     * 当未指定禁言原因时使用此默认值
     */
    private String defaultMuteReason = "违反频道规则";

    /**
     * 是否允许用户申请解除禁言
     * 默认不允许
     */
    private boolean allowMuteAppeal = false;

    /**
     * 禁言申诉审核超时时间（小时）
     * 超过此时间未处理的申诉将自动关闭
     */
    private int appealTimeoutHours = 72;

    /**
     * 获取禁言缓存键
     * 
     * @param channelId 频道ID
     * @param userId 用户ID
     * @return 缓存键
     */
    public String getMuteCacheKey(String channelId, String userId) {
        return muteCacheKeyPrefix + channelId + ":" + userId;
    }

    /**
     * 获取禁言类型缓存键
     * 
     * @param channelId 频道ID
     * @param userId 用户ID
     * @param muteType 禁言类型
     * @return 缓存键
     */
    public String getMuteTypeCacheKey(String channelId, String userId, int muteType) {
        return muteCacheKeyPrefix + channelId + ":" + userId + ":" + muteType;
    }

    /**
     * 验证禁言时长是否有效
     * 
     * @param duration 禁言时长（分钟）
     * @return 是否有效
     */
    public boolean isValidMuteDuration(int duration) {
        return duration >= minMuteDuration && duration <= maxMuteDuration;
    }

    /**
     * 验证批量禁言用户数量是否有效
     * 
     * @param userCount 用户数量
     * @return 是否有效
     */
    public boolean isValidBatchMuteUserCount(int userCount) {
        return userCount > 0 && userCount <= maxBatchMuteUsers;
    }

    /**
     * 验证禁言原因长度是否有效
     * 
     * @param reason 禁言原因
     * @return 是否有效
     */
    public boolean isValidMuteReasonLength(String reason) {
        return reason == null || reason.length() <= maxMuteReasonLength;
    }
}