package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.entity.ContentUserProfileHistory;
import org.jeecg.modules.content.user.enums.ContentProfileHistoryTypeEnum;
import org.jeecg.modules.content.user.mapper.ContentUserProfileHistoryMapper;
import org.jeecg.modules.content.user.req.profile.ContentUserProfileUpdateReq;
import org.jeecg.modules.content.user.service.IContentUserProfileHistoryService;
import org.jeecg.modules.content.user.service.IContentUserProfileService;
import org.jeecg.modules.content.user.vo.ContentUserProfileHistoryVO;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 内容社区资料历史服务实现。
 */
@Service
public class ContentUserProfileHistoryServiceImpl implements IContentUserProfileHistoryService {

    private static final int HISTORY_LIMIT = 20;
    private static final int RETAIN_DAYS = 180;

    @Resource
    private ContentUserProfileHistoryMapper historyMapper;

    @Lazy
    @Resource
    private IContentUserProfileService profileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordEffectiveChange(String userId, String historyType, String previousValue, String sourceUpdateId) {
        if (previousValue == null || previousValue.trim().isEmpty()) {
            return;
        }
        ContentUserProfileHistory history = new ContentUserProfileHistory()
            .setUserId(userId)
            .setHistoryType(historyType)
            .setHistoryValue(previousValue)
            .setSourceUpdateId(sourceUpdateId)
            .setExpired(Boolean.FALSE)
            .setExpiresAt(addDays(new Date(), RETAIN_DAYS));
        history.setId(UUIDGenerator.generate());
        historyMapper.insert(history);
        pruneOverflow(userId, historyType);
    }

    @Override
    public List<ContentUserProfileHistoryVO> listHistory(String userId, String historyType) {
        return historyMapper.selectActiveByType(userId, historyType, HISTORY_LIMIT).stream()
            .map(ContentUserProfileHistoryVO::from)
            .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreHistory(String userId, String historyId) {
        if (historyId == null || historyId.trim().isEmpty()) {
            throw new JeecgBootException("历史ID不能为空");
        }
        ContentUserProfileHistory history = historyMapper.selectById(historyId);
        if (history == null || !userId.equals(history.getUserId()) || Boolean.TRUE.equals(history.getExpired())
            || history.getExpiresAt().before(new Date())) {
            throw new JeecgBootException("历史记录不存在或已过期");
        }
        ContentUserProfileUpdateReq req = new ContentUserProfileUpdateReq();
        if (ContentProfileHistoryTypeEnum.NICKNAME.getCode().equals(history.getHistoryType())) {
            req.setNickname(history.getHistoryValue());
        } else if (ContentProfileHistoryTypeEnum.AVATAR.getCode().equals(history.getHistoryType())) {
            req.setAvatar(history.getHistoryValue());
        } else {
            throw new JeecgBootException("历史类型不支持恢复");
        }
        profileService.updateProfile(userId, req);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupExpiredHistory() {
        List<ContentUserProfileHistory> histories = historyMapper.selectList(
            Wrappers.<ContentUserProfileHistory>lambdaQuery()
                .eq(ContentUserProfileHistory::getExpired, Boolean.FALSE)
                .lt(ContentUserProfileHistory::getExpiresAt, new Date()));
        for (ContentUserProfileHistory history : histories) {
            history.setExpired(Boolean.TRUE);
            historyMapper.updateById(history);
        }
        return histories.size();
    }

    private void pruneOverflow(String userId, String historyType) {
        List<ContentUserProfileHistory> histories = historyMapper.selectList(
            Wrappers.<ContentUserProfileHistory>lambdaQuery()
                .eq(ContentUserProfileHistory::getUserId, userId)
                .eq(ContentUserProfileHistory::getHistoryType, historyType)
                .eq(ContentUserProfileHistory::getExpired, Boolean.FALSE)
                .orderByDesc(ContentUserProfileHistory::getCreateTime));
        for (int i = HISTORY_LIMIT; i < histories.size(); i++) {
            ContentUserProfileHistory history = histories.get(i);
            history.setExpired(Boolean.TRUE);
            historyMapper.updateById(history);
        }
    }

    private Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }
}
