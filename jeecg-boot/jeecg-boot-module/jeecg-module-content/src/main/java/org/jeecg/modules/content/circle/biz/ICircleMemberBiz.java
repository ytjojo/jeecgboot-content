package org.jeecg.modules.content.circle.biz;

import org.jeecg.modules.content.circle.req.create.CircleJoinReq;
import org.jeecg.modules.content.circle.req.update.CircleMemberUpdateReq;

public interface ICircleMemberBiz {

    void joinCircle(CircleJoinReq req, String userId);

    void leaveCircle(String circleId, String userId);

    void changeRole(CircleMemberUpdateReq req, String operatorId);

    void muteMember(CircleMemberUpdateReq req, String operatorId);

    void unmuteMember(String circleId, String targetUserId, String operatorId);

    void removeMember(CircleMemberUpdateReq req, String operatorId);
}
