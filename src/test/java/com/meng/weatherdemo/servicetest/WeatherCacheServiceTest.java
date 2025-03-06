package com.meng.weatherdemo.servicetest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meng.weatherdemo.model.WeatherResponse;
import com.meng.weatherdemo.service.WeatherCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class WeatherCacheServiceTest {

    @Mock
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WeatherCacheService weatherCacheService;

    private final String cacheKey = "94043";
    private WeatherResponse mockWeatherResponse;
    private String mockJson;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        MockitoAnnotations.openMocks(this);
        mockWeatherResponse = new WeatherResponse();
        mockWeatherResponse.setCity(new WeatherResponse.City());
        mockWeatherResponse.getCity().setName("Mountain View");
        mockWeatherResponse.getCity().setCountry("US");
        mockJson = "{\"city\":{\"name\":\"Mountain View\",\"country\":\"US\"}}";
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    /**
     * Test case: saveToCache() should store weather data in Redis and return success.
     */
    @Test
    void testSaveToCache_Success() throws JsonProcessingException {
        // Mock & Given
        when(objectMapper.writeValueAsString(mockWeatherResponse)).thenReturn(mockJson);
        when(valueOperations.set(cacheKey, mockJson)).thenReturn(Mono.just(true));
        when(reactiveRedisTemplate.expire(eq(cacheKey), any())).thenReturn(Mono.just(true));

        // When
        StepVerifier.create(weatherCacheService.saveToCache(cacheKey, mockWeatherResponse))
                .expectNext(true)
                .verifyComplete();

        // Then & Verify
        verify(valueOperations, times(1)).set(cacheKey, mockJson);
        verify(reactiveRedisTemplate, times(1)).expire(eq(cacheKey), any());
    }

    /**
     * Test case: saveToCache() should log an error when serialization fails.
     */
    @Test
    void testSaveToCache_Failure() throws JsonProcessingException {
        // Mock & Given
        when(objectMapper.writeValueAsString(mockWeatherResponse)).thenThrow(new JsonProcessingException("Serialization Error") {});

        // When
        StepVerifier.create(weatherCacheService.saveToCache(cacheKey, mockWeatherResponse))
                .expectErrorMatches(throwable -> throwable.getMessage().contains("Serialization Error"))
                .verify();

        // Then & Verify
        verify(valueOperations, never()).set(any(), any());
    }

    /**
     * Test case: getFromCache() should return WeatherResponse when data exists in Redis.
     */
    @Test
    void testGetFromCache_Success() throws Exception {
        // Mock & Given
        when(valueOperations.get(cacheKey)).thenReturn(Mono.just(mockJson));
        when(objectMapper.readValue(mockJson, WeatherResponse.class)).thenReturn(mockWeatherResponse);

        // When
        StepVerifier.create(weatherCacheService.getFromCache(cacheKey))
                .expectNext(mockWeatherResponse)
                .verifyComplete();

        // Then & Verify
        verify(valueOperations, times(1)).get(cacheKey);
    }

    /**
     * Test case: getFromCache() should return empty when data does not exist.
     */
    @Test
    void testGetFromCache_NotFound() {
        // Mock & Given
        when(valueOperations.get(cacheKey)).thenReturn(Mono.empty());

        // When
        StepVerifier.create(weatherCacheService.getFromCache(cacheKey))
                .verifyComplete();

        // Then & Verify
        verify(valueOperations, times(1)).get(cacheKey);
    }

    /**
     * Test case: getFromCache() should return empty when JSON deserialization fails.
     */
    @Test
    void testGetFromCache_DeserializationError() throws Exception {
        // Mock & Given
        when(valueOperations.get(cacheKey)).thenReturn(Mono.just(mockJson));
        when(objectMapper.readValue(mockJson, WeatherResponse.class)).thenThrow(new RuntimeException("Deserialization Error"));

        // When
        StepVerifier.create(weatherCacheService.getFromCache(cacheKey))
                .verifyComplete();

        // Then & Verify
        verify(valueOperations, times(1)).get(cacheKey);
    }
}