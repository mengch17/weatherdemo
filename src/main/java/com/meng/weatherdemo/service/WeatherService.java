package com.meng.weatherdemo.service;

import com.meng.weatherdemo.model.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WeatherService {

    private final WebClient webClient;
    private final WeatherCacheService cacheService;

    @Value("${weather.api.url}")
    private String apiUrl;

    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherService(WebClient.Builder webClientBuilder, WeatherCacheService cacheService) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
        this.cacheService = cacheService;
    }

    public Mono<WeatherResponse> getWeatherByZipCode(String zipCode, String countryCode) {
        WeatherResponse cachedResponse = cacheService.getFromCache(zipCode);
        if (cachedResponse != null) {
            System.out.println("Cache hit for zipCode: " + zipCode);
            return Mono.just(cachedResponse);
        }

        String url = String.format("%s/forecast?zip=%s,%s&appid=%s&units=metric", apiUrl, zipCode, countryCode, apiKey);
        System.out.println("Fetching from API: " + url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .doOnNext(response -> {
                    System.out.println("Weather API Response: " + response);
                    cacheService.saveToCache(zipCode, response); // 儲存到 Redis
                })
                .doOnError(error -> System.err.println("Error fetching weather: " + error.getMessage()));
    }
}