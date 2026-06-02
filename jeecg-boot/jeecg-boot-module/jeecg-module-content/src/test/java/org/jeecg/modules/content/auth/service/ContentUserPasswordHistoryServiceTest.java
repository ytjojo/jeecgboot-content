package org.jeecg.modules.content.auth.service;

import org.jeecg.modules.content.auth.entity.ContentUserPasswordHistory;
import org.jeecg.modules.content.auth.mapper.ContentUserPasswordHistoryMapper;
import org.jeecg.modules.content.auth.service.impl.ContentUserPasswordHistoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentUserPasswordHistoryServiceTest {

    @Mock
    private ContentUserPasswordHistoryMapper passwordHistoryMapper;

    @InjectMocks
    private ContentUserPasswordHistoryServiceImpl service;

    @Test
    void shouldInstantiateWithMockMapper() {
        assertThat(service).isNotNull();
    }

    @Test
    void getById_shouldDelegateToMapperAndReturnEntity() {
        ContentUserPasswordHistory entity = new ContentUserPasswordHistory()
            .setUserId("u_2001")
            .setPasswordHash("hashed_pwd_001")
            .setSalt("salt_001");
        when(passwordHistoryMapper.selectById("pwd_hist_001")).thenReturn(entity);

        ContentUserPasswordHistory result = service.getById("pwd_hist_001");

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("u_2001");
        assertThat(result.getPasswordHash()).isEqualTo("hashed_pwd_001");
        assertThat(result.getSalt()).isEqualTo("salt_001");
        verify(passwordHistoryMapper).selectById("pwd_hist_001");
    }

    @Test
    void getById_shouldReturnNullWhenNotFound() {
        when(passwordHistoryMapper.selectById("nonexistent")).thenReturn(null);

        ContentUserPasswordHistory result = service.getById("nonexistent");

        assertThat(result).isNull();
        verify(passwordHistoryMapper).selectById("nonexistent");
    }
}
