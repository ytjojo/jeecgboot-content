package org.jeecg.modules.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import org.jeecg.modules.channel.constant.ChannelStatusEnum;
import org.jeecg.modules.channel.constant.ViewPermissionEnum;
import org.jeecg.modules.channel.entity.ChannelEntity;
import org.jeecg.modules.content.constant.JoinTypeEnum;
import org.jeecg.modules.content.constant.PostPermissionEnum;


/**
 * 频道数据传输对象
 * 合并了ChannelBaseDTO、ChannelDetailDTO和ChannelItemDTO的所有功能
 * 用于频道相关的数据传输，包含频道的完整信息
 * 
 * @author jeecg-boot
 * @version V1.0
 * @since 2024-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "频道数据传输对象")
public class ChannelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== 基础字段（来自ChannelBaseDTO） ==========

    /**
     * 频道ID
     */
    @Schema(description = "频道ID")
    private String id;

    /**
     * 频道名称
     */
    @Schema(description = "频道名称")
    private String name;

    /**
     * 频道描述
     */
    @Schema(description = "频道描述")
    private String description;

    /**
     * 频道头像
     */
    @Schema(description = "频道头像")
    private String avatar;

    /**
     * 频道分类ID
     */
    @Schema(description = "频道分类ID")
    private String categoryId;

    /**
     * 创建者ID
     */
    @Schema(description = "创建者ID")
    private String creatorId;


    /**
     * 频道状态：0-禁用 1-正常 2-审核中
     */
    @Schema(description = "频道状态: -1 DELETED 频道删除, 0 DISABLED-禁用 ,1 ENABLED-正常 ,2 REVIEWING-审核中, 3 REJECTED-审核拒绝")
    private ChannelStatusEnum status;

    /**
     * 成员数量
     */
    @Schema(description = "成员数量")
    private Integer memberCount;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    // ========== 详情字段（来自ChannelDetailDTO和ChannelItemDTO） ==========

    /**
     * 频道封面图
     */
    @Schema(description = "频道封面图")
    private String coverImage;

    /**
     * 父频道ID
     */
    @Schema(description = "父频道ID")
    private String parentId;

    /**
     * 加入方式：1-自由加入 2-申请加入 3-邀请加入
     */
    @Schema(description = "加入方式：1-自由加入 2-申请加入 3-邀请加入")
    private JoinTypeEnum joinType;

    /**
     * 发帖权限：1-所有成员 2-管理员 3-版主及以上
     */
    @Schema(description = "发布权限：1-所有成员 2-管理员 3-版主及以上")
    private PostPermissionEnum postPermission;


    /**
     * 查看权限：0-所有成员 1-仅成员 3-管理员及以上 4-指定成员
     */
    @Schema(description = "查看权限：0-所有成员 1-仅成员 3-管理员及以上 4-指定成员")
    private ViewPermissionEnum viewPermission;

    /**
     * 允许的内容类型（JSON格式）
     */
    @Schema(description = "允许的内容类型")
    private List<String> allowedContentTypes;

    /**
     * 内容数量
     */
    @Schema(description = "内容数量")
    private Integer contentCount;

    /**
     * 最大成员数
     */
    @Schema(description = "最大成员数")
    private Integer maxMembers;

    /**
     * 是否推荐：0-否 1-是
     */
    @Schema(description = "是否推荐：0-否 1-是")
    private Integer isRecommended;

    /**
     * 排序权重
     */
    @Schema(description = "排序权重")
    private Integer sortOrder;

    /**
     * 频道规则
     */
    @Schema(description = "频道规则")
    private String rules;

    /**
     * 频道公告
     */
    @Schema(description = "频道公告")
    private String announcement;

    /**
     * 频道标签（多个标签用逗号分隔）
     */
    @Schema(description = "频道标签")
    private String tags;

    /**
     * 扩展数据（JSON格式）
     */
    @Schema(description = "扩展数据")
    private Object extData;

    // ========== 关联字段（来自ChannelDetailDTO和ChannelItemDTO） ==========

    /**
     * 创建者名称
     */
    @Schema(description = "创建者名称")
    private String creatorName;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称")
    private String categoryName;

    /**
     * 父频道名称
     */
    @Schema(description = "父频道名称")
    private String parentName;

    // ========== 系统字段（来自ChannelDetailDTO） ==========

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 更新人
     */
    @Schema(description = "更新人")
    private String updateBy;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

   
   
}