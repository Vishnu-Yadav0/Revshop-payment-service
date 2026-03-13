package com.revshop.payment.service.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.revshop.payment.model.Wallet;
import com.revshop.payment.model.WalletTransaction;
import com.revshop.payment.repository.WalletRepository;
import com.revshop.payment.repository.WalletTransactionRepository;
import com.revshop.payment.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    // In microservices, we'd call notification-service for Twilio/SMS
    // private final NotificationClient notificationClient;

    @Value("${razorpay.key.id:rzp_test_placeholder}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:secret_placeholder}")
    private String razorpayKeySecret;

    public WalletServiceImpl(WalletRepository walletRepository,
                             WalletTransactionRepository walletTransactionRepository) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    @Override
    public void sendSmsOtp(Long userId, String mobileNumber) {
        log.info("Sending SMS OTP to user={} at {}", userId, mobileNumber);
        // Note: In a full microservice setup, this would call notification-service.
        // For now, we simulate the OTP sending.
        log.debug("Simulated OTP sent to {}", mobileNumber);
    }

    @Override
    public Wallet verifyMobileKyc(Long userId, String mobileNumber, String otp) {
        log.info("Verifying mobile KYC for user={} with otp", userId);
        
        // Note: In a full microservice setup, this would call notification-service.
        // For now, we simulate OTP verification.
        log.debug("Simulated OTP verification for mobile: {}, otp: {}", mobileNumber, otp);
        boolean isVerified = true; // Simulating verification for now
        
        if (!isVerified) {
            throw new RuntimeException("Invalid OTP provided.");
        }

        Wallet wallet = walletRepository.findByUserId(userId).orElse(new Wallet());
        wallet.setUserId(userId);
        wallet.setMobileNumber(mobileNumber);
        wallet.setKycVerified(true);
        wallet.setActive(true);

        if (wallet.getBalance() == null) {
            wallet.setBalance(BigDecimal.ZERO);
        }

        return walletRepository.save(wallet);
    }

    @Override
    public Wallet getWalletByUser(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found or not activated"));
    }

    @Override
    public String createRazorpayOrder(BigDecimal amount) throws Exception {
        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();
        // sum must be an integer in paise
        orderRequest.put("amount", amount.multiply(new BigDecimal(100)).intValue());
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "wallet_txn_" + System.currentTimeMillis());

        Order order = razorpay.orders.create(orderRequest);
        return order.get("id");
    }

    @Override
    public Wallet verifyPaymentAndAddMoney(Long userId, BigDecimal amount, String razorpayPaymentId,
            String razorpayOrderId, String razorpaySignature) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);

            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            if (!isValid) {
                throw new RuntimeException("Payment Signature Verification Failed");
            }

            Wallet wallet = getWalletByUser(userId);
            wallet.setBalance(wallet.getBalance().add(amount));
            walletRepository.save(wallet);

            WalletTransaction transaction = WalletTransaction.builder()
                    .wallet(wallet)
                    .amount(amount)
                    .transactionType(WalletTransaction.TransactionType.CREDIT)
                    .description("Added funds via Razorpay")
                    .referenceId(razorpayPaymentId)
                    .build();
            walletTransactionRepository.save(transaction);

            return wallet;

        } catch (Exception e) {
            throw new RuntimeException("Error verifying payment: " + e.getMessage());
        }
    }

    @Override
    public boolean deductMoneyFromWallet(Long userId, BigDecimal amount, String description, String referenceId) {
        Wallet wallet = getWalletByUser(userId);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient wallet balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction(
                null,
                wallet,
                amount,
                WalletTransaction.TransactionType.DEBIT,
                description,
                referenceId,
                java.time.LocalDateTime.now()
        );
        walletTransactionRepository.save(transaction);

        return true;
    }

    @Override
    public List<WalletTransaction> getWalletTransactions(Long userId) {
        Wallet wallet = getWalletByUser(userId);
        return walletTransactionRepository.findByWallet_WalletIdOrderByCreatedAtDesc(wallet.getWalletId());
    }
}
