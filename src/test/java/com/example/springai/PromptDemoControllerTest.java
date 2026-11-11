package com.example.springai;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PromptDemoController.class)
class PromptDemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS, reset = MockReset.NONE)
    private ChatClient.Builder chatClientBuilder;

    @MockitoBean
    private WeatherTools weatherTools;

    @BeforeEach
    void clearStubInvocations() {
        ChatClient chatClient = chatClientBuilder.build();
        clearInvocations(chatClient, chatClient.prompt());
    }

    @Test
    void joke_adjectiveAndTopicParams_passesBothToTemplate() throws Exception {
        ChatClient chatClient = chatClientBuilder.build();
        when(chatClient.prompt(any(Prompt.class)).call().content()).thenReturn("A silly joke");

        mockMvc.perform(get("/joke").param("adjective", "silly").param("topic", "cats"))
                .andExpect(status().isOk())
                .andExpect(content().string("A silly joke"));

        verify(chatClient).prompt(argThat((Prompt p) -> p != null
                && p.getContents().contains("silly")
                && p.getContents().contains("cats")));
    }

    @Test
    void travelGuide_cityAndDays_bindsBothParams() throws Exception {
        ChatClient chatClient = chatClientBuilder.build();
        when(chatClient.prompt().user(any(Consumer.class)).call().content()).thenReturn("Itinerary");
        clearInvocations(chatClient.prompt());

        mockMvc.perform(get("/travel-guide").param("city", "Paris").param("days", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Itinerary"));

        ArgumentCaptor<Consumer> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(chatClient.prompt()).user(captor.capture());
        ChatClient.PromptUserSpec spec = mock(ChatClient.PromptUserSpec.class, Answers.RETURNS_SELF);
        captor.getValue().accept(spec);
        verify(spec).text(contains("{city}"));
        verify(spec).param("city", "Paris");
        verify(spec).param("days", 5);
    }

    @Test
    void weather_cityParam_callsClientWithToolCallback() throws Exception {
        ChatClient chatClient = chatClientBuilder.build();
        when(chatClient.prompt().user(anyString()).toolCallbacks(any(ToolCallback[].class))
                .call().content()).thenReturn("Sunny in London");

        mockMvc.perform(get("/weather").param("city", "London"))
                .andExpect(status().isOk())
                .andExpect(content().string("Sunny in London"));

        verify(chatClient.prompt()).user("What is the current weather in London?");
    }
}
