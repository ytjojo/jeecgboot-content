package org.jeecg.modules.content.circle.service;

import org.jeecg.modules.content.circle.entity.CircleGovernanceLog;
import org.jeecg.modules.content.circle.mapper.CircleGovernanceLogMapper;
import org.jeecg.modules.content.circle.service.impl.CircleGovernanceLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleGovernanceLogService")
class CircleGovernanceLogServiceTest {

    @Mock
    private CircleGovernanceLogMapper circleGovernanceLogMapper;

    @InjectMocks
    private CircleGovernanceLogServiceImpl governanceLogService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(governanceLogService, "baseMapper", circleGovernanceLogMapper);
    }

    @Test
    @DisplayName("logMute - saves log")
    void logMute_savesLog() {
        when(circleGovernanceLogMapper.insert(any(CircleGovernanceLog.class))).thenReturn(1);

        governanceLogService.logMute("c_001", "op_001", "target_001", "违规发言", "24h");

        verify(circleGovernanceLogMapper).insert(any(CircleGovernanceLog.class));
    }

    @Test
    @DisplayName("logRoleChange - saves log")
    void logRoleChange_savesLog() {
        when(circleGovernanceLogMapper.insert(any(CircleGovernanceLog.class))).thenReturn(1);

        governanceLogService.logRoleChange("c_001", "op_001", "target_001", "MEMBER", "MODERATOR");

        verify(circleGovernanceLogMapper).insert(any(CircleGovernanceLog.class));
    }
}
