package org.jeecg.modules.channel.constant;

/**
 * 频道相关常量定义
 * 
 * @author system
 * @since 2024-01-20
 */
public class ChannelConstant {

   

   
    /**
     * 频道可见性常量
     */
    public static class Visibility {
        /** 公开频道 */
        public static final Integer PUBLIC = 1;
        /** 私有频道 */
        public static final Integer PRIVATE = 0;
    }

    /**
     * 禁言类型常量 - 新增
     */
    public static class MuteType {
        /** 未禁言 */
        public static final Integer UNMUTE = 0;
        /** 发布内容禁言 */
        public static final Integer POST_MUTE = 1;
        /** 评论禁言 */
        public static final Integer COMMENT_MUTE = 2;
        /** 全局禁言（发言+评论） */
        public static final Integer GLOBAL_MUTE = 3;
    }

    /**
     * 频道权限类型常量
     */
    public static class PermissionType {
        /** 查看频道 */
        public static final String VIEW_CHANNEL = "VIEW_CHANNEL";
        /** 发送消息 */
        public static final String SEND_MESSAGE = "SEND_MESSAGE";
        /** 管理消息 */
        public static final String MANAGE_MESSAGE = "MANAGE_MESSAGE";
        /** 邀请成员 */
        public static final String INVITE_MEMBER = "INVITE_MEMBER";
        /** 管理成员 */
        public static final String MANAGE_MEMBER = "MANAGE_MEMBER";
        /** 管理频道 */
        public static final String MANAGE_CHANNEL = "MANAGE_CHANNEL";
        /** 删除频道 */
        public static final String DELETE_CHANNEL = "DELETE_CHANNEL";
        /** 禁言权限 - 新增 */
        public static final String MUTE_MEMBER = "MUTE_MEMBER";
        
        // 权限类型编码常量
        /** 查看权限编码 */
        public static final Integer VIEW_CODE = 1;
        /** 发帖权限编码 */
        public static final Integer POST_CODE = 2;
        /** 评论权限编码 */
        public static final Integer COMMENT_CODE = 3;
        /** 管理权限编码 */
        public static final Integer MANAGE_CODE = 4;
        /** 禁言权限编码 - 新增 */
        public static final Integer MUTE_CODE = 5;
        
        // Shiro权限字符串格式常量
        /** 频道访问权限格式 */
        public static final String CHANNEL_ACCESS_FORMAT = "channel:%s:access";
        /** 频道管理权限格式 */
        public static final String CHANNEL_MANAGE_FORMAT = "channel:%s:manage";
        /** 频道删除权限格式 */
        public static final String CHANNEL_DELETE_FORMAT = "channel:%s:delete";
        /** 频道发帖权限格式 */
        public static final String CHANNEL_POST_FORMAT = "channel:%s:post";
        /** 频道成员管理权限格式 */
        public static final String CHANNEL_MEMBER_MANAGE_FORMAT = "channel:%s:member:manage";
        /** 频道禁言权限格式 - 新增 */
        public static final String CHANNEL_MUTE_FORMAT = "channel:%s:mute";
        
        /**
         * 生成频道权限字符串
         * @param channelId 频道ID
         * @param operation 操作类型
         * @return 权限字符串
         */
        public static String buildChannelPermission(String channelId, String operation) {
            return String.format("channel:%s:%s", channelId, operation);
        }
        
        /**
         * 生成频道角色权限字符串
         * @param channelId 频道ID
         * @param role 角色
         * @return 角色权限字符串
         */
        public static String buildChannelRole(String channelId, String role) {
            return String.format("channel:%s:%s", channelId, role);
        }
    }

    /**
     * 频道权限值常量
     */
    public static class PermissionValue {
        /** 允许 */
        public static final Integer ALLOW = 1;
        /** 拒绝 */
        public static final Integer DENY = 0;
        /** 继承 */
        public static final Integer INHERIT = -1;
    }

    /**
     * 频道权限状态常量
     */
    public static class PermissionStatus {
        /** 启用 */
        public static final Integer ENABLED = 1;
        /** 禁用 */
        public static final Integer DISABLED = 0;
    }

    /**
     * 频道排序字段常量
     */
    public static class OrderField {
        /** 按创建时间排序 */
        public static final String CREATE_TIME = "create_time";
        /** 按更新时间排序 */
        public static final String UPDATE_TIME = "update_time";
        /** 按频道名称排序 */
        public static final String CHANNEL_NAME = "channel_name";
        /** 按成员数量排序 */
        public static final String MEMBER_COUNT = "member_count";
        /** 按内容数量排序 */
        public static final String CONTENT_COUNT = "content_count";
    }

    /**
     * 频道缓存键前缀常量
     */
    public static class CacheKey {
        /** 频道详情缓存键前缀 */
        public static final String CHANNEL_DETAIL = "channel:detail:";
        /** 频道成员缓存键前缀 */
        public static final String CHANNEL_MEMBER = "channel:member:";
        /** 频道权限缓存键前缀 */
        public static final String CHANNEL_PERMISSION = "channel:permission:";
        /** 用户频道列表缓存键前缀 */
        public static final String USER_CHANNELS = "user:channels:";
    }

