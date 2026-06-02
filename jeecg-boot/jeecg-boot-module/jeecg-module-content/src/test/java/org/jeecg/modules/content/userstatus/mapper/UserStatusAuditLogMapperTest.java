package org.jeecg.modules.content.userstatus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.userstatus.entity.UserStatusAuditLog;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 UserStatusAuditLogMapper 的接口契约：继承 BaseMapper、标注 @Mapper、自定义方法签名正确。
 * 与 ContentAuthMapperCompilationTest 对齐。
 */
class UserStatusAuditLogMapperTest {

    @Test
    void shouldExtendBaseMapper() {
        assertThat(BaseMapper.class.isAssignableFrom(UserStatusAuditLogMapper.class)).isTrue();
    }

    @Test
    void shouldBeAnnotatedWithMapper() {
        assertThat(UserStatusAuditLogMapper.class.isAnnotationPresent(Mapper.class)).isTrue();
    }

    @Test
    void shouldHaveSelectByUserIdMethod() throws NoSuchMethodException {
        Method method = UserStatusAuditLogMapper.class.getMethod("selectByUserId", String.class);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(java.util.List.class);
    }

    @Test
    void shouldHaveSelectByUserIdAndTimeRangeMethod() throws NoSuchMethodException {
        Method method = UserStatusAuditLogMapper.class.getMethod(
            "selectByUserIdAndTimeRange", String.class, Date.class, Date.class);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(java.util.List.class);

        Parameter[] parameters = method.getParameters();
        assertThat(parameters).hasSize(3);
        assertThat(parameters[0].getName()).isEqualTo("userId");
        assertThat(parameters[1].getName()).isEqualTo("startTime");
        assertThat(parameters[2].getName()).isEqualTo("endTime");
    }

    @Test
    void genericTypeShouldBeUserStatusAuditLog() {
        // 验证 BaseMapper<UserStatusAuditLog> 的泛型参数
        assertThat(UserStatusAuditLogMapper.class.getGenericInterfaces())
            .extracting(t -> t.toString())
            .anyMatch(s -> s.contains("BaseMapper") && s.contains("UserStatusAuditLog"));
    }
}
