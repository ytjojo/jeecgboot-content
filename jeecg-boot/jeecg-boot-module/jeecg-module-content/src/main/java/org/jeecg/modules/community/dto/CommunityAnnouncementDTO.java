package org.jeecg.modules.community.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.jeecg.modules.community.entity.CommunityAnnouncement;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 社群公告DTO
 * 用于服务层创建和更新社群公告时的数据传输
 * 符合DTO技术规范，包含转换方法
 * 
 * @author system
 * @since 2024-12-16
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CommunityAnnouncementDTO", description = "社群公告参数")
public class CommunityAnnouncementDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 公告ID（更新时需要）
     */
    @Schema(description = "公告ID")
    private String id;
    
    /**
     * 社群ID
     */
    @NotBlank(message = "社群ID不能为空")
    @Schema(description = "社群ID", required = true)
    private String communityId;
    
    /**
     * 公告标题
     */
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 200, message = "公告标题长度不能超过200个字符")
    @Schema(description = "公告标题", required = true)
    private String title;
    
    /**
     * 公告内容
     */
    @NotBlank(message = "公告内容不能为空")
    @Size(max = 5000, message = "公告内容长度不能超过5000个字符")
    @Schema(description = "公告内容", required = true)
    private String content;
    
    /**
     * 公告类型（1-普通公告 2-重要公告 3-紧急公告）
     */
    @NotNull(message = "公告类型不能为空")
    @Schema(description = "公告类型", required = true)
    private Integer type;
    
    /**
     * 是否置顶（0-否 1-是）
     */
    @Schema(description = "是否置顶")
    private Integer isTop;
    
    /**
     * 发布状态（0-草稿 1-已发布 2-已撤回）
     */
    @Schema(description = "发布状态")
    private Integer publishStatus;
    
    /**
     * 生效时间
     */
    @Schema(description = "生效时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;
    
    /**
     * 过期时间
     */
    @Schema(description = "过期时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;
    
    /*
    /**
     * 排序号
     */
    @Schema(description = "排序号")
    private Integer sortOrder;
    
    /**
     * 转换为实体对象
     * @return CommunityAnnouncement实体对象
     */
    public CommunityAnnouncement toEntity() {
        CommunityAnnouncement entity = new CommunityAnnouncement();
        entity.setCommunityId(this.communityId);
        entity.setTitle(this.title);
        entity.setContent(this.content);
        entity.setType(this.type);
        entity.setIsPinned(this.isTop);  // isTop -> isPinned
        entity.setStatus(this.publishStatus);  // publishStatus -> status
        entity.setPublishTime(this.publishTime);  // publishTime -> publishTime
        entity.setExpireTime(this.expireTime);  // expireTime -> expiresAt
        entity.setSortOrder(this.sortOrder);
        
      
        
        // 设置时间
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        
        // 设置默认值
        if (entity.getStatus() == null) {
            entity.setStatus(0); // 默认草稿状态
        }
        if (entity.getIsPinned() == null) {
            entity.setIsPinned(0); // 默认不置顶
        }
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(0); // 默认排序号
        }
        
        return entity;
    }
    
    /**
     * 更新实体对象
     * @param entity 要更新的实体对象
     */
    public void updateEntity(CommunityAnnouncement entity) {
        if (this.title != null) {
            entity.setTitle(this.title);
        }
        if (this.content != null) {
            entity.setContent(this.content);
        }
        if (this.type != null) {
            entity.setType(this.type);
        }
        if (this.isTop != null) {
            entity.setIsPinned(this.isTop);  // isTop -> isPinned
        }
        if (this.publishStatus != null) {
            entity.setStatus(this.publishStatus);  // publishStatus -> status
        }
   
        if (this.expireTime != null) {
            entity.setExpireTime(expireTime); // expireTime -> expiresAt
        }
       
        if (this.sortOrder != null) {
            entity.setSortOrder(this.sortOrder);
        }
        
        // 设置更新时间
        entity.setUpdateTime(new Date());
    }




      /**
     * 社群名称（需要关联查询获取）
     */
    @Schema(description = "社群名称")
    private String communityName;
    
    /**
     * 公告类型名称（需要通过枚举转换获取）
     */
    @Schema(description = "公告类型名称")
    private String typeName;
    
    /**
     * 发布状态名称（需要通过枚举转换获取）
     */
    @Schema(description = "发布状态名称")
    private String statusName;
    
   
    /**
     * 浏览次数
     */
    @Schema(description = "浏览次数")
    private Integer viewCount;
    
    /**
     * 发布人ID
     */
    @Schema(description = "发布人ID")
    private String publishBy;
    
    /**
     * 发布人昵称（需要关联查询获取）
     */
    @Schema(description = "发布人昵称")
    private String publishByName;
    

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;
    
    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;
    
    /**
     * 更新人
     */
    @Schema(description = "更新人")
    private String updateBy;
    
    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;
}