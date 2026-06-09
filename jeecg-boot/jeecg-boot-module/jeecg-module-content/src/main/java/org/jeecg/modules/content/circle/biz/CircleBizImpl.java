package org.jeecg.modules.content.circle.biz;

import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.req.create.CircleCreateReq;
import org.jeecg.modules.content.circle.req.update.CircleUpdateReq;
import org.jeecg.modules.content.circle.service.ICircleMemberService;
import org.jeecg.modules.content.circle.service.ICircleService;
import org.jeecg.modules.content.circle.vo.CircleVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CircleBizImpl implements ICircleBiz {

    @Value("${circle.max-member-count:500}")
    private int defaultMaxMemberCount;

    @Resource
    private ICircleService circleService;

    @Resource
    private ICircleMemberService circleMemberService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CircleVO createCircle(CircleCreateReq req, String userId) {
        circleService.checkNameUnique(req.getName());

        Circle circle = new Circle();
        circle.setName(req.getName());
        circle.setDescription(req.getDescription());
        circle.setIconUrl(req.getIconUrl());
        circle.setCoverUrl(req.getCoverUrl());
        circle.setCategory(req.getCategory());
        circle.setPrivacyType(Circle.PrivacyType.valueOf(req.getPrivacyType()));
        circle.setJoinType(Circle.JoinType.valueOf(req.getJoinType()));
        circle.setCreatorId(userId);
        circle.setMemberCount(1);
        circle.setMaxMemberCount(defaultMaxMemberCount);
        circle.setStatus(Circle.Status.ACTIVE);

        if (circle.getPrivacyType() == Circle.PrivacyType.PASSWORD) {
            if (req.getPassword() == null || req.getPassword().isBlank()) {
                throw new JeecgBootException("密码保护圈子必须设置密码");
            }
            circle.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }

        circleService.save(circle);

        CircleMember member = new CircleMember();
        member.setCircleId(circle.getId());
        member.setUserId(userId);
        member.setRole(CircleMember.Role.CREATOR);
        member.setStatus(CircleMember.Status.ACTIVE);
        circleMemberService.save(member);

        return toVO(circle, true, CircleMember.Role.CREATOR.name());
    }

    @Override
    @Transactional
    public void updateCircle(CircleUpdateReq req, String userId) {
        Circle circle = circleService.getById(req.getCircleId());
        if (circle == null) {
            throw new JeecgBootException("圈子不存在");
        }
        if (!circle.getCreatorId().equals(userId)) {
            throw new JeecgBootException("仅创建者可修改圈子信息");
        }

        boolean changed = false;
        if (req.getDescription() != null) {
            circle.setDescription(req.getDescription());
            changed = true;
        }
        if (req.getIconUrl() != null) {
            circle.setIconUrl(req.getIconUrl());
            changed = true;
        }
        if (req.getCoverUrl() != null) {
            circle.setCoverUrl(req.getCoverUrl());
            changed = true;
        }
        if (req.getCategory() != null) {
            circle.setCategory(req.getCategory());
            changed = true;
        }
        if (changed) {
            circleService.updateById(circle);
        }
    }

    private CircleVO toVO(Circle circle, boolean joined, String myRole) {
        CircleVO vo = new CircleVO();
        vo.setId(circle.getId());
        vo.setName(circle.getName());
        vo.setDescription(circle.getDescription());
        vo.setIconUrl(circle.getIconUrl());
        vo.setCoverUrl(circle.getCoverUrl());
        vo.setCategory(circle.getCategory());
        vo.setPrivacyType(circle.getPrivacyType().name());
        vo.setJoinType(circle.getJoinType().name());
        vo.setCreatorId(circle.getCreatorId());
        vo.setMemberCount(circle.getMemberCount());
        vo.setMaxMemberCount(circle.getMaxMemberCount());
        vo.setStatus(circle.getStatus().name());
        vo.setCreateTime(circle.getCreateTime());
        vo.setJoined(joined);
        vo.setMyRole(myRole);
        return vo;
    }
}
