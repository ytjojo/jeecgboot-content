package org.jeecg.modules.content.circle.biz;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.req.create.CircleJoinReq;
import org.jeecg.modules.content.circle.req.update.CircleMemberUpdateReq;
import org.jeecg.modules.content.circle.service.ICircleGovernanceLogService;
import org.jeecg.modules.content.circle.service.ICircleMemberService;
import org.jeecg.modules.content.circle.service.ICircleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleMemberBiz")
class CircleMemberBizTest {

    @Mock
    private ICircleService circleService;

    @Mock
    private ICircleMemberService circleMemberService;

    @Mock
    private ICircleGovernanceLogService governanceLogService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CircleMemberBizImpl circleMemberBiz;

    @Nested
    @DisplayName("joinCircle")
    class JoinCircle {

        @Test
        @DisplayName("direct join - succeeds")
        void directJoin_succeeds() {
            Circle circle = new Circle();
            circle.setId("c_001");
            circle.setPrivacyType(Circle.PrivacyType.PUBLIC);
            circle.setJoinType(Circle.JoinType.DIRECT);

            when(circleService.getById("c_001")).thenReturn(circle);
            when(circleMemberService.findByCircleAndUser("c_001", "u_001")).thenReturn(null);
            when(circleMemberService.save(any())).thenReturn(true);

            circleMemberBiz.joinCircle(new CircleJoinReq() {{ setCircleId("c_001"); }}, "u_001");

            verify(circleService).incrementMemberCount("c_001");
            verify(circleMemberService).save(any());
        }

        @Test
        @DisplayName("invite only - throws exception")
        void inviteOnly_throwsException() {
            Circle circle = new Circle();
            circle.setId("c_001");
            circle.setPrivacyType(Circle.PrivacyType.PRIVATE);
            circle.setJoinType(Circle.JoinType.INVITE);

            when(circleService.getById("c_001")).thenReturn(circle);
            when(circleMemberService.findByCircleAndUser("c_001", "u_001")).thenReturn(null);

            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> circleMemberBiz.joinCircle(new CircleJoinReq() {{ setCircleId("c_001"); }}, "u_001"));
            assertEquals("该圈子仅限邀请加入", ex.getMessage());
        }

        @Test
        @DisplayName("password join wrong password - throws exception")
        void passwordJoinWrongPassword_throwsException() {
            Circle circle = new Circle();
            circle.setId("c_001");
            circle.setPrivacyType(Circle.PrivacyType.PASSWORD);
            circle.setJoinType(Circle.JoinType.PASSWORD);
            circle.setPasswordHash("$2a$encoded");

            CircleJoinReq req = new CircleJoinReq();
            req.setCircleId("c_001");
            req.setPassword("wrong");

            when(circleService.getById("c_001")).thenReturn(circle);
            when(circleMemberService.findByCircleAndUser("c_001", "u_001")).thenReturn(null);
            when(passwordEncoder.matches("wrong", "$2a$encoded")).thenReturn(false);

            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> circleMemberBiz.joinCircle(req, "u_001"));
            assertEquals("密码错误", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("muteMember")
    class MuteMember {

        @Test
        @DisplayName("member tries to mute - throws exception")
        void memberTriesToMute_throwsException() {
            CircleMemberUpdateReq req = new CircleMemberUpdateReq();
            req.setCircleId("c_001");
            req.setTargetUserId("target_001");
            req.setMuteDuration("24h");
            req.setReason("违规");

            CircleMember operator = new CircleMember();
            operator.setRole(CircleMember.Role.MEMBER);

            when(circleMemberService.findByCircleAndUser("c_001", "op_001")).thenReturn(operator);

            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> circleMemberBiz.muteMember(req, "op_001"));
            assertEquals("权限不足，仅创建者和版主可禁言成员", ex.getMessage());
        }

        @Test
        @DisplayName("moderator tries to mute moderator - throws exception")
        void moderatorTriesToMuteModerator_throwsException() {
            CircleMemberUpdateReq req = new CircleMemberUpdateReq();
            req.setCircleId("c_001");
            req.setTargetUserId("target_001");
            req.setMuteDuration("24h");
            req.setReason("违规");

            CircleMember operator = new CircleMember();
            operator.setRole(CircleMember.Role.MODERATOR);

            CircleMember target = new CircleMember();
            target.setRole(CircleMember.Role.MODERATOR);
            target.setStatus(CircleMember.Status.ACTIVE);

            when(circleMemberService.findByCircleAndUser("c_001", "op_001")).thenReturn(operator);
            when(circleMemberService.findByCircleAndUser("c_001", "target_001")).thenReturn(target);

            JeecgBootException ex = assertThrows(JeecgBootException.class,
                    () -> circleMemberBiz.muteMember(req, "op_001"));
            assertEquals("权限不足，仅创建者可管理版主", ex.getMessage());
        }
    }
}
