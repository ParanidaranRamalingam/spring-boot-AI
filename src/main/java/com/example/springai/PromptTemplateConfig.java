package com.example.springai;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PromptTemplateConfig {

    @Bean
    PromptTemplate jokePromptTemplate() {
        return new PromptTemplate("Tell me a {adjective} joke.");
    }

    @Bean
    PromptTemplate travelGuidePromptTemplate() {
        return new PromptTemplate("""
                You are an expert travel guide. Create a concise {days}-day itinerary for {city}.
                For each day suggest morning, afternoon and evening activities,
                plus one local food recommendation.
                """);
    }
}
