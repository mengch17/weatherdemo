package com.meng.weatherdemo.controllertest;

import com.meng.weatherdemo.controller.WeatherController;
import com.meng.weatherdemo.model.WeatherResponse;
import com.meng.weatherdemo.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WeatherControllerTest {

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private WeatherController weatherController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test case: getWeather() should return 200 OK when weather data is found.
     */
    @Test
    void testGetWeather_Success() {
        // Mock & Given
        WeatherResponse mockResponse = new WeatherResponse();
        mockResponse.setCity(new WeatherResponse.City());
        mockResponse.getCity().setName("Mountain View");
        mockResponse.getCity().setCountry("US");

        when(weatherService.getWeatherByZipCode("94043", "US"))
                .thenReturn(Mono.just(mockResponse));

        // When
        Mono<ResponseEntity<WeatherResponse>> result = weatherController.getWeather("94043", "US");

        //Then & Assertion
        result.subscribe(response -> {
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Mountain View", response.getBody().getCity().getName());
            assertEquals("US", response.getBody().getCity().getCountry());
        });

        // Verify
        verify(weatherService, times(1)).getWeatherByZipCode("94043", "US");
    }

    /**
     * Test case: getWeather() should return 404 NOT FOUND when no weather data is found.
     */
    @Test
    void testGetWeather_NotFound() {
        // Mock & Given
        when(weatherService.getWeatherByZipCode("99999", "XX"))
                .thenReturn(Mono.empty());

        // When
        Mono<ResponseEntity<WeatherResponse>> result = weatherController.getWeather("99999", "XX");

        //Then & Assertion
        result.subscribe(response -> {
            assertNotNull(response);
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(response.getBody());
        });

        // Verify
        verify(weatherService, times(1)).getWeatherByZipCode("99999", "XX");
    }
}