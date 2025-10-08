package com.commerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for checkout service.
 */
@Configuration
@EnableRetry
public class CheckoutConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
