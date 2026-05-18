package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.req.profile.ContentUserProfileUpdateReq;

/**
 * 内容社区资料敏感词与 AI 审核适配接口。
 */
public interface IContentUserProfileAuditAdapter {

    AuditResult review(ContentUserProfileUpdateReq req);

    /**
     * 审核结果。
     */
    record AuditResult(boolean suspicious, String reason) {
    }
}
