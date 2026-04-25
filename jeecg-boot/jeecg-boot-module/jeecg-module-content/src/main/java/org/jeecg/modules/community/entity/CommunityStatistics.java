package org.jeecg.modules.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 社群统计实体类
 * 对应数据库表：community_statistics
 * 
 * @author system
 * @since 2024-12-16
 */
@Data
@TableName("community_statistics")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CommunityStatistics", description = "社群统计")
public class CommunityStatistics extends JeecgEntity {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;
    
    /**
     * 社群ID
     */
    @Excel(name = "社群ID", width = 15)
    @Schema(description = "社群ID")
    @TableField("community_id")
    private String communityId;
    
    /**
     * 统计日期
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "统计日期", width = 15, format = "yyyy-MM-dd")
    @Schema(description = "统计日期")
    @TableField("statistics_date")
    private Date statisticsDate;
    
    /**
     * 总成员数
     */
    @Excel(name = "总成员数", width = 15)
    @Schema(description = "总成员数")
    @TableField("total_members")
    private Integer totalMembers;
    
    /**
     * 活跃成员数
     */
    @Excel(name = "活跃成员数", width = 15)
    @Schema(description = "活跃成员数")
    @TableField("active_members")
    private Integer activeMembers;
    
    /**
     * 新增成员数
     */
    @Excel(name = "新增成员数", width = 15)
    @Schema(description = "新增成员数")
    @TableField("new_members")
    private Integer newMembers;
    
    /**
     * 退出成员数
     */
    @Excel(name = "退出成员数", width = 15)
    @Schema(description = "退出成员数")
    @TableField("left_members")
    private Integer leftMembers;
    
    /**
     * 总帖子数
     */
    @Excel(name = "总帖子数", width = 15)
    @Schema(description = "总帖子数")
    @TableField("total_posts")
    private Integer totalPosts;
    
    /**
     * 新增帖子数
     */
    @Excel(name = "新增帖子数", width = 15)
    @Schema(description = "新增帖子数")
    @TableField("new_posts")
    private Integer newPosts;
    
    /**
     * 总评论数
     */
    @Excel(name = "总评论数", width = 15)
    @Schema(description = "总评论数")
    @TableField("total_comments")
    private Integer totalComments;
    
    /**
     * 新增评论数
     */
    @Excel(name = "新增评论数", width = 15)
    @Schema(description = "新增评论数")
    @TableField("new_comments")
    private Integer newComments;
    
    /**
     * 总点赞数
     */
    @Excel(name = "总点赞数", width = 15)
    @Schema(description = "总点赞数")
    @TableField("total_likes")
    private Integer totalLikes;
    
    /**
     * 新增点赞数
     */
    @Excel(name = "新增点赞数", width = 15)
    @Schema(description = "新增点赞数")
    @TableField("new_likes")
    private Integer newLikes;
    
    /**
     * 总浏览量
     */
    @Excel(name = "总浏览量", width = 15)
    @Schema(description = "总浏览量")
    @TableField("total_views")
    private Integer totalViews;
    
    /**
     * 新增浏览量
     */
    @Excel(name = "新增浏览量", width = 15)
    @Schema(description = "新增浏览量")
    @TableField("new_views")
    private Integer newViews;
    
    /**
     * 申请加入数
     */
    @Excel(name = "申请加入数", width = 15)
    @Schema(description = "申请加入数")
    @TableField("join_requests")
    private Integer joinRequests;
    
    /**
     * 邀请数
     */
    @Excel(name = "邀请数", width = 15)
    @Schema(description = "邀请数")
    @TableField("invitations")
    private Integer invitations;
    
    /**
     * 活跃度评分
     */
    @Excel(name = "活跃度评分", width = 15)
    @Schema(description = "活跃度评分")
    @TableField("activity_score")
    private BigDecimal activityScore;
    
    /**
     * 健康度评分
     */
    @Excel(name = "健康度评分", width = 15)
    @Schema(description = "健康度评分")
    @TableField("health_score")
    private BigDecimal healthScore;
    
    /**
     * 成长指数
     */
    @Excel(name = "成长指数", width = 15)
    @Schema(description = "成长指数")
    @TableField("growth_index")
    private BigDecimal growthIndex;
    
    /**
     * 互动指数
     */
    @Excel(name = "互动指数", width = 15)
    @Schema(description = "互动指数")
    @TableField("interaction_index")
    private BigDecimal interactionIndex;
    
    /**
     * 内容质量指数
     */
    @Excel(name = "内容质量指数", width = 15)
    @Schema(description = "内容质量指数")
    @TableField("content_quality_index")
    private BigDecimal contentQualityIndex;
    
    /**
     * 用户留存率
     */
    @Excel(name = "用户留存率", width = 15)
    @Schema(description = "用户留存率")
    @TableField("retention_rate")
    private BigDecimal retentionRate;
    
    /**
     * 日活跃用户数
     */
    @Excel(name = "日活跃用户数", width = 15)
    @Schema(description = "日活跃用户数")
    @TableField("daily_active_users")
    private Integer dailyActiveUsers;
    
    /**
     * 周活跃用户数
     */
    @Excel(name = "周活跃用户数", width = 15)
    @Schema(description = "周活跃用户数")
    @TableField("weekly_active_users")
    private Integer weeklyActiveUsers;
    
    /**
     * 月活跃用户数
     */
    @Excel(name = "月活跃用户数", width = 15)
    @Schema(description = "月活跃用户数")
    @TableField("monthly_active_users")
    private Integer monthlyActiveUsers;
    
    /**
     * 平均在线时长（分钟）
     */
    @Excel(name = "平均在线时长", width = 15)
    @Schema(description = "平均在线时长（分钟）")
    @TableField("avg_online_time")
    private Integer avgOnlineTime;
    

    /**
     * 删除标志（0-正常，1-已删除）
     */
    @Schema(description = "删除标志")
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;
}