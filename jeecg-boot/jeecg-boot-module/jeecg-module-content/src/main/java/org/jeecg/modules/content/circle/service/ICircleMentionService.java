package org.jeecg.modules.content.circle.service;

import java.util.List;

/**
 * 圈子@提及服务接口。
 */
public interface ICircleMentionService {

    /**
     * 从内容文本中解析被@提及的用户ID。
     *
     * @param content 内容文本
     * @return 被提及的用户ID列表
     */
    List<String> parseMentions(String content);

    /**
     * 获取圈子中可被@提及的成员列表（用于输入联想）。
     *
     * @param circleId 圈子ID
     * @param keyword  关键词（可为空）
     * @return 匹配的成员用户ID列表
     */
    List<String> getMentionCandidates(String circleId, String keyword);

    /**
     * 异步发送@提及通知。
     *
     * @param circleId         圈子ID
     * @param contentId        内容ID
     * @param mentionedUserIds 被提及的用户ID列表
     * @param publisherId      发布者ID
     */
    void sendMentionNotifications(String circleId, String contentId,
                                  List<String> mentionedUserIds, String publisherId);
}
