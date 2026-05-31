package org.jeecg.modules.content.circle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.jeecg.modules.content.circle.entity.CircleJoinRequest;
import org.jeecg.modules.content.circle.enums.CircleJoinRequestStatusEnum;
import org.jeecg.modules.content.circle.mapper.CircleJoinRequestMapper;
import org.jeecg.modules.content.circle.service.ICircleJoinReviewService;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 圈子加入申请审核服务实现。
 */
@Service
public class CircleJoinReviewServiceImpl extends ServiceImpl<CircleJoinRequestMapper, CircleJoinRequest>
        implements ICircleJoinReviewService {

    @Resource
    private IContentNotificationService contentNotificationService;

    @Override
    public void approve(String requestId, String operatorId) {
        CircleJoinRequest request = getById(requestId);
        request.setStatus(CircleJoinRequestStatusEnum.APPROVED.getCode());
        request.setOperatorId(operatorId);
        request.setOperateTime(new Date());
        updateById(request);

        contentNotificationService.sendNotification(
                request.getUserId(), "JOIN_APPROVED",
                "加入申请已通过", "您的圈子加入申请已被批准");
    }

    @Override
    public void reject(String requestId, String operatorId, String reason) {
        CircleJoinRequest request = getById(requestId);
        request.setStatus(CircleJoinRequestStatusEnum.REJECTED.getCode());
        request.setOperatorId(operatorId);
        request.setOperateTime(new Date());
        request.setRejectReason(reason);
        updateById(request);

        contentNotificationService.sendNotification(
                request.getUserId(), "JOIN_REJECTED",
                "加入申请被拒绝", "您的圈子加入申请已被拒绝：" + reason);
    }

    @Override
    public List<CircleJoinRequest> getPendingRequests(String circleId) {
        return baseMapper.selectPendingByCircleId(circleId);
    }

    @Override
    public List<CircleJoinRequest> getTimedOutRequests() {
        return baseMapper.selectTimedOutRequests();
    }
}
