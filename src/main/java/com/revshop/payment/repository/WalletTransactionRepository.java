package com.revshop.payment.repository;

import com.revshop.payment.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByWallet_WalletIdOrderByCreatedAtDesc(Long walletId);
}
