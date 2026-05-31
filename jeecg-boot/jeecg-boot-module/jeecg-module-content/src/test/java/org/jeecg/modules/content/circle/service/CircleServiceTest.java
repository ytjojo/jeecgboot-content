package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.mapper.CircleMapper;
import org.jeecg.modules.content.circle.service.impl.CircleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleService")
class CircleServiceTest {

    @Mock
    private CircleMapper circleMapper;

    @InjectMocks
    private CircleServiceImpl circleService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(circleService, "baseMapper", circleMapper);
    }

    @Nested
    @DisplayName("createCircle")
    class CreateCircle {

        @Test
        @DisplayName("name exists - throws exception")
        void nameExists_throwsException() {
            when(circleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> circleService.checkNameUnique("已存在的圈子"));
            assertEquals("该圈子名称已存在，请修改", ex.getMessage());
        }

        @Test
        @DisplayName("name unique - passes")
        void nameUnique_passes() {
            when(circleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            assertDoesNotThrow(() -> circleService.checkNameUnique("新圈子"));
        }
    }

    @Nested
    @DisplayName("incrementMemberCount")
    class IncrementMemberCount {

        @Test
        @DisplayName("reaches max - throws exception")
        void reachesMax_throwsException() {
            when(circleMapper.incrementMemberCount("c_001")).thenReturn(0);

            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> circleService.incrementMemberCount("c_001"));
            assertEquals("圈子已满员，无法加入", ex.getMessage());
        }

        @Test
        @DisplayName("under max - increments")
        void underMax_increments() {
            when(circleMapper.incrementMemberCount("c_001")).thenReturn(1);

            assertDoesNotThrow(() -> circleService.incrementMemberCount("c_001"));
            verify(circleMapper).incrementMemberCount("c_001");
        }
    }
}
