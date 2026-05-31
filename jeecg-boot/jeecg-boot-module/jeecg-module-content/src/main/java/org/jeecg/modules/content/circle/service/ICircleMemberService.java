package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.circle.entity.CircleMember;

public interface ICircleMemberService extends IService<CircleMember> {

    CircleMember findByCircleAndUser(String circleId, String userId);

    void checkAlreadyMember(String circleId, String userId);

    void checkNotMuted(String circleId, String userId);

    void checkCreatorPermission(String circleId, String operatorId);

    void checkModeratorManageable(String circleId, String targetUserId);
}
