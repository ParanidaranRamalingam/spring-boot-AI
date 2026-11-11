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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ChatViewController.class)
@TestPropertySource(properties = "test.context.id=chat-view-error")
class ChatViewControllerErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS, reset = MockReset.NONE)
    private ChatClient.Builder chatClientBuilder;

    @Test
    void sendMessage_chatClientThrows_addsErrorToModel() throws Exception {
        ChatClient chatClient = chatClientBuilder.build();
        when(chatClient.prompt().user(anyString()).call().content())
                .thenThrow(new RuntimeException("LLM unavailable"));

        mockMvc.perform(post("/").param("message", "hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("chat"))
                .andExpect(model().attribute("userMessage", "hello"))
                .andExpect(model().attribute("error", "LLM unavailable"))
                .andExpect(model().attributeDoesNotExist("response"));
    }
}
