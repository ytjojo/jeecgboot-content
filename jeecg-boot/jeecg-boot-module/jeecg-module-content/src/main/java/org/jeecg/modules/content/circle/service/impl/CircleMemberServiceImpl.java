package org.jeecg.modules.content.circle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.mapper.CircleMemberMapper;
import org.jeecg.modules.content.circle.service.ICircleMemberService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CircleMemberServiceImpl extends ServiceImpl<CircleMemberMapper, CircleMember> implements ICircleMemberService {

    @Override
    public CircleMember findByCircleAndUser(String circleId, String userId) {
        return getOne(new LambdaQueryWrapper<CircleMember>()
                .eq(CircleMember::getCircleId, circleId)
                .eq(CircleMember::getUserId, userId));
    }

    @Override
    public void checkAlreadyMember(String circleId, String userId) {
        long count = count(new LambdaQueryWrapper<CircleMember>()
                .eq(CircleMember::getCircleId, circleId)
                .eq(CircleMember::getUserId, userId)
                .ne(CircleMember::getStatus, CircleMember.Status.REMOVED));
        if (count > 0) {
            throw new JeecgBootException("您已是圈子成员");
        }
    }

    @Override
    public void checkNotMuted(String circleId, String userId) {
        CircleMember member = getOne(new LambdaQueryWrapper<CircleMember>()
                .eq(CircleMember::getCircleId, circleId)
                .eq(CircleMember::getUserId, userId));
        if (member == null) {
            return;
        }
        if (member.getStatus() == CircleMember.Status.MUTED) {
            if (member.getMuteEndTime() != null && member.getMuteEndTime().isBefore(LocalDateTime.now())) {
                member.setStatus(CircleMember.Status.ACTIVE);
                member.setMuteEndTime(null);
                updateById(member);
            } else {
                String endTime = member.getMuteEndTime() != null ? member.getMuteEndTime().toString() : "永久";
                throw new JeecgBootException("您已被禁言至 " + endTime);
            }
        }
    }

    @Override
    public void checkCreatorPermission(String circleId, String operatorId) {
        CircleMember operator = getOne(new LambdaQueryWrapper<CircleMember>()
                .eq(CircleMember::getCircleId, circleId)
                .eq(CircleMember::getUserId, operatorId));
        if (operator == null || operator.getRole() != CircleMember.Role.CREATOR) {
            throw new JeecgBootException("权限不足，仅创建者可管理角色");
        }
    }

    @Override
    public void checkModeratorManageable(String circleId, String targetUserId) {
        CircleMember target = getOne(new LambdaQueryWrapper<CircleMember>()
                .eq(CircleMember::getCircleId, circleId)
                .eq(CircleMember::getUserId, targetUserId));
        if (target != null && target.getRole() == CircleMember.Role.MODERATOR) {
            throw new JeecgBootException("权限不足，仅创建者可管理版主");
        }
    }
}
