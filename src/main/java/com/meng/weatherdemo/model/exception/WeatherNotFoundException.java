package com.meng.weatherdemo.model.exception;


import com.meng.weatherdemo.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WeatherNotFoundException extends RuntimeException {

    private static final Logger log = LoggerFactory.getLogger(WeatherNotFoundException.class);

    private final String zipCode;
    private final String countryCode;

    public WeatherNotFoundException(String zipCode, String countryCode, String message) {
        super("Weather data not found for zipCode: " + zipCode + ", countryCode: " + countryCode + ". " + message);
        this.zipCode = zipCode;
        this.countryCode = countryCode;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getCountryCode() {
        return countryCode;
    }
}