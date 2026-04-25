package org.jeecg.modules.community.entity;

import com.baomidou.mybatisplus.annotation.*;
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

import java.util.Date;

/**
 * 社群成员实体类
 * 对应数据库表：community_members
 * 用于存储社群成员的信息和状态
 * 
 * @author system
 * @since 2024-12-16
 */
@Data
@TableName("community_members")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CommunityMember", description = "社群成员")
public class CommunityMember extends JeecgEntity {
    private static final long serialVersionUID = 1L;

 

    /**
     * 社区ID
     */
    @Excel(name = "社区ID", width = 15)
    @Schema(description = "社区ID")
    @TableField("community_id")
    private String communityId;

    /**
     * 用户ID
     */
    @Excel(name = "用户ID", width = 15)
    @Schema(description = "用户ID")
    @TableField("user_id")
    private String userId;

    /**
     * 成员角色：1-普通成员 2-版主 3-管理员 4-创建者
     */
    @Excel(name = "成员角色", width = 15, dicCode = "community_member_role")
    @Schema(description = "成员角色：1-普通成员 2-版主 3-管理员 4-创建者")
    @TableField("role")
    private MemberRoleEnum role;

    /**
     * 成员状态：1-正常 2-禁言 3-踢出
     */
    @Excel(name = "成员状态", width = 15, dicCode = "community_member_status")
    @Schema(description = "成员状态：0-已退出 1-正常 2-待审核 3-被踢出 4-申请被拒绝")
    @TableField("status")
    private MemberJoinStatusEnum status;



    /**
     * 邀请人ID（如果是邀请加入）
     */
    @Excel(name = "邀请人ID", width = 15)
    @Schema(description = "邀请人ID（如果是邀请加入）")
    @TableField("inviter_id")
    private String inviterId;

    /**
     * 邀请时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "邀请时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "邀请时间")
    @TableField("inviter_at")
    private Date inviterAt;

    /**
     * 审核时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审核时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "审核时间")
    @TableField("approved_at")
    private Date approvedAt;

    /**
     * 审核人
     */
    @Excel(name = "审核人", width = 15)
    @Schema(description = "审核人")
    @TableField("approved_by")
    private String approvedBy;

    /**
     * 加入方式
     */
    @Excel(name = "加入方式", width = 15, dicCode = "community_member_join_type")
    @Schema(description = "加入方式：1-自由加入 2-申请通过 3-邀请加入")
    @TableField("join_type")
    private JoinTypeEnum joinType;

    /**
     * 加入时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "加入时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "加入时间")
    @TableField("join_at")
    private Date joinAt;

    /**
     * 禁言到期时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "禁言到期时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "禁言到期时间")
    @TableField("mute_until")
    private Date muteUntil;

    /**
     * 禁言原因
     */
    @Excel(name = "禁言原因", width = 30)
    @Schema(description = "禁言原因")
    @TableField("mute_reason")
    private String muteReason;

    /**
     * 最后活跃时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后活跃时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最后活跃时间")
    @TableField("last_active_time")
    private Date lastActiveTime;

    /**
     * 发帖数量
     */
    @Excel(name = "发帖数量", width = 15)
    @Schema(description = "发帖数量")
    @TableField("post_count")
    private Long postCount;

    /**
     * 评论数量
     */
    @Excel(name = "评论数量", width = 15)
    @Schema(description = "评论数量")
    @TableField("comment_count")
    private Long commentCount;

    /**
     * 点赞数量
     */
    @Excel(name = "点赞数量", width = 15)
    @Schema(description = "点赞数量")
    @TableField("like_count")
    private Long likeCount;

   
    /**
     * 扩展数据（JSON格式）
     */
    @Schema(description = "扩展数据")
    @TableField("ext_data")
    private String extData;

    /**
     * 删除标志：0-正常 1-删除
     */
    @Excel(name = "删除标志", width = 15)
    @Schema(description = "删除标志")
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;

}