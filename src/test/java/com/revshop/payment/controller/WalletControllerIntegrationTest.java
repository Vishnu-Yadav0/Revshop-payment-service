package com.revshop.payment.controller;

import com.revshop.payment.model.Wallet;
import com.revshop.payment.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
@ActiveProfiles("test")
public class WalletControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletService walletService;

    @Test
    void getWalletBalance_ReturnsBalance() throws Exception {
        Wallet wallet = Wallet.builder()
                .walletId(1L)
                .userId(1L)
                .balance(new BigDecimal("500.00"))
                .kycVerified(true)
                .build();

        when(walletService.getWalletByUser(1L)).thenReturn(wallet);

        mockMvc.perform(get("/api/wallets/balance")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(500.00));
    }

    @Test
    void sendSmsOtp_ReturnsOk() throws Exception {
        mockMvc.perform(post("/api/wallets/kyc/send-sms")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mobileNumber\": \"1234567890\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SMS OTP sent successfully to 1234567890"));
    }
}
