package com.example.springai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WeatherTools {

    private final RestClient restClient = RestClient.create();

    @Tool(description = "Get the current weather for a city")
    public Object getCurrentWeather(
            @ToolParam(description = "city name, e.g. Paris or Tokyo") String city) {
        GeoResults geo = restClient.get()
                .uri("https://geocoding-api.open-meteo.com/v1/search?name={name}&count=1&language=en&format=json",
                        city)
                .retrieve()
                .body(GeoResults.class);
        if (geo == null || geo.results() == null || geo.results().isEmpty()) {
            return "Could not find a city named '" + city + "'.";
        }
        GeoResult place = geo.results().getFirst();

        Forecast forecast = restClient.get()
                .uri("https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}"
                                + "&current=temperature_2m,relative_humidity_2m,wind_speed_10m&timezone=auto",
                        place.latitude(), place.longitude())
                .retrieve()
                .body(Forecast.class);
        if (forecast == null || forecast.current() == null) {
            return "Weather data unavailable for " + place.name() + ".";
        }
        Current c = forecast.current();
        return new WeatherReport(place.name(), place.country(), c.temperature(),
                c.humidity(), c.windSpeed());
    }

    record GeoResults(List<GeoResult> results) {
    }

    record GeoResult(String name, String country, double latitude, double longitude) {
    }

    record Forecast(Current current) {
    }

    record Current(@JsonProperty("temperature_2m") double temperature,
                   @JsonProperty("relative_humidity_2m") int humidity,
                   @JsonProperty("wind_speed_10m") double windSpeed) {
    }

    record WeatherReport(String city, String country, double temperatureCelsius,
                         int humidityPercent, double windSpeedKmh) {
    }
}
