package com.revshop.payment.service;

import com.revshop.payment.model.Wallet;
import com.revshop.payment.model.WalletTransaction;
import com.revshop.payment.repository.WalletRepository;
import com.revshop.payment.repository.WalletTransactionRepository;
import com.revshop.payment.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = Wallet.builder()
                .walletId(1L)
                .userId(1L)
                .balance(new BigDecimal("500.00"))
                .kycVerified(true)
                .isActive(true)
                .build();
    }

    @Test
    void getWalletByUser_Success() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        Wallet foundWallet = walletService.getWalletByUser(1L);

        assertNotNull(foundWallet);
        assertEquals(1L, foundWallet.getUserId());
    }

    @Test
    void deductMoneyFromWallet_Success() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        boolean result = walletService.deductMoneyFromWallet(1L, new BigDecimal("100.00"), "Test deduction", "REF123");

        assertTrue(result);
        assertEquals(new BigDecimal("400.00"), wallet.getBalance());
        verify(walletTransactionRepository, times(1)).save(any(WalletTransaction.class));
    }

    @Test
    void deductMoneyFromWallet_InsufficientBalance() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        assertThrows(RuntimeException.class, () -> {
            walletService.deductMoneyFromWallet(1L, new BigDecimal("600.00"), "Test deduction", "REF123");
        });
    }
}
