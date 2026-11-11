package com.example.springai;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.ai.anthropic.api-key=test-key",
        "spring.ai.anthropic.base-url=https://example.invalid"
})
class SpringAiApplicationTests {

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private ChatClient.Builder chatClientBuilder;

    @Test
    void contextLoads() {
    }
}
