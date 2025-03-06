package com.meng.weatherdemo.servicetest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.meng.weatherdemo.service.WeatherCacheService;
import com.meng.weatherdemo.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.test.util.ReflectionTestUtils;

import com.meng.weatherdemo.model.WeatherResponse;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class WeatherServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private WeatherCacheService cacheService;

    @InjectMocks
    private WeatherService weatherService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(WeatherResponse.class)).thenReturn(Mono.just(new WeatherResponse()));
        ReflectionTestUtils.setField(weatherService, "apiUrl", "https://api.openweathermap.org/data/2.5");
        ReflectionTestUtils.setField(weatherService, "apiKey", "dummy-api-key");
    }

    @Test
    public void testGetWeatherByZipCode_CacheHit() {
        String zipCode = "94043";
        String countryCode = "US";
        String cacheKey = generateCacheKey(zipCode, countryCode);
        WeatherResponse cachedResponse = new WeatherResponse();

        when(cacheService.getFromCache(cacheKey)).thenReturn(Mono.just(cachedResponse));

        Mono<WeatherResponse> result = weatherService.getWeatherByZipCode(zipCode, countryCode);

        StepVerifier.create(result)
                .expectNext(cachedResponse)
                .verifyComplete();
    }

    private String generateCacheKey(String zipCode, String countryCode) {
        return String.format("%s:%s", zipCode, countryCode);
    }
}