package com.example.springai;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PromptDemoController {

    private final ChatClient chatClient;
    private final WeatherTools weatherTools;

    public PromptDemoController(ChatClient.Builder chatClientBuilder,
                                WeatherTools weatherTools) {
        this.chatClient = chatClientBuilder.build();
        this.weatherTools = weatherTools;
    }

    /**
     * GET /joke?adjective=...&topic=...
     * {@link PromptTemplate} is a reusable template object: create() substitutes
     * the {adjective} and {topic} placeholders with values from the map and
     * produces a Prompt that ChatClient can send directly.
     */
    @GetMapping("/joke")
    public String joke(
            @RequestParam(defaultValue = "funny") String adjective,
            @RequestParam(defaultValue = "programming") String topic) {
        PromptTemplate promptTemplate = new PromptTemplate(
                "Tell me a {adjective} joke about {topic}.");
        return chatClient.prompt(promptTemplate.create(
                        Map.of("adjective", adjective, "topic", topic)))
                .call()
                .content();
    }

    /**
     * GET /travel-guide?city=...&days=...
     * Same templating idea without a PromptTemplate object: the user() lambda
     * takes the template text plus .param() bindings, and ChatClient resolves
     * the {city}/{days} placeholders internally.
     */
    @GetMapping("/travel-guide")
    public String travelGuide(
            @RequestParam(defaultValue = "Paris") String city,
            @RequestParam(defaultValue = "3") int days) {
        return chatClient.prompt()
                .user(u -> u.text("""
                                Create a short itinerary for a {days}-day trip to {city}.
                                Suggest one highlight per day.
                                """)
                        .param("city", city)
                        .param("days", days))
                .call()
                .content();
    }

    /**
     * GET /weather?city=...
     * Tool calling: the model invokes WeatherTools.getCurrentWeather (a real
     * Open-Meteo lookup) and formats the result.
     */
    @GetMapping("/weather")
    public String weather(@RequestParam String city) {
        return chatClient.prompt()
                .user("What is the current weather in " + city + "?")
                .toolCallbacks(ToolCallbacks.from(weatherTools))
                .call()
                .content();
    }
}
