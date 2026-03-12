package com.revshop.payment.controller;

import com.revshop.payment.model.Payments;
import com.revshop.payment.service.PaymentsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentsController.class)
@ActiveProfiles("test")
public class PaymentsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentsService paymentsService;

    @Test
    void createPayment_ReturnsCreated() throws Exception {
        Payments payment = Payments.builder()
                .paymentId(1)
                .orderId(101)
                .amount(new BigDecimal("100.00"))
                .build();

        when(paymentsService.createPayment(any(Payments.class), eq(101))).thenReturn(payment);

        mockMvc.perform(post("/api/payments")
                        .param("orderId", "101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100.00, \"paymentMethod\": \"CREDIT_CARD\", \"paymentStatus\": \"PENDING\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.paymentId").value(1))
                .andExpect(jsonPath("$.data.orderId").value(101));
    }

    @Test
    void getPaymentById_ReturnsPayment() throws Exception {
        Payments payment = Payments.builder()
                .paymentId(1)
                .amount(new BigDecimal("100.00"))
                .build();

        when(paymentsService.getPaymentById(1)).thenReturn(Optional.of(payment));

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentId").value(1));
    }
}
