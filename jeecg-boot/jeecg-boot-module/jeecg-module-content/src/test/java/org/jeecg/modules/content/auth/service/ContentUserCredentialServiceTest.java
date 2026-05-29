package org.jeecg.modules.content.auth.service;

import org.jeecg.modules.content.auth.entity.ContentUserCredential;
import org.jeecg.modules.content.auth.mapper.ContentUserCredentialMapper;
import org.jeecg.modules.content.auth.service.impl.ContentUserCredentialServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentUserCredentialServiceTest {

    @Mock
    private ContentUserCredentialMapper credentialMapper;

    @InjectMocks
    private ContentUserCredentialServiceImpl service;

    @Test
    void shouldInstantiateWithMockMapper() {
        assertThat(service).isNotNull();
    }

    @Test
    void getById_shouldDelegateToMapperAndReturnEntity() {
        ContentUserCredential entity = new ContentUserCredential()
            .setUserId("u_1001")
            .setCredentialType("PASSWORD")
            .setCredentialValue("hashed_value")
            .setVerified(true);
        when(credentialMapper.selectById("id_002")).thenReturn(entity);

        ContentUserCredential result = service.getById("id_002");

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("u_1001");
        assertThat(result.getCredentialType()).isEqualTo("PASSWORD");
        assertThat(result.getVerified()).isTrue();
        verify(credentialMapper).selectById("id_002");
    }

    @Test
    void getById_shouldReturnNullWhenNotFound() {
        when(credentialMapper.selectById("nonexistent")).thenReturn(null);

        ContentUserCredential result = service.getById("nonexistent");

        assertThat(result).isNull();
        verify(credentialMapper).selectById("nonexistent");
    }
}
