package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.vo.ContentInviteCodeVO;
import org.jeecg.modules.content.user.vo.ContentInviteInfoVO;
import org.jeecg.modules.content.user.vo.ContentInviteRecordPageVO;
import org.jeecg.modules.content.user.vo.ContentInviteStatsVO;

/**
 * 内容社区邀请服务契约。
 */
public interface IContentInviteService {

    /**
     * 生成或获取用户的邀请码。
     */
    ContentInviteCodeVO generateOrGetInviteCode(String userId);

    /**
     * 绑定邀请关系（注册时调用）。
     */
    void bindInviteRelation(String inviteCode, String inviteeUserId);

    /**
     * 分页查询邀请记录。
     */
    ContentInviteRecordPageVO listInviteRecords(String userId, Long pageNo, Long pageSize);

    /**
     * 查询邀请统计。
     */
    ContentInviteStatsVO getInviteStats(String userId);

    /**
     * 查询邀请码信息（用于落地页校验）。
     */
    ContentInviteInfoVO getInviteInfo(String inviteCode);
}
