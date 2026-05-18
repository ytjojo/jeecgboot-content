package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.vo.ContentUserProfileHistoryVO;

import java.util.List;

/**
 * 内容社区资料历史服务契约。
 */
public interface IContentUserProfileHistoryService {

    void recordEffectiveChange(String userId, String historyType, String previousValue, String sourceUpdateId);

    List<ContentUserProfileHistoryVO> listHistory(String userId, String historyType);

    void restoreHistory(String userId, String historyId);

    int cleanupExpiredHistory();
}
