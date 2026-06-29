package org.jeecg.modules.content.circle.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.req.create.CircleCreateReq;
import org.jeecg.modules.content.circle.req.query.CircleSearchReq;
import org.jeecg.modules.content.circle.req.update.CircleUpdateReq;
import org.jeecg.modules.content.circle.service.ICircleMemberService;
import org.jeecg.modules.content.circle.service.ICircleService;
import org.jeecg.modules.content.circle.vo.CircleSearchResultVO;
import org.jeecg.modules.content.circle.vo.CircleVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
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

    @Override
    @Transactional(readOnly = true)
    public Page<CircleVO> myList(Integer pageNum, Integer pageSize, String userId) {
        if (userId == null) {
            return new Page<>(pageNum, pageSize, 0);
        }

        LambdaQueryWrapper<CircleMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(CircleMember::getUserId, userId)
                .in(CircleMember::getStatus, CircleMember.Status.ACTIVE, CircleMember.Status.MUTED)
                .orderByDesc(CircleMember::getCreateTime);
        Page<CircleMember> memberPage = circleMemberService.page(new Page<>(pageNum, pageSize), memberWrapper);

        List<String> circleIds = memberPage.getRecords().stream()
                .map(CircleMember::getCircleId).collect(Collectors.toList());

        List<CircleVO> voList = new ArrayList<>();
        if (!circleIds.isEmpty()) {
            List<Circle> circles = circleService.listByIds(circleIds);
            Map<String, Circle> circleMap = circles.stream()
                    .collect(Collectors.toMap(Circle::getId, c -> c));
            for (CircleMember member : memberPage.getRecords()) {
                Circle circle = circleMap.get(member.getCircleId());
                if (circle != null) {
                    voList.add(toVO(circle, true, member.getRole().name()));
                }
            }
        }

        Page<CircleVO> result = new Page<>(pageNum, pageSize, memberPage.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CircleVO> publicList(Integer pageNum, Integer pageSize, String userId) {
        LambdaQueryWrapper<Circle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Circle::getPrivacyType, Circle.PrivacyType.PUBLIC)
                .eq(Circle::getStatus, Circle.Status.ACTIVE)
                .orderByDesc(Circle::getMemberCount);

        Page<Circle> page = circleService.page(new Page<>(pageNum, pageSize), wrapper);

        List<CircleVO> voList = convertCircleListToVOList(page.getRecords(), userId);

        Page<CircleVO> result = new Page<>(pageNum, pageSize, page.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public CircleVO getDetail(String circleId, String userId) {
        Circle circle = circleService.getById(circleId);
        if (circle == null) {
            throw new JeecgBootException("圈子不存在");
        }

        CircleMember member = null;
        if (userId != null) {
            member = circleMemberService.findByCircleAndUser(circleId, userId);
        }

        boolean joined = member != null && member.getStatus() == CircleMember.Status.ACTIVE;
        String myRole = member != null ? member.getRole().name() : null;
        return toVO(circle, joined, myRole);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CircleSearchResultVO> search(CircleSearchReq req, String userId) {
        LambdaQueryWrapper<Circle> wrapper = new LambdaQueryWrapper<Circle>()
                .eq(Circle::getStatus, Circle.Status.ACTIVE)
                .eq(Circle::getPrivacyType, Circle.PrivacyType.PUBLIC);

        if (StringUtils.hasText(req.getKeyword())) {
            String keyword = req.getKeyword()
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
            wrapper.and(w -> w
                    .like(Circle::getName, keyword)
                    .or()
                    .like(Circle::getDescription, keyword));
        }

        wrapper.orderByDesc(Circle::getMemberCount);

        Page<Circle> page = new Page<>(req.getPageNum(), req.getPageSize());
        circleService.page(page, wrapper);

        Set<String> joinedCircleIds = getJoinedCircleIds(page.getRecords(), userId);

        List<CircleSearchResultVO> voList = page.getRecords().stream().map(c -> {
            CircleSearchResultVO vo = new CircleSearchResultVO();
            vo.setId(c.getId());
            vo.setName(c.getName());
            vo.setIconUrl(c.getIconUrl());
            vo.setDescription(c.getDescription());
            vo.setCategory(c.getCategory());
            vo.setMemberCount(c.getMemberCount());
            vo.setJoined(joinedCircleIds.contains(c.getId()));
            return vo;
        }).collect(Collectors.toList());

        Page<CircleSearchResultVO> result = new Page<>(req.getPageNum(), req.getPageSize(), page.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkNameAvailable(String name) {
        try {
            circleService.checkNameUnique(name);
            return true;
        } catch (JeecgBootException e) {
            if ("该圈子名称已存在，请修改".equals(e.getMessage())) {
                return false;
            }
            throw e;
        }
    }

    private Set<String> getJoinedCircleIds(List<Circle> circles, String userId) {
        if (userId == null || circles.isEmpty()) {
            return Collections.emptySet();
        }
        List<String> circleIds = circles.stream().map(Circle::getId).collect(Collectors.toList());
        LambdaQueryWrapper<CircleMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CircleMember::getUserId, userId)
                .in(CircleMember::getCircleId, circleIds)
                .eq(CircleMember::getStatus, CircleMember.Status.ACTIVE);
        return circleMemberService.list(wrapper).stream()
                .map(CircleMember::getCircleId)
                .collect(Collectors.toSet());
    }

    private List<CircleVO> convertCircleListToVOList(List<Circle> circles, String userId) {
        if (circles.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> joinedCircleIds = getJoinedCircleIds(circles, userId);
        Map<String, CircleMember> memberMap = Collections.emptyMap();
        if (userId != null) {
            List<String> circleIds = circles.stream().map(Circle::getId).collect(Collectors.toList());
            LambdaQueryWrapper<CircleMember> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CircleMember::getUserId, userId)
                    .in(CircleMember::getCircleId, circleIds);
            memberMap = circleMemberService.list(wrapper).stream()
                    .collect(Collectors.toMap(CircleMember::getCircleId, m -> m));
        }

        Map<String, CircleMember> finalMemberMap = memberMap;
        return circles.stream().map(c -> {
            boolean joined = joinedCircleIds.contains(c.getId());
            CircleMember member = finalMemberMap.get(c.getId());
            String role = member != null ? member.getRole().name() : null;
            return toVO(c, joined, role);
        }).collect(Collectors.toList());
    }

    private CircleVO toVO(Circle circle, boolean joined, String myRole) {
        CircleVO vo = new CircleVO();
        vo.setId(circle.getId());
        vo.setName(circle.getName());
        vo.setDescription(circle.getDescription());
        vo.setIconUrl(circle.getIconUrl());
        vo.setCoverUrl(circle.getCoverUrl());
        vo.setCategory(circle.getCategory());
        if (circle.getPrivacyType() != null) {
            vo.setPrivacyType(circle.getPrivacyType().name());
        }
        if (circle.getJoinType() != null) {
            vo.setJoinType(circle.getJoinType().name());
        }
        vo.setCreatorId(circle.getCreatorId());
        vo.setMemberCount(circle.getMemberCount());
        vo.setMaxMemberCount(circle.getMaxMemberCount());
        if (circle.getStatus() != null) {
            vo.setStatus(circle.getStatus().name());
        }
        vo.setCreateTime(circle.getCreateTime());
        vo.setJoined(joined);
        vo.setMyRole(myRole);
        return vo;
    }
}
