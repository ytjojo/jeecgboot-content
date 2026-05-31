package org.jeecg.modules.content.circle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.mapper.CircleMemberMapper;
import org.jeecg.modules.content.circle.service.impl.CircleMemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleMemberService")
class CircleMemberServiceTest {

    @Mock
    private CircleMemberMapper circleMemberMapper;

    @InjectMocks
    private CircleMemberServiceImpl circleMemberService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(circleMemberService, "baseMapper", circleMemberMapper);
    }

    @Nested
    @DisplayName("checkAlreadyMember")
    class CheckAlreadyMember {

        @Test
        @DisplayName("already member - throws exception")
        void alreadyMember_throwsException() {
            when(circleMemberMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> circleMemberService.checkAlreadyMember("c_001", "u_001"));
            assertEquals("您已是圈子成员", ex.getMessage());
        }

        @Test
        @DisplayName("not member - passes")
        void notMember_passes() {
            when(circleMemberMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            assertDoesNotThrow(() -> circleMemberService.checkAlreadyMember("c_001", "u_001"));
        }
    }

    @Nested
    @DisplayName("checkNotMuted")
    class CheckNotMuted {

        @Test
        @DisplayName("muted and not expired - throws exception")
        void mutedAndNotExpired_throwsException() {
            CircleMember member = new CircleMember();
            member.setStatus(CircleMember.Status.MUTED);
            member.setMuteEndTime(LocalDateTime.now().plusHours(1));

            doReturn(member).when(circleMemberMapper).selectOne(any(LambdaQueryWrapper.class), anyBoolean());

            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> circleMemberService.checkNotMuted("c_001", "u_001"));
            assertTrue(ex.getMessage().contains("您已被禁言"));
        }

        @Test
        @DisplayName("muted but expired - auto unmute and pass")
        void mutedButExpired_autoUnmuteAndPass() {
            CircleMember member = new CircleMember();
            member.setId("m_001");
            member.setStatus(CircleMember.Status.MUTED);
            member.setMuteEndTime(LocalDateTime.now().minusHours(1));

            doReturn(member).when(circleMemberMapper).selectOne(any(LambdaQueryWrapper.class), anyBoolean());
            when(circleMemberMapper.updateById(any(CircleMember.class))).thenReturn(1);

            assertDoesNotThrow(() -> circleMemberService.checkNotMuted("c_001", "u_001"));
            verify(circleMemberMapper).updateById(any(CircleMember.class));
        }

        @Test
        @DisplayName("not muted - passes")
        void notMuted_passes() {
            CircleMember member = new CircleMember();
            member.setStatus(CircleMember.Status.ACTIVE);

            doReturn(member).when(circleMemberMapper).selectOne(any(LambdaQueryWrapper.class), anyBoolean());
            assertDoesNotThrow(() -> circleMemberService.checkNotMuted("c_001", "u_001"));
        }
    }

    @Nested
    @DisplayName("checkPermission")
    class CheckPermission {

        @Test
        @DisplayName("moderator tries to change role - throws exception")
        void moderatorTriesToChangeRole_throwsException() {
            CircleMember operator = new CircleMember();
            operator.setRole(CircleMember.Role.MODERATOR);

            doReturn(operator).when(circleMemberMapper).selectOne(any(LambdaQueryWrapper.class), anyBoolean());

            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> circleMemberService.checkCreatorPermission("c_001", "u_001"));
            assertEquals("权限不足，仅创建者可管理角色", ex.getMessage());
        }

        @Test
        @DisplayName("creator - passes")
        void creator_passes() {
            CircleMember operator = new CircleMember();
            operator.setRole(CircleMember.Role.CREATOR);

            doReturn(operator).when(circleMemberMapper).selectOne(any(LambdaQueryWrapper.class), anyBoolean());
            assertDoesNotThrow(() -> circleMemberService.checkCreatorPermission("c_001", "u_001"));
        }
    }
}
