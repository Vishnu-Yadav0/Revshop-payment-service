package com.revshop.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.revshop.payment.model.Payments;
import com.revshop.payment.repository.PaymentsRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class PaymentsService {

    private final PaymentsRepository paymentsRepository;
    // In a real microservice, we'd use a Feign client for notification-service
    // private final NotificationClient notificationClient;

    @Value("${razorpay.key.id:rzp_test_placeholder}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:secret_placeholder}")
    private String razorpayKeySecret;

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

        // Note: In a full microservice setup, we'd use a Feign client for notification-service
        // notificationClient.sendPaymentUpdate(updated.getOrderId(), status.name());
        log.debug("Simulated notification sent for orderId={} with status={}", updated.getOrderId(), status.name());

        return updated;
    }

    public Map<String, Object> createRazorpayOrder(Integer amount, Integer orderId) throws Exception {
        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();
        // amount is already in paise from frontend calculation
        orderRequest.put("amount", amount);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + orderId + "_" + System.currentTimeMillis());

        Order order = razorpay.orders.create(orderRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("razorpayOrderId", order.get("id"));
        response.put("amount", amount);
        response.put("currency", "INR");
        response.put("keyId", razorpayKeyId);
        
        return response;
    }

    @Transactional
    public boolean verifyPaymentAndConfirmOrder(String razorpayOrderId, String razorpayPaymentId, 
                                              String razorpaySignature, Integer internalOrderId) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);

            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            if (!isValid) {
                throw new RuntimeException("Payment Signature Verification Failed");
            }

            // Create or update Payments record
            Payments payment = new Payments();
            payment.setOrderId(internalOrderId);
            payment.setAmount(BigDecimal.ZERO); // Actual amount could be fetched/set if known
            payment.setTransactionId(razorpayPaymentId);
            payment.setPaymentMethod(Payments.PaymentMethod.RAZORPAY);
            payment.setPaymentStatus(Payments.PaymentStatus.SUCCESS);
            payment.setPaymentDate(LocalDateTime.now());
            paymentsRepository.save(payment);

            return true;
        } catch (Exception e) {
            log.error("Error verifying payment: ", e);
            throw new RuntimeException("Error verifying payment: " + e.getMessage());
        }
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
