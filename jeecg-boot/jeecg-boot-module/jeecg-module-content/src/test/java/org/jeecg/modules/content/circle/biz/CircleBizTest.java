package org.jeecg.modules.content.circle.biz;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.req.create.CircleCreateReq;
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
@DisplayName("CircleBiz")
class CircleBizTest {

    @Mock
    private ICircleService circleService;

    @Mock
    private ICircleMemberService circleMemberService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CircleBizImpl circleBiz;

    @Nested
    @DisplayName("createCircle")
    class CreateCircle {

        @Test
        @DisplayName("name exists - throws exception")
        void nameExists_throwsException() {
            CircleCreateReq req = new CircleCreateReq();
            req.setName("已存在的圈子");
            req.setDescription("简介");
            req.setPrivacyType("PUBLIC");
            req.setJoinType("DIRECT");

            doThrow(new JeecgBootException("该圈子名称已存在，请修改"))
                    .when(circleService).checkNameUnique("已存在的圈子");

            assertThrows(JeecgBootException.class, () -> circleBiz.createCircle(req, "u_001"));
        }

        @Test
        @DisplayName("valid request - creates circle and member")
        void validRequest_createsCircleAndMember() {
            CircleCreateReq req = new CircleCreateReq();
            req.setName("新圈子");
            req.setDescription("简介");
            req.setPrivacyType("PUBLIC");
            req.setJoinType("DIRECT");

            when(circleService.save(any())).thenReturn(true);
            when(circleMemberService.save(any())).thenReturn(true);

            circleBiz.createCircle(req, "u_001");

            verify(circleService).save(any());
            verify(circleMemberService).save(any());
        }

        @Test
        @DisplayName("password type - encodes password")
        void passwordType_encodesPassword() {
            CircleCreateReq req = new CircleCreateReq();
            req.setName("密码圈子");
            req.setDescription("简介");
            req.setPrivacyType("PASSWORD");
            req.setJoinType("PASSWORD");
            req.setPassword("secret123");

            when(passwordEncoder.encode("secret123")).thenReturn("$2a$encoded");
            when(circleService.save(any())).thenReturn(true);
            when(circleMemberService.save(any())).thenReturn(true);

            circleBiz.createCircle(req, "u_001");

            verify(passwordEncoder).encode("secret123");
        }
    }
}
