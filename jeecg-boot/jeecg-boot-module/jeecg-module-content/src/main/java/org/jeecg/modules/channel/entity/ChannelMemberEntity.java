package org.jeecg.modules.channel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecg.modules.channel.constant.MemberJoinStatusEnum;
import org.jeecg.modules.channel.constant.MemberRoleEnum;
import org.jeecg.modules.content.constant.JoinTypeEnum;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 频道成员实体类
 * 对应数据库表：channel_members
 * 用于管理频道成员关系和角色权限
 * 
 * @author jeecg-boot
 * @version V1.0
 * @since 2024-01-01
 */
@Data
@TableName(value = "channel_members", autoResultMap = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "频道成员实体")
public class ChannelMemberEntity extends JeecgEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 频道ID
     */
    @Excel(name = "频道ID", width = 15)
    @Schema(description = "频道ID")
    private String channelId;

    /**
     * 用户ID
     */
    @Excel(name = "用户ID", width = 15)
    @Schema(description = "用户ID")
    private String userId;

    /**
     * 加入方式：1-自由加入 2-申请通过 3-邀请加入
     */
    @Excel(name = "加入方式", width = 10)
    @Schema(description = "加入方式：1-自由加入 2-申请通过 3-邀请加入")
    private JoinTypeEnum joinType;

    /**
     * 成员角色：支持多角色位掩码
     * 1-所有者 2-管理员 4-版主 8-普通成员 16-访客
     * 可以通过位运算组合多个角色，例如：3表示同时拥有所有者和管理员角色
     */
    @Excel(name = "成员角色", width = 10)
    @Schema(description = "成员角色：支持多角色位掩码，1-所有者 2-管理员 4-版主 8-普通成员 16-访客")
    private MemberRoleEnum role;

    /**
     * 成员状态：0-已退出 1-正常 2-被禁言 3-待审核
     */
    @Excel(name = "成员状态", width = 10, replace = { "成员状态：0-已退出 1-正常 2-待审核 3-被踢出 4-申请被拒绝" })
    @Schema(description = "成员状态：0-已退出 1-正常 2-待审核 3-被踢出 4-申请被拒绝")
    private MemberJoinStatusEnum status;
    /**
     * 特殊权限设置（JSON格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "特殊权限设置")
    private Object permissions;

    /**
     * 加入时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "加入时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "加入时间")
    private LocalDateTime joinedAt;

    /**
     * 最后活跃时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后活跃时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最后活跃时间")
    private LocalDateTime lastActiveTime;

    /**
     * 审核人
     * 如果别人邀请自己,自己则是审核人
     */
    @Schema(description = "审核人")
    private String approvedBy;

    /**
     * 邀请人ID 如果是自己表示是申请
     */
    @Schema(description = "邀请人ID")
    private String inviterId;

    /**
     * 是否订阅：0-否 1-是
     */
    @Excel(name = "是否订阅", width = 10)
    @Schema(description = "是否订阅：0-否 1-是")
    private Integer isSubscribed;

    /**
     * 邀请时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "邀请时间")
    private LocalDateTime invitedAt;

    /**
     * 审核时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "审核时间")
    private LocalDateTime approvedAt;


     /**
     * 禁言原因
     * 当权限类型为禁言时，记录禁言的具体原因
     */
    @Excel(name = "禁言原因", width = 50)
    @Schema(description = "禁言原因")
    private String muteReason;

    /**
     * 禁言操作人ID
     * 记录执行禁言操作的管理员或系统用户ID
     */
    @Excel(name = "禁言操作人ID", width = 15)
    @Schema(description = "禁言操作人ID")
    private String muteOperatorId;

   

    /**
     * 禁言开始时间
     * 禁言生效的具体时间点，可以设置延迟生效
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "禁言开始时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "禁言开始时间")
    private LocalDateTime muteStartTime;

    /**
     * 禁言结束时间
     * 禁言解除的时间点，null表示永久禁言
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "禁言结束时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "禁言结束时间，null表示永久禁言")
    private LocalDateTime muteEndTime;

    /**
     * 是否为永久禁言
     * true-永久禁言，false-临时禁言
     */
    @Excel(name = "是否永久禁言", width = 10, replace = {"否_0", "是_1"})
    @Schema(description = "是否为永久禁言")
    private Integer isPermanentMute;

    /**
     * 禁言类型
     * 0-未禁言 1-发言禁言 2-评论禁言 3-全局禁言（发言+评论）
     */
    @Excel(name = "禁言类型", width = 10, replace = {"未禁言_0", "发言禁言_1", "评论禁言_2", "全局禁言_3"})
    @Schema(description = "禁言类型：0-未禁言 1-发言禁言 2-评论禁言 3-全局禁言")
    private Integer muteType;

    @TableLogic()
    @Schema(description = "删除标识")
    private Integer delFlag;

    /**
     * 排序顺序
     */
    @Excel(name = "排序顺序", width = 10)
    @Schema(description = "排序顺序")
    private Integer sortOrder;
}