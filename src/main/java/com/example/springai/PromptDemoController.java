package com.example.springai;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PromptDemoController {

    private final ChatClient chatClient;
    private final PromptTemplate jokePromptTemplate;
    private final PromptTemplate travelGuidePromptTemplate;
    private final WeatherTools weatherTools;

    public PromptDemoController(ChatClient.Builder chatClientBuilder,
                                PromptTemplate jokePromptTemplate,
                                PromptTemplate travelGuidePromptTemplate,
                                WeatherTools weatherTools) {
        this.chatClient = chatClientBuilder.build();
        this.jokePromptTemplate = jokePromptTemplate;
        this.travelGuidePromptTemplate = travelGuidePromptTemplate;
        this.weatherTools = weatherTools;
    }

    @GetMapping("/joke")
    public String joke(@RequestParam(defaultValue = "funny") String adjective) {
        Prompt prompt = jokePromptTemplate.create(Map.of("adjective", adjective));
        return chatClient.prompt(prompt).call().content();
    }

    @GetMapping("/travel")
    public String travel(@RequestParam String city,
                         @RequestParam(defaultValue = "3") int days) {
        Prompt prompt = travelGuidePromptTemplate.create(Map.of("city", city, "days", days));
        return chatClient.prompt(prompt).call().content();
    }

    @GetMapping("/weather")
    public String weather(@RequestParam String city) {
        return chatClient.prompt()
                .user("What is the current weather in " + city + "?")
                .toolCallbacks(ToolCallbacks.from(weatherTools))
                .call()
                .content();
    }
}
