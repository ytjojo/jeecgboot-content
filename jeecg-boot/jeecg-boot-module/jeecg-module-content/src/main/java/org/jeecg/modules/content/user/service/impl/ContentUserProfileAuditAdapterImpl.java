package org.jeecg.modules.content.user.service.impl;

import org.jeecg.modules.content.user.req.profile.ContentUserProfileUpdateReq;
import org.jeecg.modules.content.user.service.IContentUserProfileAuditAdapter;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 默认资料审核适配器，真实敏感词和 AI 服务接入前提供可测试降级实现。
 */
@Service
public class ContentUserProfileAuditAdapterImpl implements IContentUserProfileAuditAdapter {

    private static final List<String> RISK_WORDS = List.of("违规", "敏感", "spam", "risk");

    @Override
    public AuditResult review(ContentUserProfileUpdateReq req) {
        String text = String.join(" ",
            nullToEmpty(req.getNickname()),
            nullToEmpty(req.getBio()),
            nullToEmpty(req.getAvatar())
        ).toLowerCase();
        for (String word : RISK_WORDS) {
            if (text.contains(word.toLowerCase())) {
                return new AuditResult(true, "资料命中风险规则：" + word);
            }
        }
        return new AuditResult(false, null);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
