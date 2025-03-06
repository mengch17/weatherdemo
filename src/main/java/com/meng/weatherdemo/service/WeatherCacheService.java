package com.meng.weatherdemo.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meng.weatherdemo.model.WeatherResponse;

/**
 * Reactive Service Layer for Weather Cache using ReactiveRedisTemplate.
 */
@Service
public class WeatherCacheService {

    private static final Logger log = LoggerFactory.getLogger(WeatherCacheService.class);

    @Value("${cache.ttl.minutes}")
    private long CACHE_TTL;

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Save Open Weather API Response to Redis asynchronously.
     * @param cacheKey cacheKey as the key.
     * @param response WeatherResponse to store.
     * @return Mono<Boolean> indicating completion.
     */
    public Mono<Boolean> saveToCache(String cacheKey, WeatherResponse response) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(response))
                .flatMap(json -> reactiveRedisTemplate.opsForValue()
                        // Save to Redis
                        .set(cacheKey, json)
                        // Set TTL in min, use ofSeconds for manual testing.
                        .then(reactiveRedisTemplate.expire(cacheKey, Duration.ofMinutes(CACHE_TTL)))
                )
                .doOnSuccess(v -> log.info("Weather data cached for zipCode: " + cacheKey + " with TTL: " + CACHE_TTL + " minutes"))
                .doOnError(e -> log.error("Error saving to Redis: " + e.getMessage()));
    }

    /**
     * Fetch cached weather data asynchronously.
     * @param cacheKey cacheKey to look up.
     * @return Mono<WeatherResponse> containing the cached data if found.
     */
    public Mono<WeatherResponse> getFromCache(String cacheKey) {
        return reactiveRedisTemplate.opsForValue()
                .get(cacheKey)
                .flatMap(json -> {
                    try {
                        return Mono.just(objectMapper.readValue(json, WeatherResponse.class));
                    } catch (Exception e) {
                        log.error("Error reading from Redis: " + e.getMessage());
                        return Mono.empty();
                    }
                })
                .doOnNext(response -> log.info("Cache found for cacheKey: " + cacheKey))
                .doOnError(e -> log.error("Error fetching from Redis: " + e.getMessage()));
    }
}