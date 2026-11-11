package com.example.springai;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(ChatController.class)
@TestPropertySource(properties = "test.context.id=chat-controller-error")
class ChatControllerErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS, reset = MockReset.NONE)
    private ChatClient.Builder chatClientBuilder;

    @Test
    void chat_chatClientThrows_propagatesException() {
        ChatClient chatClient = chatClientBuilder.build();
        when(chatClient.prompt().user(anyString()).call().content())
                .thenThrow(new RuntimeException("LLM unavailable"));

        assertThatThrownBy(() -> mockMvc.perform(get("/chat").param("message", "hi")))
                .hasStackTraceContaining("LLM unavailable");
    }
}
