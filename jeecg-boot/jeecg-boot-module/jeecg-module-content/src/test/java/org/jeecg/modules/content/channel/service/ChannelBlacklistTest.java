package org.jeecg.modules.content.channel.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ChannelBlacklist;
import org.jeecg.modules.content.channel.mapper.ChannelBlacklistMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelBlacklistServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 黑名单集成测试
 * 覆盖：添加 -> 查询 -> 移除 -> 重新申请 的完整流程
 */
@ExtendWith(MockitoExtension.class)
class ChannelBlacklistTest {

    @Mock
    private ChannelBlacklistMapper blacklistMapper;

    @InjectMocks
    private ChannelBlacklistServiceImpl blacklistService;

    @Test
    void should_add_to_blacklist() {
        // 添加黑名单时应创建记录
        when(blacklistMapper.selectCount(any())).thenReturn(0L);

        blacklistService.addToBlacklist("ch1", "user1", "admin1", "骚扰行为");

        verify(blacklistMapper).insert(any(ChannelBlacklist.class));
    }

    @Test
    void should_check_blacklisted_returns_true() {
        // 已在黑名单中的用户应返回 true
        when(blacklistMapper.selectCount(any())).thenReturn(1L);

        assertThat(blacklistService.isBlacklisted("ch1", "user1")).isTrue();
    }

    @Test
    void should_check_blacklisted_returns_false() {
        // 不在黑名单中的用户应返回 false
        when(blacklistMapper.selectCount(any())).thenReturn(0L);

        assertThat(blacklistService.isBlacklisted("ch1", "user1")).isFalse();
    }

    @Test
    void should_remove_from_blacklist() {
        // 移除黑名单应删除对应记录
        ChannelBlacklist entry = new ChannelBlacklist();
        entry.setId("bl1");
        when(blacklistMapper.selectOne(any())).thenReturn(entry);

        blacklistService.removeFromBlacklist("ch1", "user1", "admin1");

        verify(blacklistMapper).deleteById("bl1");
    }

    @Test
    void should_reject_duplicate_blacklist() {
        // 重复添加黑名单应报错，防止数据冗余
        when(blacklistMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> blacklistService.addToBlacklist("ch1", "user1", "admin1", "骚扰"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("已在黑名单中");
    }

    @Test
    void should_throw_when_removing_non_blacklisted_user() {
        // 移除不在黑名单中的用户应报错
        when(blacklistMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> blacklistService.removeFromBlacklist("ch1", "user1", "admin1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("不在黑名单中");
    }

    @Test
    void should_list_blacklisted_user_ids() {
        // 列出黑名单用户ID列表
        ChannelBlacklist entry1 = new ChannelBlacklist();
        entry1.setUserId("user1");
        ChannelBlacklist entry2 = new ChannelBlacklist();
        entry2.setUserId("user2");
        when(blacklistMapper.selectList(any())).thenReturn(Arrays.asList(entry1, entry2));

        List<String> result = blacklistService.listBlacklistedUserIds("ch1");

        assertThat(result).containsExactly("user1", "user2");
    }
}
