package com.meng.weatherdemo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherResponse {

    private City city;
    private List<WeatherData> list;

    @Data
    public static class City {
        private String name;
        private String country;
    }

    @Data
    public static class WeatherData {
        private long dt;
        private Main main;
        private List<Weather> weather;
    }

    @Data
    public static class Main {
        private double temp;
        @JsonProperty("feels_like")
        private double feelsLike;
        @JsonProperty("temp_min")
        private double tempMin;
        @JsonProperty("temp_max")
        private double tempMax;
        private int pressure;
        private int humidity;
    }

    @Data
    public static class Weather {
        private String description;
        private String icon;
    }
}