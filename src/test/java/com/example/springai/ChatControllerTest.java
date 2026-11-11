package com.example.springai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS, reset = MockReset.NONE)
    private ChatClient.Builder chatClientBuilder;

    @BeforeEach
    void clearStubInvocations() {
        clearInvocations(chatClientBuilder.build().prompt());
    }

    @Test
    void chat_messageParamProvided_returnsChatClientContent() throws Exception {
        ChatClient chatClient = chatClientBuilder.build();
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn("Spring Boot rocks");

        mockMvc.perform(get("/chat").param("message", "hi"))
                .andExpect(status().isOk())
                .andExpect(content().string("Spring Boot rocks"));

        verify(chatClient.prompt()).user("hi");
    }

    @Test
    void chat_noMessageParam_usesDefaultMessage() throws Exception {
        ChatClient chatClient = chatClientBuilder.build();
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn("Fun fact");

        mockMvc.perform(get("/chat"))
                .andExpect(status().isOk())
                .andExpect(content().string("Fun fact"));

        verify(chatClient.prompt()).user("Tell me a fun fact about Spring Boot");
    }

    @Test
    void chat_blankMessageParam_returnsChatClientContent() throws Exception {
        ChatClient chatClient = chatClientBuilder.build();
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn("Blank reply");

        mockMvc.perform(get("/chat").param("message", ""))
                .andExpect(status().isOk())
                .andExpect(content().string("Blank reply"));

        verify(chatClient.prompt()).user("");
    }
}
