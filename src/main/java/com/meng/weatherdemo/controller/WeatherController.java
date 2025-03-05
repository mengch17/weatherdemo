package com.meng.weatherdemo.controller;

import com.meng.weatherdemo.model.WeatherResponse;
import com.meng.weatherdemo.service.WeatherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * OpenWeather API - Get Current Weather by ZIP Code
 * https://openweathermap.org/current
 */
@RestController
@RequestMapping("v1/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/{zipCode}/{countryCode}")
    public Mono<ResponseEntity<WeatherResponse>> getWeather(@PathVariable String zipCode, @PathVariable String countryCode) {
        return weatherService.getWeatherByZipCode(zipCode, countryCode)
                .map(weather -> ResponseEntity.ok().body(weather))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("health/full")
    public ResponseEntity<String> healthCheck() {
        return new ResponseEntity<>("Health Check Ok", HttpStatus.OK);
    }
}