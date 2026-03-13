package com.revshop.payment.controller;

import com.revshop.payment.dto.ApiResponse;
import com.revshop.payment.model.Wallet;
import com.revshop.payment.model.WalletTransaction;
import com.revshop.payment.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // In microservices, userId should ideally come from a JWT header populated by the gateway
    // For now, accepting it as a header or param for flexibility
    @PostMapping("/kyc/send-sms")
    public ResponseEntity<ApiResponse<String>> sendSmsOtp(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, String> request) {
        String mobileNumber = request.get("mobileNumber");
        walletService.sendSmsOtp(userId, mobileNumber);
        return ResponseEntity.ok(new ApiResponse<>("SMS OTP sent successfully to " + mobileNumber, null));
    }

    @PostMapping("/kyc/verify")
    public ResponseEntity<ApiResponse<Wallet>> verifyMobileKyc(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, String> request) {
        String mobileNumber = request.get("mobileNumber");
        String otp = request.get("otp");
        Wallet wallet = walletService.verifyMobileKyc(userId, mobileNumber, otp);
        return ResponseEntity.ok(new ApiResponse<>("Wallet created and KYC verified successfully", wallet));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<Wallet>> getWalletBalance(@RequestHeader("X-User-Id") Long userId) {
        try {
            Wallet wallet = walletService.getWalletByUser(userId);
            return ResponseEntity.ok(new ApiResponse<>("Wallet fetched successfully", wallet));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new ApiResponse<>("Wallet not found or not activated", null));
        }
    }

    @PostMapping("/create-razorpay-order")
    public ResponseEntity<ApiResponse<String>> createRazorpayOrder(@RequestBody Map<String, Object> request) {
        try {
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String orderId = walletService.createRazorpayOrder(amount);
            return ResponseEntity.ok(new ApiResponse<>("Razorpay order created", orderId));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Razorpay Order: " + e.getMessage());
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<ApiResponse<Wallet>> verifyPaymentAndAddMoney(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, Object> request) {
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String razorpayPaymentId = (String) request.get("razorpayPaymentId");
        String razorpayOrderId = (String) request.get("razorpayOrderId");
        String razorpaySignature = (String) request.get("razorpaySignature");

        Wallet wallet = walletService.verifyPaymentAndAddMoney(
                userId, amount, razorpayPaymentId, razorpayOrderId, razorpaySignature);
        return ResponseEntity.ok(new ApiResponse<>("Payment verified and money added successfully", wallet));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<WalletTransaction>>> getWalletTransactions(@RequestHeader("X-User-Id") Long userId) {
        List<WalletTransaction> transactions = walletService.getWalletTransactions(userId);
        return ResponseEntity.ok(new ApiResponse<>("Wallet transactions fetched successfully", transactions));
    }
}
