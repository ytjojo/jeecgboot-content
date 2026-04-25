package org.jeecg.modules.community.constant;

/**
 * 社群模块常量类
 * 定义社群模块中使用的各种常量
 * 
 * @author system
 * @since 2024-12-16
 */
public class CommunityConstants {
    
    /**
     * 缓存相关常量
     */
    public static class Cache {
        /** 社群信息缓存前缀 */
        public static final String COMMUNITY_INFO_PREFIX = "community:info:";
        
        /** 社群成员缓存前缀 */
        public static final String COMMUNITY_MEMBER_PREFIX = "community:member:";
        
        /** 社群统计缓存前缀 */
        public static final String COMMUNITY_STATS_PREFIX = "community:stats:";
        
        /** 用户社群列表缓存前缀 */
        public static final String USER_COMMUNITIES_PREFIX = "user:communities:";
        
        /** 社群公告缓存前缀 */
        public static final String COMMUNITY_ANNOUNCEMENT_PREFIX = "community:announcement:";
        
        /** 缓存过期时间（秒）- 1小时 */
        public static final long CACHE_EXPIRE_TIME = 3600L;
        
        /** 统计缓存过期时间（秒）- 30分钟 */
        public static final long STATS_CACHE_EXPIRE_TIME = 1800L;
    }
    
    /**
     * 权限相关常量
     */
    public static class Permission {
        /** 社群管理权限 */
        public static final String COMMUNITY_MANAGE = "community:manage";
        
        /** 社群查看权限 */
        public static final String COMMUNITY_VIEW = "community:view";
        
        /** 社群创建权限 */
        public static final String COMMUNITY_CREATE = "community:create";
        
        /** 社群编辑权限 */
        public static final String COMMUNITY_EDIT = "community:edit";
        
        /** 社群删除权限 */
        public static final String COMMUNITY_DELETE = "community:delete";
        
        /** 成员管理权限 */
        public static final String MEMBER_MANAGE = "community:member:manage";
        
        /** 公告管理权限 */
        public static final String ANNOUNCEMENT_MANAGE = "community:announcement:manage";
    }
    
    /**
     * 业务规则常量
     */
    public static class BusinessRule {
        /** 社群名称最大长度 */
        public static final int COMMUNITY_NAME_MAX_LENGTH = 100;
        
        /** 社群描述最大长度 */
        public static final int COMMUNITY_DESCRIPTION_MAX_LENGTH = 1000;
        
        /** 社群规则最大长度 */
        public static final int COMMUNITY_RULES_MAX_LENGTH = 5000;
        
        /** 社群公告最大长度 */
        public static final int COMMUNITY_ANNOUNCEMENT_MAX_LENGTH = 2000;
        
        /** 邀请码长度 */
        public static final int INVITATION_CODE_LENGTH = 8;
        
        /** 邀请码有效期（天） */
        public static final int INVITATION_EXPIRE_DAYS = 7;
        
        /** 申请有效期（天） */
        public static final int JOIN_REQUEST_EXPIRE_DAYS = 30;
        
        /** 单个用户最大可创建社群数量 */
        public static final int MAX_COMMUNITIES_PER_USER = 10;
        
        /** 单个社群最大成员数量 */
        public static final int MAX_MEMBERS_PER_COMMUNITY = 100000;
        
        /** 禁言最大天数 */
        public static final int MAX_MUTE_DAYS = 365;
    }
    
    /**
     * 消息模板常量
     */
    public static class MessageTemplate {
        /** 加入社群成功消息 */
        public static final String JOIN_SUCCESS = "欢迎加入社群：{0}";
        
        /** 申请被拒绝消息 */
        public static final String JOIN_REJECTED = "您的加入申请被拒绝，社群：{0}，原因：{1}";
        
        /** 邀请消息 */
        public static final String INVITATION_MESSAGE = "您被邀请加入社群：{0}，邀请人：{1}";
        
        /** 被踢出社群消息 */
        public static final String KICKED_OUT = "您已被移出社群：{0}";
        
        /** 被禁言消息 */
        public static final String MUTED = "您在社群 {0} 中被禁言至 {1}";
    }
    
    /**
     * 事件类型常量
     */
    public static class EventType {
        /** 社群创建事件 */
        public static final String COMMUNITY_CREATED = "community.created";
        
        /** 成员加入事件 */
        public static final String MEMBER_JOINED = "community.member.joined";
        
        /** 成员离开事件 */
        public static final String MEMBER_LEFT = "community.member.left";
        
        /** 成员被踢出事件 */
        public static final String MEMBER_KICKED = "community.member.kicked";
        
        /** 成员被禁言事件 */
        public static final String MEMBER_MUTED = "community.member.muted";
        
        /** 公告发布事件 */
        public static final String ANNOUNCEMENT_PUBLISHED = "community.announcement.published";
    }
    
    /**
     * 默认值常量
     */
    public static class DefaultValue {
        /** 默认社群类型 */
        public static final Integer DEFAULT_COMMUNITY_TYPE = 1;
        
        /** 默认加入方式 */
        public static final Integer DEFAULT_JOIN_TYPE = 1;
        
        /** 默认发帖权限 */
        public static final Integer DEFAULT_POST_PERMISSION = 1;
        
        /** 默认成员角色 */
        public static final Integer DEFAULT_MEMBER_ROLE = 1;
        
        /** 默认成员状态 */
        public static final Integer DEFAULT_MEMBER_STATUS = 1;
        
        /** 默认申请状态 */
        public static final Integer DEFAULT_REQUEST_STATUS = 0;
        
        /** 默认邀请状态 */
        public static final Integer DEFAULT_INVITATION_STATUS = 0;
    }
    
    /**
     * 正则表达式常量
     */
    public static class Regex {
        /** 社群名称正则（中英文、数字、下划线、连字符） */
        public static final String COMMUNITY_NAME_PATTERN = "^[\u4e00-\u9fa5a-zA-Z0-9_-]+$";
        
        /** 邀请码正则（大小写字母和数字） */
        public static final String INVITATION_CODE_PATTERN = "^[a-zA-Z0-9]+$";
    }
}