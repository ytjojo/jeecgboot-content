package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_channel_category")
@Schema(description = "平台频道分类")
public class ContentChannelCategory extends JeecgEntity {

    @Schema(description = "父级分类ID，null表示根分类")
    private String parentId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类路径，如 /001/002/003")
    private String path;

    @Schema(description = "分类层级 1-4")
    private Integer level;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "状态 0=停用 1=启用")
    private Integer status;

    @Schema(description = "是否特殊分类 0=普通 1=特殊")
    private Integer isSystem;
}
