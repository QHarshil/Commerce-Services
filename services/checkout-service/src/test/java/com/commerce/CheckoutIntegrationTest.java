package com.commerce;

import com.commerce.dto.CheckoutRequest;
import com.commerce.dto.CheckoutResponse;
import com.commerce.dto.CheckoutItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for checkout service.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
public class CheckoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCheckoutProcess() throws Exception {
        // Create checkout request
        CheckoutRequest request = new CheckoutRequest();
        request.setCustomerId(UUID.randomUUID());
        request.setPaymentMethod("CREDIT_CARD");
        request.setItems(Arrays.asList(
            new CheckoutItem(UUID.randomUUID(), 2),
            new CheckoutItem(UUID.randomUUID(), 1)
        ));

        // Perform checkout
        mockMvc.perform(post("/api/v1/checkout/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").exists())
                .andExpect(jsonPath("$.processingTimeMs").exists());
    }

    @Test
    public void testHealthCheck() throws Exception {
        mockMvc.perform(post("/api/v1/checkout/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
