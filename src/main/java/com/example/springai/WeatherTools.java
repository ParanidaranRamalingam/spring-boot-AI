package com.example.springai;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class WeatherTools {

    @Tool(description = "Get the current weather conditions for a city")
    public String getCurrentWeather(
            @ToolParam(description = "The city to get the weather for") String city) {
        return "Sunny, 24°C, light breeze in " + city;
    }
}
