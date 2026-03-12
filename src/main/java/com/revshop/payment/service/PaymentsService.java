package com.revshop.payment.service;

import com.revshop.payment.model.Payments;
import com.revshop.payment.repository.PaymentsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PaymentsService {

    private final PaymentsRepository paymentsRepository;
    // In a real microservice, we'd use a Feign client for notification-service
    // private final NotificationClient notificationClient;

    public PaymentsService(PaymentsRepository paymentsRepository) {
        this.paymentsRepository = paymentsRepository;
    }

    @Transactional
    public Payments createPayment(Payments payment, Integer orderId) {
        log.info("Creating payment for orderId={}", orderId);
        payment.setOrderId(orderId);
        
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }

        if (payment.getPaymentStatus() == null) {
            payment.setPaymentStatus(Payments.PaymentStatus.PENDING);
        }

        return paymentsRepository.save(payment);
    }

    @Transactional
    public Payments updatePaymentStatus(Integer paymentId, Payments.PaymentStatus status) {
        log.info("Updating payment id={} status={}", paymentId, status);

        Payments payment = paymentsRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setPaymentStatus(status);
        Payments updated = paymentsRepository.save(payment);

        // TODO: Call notification-service here to notify User
        // notificationClient.sendPaymentUpdate(updated.getOrderId(), status.name());

        return updated;
    }

    public Optional<Payments> getPaymentById(Integer paymentId) {
        return paymentsRepository.findById(paymentId);
    }

    public List<Payments> getPaymentByOrderId(Integer orderId) {
        return paymentsRepository.findByOrderId(orderId);
    }

    public List<Payments> getAllPayments() {
        return paymentsRepository.findAll();
    }

    @Transactional
    public void deletePayment(Integer paymentId) {
        log.info("Deleting payment id={}", paymentId);
        if (!paymentsRepository.existsById(paymentId)) {
            throw new RuntimeException("Payment not found");
        }
        paymentsRepository.deleteById(paymentId);
    }

    @Transactional
    public Payments savePayment(Payments payment) {
        log.info("Saving payment id={}", payment.getPaymentId());
        return paymentsRepository.save(payment);
    }
}
