package com.meng.weatherdemo.service;

import com.meng.weatherdemo.model.exception.WeatherNotFoundException;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.meng.weatherdemo.model.WeatherResponse;

/**
 * Weather Service Layer with ReactiveRedisTemplate cache.
 */
@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private final WebClient webClient;
    private final WeatherCacheService cacheService;

    @Value("${weather.api.url}")
    private String apiUrl;

    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherService(WebClient webClient, WeatherCacheService cacheService) {
        this.webClient = webClient;
        this.cacheService = cacheService;
    }

    /**
     * Get Weather Data with input Zip Code & Country Code using cache first, then fetch from API if not found.
     * @param zipCode The zip code to query.
     * @param countryCode The country code to query.
     * @return Mono<WeatherResponse> with cached or API response.
     */
    public Mono<WeatherResponse> getWeatherByZipCode(String zipCode, String countryCode) {

        // First try getting from cache.
        return cacheService.getFromCache(generateCacheKey(zipCode, countryCode))
                .flatMap(cachedResponse -> {
                    log.info("Cache hit for zipCode: " + zipCode + ", " + countryCode);
                    // If found in cache, return it.
                    return Mono.just(cachedResponse);
                })
                // If cache is empty, fetch from API.
                .switchIfEmpty(fetchWeatherFromApi(zipCode, countryCode));
    }

    /**
     * Fetch weather data from OpenWeather API and cache the response.
     * @param zipCode The zip code to query.
     * @param countryCode The country code to query.
     * @return Mono<WeatherResponse> from API.
     */
    private Mono<WeatherResponse> fetchWeatherFromApi(String zipCode, String countryCode) {
        String url = String.format("%s/forecast?zip=%s,%s&appid=%s&units=metric", apiUrl, zipCode, countryCode, apiKey);
        log.info("Fetching from API: " + url);
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .flatMap(response -> {
                    log.info("Weather API Response: " + response);
                    // Cache the response & return it.
                    return cacheService.saveToCache(zipCode, response).thenReturn(response);
                })
                .doOnError(error -> log.error("Error fetching weather: " + error.getMessage()));
    }

    private String generateCacheKey(String zipCode, String countryCode) {
        return String.format("%s:%s", zipCode, countryCode);
    }
}