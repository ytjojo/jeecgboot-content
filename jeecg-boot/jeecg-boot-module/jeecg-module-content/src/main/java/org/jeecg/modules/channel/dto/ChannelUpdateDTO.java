package org.jeecg.modules.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

import org.jeecg.modules.channel.constant.ChannelStatusEnum;
import org.jeecg.modules.channel.constant.ViewPermissionEnum;
import org.jeecg.modules.content.constant.JoinTypeEnum;
import org.jeecg.modules.content.constant.PostPermissionEnum;

/**
 * 频道更新DTO类
 * 用于更新频道时的数据传输
 * 包含可更新的频道字段
 * 
 * @author jeecg-boot
 * @version V1.0
 * @since 2024-01-01
 */
@Data
@Schema(description = "频道更新DTO")
public class ChannelUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 频道ID
     */
    @Schema(description = "频道ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /**
     * 频道名称
     */
    @Size(max = 50, message = "频道名称长度不能超过50个字符")
    @Schema(description = "频道名称")
    private String name;

    /**
     * 频道描述
     */
    @Size(max = 500, message = "频道描述长度不能超过500个字符")
    @Schema(description = "频道描述")
    private String description;

    /**
     * 频道头像
     */
    @Schema(description = "频道头像")
    private String channelAvatar;

    /**
     * 频道封面图
     */
    @Schema(description = "频道封面图")
    private String channelCoverImage;

    /**
     * 频道分类ID
     */
    @Schema(description = "频道分类ID")
    private String categoryId;

    /**
     * 是否公开：0-私有 1-公开
     */
    @Schema(description = "是否公开：0-私有 1-公开")
    private Integer isPublic;

    /**
     * 加入方式：1-自由加入 2-申请加入 3-邀请加入
     */
    @Schema(description = "加入方式：1-自由加入 2-申请加入 3-邀请加入")
    private JoinTypeEnum joinType;

    /**
     * 发帖权限：1-所有成员 2-管理员 3-版主及以上
     */
    @Schema(description = "发帖权限：1-所有成员 2-管理员 3-版主及以上")
    private PostPermissionEnum postPermission;

    /**
     * 查看权限：1-所有成员 2-管理员 3-版主及以上
     */
    @Schema(description = "查看权限：1-所有成员 2-管理员 3-版主及以上")
    private ViewPermissionEnum viewPermission;

    /**
     * 允许的内容类型
     */
    @Schema(description = "允许的内容类型")
    private List<String> allowedContentTypes;

    /**
     * 最大成员数
     */
    @Schema(description = "最大成员数")
    private Integer maxMembers;

    /**
     * 频道状态: -1 DELETED 频道删除, 0 DISABLED-禁用 ,1 ENABLED-正常 ,2 REVIEWING-审核中, 3 REJECTED-审核拒绝
     */
    @Schema(description = "频道状态: -1 DELETED 频道删除, 0 DISABLED-禁用 ,1 ENABLED-正常 ,2 REVIEWING-审核中, 3 REJECTED-审核拒绝")
    private ChannelStatusEnum status;

    /**
     * 是否推荐：0-否 1-是
     */
    @Schema(description = "是否推荐：0-否 1-是")
    private Integer isRecommend;

    /**
     * 排序权重
     */
    @Schema(description = "排序权重")
    private Integer sortOrder;

    /**
     * 频道规则
     */
    @Size(max = 2000, message = "频道规则长度不能超过2000个字符")
    @Schema(description = "频道规则")
    private String rules;

    /**
     * 频道公告
     */
    @Size(max = 1000, message = "频道公告长度不能超过1000个字符")
    @Schema(description = "频道公告")
    private String announcement;

    /**
     * 频道标签
     */
    @Schema(description = "频道标签")
    private List<String> tags;

    /**
     * 扩展数据（JSON格式）
     */
    @Schema(description = "扩展数据")
    private Object extData;
}