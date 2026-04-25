package org.jeecg.modules.community.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.jeecg.modules.channel.constant.ViewPermissionEnum;
import org.jeecg.modules.community.entity.CommunityEntity;
import org.jeecg.modules.community.enums.CommunityStatusEnum;
import org.jeecg.modules.community.enums.CommunityTypeEnum;
import org.jeecg.modules.content.constant.JoinTypeEnum;
import org.jeecg.modules.content.constant.PostPermissionEnum;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 社群详情DTO
 * 用于社群详情展示，包含完整的社群信息
 * 
 * @author system
 * @since 2024-12-16
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CommunityDetailDTO", description = "社群详情信息")
public class CommunityDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 社群ID
     */
    @Schema(description = "社群ID")
    protected String id;
    
    /**
     * 社群名称
     */
    @Schema(description = "社群名称")
    protected String name;
    
    /**
     * 社群头像URL
     */
    @Schema(description = "社群头像URL")
    protected String avatar;
    
    /**
     * 社群类型（1-公开社群 2-私密社群 3-付费社群）
     */
    @Schema(description = "社群类型")
    protected CommunityTypeEnum type;
    
    /**
     * 社群状态（0-禁用 1-启用）
     */
    @Schema(description = "社群状态")
    protected CommunityStatusEnum status;
    /**
     * 社群描述
     */
    @Schema(description = "社群描述")
    private String description;
    
    /**
     * 社群封面URL
     */
    @Schema(description = "社群封面URL")
    private String coverImage;
    
    /**
     * 加入方式（1-自由加入 2-申请加入 3-邀请加入）
     */
    @Schema(description = "加入方式")
    private JoinTypeEnum joinType;
    
    /**
     * 发帖权限（1-所有成员 2-版主及以上 3-管理员及以上 4-仅创建者）
     */
    @Schema(description = "发帖权限")
    private PostPermissionEnum postPermission;

    /**
     * 查看权限（1-所有成员 2-版主及以上 3-管理员及以上 4-仅创建者）
     */
    @Schema(description = "查看权限")
    private ViewPermissionEnum viewPermission;
    
    /**
     * 当前成员数量
     */
    @Schema(description = "当前成员数量")
    private Long memberCount;
    
    /**
     * 最大成员数量
     */
    @Schema(description = "最大成员数量")
    private Long maxMemberCount;
    
    /**
     * 帖子数量
     */
    @Schema(description = "帖子数量")
    private Long contentCount;
    
    /**
     * 社群标签列表
     */
    @Schema(description = "社群标签列表")
    private List<String> tagList;
    
    /**
     * 社群规则
     */
    @Schema(description = "社群规则")
    private String rules;
    
    /**
     * 社群公告
     */
    @Schema(description = "社群公告")
    private String announcement;



    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private String createBy;
    
    /**
     * 创建人昵称
     */
    @Schema(description = "创建人昵称")
    private String creatorNickname;
    
    /**
     * 创建人头像URL
     */
    @Schema(description = "创建人头像URL")
    private String creatorAvatarUrl;
    
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
    
    /**
     * 是否已加入（当前用户）
     */
    @Schema(description = "是否已加入")
    private Boolean isJoined;
    
    /**
     * 当前用户在该社群的角色（如果已加入）
     */
    @Schema(description = "当前用户角色")
    private Integer currentUserRole;
    
    /**
     * 当前用户在该社群的状态（如果已加入）
     */
    @Schema(description = "当前用户状态")
    private Integer currentUserStatus;
    
    /**
     * 排序号
     */
    @Schema(description = "排序号")
    private Integer sortOrder;




    /**
     * 从实体类转换为DTO
     * 
     * @param entity 社区实体
     * @return CommunityDTO
     */
    public static CommunityDTO fromEntity(CommunityEntity entity) {
        if (entity == null) {
            return null;
        }
        
        CommunityDTO dto = new CommunityDTO();
        dto.setId(entity.getId())
           .setName(entity.getName())
           .setDescription(entity.getDescription())
           .setAvatar(entity.getAvatar())
           .setCoverImage(entity.getCoverImage())
           .setType(entity.getType())
           .setStatus(CommunityStatusEnum.getByValue(entity.getStatus()))
           .setCreateBy(entity.getCreateBy())
           .setMemberCount(entity.getMemberCount())
           .setContentCount(entity.getContentCount())
           .setRules(entity.getRules())
           .setAnnouncement(entity.getAnnouncement())
           .setCreateTime(entity.getCreateTime())
           .setUpdateTime(entity.getUpdateTime());
        
        return dto;
    }
}