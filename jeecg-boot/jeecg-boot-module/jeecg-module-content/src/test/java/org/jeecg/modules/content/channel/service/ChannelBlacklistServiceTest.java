package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ChannelBlacklist;
import org.jeecg.modules.content.channel.mapper.ChannelBlacklistMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelBlacklistServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelBlacklistServiceTest {

    @Mock
    private ChannelBlacklistMapper blacklistMapper;

    @InjectMocks
    private ChannelBlacklistServiceImpl blacklistService;

    @Test
    void should_add_to_blacklist() {
        when(blacklistMapper.selectCount(any())).thenReturn(0L);

        blacklistService.addToBlacklist("ch1", "user1", "admin1", "骚扰");

        verify(blacklistMapper).insert(any(ChannelBlacklist.class));
    }

    @Test
    void should_reject_duplicate_blacklist() {
        when(blacklistMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> blacklistService.addToBlacklist("ch1", "user1", "admin1", "骚扰"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("已在黑名单中");
    }

    @Test
    void should_check_blacklist() {
        when(blacklistMapper.selectCount(any())).thenReturn(1L);

        assertThat(blacklistService.isBlacklisted("ch1", "user1")).isTrue();
    }

    @Test
    void should_remove_from_blacklist() {
        ChannelBlacklist entry = new ChannelBlacklist();
        entry.setId("bl1");
        when(blacklistMapper.selectOne(any())).thenReturn(entry);

        blacklistService.removeFromBlacklist("ch1", "user1", "admin1");

        verify(blacklistMapper).deleteById("bl1");
    }

    @Test
    void should_list_by_channel() {
        ChannelBlacklist entry1 = new ChannelBlacklist();
        entry1.setId("bl1");
        ChannelBlacklist entry2 = new ChannelBlacklist();
        entry2.setId("bl2");
        when(blacklistMapper.selectList(any())).thenReturn(List.of(entry1, entry2));

        List<ChannelBlacklist> result = blacklistService.listByChannel("ch1");

        assertThat(result).hasSize(2);
        verify(blacklistMapper).selectList(any());
    }

    @Test
    void should_list_by_channel_return_empty_when_no_entries() {
        when(blacklistMapper.selectList(any())).thenReturn(List.of());

        List<ChannelBlacklist> result = blacklistService.listByChannel("ch1");

        assertThat(result).isEmpty();
    }
}
