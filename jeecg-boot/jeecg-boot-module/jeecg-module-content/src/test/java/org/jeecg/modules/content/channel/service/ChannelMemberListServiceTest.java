package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.mapper.ChannelMemberMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelMemberListServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 频道成员列表服务测试
 */
@ExtendWith(MockitoExtension.class)
class ChannelMemberListServiceTest {

    @Mock
    private ChannelMemberMapper memberMapper;

    @InjectMocks
    private ChannelMemberListServiceImpl memberListService;

    @Test
    void should_list_members_without_role_filter() {
        Page<ChannelMember> page = new Page<>(1, 10);
        page.setRecords(List.of(new ChannelMember()));
        when(memberMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        IPage<ChannelMember> result = memberListService.listMembers("ch1", null, 1, 10);

        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    void should_list_members_with_role_filter() {
        Page<ChannelMember> page = new Page<>(1, 10);
        when(memberMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        IPage<ChannelMember> result = memberListService.listMembers("ch1", 2, 1, 10);

        assertThat(result.getRecords()).isEmpty();
        verify(memberMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void should_search_members_by_keyword() {
        Page<ChannelMember> page = new Page<>(1, 10);
        when(memberMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        IPage<ChannelMember> result = memberListService.searchMembers("ch1", "alice", 1, 10);

        assertThat(result.getRecords()).isEmpty();
    }
}
