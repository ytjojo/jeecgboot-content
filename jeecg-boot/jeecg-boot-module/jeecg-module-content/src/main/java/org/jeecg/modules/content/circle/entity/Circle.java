package org.jeecg.modules.content.circle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_circle")
public class Circle extends JeecgEntity {

    @Schema(description = "圈子名称")
    private String name;

    @Schema(description = "圈子简介")
    private String description;

    @Schema(description = "图标URL")
    private String iconUrl;

    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "分类标签")
    private String category;

    @Schema(description = "隐私类型: PUBLIC/PRIVATE/PASSWORD")
    private PrivacyType privacyType;

    @Schema(description = "加入方式: DIRECT/APPROVAL/INVITE/PASSWORD")
    private JoinType joinType;

    @Schema(description = "密码保护密码哈希(BCrypt)")
    private String passwordHash;

    @Schema(description = "创建者用户ID")
    private String creatorId;

    @Schema(description = "成员数")
    private Integer memberCount;

    @Schema(description = "最大成员数")
    private Integer maxMemberCount;

    @Schema(description = "状态: ACTIVE/DISABLED")
    private Status status;

    public enum PrivacyType {
        PUBLIC, PRIVATE, PASSWORD
    }

    public enum JoinType {
        DIRECT, APPROVAL, INVITE, PASSWORD
    }

    public enum Status {
        ACTIVE, DISABLED
    }
}
