package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ChannelContentPublish;
import org.jeecg.modules.content.channel.entity.ChannelLifecycleLog;
import org.jeecg.modules.content.channel.enums.ChannelLifecycleStatus;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.mapper.ChannelContentPublishMapper;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.jeecg.modules.content.channel.service.IChannelLifecycleLogService;
import org.jeecg.modules.content.user.service.IContentNotificationService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelMergeBizTest {

    @Mock
    private ChannelService channelService;

    @Mock
    private IChannelLifecycleLogService lifecycleLogService;

    @Mock
    private ChannelContentPublishMapper publishMapper;

    @Mock
    private IContentNotificationService notificationService;

    @InjectMocks
    private ChannelMergeBiz mergeBiz;

    @Test
    void shouldValidateMergeSuccessfully() {
        // Given
        Channel source = new Channel().setName("源频道").setChannelType(ChannelType.PERSONAL);
        Channel target = new Channel().setName("目标频道").setChannelType(ChannelType.PERSONAL);
        when(channelService.getById("src-001")).thenReturn(source);
        when(channelService.getById("tgt-001")).thenReturn(target);
        when(lifecycleLogService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null); // ACTIVE
        when(publishMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // When
        Map<String, Object> result = mergeBiz.validateMerge("src-001", "tgt-001");

        // Then
        assertNotNull(result);
        assertEquals("源频道", result.get("sourceChannelName"));
        assertEquals("目标频道", result.get("targetChannelName"));
        assertEquals(5L, result.get("contentCount"));
        assertEquals(false, result.get("needOrgApproval"));
    }

    @Test
    void shouldThrowWhenSourceChannelNotFound() {
        // Given
        when(channelService.getById("nonexistent")).thenReturn(null);

        // When / Then
        assertThrows(IllegalArgumentException.class,
                () -> mergeBiz.validateMerge("nonexistent", "tgt-001"));
    }

    @Test
    void shouldThrowWhenTargetChannelNotFound() {
        // Given
        Channel source = new Channel().setName("源频道");
        when(channelService.getById("src-001")).thenReturn(source);
        when(channelService.getById("nonexistent")).thenReturn(null);

        // When / Then
        assertThrows(IllegalArgumentException.class,
                () -> mergeBiz.validateMerge("src-001", "nonexistent"));
    }

    @Test
    void shouldThrowWhenSourceAndTargetAreSame() {
        // Given
        Channel source = new Channel().setName("源频道");
        when(channelService.getById("ch-001")).thenReturn(source);

        // When / Then
        assertThrows(IllegalArgumentException.class,
                () -> mergeBiz.validateMerge("ch-001", "ch-001"));
    }

    @Test
    void shouldThrowWhenSourceIsArchived() {
        // Given
        Channel source = new Channel().setName("源频道");
        Channel target = new Channel().setName("目标频道");
        when(channelService.getById("src-001")).thenReturn(source);
        when(channelService.getById("tgt-001")).thenReturn(target);

        ChannelLifecycleLog archivedLog = new ChannelLifecycleLog()
                .setToStatus(ChannelLifecycleStatus.ARCHIVED.getCode());
        when(lifecycleLogService.getOne(any(LambdaQueryWrapper.class)))
                .thenReturn(archivedLog)
                .thenReturn(null); // target is ACTIVE

        // When / Then
        assertThrows(IllegalStateException.class,
                () -> mergeBiz.validateMerge("src-001", "tgt-001"));
    }

    @Test
    void shouldSetNeedOrgApprovalForOrganizationChannel() {
        // Given
        Channel source = new Channel().setName("组织频道").setChannelType(ChannelType.ORGANIZATION);
        Channel target = new Channel().setName("目标频道").setChannelType(ChannelType.PERSONAL);
        when(channelService.getById("src-001")).thenReturn(source);
        when(channelService.getById("tgt-001")).thenReturn(target);
        when(lifecycleLogService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(publishMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // When
        Map<String, Object> result = mergeBiz.validateMerge("src-001", "tgt-001");

        // Then
        assertEquals(true, result.get("needOrgApproval"));
    }

    @Test
    void shouldExecuteMergeAndMigrateContent() {
        // Given
        Channel source = new Channel().setName("源频道").setOwnerId("owner-001");
        Channel target = new Channel().setName("目标频道");
        when(channelService.getById("src-001")).thenReturn(source);
        when(channelService.getById("tgt-001")).thenReturn(target);
        when(lifecycleLogService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null); // ACTIVE

        ChannelContentPublish publish1 = new ChannelContentPublish();
        publish1.setChannelId("src-001");
        ChannelContentPublish publish2 = new ChannelContentPublish();
        publish2.setChannelId("src-001");
        when(publishMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(publish1, publish2));
        when(publishMapper.updateById(any(ChannelContentPublish.class))).thenReturn(1);
        when(lifecycleLogService.save(any())).thenReturn(true);

        // When
        mergeBiz.executeMerge("src-001", "tgt-001", "operator-001");

        // Then
        // 内容迁移到目标频道
        assertEquals("tgt-001", publish1.getChannelId());
        assertEquals("tgt-001", publish2.getChannelId());
        verify(publishMapper, times(2)).updateById(any(ChannelContentPublish.class));

        // 合并日志已记录
        verify(lifecycleLogService).save(argThat(log ->
                "merge".equals(((ChannelLifecycleLog) log).getActionType())
                        && "Merged".equals(((ChannelLifecycleLog) log).getToStatus())
        ));

        // 通知已发送
        verify(notificationService).sendNotification(
                eq("owner-001"), eq("channel_merge"), anyString(), anyString());
    }

    @Test
    void shouldExecuteMergeWithoutNotificationWhenNoOwner() {
        // Given
        Channel source = new Channel().setName("源频道").setOwnerId(null);
        Channel target = new Channel().setName("目标频道");
        when(channelService.getById("src-001")).thenReturn(source);
        when(channelService.getById("tgt-001")).thenReturn(target);
        when(lifecycleLogService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(publishMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());
        when(lifecycleLogService.save(any())).thenReturn(true);

        // When
        mergeBiz.executeMerge("src-001", "tgt-001", "operator-001");

        // Then
        verify(notificationService, never()).sendNotification(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldThrowWhenExecuteMergeWithBlockedSource() {
        // Given
        Channel source = new Channel().setName("源频道");
        Channel target = new Channel().setName("目标频道");
        when(channelService.getById("src-001")).thenReturn(source);
        when(channelService.getById("tgt-001")).thenReturn(target);

        ChannelLifecycleLog closedLog = new ChannelLifecycleLog()
                .setToStatus(ChannelLifecycleStatus.CLOSED.getCode());
        when(lifecycleLogService.getOne(any(LambdaQueryWrapper.class))).thenReturn(closedLog);

        // When / Then
        assertThrows(IllegalStateException.class,
                () -> mergeBiz.executeMerge("src-001", "tgt-001", "operator-001"));
    }
}
