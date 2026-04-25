package org.jeecg.modules.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.experimental.Accessors;

import org.jeecg.modules.channel.constant.MemberRoleEnum;
import org.jeecg.modules.channel.constant.MemberJoinStatusEnum;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 社群成员基础DTO
 * 包含社群成员的核心基础信息，用于组合模式的基础类
 * 
 * @author system
 * @since 2024-12-16
 */
@Data
@Accessors(chain = true)
@Schema(name = "CommunityMemberBaseDTO", description = "社群成员基础信息")
public class CommunityMemberDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成员ID
     */
    @Schema(description = "成员ID")
    protected String id;

    /**
     * 社群ID
     */
    @Schema(description = "社群ID")
    protected String communityId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    protected String userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    protected String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    protected String nickname;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    protected String avatarUrl;

    /**
     *  成员角色：0-访客  1-普通成员 2-版主 3-管理员 4-拥有者
     */
    @Schema(description = "成员角色：0-访客  1-普通成员 2-版主 3-管理员 4-拥有者")
    protected MemberRoleEnum role;

    /**
     * 成员状态：0-已退出 1-正常 2-待审核 3-被踢出 4-申请被拒绝
     */
    @Schema(description = "成员状态")
    protected MemberJoinStatusEnum status;

    /**
     * 社群名称
     */
    @Schema(description = "社群名称")
    private String communityName;

    /**
     * 成员角色名称
     */
    @Schema(description = "成员角色名称")
    private String roleName;

    /**
     * 成员状态名称
     */
    @Schema(description = "成员状态名称")
    private String statusName;

    /**
     * 加入时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "加入时间")
    private Date joinTime;

    /**
     * 最后活跃时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最后活跃时间")
    private Date lastActiveTime;

    /**
     * 发帖数量
     */
    @Schema(description = "发帖数量")
    private Integer postCount;

    /**
     * 评论数量
     */
    @Schema(description = "评论数量")
    private Integer commentCount;

    /**
     * 点赞数量
     */
    @Schema(description = "点赞数量")
    private Integer likeCount;

    /**
     * 积分
     */
    @Schema(description = "积分")
    private Integer points;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

}