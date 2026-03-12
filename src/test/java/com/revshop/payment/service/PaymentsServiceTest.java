package com.revshop.payment.service;

import com.revshop.payment.model.Payments;
import com.revshop.payment.repository.PaymentsRepository;
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
public class PaymentsServiceTest {

    @Mock
    private PaymentsRepository paymentsRepository;

    @InjectMocks
    private PaymentsService paymentsService;

    private Payments payment;

    @BeforeEach
    void setUp() {
        payment = Payments.builder()
                .paymentId(1)
                .orderId(101)
                .amount(new BigDecimal("1000.00"))
                .paymentMethod(Payments.PaymentMethod.CREDIT_CARD)
                .paymentStatus(Payments.PaymentStatus.PENDING)
                .build();
    }

    @Test
    void createPayment_Success() {
        when(paymentsRepository.save(any(Payments.class))).thenReturn(payment);

        Payments savedPayment = paymentsService.createPayment(payment, 101);

        assertNotNull(savedPayment);
        assertEquals(101, savedPayment.getOrderId());
        verify(paymentsRepository, times(1)).save(any(Payments.class));
    }

    @Test
    void getPaymentById_Success() {
        when(paymentsRepository.findById(1)).thenReturn(Optional.of(payment));

        Optional<Payments> foundPayment = paymentsService.getPaymentById(1);

        assertTrue(foundPayment.isPresent());
        assertEquals(1, foundPayment.get().getPaymentId());
    }

    @Test
    void updatePaymentStatus_Success() {
        when(paymentsRepository.findById(1)).thenReturn(Optional.of(payment));
        when(paymentsRepository.save(any(Payments.class))).thenReturn(payment);

        Payments updatedPayment = paymentsService.updatePaymentStatus(1, Payments.PaymentStatus.SUCCESS);

        assertEquals(Payments.PaymentStatus.SUCCESS, updatedPayment.getPaymentStatus());
        verify(paymentsRepository, times(1)).save(payment);
    }
}
