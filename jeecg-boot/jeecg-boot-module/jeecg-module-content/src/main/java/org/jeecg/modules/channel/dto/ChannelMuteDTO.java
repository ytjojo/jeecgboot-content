package org.jeecg.modules.channel.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 频道禁言数据传输对象
 * 用于服务层间的数据传输和业务逻辑处理
 * 
 * @author jeecg-boot
 * @version V1.0
 * @since 2024-01-01
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "ChannelMuteDTO", description = "频道禁言数据传输对象")
public class ChannelMuteDTO implements Serializable {

    private static final Long serialVersionUID = 1L;

    @Schema(description = "权限ID")
    private String id;

    @Schema(description = "频道ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String channelId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;

    @Schema(description = "用户名称")
    private String userName;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "禁言类型：1-发言禁言 2-评论禁言 3-全局禁言", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer muteType;

    @Schema(description = "禁言类型名称")
    private String muteTypeName;

    @Schema(description = "禁言原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private String muteReason;

    @Schema(description = "禁言操作人ID")
    private String muteOperatorId;

    @Schema(description = "禁言操作人姓名")
    private String muteOperatorName;

    @Schema(description = "禁言开始时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime muteStartTime;

    @Schema(description = "禁言结束时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime muteEndTime;

    @Schema(description = "是否永久禁言")
    private Boolean isPermanentMute;

    @Schema(description = "权限状态：1-有效 0-无效")
    private Integer status;

    @Schema(description = "是否已过期")
    private Boolean isExpired;

    @Schema(description = "剩余禁言时长（分钟）")
    private Long remainingMinutes;

    @Schema(description = "创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "更新时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "更新人")
    private String updateBy;

   

    /**
     * 从请求VO创建DTO
     * 
     * @param createVO 创建请求VO
     * @return DTO对象
     */
    public static ChannelMuteDTO fromRequest(org.jeecg.modules.channel.vo.req.ChannelMuteCreateVO createVO) {
        if (createVO == null) {
            return null;
        }
        
        ChannelMuteDTO dto = new ChannelMuteDTO();
        dto.setChannelId(createVO.getChannelId());
        dto.setUserId(createVO.getUserId());
        dto.setMuteType(createVO.getMuteType());
        dto.setMuteReason(createVO.getMuteReason());
        dto.setMuteEndTime(createVO.getMuteEndTime());
        dto.setIsPermanentMute(createVO.getIsPermanentMute());
        dto.setMuteStartTime(LocalDateTime.now());
        dto.setStatus(1); // 默认有效状态
        
        return dto;
    }

    /**
     * 获取禁言类型名称
     * 
     * @param muteType 禁言类型
     * @return 类型名称
     */
    private static String getMuteTypeName(Integer muteType) {
        if (muteType == null) {
            return "未知";
        }
        
        switch (muteType) {
            case 1:
                return "发言禁言";
            case 2:
                return "评论禁言";
            case 3:
                return "全局禁言";
            default:
                return "未知类型";
        }
    }

    /**
     * 检查是否为有效的禁言记录
     * 
     * @return 是否有效
     */
    public boolean isValidMute() {
        if (status == null || status != 1) {
            return false;
        }
        
        if (isPermanentMute != null && isPermanentMute) {
            return true;
        }
        
        if (muteEndTime != null) {
            return muteEndTime.isAfter(LocalDateTime.now());
        }
        
        return false;
    }

    /**
     * 获取禁言剩余时长描述
     * 
     * @return 时长描述
     */
    public String getRemainingTimeDesc() {
        if (isPermanentMute != null && isPermanentMute) {
            return "永久";
        }
        
        if (remainingMinutes == null || remainingMinutes <= 0) {
            return "已过期";
        }
        
        if (remainingMinutes < 60) {
            return remainingMinutes + "分钟";
        } else if (remainingMinutes < 1440) { // 24小时
            return (remainingMinutes / 60) + "小时" + (remainingMinutes % 60 > 0 ? (remainingMinutes % 60) + "分钟" : "");
        } else {
            long days = remainingMinutes / 1440;
            long hours = (remainingMinutes % 1440) / 60;
            return days + "天" + (hours > 0 ? hours + "小时" : "");
        }
    }



    /**
     * 检查禁言是否已过期
     * @return 是否已过期
     */
    public boolean checkExpired() {
        if (isPermanentMute != null && isPermanentMute) {
            return false;
        }
        if (muteEndTime == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(muteEndTime);
    }

    /**
     * 计算剩余禁言时间（分钟）
     * @return 剩余时间，如果已过期返回0
     */
    public long calculateRemainingMinutes() {
        if (checkExpired()) {
            return 0L;
        }
        if (isPermanentMute != null && isPermanentMute) {
            return Long.MAX_VALUE;
        }
        if (muteEndTime == null) {
            return 0L;
        }
        
        LocalDateTime now = LocalDateTime.now();
        return java.time.Duration.between(now, muteEndTime).toMinutes();
    }

}