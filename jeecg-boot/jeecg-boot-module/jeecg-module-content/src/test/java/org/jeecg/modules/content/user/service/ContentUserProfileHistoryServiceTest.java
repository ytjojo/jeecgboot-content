package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.entity.ContentUserProfileHistory;
import org.jeecg.modules.content.user.mapper.ContentUserProfileHistoryMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserProfileHistoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 内容社区资料历史服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentUserProfileHistoryServiceTest {

    @Mock
    private ContentUserProfileHistoryMapper historyMapper;

    @Mock
    private IContentUserProfileService profileService;

    @InjectMocks
    private ContentUserProfileHistoryServiceImpl historyService;

    @Test
    void shouldRecordEffectiveChangeAndPruneOverflow() {
        when(historyMapper.selectList(any())).thenReturn(histories(21));

        historyService.recordEffectiveChange("u1", "NICKNAME", "旧昵称", "review1");

        verify(historyMapper).insert(any(ContentUserProfileHistory.class));
        verify(historyMapper).updateById(org.mockito.ArgumentMatchers.<ContentUserProfileHistory>argThat(ContentUserProfileHistory::getExpired));
    }

    @Test
    void shouldNotRecordEmptyOrPendingHistoryValue() {
        historyService.recordEffectiveChange("u1", "AVATAR", "", "review1");

        verify(historyMapper, never()).insert(any(ContentUserProfileHistory.class));
    }

    @Test
    void shouldListHistoryInMapperOrderWithLimit() {
        when(historyMapper.selectActiveByType("u1", "NICKNAME", 20)).thenReturn(histories(2));

        assertThat(historyService.listHistory("u1", "NICKNAME")).hasSize(2);
    }

    @Test
    void shouldCleanupExpiredHistoryIdempotently() {
        ContentUserProfileHistory expired = history("h1");
        when(historyMapper.selectList(any())).thenReturn(List.of(expired), List.of());

        assertThat(historyService.cleanupExpiredHistory()).isEqualTo(1);
        assertThat(historyService.cleanupExpiredHistory()).isZero();
        verify(historyMapper).updateById(org.mockito.ArgumentMatchers.<ContentUserProfileHistory>argThat(ContentUserProfileHistory::getExpired));
    }

    @Test
    void shouldRestoreNicknameOrRejectUnavailableHistory() {
        ContentUserProfileHistory nickname = history("h1").setHistoryType("NICKNAME").setHistoryValue("旧昵称");
        when(historyMapper.selectById("h1")).thenReturn(nickname);

        historyService.restoreHistory("u1", "h1");

        verify(profileService).updateProfile(org.mockito.ArgumentMatchers.eq("u1"), org.mockito.ArgumentMatchers.argThat(req -> "旧昵称".equals(req.getNickname())));
        assertThatThrownBy(() -> historyService.restoreHistory("u1", ""))
            .hasMessageContaining("历史ID不能为空");
        when(historyMapper.selectById("missing")).thenReturn(null);
        assertThatThrownBy(() -> historyService.restoreHistory("u1", "missing"))
            .hasMessageContaining("历史记录不存在或已过期");
    }

    private List<ContentUserProfileHistory> histories(int count) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(index -> history("h" + index))
            .toList();
    }

    private ContentUserProfileHistory history(String id) {
        ContentUserProfileHistory history = new ContentUserProfileHistory()
            .setUserId("u1")
            .setHistoryType("NICKNAME")
            .setHistoryValue("旧昵称")
            .setExpired(Boolean.FALSE)
            .setExpiresAt(new Date(System.currentTimeMillis() + 86_400_000L));
        history.setId(id);
        return history;
    }
}
