package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_point_ledger")
public class ContentUserPointLedger extends JeecgEntity {

    private String userId;
    private String sourceType;
    private String bizId;
    private Integer pointDelta;
    private Integer balanceAfter;
    private String dailyBucket;
    private String remark;

    public static ContentUserPointLedger of(String userId, String sourceType, int pointDelta) {
        return new ContentUserPointLedger()
            .setUserId(userId)
            .setSourceType(sourceType)
            .setPointDelta(pointDelta);
    }

    public static ContentUserPointLedger of(String userId, String sourceType, String bizId, int pointDelta, String remark) {
        return of(userId, sourceType, pointDelta)
            .setBizId(bizId)
            .setRemark(remark);
    }
}
