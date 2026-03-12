package com.revshop.payment.service;

import com.revshop.payment.model.Wallet;
import com.revshop.payment.model.WalletTransaction;
import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    // SMS OTP logic moved to Notification Service, here we just initiate and verify
    void sendSmsOtp(Long userId, String mobileNumber);

    Wallet verifyMobileKyc(Long userId, String mobileNumber, String otp);

    Wallet getWalletByUser(Long userId);

    String createRazorpayOrder(BigDecimal amount) throws Exception;

    Wallet verifyPaymentAndAddMoney(Long userId, BigDecimal amount, String razorpayPaymentId, String razorpayOrderId,
            String razorpaySignature);
    List<WalletTransaction> getWalletTransactions(Long userId);
    boolean deductMoneyFromWallet(Long userId, BigDecimal amount, String description, String referenceId);
}
