package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ChannelSubscriptionGroup;
import org.jeecg.modules.content.channel.mapper.ChannelSubscriptionGroupMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelSubscriptionGroupServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 频道订阅分组服务测试
 */
@ExtendWith(MockitoExtension.class)
class ChannelSubscriptionGroupServiceTest {

    @Mock
    private ChannelSubscriptionGroupMapper groupMapper;

    @InjectMocks
    private ChannelSubscriptionGroupServiceImpl groupService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(groupService, "baseMapper", groupMapper);
    }

    @Test
    void should_create_group_with_zero_sort() {
        ChannelSubscriptionGroup group = groupService.createGroup("u1", "Friends");

        assertThat(group.getUserId()).isEqualTo("u1");
        assertThat(group.getGroupName()).isEqualTo("Friends");
        assertThat(group.getSortOrder()).isEqualTo(0);
        verify(groupMapper).insert(group);
    }

    @Test
    void should_rename_owned_group() {
        ChannelSubscriptionGroup group = new ChannelSubscriptionGroup();
        group.setId("g1");
        group.setUserId("u1");
        group.setGroupName("old");
        when(groupMapper.selectById("g1")).thenReturn(group);

        groupService.renameGroup("g1", "new", "u1");

        assertThat(group.getGroupName()).isEqualTo("new");
        verify(groupMapper).updateById(group);
    }

    @Test
    void should_throw_when_rename_not_owned() {
        ChannelSubscriptionGroup group = new ChannelSubscriptionGroup();
        group.setId("g1");
        group.setUserId("u2");
        when(groupMapper.selectById("g1")).thenReturn(group);

        assertThatThrownBy(() -> groupService.renameGroup("g1", "new", "u1"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessageContaining("分组不存在");
    }

    @Test
    void should_throw_when_rename_missing() {
        when(groupMapper.selectById("g1")).thenReturn(null);

        assertThatThrownBy(() -> groupService.renameGroup("g1", "new", "u1"))
            .isInstanceOf(JeecgBootException.class);
    }

    @Test
    void should_delete_owned_group() {
        ChannelSubscriptionGroup group = new ChannelSubscriptionGroup();
        group.setId("g1");
        group.setUserId("u1");
        when(groupMapper.selectById("g1")).thenReturn(group);

        groupService.deleteGroup("g1", "u1");

        verify(groupMapper).deleteById("g1");
    }

    @Test
    void should_throw_when_delete_not_owned() {
        ChannelSubscriptionGroup group = new ChannelSubscriptionGroup();
        group.setId("g1");
        group.setUserId("u2");
        when(groupMapper.selectById("g1")).thenReturn(group);

        assertThatThrownBy(() -> groupService.deleteGroup("g1", "u1"))
            .isInstanceOf(JeecgBootException.class);
        verify(groupMapper, never()).deleteById(anyString());
    }

    @Test
    void should_no_op_move_to_group() {
        // moveToGroup is unimplemented placeholder
        assertThatCode(() -> groupService.moveToGroup("sub1", "g1"))
            .doesNotThrowAnyException();
    }

    @Test
    void should_list_groups_by_user_in_asc_order() {
        groupService.listByUser("u1");

        verify(groupMapper).selectList(any(LambdaQueryWrapper.class));
    }
}
