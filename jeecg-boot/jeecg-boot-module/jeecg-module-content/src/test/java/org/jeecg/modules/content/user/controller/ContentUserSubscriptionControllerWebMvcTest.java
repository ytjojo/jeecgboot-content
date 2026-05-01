package org.jeecg.modules.content.user.controller;

import org.jeecg.modules.content.user.service.IContentUserSubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContentUserSubscriptionControllerWebMvcTest {

    private MockMvc mockMvc;

    @Mock
    private IContentUserSubscriptionService subscriptionService;

    @InjectMocks
    private ContentUserSubscriptionController subscriptionController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(subscriptionController)
            .setValidator(validator)
            .build();
    }

    @Test
    void shouldRejectInvalidSubscriptionRequest() throws Exception {
        mockMvc.perform(post("/content/user/subscription/subscribe")
                .param("userId", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sourceType\":\"\",\"sourceId\":\"topic-1\",\"sourceName\":\"专题\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPauseSubscriptionSuccessfully() throws Exception {
        mockMvc.perform(post("/content/user/subscription/pause")
                .param("userId", "u1")
                .param("subscriptionId", "sub-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("暂停订阅成功"));
    }
}
