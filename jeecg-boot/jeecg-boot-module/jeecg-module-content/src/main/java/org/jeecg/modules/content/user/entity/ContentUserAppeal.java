package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecg.modules.content.user.req.support.ContentAppealCreateReq;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_appeal")
public class ContentUserAppeal extends JeecgEntity {

    private String userId;
    private String appealType;
    private String targetId;
    private String targetType;
    private String status;
    private String reason;
    private String evidenceJson;
    private String progressNote;

    public static ContentUserAppeal from(ContentAppealCreateReq req) {
        return new ContentUserAppeal()
            .setUserId(req.getUserId())
            .setAppealType(req.getAppealType())
            .setTargetId(req.getTargetId())
            .setTargetType(req.getTargetType())
            .setReason(req.getReason())
            .setEvidenceJson(req.getEvidenceJson())
            .setStatus("PENDING");
    }
}
