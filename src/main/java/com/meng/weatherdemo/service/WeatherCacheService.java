package com.meng.weatherdemo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meng.weatherdemo.model.WeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class WeatherCacheService {
    // 30 mins
    private static final long CACHE_TTL = 30;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void saveToCache(String zipCode, WeatherResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(zipCode, json, CACHE_TTL, TimeUnit.MINUTES);
            System.out.println("Weather data cached for zipCode: " + zipCode);
        } catch (Exception e) {
            System.err.println("Error saving to Redis: " + e.getMessage());
        }
    }

    public WeatherResponse getFromCache(String zipCode) {
        try {
            String json = redisTemplate.opsForValue().get(zipCode);
            if (json != null) {
                System.out.println("Cache found for zipCode: " + zipCode);
                return objectMapper.readValue(json, WeatherResponse.class);
            }
        } catch (Exception e) {
            System.err.println("Error reading from Redis: " + e.getMessage());
        }
        return null;
    }
}