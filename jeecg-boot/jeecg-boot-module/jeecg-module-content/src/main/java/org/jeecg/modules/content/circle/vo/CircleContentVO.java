package org.jeecg.modules.content.circle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.circle.entity.CircleContent;
import org.jeecg.modules.content.user.vo.ContentUserBadgeVO;

import java.util.List;

/**
 * 圈子内容 VO，携带作者佩戴的勋章信息。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "圈子内容详情")
public class CircleContentVO extends CircleContent {

    @Schema(description = "作者佩戴的勋章列表")
    private List<ContentUserBadgeVO> authorBadges;
}
