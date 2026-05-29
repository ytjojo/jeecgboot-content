package org.jeecg.modules.content.auth.service;

import org.jeecg.modules.content.auth.entity.ContentUserAccount;
import org.jeecg.modules.content.auth.mapper.ContentUserAccountMapper;
import org.jeecg.modules.content.auth.service.impl.ContentUserAccountServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentUserAccountServiceTest {

    @Mock
    private ContentUserAccountMapper accountMapper;

    @InjectMocks
    private ContentUserAccountServiceImpl service;

    @Test
    void shouldInstantiateWithMockMapper() {
        assertThat(service).isNotNull();
    }

    @Test
    void getById_shouldDelegateToMapperAndReturnEntity() {
        ContentUserAccount entity = new ContentUserAccount()
            .setUserId("u_1001")
            .setNickname("testUser")
            .setAccountStatus("ACTIVE");
        when(accountMapper.selectById("id_001")).thenReturn(entity);

        ContentUserAccount result = service.getById("id_001");

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("u_1001");
        assertThat(result.getNickname()).isEqualTo("testUser");
        assertThat(result.getAccountStatus()).isEqualTo("ACTIVE");
        verify(accountMapper).selectById("id_001");
    }

    @Test
    void getById_shouldReturnNullWhenNotFound() {
        when(accountMapper.selectById("nonexistent")).thenReturn(null);

        ContentUserAccount result = service.getById("nonexistent");

        assertThat(result).isNull();
        verify(accountMapper).selectById("nonexistent");
    }
}
