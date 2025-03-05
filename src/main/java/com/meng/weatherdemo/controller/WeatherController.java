package com.meng.weatherdemo.controller;

import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.meng.weatherdemo.model.WeatherResponse;
import com.meng.weatherdemo.service.WeatherService;

/**
 * Control Layer for Weather Demo.
 */
@RestController
@RequestMapping("v1/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * OpenWeather API - Get Current Weather by ZIP Code.
     * https://openweathermap.org/current
     *
     * @param zipCode: zip code, example: 94043
     * @param countryCode: Country, example: US
     * @return
     */
    @GetMapping("/{zipCode}/{countryCode}")
    public Mono<ResponseEntity<WeatherResponse>> getWeather(@PathVariable String zipCode, @PathVariable String countryCode) {
        return weatherService.getWeatherByZipCode(zipCode, countryCode)
                .map(weather -> ResponseEntity.ok().body(weather))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Health check.
     */
    @GetMapping("health/full")
    public ResponseEntity<String> healthCheck() {
        return new ResponseEntity<>("Health Check Ok", HttpStatus.OK);
    }
}