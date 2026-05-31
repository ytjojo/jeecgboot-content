package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.circle.entity.CircleJoinRequest;

import java.util.List;

/**
 * 圈子加入申请审核服务接口。
 */
public interface ICircleJoinReviewService extends IService<CircleJoinRequest> {

    /**
     * 批准加入申请。
     *
     * @param requestId  申请ID
     * @param operatorId 操作人ID
     */
    void approve(String requestId, String operatorId);

    /**
     * 拒绝加入申请。
     *
     * @param requestId  申请ID
     * @param operatorId 操作人ID
     * @param reason     拒绝原因
     */
    void reject(String requestId, String operatorId, String reason);

    /**
     * 查询圈子待审核的加入申请。
     *
     * @param circleId 圈子ID
     * @return 待审核申请列表
     */
    List<CircleJoinRequest> getPendingRequests(String circleId);

    /**
     * 查询超时未处理的加入申请（超过3天）。
     *
     * @return 超时申请列表
     */
    List<CircleJoinRequest> getTimedOutRequests();
}
