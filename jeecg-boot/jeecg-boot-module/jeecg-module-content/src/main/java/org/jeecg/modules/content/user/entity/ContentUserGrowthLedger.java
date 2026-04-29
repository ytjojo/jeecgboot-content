package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_growth_ledger")
public class ContentUserGrowthLedger extends JeecgEntity {

    private String userId;
    private String sourceType;
    private String bizId;
    private Integer growthDelta;
    private Integer growthAfter;
    private String remark;

    public static ContentUserGrowthLedger of(String userId, String sourceType, int growthDelta) {
        return new ContentUserGrowthLedger()
            .setUserId(userId)
            .setSourceType(sourceType)
            .setGrowthDelta(growthDelta);
    }

    public static ContentUserGrowthLedger of(String userId, String sourceType, String bizId, int growthDelta, String remark) {
        return of(userId, sourceType, growthDelta)
            .setBizId(bizId)
            .setRemark(remark);
    }
}
