package com.revshop.payment.controller;

import com.revshop.payment.dto.ApiResponse;
import com.revshop.payment.dto.PaymentsDTO;
import com.revshop.payment.model.Payments;
import com.revshop.payment.service.PaymentsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@Slf4j
public class PaymentsController {

    private final PaymentsService paymentsService;

    public PaymentsController(PaymentsService paymentsService) {
        this.paymentsService = paymentsService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentsDTO>> createPayment(@RequestParam Integer orderId,
            @RequestBody PaymentsDTO paymentsDTO) {

        log.info("POST /api/payments - orderId={}", orderId);
        Payments payment = convertToEntity(paymentsDTO);
        Payments savedPayment = paymentsService.createPayment(payment, orderId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Payment created successfully",
                        convertToDTO(savedPayment)));
    }

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createRazorpayOrder(@RequestBody Map<String, Object> request) {
        try {
            Integer amount = Integer.valueOf(request.get("amount").toString());
            Integer orderId = Integer.valueOf(request.get("orderId").toString());
            Map<String, Object> orderDetails = paymentsService.createRazorpayOrder(amount, orderId);
            return ResponseEntity.ok(new ApiResponse<>("Razorpay order created", orderDetails));
        } catch (Exception e) {
            log.error("Failed to create Razorpay Order: {}", e.getMessage());
            throw new RuntimeException("Failed to create Razorpay Order: " + e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyPayment(@RequestBody Map<String, Object> request) {
        try {
            String razorpayOrderId = (String) request.get("razorpayOrderId");
            String razorpayPaymentId = (String) request.get("razorpayPaymentId");
            String razorpaySignature = (String) request.get("razorpaySignature");
            Integer internalOrderId = Integer.valueOf(request.get("internalOrderId").toString());

            boolean isVerified = paymentsService.verifyPaymentAndConfirmOrder(
                    razorpayOrderId, razorpayPaymentId, razorpaySignature, internalOrderId);
            
            return ResponseEntity.ok(new ApiResponse<>("Payment verified successfully", isVerified));
        } catch (Exception e) {
            log.error("Payment verification failed: {}", e.getMessage());
            throw new RuntimeException("Payment verification failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentsDTO>> getPaymentById(
            @PathVariable Integer id) {

        log.info("GET /api/payments/{}", id);
        Payments payment = paymentsService.getPaymentById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return ResponseEntity.ok(
                new ApiResponse<>("Payment fetched successfully",
                        convertToDTO(payment)));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<PaymentsDTO>>> getPaymentByOrderId(
            @PathVariable Integer orderId) {

        log.info("GET /api/payments/order/{}", orderId);
        List<PaymentsDTO> payments = paymentsService.getPaymentByOrderId(orderId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ApiResponse<>("Payments fetched successfully", payments));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentsDTO>>> getAllPayments() {

        log.info("GET /api/payments");
        List<PaymentsDTO> payments = paymentsService.getAllPayments()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ApiResponse<>("Payments fetched successfully", payments));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentsDTO> updatePaymentStatus(@PathVariable Integer id,
            @RequestParam String status) {
        log.info("PUT /api/payments/{}/status - status={}", id, status);
        Payments.PaymentStatus paymentStatus = Payments.PaymentStatus.valueOf(status);
        Payments updatedPayment = paymentsService.updatePaymentStatus(id, paymentStatus);
        return ResponseEntity.ok(convertToDTO(updatedPayment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(
            @PathVariable Integer id) {

        log.info("DELETE /api/payments/{}", id);
        paymentsService.deletePayment(id);

        return ResponseEntity.ok(
                new ApiResponse<>("Payment deleted successfully", null));
    }

    private PaymentsDTO convertToDTO(Payments payment) {
        PaymentsDTO dto = new PaymentsDTO();
        dto.setPaymentId(payment.getPaymentId());
        dto.setAmount(payment.getAmount());
        dto.setTransactionId(payment.getTransactionId());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        dto.setOrderId(payment.getOrderId());

        if (payment.getPaymentMethod() != null)
            dto.setPaymentMethod(payment.getPaymentMethod().name());

        if (payment.getPaymentStatus() != null)
            dto.setPaymentStatus(payment.getPaymentStatus().name());

        return dto;
    }

    private Payments convertToEntity(PaymentsDTO dto) {
        Payments payment = new Payments();
        payment.setAmount(dto.getAmount());
        payment.setTransactionId(dto.getTransactionId());
        payment.setPaymentDate(dto.getPaymentDate());

        if (dto.getPaymentMethod() != null)
            payment.setPaymentMethod(Payments.PaymentMethod.valueOf(dto.getPaymentMethod()));

        if (dto.getPaymentStatus() != null)
            payment.setPaymentStatus(Payments.PaymentStatus.valueOf(dto.getPaymentStatus()));

        return payment;
    }
}
