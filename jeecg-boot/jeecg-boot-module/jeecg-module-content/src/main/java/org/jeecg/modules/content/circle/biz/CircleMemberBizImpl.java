package org.jeecg.modules.content.circle.biz;

import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.req.create.CircleJoinReq;
import org.jeecg.modules.content.circle.req.update.CircleMemberUpdateReq;
import org.jeecg.modules.content.circle.service.ICircleGovernanceLogService;
import org.jeecg.modules.content.circle.service.ICircleMemberService;
import org.jeecg.modules.content.circle.service.ICircleService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CircleMemberBizImpl implements ICircleMemberBiz {

    @Resource
    private ICircleService circleService;

    @Resource
    private ICircleMemberService circleMemberService;

    @Resource
    private ICircleGovernanceLogService governanceLogService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void joinCircle(CircleJoinReq req, String userId) {
        Circle circle = circleService.getById(req.getCircleId());
        if (circle == null) {
            throw new JeecgBootException("圈子不存在");
        }

        CircleMember existing = circleMemberService.findByCircleAndUser(req.getCircleId(), userId);
        if (existing != null && existing.getStatus() != CircleMember.Status.REMOVED) {
            circleMemberService.checkNotMuted(req.getCircleId(), userId);
            throw new JeecgBootException("您已是圈子成员");
        }
        if (existing != null && existing.getStatus() == CircleMember.Status.MUTED) {
            circleMemberService.checkNotMuted(req.getCircleId(), userId);
        }

        switch (circle.getJoinType()) {
            case DIRECT:
                break; // 直接加入
            case APPROVAL:
                throw new JeecgBootException("申请已提交，请等待审核");
            case INVITE:
                throw new JeecgBootException("该圈子仅限邀请加入");
            case PASSWORD:
                if (req.getPassword() == null || !passwordEncoder.matches(req.getPassword(), circle.getPasswordHash())) {
                    throw new JeecgBootException("密码错误");
                }
                break;
        }

        // 增加成员数
        circleService.incrementMemberCount(req.getCircleId());

        // 创建成员记录
        if (existing != null) {
            existing.setStatus(CircleMember.Status.ACTIVE);
            existing.setRole(CircleMember.Role.MEMBER);
            circleMemberService.updateById(existing);
        } else {
            CircleMember member = new CircleMember();
            member.setCircleId(req.getCircleId());
            member.setUserId(userId);
            member.setRole(CircleMember.Role.MEMBER);
            member.setStatus(CircleMember.Status.ACTIVE);
            circleMemberService.save(member);
        }
    }

    @Override
    @Transactional
    public void leaveCircle(String circleId, String userId) {
        Circle circle = circleService.getById(circleId);
        if (circle == null) {
            throw new JeecgBootException("圈子不存在");
        }
        if (circle.getCreatorId().equals(userId)) {
            throw new JeecgBootException("创建者不可退出圈子，请先转让或解散圈子");
        }

        CircleMember member = circleMemberService.findByCircleAndUser(circleId, userId);
        if (member == null || member.getStatus() == CircleMember.Status.REMOVED) {
            throw new JeecgBootException("您不是该圈子成员");
        }

        member.setStatus(CircleMember.Status.REMOVED);
        circleMemberService.updateById(member);
        circleService.decrementMemberCount(circleId);
    }

    @Override
    @Transactional
    public void changeRole(CircleMemberUpdateReq req, String operatorId) {
        circleMemberService.checkCreatorPermission(req.getCircleId(), operatorId);

        CircleMember target = circleMemberService.findByCircleAndUser(req.getCircleId(), req.getTargetUserId());
        if (target == null) {
            throw new JeecgBootException("目标用户不是圈子成员");
        }
        if (target.getRole() == CircleMember.Role.CREATOR) {
            throw new JeecgBootException("创建者角色不可变更");
        }

        String fromRole = target.getRole().name();
        target.setRole(CircleMember.Role.valueOf(req.getTargetRole()));
        circleMemberService.updateById(target);

        governanceLogService.logRoleChange(req.getCircleId(), operatorId, req.getTargetUserId(), fromRole, req.getTargetRole());
    }

    @Override
    @Transactional
    public void muteMember(CircleMemberUpdateReq req, String operatorId) {
        CircleMember operator = circleMemberService.findByCircleAndUser(req.getCircleId(), operatorId);
        if (operator == null || operator.getRole() == CircleMember.Role.MEMBER) {
            throw new JeecgBootException("权限不足，仅创建者和版主可禁言成员");
        }

        CircleMember target = circleMemberService.findByCircleAndUser(req.getCircleId(), req.getTargetUserId());
        if (target == null || target.getStatus() == CircleMember.Status.REMOVED) {
            throw new JeecgBootException("目标用户不是圈子成员");
        }

        if (operator.getRole() == CircleMember.Role.MODERATOR && target.getRole() == CircleMember.Role.MODERATOR) {
            throw new JeecgBootException("权限不足，仅创建者可管理版主");
        }

        target.setStatus(CircleMember.Status.MUTED);
        target.setMuteEndTime(calculateMuteEndTime(req.getMuteDuration()));
        circleMemberService.updateById(target);

        governanceLogService.logMute(req.getCircleId(), operatorId, req.getTargetUserId(), req.getReason(), req.getMuteDuration());
    }

    @Override
    @Transactional
    public void unmuteMember(String circleId, String targetUserId, String operatorId) {
        CircleMember target = circleMemberService.findByCircleAndUser(circleId, targetUserId);
        if (target == null || target.getStatus() != CircleMember.Status.MUTED) {
            throw new JeecgBootException("该成员未被禁言");
        }

        target.setStatus(CircleMember.Status.ACTIVE);
        target.setMuteEndTime(null);
        circleMemberService.updateById(target);

        governanceLogService.logUnmute(circleId, operatorId, targetUserId);
    }

    @Override
    @Transactional
    public void removeMember(CircleMemberUpdateReq req, String operatorId) {
        CircleMember operator = circleMemberService.findByCircleAndUser(req.getCircleId(), operatorId);
        if (operator == null || operator.getRole() == CircleMember.Role.MEMBER) {
            throw new JeecgBootException("权限不足，仅创建者和版主可移除成员");
        }

        CircleMember target = circleMemberService.findByCircleAndUser(req.getCircleId(), req.getTargetUserId());
        if (target == null || target.getStatus() == CircleMember.Status.REMOVED) {
            throw new JeecgBootException("目标用户不是圈子成员");
        }

        if (operator.getRole() == CircleMember.Role.MODERATOR && target.getRole() == CircleMember.Role.MODERATOR) {
            throw new JeecgBootException("权限不足，仅创建者可管理版主");
        }

        target.setStatus(CircleMember.Status.REMOVED);
        circleMemberService.updateById(target);
        circleService.decrementMemberCount(req.getCircleId());

        governanceLogService.logRemove(req.getCircleId(), operatorId, req.getTargetUserId(), req.getReason());
    }

    private LocalDateTime calculateMuteEndTime(String duration) {
        if (duration == null || "PERMANENT".equals(duration)) {
            return null; // 永久禁言
        }
        return switch (duration) {
            case "1h" -> LocalDateTime.now().plusHours(1);
            case "24h" -> LocalDateTime.now().plusHours(24);
            case "7d" -> LocalDateTime.now().plusDays(7);
            default -> throw new JeecgBootException("无效的禁言时长: " + duration);
        };
    }
}
