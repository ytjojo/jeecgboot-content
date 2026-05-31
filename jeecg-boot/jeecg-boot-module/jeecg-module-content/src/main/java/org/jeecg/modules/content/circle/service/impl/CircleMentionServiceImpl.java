package org.jeecg.modules.content.circle.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.circle.mapper.CircleMemberMapper;
import org.jeecg.modules.content.circle.service.ICircleMentionService;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 圈子@提及服务实现。
 */
@Slf4j
@Service
public class CircleMentionServiceImpl implements ICircleMentionService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\S+)");

    @Resource
    private CircleMemberMapper circleMemberMapper;

    @Resource
    private IContentNotificationService contentNotificationService;

    @Override
    public List<String> parseMentions(String content) {
        if (content == null || content.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> mentionedUserIds = new ArrayList<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            mentionedUserIds.add(matcher.group(1));
        }
        return mentionedUserIds;
    }

    @Override
    public List<String> getMentionCandidates(String circleId, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return circleMemberMapper.selectMemberUserIds(circleId);
        }
        return circleMemberMapper.selectMemberUserIdsByKeyword(circleId, keyword);
    }

    @Async
    @Override
    public void sendMentionNotifications(String circleId, String contentId,
                                         List<String> mentionedUserIds, String publisherId) {
        Set<String> activeMembers = new HashSet<>(
                circleMemberMapper.selectActiveMemberUserIds(circleId, mentionedUserIds));

        for (String userId : mentionedUserIds) {
            if (!activeMembers.contains(userId)) {
                log.info("用户 {} 已退出圈子 {}，跳过@提及通知", userId, circleId);
                continue;
            }
            try {
                contentNotificationService.sendNotification(
                        userId,
                        "MENTION",
                        "你被@提及了",
                        "圈子内容 " + contentId + " 中提到了你"
                );
            } catch (Exception e) {
                log.error("发送@提及通知失败，用户: {}, 内容: {}", userId, contentId, e);
            }
        }
    }
}