    /**
     * 频道配置常量
     */
    public static class Config {
        /** 频道名称最大长度 */
        public static final int MAX_CHANNEL_NAME_LENGTH = 50;
        /** 频道描述最大长度 */
        public static final int MAX_CHANNEL_DESCRIPTION_LENGTH = 500;
        /** 频道标签最大数量 */
        public static final int MAX_CHANNEL_TAGS_COUNT = 10;
        /** 频道成员默认最大数量 */
        public static final int DEFAULT_MAX_MEMBER_COUNT = 1000;
        /** 频道层级最大深度 */
        public static final int MAX_CHANNEL_DEPTH = 5;
    }

    /**
     * 频道操作类型常量
     */
    public static class OperationType {
        /** 创建频道 */
        public static final String CREATE = "CREATE";
        /** 更新频道 */
        public static final String UPDATE = "UPDATE";
        /** 删除频道 */
        public static final String DELETE = "DELETE";
        /** 加入频道 */
        public static final String JOIN = "JOIN";
        /** 离开频道 */
        public static final String LEAVE = "LEAVE";
        /** 邀请成员 */
        public static final String INVITE = "INVITE";
        /** 踢出成员 */
        public static final String KICK = "KICK";
    }

    /**
     * 频道消息类型常量
     */
    public static class MessageType {
        /** 系统消息 */
        public static final String SYSTEM = "SYSTEM";
        /** 用户消息 */
        public static final String USER = "USER";
        /** 公告消息 */
        public static final String ANNOUNCEMENT = "ANNOUNCEMENT";
    }

    /**
     * 频道事件类型常量
     */
    public static class EventType {
        /** 频道创建事件 */
        public static final String CHANNEL_CREATED = "CHANNEL_CREATED";
        /** 频道更新事件 */
        public static final String CHANNEL_UPDATED = "CHANNEL_UPDATED";
        /** 频道删除事件 */
        public static final String CHANNEL_DELETED = "CHANNEL_DELETED";
        /** 成员加入事件 */
        public static final String MEMBER_JOINED = "MEMBER_JOINED";
        /** 成员离开事件 */
        public static final String MEMBER_LEFT = "MEMBER_LEFT";
        /** 权限变更事件 */
        public static final String PERMISSION_CHANGED = "PERMISSION_CHANGED";
    }

     public static class MuteDuration {
        /** 10分钟 */
        public static final int TEN_MINUTES = 10;
        /** 30分钟 */
        public static final int THIRTY_MINUTES = 30;
        /** 1小时 */
        public static final int ONE_HOUR = 60;
        /** 3小时 */
        public static final int THREE_HOURS = 180;
        /** 12小时 */
        public static final int TWELVE_HOURS = 720;
        /** 1天 */
        public static final int ONE_DAY = 1440;
        /** 3天 */
        public static final int THREE_DAYS = 4320;
        /** 7天 */
        public static final int SEVEN_DAYS = 10080;
        /** 30天 */
        public static final int THIRTY_DAYS = 43200;
        /** 永久禁言 */
        public static final int PERMANENT = Integer.MAX_VALUE;
        
        /**
         * 获取所有预设时长选项
         * @return 时长选项数组
         */
        public static int[] getAllDurations() {
            return new int[]{
                TEN_MINUTES, THIRTY_MINUTES, ONE_HOUR, THREE_HOURS,
                TWELVE_HOURS, ONE_DAY, THREE_DAYS, SEVEN_DAYS, THIRTY_DAYS
            };
        }
        
        /**
         * 格式化时长显示
         * @param minutes 分钟数
         * @return 格式化后的时长字符串
         */
        public static String formatDuration(int minutes) {
            if (minutes == PERMANENT) {
                return "永久";
            } else if (minutes >= ONE_DAY) {
                return (minutes / ONE_DAY) + "天";
            } else if (minutes >= ONE_HOUR) {
                return (minutes / ONE_HOUR) + "小时";
            } else {
                return minutes + "分钟";
            }
        }

         /**
     * 禁言原因预设值
     */
    public static class MuteReason {
        /** 发布违规内容 */
        public static final String INAPPROPRIATE_CONTENT = "发布违规内容";
        /** 恶意刷屏 */
        public static final String SPAM = "恶意刷屏";
        /** 人身攻击 */
        public static final String PERSONAL_ATTACK = "人身攻击";
        /** 发布广告 */
        public static final String ADVERTISEMENT = "发布广告";
        /** 政治敏感内容 */
        public static final String POLITICAL_CONTENT = "政治敏感内容";
        /** 色情低俗内容 */
        public static final String PORNOGRAPHIC_CONTENT = "色情低俗内容";
        /** 违反频道规则 */
        public static final String RULE_VIOLATION = "违反频道规则";
        /** 其他原因 */
        public static final String OTHER = "其他原因";
        
        /**
         * 获取所有预设原因
         * @return 原因数组
         */
        public static String[] getAllReasons() {
            return new String[]{
                INAPPROPRIATE_CONTENT, SPAM, PERSONAL_ATTACK, ADVERTISEMENT,
                POLITICAL_CONTENT, PORNOGRAPHIC_CONTENT, RULE_VIOLATION, OTHER
            };
        }
    }
    }
}