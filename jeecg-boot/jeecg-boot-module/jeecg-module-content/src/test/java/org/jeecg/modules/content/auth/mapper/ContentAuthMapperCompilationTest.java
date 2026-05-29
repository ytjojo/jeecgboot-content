package org.jeecg.modules.content.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.content.auth.entity.ContentUserAccount;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verify all mapper interfaces exist, extend BaseMapper, and have the expected custom query method.
 */
class ContentAuthMapperCompilationTest {

    @Test
    void contentUserAccountMapper_shouldExtendBaseMapper() {
        assertThat(BaseMapper.class.isAssignableFrom(ContentUserAccountMapper.class)).isTrue();
    }

    @Test
    void contentUserCredentialMapper_shouldExtendBaseMapper() {
        assertThat(BaseMapper.class.isAssignableFrom(ContentUserCredentialMapper.class)).isTrue();
    }

    @Test
    void contentUserPasswordHistoryMapper_shouldExtendBaseMapper() {
        assertThat(BaseMapper.class.isAssignableFrom(ContentUserPasswordHistoryMapper.class)).isTrue();
    }

    @Test
    void contentRiskEventMapper_shouldExtendBaseMapper() {
        assertThat(BaseMapper.class.isAssignableFrom(ContentRiskEventMapper.class)).isTrue();
    }

    @Test
    void contentCancellationRequestMapper_shouldExtendBaseMapper() {
        assertThat(BaseMapper.class.isAssignableFrom(ContentCancellationRequestMapper.class)).isTrue();
    }

    @Test
    void contentUserAccountMapper_shouldHaveSelectActiveByUserIdMethod() throws NoSuchMethodException {
        Method method = ContentUserAccountMapper.class.getMethod("selectActiveByUserId", String.class);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(ContentUserAccount.class);
    }
}
